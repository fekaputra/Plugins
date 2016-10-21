package cz.cuni.mff.xrg.uv.transformer.tabular;

import cz.cuni.mff.xrg.uv.transformer.tabular.mapper.TableToRdf;
import cz.cuni.mff.xrg.uv.transformer.tabular.parser.ParserCsv;
import cz.cuni.mff.xrg.uv.transformer.tabular.parser.ParserDbf;
import cz.cuni.mff.xrg.uv.transformer.tabular.parser.ParserXls;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
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
import eu.unifiedviews.plugins.transformer.tabular.TabularVaadinDialog;
import eu.unifiedviews.plugins.transformer.tabular.parser.ParseFailed;
import eu.unifiedviews.plugins.transformer.tabular.parser.Parser;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.unifiedviews.helpers.dpu.extension.rdf.validation.RdfValidation;


import java.util.List;

@DPU.AsTransformer
public class Tabular extends AbstractDpu<TabularConfig_V2> {

    private static final Logger LOG = LoggerFactory.getLogger(Tabular.class);

    @DataUnit.AsInput(name = "table")
    public FilesDataUnit inFilesTable;

    @DataUnit.AsOutput(name = "triplifiedTable")
    public WritableRDFDataUnit outRdfTables;

    @ExtensionInitializer.Init(param = "outRdfTables")
    public WritableSimpleRdf rdfTableWrap;

    @ExtensionInitializer.Init
    public RdfValidation rdfValidation;

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    public Tabular() {
        super(TabularVaadinDialog.class,
                ConfigHistory.history(TabularConfig_V1.class).addCurrent(TabularConfig_V2.class));
    }

    @Override
    protected void innerExecute() throws DPUException {
        // Prepare tabular convertor.
        final TableToRdf tableToRdf = new TableToRdf(
                config.getTableToRdfConfig(),
                rdfTableWrap,
                rdfTableWrap.getValueFactory());
        // Prepare parser based on type.
        final Parser parser;
        switch (config.getTableType()) {
            case CSV:
                parser = new ParserCsv(config.getParserCsvConfig(), tableToRdf, ctx);
                break;
            case DBF:
                parser = new ParserDbf(config.getParserDbfConfig(), tableToRdf, ctx);
                break;
            case XLS:
                parser = new ParserXls(config.getParserXlsConfig(), tableToRdf, ctx);
                break;
            default:
                throw ContextUtils.dpuException(this.ctx, "execution.errors.table.unknown", this.config.getXlsSheetName());
        }
        // Get files to process.
        final List<FilesDataUnit.Entry> files = FaultToleranceUtils.getEntries(faultTolerance, inFilesTable,
                FilesDataUnit.Entry.class);

        for (final FilesDataUnit.Entry entry : files) {
            if (ctx.canceled()) {
                throw ContextUtils.dpuExceptionCancelled(ctx);
            }
            // Set output graph.
            final RDFDataUnit.Entry entryOutput = faultTolerance.execute(new FaultTolerance.ActionReturn<RDFDataUnit.Entry>() {

                @Override
                public RDFDataUnit.Entry action() throws Exception {
                    return RdfDataUnitUtils.addGraph(outRdfTables, entry.getSymbolicName());
                }
            });
            rdfTableWrap.setOutput(entryOutput);
            final String symbolicName = faultTolerance.execute(new FaultTolerance.ActionReturn<String>() {

                @Override
                public String action() throws Exception {
                    return entry.getSymbolicName();
                }
            });

            ContextUtils.sendShortInfo(this.ctx, "dpu.execution.file.processing", symbolicName);
            // Output data.
            try {
                // If set add subject for the whole table.
                if (config.isUseTableSubject()) {
                    // Prepare subject for table.
                    // TODO: We can use better subject here!
                    final URI tableURI = faultTolerance.execute(new FaultTolerance.ActionReturn<URI>() {

                        @Override
                        public URI action() throws Exception {
                            return rdfTableWrap.getValueFactory().createURI(entry.getFileURIString());
                        }
                    });
                    // Set as a table subject.
                    tableToRdf.setTableSubject(tableURI);
                    // Add metadata (symbolic name) to table subject.
                    faultTolerance.execute(new FaultTolerance.Action() {

                        @Override
                        public void action() throws Exception {
                            rdfTableWrap.add(tableURI, TabularOntology.TABLE_SYMBOLIC_NAME,
                                    rdfTableWrap.getValueFactory().createLiteral(entry.getSymbolicName()));
                        }
                    });
                }
                // Parse file.
                parser.parse(FaultToleranceUtils.asFile(faultTolerance, entry));
            } catch (ParseFailed ex) {
                throw ContextUtils.dpuException(this.ctx, ex, "dpu.execution.errors.conversion.failed", entry);
            }
        }
    }

}
