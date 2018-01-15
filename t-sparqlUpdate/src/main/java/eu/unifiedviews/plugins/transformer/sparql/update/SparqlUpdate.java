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
import eu.unifiedviews.helpers.dataunit.copy.CopyHelper;
import eu.unifiedviews.helpers.dataunit.copy.CopyHelpers;
import eu.unifiedviews.helpers.dataunit.metadata.MetadataUtilsInstance;
import eu.unifiedviews.helpers.dataunit.rdf.RdfDataUnitUtils;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.config.migration.ConfigurationUpdate;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.rdf.profiler.RdfProfiler;
import eu.unifiedviews.helpers.dpu.extension.rdf.validation.RdfValidation;
import eu.unifiedviews.plugins.transformer.sparql.SPARQLConfig_V1;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.UpdateExecutionException;
import org.eclipse.rdf4j.query.impl.DatasetImpl;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
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

    @ExtensionInitializer.Init
    public RdfProfiler rdfProfiler;

    @ExtensionInitializer.Init(param = "eu.unifiedviews.plugins.transformer.sparql.SPARQLConfig__V1")
    public ConfigurationUpdate _ConfigurationUpdate;

    public SparqlUpdate() {
        super(SparqlUpdateVaadinDialog.class,
                ConfigHistory.history(SPARQLConfig_V1.class).addCurrent(SparqlUpdateConfig_V1.class));
    }

    @Override
    protected void innerExecute() throws DPUException {

        boolean performanceOptimizationEnabled = false;

        if (ctx.isPerformanceOptimizationEnabled(rdfInput)) {
            performanceOptimizationEnabled = true;
            if (!config.isPerGraph()) {
                //activated, but no effect, as per graph option not selected
                ContextUtils.sendInfo(ctx, "sparqlUpdate.dpu.opt.all.warn", "sparqlUpdate.dpu.opt.all.warn.detailed");
            } else {
                //activated
                ContextUtils.sendInfo(ctx, "sparqlUpdate.dpu.optimistic.on", "sparqlUpdate.dpu.optimistic.on.detailed");
            }

        } else {
            //not activated
            ContextUtils.sendInfo(ctx, "sparqlUpdate.dpu.optimistic.off", "sparqlUpdate.dpu.optimistic.off.detailed");
        }

        if (useDataset()) {
            //default and usual choice
            LOG.debug("Dataset clause is used"); //ContextUtils.sendShortInfo(ctx, "sparqlUpdate.dpu.mode.openRdf");
        } else {
            LOG.debug("Dataset clause is NOT used"); //ContextUtils.sendShortInfo(ctx, "sparqlUpdate.dpu.mode.virtuoso");
        }
        // Get update query.
        final String query = config.getQuery();
        if (query == null || query.isEmpty()) {
            throw ContextUtils.dpuException(ctx, "sparqlUpdate.dpu.error.emptyQuery");
        }
        final List<IRI> outputGraphs = new LinkedList<>();
        // Get graphs.
        final List<RDFDataUnit.Entry> sourceEntries = getInputEntries(rdfInput);

        //calculate input size (before executing changes)
        long inputSize = 0;
        try {
            inputSize = getTriplesCount(rdfInput, RdfDataUnitUtils.asGraphs(sourceEntries));
        } catch (DataUnitException e) {
            LOG.error(e.getLocalizedMessage(), e.getStackTrace());
        }

        int numberOfInputGraphs = sourceEntries.size();
        LOG.info("{} graph(s) in the input data unit", numberOfInputGraphs);

        int numberOfOutputGraphs;


        if (config.isPerGraph()) {
            ContextUtils.sendMessage(ctx, DPUContext.MessageType.INFO, "sparqlUpdate.dpu.info.perGraph",
                    "sparqlUpdate.dpu.info.perGraph.body", sourceEntries.size());
            // Execute on per-graph basis. So if optimalized, it just copies the graph entries to the output data unit (with the original graphs),
            // instead of producing new data graphs and copying content, and then executes on per-graph basis
            int counter = 1;
            CopyHelper copyHelper = CopyHelpers.create(rdfInput, rdfOutput);
            try {
                for (final RDFDataUnit.Entry sourceEntry : sourceEntries) {
                    LOG.info("Executing {}/{}", counter++, sourceEntries.size());

                    String sourceSymbolicName;
                    IRI sourceDataGraphURI;
                    try {
                        sourceSymbolicName = sourceEntry.getSymbolicName();
                        sourceDataGraphURI = sourceEntry.getDataGraphURI();
                    } catch (DataUnitException e) {
                        throw new DPUException(e);
                    }

                    //get target entries graph URL
                    IRI targetGraph;
                    if (performanceOptimizationEnabled) {
                        try {
                            copyHelper.copyMetadata(sourceSymbolicName);
                        } catch (DataUnitException e) {
                            throw new DPUException(e);
                        }
                        //target graph is the same as source graph
                        try {
                            targetGraph = sourceEntry.getDataGraphURI();
                        } catch (DataUnitException e) {
                            throw new DPUException(e);
                        }

                    }
                    else {
                        // For each input graph create new output graph
                        targetGraph = createOutputGraph(sourceEntry);
                    }

                    LOG.info("Source: {}, graph {} is copied to graph {}", sourceSymbolicName, sourceDataGraphURI, targetGraph);

                    // Execute query 1 -> 1.
                    updateEntries(query, getGraphUriList(Arrays.asList(sourceEntry)), targetGraph, performanceOptimizationEnabled);
                    outputGraphs.add(targetGraph);

                    if (ctx.canceled()) {
                        throw ContextUtils.dpuExceptionCancelled(ctx);
                    }
                }
            } finally {
                copyHelper.close();
            }

            numberOfOutputGraphs = numberOfInputGraphs;
        } else {
            // Execute query on top of all graphs at once.
            // In case of NON-optimalized approach (the original one) it creates only one OUTPUT graph, but in case of optimalized version it produces more graphs!
            // For Optimized mode, this mode is not available!
            ContextUtils.sendInfo(ctx, "sparqlUpdate.dpu.info.singleOutput", "sparqlUpdate.dpu.info.singleOutput.detailed");

            if (numberOfInputGraphs > MAX_GRAPH_COUNT) {
                throw ContextUtils.dpuException(ctx, "sparqlUpdate.dpu.error.tooManyGraphs",
                        MAX_GRAPH_COUNT, sourceEntries.size());
            }

            final IRI targetGraph = createOutputGraph();
            LOG.info("All source graphs are copied to target graph {}", targetGraph);

            // Execute over all intpu graph ie. m -> 1
            updateEntries(query, getGraphUriList(sourceEntries), targetGraph, false);
            outputGraphs.add(targetGraph);

            numberOfOutputGraphs = 1;
        }

        // Calculate output size
        long outputSize = getTriplesCount(rdfOutput, outputGraphs.toArray(new IRI[0]));

        ContextUtils.sendInfo(ctx, "sparqlUpdate.dpu.msg.report", "sparqlUpdate.dpu.msg.report.detailed", inputSize, outputSize, numberOfInputGraphs, numberOfOutputGraphs);
    }

    /**
     * Get connection and use it to execute given query. Based on user option the query is executed over one
     * or over updateQuery graphs.
     * 
     * @param updateQuery
     * @param sourceGraphs
     * @param targetgraph
     * @param optimalized
     * @throws DPUException
     */
    protected void updateEntries(final String updateQuery, final List<IRI> sourceGraphs,
                                 final IRI targetgraph, boolean optimalized) throws DPUException {
        RepositoryConnection connection = null;
        try {
            connection = rdfInput.getConnection();

            if (optimalized) {
                // Execute user query
                executeUpdateQuery(updateQuery, sourceGraphs, targetgraph, connection);
            }
            else {
                //copy the data before
                executeUpdateQuery(QUERY_COPY, sourceGraphs, targetgraph, connection);
                // Execute user query over new graph.
                executeUpdateQuery(updateQuery, Arrays.asList(targetgraph), targetgraph, connection);
            }

        } catch (DataUnitException e) {
            LOG.error(e.getLocalizedMessage(), e.getStackTrace());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException e) {
                    LOG.warn("Cannot close connection", e);
                }
            }
        }


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
    protected void executeUpdateQuery(String query, List<IRI> sourceGraphs, IRI targetGraph,
            RepositoryConnection connection) throws DPUException {
        // Prepare query.
        if (!useDataset()) {
            if (Pattern.compile(Pattern.quote("DELETE"), Pattern.CASE_INSENSITIVE).matcher(query).find()) {
                query = query.replaceFirst("(?i)DELETE", prepareWithClause(targetGraph) + " DELETE");
            } else {
                query = query.replaceFirst("(?i)INSERT", prepareWithClause(targetGraph) + " INSERT");
            }
            query = query.replaceFirst("(?i)WHERE", prepareUsingClause(sourceGraphs) + "WHERE");
        }
        LOG.info("Query to execute: {}", query);
        try {
            // Execute query.
            final Update update = connection.prepareUpdate(QueryLanguage.SPARQL, query);
            if (useDataset()) {
                final DatasetImpl dataset = new DatasetImpl();
                for (IRI graph : sourceGraphs) {
                    dataset.addDefaultGraph(graph);
                }
                dataset.addDefaultRemoveGraph(targetGraph);
                dataset.setDefaultInsertGraph(targetGraph);
                update.setDataset(dataset);
            }
            update.execute();
        } catch (MalformedQueryException | UpdateExecutionException ex) {
            throw ContextUtils.dpuException(ctx, ex, "sparqlUpdate.dpu.error.query");
        } catch (RepositoryException ex) {
            throw ContextUtils.dpuException(ctx, ex, "sparqlUpdate.dpu.error.repository");
        }
    }

    /**
     * Creates new output graph. Symbolic name is completely new (using time stamp in suffix).
     * Data graph IRI is generated.
     * @return New output graph.
     * @throws DPUException
     */
    protected IRI createOutputGraph() throws DPUException {
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
     * Creates new output graph. Symbolic name is based on the input graph (but modifed with this DPU's id as suffix).
     * Data graph IRI is generated.
     * @param entry
     * @return New output graph.
     * @throws DPUException
     */
    protected IRI createOutputGraph(RDFDataUnit.Entry entry) throws DPUException {
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
    protected String prepareWithClause(IRI graph) {
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
    protected List<IRI> getGraphUriList(final List<RDFDataUnit.Entry> entries) throws DPUException {
        final List<IRI> result = new ArrayList<>(entries.size());

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
    protected String prepareUsingClause(List<IRI> uris) throws DPUException {
        final StringBuilder usingClause = new StringBuilder();
        for (IRI uri : uris) {
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
    protected Long getTriplesCount(final RDFDataUnit dataUnit, final IRI[] graphs)
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
