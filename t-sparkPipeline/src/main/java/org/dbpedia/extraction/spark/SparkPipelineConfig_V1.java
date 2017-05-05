package org.dbpedia.extraction.spark;

import eu.unifiedviews.dpu.config.DPUConfigException;
import org.dbpedia.extraction.spark.dialog.SparkConfigEntry;
import org.dbpedia.extraction.spark.dialog.SparkDpuConfig;
import org.dbpedia.extraction.spark.utils.SparkPipelineUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;


/**
 * Configuration class for SparkPipeline.
 *
 * @author Kay
 */
public class SparkPipelineConfig_V1 {

    private SparkDpuConfig sparkMandatoryEntries;
    private SparkDpuConfig sparkRecommendedEntries;
    private SparkDpuConfig sparkOptionalEntries;
    private SparkDpuConfig useCaseMandatoryEntries;
    private SparkDpuConfig useCaseOptionalEntries;

    /** spark configuration */
    private SparkDpuConfig sparkConfig;

    public SparkPipelineConfig_V1()  {
        try {
            URL configUrl = SparkPipeline.class.getClassLoader().getResource("spark.config");
            this.sparkConfig = new SparkDpuConfig(configUrl);
            splitConfig();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public SparkPipelineConfig_V1(SparkDpuConfig sparkDpuConfig)  {
        try {
            this.sparkConfig = sparkDpuConfig;
            splitConfig();
        } catch (DPUConfigException e) {
            throw new RuntimeException(e);
        }
    }

    public SparkPipelineConfig_V1(URL configUrl)  {
        try {
            this.sparkConfig = new SparkDpuConfig(configUrl);
            splitConfig();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void splitConfig() throws DPUConfigException {
        try {
            this.sparkMandatoryEntries = (SparkDpuConfig)this.getSparkConfig().clone();
            this.sparkMandatoryEntries.addContainerFilter(SparkPipelineUtils.getContainerFilter(Arrays.asList(SparkConfigEntry.SparkPropertyCategory.SparkMandatory), null));
            this.sparkRecommendedEntries = (SparkDpuConfig)this.getSparkConfig().clone();
            this.sparkRecommendedEntries.addContainerFilter(SparkPipelineUtils.getContainerFilter(Arrays.asList(SparkConfigEntry.SparkPropertyCategory.SparkRecommended), null));
            this.sparkOptionalEntries = (SparkDpuConfig)this.getSparkConfig().clone();
            this.sparkOptionalEntries.addContainerFilter(SparkPipelineUtils.getContainerFilter(Arrays.asList(SparkConfigEntry.SparkPropertyCategory.SparkOptional), null));
            this.useCaseMandatoryEntries = (SparkDpuConfig)this.getSparkConfig().clone();
            this.useCaseMandatoryEntries.addContainerFilter(SparkPipelineUtils.getContainerFilter(
                    Arrays.asList(SparkConfigEntry.SparkPropertyCategory.UsecaseRecommended, SparkConfigEntry.SparkPropertyCategory.UsecaseMandatory),
                    this.getSparkConfig().getAppName()));
            this.useCaseOptionalEntries = (SparkDpuConfig)this.getSparkConfig().clone();
            this.useCaseOptionalEntries.addContainerFilter(SparkPipelineUtils.getContainerFilter(
                    Arrays.asList(SparkConfigEntry.SparkPropertyCategory.UsecaseOptional),
                    this.getSparkConfig().getAppName()));
        } catch (CloneNotSupportedException e) {
            throw new DPUConfigException(e);
        }
    }

    public SparkDpuConfig updateSparkConfig() {

        SparkDpuConfig conf = null;
        try {
            conf = new SparkDpuConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        conf.addAll(this.getSparkMandatoryEntries().getFilteredIds());
        conf.addAll(this.getSparkRecommendedEntries().getFilteredIds());
        conf.addAll(this.getSparkOptionalEntries().getFilteredIds());
        conf.addAll(this.getUseCaseMandatoryEntries().getFilteredIds());
        conf.addAll(this.getUseCaseOptionalEntries().getFilteredIds());

        this.sparkConfig = conf;
        return conf;
    }

    public SparkDpuConfig getSparkConfig() {
        return this.sparkConfig;
    }

    public SparkDpuConfig getSparkMandatoryEntries() {
        return sparkMandatoryEntries;
    }

    public SparkDpuConfig getSparkRecommendedEntries() {
        return sparkRecommendedEntries;
    }

    public SparkDpuConfig getSparkOptionalEntries() {
        return sparkOptionalEntries;
    }

    public SparkDpuConfig getUseCaseMandatoryEntries() {
        return useCaseMandatoryEntries;
    }

    public SparkDpuConfig getUseCaseOptionalEntries() {
        return useCaseOptionalEntries;
    }

    public <T> T getSparkEntry(String key){
        //TODO test
        return (T) sparkConfig.getItem(key).getBean().getValue();
    }
}