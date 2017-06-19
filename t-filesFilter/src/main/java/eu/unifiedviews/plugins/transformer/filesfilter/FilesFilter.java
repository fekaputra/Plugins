package eu.unifiedviews.plugins.transformer.filesfilter;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.DataUnitUtils;
import eu.unifiedviews.helpers.dataunit.metadata.MetadataVocabulary;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.config.migration.ConfigurationUpdate;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.rdf.sparql.SparqlUtils;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Matcher;

@DPU.AsTransformer
public class FilesFilter extends AbstractDpu<FilesFilterConfig_V1> {

    protected static final String PATH_PLACEHOLDER = "pathBinding";

    private static final String SPARQL_EXACT_MATCH = getSparqlExactMatch();

    private static final String SPARQL_REG_EXP = getSparqlRegExp();

    private static final Logger log = LoggerFactory.getLogger(FilesFilter.class);

    private static String getSparqlExactMatch() {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT { ?sA1 ?pA1 ?oA1 . ?oB1 ?pB2 ?oB2 . ?oC2 ?pC3 ?oC3 } WHERE { ");
        sb.append("{ ");
        sb.append("?sA1 ?pA1 ?oA1 . ");
        sb.append("?sA1 <" + MetadataVocabulary.UV_SYMBOLIC_NAME + "> \"" + PATH_PLACEHOLDER + "\" . ");
        sb.append("} ");
        sb.append("UNION ");
        sb.append("{ ");
        sb.append("?oB1 ?pB2 ?oB2 . ");
        sb.append("?sB1 ?pB1 ?oB1 . ");
        sb.append("?sB1 <" + MetadataVocabulary.UV_SYMBOLIC_NAME + "> \"" + PATH_PLACEHOLDER + "\" . ");
        sb.append("FILTER ((isURI(?oB1) || isBlank(?oB1))) ");
        sb.append("} ");
        sb.append("UNION ");
        sb.append("{ ");
        sb.append("?oC2 ?pC3 ?oC3 . ");
        sb.append("?oC1 ?pC2 ?oC2 . ");
        sb.append("?sC1 ?pC1 ?oC1 . ");
        sb.append("?sC1 <" + MetadataVocabulary.UV_SYMBOLIC_NAME + "> \"" + PATH_PLACEHOLDER + "\" . ");
        sb.append("FILTER ((isURI(?oC1) || isBlank(?oC1)) && (isURI(?oC2) || isBlank(?oC2))) ");
        sb.append("} ");
        sb.append("}");
        return sb.toString();
    }

    private static String getSparqlRegExp() {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT { ?sA1 ?pA1 ?oA1 . ?oB1 ?pB2 ?oB2 . ?oC2 ?pC3 ?oC3 } WHERE { ");
        sb.append("{ ");
        sb.append("?sA1 ?pA1 ?oA1 . ");
        sb.append("?sA1 <" + MetadataVocabulary.UV_SYMBOLIC_NAME + "> ?path . ");
        sb.append("FILTER regex(?path, \"" + PATH_PLACEHOLDER + "\", \"i\" ) ");
        sb.append("} ");
        sb.append("UNION ");
        sb.append("{ ");
        sb.append("?oB1 ?pB2 ?oB2 . ");
        sb.append("?sB1 ?pB1 ?oB1 . ");
        sb.append("?sB1 <" + MetadataVocabulary.UV_SYMBOLIC_NAME + "> ?path . ");
        sb.append("FILTER (regex(?path, \"" + PATH_PLACEHOLDER + "\", \"i\" ) && ");
        sb.append("((isURI(?oB1) || isBlank(?oB1))) ) ");
        sb.append("} ");
        sb.append("UNION ");
        sb.append("{ ");
        sb.append("?oC2 ?pC3 ?oC3 . ");
        sb.append("?oC1 ?pC2 ?oC2 . ");
        sb.append("?sC1 ?pC1 ?oC1 . ");
        sb.append("?sC1 <" + MetadataVocabulary.UV_SYMBOLIC_NAME + "> ?path . ");
        sb.append("FILTER (regex(?path, \"" + PATH_PLACEHOLDER + "\", \"i\" ) && ");
        sb.append("((isURI(?oC1) || isBlank(?oC1)) && (isURI(?oC2) || isBlank(?oC2))) ) ");
        sb.append("} ");
        sb.append("}");
        return sb.toString();
    }

    @DataUnit.AsInput(name = "input")
    public FilesDataUnit inFilesData;

    @DataUnit.AsOutput(name = "output")
    public WritableFilesDataUnit outFilesData;

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    @ExtensionInitializer.Init(param = "eu.unifiedviews.plugins.transformer.filesfilter.FilesFilterConfig__V1")
    public ConfigurationUpdate _ConfigurationUpdate;

    public FilesFilter() {
        super(FilesFilterVaadinDialog.class, ConfigHistory.noHistory(FilesFilterConfig_V1.class));
    }

    @Override
    protected void innerExecute() throws DPUException {
        // Get input and output
        final List<RDFDataUnit.Entry> source = faultTolerance.execute(new FaultTolerance.ActionReturn<List<RDFDataUnit.Entry>>() {

            @Override
            public List<RDFDataUnit.Entry> action() throws Exception {
                return DataUnitUtils.getMetadataEntries(inFilesData);
            }
        }, "filesFilter.error.metadata.read");

        //        final List<FilesDataUnit.Entry> sourceFiles = FaultToleranceUtils.getEntries(faultTolerance, inFilesData, FilesDataUnit.Entry.class);
        //        for (FilesDataUnit.Entry e : sourceFiles) {
        //            try {
        //                log.info("Processing file with sn '{}' and path '{}'", e.getSymbolicName(), e.getFileURIString());
        //            } catch (DataUnitException ex) {
        //                java.util.logging.Logger.getLogger(FilesFilter.class.getName()).log(Level.SEVERE, null, ex);
        //            }
        //        }

        final RDFDataUnit.Entry target = faultTolerance.execute(
                new FaultTolerance.ActionReturn<RDFDataUnit.Entry>() {

                    @Override
                    public RDFDataUnit.Entry action() throws Exception {
                        return DataUnitUtils.getWritableMetadataEntry(outFilesData);
                    }
                }, "filesFilter.error.metadata.write");
        // Prepare SPARQL update query.
        final SparqlUtils.SparqlUpdateObject updateQuery = faultTolerance.execute(new FaultTolerance.ActionReturn<SparqlUtils.SparqlUpdateObject>() {

            @Override
            public SparqlUtils.SparqlUpdateObject action() throws Exception {
                String query;
                if (config.isUseRegExp()) {
                    query = SPARQL_REG_EXP;
                } else {
                    query = SPARQL_EXACT_MATCH;
                }
                // Do name substitution.
                query = query.replaceAll(PATH_PLACEHOLDER, Matcher.quoteReplacement(config.getObject()));
                return SparqlUtils.createInsert(String.format(query, config.getObject()), source, target);
            }
        }, "filesFilter.error.sparql.preparation");

        // Execute SPARQL ie. copy metadata that match given conditions.
        faultTolerance.execute(outFilesData, new FaultTolerance.ConnectionAction() {

            @Override
            public void action(RepositoryConnection connection) throws Exception {
                SparqlUtils.execute(connection, updateQuery);
            }
        }, "filesFilter.error.sparql.execution");
    }
}
