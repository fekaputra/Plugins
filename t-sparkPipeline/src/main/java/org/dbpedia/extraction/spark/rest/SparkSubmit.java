package org.dbpedia.extraction.spark.rest;

import org.dbpedia.extraction.spark.utils.SparkDpuConfig;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by chile on 03.04.17.
 */
public class SparkSubmit extends JSONObject {

    public SparkSubmit(SparkDpuConfig config) throws JSONException {
        super();
        fillFromSparkConfig(config);
    }

    private void fillFromSparkConfig(SparkDpuConfig config) throws JSONException {
        String appPrefix = "spark." + config.getAppName() + ".";

        //add all relevant submit properties
        this.put("action", "CreateSubmissionRequest");

        if(config.getProperty(appPrefix + "jarLocation").isPresent())
            this.put("appResource", config.getProperty(appPrefix + "jarLocation").get());
        else
            throw new JSONException("jarLocation" + " property was not provided for use case " + config.getAppName());

        if(config.getProperty("spark.clientSparkVersion").isPresent())
            this.put("clientSparkVersion", config.getProperty("spark.clientSparkVersion").get());
        //else  TODO check if needed
        //    throw new JSONException("spark.clientSparkVersion" + " property was not provided for use case " + config.getAppName());

        this.put("mainClass", "SparkPipelineExecutor");
        if(config.getProperty(appPrefix + "executorArguments").isPresent())
            this.put("appArgs", config.getProperty(appPrefix + "executorArguments").get().split(","));

        //now add all spark properties
        JSONObject sparkProperties = new JSONObject();
        for(Map.Entry<String, String> ent : config.getSparkConfig().entrySet()){
            sparkProperties.put(ent.getKey(), ent.getValue());
        }
        this.put("sparkProperties", sparkProperties);
    }
}
