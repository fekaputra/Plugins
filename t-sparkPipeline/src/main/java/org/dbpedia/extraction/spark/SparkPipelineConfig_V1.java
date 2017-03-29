package org.dbpedia.extraction.spark;

import org.dbpedia.extraction.spark.utils.SparkDpuConfig;

/**
 * Configuration class for SparkPipeline.
 *
 * @author Kay
 */
public class SparkPipelineConfig_V1 {

    /** spark configuration */
    final SparkDpuConfig sparkConfig;

    //dummy constructor
    public SparkPipelineConfig_V1() {  throw new IllegalArgumentException("Please provide configurstion File");  }

    public SparkPipelineConfig_V1(String configPath)  {
        try {
            this.sparkConfig = new SparkDpuConfig(configPath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public SparkDpuConfig getConfig() {
        return this.sparkConfig;
    }

    public String getSparkEntry(String key){
        return sparkConfig.getSparkConfig().get(key);
    }
}