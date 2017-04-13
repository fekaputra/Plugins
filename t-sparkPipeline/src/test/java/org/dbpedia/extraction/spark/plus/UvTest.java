package org.dbpedia.extraction.spark.plus;

import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dataunit.files.impl.ManageableWritableFilesDataUnit;
import eu.unifiedviews.dpu.config.vaadin.ConfigDialogContext;
import eu.unifiedviews.helpers.dpu.test.config.ConfigurationBuilder;
import org.dbpedia.extraction.spark.SparkPipeline;
import org.dbpedia.extraction.spark.SparkPipelineConfig_V1;
import org.dbpedia.extraction.spark.SparkPipelineVaadinDialog;
import org.junit.Test;

import java.util.Locale;
import java.util.Map;

/**
 * Created by chile on 29.03.17.
 */
public class UvTest {

    @Test
    public void execute() throws Exception {
        // Prepare config.
        SparkPipelineConfig_V1 config = new SparkPipelineConfig_V1();


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


        SparkPipelineVaadinDialog dialog = new SparkPipelineVaadinDialog();
        dialog.setContext(new ConfigDialogContext() {
            @Override
            public boolean isTemplate() {
                return true;
            }

            @Override
            public Locale getLocale() {
                return Locale.GERMAN;
            }

            @Override
            public Map<String, String> getEnvironment() {
                return environment.getContext().getEnvironment();
            }

            @Override
            public String getUserExternalId() {
                return "xx";
            }

            @Override
            public Long getUserId() {
                return 1l;
            }
        });
        dialog.initialize();
        dialog.buildDialogLayout();

        try {
            // Run.
            environment.run(dpuInstance);
        } finally {
            // Release resources.
            environment.release();
        }
    }

}
