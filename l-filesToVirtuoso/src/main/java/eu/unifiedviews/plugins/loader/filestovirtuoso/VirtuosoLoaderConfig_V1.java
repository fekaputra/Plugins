package eu.unifiedviews.plugins.loader.filestovirtuoso;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.config.VersionedConfig;
import eu.unifiedviews.helpers.dpu.ontology.EntityDescription;

@EntityDescription.Entity(type = "http://unifiedviews.eu/ontology/dpu/filesToVirtuoso/Config")
public class VirtuosoLoaderConfig_V1 implements VersionedConfig<VirtuosoLoaderConfig_V2> {

    private String virtuosoUrl = "";

    private String username = "";

    private String password = "";

    private boolean clearDestinationGraph = false;

    private String loadDirectoryPath = "";

    private boolean includeSubdirectories = true;

    @EntityDescription.Property(uri = "http://unifiedviews.eu/ontology/dpu/filesToVirtuoso/config/fileName")
    private String loadFilePattern = "%";

    @EntityDescription.Property(uri = "http://unifiedviews.eu/ontology/dpu/filesToVirtuoso/config/graphUri")
    private String targetContext = "";

    //    private String targetTempContext = "";

    private long statusUpdateInterval = 60L;

    private int threadCount = 1;

    private boolean skipOnError = false;

    public VirtuosoLoaderConfig_V1() {
    }

    public String getVirtuosoUrl() {
        return virtuosoUrl;
    }

    public void setVirtuosoUrl(String virtuosoUrl) {
        this.virtuosoUrl = virtuosoUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isClearDestinationGraph() {
        return clearDestinationGraph;
    }

    public void setClearDestinationGraph(boolean clearDestinationGraph) {
        this.clearDestinationGraph = clearDestinationGraph;
    }

    public String getLoadDirectoryPath() {
        return loadDirectoryPath;
    }

    public void setLoadDirectoryPath(String loadDirectoryPath) {
        this.loadDirectoryPath = loadDirectoryPath;
    }

    public boolean isIncludeSubdirectories() {
        return includeSubdirectories;
    }

    public void setIncludeSubdirectories(boolean includeSubdirectories) {
        this.includeSubdirectories = includeSubdirectories;
    }

    public String getLoadFilePattern() {
        return loadFilePattern;
    }

    public void setLoadFilePattern(String loadFilePattern) {
        this.loadFilePattern = loadFilePattern;
    }

    public String getTargetContext() {
        return targetContext;
    }

    public void setTargetContext(String targetContext) {
        this.targetContext = targetContext;
    }

    public long getStatusUpdateInterval() {
        return statusUpdateInterval;
    }

    public void setStatusUpdateInterval(long statusUpdateInterval) {
        this.statusUpdateInterval = statusUpdateInterval;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public boolean isSkipOnError() {
        return skipOnError;
    }

    public void setSkipOnError(boolean skipOnError) {
        this.skipOnError = skipOnError;
    }

    @Override
    public VirtuosoLoaderConfig_V2 toNextVersion() throws DPUConfigException {

        final VirtuosoLoaderConfig_V2 config = new VirtuosoLoaderConfig_V2();
        if (!virtuosoUrl.isEmpty()) {
            config.setVirtuosoUrl(virtuosoUrl);
        }
        if (!username.isEmpty()) {
            config.setUsername(username);
        }
        if (!password.isEmpty()) {
            config.setPassword(password);
        }
        config.setClearDestinationGraph(clearDestinationGraph);
        config.setLoadDirectoryPath(loadDirectoryPath);
        config.setIncludeSubdirectories(includeSubdirectories);
        config.setLoadFilePattern(loadFilePattern);
        config.setTargetContext(targetContext);
        config.setStatusUpdateInterval(statusUpdateInterval);
        config.setThreadCount(threadCount);
        config.setSkipOnError(skipOnError);

        return config;

    }

}
