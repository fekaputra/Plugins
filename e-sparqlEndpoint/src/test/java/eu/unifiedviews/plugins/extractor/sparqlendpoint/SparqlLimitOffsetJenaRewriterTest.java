package eu.unifiedviews.plugins.extractor.sparqlendpoint;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.helpers.dataunit.rdf.RDFHelper;
import eu.unifiedviews.helpers.dpu.test.config.ConfigurationBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.eclipse.rdf4j.repository.util.RDFInserter;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class SparqlLimitOffsetJenaRewriterTest {
    static class BNodeIgnoreStatementComparator implements Comparator<Statement> {
        /**
         * A thread-safe pre-instantiated instance of StatementComparator.
         */
        private final static BNodeIgnoreStatementComparator INSTANCE = new BNodeIgnoreStatementComparator();

        /**
         * @return A thread-safe pre-instantiated instance of StatementComparator.
         */
        public final static BNodeIgnoreStatementComparator getInstance() {
            return INSTANCE;
        }

        public final static int BEFORE = -1;

        public final static int EQUALS = 0;

        public final static int AFTER = 1;

        @Override
        public int compare(Statement first, Statement second) {
            // Cannot use Statement.equals as it does not take Context into account,
            // but can check for reference equality (==)
            if (first == second) {
                return EQUALS;
            }

            if (first.getSubject().equals(second.getSubject())) {
                if (first.getPredicate().equals(second.getPredicate())) {
                    if (first.getObject().equals(second.getObject())) {
                        // Context is the only part of a statement that should legitimately be null
                        return EQUALS;
                    } else {
                        return BNodeIgnoreValueComparator.getInstance().compare(first.getObject(), second.getObject());
                    }
                } else {
                    return BNodeIgnoreValueComparator.getInstance().compare(first.getPredicate(), second.getPredicate());
                }
            } else {
                return BNodeIgnoreValueComparator.getInstance().compare(first.getSubject(), second.getSubject());
            }
        }
    }

    static class BNodeIgnoreValueComparator implements Comparator<Value> {

        /**
         * A thread-safe pre-instantiated instance of ValueComparator.
         */
        private final static BNodeIgnoreValueComparator INSTANCE = new BNodeIgnoreValueComparator();

        /**
         * A thread-safe pre-instantiated instance of ValueComparator.
         */
        public final static BNodeIgnoreValueComparator getInstance() {
            return INSTANCE;
        }

        public final static int BEFORE = -1;

        public final static int EQUALS = 0;

        public final static int AFTER = 1;

        /**
         * Sorts in the order nulls&gt;BNodes&gt;URIs&gt;Literals
         * <p>
         * This is due to the fact that nulls are only applicable to contexts, and according to the OpenRDF documentation, the type of the null cannot be
         * sufficiently distinguished from any other Value to make an intelligent comparison to other Values:
         * </p>
         * <p>
         * BNodes are sorted according to the lexical compare of their identifiers, which provides a way to sort statements with the same BNodes in the same
         * positions, near each other BNode sorting is not specified across sessions
         * </p>
         */
        @Override
        public int compare(Value first, Value second) {
            if (first == null) {
                if (second == null) {
                    return EQUALS;
                } else {
                    return BEFORE;
                }
            } else if (second == null) {
                // always sort null Values before others, so if the second is null,
                // but the first wasn't, sort the first after the second
                return AFTER;
            }

            if (first == second || first.equals(second)) {
                return EQUALS;
            }

            if (first instanceof BNode) {
                if (second instanceof BNode) {
                    return EQUALS;
                } else {
                    return BEFORE;
                }
            } else if (second instanceof BNode) {
                // sort BNodes before other things, and first was not a BNode
                return AFTER;
            } else if (first instanceof IRI) {
                if (second instanceof IRI) {
                    return ((IRI) first).stringValue().compareTo(((IRI) second).stringValue());
                } else {
                    return BEFORE;
                }
            } else if (second instanceof IRI) {
                // sort URIs before Literals
                return AFTER;
            }
            // they must both be Literal's, so sort based on the lexical value of the Literal
            else {
                Literal firstLiteral = (Literal) first;
                Literal secondLiteral = (Literal) second;
                int cmp = firstLiteral.getLabel().compareTo(secondLiteral.getLabel());

                if (EQUALS == cmp) {
                    String firstLang = firstLiteral.getLanguage().orElse(null);
                    String secondLang = secondLiteral.getLanguage().orElse(null);
                    if (null != firstLang) {
                        if (null != secondLang) {
                            return firstLang.compareTo(secondLang);
                        } else {
                            return AFTER;
                        }
                    } else if (null != secondLang) {
                        return BEFORE;
                    }

                    IRI firstType = firstLiteral.getDatatype();
                    IRI secondType = secondLiteral.getDatatype();
                    if (null == firstType) {
                        if (null == secondType) {
                            return EQUALS;
                        } else {
                            return BEFORE;
                        }
                    } else if (null == secondType) {
                        return AFTER;
                    } else {
                        return firstType.stringValue().compareTo(secondType.stringValue());
                    }
                } else {
                    return cmp;
                }
            }
        }
    }

    static String baseURI = "http://default";

    static String query1;

    static String query2;

    static String query3;

    static String query4;

    static String query5;

    static String query6;

    static String query7;

    static String queryA;

    static String queryB;

    static String result1;

    static String result2;

    static String result3;

    static String result4;

    static String result5;

    static String result6;

    static String result7;

    static String resultA;

    static String resultB;

    static Map<String, String> queries = new LinkedHashMap<>();

    static int[] expSizes = { 100, 100, 100, 10, 101, 100, 100, 10, 148 };

    static int[] limSizes = { 100, 100, 100, 10, 100, 100, 100, 10, 100 };

    static Pattern anonStripper = Pattern.compile("(_anon-)(.{36})");

    @BeforeClass
    public static void init() throws IOException {
        query1 = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("query1.txt"));
        query2 = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("query2.txt"));
        query3 = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("query3.txt"));
        query4 = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("query4.txt"));
        query5 = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("query5.txt"));
        query6 = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("query6.txt"));
        query7 = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("query7.txt"));
        queryA = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("queryA.txt"));
        queryB = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("queryB.txt"));
        result1 = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("result1.txt"));
        result2 = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("result2.txt"));
        result3 = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("result3.txt"));
        result4 = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("result4.txt"));
        result5 = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("result5.txt"));
        result6 = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("result6.txt"));
        result7 = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("result7.txt"));
        resultA = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("resultA.txt"));
        resultB = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("resultB.txt"));
        queries.put(query1, result1);
        queries.put(query2, result2);
        queries.put(query3, result3);
        queries.put(query4, result4);
        queries.put(query5, result5);
        queries.put(query6, result6);
        queries.put(query7, result7);
        queries.put(queryA, resultA);
        queries.put(queryB, resultB);

    }

    @Test
    public void jenaTestQuery1() {
        String query = query1;
        Query qc = QueryFactory.create(query);
        System.out.println("Query1:" + qc);
        List<String> result = QueryPagingRewriter2.getCollectedVars(query);
        Assert.assertEquals("[village]", StringUtils.join(result));

        System.out.println("Result1:" + QueryPagingRewriter2.rewriteQuery(query, 100, 0));
    }

    @Test
    public void jenaTestQuery2() {
        String query = query2;
        Query qc = QueryFactory.create(query);
        System.out.println("Query2:" + qc);
        List<String> result = QueryPagingRewriter2.getCollectedVars(query);
        Assert.assertEquals("[village]", StringUtils.join(result));

        System.out.println("Result2:" + QueryPagingRewriter2.rewriteQuery(query, 100, 0));
    }

    @Test
    public void jenaTestQuery3() {
        String query = query3;
        Query qc = QueryFactory.create(query);
        System.out.println("Query3:" + qc);
        List<String> result = QueryPagingRewriter2.getCollectedVars(query);
        Assert.assertEquals("[village]", StringUtils.join(result));

        System.out.println("Result3:" + QueryPagingRewriter2.rewriteQuery(query, 100, 0));
    }

    @Test
    public void jenaTestQuery4() {
        String query = query4;
        Query qc = QueryFactory.create(query);
        System.out.println("Query4:" + qc);
        List<String> result = QueryPagingRewriter2.getCollectedVars(query);
        Assert.assertEquals("[village]", StringUtils.join(result));

        System.out.println("Result4:" + QueryPagingRewriter2.rewriteQuery(query, 100, 0));
    }

    @Test
    public void jenaTestQuery5() {
        String query = query5;
        Query qc = QueryFactory.create(query);
        System.out.println("Query5:" + qc);
        List<String> result = QueryPagingRewriter2.getCollectedVars(query);
        Assert.assertEquals("[label, village]", StringUtils.join(result));

        System.out.println("Result5:" + QueryPagingRewriter2.rewriteQuery(query, 100, 0));
    }

    @Test
    public void jenaTestQueryA() {
        String query = queryA;
        Query qc = QueryFactory.create(query);
        System.out.println("QueryA:" + qc);
        List<String> result = QueryPagingRewriter2.getCollectedVars(query);
        Assert.assertEquals("[village]", StringUtils.join(result));

        System.out.println("ResultA:" + QueryPagingRewriter2.rewriteQuery(query, 100, 0));
    }

    @Test
    public void jenaTestQueryB() {
        String query = queryB;
        Query qc = QueryFactory.create(query);
        System.out.println("QueryB:" + qc);
        List<String> result = QueryPagingRewriter2.getCollectedVars(query);
        Assert.assertEquals("[label, village]", StringUtils.join(result));

        System.out.println("ResultB:" + QueryPagingRewriter2.rewriteQuery(query, 100, 0));
    }

    //@Test
    public void rewriteTestQuery() throws RepositoryException, QueryEvaluationException, RDFHandlerException, MalformedQueryException {
        int i = 0;
        for (Map.Entry<String, String> entry : queries.entrySet()) {
            String query = entry.getKey();
            String expected = entry.getValue();
            Query qc = QueryFactory.create(query);
            Query qcExpected = QueryFactory.create(expected);
            System.out.println("Query" + (i + 1) + ":" + qc);
            System.out.println("Expected" + (i + 1) + ":" + qcExpected);

            String rewritten = QueryPagingRewriter2.rewriteQuery(query, limSizes[i], 0);
            Query qcRewritten = QueryFactory.create(rewritten);
            System.out.println("Rewritten" + (i + 1) + ":" + qcRewritten);

            SPARQLRepository sparql = new SPARQLRepository("http://dbpedia.org/sparql");
            sparql.initialize();

            RepositoryConnection sparqlCon = sparql.getConnection();
            GraphQuery graphQueryExp = sparqlCon.prepareGraphQuery(QueryLanguage.SPARQL, expected);
            StatementCollector scExp = new StatementCollector();
            graphQueryExp.evaluate(scExp);
            List<Statement> resultStatementsExp = new ArrayList<>(scExp.getStatements());
            Collections.<Statement> sort(resultStatementsExp, new BNodeIgnoreStatementComparator());

            GraphQuery graphQueryRew = sparqlCon.prepareGraphQuery(QueryLanguage.SPARQL, rewritten);
            StatementCollector scRew = new StatementCollector();
            graphQueryRew.evaluate(scRew);
            List<Statement> resultStatementsRew = new ArrayList<>(scRew.getStatements());
            Collections.<Statement> sort(resultStatementsRew, new BNodeIgnoreStatementComparator());
            Assert.assertEquals(expSizes[i], resultStatementsExp.size());

            Assert.assertEquals(resultStatementsExp.size(), resultStatementsRew.size());
            Iterator<Statement> itExp = resultStatementsExp.iterator();
            Iterator<Statement> itRew = resultStatementsRew.iterator();
            while (itExp.hasNext()) {
                Assert.assertEquals(0, new BNodeIgnoreStatementComparator().compare(itExp.next(), itRew.next()));
            }

            ValueFactory vf = SimpleValueFactory.getInstance();

            IRI uriExp = vf.createIRI("http://exp");
            IRI uriRew = vf.createIRI("http://rew");
            SailRepository memoryRepository = new SailRepository(new MemoryStore());
            memoryRepository.initialize();
            RepositoryConnection memCon = memoryRepository.getConnection();
            RDFInserter scExp2 = new RDFInserter(memCon);
            scExp2.enforceContext(uriExp);
            graphQueryExp.evaluate(scExp2);

            RDFInserter scRew2 = new RDFInserter(memCon);
            scRew2.enforceContext(uriRew);
            graphQueryRew.evaluate(scRew2);

            Assert.assertEquals(memCon.size(uriExp), memCon.size(uriRew));

            RepositoryResult<Statement> resultExp = memCon.getStatements(null, null, null, false, uriExp);
            resultStatementsExp = new ArrayList<>();
            while (resultExp.hasNext()) {
                resultStatementsExp.add(resultExp.next());
            }
            Collections.<Statement> sort(resultStatementsExp, new BNodeIgnoreStatementComparator());

            RepositoryResult<Statement> resultRew = memCon.getStatements(null, null, null, false, uriRew);
            resultStatementsRew = new ArrayList<>();
            while (resultRew.hasNext()) {
                resultStatementsRew.add(resultRew.next());
            }
            Collections.<Statement> sort(resultStatementsRew, new BNodeIgnoreStatementComparator());
            Assert.assertEquals(resultStatementsExp.size(), resultStatementsRew.size());
            itExp = resultStatementsExp.iterator();
            itRew = resultStatementsRew.iterator();
            while (itExp.hasNext()) {
                Statement exp = itExp.next();
                Statement rew = itRew.next();
                Assert.assertEquals(0, new BNodeIgnoreStatementComparator().compare(exp, rew));
            }
            i++;
        }
    }

    //@Test
    public void executeDPUTest4() throws Exception {
        // Prepare config.
        SparqlEndpointConfig_V1 config = new SparqlEndpointConfig_V1();
        config.setChunkSize(null);
        config.setEndpoint("http://dbpedia.org/sparql");
        config.setQuery(query4);
        // Prepare config.
        SparqlEndpointConfig_V1 configSlice = new SparqlEndpointConfig_V1();
        configSlice.setChunkSize(5);
        configSlice.setEndpoint("http://dbpedia.org/sparql");
        configSlice.setQuery(query4);

        // Spa DPU.
        SparqlEndpoint dpu = new SparqlEndpoint();
        dpu.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());
        SparqlEndpoint dpuSlice = new SparqlEndpoint();
        dpuSlice.configure((new ConfigurationBuilder()).setDpuConfiguration(configSlice).toString());

        // Prepare test environment.
        TestEnvironment environment = new TestEnvironment();
        TestEnvironment environmentSlice = new TestEnvironment();
        // Prepare data unit.
        WritableRDFDataUnit output = environment.createRdfOutput("output", false);
        // Prepare data unit.
        WritableRDFDataUnit outputSlice = environmentSlice.createRdfOutput("output", false);

        try {
            RepositoryConnection connection;
            RepositoryConnection connectionSlice = outputSlice.getConnection();
            // Run.
            environment.run(dpu);
            environmentSlice.run(dpuSlice);
            connection = output.getConnection();
            System.out.println(connection.size(RDFHelper.getGraphsURIArray(output)));
            Assert.assertEquals(connection.size(RDFHelper.getGraphsURIArray(output)), connectionSlice.size(RDFHelper.getGraphsURIArray(outputSlice)));

        } finally {
            // Release resources.
            environment.release();
            environmentSlice.release();
        }
    }

    //@Test
    public void executeDPUTest1() throws Exception {
        // Prepare config.
        SparqlEndpointConfig_V1 config = new SparqlEndpointConfig_V1();
        config.setChunkSize(null);
        config.setEndpoint("http://dbpedia.org/sparql");
        config.setQuery(query1);
        // Prepare config.
        SparqlEndpointConfig_V1 configSlice = new SparqlEndpointConfig_V1();
        configSlice.setChunkSize(5000);
        configSlice.setEndpoint("http://dbpedia.org/sparql");
        configSlice.setQuery(query1);

        // Spa DPU.
        SparqlEndpoint dpu = new SparqlEndpoint();
        dpu.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());
        SparqlEndpoint dpuSlice = new SparqlEndpoint();
        dpuSlice.configure((new ConfigurationBuilder()).setDpuConfiguration(configSlice).toString());

        // Prepare test environment.
        TestEnvironment environment = new TestEnvironment();
        TestEnvironment environmentSlice = new TestEnvironment();
        // Prepare data unit.
        WritableRDFDataUnit output = environment.createRdfOutput("output", false);
        // Prepare data unit.
        WritableRDFDataUnit outputSlice = environmentSlice.createRdfOutput("output", false);

        try {
            RepositoryConnection connection = output.getConnection();
            RepositoryConnection connectionSlice = outputSlice.getConnection();
            // Run.
            environment.run(dpu);
            environmentSlice.run(dpuSlice);
            connection = output.getConnection();
            System.out.println(connection.size(RDFHelper.getGraphsURIArray(output)));
            System.out.println(connection.size(RDFHelper.getGraphsURIArray(outputSlice)));
            Assert.assertEquals(connection.size(RDFHelper.getGraphsURIArray(output)), connection.size(RDFHelper.getGraphsURIArray(outputSlice)));
        } finally {
            // Release resources.
            environment.release();
            environmentSlice.release();
        }
    }

}
