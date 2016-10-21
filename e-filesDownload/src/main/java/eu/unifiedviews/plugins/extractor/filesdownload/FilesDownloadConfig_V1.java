package eu.unifiedviews.plugins.extractor.filesdownload;

import eu.unifiedviews.helpers.dpu.config.VersionedConfig;
import eu.unifiedviews.helpers.dpu.ontology.EntityDescription;

import java.util.LinkedList;
import java.util.List;

@EntityDescription.Entity(type = FilesDownloadVocabulary.STR_CONFIG_CLASS)
public class FilesDownloadConfig_V1 implements VersionedConfig<FilesDownloadConfig_V1> {

    @EntityDescription.Property(uri = FilesDownloadVocabulary.STR_CONFIG_HAS_FILE)
    private List<VfsFile> vfsFiles = new LinkedList<>();

    private boolean softFail = false;
    
    private int defaultTimeout = 20000;
    
    private boolean ignoreTlsErrors = false;

    private boolean checkForDuplicatedInputFiles = false;

    public List<VfsFile> getVfsFiles() {
        return vfsFiles;
    }

    public void setVfsFiles(List<VfsFile> vfsFiles) {
        this.vfsFiles = vfsFiles;
    }

    public boolean isSoftFail() {
        return softFail;
    }

    public void setSoftFail(boolean softFail) {
        this.softFail = softFail;
    }

    public int getDefaultTimeout() {
        return defaultTimeout;
    }

    public void setDefaultTimeout(int defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }
    
    public boolean isIgnoreTlsErrors() {
        return ignoreTlsErrors;
    }

    public void setIgnoreTlsErrors(boolean ignoreTlsErrors) {
        this.ignoreTlsErrors = ignoreTlsErrors;
    }

    public boolean isCheckForDuplicatedInputFiles() {
        return checkForDuplicatedInputFiles;
    }

    public void setCheckForDuplicatedInputFiles(boolean checkForDuplicatedInputFiles) {
        this.checkForDuplicatedInputFiles = checkForDuplicatedInputFiles;
    }

    @Override
    public FilesDownloadConfig_V1 toNextVersion() {
        // This is just a trick to enable use history with non-linear history, ie. many to one history.
        return this;
    }

}
