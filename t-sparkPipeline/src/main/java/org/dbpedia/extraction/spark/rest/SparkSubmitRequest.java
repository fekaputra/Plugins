package org.dbpedia.extraction.spark.rest;

import org.apache.http.client.utils.URIBuilder;
import org.dbpedia.extraction.spark.dialog.SparkConfigEntry;
import org.dbpedia.extraction.spark.dialog.SparkDpuConfig;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by chile on 03.04.17.
 *
 * This will create a REST request to submit a new SPARK job.
 * Based on the hidden SPARK REST Api (which is not well documented!).
 *
 * Template: (see: http://arturmkrtchyan.com/apache-spark-hidden-rest-api)
 *
 * curl -X POST http://spark-cluster-ip:6066/v1/submissions/create --header "Content-Type:application/json;charset=UTF-8" --data '{
 "action" : "CreateSubmissionRequest",
 "appArgs" : [ "myAppArgument1" ],
 "appResource" : "file:/myfilepath/spark-job-1.0.jar",
 "clientSparkVersion" : "1.5.0",
 "environmentVariables" : {
 "SPARK_ENV_LOADED" : "1"
 },
 "mainClass" : "com.mycompany.MyJob",
 "sparkProperties" : {
 "spark.jars" : "file:/myfilepath/spark-job-1.0.jar",
 "spark.driver.supervise" : "false",
 "spark.app.name" : "MyJob",
 "spark.eventLog.enabled": "true",
 "spark.submit.deployMode" : "cluster",
 "spark.master" : "spark://spark-cluster-ip:6066"
 }
 }'
 */
public class SparkSubmitRequest extends JSONObject {

    private URI submitUri;

    public SparkSubmitRequest(SparkDpuConfig config) throws JSONException, URISyntaxException {
        super();
        submitUri = new URIBuilder(config.getRestApiUri()).setPath("/v1/submissions/create").build();

        fillFromSparkConfig(config);
    }

    private void fillFromSparkConfig(SparkDpuConfig config) throws JSONException {
        String appPrefix = "spark." + config.getAppName() + ".";

        //add all relevant submit properties
        this.put("action", "CreateSubmissionRequest");

        if(config.getProperty("spark.jars").isPresent())
            this.put("appResource", config.getByStringKey("spark.jars").toString());
        else
            throw new JSONException("spark.jars" + " property was not provided for use case " + config.getAppName());

        if(config.getProperty("spark.client.version").isPresent())
            this.put("clientSparkVersion", config.getByStringKey("spark.client.version").toString());
        else
            throw new JSONException("spark.client.version" + " property was not provided for use case " + config.getAppName());

        //TODO main class of spark pipeline - server part - hardcoded
        this.put("mainClass", "org.dbpedia.spark.core.SparkPipelineExecutor");
        if(config.getProperty(appPrefix + "executorArguments").isPresent())
            this.put("appArgs", config.getByStringKey(appPrefix + "executorArguments").toString().split(","));
        else
            this.put("appArgs", new String[]{});

        JSONObject sparkEnvironment = new JSONObject();
        sparkEnvironment.put("SPARK_ENV_LOADED", "1");

        this.put("environmentVariables", sparkEnvironment);

        //now add all spark properties
        JSONObject sparkProperties = new JSONObject();
        for(SparkConfigEntry ent : config.getItemIds()){
            String[] keySplits = ent.getKey().split("\\.");
            if(!keySplits[1].equals(config.getAppName()) && config.getKnownUseCaseNames().contains(keySplits[1]))
                continue;
            sparkProperties.put(ent.getKey(), ent.toString());
        }
        this.put("sparkProperties", sparkProperties);
    }

    public URI getSubmitUri() {
        return submitUri;
    }
}
