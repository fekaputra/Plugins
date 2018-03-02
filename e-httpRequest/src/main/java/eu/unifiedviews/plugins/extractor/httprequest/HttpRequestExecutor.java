package eu.unifiedviews.plugins.extractor.httprequest;

import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.plugins.extractor.httprequest.rdfConfig.FormParam;
import eu.unifiedviews.plugins.extractor.httprequest.rdfConfig.FormParamBody;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

public class HttpRequestExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(HttpRequestExecutor.class);

    private HttpStateWrapper httpWrapper;

    /**
     * Executes GET HTTP request based on configuration
     * 
     * @param config
     *            DPU configuration
     * @param client
     *            HTTP client used to execute request
     * @return HTTP response
     * @throws Exception
     *             if request execution fails
     */
    public CloseableHttpResponse sendGetRequest(HttpRequestConfig_V1 config, CloseableHttpClient client) throws Exception {
        CloseableHttpResponse response = null;
        try {
            URIBuilder uriBuilder = new URIBuilder(config.getRequestURL());
            uriBuilder.setPath(uriBuilder.getPath());
            HttpGet request = new HttpGet(uriBuilder.build().normalize());
//            if (rdfConfig.isUseAuthentication()) {
//                addBasiAuthenticationForHttpRequest(request, rdfConfig.getUserName(), rdfConfig.getPassword());
//            }

            LOG.info("Request: {}", request.toString());

            response = this.httpWrapper.getClient().execute(this.httpWrapper.getHost(), request, this.httpWrapper.getContext());
//            response = client.execute(request);
            checkHttpResponseStatus(response);
        } catch (URISyntaxException | IllegalStateException | IOException ex) {
            String errorMsg = String.format("Failed to execute HTTP GET request to URL %s", config.getRequestURL());
            LOG.error(errorMsg);
            throw new Exception(errorMsg, ex);
        }

        return response;
    }

    /**
     * Executes DELETE HTTP request based on configuration
     *
     * @param config
     *            DPU configuration
     * @param client
     *            HTTP client used to execute request
     * @return HTTP response
     * @throws Exception
     *             if request execution fails
     */
    public CloseableHttpResponse sendDeleteRequest(HttpRequestConfig_V1 config, CloseableHttpClient client) throws Exception {
        CloseableHttpResponse response = null;
        try {
            URIBuilder uriBuilder = new URIBuilder(config.getRequestURL());
            uriBuilder.setPath(uriBuilder.getPath());
            HttpDelete request = new HttpDelete(uriBuilder.build().normalize());
//            if (rdfConfig.isUseAuthentication()) {
//                addBasiAuthenticationForHttpRequest(request, rdfConfig.getUserName(), rdfConfig.getPassword());
//            }

            LOG.info("Request: {}", request.toString());

            response = this.httpWrapper.getClient().execute(this.httpWrapper.getHost(), request, this.httpWrapper.getContext());
//            response = client.execute(request);
            checkHttpResponseStatus(response);
        } catch (URISyntaxException | IllegalStateException | IOException ex) {
            String errorMsg = String.format("Failed to execute HTTP DELETE request to URL %s", config.getRequestURL());
            LOG.error(errorMsg);
            throw new Exception(errorMsg, ex);
        }

        return response;
    }

    /**
     * Executes FILE (binary) HTTP POST request based on configuration
     * 
     * @param config
     *            DPU configuration
     * @param client
     *            HTTP client used to execute request
     * @return HTTP response
     * @throws Exception
     *             if request execution fails
     */
    public CloseableHttpResponse sendFilePostRequest(HttpRequestConfig_V1 config, File file, CloseableHttpClient client) throws Exception {
        CloseableHttpResponse response = null;
        try {
            URIBuilder uriBuilder = new URIBuilder(config.getRequestURL());
            uriBuilder.setPath(uriBuilder.getPath());

            HttpPost request = new HttpPost(uriBuilder.build().normalize());
//            if (rdfConfig.isUseAuthentication()) {
//                addBasiAuthenticationForHttpRequest(request, rdfConfig.getUserName(), rdfConfig.getPassword());
//            }

            EntityBuilder builder = EntityBuilder.create();
            builder.setContentEncoding(config.getCharset());

            //ContentType contentType = ContentType.DEFAULT_BINARY;
            ContentType contentType = ContentType.create(config.getContentType().getDescription()).withCharset(config.getCharset());
            builder.setFile(file);
            builder.setContentType(contentType);

            HttpEntity entity = builder.build();
            request.setEntity(entity);
            request.addHeader("Content-Type", contentType.toString());

            LOG.info("Request: {}", request.toString());

            response = this.httpWrapper.getClient().execute(this.httpWrapper.getHost(), request, this.httpWrapper.getContext());
            //response = client.execute(request);
            checkHttpResponseStatus(response);

        } catch (URISyntaxException | IllegalStateException | IOException ex) {
            String errorMsg = String.format("Failed to execute HTTP file POST request to URL %s", config.getRequestURL());
            LOG.error(errorMsg, ex);
            throw new Exception(errorMsg, ex);
        }
        return response;

    }

    /**
     * Executes FILE (binary) HTTP PUT request based on configuration
     *
     * @param config
     *            DPU configuration
     * @param client
     *            HTTP client used to execute request
     * @return HTTP response
     * @throws Exception
     *             if request execution fails
     */
    public CloseableHttpResponse sendFilePutRequest(HttpRequestConfig_V1 config, File file, CloseableHttpClient client) throws Exception {
        CloseableHttpResponse response = null;
        try {
            URIBuilder uriBuilder = new URIBuilder(config.getRequestURL());
            uriBuilder.setPath(uriBuilder.getPath());

            HttpPut request = new HttpPut(uriBuilder.build().normalize());
//            if (rdfConfig.isUseAuthentication()) {
//                addBasiAuthenticationForHttpRequest(request, rdfConfig.getUserName(), rdfConfig.getPassword());
//            }

            EntityBuilder builder = EntityBuilder.create();
            builder.setContentEncoding(config.getCharset());

            //ContentType contentType = ContentType.DEFAULT_BINARY;
            ContentType contentType = ContentType.create(config.getContentType().getDescription()).withCharset(config.getCharset());
            builder.setFile(file);
            builder.setContentType(contentType);

            HttpEntity entity = builder.build();
            request.setEntity(entity);
            request.addHeader("Content-Type", contentType.toString());

            LOG.info("Request: {}", request.toString());

            response = this.httpWrapper.getClient().execute(this.httpWrapper.getHost(), request, this.httpWrapper.getContext());
            //response = client.execute(request);
            checkHttpResponseStatus(response);

        } catch (URISyntaxException | IllegalStateException | IOException ex) {
            String errorMsg = String.format("Failed to execute HTTP file PUT request to URL %s", config.getRequestURL());
            LOG.error(errorMsg, ex);
            throw new Exception(errorMsg, ex);
        }
        return response;

    }



    /**
     * Executes MULTIPART (form data) HTTP POST request based on configuration
     * 
     * @param config
     *            DPU configuration
     * @param client
     *            HTTP client used to execute request
     * @return HTTP response
     * @throws Exception
     *             if request execution fails
     */
    public CloseableHttpResponse sendMultipartPostRequest(HttpRequestConfig_V1 config, CloseableHttpClient client) throws Exception {
        CloseableHttpResponse response = null;
        try {
            URIBuilder uriBuilder = new URIBuilder(config.getRequestURL());
            uriBuilder.setPath(uriBuilder.getPath());

            HttpPost request = new HttpPost(uriBuilder.build().normalize());
//            if (rdfConfig.isUseAuthentication()) {
//                addBasiAuthenticationForHttpRequest(request, rdfConfig.getUserName(), rdfConfig.getPassword());
//            }

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            for (String key : config.getFormDataRequestBody().keySet()) {
                builder.addTextBody(key, config.getFormDataRequestBody().get(key));
            }
            ContentType contentType = ContentType.MULTIPART_FORM_DATA.withCharset(config.getCharset());
            builder.setContentType(contentType);
            HttpEntity entity = builder.build();
            request.setEntity(entity);

            LOG.info("Request: {}", request.toString());

            response = this.httpWrapper.getClient().execute(this.httpWrapper.getHost(), request, this.httpWrapper.getContext());
//            response = client.execute(request);
            checkHttpResponseStatus(response);

        } catch (URISyntaxException | IllegalStateException | IOException ex) {
            String errorMsg = String.format("Failed to execute HTTP multipart POST request to URL %s", config.getRequestURL());
            LOG.error(errorMsg);
            throw new Exception(errorMsg, ex);
        }
        return response;
    }

    /**
     * Executes MULTIPART (form data) HTTP PUT request based on configuration
     *
     * @param config
     *            DPU configuration
     * @param client
     *            HTTP client used to execute request
     * @return HTTP response
     * @throws Exception
     *             if request execution fails
     */
    public CloseableHttpResponse sendMultipartPutRequest(HttpRequestConfig_V1 config, CloseableHttpClient client) throws Exception {
        CloseableHttpResponse response = null;
        try {
            URIBuilder uriBuilder = new URIBuilder(config.getRequestURL());
            uriBuilder.setPath(uriBuilder.getPath());

            HttpPut request = new HttpPut(uriBuilder.build().normalize());
//            if (rdfConfig.isUseAuthentication()) {
//                addBasiAuthenticationForHttpRequest(request, rdfConfig.getUserName(), rdfConfig.getPassword());
//            }

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            for (String key : config.getFormDataRequestBody().keySet()) {
                builder.addTextBody(key, config.getFormDataRequestBody().get(key));
            }
            ContentType contentType = ContentType.MULTIPART_FORM_DATA.withCharset(config.getCharset());
            builder.setContentType(contentType);
            HttpEntity entity = builder.build();
            request.setEntity(entity);

            LOG.info("Request: {}", request.toString());

            response = this.httpWrapper.getClient().execute(this.httpWrapper.getHost(), request, this.httpWrapper.getContext());
//            response = client.execute(request);
            checkHttpResponseStatus(response);

        } catch (URISyntaxException | IllegalStateException | IOException ex) {
            String errorMsg = String.format("Failed to execute HTTP multipart PUT request to URL %s", config.getRequestURL());
            LOG.error(errorMsg);
            throw new Exception(errorMsg, ex);
        }
        return response;
    }


    /**
     * Executes FILE (binary) HTTP POST request based on configuration
     *
     * @param config
     *            DPU configuration
     * @param client
     *            HTTP client used to execute request
     * @return HTTP response
     * @throws Exception
     *             if request execution fails
     */
    public CloseableHttpResponse sendMultipartFormPostRequestFromRdf(HttpRequestConfig_V1 config, FormParamBody paramsBody, CloseableHttpClient client) throws Exception {

        CloseableHttpResponse response = null;
        try {
            URIBuilder uriBuilder = new URIBuilder(config.getRequestURL());
            uriBuilder.setPath(uriBuilder.getPath());

            HttpPost request = new HttpPost(uriBuilder.build().normalize());

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            for (FormParam param : paramsBody.getFormParams()) {
                builder.addTextBody(param.getParam(), param.getValue(), ContentType.TEXT_PLAIN.withCharset(config.getCharset()));
            }
            ContentType contentType = ContentType.MULTIPART_FORM_DATA.withCharset(config.getCharset());
            builder.setContentType(contentType);
            HttpEntity entity = builder.build();
            request.setEntity(entity);

            LOG.info("Request: {}", request.toString());

            response = this.httpWrapper.getClient().execute(this.httpWrapper.getHost(), request, this.httpWrapper.getContext());
            checkHttpResponseStatus(response);

        } catch (URISyntaxException | IllegalStateException | IOException ex) {
            String errorMsg = String.format("Failed to execute HTTP multipart POST request to URL %s", config.getRequestURL());
            LOG.error(errorMsg);
            throw new Exception(errorMsg, ex);
        }
        return response;

    }

    /**
     * Executes FILE (binary) HTTP PUT request based on configuration
     *
     * @param config
     *            DPU configuration
     * @param client
     *            HTTP client used to execute request
     * @return HTTP response
     * @throws Exception
     *             if request execution fails
     */
    public CloseableHttpResponse sendMultipartFormPutRequestFromRdf(HttpRequestConfig_V1 config, FormParamBody paramsBody, CloseableHttpClient client) throws Exception {

        CloseableHttpResponse response = null;
        try {
            URIBuilder uriBuilder = new URIBuilder(config.getRequestURL());
            uriBuilder.setPath(uriBuilder.getPath());

            HttpPut request = new HttpPut(uriBuilder.build().normalize());

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            for (FormParam param : paramsBody.getFormParams()) {
                builder.addTextBody(param.getParam(), param.getValue(), ContentType.TEXT_PLAIN.withCharset(config.getCharset()));
            }
            ContentType contentType = ContentType.MULTIPART_FORM_DATA.withCharset(config.getCharset());
            builder.setContentType(contentType);
            HttpEntity entity = builder.build();
            request.setEntity(entity);

            LOG.info("Request: {}", request.toString());

            response = this.httpWrapper.getClient().execute(this.httpWrapper.getHost(), request, this.httpWrapper.getContext());
            checkHttpResponseStatus(response);

        } catch (URISyntaxException | IllegalStateException | IOException ex) {
            String errorMsg = String.format("Failed to execute HTTP multipart PUT request to URL %s", config.getRequestURL());
            LOG.error(errorMsg);
            throw new Exception(errorMsg, ex);
        }
        return response;

    }

    /**
     * Executes RAW data (text data) HTTP POST request based on configuration
     * 
     * @param config
     *            DPU configuration
     * @param client
     *            HTTP client used to execute request
     * @return HTTP response
     * @throws Exception
     *             if request execution fails
     */
    public CloseableHttpResponse sendRawDataPostRequest(HttpRequestConfig_V1 config, CloseableHttpClient client) throws Exception {
        CloseableHttpResponse response = null;
        try {
            URIBuilder uriBuilder = new URIBuilder(config.getRequestURL());
            uriBuilder.setPath(uriBuilder.getPath());

            HttpPost request = new HttpPost(uriBuilder.build().normalize());
//            if (rdfConfig.isUseAuthentication()) {
//                addBasiAuthenticationForHttpRequest(request, rdfConfig.getUserName(), rdfConfig.getPassword());
//            }

            EntityBuilder builder = EntityBuilder.create();
            builder.setContentEncoding(config.getCharset());

            ContentType contentType = ContentType.create(config.getContentType().getDescription()).withCharset(config.getCharset());
            builder.setText(config.getRawRequestBody());
            builder.setContentType(contentType);

            HttpEntity entity = builder.build();
            request.setEntity(entity);
            request.addHeader("Content-Type", contentType.toString());

            response = this.httpWrapper.getClient().execute(this.httpWrapper.getHost(), request, this.httpWrapper.getContext());
//            response = client.execute(request);
            checkHttpResponseStatus(response);

        } catch (URISyntaxException | IllegalStateException | IOException ex) {
            String errorMsg = String.format("Failed to execute HTTP raw POST request to URL %s", config.getRequestURL());
            LOG.error(errorMsg);
            throw new Exception(errorMsg, ex);
        }
        return response;
    }

    /**
     * Executes RAW data (text data) HTTP PUT request based on configuration
     *
     * @param config
     *            DPU configuration
     * @param client
     *            HTTP client used to execute request
     * @return HTTP response
     * @throws Exception
     *             if request execution fails
     */
    public CloseableHttpResponse sendRawDataPutRequest(HttpRequestConfig_V1 config, CloseableHttpClient client) throws Exception {
        CloseableHttpResponse response = null;
        try {
            URIBuilder uriBuilder = new URIBuilder(config.getRequestURL());
            uriBuilder.setPath(uriBuilder.getPath());

            HttpPut request = new HttpPut(uriBuilder.build().normalize());
//            if (rdfConfig.isUseAuthentication()) {
//                addBasiAuthenticationForHttpRequest(request, rdfConfig.getUserName(), rdfConfig.getPassword());
//            }

            EntityBuilder builder = EntityBuilder.create();
            builder.setContentEncoding(config.getCharset());

            ContentType contentType = ContentType.create(config.getContentType().getDescription()).withCharset(config.getCharset());
            builder.setText(config.getRawRequestBody());
            builder.setContentType(contentType);

            HttpEntity entity = builder.build();
            request.setEntity(entity);
            request.addHeader("Content-Type", contentType.toString());

            response = this.httpWrapper.getClient().execute(this.httpWrapper.getHost(), request, this.httpWrapper.getContext());
//            response = client.execute(request);
            checkHttpResponseStatus(response);

        } catch (URISyntaxException | IllegalStateException | IOException ex) {
            String errorMsg = String.format("Failed to execute HTTP raw PUT request to URL %s", config.getRequestURL());
            LOG.error(errorMsg);
            throw new Exception(errorMsg, ex);
        }
        return response;
    }

    private static void checkHttpResponseStatus(CloseableHttpResponse response) throws Exception {
        int statusCode = response.getStatusLine().getStatusCode();
        LOG.info("HTTP Response code {}", statusCode);
        if (statusCode >= 300) {
            StringBuilder responseAsString = new StringBuilder();
            responseAsString.append(response.getStatusLine().toString()).append('\n');
            for (Header h : response.getAllHeaders()) {
                responseAsString.append(h.toString()).append('\n');
            }
            String errorMsg = String.format("HTTP request was not successful. Received HTTP status and headers:\n%s", responseAsString);
            LOG.error(errorMsg);
            try {
                LOG.error("Response content: {}", EntityUtils.toString(response.getEntity()));
            } catch (Exception err) {
                // ignore
            }
            throw new Exception(errorMsg);
        }
    }

