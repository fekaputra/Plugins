package eu.unifiedviews.plugins.trandformer.sparqlselect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.query.resultio.text.csv.SPARQLResultsCSVWriterFactory;
import org.openrdf.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.cuni.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.cuni.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.cuni.dpu.exec.AutoInitializer;
import eu.unifiedviews.helpers.cuni.extensions.FaultTolerance;
import eu.unifiedviews.helpers.cuni.migration.ConfigurationUpdate;
import eu.unifiedviews.helpers.cuni.rdf.sparql.SparqlUtils;
import eu.unifiedviews.helpers.dataunit.DataUnitUtils;
import eu.unifiedviews.helpers.dataunit.files.FilesDataUnitUtils;

@DPU.AsTransformer
public class SparqlSelect extends AbstractDpu<SparqlSelectConfig> {

    private static final Logger LOG = LoggerFactory.getLogger(SparqlSelect.class);

    @DataUnit.AsInput(name = "input")
    public RDFDataUnit inRdfData;

    @DataUnit.AsOutput(name = "output")
    public WritableFilesDataUnit outFilesData;

    @AutoInitializer.Init
    public FaultTolerance faultTolerance;

    @AutoInitializer.Init(param = "eu.unifiedviews.plugins.trandformer.sparqlselect.SparqlSelectConfig")
    public ConfigurationUpdate _ConfigurationUpdate;

    public SparqlSelect() {
        super(SparqlSelectVaadinDialog.class, ConfigHistory.noHistory(SparqlSelectConfig.class));
    }

    @Override
    protected void innerExecute() throws DPUException {
        // Prepare output path.
        final FilesDataUnit.Entry output = faultTolerance.execute(new FaultTolerance.ActionReturn<FilesDataUnit.Entry>() {

            @Override
            public FilesDataUnit.Entry action() throws Exception {
                return FilesDataUnitUtils.createFile(outFilesData, config.getTargetPath());
            }
        });
        // Prepare query.
        final String queryAsString = faultTolerance.execute(new FaultTolerance.ActionReturn<String>() {

            @Override
            public String action() throws Exception {
                final List<RDFDataUnit.Entry> sources = DataUnitUtils.getEntries(inRdfData, RDFDataUnit.Entry.class);
                return config.getQuery().replaceFirst("(?i)WHERE", 
                        SparqlUtils.prepareClause("FROM", sources) + "WHERE ");
            }
        });
        // Execute query and import into csv.
        faultTolerance.execute(inRdfData, new FaultTolerance.ConnectionAction() {

            @Override
            public void action(RepositoryConnection connection) throws Exception {
                final File outFile = FilesDataUnitUtils.asFile(output);
                final SPARQLResultsCSVWriterFactory writerFactory = new SPARQLResultsCSVWriterFactory();
                // Create output file and write the result.
                try (OutputStream outputStream = new FileOutputStream(outFile)) {
                    final TupleQueryResultWriter resultWriter = writerFactory.getWriter(outputStream);
                    final TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryAsString);
                    query.evaluate(resultWriter);
                } catch (IOException ex) {
                    throw new DPUException(ex);
                }
            }
        });
    }

}
