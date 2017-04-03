package org.dbpedia.extraction.spark.rest;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.dbpedia.extraction.spark.utils.SparkDpuConfig;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chile on 02.04.17.
 */
public class SparkRestClient {

    private final CloseableHttpClient cClient;
    private final SparkDpuConfig config;

    public SparkRestClient(SparkDpuConfig config){
        this.cClient = HttpClientBuilder.create().build();
        this.config = config;
    }

    public void post(URI url) throws IOException {
        HttpPost post = new HttpPost(url);
        List<NameValuePair> postParams = new ArrayList<>();

        // add header
        post.setHeader("Content-Type", "application/json;charset=UTF-8");

        JSONObject jo = new JSONObject();
        post.setEntity(new StringEntity(jo.toString()));

        HttpResponse response = cClient.execute(post);

        int responseCode = response.getStatusLine().getStatusCode();
    }

    private JSONObject createDataObject(){
        JSONObject obj = new JSONObject();



        return obj;
    }
}
