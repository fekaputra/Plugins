package eu.unifiedviews.plugins.transformer.xslt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.files.FilesDataUnitUtils;
import eu.unifiedviews.helpers.dataunit.files.FilesHelper;
import eu.unifiedviews.helpers.dataunit.rdf.RdfDataUnitUtils;
import eu.unifiedviews.helpers.dataunit.virtualpath.VirtualPathHelper;
import eu.unifiedviews.helpers.dataunit.virtualpath.VirtualPathHelpers;
import eu.unifiedviews.helpers.dpu.test.config.ConfigurationBuilder;
import eu.unifiedviews.helpers.dpu.test.rdf.InputOutputUtils;

public class XsltTest {

    private static final Logger LOG = LoggerFactory.getLogger(XsltTest.class);

    private Xslt dpu;

    private TestEnvironment env;

    private WritableFilesDataUnit input;

    private WritableFilesDataUnit output;

    @Before
    public void before() throws Exception {
        // prepare DPU
        dpu = new Xslt();

        env = new TestEnvironment();
        input = env.createFilesInput("filesInput");
        output = env.createFilesOutput("filesOutput");
    }

    @After
    public void after() throws Exception {
        env.release();
    }

    @Test
    public void passForValidXmlAndTemplate() throws Exception {
        addFileToInput("valid-for-xslt.xml");

        XSLTConfig_V1 config = new XSLTConfig_V1();
        config.setXslTemplate(resourceAsString("produce-list.xslt"));
        dpu.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());

        env.run(dpu);

        assertNonEmptyOutput();
    }

    @Test
    public void throwExceptionWhenInvalidTemplateSpecified() throws Exception {
        addFileToInput("valid.xml");

        XSLTConfig_V1 config = new XSLTConfig_V1();
        config.setXslTemplate(resourceAsString("invalid.xslt"));
        dpu.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());

        try {
            env.run(dpu);
            fail("Should throw DPUException: template is invalid");
        } catch (DPUException e) {
            // good job!
        }
    }

    @Test
    public void throwExceptionWhenNoTemplateIsSpecified() throws Exception {
        addFileToInput("valid.xml");

        try {
            env.run(dpu);
            fail("Should throw DPUException: no template specified");
        } catch (DPUException e) {
            // good job!
        }
    }

    @Test
    public void throwExceptionWhenNonValidXmlIsSpecified() throws Exception {
        addFileToInput("not-well-formed.xml");

        XSLTConfig_V1 config = new XSLTConfig_V1();
        config.setXslTemplate(resourceAsString("produce-list.xslt"));
        dpu.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());

        try {
            env.run(dpu);
            fail("Should throw DPUException: invalid XML on input");
        } catch (DPUException e) {
            // good job!
        }
    }

    @Test
    public void checkTemplateVersion2IsSupported() throws Exception {
        addFileToInput("document-for-template-version-2.xml");

        XSLTConfig_V1 config = new XSLTConfig_V1();
        config.setXslTemplate(resourceAsString("template-version-2.xslt"));
        dpu.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());

        env.run(dpu);

        assertNonEmptyOutput();
    }

    @Test
    public void checkCorrectOutputFileExtension() throws Exception {
        addFileToInput("valid-for-xslt.xml");

        XSLTConfig_V1 config = new XSLTConfig_V1();
        config.setXslTemplate(resourceAsString("produce-list.xslt"));
        config.setOutputFileExtension(".extension");
        dpu.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());

        env.run(dpu);

        VirtualPathHelper helper = VirtualPathHelpers.create(output);
        List<FilesDataUnit.Entry> outputList = new ArrayList<>(FilesHelper.getFiles(output));
        Iterator<FilesDataUnit.Entry> outputIterator = outputList.iterator();

        while (outputIterator.hasNext()) {
            String symbolicName = outputIterator.next().getSymbolicName();
            assertEquals("symbolicName-test.extension", helper.getVirtualPath(symbolicName));
        }
    }

    @Test
    public void checkParamsFromMapAreInOutput() throws Exception {
        addFileToInput("valid.xml");

        // Prepare configuration.
        final WritableRDFDataUnit configRdf = env.createRdfInput("config", false);
        InputOutputUtils.extractFromFile(new File(getClass().getClassLoader().getResource("config.ttl").toURI()), RDFFormat.TURTLE, configRdf,
                RdfDataUnitUtils.addGraph(configRdf, "main-config"));

        // configure DPU
        XSLTConfig_V1 config = new XSLTConfig_V1();
        config.setXslTemplate(resourceAsString("output-params.xslt"));
        dpu.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());

        // run DPU
        env.run(dpu);

        // check output for parameters
        List<FilesDataUnit.Entry> outputList = new ArrayList<>(FilesHelper.getFiles(output));
        Iterator<FilesDataUnit.Entry> outputIterator = outputList.iterator();

        while (outputIterator.hasNext()) {
            String outputContents = IOUtils.toString(java.net.URI.create(outputIterator.next().getFileURIString()), "UTF-8");

            // verify result
            assertTrue("Output contents should contain string '1'", outputContents.contains("1"));
            assertTrue("Output contents should contain string '2'", outputContents.contains("2"));
        }
    }

    private void assertNonEmptyOutput() throws Exception {
        List<FilesDataUnit.Entry> outputList = new ArrayList<>(FilesHelper.getFiles(output));

        // iterate over files
        Iterator<FilesDataUnit.Entry> outputIterator = outputList.iterator();

        while (outputIterator.hasNext()) {
            String outputContents = IOUtils.toString(java.net.URI.create(outputIterator.next().getFileURIString()), "UTF-8");

            // verify result
            assertTrue("Transformation should generate output", !outputContents.isEmpty());
        }
    }

    private void addFileToInput(final String filename) throws Exception {
        final File file = new File(getClass().getClassLoader().getResource(filename).toURI());
        FilesDataUnitUtils.addFile(input, file, "symbolicName-test");
    }

    private String resourceAsString(final String filename) {
        String result = "";

        try {
            result = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(filename), "UTF-8");
        } catch (IOException ex) {
            LOG.error("Can't read resource.", ex);
        }

        return result;
    }

}
