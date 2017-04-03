package org.dbpedia.extraction.spark.utils;

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

    /** appName value */
    private String appName;

    public SparkDpuConfig(final String filePath) throws Exception {
        if (null != filePath) {
            SparkConfigReader reader = new SparkConfigReader(filePath);

            // create Spark config
            this.sparkConfig = new HashMap<>();

            // load config with loaded parameters
            Map<String, String> loadedConfigParameters = reader.getConfigParameters();
            for (String key : loadedConfigParameters.keySet()) {
                String parameter = loadedConfigParameters.get(key);

                String keyLowerCase = key.toLowerCase();
                if (keyLowerCase.endsWith(".master")) {
                    // found master config parameter
                    this.master = parameter;
                } else if (keyLowerCase.endsWith(".app.name")) {
                    // found appName config parameter
                    this.appName = parameter;
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
}