package eu.unifiedviews.plugins.loader.filestolocalfs;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.CopyOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Date;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.nio.file.ExtendedCopyOption;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.MetadataDataUnit;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.copyhelper.CopyHelper;
import eu.unifiedviews.helpers.dataunit.copyhelper.CopyHelpers;
import eu.unifiedviews.helpers.dataunit.virtualpathhelper.VirtualPathHelper;
import eu.unifiedviews.helpers.dataunit.virtualpathhelper.VirtualPathHelpers;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import eu.unifiedviews.helpers.dpu.config.ConfigDialogProvider;
import eu.unifiedviews.helpers.dpu.config.ConfigurableBase;

@DPU.AsLoader
public class FilesToLocalFS extends
        ConfigurableBase<FilesToLocalFSConfig_V1> implements
        ConfigDialogProvider<FilesToLocalFSConfig_V1> {

    public static final String PREDICATE_HAS_DISTRIBUTION = "http://comsode.eu/hasDistribution";

    private static final Logger LOG = LoggerFactory
            .getLogger(FilesToLocalFS.class);

    @DataUnit.AsInput(name = "filesInput")
    public FilesDataUnit filesInput;

    @DataUnit.AsOutput(name = "filesOutput")
    public WritableFilesDataUnit filesOutput;

    public FilesToLocalFS() {
        super(FilesToLocalFSConfig_V1.class);
    }

    @Override
    public void execute(DPUContext dpuContext) throws DPUException,
            InterruptedException {
        String shortMessage = this.getClass().getSimpleName() + " starting.";
        String longMessage = String.valueOf(config);
        dpuContext.sendMessage(DPUContext.MessageType.INFO, shortMessage, longMessage);

        FilesDataUnit.Iteration filesIteration;
        try {
            filesIteration = filesInput.getIteration();
        } catch (DataUnitException ex) {
            throw new DPUException("Could not obtain filesInput", ex);
        }
        File destinationDirFile = new File(config.getDestination());
        destinationDirFile.mkdirs();
        String destinationAbsolutePath = destinationDirFile
                .getAbsolutePath();

        boolean moveFiles = config.isMoveFiles();
        ArrayList<CopyOption> copyOptions = new ArrayList<>(1);
        if (config.isReplaceExisting()) {
            copyOptions.add(StandardCopyOption.REPLACE_EXISTING);
            copyOptions.add(ExtendedCopyOption.INTERRUPTIBLE);
        }
        CopyOption[] copyOptionsArray = copyOptions.toArray(new CopyOption[copyOptions.size()]);

        long index = 0L;
        boolean shouldContinue = !dpuContext.canceled();
        VirtualPathHelper inputVirtualPathHelper = VirtualPathHelpers.create(filesInput);
        CopyHelper copyHelper = CopyHelpers.create(filesInput, filesOutput);
        RepositoryConnection outputMetadataConnection = null;
        try {
            outputMetadataConnection = filesOutput.getConnection();
            ValueFactory valueFactory = outputMetadataConnection.getValueFactory();
            while (shouldContinue && filesIteration.hasNext()) {
                index++;

                FilesDataUnit.Entry entry;
                entry = filesIteration.next();

                try {
                    Path inputPath = new File(URI.create(entry.getFileURIString())).toPath();
                    String outputRelativePath = inputVirtualPathHelper.getVirtualPath(entry.getSymbolicName());
                    if (outputRelativePath == null || outputRelativePath.isEmpty()) {
                        outputRelativePath = entry.getSymbolicName();
                    }
                    File outputFile = new File(destinationAbsolutePath + File.separator
                            + outputRelativePath);

                    Path outputPath = outputFile.toPath();

                    Date start = new Date();
                    if (dpuContext.isDebugging()) {
                        LOG.debug("Processing {} file {}", appendNumber(index), entry);
                    }
                    if (moveFiles) {
                        java.nio.file.Files.move(inputPath, outputPath, copyOptionsArray);
                    } else {
                        java.nio.file.Files.copy(inputPath, outputPath, copyOptionsArray);
                    }
                    java.nio.file.Files.setPosixFilePermissions(outputPath, PosixFilePermissions.fromString("rw-r--r--"));
                    copyHelper.copyMetadata(entry.getSymbolicName());

                    Resource symbolicNameResource = outputMetadataConnection.getStatements(
                            null, valueFactory.createURI(MetadataDataUnit.PREDICATE_SYMBOLIC_NAME), valueFactory.createLiteral(entry.getSymbolicName()),
                            false, filesOutput.getMetadataGraphnames().toArray(new URIImpl[0])).next().getSubject();

                    BNode distributionRoot = valueFactory.createBNode();
                    outputMetadataConnection.add(valueFactory.createStatement(
                            symbolicNameResource, valueFactory.createURI(PREDICATE_HAS_DISTRIBUTION), distributionRoot),
                            filesOutput.getMetadataWriteGraphname());
                    outputMetadataConnection.add(valueFactory.createStatement(
                            distributionRoot, RDF.TYPE, DCAT.DISTRIBUTION),
                            filesOutput.getMetadataWriteGraphname());

                    outputMetadataConnection.add(valueFactory.createStatement(
                            distributionRoot, DCTERMS.MODIFIED, valueFactory.createLiteral(new Date())),
                            filesOutput.getMetadataWriteGraphname());

                    outputMetadataConnection.add(valueFactory.createStatement(
                            distributionRoot, DCAT.DOWNLOAD_URL, valueFactory.createURI(outputFile.toURI().toASCIIString())),
                            filesOutput.getMetadataWriteGraphname());

                    if (dpuContext.isDebugging()) {
                        LOG.debug("Processed {} file in {}s", appendNumber(index), (System.currentTimeMillis() - start.getTime()) / 1000);
                    }
                } catch (IOException | RepositoryException ex) {
                    if (config.isSkipOnError()) {
                        LOG.warn("Error processing {} file {}", appendNumber(index), String.valueOf(entry), ex);
                    } else {
                        throw new DPUException("Error processing " + appendNumber(index) + " file " + String.valueOf(entry), ex);
                    }
                }

                shouldContinue = !dpuContext.canceled();
            }
        } catch (DataUnitException ex) {
            throw new DPUException("Error iterating filesInput.", ex);
        } finally {
            if (outputMetadataConnection != null) {
                try {
                    outputMetadataConnection.close();
                } catch (RepositoryException ex) {
                    LOG.warn("Error in close", ex);
                }
            }
            try {
                filesIteration.close();
            } catch (DataUnitException ex) {
                LOG.warn("Error closing filesInput", ex);
            }
            try {
                inputVirtualPathHelper.close();
            } catch (DataUnitException ex) {
                LOG.warn("Error in close", ex);
            }
            try {
                copyHelper.close();
            } catch (DataUnitException ex) {
                LOG.warn("Error in close", ex);
            }
        }
    }

    @Override
    public AbstractConfigDialog<FilesToLocalFSConfig_V1> getConfigurationDialog() {
        return new FilesToLocalFSVaadinDialog();
    }

    public static String appendNumber(long number) {
        String value = String.valueOf(number);
        if (value.length() > 1) {
            // Check for special case: 11 - 13 are all "th".
            // So if the second to last digit is 1, it is "th".
            char secondToLastDigit = value.charAt(value.length() - 2);
            if (secondToLastDigit == '1') {
                return value + "th";
            }
        }
        char lastDigit = value.charAt(value.length() - 1);
        switch (lastDigit) {
            case '1':
                return value + "st";
            case '2':
                return value + "nd";
            case '3':
                return value + "rd";
            default:
                return value + "th";
        }
    }
}
