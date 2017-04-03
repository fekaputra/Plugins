package org.dbpedia.extraction.spark.rest;

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

/**
 * Created by chile on 03.04.17.
 */
public class SparkResponse extends JSONObject {

    private String action;
    private String message;
    private String serverSparkVersion;
    private String submissionId;
    private boolean success;
    private String driverState;
    private String workerHostPort;
    private String workerId;

    public SparkResponse(HttpResponse httpResponse) throws IOException, JSONException {
        super(new JSONTokener(new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()))));
        fillResponse();
    }

    private void fillResponse() throws IOException, JSONException {
        Iterator keys = this.keys();
        while(keys.hasNext()){
            String key = keys.next().toString();

            if(key.equals("action"))
                this.action = this.getString(key);
            else if(key.equals("message"))
                this.message = this.getString(key);
            else if(key.equals("serverSparkVersion"))
                this.serverSparkVersion = this.getString(key);
            else if(key.equals("submissionId"))
                this.submissionId = this.getString(key);
            else if(key.equals("driverState"))
                this.driverState = this.getString(key);
            else if(key.equals("workerHostPort"))
                this.workerHostPort = this.getString(key);
            else if(key.equals("workerId"))
                this.workerId = this.getString(key);
            else if(key.equals("success"))
                this.success = this.getBoolean(key);
        }
    }
}
