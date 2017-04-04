package org.dbpedia.extraction.spark.utils;

import org.dbpedia.extraction.spark.SparkPipeline;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by chile on 27.03.17.
 */
public class SparkDpuConfig {

    /** Spark configuration instance */
    final Map<String, String> sparkConfig;

    /** master value */
    private String master;

    /** rest endpoint */
    private String restApi;

    /** appName value */
    private String appName;

    public SparkDpuConfig(final String resourceName) throws Exception {
        if (null != resourceName && !resourceName.isEmpty()) {
            InputStream configStream = SparkPipeline.class.getClassLoader().getResourceAsStream(resourceName);
            SparkConfigReader reader = new SparkConfigReader(configStream);

            // create Spark config
            this.sparkConfig = new HashMap<>();

            // load config with loaded parameters
            Map<String, String> loadedConfigParameters = reader.getConfigParameters();
            for (String key : loadedConfigParameters.keySet()) {
                String parameter = loadedConfigParameters.get(key);

                String keyLowerCase = key.toLowerCase();
                if (keyLowerCase.endsWith("spark.master")) {
                    // found master config parameter
                    this.master = parameter;
                } else if (keyLowerCase.endsWith("spark.app.name")) {
                    // found appName config parameter
                    this.appName = parameter;
                } else if (keyLowerCase.endsWith("spark.restApi")) {
                    // found restApi config parameter
                    this.restApi = parameter;
                }

                sparkConfig.put(key, parameter);
            }
        } else
            throw new IllegalArgumentException("No SPARK config file was provided!");
    }

    public String getAppName() {
        return (null == this.appName ? "sparkpipeline" : this.appName);
    }

    public String getMasterUrl(){
        return (null == this.master ? "local[*]" : this.master);
    }

    public Map<String, String> getSparkConfig() {
        return this.sparkConfig;
    }

    public Optional<String> getProperty(String key){
        return Optional.ofNullable(this.sparkConfig.get(key));
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getRestApiUri(){
        if(this.restApi == null){
            //infer from master using the default port
            this.restApi = this.master.substring(0, this.master.lastIndexOf(':')) + ":6066";
        }
        return this.restApi.replace("spark:", "http:");
    }

    public String getSparkOutputDir(String appName){
        return sparkConfig.get("spark." + appName + ".filemanager.outputdir");
    }
}