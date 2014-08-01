package eu.unifiedviews.plugins.transformer.zipper;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.copyhelper.CopyHelpers;
import eu.unifiedviews.helpers.dataunit.virtualpathhelper.VirtualPathHelpers;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import eu.unifiedviews.helpers.dpu.config.ConfigDialogProvider;
import eu.unifiedviews.helpers.dpu.config.ConfigurableBase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Škoda Petr
 */
@DPU.AsTransformer
public class Zipper extends ConfigurableBase<ZipperConfiguration> implements ConfigDialogProvider<ZipperConfiguration> {

    private static final Logger LOG = LoggerFactory.getLogger(Zipper.class);

    @DataUnit.AsInput(name = "input")
    public FilesDataUnit inFilesData;

    @DataUnit.AsOutput(name = "output")
    public WritableFilesDataUnit outFilesData;

    private DPUContext context;

    /**
     * Is set to true if the symbolicName is used as a path/file name.
     * The purpose is to secure that the warning log message will be logged
     * only once.
     */
    private boolean symbolicNameUsed = false;

    public Zipper() {
        super(ZipperConfiguration.class);
    }

    @Override
    public AbstractConfigDialog<ZipperConfiguration> getConfigurationDialog() {
        return new ZipperDialog();
    }

    @Override
    public void execute(DPUContext context) throws DPUException {
        this.context = context;
        final FilesDataUnit.Iteration filesIteration;
        try {
            filesIteration = inFilesData.getIteration();
        } catch (DataUnitException ex) {
            context.sendMessage(DPUContext.MessageType.ERROR, "DPU Failed", "Can't get file iterator.", ex);
            return;
        }
        //
        // Prepare zip file 
        //
        final String zipSymbolicName;
        final String zipFileUri;
        final File zipFile;
        try {
            zipSymbolicName = config.getZipFile();
            zipFileUri = outFilesData.addNewFile(zipSymbolicName);
            zipFile = new File(java.net.URI.create(zipFileUri));
            // add metadata 
            VirtualPathHelpers.setVirtualPath(outFilesData, zipSymbolicName, config.getZipFile());
            //
            // Create zip file
            //        
            zipFiles(zipFile, zipSymbolicName, filesIteration);
        } catch (DataUnitException ex) {
            throw new DPUException(ex);
        } finally {
            try {
                filesIteration.close();
            } catch (DataUnitException ex) {
                throw new DPUException(ex);
            }
        }
    }

    /**
     * Pack files in given iterator into zip file and add metadata.
     * 
     * @param zipFile
     * @param zipSymbolicName
     * @param filesIteration
     */
    private void zipFiles(File zipFile, String zipSymbolicName, FilesDataUnit.Iteration filesIteration) {
        final byte[] buffer = new byte[8196];

        // used to publish the error mesage only for the first time
        boolean firstFailure = true;

        try (FileOutputStream fos = new FileOutputStream(zipFile); ZipOutputStream zos = new ZipOutputStream(fos)) {
            //
            // Itarate over files and zip them
            //
            while (!context.canceled() && filesIteration.hasNext()) {
                final FilesDataUnit.Entry entry = filesIteration.next();
                LOG.debug("Adding file: {}", entry.getSymbolicName());
                if (!addZipEntry(zos, buffer, entry)) {
                    if (firstFailure) {
                        context.sendMessage(DPUContext.MessageType.ERROR, "Faild to zip all files");
                    }
                    firstFailure = false;
                } else {
                    // add metadata
                    CopyHelpers.copyMetadata(entry.getSymbolicName(), inFilesData, outFilesData);
                }
            }
        } catch (IOException | DataUnitException ex) {
            context.sendMessage(DPUContext.MessageType.ERROR, "Failed to create zip file.", "", ex);
        }

    }

    /**
     * Add single file into stream as zip entry.
     * 
     * @param zos
     * @param buffer
     * @param entry
     * @return True if file has been added.
     * @throws DataUnitException
     */
    private boolean addZipEntry(ZipOutputStream zos, byte[] buffer, FilesDataUnit.Entry entry) throws DataUnitException {

        String virtualPath = VirtualPathHelpers.getVirtualPath(inFilesData, entry.getSymbolicName());
        if (virtualPath == null) {
            // use symbolicv name
            virtualPath = entry.getSymbolicName();
            if (!symbolicNameUsed) {
                // first usage
                LOG.warn("Not all input files use VirtualPath, symbolic name is used instead.");
            }
            symbolicNameUsed = true;
        }

        final File sourceFile = new File(java.net.URI.create(entry.getFileURIString()));
        //
        // Do the action .. 
        //
        try (FileInputStream in = new FileInputStream(sourceFile)) {
            final ZipEntry ze = new ZipEntry(virtualPath);
            zos.putNextEntry(ze);
            //
            // Copy data
            //
            int len;
            while ((len = in.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
        } catch (Exception ex) {
            LOG.error("Failed to add file: {}", entry.getSymbolicName(), ex);
            return false;
        }
        return true;
    }

}
