package eu.unifiedviews.plugins.transformer.sparql.update;

import cz.cuni.mff.xrg.uv.transformer.sparql.update.SparqlUpdateConfig_V1;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.DataUnitUtils;
import eu.unifiedviews.helpers.dataunit.metadata.MetadataUtilsInstance;
import eu.unifiedviews.helpers.dataunit.rdf.RdfDataUnitUtils;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.config.migration.ConfigurationUpdate;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.rdf.validation.RdfValidation;
import eu.unifiedviews.plugins.transformer.sparql.SPARQLConfig_V1;
import org.openrdf.model.URI;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Škoda Petr
 */
@DPU.AsTransformer
public class SparqlUpdate extends AbstractDpu<SparqlUpdateConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(SparqlUpdate.class);

    private static final int MAX_GRAPH_COUNT = 1000;

    /**
     * Query used to copy all data from input to output graph.
     * We could use ADD here, but in this way copy query is executed by the same procedure as a user query.
     */
    private static final String QUERY_COPY = "INSERT {?s ?p ?o} WHERE {?s ?p ?o}";

    @DataUnit.AsInput(name = "input")
    public RDFDataUnit rdfInput;

    @DataUnit.AsOutput(name = "output")
    public WritableRDFDataUnit rdfOutput;

    @ExtensionInitializer.Init
    public RdfValidation rdfValidation;

    @ExtensionInitializer.Init(param = "eu.unifiedviews.plugins.transformer.sparql.SPARQLConfig__V1")
    public ConfigurationUpdate _ConfigurationUpdate;

    public SparqlUpdate() {
        super(SparqlUpdateVaadinDialog.class,
                ConfigHistory.history(SPARQLConfig_V1.class).addCurrent(SparqlUpdateConfig_V1.class));
    }

    @Override
    protected void innerExecute() throws DPUException {

        if (useDataset()) {
            ContextUtils.sendShortInfo(ctx, "sparqlUpdate.dpu.mode.openRdf");
        } else {
            ContextUtils.sendShortInfo(ctx, "sparqlUpdate.dpu.mode.virtuoso");
        }
        // Get update query.
        final String query = config.getQuery();
        if (query == null || query.isEmpty()) {
            throw ContextUtils.dpuException(ctx, "sparqlUpdate.dpu.error.emptyQuery");
        }
        final List<URI> outputGraphs = new LinkedList<>();
        // Get graphs.
        final List<RDFDataUnit.Entry> sourceEntries = getInputEntries(rdfInput);
        // Copy data into a new graph.
        if (config.isPerGraph()) {
            ContextUtils.sendMessage(ctx, DPUContext.MessageType.INFO, "sparqlUpdate.dpu.info.perGraph",
                    "sparqlUpdate.dpu.info.perGraph.body", sourceEntries.size());
            // Execute on per-graph basis.
            int counter = 1;
            for (final RDFDataUnit.Entry sourceEntry : sourceEntries) {
                LOG.info("Executing {}/{}", counter++, sourceEntries.size());
                // For each input graph prepare output graph.
                final URI targetGraph = createOutputGraph(sourceEntry);
                try {
                    LOG.info("   {} -> {}", sourceEntry.getDataGraphURI(), targetGraph);
                } catch (DataUnitException e) {
                    throw new DPUException(e);
                }


                // Execute query 1 -> 1.
                updateEntries(query, Arrays.asList(sourceEntry), targetGraph);
                outputGraphs.add(targetGraph);
                if (ctx.canceled()) {
                    throw ContextUtils.dpuExceptionCancelled(ctx);
                }
            }
        } else {
            // All graph at once, just check size.
            if (sourceEntries.size() > MAX_GRAPH_COUNT) {
                throw ContextUtils.dpuException(ctx, "sparqlUpdate.dpu.error.tooManyGraphs",
                        MAX_GRAPH_COUNT, sourceEntries.size());
            }
            // Prepare single output graph.
            final URI targetGraph = createOutputGraph();

            // Execute over all intpu graph ie. m -> 1
            ContextUtils.sendShortInfo(ctx, "sparqlUpdate.dpu.info.singleOutput");
            updateEntries(query, sourceEntries, targetGraph);
            outputGraphs.add(targetGraph);
        }
        // Summmary message.
        long inputSize = 0;
        try {
            inputSize = getTriplesCount(rdfOutput, RdfDataUnitUtils.asGraphs(sourceEntries));
        } catch (DataUnitException e) {
            LOG.error(e.getLocalizedMessage(), e.getStackTrace());
        }
        long outputSize = getTriplesCount(rdfOutput, outputGraphs.toArray(new URI[0]));

        ContextUtils.sendShortInfo(ctx, "sparqlUpdate.dpu.msg.report", inputSize, outputSize);
    }

    /**
     * Get connection and use it to execute given query. Based on user option the query is executed over one
     * or over updateQuery graphs.
     * 
     * @param updateQuery
     * @param sourceEntries
     * @param targetgraph
     * @throws DPUException
     */
    protected void updateEntries(final String updateQuery, final List<RDFDataUnit.Entry> sourceEntries,
            final URI targetgraph) throws DPUException {
        RepositoryConnection connection = null;
        try {
            connection = rdfInput.getConnection();
        } catch (DataUnitException e) {
            LOG.error(e.getLocalizedMessage(), e.getStackTrace());
        }
        //copy data
        executeUpdateQuery(QUERY_COPY, toUriList(sourceEntries), targetgraph, connection);
        // Execute user query over new graph.
        executeUpdateQuery(updateQuery, Arrays.asList(targetgraph), targetgraph, connection);

    }

    /**
     * Execute given query.
     * 
     * @param query
     * @param sourceGraphs
     *            USING graphs.
     * @param targetGraph
     *            WITH graphs.
     * @param connection
     * @throws eu.unifiedviews.dpu.DPUException
     */
    protected void executeUpdateQuery(String query, List<URI> sourceGraphs, URI targetGraph,
            RepositoryConnection connection) throws DPUException {
        // Prepare query.
//        if (!useDataset()) {
            if (Pattern.compile(Pattern.quote("DELETE"), Pattern.CASE_INSENSITIVE).matcher(query).find()) {
                query = query.replaceFirst("(?i)DELETE", prepareWithClause(targetGraph) + " DELETE");
            } else {
                query = query.replaceFirst("(?i)INSERT", prepareWithClause(targetGraph) + " INSERT");
            }
            query = query.replaceFirst("(?i)WHERE", prepareUsingClause(sourceGraphs) + "WHERE");
//        }
        LOG.info("Query to execute: {}", query);
        try {
            // Execute query.
            final Update update = connection.prepareUpdate(QueryLanguage.SPARQL, query);
//            if (useDataset()) {
//                final DatasetImpl dataset = new DatasetImpl();
//                for (URI graph : sourceGraphs) {
//                    dataset.addDefaultGraph(graph);
//                }
//                dataset.addDefaultRemoveGraph(targetGraph);
//                dataset.setDefaultInsertGraph(targetGraph);
//                update.setDataset(dataset);
//            }
            update.execute();
        } catch (MalformedQueryException | UpdateExecutionException ex) {
            throw ContextUtils.dpuException(ctx, ex, "sparqlUpdate.dpu.error.query");
        } catch (RepositoryException ex) {
            throw ContextUtils.dpuException(ctx, ex, "sparqlUpdate.dpu.error.repository");
        }
    }

    /**
     * @return New output graph.
     * @throws DPUException
     */
    protected URI createOutputGraph() throws DPUException {
        // Register new output graph
        final String symbolicName = "http://unifiedviews.eu/resource/sparql-construct/"
                + Long.toString((new Date()).getTime());
        try {
            return rdfOutput.addNewDataGraph(symbolicName);
        } catch (DataUnitException ex) {
            throw ContextUtils.dpuException(ctx, ex, "sparqlUpdate.dpu.error.cantAddGraph");
        }
    }

    /**
     * @param entry
     * @return New output graph.
     * @throws DPUException
     */
    protected URI createOutputGraph(RDFDataUnit.Entry entry) throws DPUException {
        final String suffix = "/" + ctx.getExecMasterContext().getDpuContext().getDpuInstanceId().toString();
        try {
            return rdfOutput.addNewDataGraph(entry.getSymbolicName() + suffix);
        } catch (DataUnitException ex) {
            throw ContextUtils.dpuException(ctx, ex, "sparqlUpdate.dpu.error.cantAddGraph");
        }
    }

    /**
     * Register new output graph and return WITH clause for SPARQL insert.
     * 
     * @param graph
     * @return
     */
    protected String prepareWithClause(URI graph) {
        final StringBuilder withClause = new StringBuilder();
        withClause.append("WITH <");
        withClause.append(graph.stringValue());
        withClause.append("> \n");
        return withClause.toString();
    }

    /**
     * Get graph URIs from entry list.
     * 
     * @param entries
     * @return
     * @throws DPUException
     */
    protected List<URI> toUriList(final List<RDFDataUnit.Entry> entries) throws DPUException {
        final List<URI> result = new ArrayList<>(entries.size());

        for (RDFDataUnit.Entry entry : entries) {
            try {
                result.add(entry.getDataGraphURI());
            } catch (DataUnitException ex) {
                throw ContextUtils.dpuException(ctx, ex, "sparqlUpdate.dpu.error.dataUnit");
            }
        }

        return result;
    }

    /**
     * @param uris
     * @return Using clause for SPARQL insert, based on input graphs.
     * @throws DPUException
     */
    protected String prepareUsingClause(List<URI> uris) throws DPUException {
        final StringBuilder usingClause = new StringBuilder();
        for (URI uri : uris) {
            usingClause.append("USING <");
            usingClause.append(uri.stringValue());
            usingClause.append("> \n");
        }
        return usingClause.toString();
    }

    /**
     * @param dataUnit
     * @return Data graphs in given DataUnit.
     * @throws DPUException
     */
    protected List<RDFDataUnit.Entry> getInputEntries(final RDFDataUnit dataUnit) throws DPUException {
        try {
            return  DataUnitUtils.getEntries(dataUnit, RDFDataUnit.Entry.class);
        } catch (DataUnitException e) {
            throw new DPUException(e);
        }
    }

    protected final boolean useDataset() {
        // Should be removed once bug in Sesame or Virtuoso is fixex.
        return System.getProperty(MetadataUtilsInstance.ENV_PROP_VIRTUOSO) == null;
    }

    /**
     * @param dataUnit
     * @param graphs
     * @return Number of triples in given entries.
     */
    protected Long getTriplesCount(final RDFDataUnit dataUnit, final URI[] graphs)
            throws DPUException {

        RepositoryConnection connection = null;
        Long size = 0l;
        try {
            try {
                connection = dataUnit.getConnection();
            } catch (DataUnitException e) {
                throw new DPUException(e);
            }
            try {
                size = connection.size(graphs);
            } catch (RepositoryException e) {
                throw new DPUException(e);
            }
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException e) {
                    throw new DPUException(e);
                }
            }
        }
        return size;
    }



}
