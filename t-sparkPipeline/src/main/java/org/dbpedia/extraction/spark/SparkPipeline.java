package org.dbpedia.extraction.spark;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import org.dbpedia.extraction.spark.rest.SparkRestClient;
import org.dbpedia.extraction.spark.rest.SparkRestResponse;
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
        this.config = new SparkPipelineConfig_V1();
	}

	public void outerExecute() throws DPUException {
	    innerExecute();
    }
		
    @Override
    protected void innerExecute() throws DPUException {

        //TODO!
        String currentPiepelineApp = this.config.getConfig().getAppName();

        long startTimeStamp = System.currentTimeMillis();
        ContextUtils.sendShortInfo(ctx, "Running SPARK pipeline: " + config.getConfig().getByStringKey("spark.app.name"));

        // we have to initialize the FileManager here, since DataUnits not initialized at construction
        this.fileManager = new SparkDpuFileManager(this.config.getConfig(), this.input, this.output);
        SparkRestClient restClient = new SparkRestClient(this.config.getConfig());

        //upload all input files to spark working dir
        try {
            this.fileManager.copyToSparkWorkingDir();
            SparkRestResponse submission = restClient.submitJob(currentPiepelineApp);

            //wait for the launch
            Thread.sleep(3000);

            //wait until the pipeline is done
            SparkRestResponse status = restClient.getStatus(submission.getSubmissionId());
            while(status.getDriverState().equals("RUNNING") || status.getDriverState().equals("LAUNCHING"))
            {
                //TODO interrupt? Use a better way than Thread.sleep()
                Thread.sleep(1000);
                status = restClient.getStatus(submission.getSubmissionId());
            }

            //if no error occurred...
            if(status.getDriverState().equals("FINISHED"))
                this.fileManager.copyDirectoryToOutputDirectory(this.config.getConfig().getSparkOutputDir(currentPiepelineApp));
            else
                throw new DPUException("Spark pipeline " + this.config.getConfig().getAppName() + " experienced an error: " + status.getMessage());

        } catch (Exception e) {
            throw new DPUException(e);
        }

        ContextUtils.sendShortInfo(ctx, "SPARK pipeline completed: " + config.getSparkEntry("spark.app.name") +
                " after " + prettyMilliseconds(System.currentTimeMillis() - startTimeStamp));
    }
	
}