package org.dbpedia.extraction.spark;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.files.FilesHelper;
import org.dbpedia.extraction.spark.plus.FilePipeline;
import org.dbpedia.spark.core.RddLike;
import org.dbpedia.spark.core.RddLike$;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;

import java.util.Collections;


/**
 * Main data processing unit class.
 *
 * @author Unknown
 */
@DPU.AsTransformer
public class SparkPipeline extends AbstractDpu<SparkPipelineConfig_V1> {

    private static final Logger log = LoggerFactory.getLogger(SparkPipeline.class);

    @DataUnit.AsInput(name = "input")
    private FilesDataUnit input;

    @DataUnit.AsOutput(name = "output")
    private WritableFilesDataUnit output;

	public SparkPipeline() {
		super(SparkPipelineVaadinDialog.class, ConfigHistory.noHistory(SparkPipelineConfig_V1.class));
	}
		
    @Override
    protected void innerExecute() throws DPUException {

        ContextUtils.sendShortInfo(ctx, "SparkPipeline.message");

        try {
            for(FilesDataUnit.Entry entry : FilesHelper.getFiles(input)){
                RddLike<String> rdd = RddLike$.MODULE$.fromTextFiles(Collections.singletonList(entry.getFileURIString()));

                FilePipeline pipeline = new FilePipeline();
                rdd = (RddLike<String>) pipeline.transform(rdd);

                FilesHelper.createFile(output, entry.getFileURIString());
                rdd.saveAsTextFile(entry.getFileURIString());
            }

        } catch (DataUnitException e) {
            throw new DPUException(e);
        }
    }
	
}
