package eu.unifiedviews.plugins.extractor.filesdownload;

import java.io.IOException;
import java.net.URI;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
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

/**
 * TODO Add support for caching.
 * TODO Add support for soft failure.
 */
@DPU.AsExtractor
public class FilesDownload extends AbstractDpu<FilesDownloadConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(FilesDownload.class);

    public static final String SUPPORTED_PROTOCOLS = "dpu.uv-e-filesDownload.supported.protocols";

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
        }

        final NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMaximumFractionDigits(0);

        try {
            standardFileSystemManager.setFilesCache(new NullFilesCache());
            standardFileSystemManager.init();
        } catch (FileSystemException ex) {
            throw ContextUtils.dpuException(ctx, ex, "FilesDownload.execute.exception");
        }
        // For each file in cofiguration.
        int vfsProgress = 0;
        for (final VfsFile vfsFile : config.getVfsFiles()) {
            LOG.info("Processing VFS entry: {}/{}", ++vfsProgress, config.getVfsFiles().size());
            if (ctx.canceled()) {
                throw ContextUtils.dpuExceptionCancelled(ctx);
            }

            if (!checkURIProtocolSupported(vfsFile.getUri())) {
                ContextUtils.sendWarn(this.ctx, "FilesDownload.protocol.not.supported", "FilesDownload.protocol.not.supported.long",
                        vfsFile.getUri(),
                        getSupportedProtocols());
                continue;
            }

            // If user name is not blank, then we prepare for autentification.
            if (StringUtils.isNotBlank(vfsFile.getUsername())) {
                final StaticUserAuthenticator staticUserAuthenticator;
                try {
                    staticUserAuthenticator = new StaticUserAuthenticator(
                            URI.create(vfsFile.getUri()).getHost(),
                            vfsFile.getUsername(),
                            vfsFile.getPassword());
                } catch (Exception ex) {
                    throw ContextUtils.dpuException(ctx, ex, "FilesDownload.execute.exception");
                }
                try {
                    DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(fileSystemOptions,
                            staticUserAuthenticator);
                } catch (FileSystemException ex) {
                    throw ContextUtils.dpuException(ctx, ex, "FilesDownload.execute.exception");
                }
            }
            // One path can be resolved in multiple files (like directory.
            final FileObject[] fileObjects;
            try {
                fileObjects = standardFileSystemManager.resolveFile(vfsFile.getUri(), fileSystemOptions)
                        .findFiles(new AllFileSelector());
            } catch (FileSystemException ex) {
                throw ContextUtils.dpuException(ctx, ex, "FilesDownload.execute.exception");
            }

            if (fileObjects == null) {
                // Skip null files but add a log.                    
                LOG.warn("Skipping file: '{}' as it resolves on null value.", vfsFile.getUri());
                continue;
            }

            // We download each file.
            int fileProgress = 0;
            for (FileObject fileObject : fileObjects) {
                fileProgress++;
                if (fileProgress % (int) Math.ceil(fileObjects.length / 10.0) == 0) {
                    LOG.info("Downloading progress: {}%", numberFormat.format((double) fileProgress / (double) fileObjects.length * 100));
                }
                final boolean isFile;
                try {
                    isFile = FileType.FILE.equals(fileObject.getType());
                } catch (FileSystemException ex) {
                    throw ContextUtils.dpuException(ctx, ex, "FilesDownload.execute.exception");
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
                        throw ContextUtils.dpuException(ctx, ex, "FilesDownload.execute.exception");
                    }
                }
            }
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
