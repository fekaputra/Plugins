package org.dbpedia.extraction.spark.utils;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaSparkContext;
import org.dbpedia.spark.core.datasources.MasterContext;

/**
 * This class can be used to handle spark context
 *
 * @author kay
 *
 */
public class SparkDpuContext {

    /** config which holds information for the DPU and for Apache Spark */
    final SparkDpuConfig sparkConfig;

    /** spark context instance */
    private JavaSparkContext sparkContext;

    public SparkDpuContext(final SparkDpuConfig sparkConfig) {
        this.sparkConfig = sparkConfig;
        this.sparkContext = new JavaSparkContext(sparkConfig.sparkConfig);

        try {
            MasterContext.init(this.sparkContext.sc());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public JavaSparkContext getJavaSparkContext() {
        return sparkContext;
    }

    public SparkContext getSparkContext() {
        return getJavaSparkContext().sc();
    }
}
