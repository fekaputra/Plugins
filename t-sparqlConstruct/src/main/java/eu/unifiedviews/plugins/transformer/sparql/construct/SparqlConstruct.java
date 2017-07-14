package eu.unifiedviews.plugins.transformer.sparql.construct;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.UpdateExecutionException;
import org.eclipse.rdf4j.query.impl.DatasetImpl;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;

import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.transformer.sparql.construct.SparqlConstructConfig_V1;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPUContext.MessageType;
import eu.unifiedviews.helpers.dataunit.DataUnitUtils;
import eu.unifiedviews.helpers.dataunit.metadata.MetadataUtilsInstance;
import eu.unifiedviews.helpers.dataunit.rdf.RdfDataUnitUtils;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.config.migration.ConfigurationUpdate;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.plugins.transformer.sparql.SPARQLConfig_V1;
import eu.unifiedviews.helpers.dpu.extension.rdf.validation.RdfValidation;


/*
 *
 * @author Škoda Petr
 */
@DPU.AsTransformer
public class SparqlConstruct extends AbstractDpu<SparqlConstructConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(SparqlConstruct.class);

    private static final int MAX_GRAPH_COUNT = 1000;

    @DataUnit.AsInput(name = "input")
    public RDFDataUnit rdfInput;

    @DataUnit.AsOutput(name = "output")
    public WritableRDFDataUnit rdfOutput;

    @ExtensionInitializer.Init
    public RdfValidation rdfValidation;

    @ExtensionInitializer.Init
    public RdfProfiler rdfProfiler;

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    @ExtensionInitializer.Init(param = "eu.unifiedviews.plugins.transformer.sparql.SPARQLConfig__V1")
    public ConfigurationUpdate _ConfigurationUpdate;

    public SparqlConstruct() {
        super(SparqlConstructVaadinDialog.class,
                ConfigHistory.history(SPARQLConfig_V1.class).addCurrent(SparqlConstructConfig_V1.class));
    }

    @Override
    protected void innerExecute() throws DPUException {
        if (useDataset()) {
            ContextUtils.sendShortInfo(ctx, "SparqlConstruct.execute.openrdfMode");
        } else {
            ContextUtils.sendShortInfo(ctx, "SparqlConstruct.execute.virtuosoMode");
        }
        // Update query ie. substitute constract with insert.
        String query = config.getQuery();
        if (query == null || query.isEmpty()) {
            throw ContextUtils.dpuException(ctx, "SparqlConstruct.execute.exception.emptyQuery");
        }
        // Modify query - we always do inserts.
        query = query.replaceFirst("(?i)CONSTRUCT", "INSERT");
        // Get graphs.
        final List<RDFDataUnit.Entry> sourceEntries = getInputEntries(rdfInput);
        // Execute.
        executeUpdateQuery(query, sourceEntries);
    }

    /**
     * Get connection and use it to execute given query. Based on user option the query is executed over one
     * or over multiple graphs.
     *
     * @param query
     * @param sourceEntries
     * @param targetgraph
     * @throws DPUException
     */
    protected void executeUpdateQuery(final String query, final List<RDFDataUnit.Entry> sourceEntries)
            throws DPUException {
        final List<IRI> outputGraphs = new LinkedList<>();
        // Execute based on configuration.
        if (config.isPerGraph()) {
            // Execute one graph at time.
            ContextUtils.sendInfo(ctx, "SparqlConstruct.execute.perGraphMode", "SparqlConstruct.execute.graphCount", sourceEntries.size());
            // Execute one query per graph, the target graph is always the same.
            int counter = 1;
            for (final RDFDataUnit.Entry sourceEntry : sourceEntries) {
                LOG.info("Executing {}/{}", counter++, sourceEntries.size());
                // For each input graph prepare output graph.
                final IRI targetGraph = faultTolerance.execute(new FaultTolerance.ActionReturn<IRI>() {

                    @Override
                    public IRI action() throws Exception {
                        final IRI outputUri = createOutputGraph(sourceEntry);
                        LOG.info("   {} -> {}", sourceEntry.getDataGraphURI(), outputUri);
                        return outputUri;
                    }

                });
                // Execute query 1 -> 1.
                faultTolerance.execute(rdfInput, new FaultTolerance.ConnectionAction() {

                    @Override
                    public void action(RepositoryConnection connection) throws Exception {
                        executeUpdateQuery(query, Arrays.asList(sourceEntry), targetGraph, connection);
                    }

                });
                outputGraphs.add(targetGraph);
            }
        } else {
            // All graph at once, just check size.
            if (sourceEntries.size() > MAX_GRAPH_COUNT) {
                throw ContextUtils.dpuException(ctx, "SparqlConstruct.execute.exception.tooManyGraphs", MAX_GRAPH_COUNT, sourceEntries.size());
            }
            ContextUtils.sendInfo(ctx, "SparqlConstruct.execute.allGraphMode", "SparqlConstruct.execute.allGraphMode.count", sourceEntries.size());
            // Prepare single output graph.
            final IRI targetGraph = faultTolerance.execute(new FaultTolerance.ActionReturn<IRI>() {

                @Override
                public IRI action() throws Exception {
                    return createOutputGraph();
                }

            });
            // Execute query m -> 1.
            faultTolerance.execute(rdfInput, new FaultTolerance.ConnectionAction() {

                @Override
                public void action(RepositoryConnection connection) throws Exception {
                    executeUpdateQuery(query, sourceEntries, targetGraph, connection);
                }

            });
            outputGraphs.add(targetGraph);
        }
        // Summmary message.
        long inputSize = getTriplesCount(rdfOutput, faultTolerance.execute(new FaultTolerance.ActionReturn<IRI[]>() {

            @Override
            public IRI[] action() throws Exception {
                return RdfDataUnitUtils.asGraphs(sourceEntries);
            }
        }));
        long outputSize = getTriplesCount(rdfOutput, outputGraphs.toArray(new IRI[0]));

        ContextUtils.sendShortInfo(ctx, "SparqlConstruct.execute.report", inputSize, outputSize);
    }

    /**
     * Execute given query.
     *
     * @param query
     * @param sourceEntries
     *            USING graphs.
     * @param targetGraph
     *            WITH graphs.
     * @param connection
     * @throws eu.unifiedviews.dpu.DPUException
     * @throws eu.unifiedviews.dataunit.DataUnitException
     */
    protected void executeUpdateQuery(String query, final List<RDFDataUnit.Entry> sourceEntries,
            IRI targetGraph,
            RepositoryConnection connection) throws DPUException, DataUnitException {
        // Prepare query.
        if (!useDataset()) {
            if (Pattern.compile(Pattern.quote("DELETE"), Pattern.CASE_INSENSITIVE).matcher(query).find()) {
                query = query.replaceFirst("(?i)DELETE", prepareWithClause(targetGraph) + " DELETE");
            } else {
                query = query.replaceFirst("(?i)INSERT", prepareWithClause(targetGraph) + " INSERT");
            }
            query = query.replaceFirst("(?i)WHERE", prepareUsingClause(sourceEntries) + "WHERE");
        }
        LOG.debug("Query to execute: {}", query);
        try {
            // Execute query.
            final Update update = connection.prepareUpdate(QueryLanguage.SPARQL, query);
            if (useDataset()) {
                final DatasetImpl dataset = new DatasetImpl();
                for (RDFDataUnit.Entry entry : sourceEntries) {
                    dataset.addDefaultGraph(entry.getDataGraphURI());
                }
                dataset.addDefaultRemoveGraph(targetGraph);
                dataset.setDefaultInsertGraph(targetGraph);
                update.setDataset(dataset);
            }
            update.execute();
        } catch (RepositoryException | MalformedQueryException | UpdateExecutionException ex) {
            throw ContextUtils.dpuException(ctx, ex, "SparqlConstruct.execute.exception.updateExecute");
        }
    }

    /**
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
            throw ContextUtils.dpuException(ctx, ex, "SparqlConstruct.execute.exception.addGraph");
        }
    }

    /**
     * @param symbolicName
     * @return New output graph.
     * @throws DPUException
     */
    protected IRI createOutputGraph(RDFDataUnit.Entry entry) throws DPUException {
        final String suffix = "/" + ctx.getExecMasterContext().getDpuContext().getDpuInstanceId().toString();
        try {
            return rdfOutput.addNewDataGraph(entry.getSymbolicName() + suffix);
        } catch (DataUnitException ex) {
            throw ContextUtils.dpuException(ctx, ex, "SparqlConstruct.execute.exception.addGraph");
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
     * @param entries
     *            List of entries to use.
     * @return Using clause for SPARQL insert, based on input graphs.
     * @throws DPUException
     */
    protected String prepareUsingClause(final List<RDFDataUnit.Entry> entries) throws DPUException {
        return faultTolerance.execute(new FaultTolerance.ActionReturn<String>() {

            @Override
            public String action() throws Exception {
                final StringBuilder usingClause = new StringBuilder();
                for (RDFDataUnit.Entry entry : entries) {
                    usingClause.append("USING <");
                    try {
                        usingClause.append(entry.getDataGraphURI().stringValue());
                    } catch (DataUnitException ex) {
                        throw ContextUtils.dpuException(ctx, ex, "SparqlConstruct.execute.exception.internal");
                    }
                    usingClause.append("> \n");
                }
                return usingClause.toString();
            }

        });
    }

    /**
     * @param dataUnit
     * @return Data graphs in given DataUnit.
     * @throws DPUException
     */
    protected List<RDFDataUnit.Entry> getInputEntries(final RDFDataUnit dataUnit) throws DPUException {
        return faultTolerance.execute(new FaultTolerance.ActionReturn<List<RDFDataUnit.Entry>>() {

            @Override
            public List<RDFDataUnit.Entry> action() throws Exception {
                return DataUnitUtils.getEntries(dataUnit, RDFDataUnit.Entry.class);
            }
        });
    }

    protected final boolean useDataset() {
        // Should be removed once bug in Sesame or Virtuoso is fixex.
        return System.getProperty(MetadataUtilsInstance.ENV_PROP_VIRTUOSO) == null;
    }

    /**
     * @param dataUnit
     * @param entries
     * @return Number of triples in given entries.
     */
    protected Long getTriplesCount(final RDFDataUnit dataUnit, final IRI[] graphs)
            throws DPUException {
        return faultTolerance.execute(new FaultTolerance.ActionReturn<Long>() {

            @Override
            public Long action() throws Exception {
                RepositoryConnection connection = null;
                Long size = 0l;
                try {
                    connection = dataUnit.getConnection();
                    size = connection.size(graphs);
                } finally {
                    if (connection != null) {
                        connection.close();
                    }
                }
                return size;
            }

        });
    }

}
