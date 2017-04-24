package eu.unifiedviews.plugins;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.files.FilesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;

import java.io.File;

/**
 * Main data processing unit class.
 *
 * @author Unknown
 */

@DPU.AsQuality
public class RdfUnitDPU extends AbstractDpu<RdfUnitDPUConfig_V1> {

    private static final Logger log = LoggerFactory.getLogger(RdfUnitDPU.class);

    @DataUnit.AsInput(name = "input")
    public FilesDataUnit input;

    @DataUnit.AsOutput(name = "output")
    public WritableFilesDataUnit output;

    //the config, created by you dynamically or by reading from input in the Vaadin dialog (see other DPUs for examples)
    final private RdfUnitDPUConfig_V1 config;

	public RdfUnitDPU() {
	    super(RdfUnitDPUVaadinDialog.class, ConfigHistory.noHistory(RdfUnitDPUConfig_V1.class));
	    config = new RdfUnitDPUConfig_V1();
	}
		
    @Override
    protected void innerExecute() throws DPUException {

        ContextUtils.sendShortInfo(ctx, "MyDpu.message");

        try {
            for(FilesDataUnit.Entry file : FilesHelper.getFiles(this.input)) {
                File localFile = new File(file.getFileURIString());
                if (!localFile.isFile() || !localFile.toURI().getScheme().contains("file"))
                    throw new IllegalArgumentException("The provided source is not a file on the local file system: " + file.getFileURIString());

                /*
                 * Do something here...
                 * */

                /*
                 * add RDFUnit result files to the outputunit
                 */
                File results = new File("path/to/files");
                FilesHelper.addFile(this.output, results, results.toString());
            }
        } catch (DataUnitException e) {
            throw new DPUException(e);
        }

    }
	
}
