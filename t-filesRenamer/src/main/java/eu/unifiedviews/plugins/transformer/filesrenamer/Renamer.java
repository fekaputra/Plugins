package eu.unifiedviews.plugins.transformer.filesrenamer;

import java.util.Date;
import java.util.List;

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
import eu.unifiedviews.helpers.cuni.rdf.sparql.SparqlUtils;
import eu.unifiedviews.helpers.dataunit.DataUnitUtils;
import eu.unifiedviews.helpers.dataunit.files.FilesVocabulary;
import eu.unifiedviews.helpers.dataunit.metadata.MetadataVocabulary;

@DPU.AsTransformer
public class Renamer extends AbstractDpu<RenamerConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(Renamer.class);

    private static final String SYMBOLIC_NAME_SUFFIX_BINDING = "suffix";

    private static final String PATTERN_BINDING = "pattern";
    
    private static final String REPLACE_WITH_BINDING = "rpl";

    /**
     * First query copy all data from input to output.
     */
    private static final String SPARQL_COPY_ALL
            = "INSERT { ?s ?p ?o } WHERE { ?s ?p ?o }";

    /**
     * Second (optional) query create new symbolic names based on some pattern.
     */
    private static final String SPARQL_CREATE_NEW_SYMBOLIC_NAME
            = "INSERT { ?s <" + RenamerVocabulary.TEMP_SYMBOLIC_NAME + "> ?valueNew } \n"
            + "WHERE { ?s <" + MetadataVocabulary.UV_SYMBOLIC_NAME + "> ?value. \n"
            + "BIND ( CONCAT(?value , \"" + SYMBOLIC_NAME_SUFFIX_BINDING + "\" ) AS ?valueNew) \n"
            + "} ";

    /**
     * Next query creates new virtual paths.
     */
    private static final String SPARQL_CREATE_VIRTUAL_PATH
            = "INSERT { ?s <" + RenamerVocabulary.TEMP_VIRTUAL_PATH + "> ?valueNew } \n"
            + "WHERE { ?s <" + FilesVocabulary.UV_VIRTUAL_PATH + "> ?value. \n"
            + "BIND ( REPLACE(?value, \""+ PATTERN_BINDING + "\", \"" + REPLACE_WITH_BINDING + "\", \"i\") "
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

    @AutoInitializer.Init
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
        executeInsert(SPARQL_CREATE_NEW_SYMBOLIC_NAME.replaceAll(SYMBOLIC_NAME_SUFFIX_BINDING, suffix),
                source, target);

        executeInsert(SPARQL_CREATE_VIRTUAL_PATH.replaceAll(PATTERN_BINDING, config.getPattern()).
                replaceAll(REPLACE_WITH_BINDING,  config.getReplaceText()), source, target);

        executeDeleteInsert(SPARQL_REPLACE, source, target);
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
