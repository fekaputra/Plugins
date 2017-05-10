package org.dbpedia.extraction.spark;

import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.exec.ExecContext;
import org.dbpedia.extraction.spark.SparkPipelineConfig_V1;

/**
 * Created by chile on 10.05.17.
 */
public class SparkMasterContext extends ExecContext<SparkPipelineConfig_V1> {
    /**
     * Cause given DPU initialization. Must not be called in constructor!
     *
     * @param dpuInstance
     * @throws DPUException
     */
    public SparkMasterContext(AbstractDpu<SparkPipelineConfig_V1> dpuInstance) {
        super(dpuInstance);
    }

    @Override
    protected void init(String configAsString, DPUContext dpuContext) throws DPUException {
        super.init(configAsString, dpuContext);
    }
}
