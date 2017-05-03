package org.dbpedia.extraction.spark;

import org.dbpedia.extraction.spark.dialog.SparkDpuConfig;

import java.net.URL;


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
            URL configUrl = SparkPipeline.class.getClassLoader().getResource("spark.config");
            this.sparkConfig = new SparkDpuConfig(configUrl);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public SparkPipelineConfig_V1(URL configUrl)  {
        try {
            this.sparkConfig = new SparkDpuConfig(configUrl);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public SparkDpuConfig getConfig() {
        return this.sparkConfig;
    }

    public <T> T getSparkEntry(String key){
        //TODO test
        return (T) sparkConfig.getItem(key).getBean().getValue();
    }
}