//    private static void addBasiAuthenticationForHttpRequest(HttpRequestBase request, String user, String password) {
//        String basicAuth = "Basic " + encodeUserNamePassword(user, password);
//        request.addHeader("Authorization", basicAuth);
//    }

//    private static String encodeUserNamePassword(String userName, String password) {
//        String authString = userName + ":" + password;
//        return Base64.encodeBase64String(authString.getBytes());
//    }

    public void initialize(HttpRequestConfig_V1 config) throws DPUException {

        //prepare host, http client, context (to cache credentials)
        //furthermore such objects should be used when calling services
        this.httpWrapper = createHttpStateWithAuth(config);

    }

    /**
     * Create an HTTP state after authentication with credentials for future requests
     * @return a class wrapping HTTP host, client and context used for future requests
     */
    private HttpStateWrapper createHttpStateWithAuth(HttpRequestConfig_V1 config) throws DPUException {

        //check that the configuration contains URL of the service
        if (config.getRequestURL() == null || config.getRequestURL().isEmpty()) {
            throw new DPUException("Request URL is not defined");
        }

        URI uri = null;
        try {
            uri = new URI(config.getRequestURL());
        } catch (URISyntaxException e) {
            throw new DPUException(e);
        }
        String schemaString = uri.getScheme();
        String hostString = uri.getHost();
        int port =  uri.getPort();
        if (!(port > 0)) {
            //add port manually based on the protocol
            if (schemaString.equals("https")) {
                port = 443;
            } else if (schemaString.equals("http")) {
                port = 80;
            } else {
                LOG.warn("Port is not automatically derived for schema: {}. Port is automatically derived only for http/https. Port may not be properly set.", schemaString);
            }
        }

        LOG.info("Host, port, schema: {}, {}, {}", hostString, port, schemaString);
        HttpHost host = new HttpHost(hostString, port,schemaString);

        HttpClientContext localContext = HttpClientContext.create();
        CloseableHttpClient httpclient;

        if (config.isUseAuthentication()) {
            LOG.info("Caching credentials");
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(
                    AuthScope.ANY,
                    new UsernamePasswordCredentials(config.getUserName(), config.getPassword()));

            httpclient = HttpClients.custom()
                    .setDefaultCredentialsProvider(credsProvider)
                    .build();

            // Create AuthCache instance
            AuthCache authCache = new BasicAuthCache();
            BasicScheme basicAuth = new BasicScheme();
            authCache.put(host, basicAuth);
            localContext.setAuthCache(authCache);
            localContext.setCredentialsProvider(credsProvider);

        }
        else {
            LOG.info("No credentials provided");
            httpclient = HttpClients.custom().build();

        }

        return new HttpStateWrapper(host, httpclient, localContext);
    }

}
