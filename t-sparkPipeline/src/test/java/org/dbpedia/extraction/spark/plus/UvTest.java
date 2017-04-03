package org.dbpedia.extraction.spark.plus;

import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dataunit.files.impl.ManageableWritableFilesDataUnit;
import eu.unifiedviews.helpers.dpu.test.config.ConfigurationBuilder;
import org.dbpedia.extraction.spark.SparkPipeline;
import org.dbpedia.extraction.spark.SparkPipelineConfig_V1;
import org.junit.Test;

/**
 * Created by chile on 29.03.17.
 */
public class UvTest {

    @Test
    public void execute() throws Exception {
        // Prepare config.
        String configPath = UvTest.class.getClassLoader().getResource("spark.config").toString();
        configPath = configPath.startsWith("file:") ? configPath.substring(5) : configPath;
        SparkPipelineConfig_V1 config = new SparkPipelineConfig_V1(configPath);

        // Prepare DPU.
        SparkPipeline dpuInstance = new SparkPipeline();
        dpuInstance.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());

        // Prepare test environment.
        TestEnvironment environment = new TestEnvironment();

        // Prepare data unit.
        ManageableWritableFilesDataUnit filesInput = (ManageableWritableFilesDataUnit) environment.createFilesFDataUnit("sparkinput");
        //String uri = config.getSparkEntry("spark.dbpedialinks.filemanager.inputdir");
        // add input file(s)
        //filesInput.addExistingFile(uri.substring(uri.lastIndexOf('/')+1), uri);
        environment.addInput("sparkinput", filesInput);

        WritableFilesDataUnit filesOutput = environment.createFilesOutput("sparkoutput");

        try {
            // Run.
            environment.run(dpuInstance);
        } finally {
            // Release resources.
            environment.release();
        }
    }

}
