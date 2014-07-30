package eu.unifiedviews.plugins.transformer.unzipper;

import java.io.File;
import java.nio.file.Path;
import java.util.Iterator;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.copyhelper.CopyHelpers;
import eu.unifiedviews.helpers.dataunit.metadata.MetadataHelper;
import eu.unifiedviews.helpers.dataunit.virtualpathhelper.VirtualPathHelpers;

@DPU.AsTransformer
public class Main implements DPU {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    @DataUnit.AsInput(name = "input")
    public FilesDataUnit inFilesData;

    @DataUnit.AsOutput(name = "output")
    public WritableFilesDataUnit outFilesData;

    private DPUContext context;

    public Main() {

    }

    @Override
    public void execute(DPUContext context) throws DPUException {
        this.context = context;

        final FilesDataUnit.Iteration filesIteration;
        try {
            filesIteration = inFilesData.getIteration();
        } catch (DataUnitException ex) {
            context.sendMessage(DPUContext.MessageType.ERROR,
                    "DPU Failed", "Can't get file iterator.", ex);
            return;
        }

        final File baseTargetDirectory;
        try {
            baseTargetDirectory = new File(
                    java.net.URI.create(outFilesData.getBaseFileURIString()));
        } catch (DataUnitException ex) {
            context.sendMessage(DPUContext.MessageType.ERROR,
                    "DPU Failed", "Can't get base output directory.", ex);
            return;
        }

        try {
            while (!context.canceled() && filesIteration.hasNext()) {
                FilesDataUnit.Entry entry = filesIteration.next();
                //
                // Prepare source/target file/directory
                //                
                final File sourceFile = new File(java.net.URI.create(
                        entry.getFileURIString()));

                final String zipRelativePath = VirtualPathHelpers.getVirtualPath(inFilesData, entry.getSymbolicName());
                if (zipRelativePath == null) {
                    context.sendMessage(DPUContext.MessageType.WARNING,
                            "No virtual path set for: "
                            + entry.getSymbolicName()
                            + ". File is ignored.");
                    continue;
                }
                // TODO Use szmbolic name

                final File targetDirectory = new File(baseTargetDirectory, zipRelativePath);
                //
                // Unzip
                //
                if (!unzip(sourceFile, targetDirectory)) {
                    // failure
                    break;
                }
                //
                // Scan for new files and add them
                //
                scanDirectory(targetDirectory, entry.getSymbolicName());
                //
                // Copy metadta                
                //
                CopyHelpers.copyMetadata(entry.getSymbolicName(), inFilesData,
                        outFilesData);
            }

// TODO Remove
MetadataHelper.dump(outFilesData);

        } catch (DataUnitException ex) {
            context.sendMessage(DPUContext.MessageType.ERROR,
                    "Problem with data unit.", "", ex);
        } finally {
            try {
                filesIteration.close();
            } catch (DataUnitException ex) {
                LOG.warn("Error in close.", ex);
            }
        }
    }

    private void scanDirectory(File directory, String sourceSymbolicName)
            throws DataUnitException {
        final Path directoryPath = directory.toPath();
        final Iterator<File> iter = FileUtils.iterateFiles(
                directory, null, true);
        while (iter.hasNext()) {
            final File newFile = iter.next();
            final String relativePath
                    = directoryPath.relativize(newFile.toPath()).toString();
            final String newSymbolicName = outFilesData.getBaseFileURIString()
                    + relativePath;
            // add file
            outFilesData.addExistingFile(newSymbolicName, newFile.toURI().toString());
            //
            // add metadata
            //
            VirtualPathHelpers.setVirtualPath(outFilesData, newSymbolicName, relativePath);
            MetadataHelper.set(outFilesData, newSymbolicName,
                    Ontology.PREDICATE_EXTRACTED_FROM, sourceSymbolicName);
        }
    }

    /**
     * Unzip given file into given directory.
     *
     * @param zipFile
     * @param targetDirectory
     * @return
     */
    private boolean unzip(File zipFile, File targetDirectory) {
        try {
            final ZipFile zip = new ZipFile(zipFile);
            if (zip.isEncrypted()) {
                context.sendMessage(DPUContext.MessageType.ERROR,
                        "Extraction failed.", "Zip file is encrypted.");
                return false;
            }
            zip.extractAll(targetDirectory.toString());
        } catch (ZipException ex) {
            context.sendMessage(DPUContext.MessageType.ERROR,
                    "Extraction failed.", "", ex);
            return false;
        }
        return true;
    }

}
