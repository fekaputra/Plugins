/**
 * Created by chile on 24.04.17.
 */

package eu.unifiedviews.plugins.test;

import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dataunit.files.impl.ManageableWritableFilesDataUnit;
import eu.unifiedviews.dpu.config.vaadin.ConfigDialogContext;
import eu.unifiedviews.helpers.dpu.test.config.ConfigurationBuilder;
import eu.unifiedviews.plugins.RdfUnitDPU;
import eu.unifiedviews.plugins.RdfUnitDPUConfig_V1;
import eu.unifiedviews.plugins.RdfUnitDPUVaadinDialog;
import org.junit.Test;

import java.util.Locale;
import java.util.Map;

public class RdfUnitTest {

    @Test
    public void execute() throws Exception {
        // Prepare config.
        RdfUnitDPUConfig_V1 config = new RdfUnitDPUConfig_V1();


        // Prepare DPU.
        RdfUnitDPU dpuInstance = new RdfUnitDPU();
        dpuInstance.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());

        // Prepare test environment.
        final TestEnvironment environment = new TestEnvironment();

        // Prepare data unit.
        ManageableWritableFilesDataUnit filesInput = (ManageableWritableFilesDataUnit) environment.createFilesFDataUnit("sparkinput");
        //String uri = config.getSparkEntry("spark.dbpedialinks.filemanager.inputdir");
        // add input file(s)
        //filesInput.addExistingFile(uri.substring(uri.lastIndexOf('/')+1), uri);
        environment.addInput("input", filesInput);

        WritableFilesDataUnit filesOutput = environment.createFilesOutput("output");


        RdfUnitDPUVaadinDialog dialog = new RdfUnitDPUVaadinDialog();
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
