package org.dbpedia.extraction.spark.utils;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.internal.config.ConfigReader;
import org.dbpedia.extraction.spark.utils.SparkConfigReader;
import org.dbpedia.spark.core.datasources.MasterContext;

import java.util.Map;

/**
 * Created by chile on 27.03.17.
 */
public class SparkDpuConfig {

    /** Spark configuration instance */
    final SparkConf sparkConfig;

    /** master value */
    private String master;

    /** appName value */
    private String appName;

    public SparkDpuConfig(final String filePath) throws Exception {
        if (null != filePath) {
            SparkConfigReader reader = new SparkConfigReader(filePath);

            // create Spark config
            this.sparkConfig = new SparkConf();

            // load config with loaded parameters
            Map<String, String> loadedConfigParameters = reader.getConfigParameters();
            for (String key : loadedConfigParameters.keySet()) {
                String parameter = loadedConfigParameters.get(key);

                String keyLowerCase = key.toLowerCase();
                if (keyLowerCase.endsWith(".master")) {
                    // found master config parameter
                    this.master = parameter;
                    this.sparkConfig.setMaster(parameter);
                    continue;
                } else if (keyLowerCase.endsWith(".app.name")) {
                    // found appName config parameter
                    this.appName = parameter;
                    this.sparkConfig.setAppName(parameter);
                    continue;
                }

                sparkConfig.set(key, parameter);
            }
        } else {
            // parameters should be loaded by default from command line parameters
            this.sparkConfig = new SparkConf(true);

            ///TODO: km: Remove once we run on the cluster!!!!
            this.sparkConfig.setAppName(this.getAppName());
            this.sparkConfig.setMaster(this.getMasterUrl());
        }
    }

    protected String getAppName() {
        return (null == this.appName ? "sparkpipeline" : this.appName);
    }

    protected String getMasterUrl(){
        return (null == this.master ? "local[*]" : this.master);
    }

    public SparkConf getSparkConfig() {
        return this.sparkConfig;
    }
}