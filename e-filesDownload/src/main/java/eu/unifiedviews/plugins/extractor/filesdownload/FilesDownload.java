package eu.unifiedviews.plugins.extractor.filesdownload;

import cz.cuni.mff.xrg.uv.extractor.filesfromlocal.FilesFromLocalConfig_V1;
import cz.cuni.mff.xrg.uv.extractor.httpdownload.HttpDownloadConfig_V2;
import cz.cuni.mff.xrg.uv.extractor.scp.FilesFromScpConfig_V1;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.files.FilesHelper;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.*;


@DPU.AsExtractor
public class FilesDownload extends AbstractDpu<FilesDownloadConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(FilesDownload.class);

    public static final String SUPPORTED_PROTOCOLS = "dpu.uv-e-filesDownload.allowed.protocols";

    public static final String EXEC_ID_MACRO = "%7B%7BexecId%7D%7D";

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
        } catch (org.apache.commons.vfs2.FileSystemException ex) {
            throw ContextUtils.dpuException(ctx, ex, "FilesDownload.execute.exception");
        }
        // For each file in configuration.
        int totalNumberOfVfsFiles = 0;
        int totalNumberOfCorrectlyProcessedVfsFiles = 0;
        int totalNumberOfFiles = 0;
        int totalNumberOfCorrectlyProcessedFiles = 0;
        Set<String> vfsUrisProcessed = new HashSet<>();
        for (final VfsFile vfsFile : config.getVfsFiles()) {
            if (ctx.canceled()) {
                throw ContextUtils.dpuExceptionCancelled(ctx);
            }

            LOG.info("Processing entry: {}/{}", ++totalNumberOfVfsFiles, config.getVfsFiles().size());
            String fileUriString = vfsFile.getUri();
            LOG.info("Entry name: {}, uri: {}", vfsFile.getFileName(), fileUriString);

            //replace {{execId}} in the file URI (if such macro is used)
            if (fileUriString.contains(EXEC_ID_MACRO)) {
                Long pipelineId = ctx.getExecMasterContext().getDpuContext().getPipelineId();
                String pipelineIdString = String.valueOf(pipelineId);
                fileUriString = fileUriString.replace(EXEC_ID_MACRO, pipelineIdString );
                LOG.info("Entry uri after uri replacement: {}", fileUriString);
            }

            if (!checkURIProtocolSupported(fileUriString)) {
                ContextUtils.sendWarn(this.ctx, "FilesDownload.protocol.not.supported", "FilesDownload.protocol.not.supported.long",
                        fileUriString,
                        getSupportedProtocols());
                continue;
            }

            // If user name is not blank, then we prepare for authentication.
            if (StringUtils.isNotBlank(vfsFile.getUsername())) {
                final StaticUserAuthenticator staticUserAuthenticator;
                try {
                    staticUserAuthenticator = new StaticUserAuthenticator(
                            URI.create(fileUriString).getHost(),
                            vfsFile.getUsername(),
                            vfsFile.getPassword());

                    DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(fileSystemOptions, staticUserAuthenticator);
                } catch (Exception ex) {

                    if (config.isSoftFail()) {
                        ContextUtils.sendShortWarn(ctx, "FilesDownload.softfail.auth", ex, fileUriString);
                        continue;
                    }

                    throw ContextUtils.dpuException(ctx, ex, "FilesDownload.execute.auth.exception");
                }
            }
            
            if (config.isCheckForDuplicatedInputFiles()){
                //check whether we are not trying to process certain URI more times, in that case just skip processing and log warning
                //this may happen when we configure fileDownloader dynamically
                if (vfsUrisProcessed.contains(fileUriString)) {
                    //the uri was already processed
                    ContextUtils.sendInfo(ctx, "FilesDownload.softfail.alreadyprocessed", "FilesDownload.softfail.alreadyprocessed.detail", fileUriString);
                    continue;
                }
            }

            //the entry is not there, just add it
            vfsUrisProcessed.add(fileUriString);

            // One path can be resolved in multiple files (like directory.
            final FileObject[] fileObjects;
            try {
                fileObjects = standardFileSystemManager.resolveFile(fileUriString, fileSystemOptions)
                        .findFiles(new AllFileSelector());
            } catch (org.apache.commons.vfs2.FileSystemException ex) {

                if (config.isSoftFail()) {
                    ContextUtils.sendShortWarn(ctx, "FilesDownload.softfail.processingproblem", ex, fileUriString);
                    continue;
                }
                else {
                    throw ContextUtils.dpuException(ctx, ex, "FilesDownload.execute.exception");
                }
            }


            if (fileObjects == null) {
                // null files

                if (config.isSoftFail()) {
                    ContextUtils.sendShortWarn(ctx, "FilesDownload.softfail.nofile", fileUriString);
                    continue;
                }
                else {
                    ContextUtils.sendError(ctx,"FilesDownload.execute.exception.nofile", "FilesDownload.execute.exception.nofile.detail", vfsFile.getFileName(), fileUriString);
                    return;
                }
            }

//            LOG.info("Downloadable entry {} resolves to {} files (in case of folders, this may include the the folder as well)", fileUriString, fileObjects.length);

            //if it resolves to more than one file, it is directory. So in that case, we have to use the suggested filename only as a prefix
            boolean useFileNameAsPrefixOnly = false;
            if (fileObjects.length > 1) {
                if (StringUtils.isNotBlank(vfsFile.getFileName())) {
                    //there is some file name suggested by the user
                    ContextUtils.sendInfo(ctx,"FilesDownload.multiplefiles", "FilesDownload.multiplefiles.detail", fileUriString, vfsFile.getFileName());
                    useFileNameAsPrefixOnly = true;
                }
            }

            // We download each file.
            int fileProgress = 0;
            boolean errorInFileForVfsEntry = false;
            for (FileObject fileObject : fileObjects) {

                if (config.getWaitBetweenCallsMs() > 0) {
                    try {
                        Thread.sleep(config.getWaitBetweenCallsMs());
                    } catch (InterruptedException e) {
                        LOG.warn(e.getLocalizedMessage(),e);
                    }
                }

                boolean isFile = false;
                try {
                    isFile = FileType.FILE.equals(fileObject.getType());
                } catch (org.apache.commons.vfs2.FileSystemException ex) {

                    if (config.isSoftFail()) {
                        ContextUtils.sendShortWarn(ctx, "FilesDownload.softfail.processingproblem", ex, fileUriString);
                        errorInFileForVfsEntry = true;
                        continue;
                    }
                    else {
                        throw ContextUtils.dpuException(ctx, ex, "FilesDownload.execute.exception");
                    }
                }
                if (isFile) {
                    //we process only files, not folders
                    fileProgress++;
                    totalNumberOfFiles++;

                    LOG.info("Processing file with name: {}, original entry: {}", fileObject.getName(), fileUriString.toString());
                    final String symbolicName;
                    final String virtualPath = getVirtualPathForFile(fileUriString, fileObject.getName(), fileObjects.length > 1);
                    if (StringUtils.isNotBlank(vfsFile.getFileName())) {
                        if (useFileNameAsPrefixOnly) {
                            symbolicName = vfsFile.getFileName()+DigestUtils.sha1Hex(fileUriString+fileObject.getName());;
                        }
                        else {
                            symbolicName = vfsFile.getFileName();
                        }
                    } else {
                        //in this case file name is not available from config dialog
                        //combination of vfsFile + fileObject name (as vfs URI may point to a folder, but I need different symbolic name for each file within that folder)
                        symbolicName = DigestUtils.sha1Hex(fileUriString+fileObject.getName());
                    }
                    LOG.info("Symbolic name/actual filename: {}, Virtual path: {}", symbolicName, virtualPath);

                    // Prepare new output file record.

                    FilesDataUnit.Entry destinationFile;
                    try {
                        destinationFile = FilesHelper.createFile(filesOutput, symbolicName, virtualPath);
                    } catch (DataUnitException ex) {
                        throw ContextUtils.dpuException(ctx, ex, "FilesDownload.execute.exception");
                    }

                    // Add some metadata, TODO: Improve this code!
                    faultTolerance.execute(new FaultTolerance.Action() {

                        @Override
                        public void action() throws Exception {
                            final Resource resource = ResourceHelpers.getResource(filesOutput, symbolicName);
                            final Date now = new Date();
                            resource.setCreated(now);
                            resource.setLast_modified(now);
                            ResourceHelpers.setResource(filesOutput, symbolicName, resource);
                        }
                    }, "FilesDownload.execute.exception");
                    // Copy file.
                    try {
                        FileUtils.copyInputStreamToFile(fileObject.getContent().getInputStream(),
                                FaultToleranceUtils.asFile(faultTolerance, destinationFile));
                    } catch (IOException ex) {
                        if (config.isSoftFail()) {
                            ContextUtils.sendShortWarn(ctx, "FilesDownload.softfail.processingproblem", ex, fileUriString);
                            errorInFileForVfsEntry = true;
                            continue;
                        }
                        else {
                            throw ContextUtils.dpuException(ctx, ex, "FilesDownload.execute.exception");
                        }
                    }
                    totalNumberOfCorrectlyProcessedFiles++;
                }
                else {
                    LOG.info("File entry {} skipped, because it is not regular file (probably a folder), which was already expanded to a set of files", fileObject.getName());
                }
            }
            if (!errorInFileForVfsEntry) totalNumberOfCorrectlyProcessedVfsFiles++;
        }

        ContextUtils.sendInfo(ctx, "FilesDownload.stats.processedentries", "FilesDownload.stats.processedentries.detail", totalNumberOfCorrectlyProcessedVfsFiles, totalNumberOfVfsFiles);
        ContextUtils.sendInfo(ctx, "FilesDownload.stats.correctlydownloadedfiles", "FilesDownload.stats.correctlydownloadedfiles.detail", totalNumberOfCorrectlyProcessedFiles, totalNumberOfFiles);
    }

    /**
     * Gets virtual path string for the file
     * @param uri URI of the vfs entry (Can be either folder or file directly, as contained in the dialog of the DPU)
     * @param name Name of the actual file (if it was originally folder, now we process just the expanded file!), full path
     * @param isDirectory
     * @return
     */
    private String getVirtualPathForFile(String uri, FileName name, boolean isDirectory) {

        //return relativePath of the file, relative to the initial folder! If there is no initial folder, but the file directly, we can just return the file name!
        if (isDirectory) {

            Path path1 = Paths.get(uri).normalize();
            Path path2 = Paths.get(name.getURI()).normalize();
            Path relativePath = path1.relativize(path2);

            LOG.info("Relative path: {}", relativePath.toString());
            return relativePath.toString();


        }
        else {
            //get just the filename as virtualPath (so e.g. if it is file:///tmp/a.txt then return a.txt)
            return name.getBaseName();

        }



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
