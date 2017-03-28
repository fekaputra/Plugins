package org.dbpedia.extraction.spark;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaSparkContext;
import org.dbpedia.spark.core.datasources.MasterContext;

/**
 * Created by chile on 27.03.17.
 */
public class SparkDpuConfig {

    public SparkDpuConfig(String fileLocation) throws Exception {
        //TODO
        MasterContext.init(getSparkContext());
    }

    public String getAppName(){
        //TODO
        return "sparkpipeline";
    }

    public String getMasterUrl(){
        //TODO
        return "local[*]";
    }

    private SparkConf sparkConf;
    public SparkConf getSparkConfig() {
        if(this.sparkConf == null) {
            sparkConf = new SparkConf(true);
            sparkConf.setAppName(this.getAppName());
            sparkConf.setMaster(this.getMasterUrl());
        }
        return sparkConf;
    }

    private JavaSparkContext sparkContext;
    public JavaSparkContext getJavaSparkContext() {
        if(sparkContext == null) {
            this.sparkContext = new JavaSparkContext(getSparkConfig());
        }
        return sparkContext;
    }

    public SparkContext getSparkContext() {
        return getJavaSparkContext().sc();
    }
}
