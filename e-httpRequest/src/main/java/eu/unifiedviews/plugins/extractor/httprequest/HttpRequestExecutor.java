package eu.unifiedviews.plugins.extractor.httprequest;

import eu.unifiedviews.dpu.DPUException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
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
//            if (config.isUseAuthentication()) {
//                addBasiAuthenticationForHttpRequest(request, config.getUserName(), config.getPassword());
//            }
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
//            if (config.isUseAuthentication()) {
//                addBasiAuthenticationForHttpRequest(request, config.getUserName(), config.getPassword());
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
//            if (config.isUseAuthentication()) {
//                addBasiAuthenticationForHttpRequest(request, config.getUserName(), config.getPassword());
//            }

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            for (String key : config.getFormDataRequestBody().keySet()) {
                builder.addTextBody(key, config.getFormDataRequestBody().get(key));
            }
            HttpEntity entity = builder.build();
            request.setEntity(entity);

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
//            if (config.isUseAuthentication()) {
//                addBasiAuthenticationForHttpRequest(request, config.getUserName(), config.getPassword());
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

    private static void checkHttpResponseStatus(CloseableHttpResponse response) throws Exception {
        LOG.info("HTTP Response code {}", response.getStatusLine().getStatusCode());
        if (response.getStatusLine().getStatusCode() != 200) {
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

        //parse URI from config
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
                LOG.warn("Port is not automatically derived for schema: {}. Port is automcatically derived only for http/https. Port may not be properly set.", schemaString);
            }
        }

        LOG.info("Host, port, schema: {}, {}, {}", hostString, port, schemaString);
        HttpHost host = new HttpHost(hostString, port,schemaString);

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(config.getUserName(), config.getPassword()));

        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build();

        // Create AuthCache instance
        AuthCache authCache = new BasicAuthCache();
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(host, basicAuth);
        HttpClientContext localContext = HttpClientContext.create();
        localContext.setAuthCache(authCache);
        localContext.setCredentialsProvider(credsProvider);

        return new HttpStateWrapper(host, httpclient, localContext);
    }

}
