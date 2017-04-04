package org.dbpedia.extraction.spark.rest;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by chile on 03.04.17.
 */
public class SparkRestResponse extends JSONObject {

    private String action;
    private String message;
    private String serverSparkVersion;
    private String submissionId;
    private boolean success;
    private String driverState;
    private String workerHostPort;
    private String workerId;

    public SparkRestResponse(JSONTokener responseAsTokener) throws IOException, JSONException {
        super(responseAsTokener);
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

    public String getAction() {
        return action;
    }

    public String getMessage() {
        return message;
    }

    public String getServerSparkVersion() {
        return serverSparkVersion;
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getDriverState() {
        return driverState;
    }

    public String getWorkerHostPort() {
        return workerHostPort;
    }

    public String getWorkerId() {
        return workerId;
    }
}
