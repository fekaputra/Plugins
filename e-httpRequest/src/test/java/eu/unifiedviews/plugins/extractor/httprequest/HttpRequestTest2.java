package eu.unifiedviews.plugins.extractor.httprequest;

import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.helpers.dpu.test.config.ConfigurationBuilder;
import eu.unifiedviews.plugins.extractor.httprequest.HttpRequestConfig_V1.DataType;
import eu.unifiedviews.plugins.extractor.httprequest.HttpRequestConfig_V1.RequestType;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;

public class HttpRequestTest2 {

    private static final Logger LOG = LoggerFactory.getLogger(HttpRequestTest2.class);

    private TestEnvironment env;

    private HttpRequest dpu;

    HttpRequestConfig_V1 config;

    WritableFilesDataUnit input;
    WritableFilesDataUnit output;

    private static final String GET_RESPONSE_FILE = "get_response.json";

    private static final String POST_RAW_RESPONSE_FILE = "post_raw_response.xml";

    private static final String POST_MULTIPART_RESPONSE_FILE = "post_multipart_response.json";

    @Before
    public void before() throws Exception {
        this.dpu = new HttpRequest();
        this.env = new TestEnvironment();
        this.input = this.env.createFilesInput("requestFilesConfig");
        this.output = this.env.createFilesOutput("requestOutput");

        config = new HttpRequestConfig_V1();

        config.setRequestType(RequestType.POST);
        config.setPostRequestDataType(DataType.FILE);
        config.setRequestURL("https://unifiedviews-dev.semantic-web.at/GraphSearch/api/content/create");
        config.setUseAuthentication(true);
        config.setUserName("superadmin");
        config.setPassword("");
        config.setContentType(RequestContentType.JSON);
        config.setCharset("UTF-8");

        URI sentResponse = this.getClass().getClassLoader().getResource("test.json").toURI();
        //String sentResponseContent = readFile(new File(sentResponse));

        this.input.addExistingFile("test", sentResponse.toString());


    }

    @After
    public void after() throws Exception {
        this.env.release();
    }

    //to try HTTPS - in particular to execute content API call to PPGS
    //TODO finalize and make it re-runnable automatically
    //Note: password is missing
    //@Test
    public void filePOSTRequestTest() throws Exception {

        this.dpu.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());
        this.env.run(this.dpu);

//        FilesDataUnit.Entry entry = this.output.getIteration().next();
//        File receivedResponse = new File(URI.create(entry.getFileURIString()));
//        String receivedResponseContent = readFile(receivedResponse);
//
//        assertEquals(sentResponseContent, receivedResponseContent);
    }



    private String readFile(File input) {
        try (FileInputStream inputStream = new FileInputStream(input)) {
            return IOUtils.toString(inputStream);
        } catch (IOException ex) {
            LOG.error("", ex);
        }
        return null;
    }

}
