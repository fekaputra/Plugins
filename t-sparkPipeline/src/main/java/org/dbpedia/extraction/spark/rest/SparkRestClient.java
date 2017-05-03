package org.dbpedia.extraction.spark.rest;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.dbpedia.extraction.spark.dialog.SparkDpuConfig;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

    private CloseableHttpResponse post(URI url, JSONObject data) throws IOException {
        HttpPost post = new HttpPost(url);
        List<NameValuePair> postParams = new ArrayList<>();

        // add header
        post.setHeader("Content-Type", "application/json;charset=UTF-8");

        if(data != null)
            post.setEntity(new StringEntity(data.toString()));

        return cClient.execute(post);
    }

    private JSONTokener readResponse(CloseableHttpResponse response) throws IOException {
        int status = response.getStatusLine().getStatusCode();
        if(status < 205) {
            BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            //response.close();
            return new JSONTokener(br);
        }
        else
            throw new HttpResponseException(status, "HttpPost failed with status: " + status);
    }

    public SparkRestResponse submitJob(String appName) throws Exception {
        this.config.setAppName(appName);
        SparkSubmitRequest requestData = new SparkSubmitRequest(this.config);
        CloseableHttpResponse response = post(requestData.getSubmitUri(), requestData);
        SparkRestResponse ret = new SparkRestResponse(readResponse(response));
        response.close();
        return ret;
    }

    public SparkRestResponse getStatus(String submissionId) throws Exception {
        URI uri = new URIBuilder(this.config.getRestApiUri()).setPath("/v1/submissions/status/" + submissionId).build();
        HttpGet get = new HttpGet(uri);
        CloseableHttpResponse response = this.cClient.execute(get);
        SparkRestResponse ret = new SparkRestResponse(readResponse(response));
        response.close();
        return ret;
    }

    public SparkRestResponse killJob(String submissionId) throws Exception {
        URI uri = new URIBuilder(this.config.getRestApiUri()).setPath("/v1/submissions/kill/" + submissionId).build();
        CloseableHttpResponse response = post(uri, null);
        SparkRestResponse ret = new SparkRestResponse(readResponse(response));
        response.close();
        return ret;
    }
}