package org.dbpedia.extraction.spark;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.dbpedia.spark.core.datasources.MasterContext;

/**
 * Configuration class for SparkPipeline.
 *
 * @author Unknown
 */
public class SparkPipelineConfig_V1 {

    private SparkDpuConfig config;

    //dummy constructor
    public SparkPipelineConfig_V1() {  throw new IllegalArgumentException("Please provide configurstion File");  }

    public SparkPipelineConfig_V1(String configPath)  {
        try {
            config = new SparkDpuConfig(configPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SparkDpuConfig getConfig() {
        return config;
    }

    public String getInputFilePath(){
        return "/home/chile/unifiedviews/inputsToSparkFragment.nt";
    }

    public String getOutputFilePath(){
        return "/home/chile/unifiedviews/testOut.nt";
    }

    public String getPPXFilePath(){
        return "/home/chile/unifiedviews/outputs of PPX.ttl";
    }
}