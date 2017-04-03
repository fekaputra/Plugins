package org.dbpedia.extraction.spark;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import org.dbpedia.extraction.spark.utils.SparkDpuFileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.dbpedia.extraction.spark.utils.SparkPipelineUtils.prettyMilliseconds;


/**
 * This DPU initializes the SPARK context and runs a SPARK Pipeline
 *
 * @author Chile
 */
@DPU.AsTransformer
public class SparkPipeline extends AbstractDpu<SparkPipelineConfig_V1> {

    private static final Logger log = LoggerFactory.getLogger(SparkPipeline.class);

    @DataUnit.AsInput(name = "sparkinput")
    public FilesDataUnit input;

    @DataUnit.AsOutput(name = "sparkoutput")
    public WritableFilesDataUnit output;

    final private SparkPipelineConfig_V1 config;
    private SparkDpuFileManager fileManager;

    public SparkPipeline() {
		super(SparkPipelineVaadinDialog.class, ConfigHistory.noHistory(SparkPipelineConfig_V1.class));
		//init Config and MasterContext
        String configPath = SparkPipeline.class.getClassLoader().getResource("spark.config").toString();
        configPath = configPath.startsWith("file:") ? configPath.substring(5) : configPath;

        this.config = new SparkPipelineConfig_V1(configPath);
	}

	public void outerExecute() throws DPUException {
	    innerExecute();
    }
		
    @Override
    protected void innerExecute() throws DPUException {

        long startTimeStamp = System.currentTimeMillis();
        ContextUtils.sendShortInfo(ctx, "Running SPARK pipeline: " + config.getSparkEntry("spark.app.name"));

        // we have to initialize the FileManager here, since DataUnits not initialized at construction
        this.fileManager = new SparkDpuFileManager(this.config.getConfig(), this.input, this.output);

        //upload all input files to spark working dir
        try {
            this.fileManager.copyToSparkWorkingDir();
        } catch (DataUnitException e) {
            throw new DPUException(e);
        }



        ContextUtils.sendShortInfo(ctx, "SPARK pipeline completed: " + config.getSparkEntry("spark.app.name") +
                " after " + prettyMilliseconds(System.currentTimeMillis() - startTimeStamp));
    }
	
}