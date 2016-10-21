package eu.unifiedviews.plugins.transformer.relationaltordf;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dataunit.relational.RelationalDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.rdf.RdfDataUnitUtils;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultToleranceUtils;
import eu.unifiedviews.helpers.dpu.extension.rdf.simple.WritableSimpleRdf;
import eu.unifiedviews.plugins.transformer.relationaltordf.mapper.TableToRdf;
import eu.unifiedviews.helpers.dpu.extension.rdf.validation.RdfValidation;


@DPU.AsTransformer
public class RelationalToRdf extends AbstractDpu<RelationalToRdfConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(RelationalToRdf.class);

    @DataUnit.AsInput(name = "tablesInput")
    public RelationalDataUnit tablesInput;

    @DataUnit.AsOutput(name = "rdfOutput")
    public WritableRDFDataUnit rdfOutput;

    @ExtensionInitializer.Init(param = "rdfOutput")
    public WritableSimpleRdf rdfTableWrap;

    @ExtensionInitializer.Init
    public RdfValidation rdfValidation;

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    public RelationalToRdf() {
        super(RelationalToRdfVaadinDialog.class, ConfigHistory.noHistory(RelationalToRdfConfig_V1.class));
    }

    @Override
    protected void innerExecute() throws DPUException {
        String shortMessage = this.getClass().getSimpleName() + " starting.";
        String longMessage = String.format("Configuration: base URI resource: %s, map all columns: %s, generate row columns: %s",
                this.config.getBaseURI(), this.config.isGenerateNew(), this.config.isGenerateRowTriple());
        LOG.info(shortMessage + " : " + longMessage);

        // Prepare tabular converter.
        final TableToRdf tableToRdf = new TableToRdf(
                this.config.getTableToRdfConfig(),
                this.rdfTableWrap,
                this.rdfTableWrap.getValueFactory());

        RelationalToRdfConverter converter = new RelationalToRdfConverter(tableToRdf, this.ctx);

        LOG.debug("Going to get input database tables");
        final List<RelationalDataUnit.Entry> tables = FaultToleranceUtils.getEntries(this.faultTolerance, this.tablesInput,
                RelationalDataUnit.Entry.class);
        LOG.debug("Input database tables successfuly obtained");

        Connection conn = null;
        try {
            conn = this.tablesInput.getDatabaseConnection();
            for (final RelationalDataUnit.Entry entry : tables) {
                if (this.ctx.canceled()) {
                    throw ContextUtils.dpuExceptionCancelled(this.ctx);
                }
                LOG.info("Processing table {}", entry.getTableName());

                LOG.debug("Going to create output graph");
                final RDFDataUnit.Entry entryOutput = this.faultTolerance.execute(new FaultTolerance.ActionReturn<RDFDataUnit.Entry>() {

                    @Override
                    public RDFDataUnit.Entry action() throws Exception {
                        return RdfDataUnitUtils.addGraph(RelationalToRdf.this.rdfOutput, entry.getSymbolicName());
                    }
                });
                this.rdfTableWrap.setOutput(entryOutput);
                LOG.debug("Output RDF graph successfuly created");

                final String symbolicName = this.faultTolerance.execute(new FaultTolerance.ActionReturn<String>() {

                    @Override
                    public String action() throws Exception {
                        return entry.getSymbolicName();
                    }
                });
                LOG.debug("Symbolic name of processed table is {}", symbolicName);

                ContextUtils.sendShortInfo(this.ctx, "dpu.execution.table.processing", symbolicName);
                try {
                    // If set add subject for the whole table.
                    if (this.config.isUseTableSubject()) {
                        LOG.debug("Use table subject option is used, preparing subject for table");
                        // Prepare subject for table.
                        // TODO: We can use better subject here!
                        final URI tableURI = this.faultTolerance.execute(new FaultTolerance.ActionReturn<URI>() {

                            @Override
                            public URI action() throws Exception {
                                return RelationalToRdf.this.rdfTableWrap.getValueFactory().createURI(entry.getTableName());
                            }
                        });
                        // Set as a table subject.
                        tableToRdf.setTableSubject(tableURI);
                        // Add metadata (symbolic name) to table subject.
                        this.faultTolerance.execute(new FaultTolerance.Action() {

                            @Override
                            public void action() throws Exception {
                                RelationalToRdf.this.rdfTableWrap.add(tableURI, TabularOntology.TABLE_SYMBOLIC_NAME,
                                        RelationalToRdf.this.rdfTableWrap.getValueFactory().createLiteral(entry.getSymbolicName()));
                            }
                        });
                    }
                    converter.convertTable(entry, conn);
                    ContextUtils.sendShortInfo(this.ctx, "dpu.execution.table.processing.finished", symbolicName);
                } catch (ConversionFailed ex) {
                    throw ContextUtils.dpuException(this.ctx, ex, "dpu.execution.errors.conversion.failed", entry);
                }
            }
        } catch (DataUnitException e) {
            LOG.error("Database error occurred", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                LOG.warn("Failed to close connection", e);
            }
        }

    }

}
