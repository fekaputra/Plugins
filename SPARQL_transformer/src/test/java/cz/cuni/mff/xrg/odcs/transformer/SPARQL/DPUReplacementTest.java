package cz.cuni.mff.xrg.odcs.transformer.SPARQL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import cz.cuni.mff.xrg.odcs.rdf.RDFDataUnit;

/**
 * @author Jiri Tomes
 */
public class DPUReplacementTest {
    private static final Logger LOG = LoggerFactory.getLogger(DPUReplacementTest.class);

    /**
     * Test DPU replacement on SPARQL CONSTRUCT query.
     */
    @Test
    public void constructQueryTest() {

        String query = "CONSTRUCT {?s ?p ?o. ?x ?y ?z} where "
                + "{ graph ?g_input {?s ?p ?o} graph ?g_optional1 {?x ?y ?z}}";

        boolean isConstruct = true;

        // prepare test environment
        TestEnvironment env = new TestEnvironment();
        RepositoryConnection connection = null;
        RepositoryConnection connection2 = null;
        RepositoryConnection connection3 = null;

        try {

            SPARQLTransformer transformer = new SPARQLTransformer();
            SPARQLTransformerConfig config = new SPARQLTransformerConfig(
                    query, isConstruct);

            transformer.configureDirectly(config);

            RDFDataUnit input = env.createRdfInput("input", false);
            RDFDataUnit optional = env.createRdfInput("optional1", false);
            connection = input.getConnection();
            ValueFactory factory = connection.getValueFactory();
            connection.add(factory.createURI("http://s"), factory.createURI(
                    "http://p"), factory.createURI("http://o"), input.getDataGraph());
            connection.add(factory.createURI("http://subject"), factory.createURI(
                    "http://predicate"), factory.createURI("http://object"), input.getDataGraph());

            connection2 = optional.getConnection();
            ValueFactory factory2 = connection2.getValueFactory();
            connection2.add(factory2.createBNode("n25"), factory2
                    .createURI("http://hasName"), factory2.createLiteral("NAME"), optional.getDataGraph());

            assertEquals(2L, connection.size(input.getDataGraph()));
            assertEquals(1L, connection2.size(optional.getDataGraph()));

            RDFDataUnit output = env.createRdfOutput("output", false);

            env.run(transformer);

            connection3 = output.getConnection();
            assertEquals("Count of triples are not same", 3L, connection3.size(output.getDataGraph()));
            env.release();

        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Throwable ex) {
                    LOG.warn("Error closing connection", ex);
                }
            }
            if (connection2 != null) {
                try {
                    connection2.close();
                } catch (Throwable ex) {
                    LOG.warn("Error closing connection", ex);
                }
            }
            if (connection3 != null) {
                try {
                    connection3.close();
                } catch (Throwable ex) {
                    LOG.warn("Error closing connection", ex);
                }
            }
            env.release();
        }
    }

    /**
     * Test DPU replacement on SPARQL UPDATE query.
     */
    @Test
    public void updateQueryTest() {
        String query = "PREFIX foaf:  <http://xmlns.com/foaf/0.1/> \n"
                + "INSERT { ?person foaf:givenName \"William\" }\n"
                + "WHERE {"
                + " graph ?g_optional1 {?person foaf:givenName \"Bill\" }"
                + " graph ?g_input {?person ?x ?y }"
                + "} ";

        boolean isConstruct = false;

        String expectedObjectName = "William";

        // prepare test environment
        TestEnvironment env = new TestEnvironment();
        RepositoryConnection connection = null;
        RepositoryConnection connection2 = null;
        RepositoryConnection connection3 = null;

        try {

            SPARQLTransformer transformer = new SPARQLTransformer();
            SPARQLTransformerConfig config = new SPARQLTransformerConfig(
                    query, isConstruct);

            transformer.configureDirectly(config);

            RDFDataUnit input = env.createRdfInput("input", false);
            RDFDataUnit optional = env.createRdfInput("optional1", false);

            connection = input.getConnection();
            ValueFactory factory = connection.getValueFactory();
            connection.add(factory.createURI("http://person"), factory.createURI(
                    "http://predicate"), factory.createURI("http://object"), input.getDataGraph());

            connection2 = optional.getConnection();
            ValueFactory factory2 = connection2.getValueFactory();

            connection2.add(factory2.createURI("http://person"), factory2
                    .createURI("http://xmlns.com/foaf/0.1/givenName"), factory2
                    .createLiteral("Bill"), optional.getDataGraph());

            assertEquals(1L, connection.size(input.getDataGraph()));
            assertEquals(1L, connection2.size(optional.getDataGraph()));

            RDFDataUnit output = env.createRdfOutput("output", false);

            env.run(transformer);

            connection3 = output.getConnection();
            assertEquals("Count of triples are not same", 3L, connection3.size(output.getDataGraph()));

            RepositoryResult<Statement> outputTriples = connection3.getStatements(null, null, null, true, output.getDataGraph());

            boolean newInsertedTripleFound = false;
            while (outputTriples.hasNext()) {
                Statement next = outputTriples.next();
                if (expectedObjectName.equals(next.getObject().stringValue())) {
                    newInsertedTripleFound = true;
                    break;
                }
            }

            assertTrue("New inserted triple not found", newInsertedTripleFound);

        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Throwable ex) {
                    LOG.warn("Error closing connection", ex);
                }
            }
            if (connection2 != null) {
                try {
                    connection2.close();
                } catch (Throwable ex) {
                    LOG.warn("Error closing connection", ex);
                }
            }
            if (connection3 != null) {
                try {
                    connection3.close();
                } catch (Throwable ex) {
                    LOG.warn("Error closing connection", ex);
                }
            }

            env.release();
        }
    }
}
