package org.dbpedia.extraction.spark;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.exec.UserExecContext;
import eu.unifiedviews.helpers.dpu.extension.Extension;
import eu.unifiedviews.helpers.dpu.extension.ExtensionException;
import org.dbpedia.extraction.spark.dialog.SparkPipelineVaadinDialog;
import org.dbpedia.extraction.spark.rest.SparkRestClient;
import org.dbpedia.extraction.spark.rest.SparkRestResponse;
import org.dbpedia.extraction.spark.utils.SparkDpuFileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

import static org.dbpedia.extraction.spark.utils.SparkPipelineUtils.prettyMilliseconds;


/**
 * This DPU initializes the SPARK context and runs a SPARK Pipeline
 *
 * @author Chile
 */
@DPU.AsTransformer
public class SparkPipeline extends AbstractDpu<SparkPipelineConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(SparkPipeline.class);

    @DataUnit.AsInput(name = "sparkinput")
    public FilesDataUnit input;

    @DataUnit.AsOutput(name = "sparkoutput")
    public WritableFilesDataUnit output;

    final private SparkMasterContext masterContext;
    final private SparkPipelineConfig_V1 config;
    private SparkDpuFileManager fileManager;

    public SparkPipeline() {
		super(SparkPipelineVaadinDialog.class, ConfigHistory.noHistory(SparkPipelineConfig_V1.class));
        this.config = new SparkPipelineConfig_V1();
        masterContext = new SparkMasterContext(this);
        masterContext.setDpuConfig(this.config);
        try {
            Field f1 = this.getClass().getSuperclass().getDeclaredField("masterContext");
            f1.setAccessible(true);
            f1.set(this, masterContext);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

	public void outerExecute() throws DPUException {
	    innerExecute();
    }
		
    @Override
    protected void innerExecute() throws DPUException {

        //TODO!
        String currentPipelineApp = this.config.getSparkConfig().getAppName();

        long startTimeStamp = System.currentTimeMillis();
        ContextUtils.sendShortInfo(ctx, "Running SPARK pipeline: " + config.getSparkConfig().getByStringKey("spark.app.name").toString());

        // we have to initialize the FileManager here, since DataUnits not initialized at construction
        this.fileManager = new SparkDpuFileManager(this.config.getSparkConfig(), this.input, this.output);
        SparkRestClient restClient = new SparkRestClient(this.config.getSparkConfig());

        //upload all input files to spark working dir
        try {
            this.fileManager.copyToSparkWorkingDir();
            SparkRestResponse submission = restClient.submitJob(currentPipelineApp);

            //wait for the launch
            //TODO make it configurable (how long we should sleep)
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
                this.fileManager.copyDirectoryToOutputDirectory(this.config.getSparkConfig().getSparkOutputDir(currentPipelineApp));
            else
                throw new DPUException("Spark pipeline " + this.config.getSparkConfig().getAppName() + " has entered an unexpected state: " + status.getDriverState());

        } catch (Exception e) {
            throw new DPUException(e);
        }

        ContextUtils.sendShortInfo(ctx, "SPARK pipeline completed: " + config.getSparkConfig().getAppName() +
                " after " + prettyMilliseconds(System.currentTimeMillis() - startTimeStamp));
    }

    @Override
    public void configure(String config) throws DPUConfigException {
        super.configure(config);
    }

    @Override
    public ConfigHistory<SparkPipelineConfig_V1> getConfigHistoryHolder() {
        return ConfigHistory.noHistory(SparkPipelineConfig_V1.class);
    }

    @Override
    public void execute(DPUContext context) throws DPUException {
        // Set master configuration and initialize ConfigTransformer -> initialize addons.
        this.masterContext.init("", context);
        // Set variables for DPU.
        this.ctx = new UserExecContext(this.masterContext);
        // Execute DPU's code - innerInit.
        try {
            LOG.info("innerInit:start");
            innerInit();
            LOG.info("innerInit:end");
        } catch (DataUnitException ex) {
            throw new DPUException("DPU.innerInit fail for problem with data unit.", ex);
        } catch (DPUException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new DPUException("DPU.innerInit throws throwable.", ex);
        }
        // {@link Addon}'s execution point.
        boolean executeDpu = executeAddons(Extension.ExecutionPoint.PRE_EXECUTE);
        // Main execution for user code.
        DPUException exception = null;
        try {
            if (executeDpu) {
                LOG.info("innerExecute:start");
                innerExecute();
                LOG.info("innerExecute:end");
            }
        } catch (DPUException ex) {
            exception = ex;
        } catch (RuntimeException ex) {
            exception = new DPUException("DPU.innerExecute throws runtime exception.", ex);
        } catch (Throwable ex) {
            exception = new DPUException("DPU.innerExecute throws throwable.", ex);
        }
        if (exception != null) {
            LOG.error("DPU execution failed!", exception);
        }

        // Execute DPU's code - innerCleanUp.
        try {
            LOG.info("innerCleanUp:start");
            innerCleanUp();
            LOG.info("innerCleanUp:stop");
        } catch (Throwable ex) {
            if (exception == null) {
                exception = new DPUException("DPU.innerCleanUp throws throwable.", ex);
            } else {
                context.sendMessage(DPUContext.MessageType.ERROR, "DPU Failed",
                        "DPU throws Throwable in innerCleanUp method. See logs for more details.");
                LOG.error("Throwable has ben thrown from innerCleanUp!", ex);
            }
        }
        // {@link Addon}'s execution point.
        executeAddons(Extension.ExecutionPoint.POST_EXECUTE);
        // And throw an exception.
        if (exception != null) {
            throw exception;
        }
    }

    private boolean executeAddons(Extension.ExecutionPoint execPoint) {
        boolean result = true;
        for (Extension item : this.masterContext.getExtensions()) {
            if (item instanceof Extension.Executable) {
                final Extension.Executable executable = (Extension.Executable) item;
                try {
                    LOG.debug("Executing '{}' with on '{}' point", executable.getClass().getSimpleName(),
                            execPoint.toString());
                    executable.execute(execPoint);
                } catch (ExtensionException ex) {
                    ContextUtils.sendError(this.ctx, "Addon execution failed",
                            ex, "Addon: s", item.getClass().getSimpleName());
                    result = false;
                }
            }
        }
        return result;
    }
}
