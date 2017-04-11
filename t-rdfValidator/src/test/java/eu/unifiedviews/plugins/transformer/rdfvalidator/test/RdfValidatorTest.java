package eu.unifiedviews.plugins.transformer.rdfvalidator.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;

import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.rdf.RDFHelper;
import eu.unifiedviews.helpers.dpu.test.config.ConfigurationBuilder;
import eu.unifiedviews.plugins.transformer.rdfvalidator.RdfValidator;
import eu.unifiedviews.plugins.transformer.rdfvalidator.RdfValidatorConfig_V1;

public class RdfValidatorTest {

    @Test
    public void testAsk() throws Exception {
        // Prepare config.
        RdfValidatorConfig_V1 config = new RdfValidatorConfig_V1();
        config.setFailExecution(false);
        config.setOutputGraphSymbolicName("output1");
        config.setPerGraph(false);
        config.setQuery("ASK { ?s ?p ?o }");

        // Prepare DPU.
        RdfValidator rdfDataValidator = new RdfValidator();
        rdfDataValidator.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());

        // Prepare test environment.
        TestEnvironment environment = new TestEnvironment();

        // Prepare data unit.
        WritableRDFDataUnit input = environment.createRdfInput("rdfInput", false);
        WritableRDFDataUnit output = environment.createRdfOutput("rdfCopyOfInput", false);


        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("metadata.ttl");

