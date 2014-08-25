package eu.unifiedviews.plugins.transformer.sparql;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.model.Graph;
import org.openrdf.model.URI;
import org.openrdf.query.Dataset;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.QueryResults;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU.AsTransformer;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUContext.MessageType;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.rdfhelper.RDFHelper;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import eu.unifiedviews.helpers.dpu.config.ConfigDialogProvider;
import eu.unifiedviews.helpers.dpu.config.ConfigurableBase;

/**
 * SPARQL Transformer.
 *
 * @author Jiri Tomes
 * @author Petyr
 * @author tknap
 */
@AsTransformer
public class SPARQL
        extends ConfigurableBase<SPARQLConfig_V1>
        implements ConfigDialogProvider<SPARQLConfig_V1> {

    private final Logger LOG = LoggerFactory.getLogger(SPARQL.class);

    public static final String[] DPUNames = { "input", "optional1", "optional2", "optional3" };

    /**
     * The repository input for SPARQL transformer.
     */
    @DataUnit.AsInput(name = "input")
    public RDFDataUnit intputDataUnit;

    //three other optional inputs, which may be used in the queries
    /**
     * The first repository optional input for SPARQL transformer.
     */
//    @InputDataUnit(name = "optional1", optional = true)
//    public RDFDataUnit intputOptional1;

    /**
     * The second repository optional input for SPARQL transformer.
     */
//    @InputDataUnit(name = "optional2", optional = true)
//    public RDFDataUnit intputOptional2;

    /**
     * The third repository optional input for SPARQL transformer.
     */
//    @InputDataUnit(name = "optional3", optional = true)
//    public RDFDataUnit intputOptional3;

    /**
     * The repository output for SPARQL transformer.
     */
    @DataUnit.AsOutput(name = "output")
    public WritableRDFDataUnit outputDataUnit;

    public SPARQL() {
        super(SPARQLConfig_V1.class);
    }

    private Dataset createGraphDataSet(List<RDFDataUnit> inputs) throws DPUException {
        CleverDataset dataSet = new CleverDataset();

        try {
            for (RDFDataUnit repository : inputs) {
                if (repository != null) {
                    dataSet.addDefaultGraphs(RDFHelper.getGraphsURISet(repository));
                    dataSet.addNamedGraphs(RDFHelper.getGraphsURISet(repository));
                }
            }
        } catch (DataUnitException ex) {
            throw new DPUException(ex);
        }
        if (dataSet.getDefaultGraphs().isEmpty()) {
            throw new DPUException("Empty dataset");
        }
        return dataSet;
    }

    private List<RDFDataUnit> getInputs() {
        List<RDFDataUnit> inputs = new ArrayList<>();

        addInput(inputs, intputDataUnit);
//        addInput(inputs, intputOptional1);
//        addInput(inputs, intputOptional2);
//        addInput(inputs, intputOptional3);

        return inputs;
    }

    private void addInput(List<RDFDataUnit> inputs, RDFDataUnit nextInput) {
        if (inputs != null && nextInput != null) {
            inputs.add(nextInput);
        }
    }

    /**
     * Execute the SPARQL transformer.
     *
     * @param context
     *            SPARQL transformer context.
     * @throws DataUnitException
     *             if this DPU fails.
     * @throws DPUException
     *             if this DPU fails.
     */
    @Override
    public void execute(DPUContext context)
            throws DPUException {

        //GET ALL possible inputs
        List<RDFDataUnit> inputs = getInputs();

        final List<SPARQLQueryPair> queryPairs = config.getQueryPairs();

        if (queryPairs == null) {
            context.sendMessage(MessageType.ERROR,
                    "All queries for SPARQL transformer are null values");
        } else {
            if (queryPairs.isEmpty()) {
                context.sendMessage(MessageType.ERROR,
                        "Queries for SPARQL transformer are empty",
                        "SPARQL transformer must constains at least one SPARQL query");
            }
        }

        //if merge input - depend on type of quries.
        boolean isFirstUpdateQuery = true;

        int queryCount = 0;
        URI outputGraphName;
        try {
            outputGraphName = outputDataUnit.addNewDataGraph("sparqlLegacyOutput");
        } catch (DataUnitException ex) {
            throw new DPUException(ex);
        }

        for (SPARQLQueryPair nextPair : queryPairs) {

            queryCount++;
            String updateQuery = nextPair.getSPARQLQuery();
            boolean isConstructQuery = nextPair.isConstructType();

            if (updateQuery == null) {
                context.sendMessage(MessageType.ERROR,
                        "Query number " + queryCount + " is not defined");
            } else if (updateQuery.trim().isEmpty()) {
                context.sendMessage(MessageType.ERROR,
                        "Query number " + queryCount + " is not defined",
                        "SPARQL transformer must constain at least one SPARQL (Update) query");
            }

            if (isConstructQuery) {
                isFirstUpdateQuery = false;
                //creating newConstruct replaced query
                PlaceholdersHelper placeHolders = new PlaceholdersHelper(
                        context);
                String constructQuery = placeHolders.getReplacedQuery(
                        updateQuery,
                        inputs);

                //execute given construct query
                Dataset dataSet = createGraphDataSet(inputs);

                RepositoryConnection connectionInput = null;
                Graph graph = null;
                try {
                    connectionInput = intputDataUnit.getConnection();
                    graph = executeConstructQuery(connectionInput, constructQuery, dataSet);

                    if (graph != null) {
                        connectionInput.add(graph, outputGraphName);
                    }
                } catch (RepositoryException | DataUnitException ex) {
                    LOG.error("Could not add triples from graph", ex);
                } finally {
                    if (connectionInput != null) {
                        try {
                            connectionInput.close();
                        } catch (RepositoryException ex) {
                            context.sendMessage(MessageType.WARNING, ex.getMessage(), ex.fillInStackTrace().toString());
                        }
                    }
                }
            } else {

                PlaceholdersHelper placeHolders = new PlaceholdersHelper(
                        context);

                String replacedUpdateQuery = placeHolders.getReplacedQuery(
                        updateQuery,
                        inputs);

                if (isFirstUpdateQuery) {

                    isFirstUpdateQuery = false;

                    prepareRepository(inputs, outputGraphName.stringValue());

                }

                CleverDataset dataset = new CleverDataset();
                dataset.addDefaultGraph(outputGraphName);
                dataset.addNamedGraph(outputGraphName);

                RepositoryConnection connection = null;
                try {
                    connection = outputDataUnit.getConnection();
                    executeSPARQLUpdateQuery(connection, replacedUpdateQuery, dataset, outputGraphName);

                } catch (DataUnitException ex) {
                    LOG.error("Could not add triples from graph", ex);

                } finally {
                    if (connection != null) {
                        try {
                            connection.close();
                        } catch (RepositoryException ex) {
                            context.sendMessage(MessageType.WARNING, ex.getMessage(), ex.fillInStackTrace().toString());
                        }
                    }
                }
            }
        }
        RepositoryConnection connection = null;
        try {
            connection = outputDataUnit.getConnection();
            final long beforeTriplesCount = connection.size(RDFHelper.getGraphsURIArray(intputDataUnit));
            final long afterTriplesCount = connection.size(outputGraphName);
            LOG.info("Transformed thanks {} SPARQL queries {} triples into {}",
                    queryCount, beforeTriplesCount, afterTriplesCount);
        } catch (DataUnitException | RepositoryException e) {
            context.sendMessage(MessageType.ERROR,
                    "connection to repository broke down");
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {
                    context.sendMessage(MessageType.WARNING, ex.getMessage(), ex.fillInStackTrace().toString());
                }
            }
        }

    }

    //	TODO michal.klempa this should not be needed anymore
    private void prepareRepository(List<RDFDataUnit> inputs, String targetGraphName) throws DPUException {
        try {
            for (RDFDataUnit input : inputs) {
                addAll(input, outputDataUnit, targetGraphName);
            }
        } catch (DataUnitException ex) {
            throw new DPUException(ex);
        }
    }

    public void addAll(RDFDataUnit source, WritableRDFDataUnit destination, String targetGraphName) throws DataUnitException {
        RepositoryConnection connection = null;
        try {
            connection = destination.getConnection();

            for (URI sourceGraph : RDFHelper.getGraphsURISet(source)) {
                String sourceGraphName = sourceGraph.stringValue();

                LOG.info("Trying to merge {} triples from <{}> to <{}>.",
                        connection.size(sourceGraph), sourceGraphName,
                        targetGraphName);

                String mergeQuery = String.format("ADD <%s> TO <%s>", sourceGraphName,
                        targetGraphName);

                Update update = connection.prepareUpdate(
                        QueryLanguage.SPARQL, mergeQuery);

                update.execute();

                LOG.info("Merged {} triples from <{}> to <{}>.",
                        connection.size(sourceGraph), sourceGraphName,
                        targetGraphName);
            }
        } catch (MalformedQueryException ex) {
            LOG.error("NOT VALID QUERY: {}", ex);
        } catch (RepositoryException | DataUnitException | UpdateExecutionException ex) {
            LOG.error(ex.getMessage(), ex);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {
                    LOG.warn("Error when closing connection", ex);
                    // eat close exception, we cannot do anything clever here
                }
            }
        }
    }

    /**
     * Returns the configuration dialogue for SPARQL transformer.
     *
     * @return the configuration dialogue for SPARQL transformer.
     */
    @Override
    public AbstractConfigDialog<SPARQLConfig_V1> getConfigurationDialog() {
        return new SPARQLVaadinDialog();
    }

    /**
     * Transform RDF in repository by SPARQL updateQuery.
     *
     * @param updateQuery
     *            String value of update SPARQL query.
     * @param dataset
     *            Set of graph URIs used for update query.
     * @throws cz.cuni.mff.xrg.odcs.rdf.exceptions.DPUException
     *             when transformation fault.
     */
    public void executeSPARQLUpdateQuery(RepositoryConnection connection, String updateQuery, Dataset dataset, URI dataGraph)
            throws DPUException {

        try {
            String newUpdateQuery = AddGraphToUpdateQuery(updateQuery, dataGraph);
            Update myupdate = connection.prepareUpdate(QueryLanguage.SPARQL,
                    newUpdateQuery);
            myupdate.setDataset(dataset);

            LOG.debug(
                    "This SPARQL update query is valid and prepared for execution:");
            LOG.debug(newUpdateQuery);

            myupdate.execute();
            //connection.commit();

            LOG.debug("SPARQL update query for was executed successfully");

        } catch (MalformedQueryException e) {

            LOG.debug(e.getMessage());
            throw new DPUException(e.getMessage(), e);

        } catch (UpdateExecutionException ex) {

            final String message = "SPARQL query was not executed !!!";
            LOG.debug(message);
            LOG.debug(ex.getMessage());

            throw new DPUException(message + ex.getMessage(), ex);

        } catch (RepositoryException ex) {
            throw new DPUException(
                    "Connection to repository is not available. "
                            + ex.getMessage(), ex);
        }

    }

    /**
     * @param updateQuery
     *            String value of SPARQL update query.
     * @return String extension of given update query works with set repository
     *         GRAPH.
     */
    public String AddGraphToUpdateQuery(String updateQuery, URI dataGraph) {

        String regex = "(insert|delete)\\s\\{";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(updateQuery.toLowerCase());

        boolean hasResult = matcher.find();
        boolean hasWith = updateQuery.toLowerCase().contains("with");

        if (hasResult && !hasWith) {

            int index = matcher.start();

            String first = updateQuery.substring(0, index);
            String second = updateQuery.substring(index, updateQuery.length());

            String graphName = " WITH <" + dataGraph.stringValue() + "> ";

            String newQuery = first + graphName + second;
            return newQuery;

        } else {

            LOG.debug("WITH graph clause was not added, "
                    + "because the query was: {}", updateQuery);

            regex = "(insert|delete)\\sdata\\s\\{";
            pattern = Pattern.compile(regex);
            matcher = pattern.matcher(updateQuery.toLowerCase());

            hasResult = matcher.find();

            if (hasResult) {

                int start = matcher.start();
                int end = matcher.end();

                String first = updateQuery.substring(0, start);
                String second = updateQuery.substring(end, updateQuery.length());

                String myString = updateQuery.substring(start, end);
                String graphName = myString.replace("{",
                        "{ GRAPH <" + dataGraph.stringValue() + "> {");

                second = second.replaceFirst("}", "} }");
                String newQuery = first + graphName + second;

                return newQuery;

            }
        }
        return updateQuery;
    }

    /**
     * Make construct query over graph URIs in dataSet and return interface
     * Graph as result contains iterator for statements (triples).
     *
     * @param constructQuery
     *            String representation of SPARQL query.
     * @param dataSet
     *            Set of graph URIs used for construct query.
     * @return Interface Graph as result of construct SPARQL query.
     * @throws cz.cuni.mff.xrg.odcs.rdf.exceptions.InvalidQueryException
     *             when query is not valid.
     */
    public Graph executeConstructQuery(RepositoryConnection connection, String constructQuery, Dataset dataSet)
            throws InvalidQueryException {

        try {

            GraphQuery graphQuery = connection.prepareGraphQuery(
                    QueryLanguage.SPARQL,
                    constructQuery);

            graphQuery.setDataset(dataSet);

            LOG.debug("Query {} is valid.", constructQuery);

            try {
                GraphQueryResult result = graphQuery.evaluate();
                LOG.debug("Query {} has not null result.", constructQuery);
                return QueryResults.asModel(result);

            } catch (QueryEvaluationException ex) {
                throw new InvalidQueryException(
                        "This query is probably not valid. " + ex
                                .getMessage(),
                        ex);
            }

        } catch (MalformedQueryException ex) {
            throw new InvalidQueryException(
                    "This query is probably not valid. "
                            + ex.getMessage(), ex);
        } catch (RepositoryException ex) {
            LOG.error("Connection to RDF repository failed. {}",
                    ex.getMessage(), ex);
        }

        throw new InvalidQueryException(
                "Getting GraphQueryResult using SPARQL construct query failed.");
    }

}
