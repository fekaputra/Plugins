package eu.unifiedviews.plugins.extractor.httprequest;

import org.apache.http.HttpHost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;

/**
 * Created by tomasknap on 15/05/2017.
 */
public class HttpStateWrapper {

    public CloseableHttpClient getClient() {
        return client;
    }

    public HttpClientContext getContext() {
        return context;
    }

    public HttpHost getHost() {

        return host;
    }

    private HttpHost host;
    private CloseableHttpClient client;
    private HttpClientContext context;

    public HttpStateWrapper(HttpHost host, CloseableHttpClient client, HttpClientContext context) {
        this.host = host;
        this.client = client;
        this.context = context;
    }

}