        RepositoryConnection connection = null;
        try {
            connection = input.getConnection();
            URI graph = input.addNewDataGraph("test");
            connection.add(inputStream, "", RDFFormat.TURTLE, graph);
            ByteArrayOutputStream inputBos = new ByteArrayOutputStream();
            connection.export(new TurtleWriter(inputBos), graph);
            // some triples has been loaded
            assertTrue(connection.size(graph) > 0);
            // Run.
            environment.run(rdfDataValidator);

        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {

                }
            }
            // Release resources.
            environment.release();
        }
    }

    @Test(expected = DPUException.class)
    public void testAskFailExec() throws Exception {
        // Prepare config.
        RdfValidatorConfig_V1 config = new RdfValidatorConfig_V1();
        config.setFailExecution(true);
        config.setOutputGraphSymbolicName("output1");
        config.setPerGraph(false);
        config.setQuery("ASK { ?s ?p ?o }");

        // Prepare DPU.
        RdfValidator rdfDataValidator = new RdfValidator();
        rdfDataValidator.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());
        // Prepare test environment.
        TestEnvironment environment = new TestEnvironment();

        // Prepare data unit.
        WritableRDFDataUnit input = environment.createRdfInput("rdfInput", false);
        WritableRDFDataUnit output = environment.createRdfOutput("rdfCopyOfInput", false);

        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("metadata.ttl");

        RepositoryConnection connection = null;
        try {
            connection = input.getConnection();
            URI graph = input.addNewDataGraph("test");
            connection.add(inputStream, "", RDFFormat.TURTLE, graph);
            ByteArrayOutputStream inputBos = new ByteArrayOutputStream();
            connection.export(new TurtleWriter(inputBos), graph);
            // some triples has been loaded
            assertTrue(connection.size(graph) > 0);
            // Run.
            environment.run(rdfDataValidator);

            // verify result
            assertTrue(connection.size(graph) == connection.size(RDFHelper.getGraphsURIArray(output)));
            ByteArrayOutputStream outputBos = new ByteArrayOutputStream();
            connection.export(new TurtleWriter(outputBos), RDFHelper.getGraphsURIArray(output));

            assertEquals(inputBos.toString("UTF-8"), outputBos.toString("UTF-8"));
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {

                }
            }
            // Release resources.
            environment.release();
        }
    }

    @Test(expected = DPUException.class)
    public void testAskPerGraphFailExec() throws Exception {
        // Prepare config.
        RdfValidatorConfig_V1 config = new RdfValidatorConfig_V1();
        config.setFailExecution(true);
        config.setOutputGraphSymbolicName("output1");
        config.setPerGraph(true);
        config.setQuery("ASK { ?s ?p ?o }");

        // Prepare DPU.
        RdfValidator rdfDataValidator = new RdfValidator();
        rdfDataValidator.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());

        // Prepare test environment.
        TestEnvironment environment = new TestEnvironment();

        // Prepare data unit.
        WritableRDFDataUnit input = environment.createRdfInput("rdfInput", false);
        WritableRDFDataUnit output = environment.createRdfOutput("rdfCopyOfInput", false);

        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("metadata.ttl");

        RepositoryConnection connection = null;
        try {
            connection = input.getConnection();
            input.addNewDataGraph("testEmpty");
            URI graph = input.addNewDataGraph("test");
            connection.add(inputStream, "", RDFFormat.TURTLE, graph);
            ByteArrayOutputStream inputBos = new ByteArrayOutputStream();
            connection.export(new TurtleWriter(inputBos), graph);
            // some triples has been loaded
            assertTrue(connection.size(graph) > 0);
            // Run.
            environment.run(rdfDataValidator);

        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {

                }
            }
            // Release resources.
            environment.release();
        }
    }

    @Test
    public void testAskPerGraph() throws Exception {
        // Prepare config.
        RdfValidatorConfig_V1 config = new RdfValidatorConfig_V1();
        config.setFailExecution(false);
        config.setOutputGraphSymbolicName("output1");
        config.setPerGraph(true);
        config.setQuery("ASK { ?s ?p ?o }");

        // Prepare DPU.
        RdfValidator rdfDataValidator = new RdfValidator();
        rdfDataValidator.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());

        // Prepare test environment.
        TestEnvironment environment = new TestEnvironment();

        // Prepare data unit.
        WritableRDFDataUnit input = environment.createRdfInput("rdfInput", false);
        WritableRDFDataUnit output = environment.createRdfOutput("rdfCopyOfInput", false);

        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("metadata.ttl");

        RepositoryConnection connection = null;
        try {
            connection = input.getConnection();
            input.addNewDataGraph("testEmpty");
            URI graph = input.addNewDataGraph("test");
            connection.add(inputStream, "", RDFFormat.TURTLE, graph);
            ByteArrayOutputStream inputBos = new ByteArrayOutputStream();
            connection.export(new TurtleWriter(inputBos), graph);
            // some triples has been loaded
            assertTrue(connection.size(graph) > 0);
            // Run.
            environment.run(rdfDataValidator);

        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {

                }
            }
            // Release resources.
            environment.release();
        }
    }

    @Test
    public void testAskEmptyQueryFailExec() throws Exception {
        // Prepare config.
        RdfValidatorConfig_V1 config = new RdfValidatorConfig_V1();
        config.setFailExecution(true);
        config.setOutputGraphSymbolicName("output1");
        config.setPerGraph(true);
        config.setQuery("ASK { ?s ?s ?s }");

        // Prepare DPU.
        RdfValidator rdfDataValidator = new RdfValidator();
        rdfDataValidator.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());

        // Prepare test environment.
        TestEnvironment environment = new TestEnvironment();

        // Prepare data unit.
        WritableRDFDataUnit input = environment.createRdfInput("rdfInput", false);
        WritableRDFDataUnit output = environment.createRdfOutput("rdfCopyOfInput", false);

        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("metadata.ttl");

        RepositoryConnection connection = null;
        try {
            connection = input.getConnection();
            input.addNewDataGraph("testEmpty");
            URI graph = input.addNewDataGraph("test");
            connection.add(inputStream, "", RDFFormat.TURTLE, graph);
            ByteArrayOutputStream inputBos = new ByteArrayOutputStream();
            connection.export(new TurtleWriter(inputBos), graph);
            // some triples has been loaded
            assertTrue(connection.size(graph) > 0);
            // Run.
            environment.run(rdfDataValidator);

        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {

                }
            }
            // Release resources.
            environment.release();
        }
    }

    @Test
    public void testSelect() throws Exception {
        // Prepare config.
        RdfValidatorConfig_V1 config = new RdfValidatorConfig_V1();
        config.setFailExecution(false);
        config.setOutputGraphSymbolicName("output1");
        config.setPerGraph(false);
        config.setQuery("SELECT  ?s ?p ?o WHERE {?s ?p ?o }");

        // Prepare DPU.
        RdfValidator rdfDataValidator = new RdfValidator();
        rdfDataValidator.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());

        // Prepare test environment.
        TestEnvironment environment = new TestEnvironment();

        // Prepare data unit.
        WritableRDFDataUnit input = environment.createRdfInput("rdfInput", false);
        WritableRDFDataUnit output = environment.createRdfOutput("rdfCopyOfInput", false);

        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("metadata.ttl");

        RepositoryConnection connection = null;
        try {
            connection = input.getConnection();
            URI graph = input.addNewDataGraph("test");
            connection.add(inputStream, "", RDFFormat.TURTLE, graph);
            StringWriter swInput = new StringWriter();
            connection.export(Rio.createWriter(RDFFormat.TURTLE, swInput), graph);
            // some triples has been loaded
            assertTrue(connection.size(graph) > 0);
            // Run.
            environment.run(rdfDataValidator);
            
            StringWriter sw = new StringWriter();
            connection.export(Rio.createWriter(RDFFormat.TURTLE, sw), RDFHelper.getGraphsURIArray(output));
            assertEquals(swInput.toString(), sw.toString());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {

                }
            }
            // Release resources.
            environment.release();
        }
    }

    @Test(expected = DPUException.class)
    public void testSelectFailExec() throws Exception {
        // Prepare config.
        RdfValidatorConfig_V1 config = new RdfValidatorConfig_V1();
        config.setFailExecution(true);
        config.setOutputGraphSymbolicName("output1");
        config.setPerGraph(false);
        config.setQuery("SELECT  ?s ?p ?o  WHERE {?s ?p ?o }");

        // Prepare DPU.
        RdfValidator rdfDataValidator = new RdfValidator();
        rdfDataValidator.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());
        // Prepare test environment.
        TestEnvironment environment = new TestEnvironment();

        // Prepare data unit.
        WritableRDFDataUnit input = environment.createRdfInput("rdfInput", false);
        WritableRDFDataUnit output = environment.createRdfOutput("rdfCopyOfInput", false);

        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("metadata.ttl");

        RepositoryConnection connection = null;
        try {
            connection = input.getConnection();
            URI graph = input.addNewDataGraph("test");
            connection.add(inputStream, "", RDFFormat.TURTLE, graph);
            ByteArrayOutputStream inputBos = new ByteArrayOutputStream();
            connection.export(new TurtleWriter(inputBos), graph);
            // some triples has been loaded
            assertTrue(connection.size(graph) > 0);
            // Run.
            environment.run(rdfDataValidator);

        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {

                }
            }
            // Release resources.
            environment.release();
        }
    }

    @Test(expected = DPUException.class)
    public void testSelectPerGraphFailExec() throws Exception {
        // Prepare config.
        RdfValidatorConfig_V1 config = new RdfValidatorConfig_V1();
        config.setFailExecution(true);
        config.setOutputGraphSymbolicName("output1");
        config.setPerGraph(true);
        config.setQuery("SELECT  ?s ?p ?o  WHERE {?s ?p ?o }");

        // Prepare DPU.
        RdfValidator rdfDataValidator = new RdfValidator();
        rdfDataValidator.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());

        // Prepare test environment.
        TestEnvironment environment = new TestEnvironment();

        // Prepare data unit.
        WritableRDFDataUnit input = environment.createRdfInput("rdfInput", false);
        WritableRDFDataUnit output = environment.createRdfOutput("rdfCopyOfInput", false);

        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("metadata.ttl");

        RepositoryConnection connection = null;
        try {
            connection = input.getConnection();
            input.addNewDataGraph("testEmpty");
            URI graph = input.addNewDataGraph("test");
            connection.add(inputStream, "", RDFFormat.TURTLE, graph);
            ByteArrayOutputStream inputBos = new ByteArrayOutputStream();
            connection.export(new TurtleWriter(inputBos), graph);
            // some triples has been loaded
            assertTrue(connection.size(graph) > 0);
            // Run.
            environment.run(rdfDataValidator);

        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {

                }
            }
            // Release resources.
            environment.release();
        }
    }

    @Test
    public void testSelectPerGraph() throws Exception {
        // Prepare config.
        RdfValidatorConfig_V1 config = new RdfValidatorConfig_V1();
        config.setFailExecution(false);
        config.setOutputGraphSymbolicName("output1");
        config.setPerGraph(true);
        config.setQuery("SELECT  ?s ?p ?o  WHERE {?s ?p ?o }");

        // Prepare DPU.
        RdfValidator rdfDataValidator = new RdfValidator();
        rdfDataValidator.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());

        // Prepare test environment.
        TestEnvironment environment = new TestEnvironment();

        // Prepare data unit.
        WritableRDFDataUnit input = environment.createRdfInput("rdfInput", false);
        WritableRDFDataUnit output = environment.createRdfOutput("rdfCopyOfInput", false);

        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("metadata.ttl");

        RepositoryConnection connection = null;
        try {
            connection = input.getConnection();
            input.addNewDataGraph("testEmpty");
            URI graph = input.addNewDataGraph("test");
            connection.add(inputStream, "", RDFFormat.TURTLE, graph);
            ByteArrayOutputStream inputBos = new ByteArrayOutputStream();
            connection.export(new TurtleWriter(inputBos), graph);
            // some triples has been loaded
            assertTrue(connection.size(graph) > 0);
            // Run.
            environment.run(rdfDataValidator);

        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {

                }
            }
            // Release resources.
            environment.release();
        }
    }

    @Test
    public void testEmptyQueryFailGraph() throws Exception {
        // Prepare config.
        RdfValidatorConfig_V1 config = new RdfValidatorConfig_V1();
        config.setFailExecution(true);
        config.setOutputGraphSymbolicName("output1");
        config.setPerGraph(true);
        config.setQuery("SELECT  ?s ?p ?o  WHERE {?s ?s ?s }");

        // Prepare DPU.
        RdfValidator rdfDataValidator = new RdfValidator();
        rdfDataValidator.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());

        // Prepare test environment.
        TestEnvironment environment = new TestEnvironment();

        // Prepare data unit.
        WritableRDFDataUnit input = environment.createRdfInput("rdfInput", false);
        WritableRDFDataUnit output = environment.createRdfOutput("rdfCopyOfInput", false);

        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("metadata.ttl");

        RepositoryConnection connection = null;
        try {
            connection = input.getConnection();
            input.addNewDataGraph("testEmpty");
            URI graph = input.addNewDataGraph("test");
            connection.add(inputStream, "", RDFFormat.TURTLE, graph);
            ByteArrayOutputStream inputBos = new ByteArrayOutputStream();
            connection.export(new TurtleWriter(inputBos), graph);
            // some triples has been loaded
            assertTrue(connection.size(graph) > 0);
            // Run.
            environment.run(rdfDataValidator);

        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {

                }
            }
            // Release resources.
            environment.release();
        }
    }

    @Test(expected = DPUException.class)
    public void testInvalidConfig() throws Exception {
        // Prepare config.
        RdfValidatorConfig_V1 config = new RdfValidatorConfig_V1();
        config.setFailExecution(false);
        config.setOutputGraphSymbolicName("");
        config.setPerGraph(false);
        config.setQuery("");

        // Prepare DPU.
        RdfValidator rdfDataValidator = new RdfValidator();
        rdfDataValidator.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());

        // Prepare test environment.
        TestEnvironment environment = new TestEnvironment();

        // Prepare data unit.
        WritableRDFDataUnit input = environment.createRdfInput("rdfInput", false);
        WritableRDFDataUnit output = environment.createRdfOutput("rdfCopyOfInput", false);
        
        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("metadata.ttl");

        RepositoryConnection connection = null;
        try {
            connection = input.getConnection();
            URI graph = input.addNewDataGraph("test");
            connection.add(inputStream, "", RDFFormat.TURTLE, graph);
            ByteArrayOutputStream inputBos = new ByteArrayOutputStream();
            connection.export(new TurtleWriter(inputBos), graph);
            // some triples has been loaded
            assertTrue(connection.size(graph) > 0);
            // Run.
            environment.run(rdfDataValidator);

        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {

                }
            }
            // Release resources.
            environment.release();
        }
    }

    @Test(expected = DPUException.class)
    public void testInvalidConfig1() throws Exception {
        // Prepare config.
        RdfValidatorConfig_V1 config = new RdfValidatorConfig_V1();
        config.setFailExecution(false);
        config.setOutputGraphSymbolicName("");
        config.setPerGraph(false);
        config.setQuery("SELECT ");

        // Prepare DPU.
        RdfValidator rdfDataValidator = new RdfValidator();
        rdfDataValidator.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());

        // Prepare test environment.
        TestEnvironment environment = new TestEnvironment();

        // Prepare data unit.
        WritableRDFDataUnit input = environment.createRdfInput("rdfInput", false);
        WritableRDFDataUnit output = environment.createRdfOutput("rdfCopyOfInput", false);

        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("metadata.ttl");

        RepositoryConnection connection = null;
        try {
            connection = input.getConnection();
            URI graph = input.addNewDataGraph("test");
            connection.add(inputStream, "", RDFFormat.TURTLE, graph);
            ByteArrayOutputStream inputBos = new ByteArrayOutputStream();
            connection.export(new TurtleWriter(inputBos), graph);
            // some triples has been loaded
            assertTrue(connection.size(graph) > 0);
            // Run.
            environment.run(rdfDataValidator);

        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {

                }
            }
            // Release resources.
            environment.release();
        }
    }

    @Test(expected = DPUException.class)
    public void testInvalidConfig2() throws Exception {
        // Prepare config.
        RdfValidatorConfig_V1 config = new RdfValidatorConfig_V1();
        config.setFailExecution(false);
        config.setOutputGraphSymbolicName(null);
        config.setPerGraph(false);
        config.setQuery("SELECT ");

        // Prepare DPU.
        RdfValidator rdfDataValidator = new RdfValidator();
        rdfDataValidator.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());

        // Prepare test environment.
        TestEnvironment environment = new TestEnvironment();

        // Prepare data unit.
        WritableRDFDataUnit input = environment.createRdfInput("rdfInput", false);
        WritableRDFDataUnit output = environment.createRdfOutput("rdfCopyOfInput", false);

        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("metadata.ttl");

        RepositoryConnection connection = null;
        try {
            connection = input.getConnection();
            URI graph = input.addNewDataGraph("test");
            connection.add(inputStream, "", RDFFormat.TURTLE, graph);
            ByteArrayOutputStream inputBos = new ByteArrayOutputStream();
            connection.export(new TurtleWriter(inputBos), graph);
            // some triples has been loaded
            assertTrue(connection.size(graph) > 0);
            // Run.
            environment.run(rdfDataValidator);

        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {

                }
            }
            // Release resources.
            environment.release();
        }
    }

    @Test(expected = DPUException.class)
    public void testInvalidConfig3() throws Exception {
        // Prepare config.
        RdfValidatorConfig_V1 config = new RdfValidatorConfig_V1();
        config.setFailExecution(false);
        config.setOutputGraphSymbolicName(null);
        config.setPerGraph(true);
        config.setQuery("ASK");

        // Prepare DPU.
        RdfValidator rdfDataValidator = new RdfValidator();
        rdfDataValidator.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());

        // Prepare test environment.
        TestEnvironment environment = new TestEnvironment();

        // Prepare data unit.
        WritableRDFDataUnit input = environment.createRdfInput("rdfInput", false);
        WritableRDFDataUnit output = environment.createRdfOutput("rdfCopyOfInput", false);

        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("metadata.ttl");

        RepositoryConnection connection = null;
        try {
            connection = input.getConnection();
            URI graph = input.addNewDataGraph("test");
            connection.add(inputStream, "", RDFFormat.TURTLE, graph);
            ByteArrayOutputStream inputBos = new ByteArrayOutputStream();
            connection.export(new TurtleWriter(inputBos), graph);
            // some triples has been loaded
            assertTrue(connection.size(graph) > 0);
            // Run.
            environment.run(rdfDataValidator);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {

                }
            }
            // Release resources.
            environment.release();
        }
    }
}
