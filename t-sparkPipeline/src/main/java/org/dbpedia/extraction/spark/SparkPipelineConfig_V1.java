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

    public SparkPipelineConfig_V1()  {
        try {
            this.sparkConfig = new SparkDpuConfig("spark.config");
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