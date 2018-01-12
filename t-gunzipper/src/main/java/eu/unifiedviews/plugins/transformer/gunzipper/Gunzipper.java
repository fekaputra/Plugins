package eu.unifiedviews.plugins.transformer.gunzipper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.FilesDataUnit.Entry;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.copy.CopyHelpers;
import eu.unifiedviews.helpers.dataunit.files.FilesHelper;
import eu.unifiedviews.helpers.dataunit.virtualpath.VirtualPathHelpers;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;

//import eu.unifiedviews.helpers.dpu.rdf.sparql.SparqlUtils;

/**
 * Main data processing unit class.
 */
@DPU.AsTransformer
public class Gunzipper extends AbstractDpu<GunzipperConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(Gunzipper.class);

    @DataUnit.AsInput(name = "filesInput")
    public FilesDataUnit filesInput;

    @DataUnit.AsOutput(name = "filesOutput")
    public WritableFilesDataUnit filesOutput;

    public Gunzipper() {
        super(GunzipperVaadinDialog.class, ConfigHistory.noHistory(GunzipperConfig_V1.class));
    }

    @Override
    protected void innerExecute() throws DPUException {
        Set<FilesDataUnit.Entry> files;
        boolean skipOnError = config.isSkipOnError();
        try {
            files = FilesHelper.getFiles(filesInput);

            for (Entry entry : files) {
                try {
                    File inputFile = new File(URI.create(entry.getFileURIString()));
                    File outputFile = File.createTempFile("___", inputFile.getName(), new File(URI.create(filesOutput.getBaseFileURIString())));

                    try (GZIPInputStream inputStream = new GZIPInputStream(new FileInputStream(inputFile)); FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                        IOUtils.copyLarge(inputStream, outputStream);
                    }
                    FilesHelper.addFile(filesOutput, outputFile,entry.getSymbolicName());
                    CopyHelpers.copyMetadata(entry.getSymbolicName(), filesInput, filesOutput);

                    //set virtual path
                    String virtualPath = VirtualPathHelpers.getVirtualPath(filesOutput, entry.getSymbolicName());
                    if (StringUtils.isEmpty(virtualPath)) {
                        virtualPath = entry.getSymbolicName();
                    }
                    VirtualPathHelpers.setVirtualPath(filesOutput, entry.getSymbolicName(), virtualPath.toLowerCase().endsWith(".gz") ? virtualPath.substring(0, virtualPath.lastIndexOf('.')) : virtualPath);

                    filesOutput.updateExistingFileURI(entry.getSymbolicName(), outputFile.toURI().toASCIIString());
                } catch (IOException ex) {
                    if (skipOnError) {
                        LOG.warn("Skipping file: '{}' because of error.", entry.toString(), ex);
                    } else {
                        throw ex;
                    }
                }
            }
        } catch (DataUnitException | IOException ex) {
            throw ContextUtils.dpuException(ctx, ex, "Gunzipper.executeInner.exception");
        }
    }
}
