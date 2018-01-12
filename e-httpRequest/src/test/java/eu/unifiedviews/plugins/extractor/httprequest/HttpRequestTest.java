package eu.unifiedviews.plugins.extractor.httprequest;

import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.test.config.ConfigurationBuilder;
import eu.unifiedviews.plugins.extractor.httprequest.HttpRequestConfig_V1.DataType;
import eu.unifiedviews.plugins.extractor.httprequest.HttpRequestConfig_V1.RequestType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class HttpRequestTest {

    private static final Logger LOG = LoggerFactory.getLogger(HttpRequestTest.class);

    private TestEnvironment env;

    private HttpRequest dpu;

    private WritableFilesDataUnit output;

    private static final String GET_RESPONSE_FILE = "get_response.json";

    private static final String DELETE_RESPONSE_FILE = "delete_response.json";

    private static final String POST_RAW_RESPONSE_FILE = "post_raw_response.xml";

    private static final String POST_MULTIPART_RESPONSE_FILE = "post_multipart_response.json";

    private static HttpRequestExecutor mockHttpExecutorWithErrorResponses() throws Exception {
        HttpRequestExecutor executor = Mockito.mock(HttpRequestExecutor.class);
        Mockito.when(executor.sendGetRequest(Matchers.any(HttpRequestConfig_V1.class), Matchers.any(CloseableHttpClient.class))).thenThrow(new Exception("HTTP error code 404 NOT FOUND"));
        Mockito.when(executor.sendMultipartPostRequest(Matchers.any(HttpRequestConfig_V1.class), Matchers.any(CloseableHttpClient.class))).thenThrow(new Exception("HTTP error code 404 NOT FOUND"));
        Mockito.when(executor.sendRawDataPostRequest(Matchers.any(HttpRequestConfig_V1.class), Matchers.any(CloseableHttpClient.class))).thenThrow(new Exception("HTTP error code 404 NOT FOUND"));
        Mockito.when(executor.sendFilePostRequest(Matchers.any(HttpRequestConfig_V1.class), Matchers.any(File.class), Matchers.any(CloseableHttpClient.class))).thenThrow(new Exception("HTTP error code 404 NOT FOUND"));

        return executor;
    }

    private HttpRequestExecutor mockHttpExecutorWithResponses(HttpRequestConfig_V1 config) throws Exception {
        HttpRequestExecutor executor = Mockito.mock(HttpRequestExecutor.class);

        StatusLine statusLine = Mockito.mock(StatusLine.class);
        Mockito.when(statusLine.getStatusCode()).thenReturn(200);
        Mockito.when(statusLine.toString()).thenReturn("200 OK");

        CloseableHttpResponse response = Mockito.mock(CloseableHttpResponse.class);
        HttpEntity httpEntity = Mockito.mock(HttpEntity.class);
        Mockito.when(response.getStatusLine()).thenReturn(statusLine);

        URI responseResource = null;
        if (config.getRequestType() == RequestType.GET) {
            responseResource = this.getClass().getClassLoader().getResource(GET_RESPONSE_FILE).toURI();
        } else if (config.getRequestType() == RequestType.DELETE) {
            responseResource = this.getClass().getClassLoader().getResource(DELETE_RESPONSE_FILE).toURI();
        } else {
            if (config.getPostRequestDataType() == DataType.FORM_DATA) {
                responseResource = this.getClass().getClassLoader().getResource(POST_MULTIPART_RESPONSE_FILE).toURI();
            } else if (config.getPostRequestDataType() == DataType.RAW_DATA) {
                responseResource = this.getClass().getClassLoader().getResource(POST_RAW_RESPONSE_FILE).toURI();
            }
        }
        File responseFile = new File(responseResource);
        Mockito.when(httpEntity.getContent()).thenReturn(FileUtils.openInputStream(responseFile));
        Mockito.when(response.getEntity()).thenReturn(httpEntity);

        Mockito.when(executor.sendGetRequest(Matchers.any(HttpRequestConfig_V1.class), Matchers.any(CloseableHttpClient.class))).thenReturn(response);
        Mockito.when(executor.sendDeleteRequest(Matchers.any(HttpRequestConfig_V1.class), Matchers.any(CloseableHttpClient.class))).thenReturn(response);
        Mockito.when(executor.sendMultipartPostRequest(Matchers.any(HttpRequestConfig_V1.class), Matchers.any(CloseableHttpClient.class))).thenReturn(response);
        Mockito.when(executor.sendRawDataPostRequest(Matchers.any(HttpRequestConfig_V1.class), Matchers.any(CloseableHttpClient.class))).thenReturn(response);
        Mockito.when(executor.sendFilePostRequest(Matchers.any(HttpRequestConfig_V1.class), Matchers.any(File.class), Matchers.any(CloseableHttpClient.class))).thenReturn(response);
        Mockito.when(executor.sendMultipartPutRequest(Matchers.any(HttpRequestConfig_V1.class), Matchers.any(CloseableHttpClient.class))).thenReturn(response);
        Mockito.when(executor.sendRawDataPutRequest(Matchers.any(HttpRequestConfig_V1.class), Matchers.any(CloseableHttpClient.class))).thenReturn(response);
        Mockito.when(executor.sendFilePutRequest(Matchers.any(HttpRequestConfig_V1.class), Matchers.any(File.class), Matchers.any(CloseableHttpClient.class))).thenReturn(response);

        return executor;
    }

    @Before
    public void before() throws Exception {
        this.env = new TestEnvironment();
        this.output = this.env.createFilesOutput("requestOutput");

        this.dpu = new HttpRequest();
        this.dpu.client = HttpClients.createDefault();
    }

    @After
    public void after() throws Exception {
        this.env.release();
    }

    @Test(expected = DPUException.class)
    public void GETRequestResponseErrorCodeTest() throws Exception {
        HttpRequestConfig_V1 config = createGetRequestConfig();
        this.dpu.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());
        HttpRequestExecutor executor = mockHttpExecutorWithErrorResponses();
        this.dpu.setRequestExecutor(executor);
        this.env.run(this.dpu);
    }

    @Test(expected = DPUException.class)
    public void rawPOSTRequestResponseErrorCodeTest() throws Exception {
        HttpRequestConfig_V1 config = createPostRawRequestConfig();
        this.dpu.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());
        HttpRequestExecutor executor = mockHttpExecutorWithErrorResponses();
        this.dpu.setRequestExecutor(executor);
        this.env.run(this.dpu);
    }

    @Test(expected = DPUException.class)
    public void rawPUTRequestResponseErrorCodeTest() throws Exception {
        HttpRequestConfig_V1 config = createPutRawRequestConfig();
        this.dpu.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());
        HttpRequestExecutor executor = mockHttpExecutorWithErrorResponses();
        this.dpu.setRequestExecutor(executor);
        this.env.run(this.dpu);
    }

    @Test(expected = DPUException.class)
    public void multipartPOSTResponseErrorCodeTest() throws Exception {
        HttpRequestConfig_V1 config = createPostMultipartRequestConfig();
        this.dpu.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());
        HttpRequestExecutor executor = mockHttpExecutorWithErrorResponses();
        this.dpu.setRequestExecutor(executor);
        this.env.run(this.dpu);
    }

    @Test(expected = DPUException.class)
    public void multipartPUTResponseErrorCodeTest() throws Exception {
        HttpRequestConfig_V1 config = createPutMultipartRequestConfig();
        this.dpu.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());
        HttpRequestExecutor executor = mockHttpExecutorWithErrorResponses();
        this.dpu.setRequestExecutor(executor);
        this.env.run(this.dpu);
    }

    @Test
    public void GETRequestTest() throws Exception {
        HttpRequestConfig_V1 config = createGetRequestConfig();

        URI sentResponse = this.getClass().getClassLoader().getResource(GET_RESPONSE_FILE).toURI();
        String sentResponseContent = readFile(new File(sentResponse));

        this.dpu.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());
        HttpRequestExecutor executor = mockHttpExecutorWithResponses(config);
        this.dpu.setRequestExecutor(executor);
        this.env.run(this.dpu);

        FilesDataUnit.Entry entry = this.output.getIteration().next();
        File receivedResponse = new File(URI.create(entry.getFileURIString()));
        String receivedResponseContent = readFile(receivedResponse);

        assertEquals(sentResponseContent, receivedResponseContent);
    }

    @Test
    public void DELETERequestTest() throws Exception {
        HttpRequestConfig_V1 config = createDeleteRequestConfig();

        URI sentResponse = this.getClass().getClassLoader().getResource(DELETE_RESPONSE_FILE).toURI();
        String sentResponseContent = readFile(new File(sentResponse));

        this.dpu.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());
        HttpRequestExecutor executor = mockHttpExecutorWithResponses(config);
        this.dpu.setRequestExecutor(executor);
        this.env.run(this.dpu);

        FilesDataUnit.Entry entry = this.output.getIteration().next();
        File receivedResponse = new File(URI.create(entry.getFileURIString()));
        String receivedResponseContent = readFile(receivedResponse);

        assertEquals(sentResponseContent, receivedResponseContent);
    }

    @Test
    public void rawPOSTRequestTest() throws Exception {
        HttpRequestConfig_V1 config = createPostRawRequestConfig();

        URI sentResponse = this.getClass().getClassLoader().getResource(POST_RAW_RESPONSE_FILE).toURI();
        String sentResponseContent = readFile(new File(sentResponse));

        this.dpu.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());
        HttpRequestExecutor executor = mockHttpExecutorWithResponses(config);
        this.dpu.setRequestExecutor(executor);
        this.env.run(this.dpu);

        FilesDataUnit.Entry entry = this.output.getIteration().next();
        File receivedResponse = new File(URI.create(entry.getFileURIString()));
        String receivedResponseContent = readFile(receivedResponse);

        assertEquals(sentResponseContent, receivedResponseContent);
    }

    @Test
    public void rawPUTRequestTest() throws Exception {
        HttpRequestConfig_V1 config = createPutRawRequestConfig();

        URI sentResponse = this.getClass().getClassLoader().getResource(POST_RAW_RESPONSE_FILE).toURI();
        String sentResponseContent = readFile(new File(sentResponse));

        this.dpu.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());
        HttpRequestExecutor executor = mockHttpExecutorWithResponses(config);
        this.dpu.setRequestExecutor(executor);
        this.env.run(this.dpu);

        FilesDataUnit.Entry entry = this.output.getIteration().next();
        File receivedResponse = new File(URI.create(entry.getFileURIString()));
        String receivedResponseContent = readFile(receivedResponse);

        assertEquals(sentResponseContent, receivedResponseContent);
    }

    @Test
    public void multipartPOSTRequestTest() throws Exception {
        HttpRequestConfig_V1 config = createPostMultipartRequestConfig();

        URI sentResponse = this.getClass().getClassLoader().getResource(POST_MULTIPART_RESPONSE_FILE).toURI();
        String sentResponseContent = readFile(new File(sentResponse));

        this.dpu.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());
        HttpRequestExecutor executor = mockHttpExecutorWithResponses(config);
        this.dpu.setRequestExecutor(executor);
        this.env.run(this.dpu);

        FilesDataUnit.Entry entry = this.output.getIteration().next();
        File receivedResponse = new File(URI.create(entry.getFileURIString()));
        String receivedResponseContent = readFile(receivedResponse);

        assertEquals(sentResponseContent, receivedResponseContent);
    }

    @Test
    public void multipartPUTRequestTest() throws Exception {
        HttpRequestConfig_V1 config = createPutMultipartRequestConfig();

        URI sentResponse = this.getClass().getClassLoader().getResource(POST_MULTIPART_RESPONSE_FILE).toURI();
        String sentResponseContent = readFile(new File(sentResponse));

        this.dpu.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());
        HttpRequestExecutor executor = mockHttpExecutorWithResponses(config);
        this.dpu.setRequestExecutor(executor);
        this.env.run(this.dpu);

        FilesDataUnit.Entry entry = this.output.getIteration().next();
        File receivedResponse = new File(URI.create(entry.getFileURIString()));
        String receivedResponseContent = readFile(receivedResponse);

        assertEquals(sentResponseContent, receivedResponseContent);
    }

    private static HttpRequestConfig_V1 createGetRequestConfig() {
        HttpRequestConfig_V1 config = new HttpRequestConfig_V1();
        config.setRequestType(RequestType.GET);
        config.setRequestURL("http://echo.jsontest.com/key/value/one/two");
        config.setFileName("get_response.json");

        return config;
    }

    private static HttpRequestConfig_V1 createDeleteRequestConfig() {
        HttpRequestConfig_V1 config = new HttpRequestConfig_V1();
        config.setRequestType(RequestType.DELETE);
        config.setRequestURL("http://httpbin.org/delete");
        config.setFileName("delete_response.json");

        return config;
    }

    private static HttpRequestConfig_V1 createPostRawRequestConfig() {
        HttpRequestConfig_V1 config = new HttpRequestConfig_V1();
        config.setRequestType(RequestType.POST);
        config.setRequestURL("http://www.webservicex.net/globalweather.asmx");
        config.setContentType(RequestContentType.SOAP);
        config.setPostRequestDataType(DataType.RAW_DATA);
        config.setRawRequestBody("SOAP ENVELOPE TEXT");
        config.setFileName("raw_response.xml");

        return config;
    }

    private static HttpRequestConfig_V1 createPutRawRequestConfig() {
        HttpRequestConfig_V1 config = new HttpRequestConfig_V1();
        config.setRequestType(RequestType.PUT);
        config.setRequestURL("http://www.webservicex.net/globalweather.asmx");
        config.setContentType(RequestContentType.SOAP);
        config.setPostRequestDataType(DataType.RAW_DATA);
        config.setRawRequestBody("SOAP ENVELOPE TEXT");
        config.setFileName("raw_response.xml");

        return config;
    }

    private static HttpRequestConfig_V1 createPostMultipartRequestConfig() {
        HttpRequestConfig_V1 config = new HttpRequestConfig_V1();
        config.setRequestType(RequestType.POST);
        config.setRequestURL("https://data.gov.sk/api/action/internal_api");
        config.setPostRequestDataType(DataType.FORM_DATA);
        config.setFileName("multipart_response.xml");

        Map<String, String> formData = new HashMap<String, String>();
        formData.put("action", "resource_show");
        formData.put("token", "ee^sd5&78");
        formData.put("user_id", "8EEBA23B-6BFE-4F76-A3B5-D9B627823137");
        formData.put("data", "{\"id\":\"0bb71eaf-775f-4dac-bcf6-cfa457f816fa\"}");

        config.setFormDataRequestBody(formData);

        return config;
    }

    private static HttpRequestConfig_V1 createPutMultipartRequestConfig() {
        HttpRequestConfig_V1 config = new HttpRequestConfig_V1();
        config.setRequestType(RequestType.PUT);
        config.setRequestURL("https://data.gov.sk/api/action/internal_api");
        config.setPostRequestDataType(DataType.FORM_DATA);
        config.setFileName("multipart_response.xml");

        Map<String, String> formData = new HashMap<String, String>();
        formData.put("action", "resource_show");
        formData.put("token", "ee^sd5&78");
        formData.put("user_id", "8EEBA23B-6BFE-4F76-A3B5-D9B627823137");
        formData.put("data", "{\"id\":\"0bb71eaf-775f-4dac-bcf6-cfa457f816fa\"}");

        config.setFormDataRequestBody(formData);

        return config;
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
