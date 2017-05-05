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
            this.put("appResource", config.getProperty("spark.jars").get());
        else
            throw new JSONException("spark.jars" + " property was not provided for use case " + config.getAppName());

        if(config.getProperty("spark.clientSparkVersion").isPresent())
            this.put("clientSparkVersion", config.getProperty("spark.clientSparkVersion").get());
        //else  TODO check if needed
        //    throw new JSONException("spark.clientSparkVersion" + " property was not provided for use case " + config.getAppName());

        this.put("mainClass", "org.dbpedia.spark.core.SparkPipelineExecutor");
        if(config.getProperty(appPrefix + "executorArguments").isPresent())
            this.put("appArgs", config.getProperty(appPrefix + "executorArguments").get().split(","));
        else
            this.put("appArgs", new String[]{});

        JSONObject sparkEnvironment = new JSONObject();
        sparkEnvironment.put("SPARK_ENV_LOADED", "1");

        this.put("environmentVariables", sparkEnvironment);

        //now add all spark properties
        JSONObject sparkProperties = new JSONObject();
        for(SparkConfigEntry ent : config.getItemIds()){
            sparkProperties.put(ent.getKey(), ent.getValue());
        }
        this.put("sparkProperties", sparkProperties);
    }

    public URI getSubmitUri() {
        return submitUri;
    }
}
