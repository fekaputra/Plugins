package org.dbpedia.extraction.spark;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 * Created by chile on 21.04.17.
 */
public class SparkPipelineVvaadinDialog extends AbstractDialog<SparkPipelineConfig_V1> {

    private SparkPipelineConfig_V1 config;

    public <DPU extends AbstractDpu<SparkPipelineConfig_V1>> SparkPipelineVvaadinDialog(Class<DPU> dpuClass) {
        super(dpuClass);
    }

    @Override
    protected void buildDialogLayout() {

    }

    @Override
    protected void setConfiguration(SparkPipelineConfig_V1 conf) throws DPUConfigException {
        this.config = conf;
    }

    @Override
    protected SparkPipelineConfig_V1 getConfiguration() throws DPUConfigException {
        this.config = new SparkPipelineConfig_V1();
        return this.config;
    }
}
