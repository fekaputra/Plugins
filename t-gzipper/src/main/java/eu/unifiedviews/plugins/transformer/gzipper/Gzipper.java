package eu.unifiedviews.plugins.transformer.gzipper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.copy.CopyHelpers;
import eu.unifiedviews.helpers.dataunit.files.FilesHelper;
import eu.unifiedviews.helpers.dataunit.virtualpath.VirtualPathHelpers;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;

@DPU.AsTransformer
public class Gzipper extends AbstractDpu<GzipperConfig_V1> {
    private static final Logger LOG = LoggerFactory.getLogger(Gzipper.class);

    @DataUnit.AsInput(name = "filesInput")
    public FilesDataUnit filesInput;

    @DataUnit.AsOutput(name = "filesOutput")
    public WritableFilesDataUnit filesOutput;

    public Gzipper() {
        super(GzipperVaadinDialog.class, ConfigHistory
                .noHistory(GzipperConfig_V1.class));
    }

    @Override
    protected void innerExecute() throws DPUException {

        Set<FilesDataUnit.Entry> files;
        boolean skipOnError = config.isSkipOnError();

        try {
            files = FilesHelper.getFiles(filesInput);

            for (FilesDataUnit.Entry entry : files) {
                try {
                    File inputFile = new File(URI.create(entry.getFileURIString()));
                    File outputFile = File.createTempFile("___", inputFile.getName() + ".gz", new File(URI.create(filesOutput.getBaseFileURIString())));
                    try (FileInputStream inputStream = new FileInputStream(inputFile); GZIPOutputStream outputStream = new GZIPOutputStream(new FileOutputStream(outputFile))) {
                        IOUtils.copyLarge(inputStream, outputStream);
                    }
                    String sourceVirtualPath = VirtualPathHelpers.getVirtualPath(filesInput, entry.getSymbolicName());
                    String targetVirtualPath;
                    if (StringUtils.isEmpty(sourceVirtualPath)) {
                        targetVirtualPath = entry.getSymbolicName() + ".gz";

                    } else {
                        targetVirtualPath = sourceVirtualPath + ".gz";
                    }
                    FilesHelper.addFile(filesOutput, outputFile,entry.getSymbolicName(), targetVirtualPath );
                } catch (IOException ex) {
                    if (skipOnError) {
                        LOG.warn("Skipping file: '{}' because of error.", entry.toString(), ex);
                    } else {
                        throw ex;
                    }
                }
            }
        } catch (DataUnitException | IOException ex) {
            throw ContextUtils.dpuException(ctx, ex, "Gzipper.executeInner.exception");
        }
    }
}
