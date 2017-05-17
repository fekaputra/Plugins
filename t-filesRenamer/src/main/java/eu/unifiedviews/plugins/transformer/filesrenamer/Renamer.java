package eu.unifiedviews.plugins.transformer.filesrenamer;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.DataUnitUtils;
import eu.unifiedviews.helpers.dataunit.files.FilesVocabulary;
import eu.unifiedviews.helpers.dataunit.metadata.MetadataVocabulary;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.rdf.sparql.SparqlUtils;

@DPU.AsTransformer
public class Renamer extends AbstractDpu<RenamerConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(Renamer.class);

    /**
     * First query copy all data from input to output.
     */
    private static final String SPARQL_COPY_ALL
            = "INSERT { ?s ?p ?o } WHERE { ?s ?p ?o }";

    /**
     * Second (optional) query create new symbolic names based on some pattern.
     * %s - suffix to to symbolic name
     */
    private static final String SPARQL_CREATE_NEW_SYMBOLIC_NAME
            = "INSERT { ?s <" + RenamerVocabulary.TEMP_SYMBOLIC_NAME + "> ?valueNew } \n"
            + "WHERE { ?s <" + MetadataVocabulary.UV_SYMBOLIC_NAME + "> ?value. \n"
            + "BIND ( CONCAT(?value , \"%s\" ) AS ?valueNew) \n"
            + "} ";

    /**
     * Next query creates new virtual paths.
     * %s - regular expression pattern
     * %s - value to replace, use directly in the string
     */
    private static final String SPARQL_CREATE_VIRTUAL_PATH
            = "INSERT { ?s <" + RenamerVocabulary.TEMP_VIRTUAL_PATH + "> ?valueNew } \n"
            + "WHERE { ?s <" + FilesVocabulary.UV_VIRTUAL_PATH + "> ?value. \n"
            + "BIND ( REPLACE(?value, \"%s\", %s, \"i\") "
            + "AS ?valueNew)\n"
            + "} ";

    private static final String SPARQL_REPLACE
            = "DELETE { "
            + "?s ?p ?valueOld ; "
            + "?pTemp ?valueNew ."
            + "}\n"
            + "INSERT { "
            + "?s ?p ?valueNew "
            + "}\n"
            + "WHERE { "
            + "?s ?p ?valueOld ; "
            + " ?pTemp ?valueNew .\n"
            + "VALUES ( ?p ?pTemp ) {\n"
            + " ( <" + MetadataVocabulary.UV_SYMBOLIC_NAME + "> <" + RenamerVocabulary.TEMP_SYMBOLIC_NAME + "> )\n"
            + " ( <" + FilesVocabulary.UV_VIRTUAL_PATH + "> <" + RenamerVocabulary.TEMP_VIRTUAL_PATH + "> )\n"
            + "} }";

    @DataUnit.AsInput(name = "input")
    public FilesDataUnit inFilesData;

    @DataUnit.AsOutput(name = "output")
    public WritableFilesDataUnit outFilesData;

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    public Renamer() {
        super(RenamerVaadinDialog.class, ConfigHistory.noHistory(RenamerConfig_V1.class));
    }

    @Override
    protected void innerExecute() throws DPUException {
        // Get input and output
        final List<RDFDataUnit.Entry> source = faultTolerance.execute(
                new FaultTolerance.ActionReturn<List<RDFDataUnit.Entry>>() {

                    @Override
                    public List<RDFDataUnit.Entry> action() throws Exception {
                        return DataUnitUtils.getMetadataEntries(inFilesData);
                    }
                }, "dpu.error.metadata.read");
        final RDFDataUnit.Entry target = faultTolerance.execute(
                new FaultTolerance.ActionReturn<RDFDataUnit.Entry>() {

                    @Override
                    public RDFDataUnit.Entry action() throws Exception {
                        return DataUnitUtils.getWritableMetadataEntry(outFilesData);
                    }
                }, "dpu.error.metadata.write");
        // ...
        executeInsert(SPARQL_COPY_ALL, source, target);

        String suffix = Long.toString((new Date()).getTime());

        executeInsert(String.format(SPARQL_CREATE_NEW_SYMBOLIC_NAME, suffix), source, target);
        
        // Escape value if advance option is not used.
        final String replaceText;
        if (config.isUseAdvanceReplace()) {
            replaceText = config.getReplaceText();
        } else {
            replaceText = "\"" + config.getReplaceText() + "\"";
        }
        
        executeInsert(String.format(SPARQL_CREATE_VIRTUAL_PATH, config.getPattern(), replaceText), source,
                target);
        
        executeDeleteInsert(SPARQL_REPLACE, Arrays.asList(target), target);
    }

    private void executeInsert(final String query, final List<RDFDataUnit.Entry> source,
            final RDFDataUnit.Entry target) throws DPUException {
        LOG.info("QUERY: {}", query);
        // Prepare SPARQL update query.
        final SparqlUtils.SparqlUpdateObject updateQuery = faultTolerance.execute(
                new FaultTolerance.ActionReturn<SparqlUtils.SparqlUpdateObject>() {

                    @Override
                    public SparqlUtils.SparqlUpdateObject action() throws Exception {
                        return SparqlUtils.createInsert(query, source, target);
                    }
                }, "dpu.error.sparql.preparation");

        // Execute SPARQL ie. copy metadata that match given conditions.
        faultTolerance.execute(outFilesData, new FaultTolerance.ConnectionAction() {

            @Override
            public void action(RepositoryConnection connection) throws Exception {
                SparqlUtils.execute(connection, updateQuery);
            }
        }, "dpu.error.sparql.execution");
    }

    private void executeDeleteInsert(final String query, final List<RDFDataUnit.Entry> source,
            final RDFDataUnit.Entry target) throws DPUException {
        LOG.info("QUERY: {}", query);
        // Prepare SPARQL update query.
        final SparqlUtils.SparqlUpdateObject updateQuery = faultTolerance.execute(
                new FaultTolerance.ActionReturn<SparqlUtils.SparqlUpdateObject>() {

                    @Override
                    public SparqlUtils.SparqlUpdateObject action() throws Exception {
                        return SparqlUtils.createDelete(query, source, target);
                    }
                }, "dpu.error.sparql.preparation");

        // Execute SPARQL ie. copy metadata that match given conditions.
        faultTolerance.execute(outFilesData, new FaultTolerance.ConnectionAction() {

            @Override
            public void action(RepositoryConnection connection) throws Exception {
                SparqlUtils.execute(connection, updateQuery);
            }
        }, "dpu.error.sparql.execution");
    }

}
