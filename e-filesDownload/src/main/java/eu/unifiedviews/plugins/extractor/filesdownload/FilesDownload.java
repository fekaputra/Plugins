package eu.unifiedviews.plugins.extractor.filesdownload;

import cz.cuni.mff.xrg.uv.extractor.filesfromlocal.FilesFromLocalConfig_V1;
import cz.cuni.mff.xrg.uv.extractor.httpdownload.HttpDownloadConfig_V2;
import cz.cuni.mff.xrg.uv.extractor.scp.FilesFromScpConfig_V1;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.files.FilesDataUnitUtils;
import eu.unifiedviews.helpers.dataunit.resource.Resource;
import eu.unifiedviews.helpers.dataunit.resource.ResourceHelpers;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultToleranceUtils;
import eu.unifiedviews.helpers.dpu.extension.rdf.RdfConfiguration;
import eu.unifiedviews.plugins.extractor.httpdownload.HttpDownloadConfig_V1;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.cache.NullFilesCache;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.ftps.FtpsFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.text.NumberFormat;
import java.util.*;

/**
 * TODO Add support for caching.
 * TODO Add support for soft failure.
 */
@DPU.AsExtractor
public class FilesDownload extends AbstractDpu<FilesDownloadConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(FilesDownload.class);

    public static final String SUPPORTED_PROTOCOLS = "dpu.uv-e-filesDownload.allowed.protocols";

    @RdfConfiguration.ContainsConfiguration
    @DataUnit.AsInput(name = "config", optional = true)
    public RDFDataUnit rdfConfiguration;

    @DataUnit.AsOutput(name = "output")
    public WritableFilesDataUnit filesOutput;

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    @ExtensionInitializer.Init
    public RdfConfiguration _rdfConfiguration;

    @ExtensionInitializer.Init
    public MultipleConfigurationUpdate _configurationUpdate;

    public FilesDownload() {
        super(FilesDownloadVaadinDialog.class,
                ConfigHistory.history(FilesDownloadConfig_V1.class)
                        .alternative(HttpDownloadConfig_V1.class)
                        .alternative(FilesFromScpConfig_V1.class)
                        .alternative(FilesFromLocalConfig_V1.class)
                        .alternative(HttpDownloadConfig_V2.class)
                        .addCurrent(FilesDownloadConfig_V1.class));
    }

    @Override
    protected void innerExecute() throws DPUException {
        final StandardFileSystemManager standardFileSystemManager = new StandardFileSystemManager();
        standardFileSystemManager.setClassLoader(standardFileSystemManager.getClass().getClassLoader());

        final FileSystemOptions fileSystemOptions = new FileSystemOptions();
        FtpFileSystemConfigBuilder.getInstance().setDataTimeout(fileSystemOptions, config.getDefaultTimeout());
        FtpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(fileSystemOptions, false);
        FtpsFileSystemConfigBuilder.getInstance().setDataTimeout(fileSystemOptions, config.getDefaultTimeout());
        FtpsFileSystemConfigBuilder.getInstance().setUserDirIsRoot(fileSystemOptions, false);
        HttpConnectionManagerParams.getDefaultParams().setParameter(HttpConnectionManagerParams.CONNECTION_TIMEOUT, config.getDefaultTimeout());
        HttpConnectionManagerParams.getDefaultParams().setParameter(HttpConnectionManagerParams.SO_TIMEOUT, config.getDefaultTimeout());
        SftpFileSystemConfigBuilder.getInstance().setTimeout(fileSystemOptions, config.getDefaultTimeout());
        SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(fileSystemOptions, false);

        if (config.isIgnoreTlsErrors()) {
            Protocol.registerProtocol("https", new Protocol("https", (ProtocolSocketFactory) new EasySSL(), 443));
        } else {
            Protocol.registerProtocol("https", new Protocol("https", (ProtocolSocketFactory) new TLS12(), 443));
        }

        final NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMaximumFractionDigits(0);

        try {
            standardFileSystemManager.setFilesCache(new NullFilesCache());
            standardFileSystemManager.init();
        } catch (FileSystemException ex) {
            throw ContextUtils.dpuException(ctx, ex, "FilesDownload.execute.exception");
        }
        // For each file in configuration.
        int totalNumberOfVfsFiles = 0;
        int totalNumberOfCorrectlyProcessedVfsFiles = 0;
        int totalNumberOfFiles = 0;
        int totalNumberOfCorrectlyProcessedFiles = 0;
        Set<String> vfsUrisProcessed = new HashSet<>();
        for (final VfsFile vfsFile : config.getVfsFiles()) {
            LOG.info("Processing entry: {}/{}", ++totalNumberOfVfsFiles, config.getVfsFiles().size());
            LOG.info("Entry name: {}, uri: {}", vfsFile.getFileName(), vfsFile.getUri());
            if (ctx.canceled()) {
                throw ContextUtils.dpuExceptionCancelled(ctx);
            }

            if (!checkURIProtocolSupported(vfsFile.getUri())) {
                ContextUtils.sendWarn(this.ctx, "FilesDownload.protocol.not.supported", "FilesDownload.protocol.not.supported.long",
                        vfsFile.getUri(),
                        getSupportedProtocols());
                continue;
            }

            // If user name is not blank, then we prepare for authentication.
            if (StringUtils.isNotBlank(vfsFile.getUsername())) {
                final StaticUserAuthenticator staticUserAuthenticator;
                try {
                    staticUserAuthenticator = new StaticUserAuthenticator(
                            URI.create(vfsFile.getUri()).getHost(),
                            vfsFile.getUsername(),
                            vfsFile.getPassword());

                    DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(fileSystemOptions, staticUserAuthenticator);
                } catch (Exception ex) {

                    if (config.isSoftFail()) {
                        ContextUtils.sendWarn(ctx, "Skipping entry " + vfsFile.getUri() + "as there was a problem with authentication", ex, "Skipping entry " + vfsFile.getUri() + "as there was a problem with authentication");
                        continue;
                    }

                    throw ContextUtils.dpuException(ctx, ex, "FilesDownload.execute.auth.exception");
                }
            }

            if (config.isCheckForDuplicatedInputFiles()){
                //check whether we are not trying to process certain URI more times, in that case just skip processing and log warning
                //this may happen when we configure fileDownloader dynamically
                if (vfsUrisProcessed.contains(vfsFile.getUri())) {
                    //the uri was already processed
                    ContextUtils.sendInfo(ctx, "Skipping entry " + vfsFile.getUri() + "as it was already processed before", "Skipping entry " + vfsFile.getUri() + "as it was already processed before. Possible cause: This DPU was configured dynamically with more entries pointing to the same URL");
                    continue;
                }
            }

            //the entry is not there, just add it
            vfsUrisProcessed.add(vfsFile.getUri());



            // One path can be resolved in multiple files (like directory.
            final FileObject[] fileObjects;
            try {
                fileObjects = standardFileSystemManager.resolveFile(vfsFile.getUri(), fileSystemOptions)
                        .findFiles(new AllFileSelector());
            } catch (FileSystemException ex) {

                if (config.isSoftFail()) {
                    ContextUtils.sendWarn(ctx, "Skipping entry " + vfsFile.getUri() + "as there was a problem processing entry", ex, "Skipping entry " + vfsFile.getUri() + "as there was a problem processing entry");
                    continue;
                }
                else {
                    throw ContextUtils.dpuException(ctx, ex, "FilesDownload.execute.exception");
                }
            }

            if (fileObjects == null) {
                // null files

                if (config.isSoftFail()) {
                    ContextUtils.sendWarn(ctx, "Skipping entry " + vfsFile.getUri() + " as it does not resolve to any valid file", "Skipping entry " + vfsFile.getUri() + " as it does not resolve to any valid file");
                    continue;
                }
                else {
                    throw ContextUtils.dpuException(ctx, "FilesDownload.execute.exception.nofile", vfsFile.getUri());
                }
            }

            LOG.info("Downloadable entry {} resolves to {} files", vfsFile.getUri(), fileObjects.length);

            // We download each file.
            int fileProgress = 0;
            boolean errorInFileForVfsEntry = false;
            for (FileObject fileObject : fileObjects) {
                fileProgress++;
                totalNumberOfFiles++;
                if (fileProgress % (int) Math.ceil(fileObjects.length / 10.0) == 0) {
                    LOG.info("Downloading progress (percentage of files for the given entry): {}%", numberFormat.format((double) fileProgress / (double) fileObjects.length * 100));
                }
                final boolean isFile;
                try {
                    isFile = FileType.FILE.equals(fileObject.getType());
                } catch (FileSystemException ex) {

                    if (config.isSoftFail()) {
                        ContextUtils.sendWarn(ctx, "Skipping entry " + vfsFile.getUri() + "as there was a problem processing entry", ex, "Skipping entry " + vfsFile.getUri() + "as there was a problem processing entry");
                        errorInFileForVfsEntry = true;
                        continue;
                    }
                    else {
                        throw ContextUtils.dpuException(ctx, ex, "FilesDownload.execute.exception");
                    }
                }
                if (isFile) {
                    // Get file name.
                    final String fileName;
                    if (StringUtils.isNotBlank(vfsFile.getFileName())) {
                        fileName = vfsFile.getFileName();
                    } else {
                        //in this case file name is not available from config dialog
                        fileName = DigestUtils.sha1Hex(vfsFile.getUri());
                    }
                    LOG.debug("Filename is: {}", fileName);
                    // Prepare new output file record.
                    final FilesDataUnit.Entry destinationFile = faultTolerance.execute(new FaultTolerance.ActionReturn<FilesDataUnit.Entry>() {

                        @Override
                        public FilesDataUnit.Entry action() throws Exception {
                            return FilesDataUnitUtils.createFile(filesOutput, fileName);
                        }
                    });
                    // Add some metadata, TODO: Improve this code!
                    faultTolerance.execute(new FaultTolerance.Action() {

                        @Override
                        public void action() throws Exception {
                            final Resource resource = ResourceHelpers.getResource(filesOutput, fileName);
                            final Date now = new Date();
                            resource.setCreated(now);
                            resource.setLast_modified(now);
                            ResourceHelpers.setResource(filesOutput, fileName, resource);
                        }
                    }, "FilesDownload.execute.exception");
                    // Copy file.
                    try {
                        FileUtils.copyInputStreamToFile(fileObject.getContent().getInputStream(),
                                FaultToleranceUtils.asFile(faultTolerance, destinationFile));
                    } catch (IOException ex) {
                        if (config.isSoftFail()) {
                            ContextUtils.sendWarn(ctx, "Skipping entry " + vfsFile.getUri() + "as there was a problem processing entry", ex, "Skipping entry " + vfsFile.getUri() + "as there was a problem processing entry");
                            errorInFileForVfsEntry = true;
                            continue;
                        }
                        else {
                            throw ContextUtils.dpuException(ctx, ex, "FilesDownload.execute.exception");
                        }
                    }
                    totalNumberOfCorrectlyProcessedFiles++;
                }
            }
            if (!errorInFileForVfsEntry) totalNumberOfCorrectlyProcessedVfsFiles++;
        }
        ContextUtils.sendInfo(ctx, "Correctly processed input entries: " + totalNumberOfCorrectlyProcessedVfsFiles+ " / " + totalNumberOfVfsFiles, "Ratio of correctly processed input entries and all entries found.");
        ContextUtils.sendInfo(ctx, "Correctly downloaded files : " + totalNumberOfCorrectlyProcessedFiles + " / " + totalNumberOfFiles, "Ratio of correctly downloaded files and all detected files (for correctly processed entries). If there are less detected files than number of entries in the configuration of the DPU, please look for warning messages, maybe certain entry does not resolve to any file. ");
    }

    private boolean checkURIProtocolSupported(String uri) {
        Map<String, String> environment = this.ctx.getExecMasterContext().getDpuContext().getEnvironment();
        String supportedProtocols = environment.get(SUPPORTED_PROTOCOLS);
        if (StringUtils.isEmpty(supportedProtocols)) {
            return true;
        }

        final String scheme = UriParser.extractScheme(uri);
        String[] supportedSchemes = supportedProtocols.trim().split(",");
        Set<String> supportedSet = new HashSet<>();
        for (String s : supportedSchemes) {
            supportedSet.add(s);
        }

        if (StringUtils.isEmpty(scheme) && !supportedSet.contains("file")) {
            return false;
        }

        return supportedSet.contains(scheme);
    }

    private List<String> getSupportedProtocols() {
        Map<String, String> environment = this.ctx.getExecMasterContext().getDpuContext().getEnvironment();
        String supportedProtocols = environment.get(FilesDownload.SUPPORTED_PROTOCOLS);
        if (StringUtils.isEmpty(supportedProtocols)) {
            return null;
        }

        String[] supportedProtocolsArray = supportedProtocols.trim().split(",");
        List<String> protocols = new ArrayList<>();
        for (String protocol : supportedProtocolsArray) {
            protocols.add(protocol);
        }

        return protocols;
    }

}
