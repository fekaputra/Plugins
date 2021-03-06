package eu.unifiedviews.plugins.transformer.fusiontool.config;

import cz.cuni.mff.odcleanstore.conflictresolution.ResolutionStrategy;
import cz.cuni.mff.odcleanstore.conflictresolution.impl.ResolutionStrategyImpl;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.rio.ParserConfig;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Container of configuration values.
 * @author Jan Michelfeit
 */
public class ConfigContainerImpl implements ConfigContainer {
    private String resultDataURIPrefix = FTConfigConstants.DEFAULT_RESULT_DATA_URI_PREFIX;
    private Map<String, String> prefixes = new HashMap<>();
    private ResolutionStrategy defaultResolutionStrategy = new ResolutionStrategyImpl();
    private Map<IRI, ResolutionStrategy> propertyResolutionStrategies = new HashMap<>();

    private boolean isProfilingOn = false;
    private Long memoryLimit = null;
    private IRI requiredClassOfProcessedResources = null;
    private ParserConfig parserConfig = FTConfigConstants.DEFAULT_FILE_PARSER_CONFIG;
    private String dataGraphSymbolicName = FTConfigConstants.DEFAULT_DATA_GRAPH_NAME;
    private String metadataGraphSymbolicName = FTConfigConstants.DEFAULT_METADATA_GRAPH_NAME;

    @Override
    public String getResultDataURIPrefix() { // TODO: settable in XML configuration
        return resultDataURIPrefix;
    }

    /**
     * Sets prefix of named graphs and URIs where query results and metadata in the output are placed.
     * @param resultDataURIPrefix named graph IRI prefix
     */
    public void setResultDataURIPrefix(String resultDataURIPrefix) {
        this.resultDataURIPrefix = resultDataURIPrefix;
    }

    @Override
    public boolean getOutputMappedSubjectsOnly() {
        return FTConfigConstants.OUTPUT_MAPPED_SUBJECTS_ONLY;
    }

    @Override
    public Map<String, String> getPrefixes() {
        return prefixes;
    }

    /**
     * Sets map of defined namespace prefixes.
     * @param prefixes map of namespace prefixes
     */
    public void setPrefixes(Map<String, String> prefixes) {
        this.prefixes = prefixes;
    }

    @Override
    public IRI getRequiredClassOfProcessedResources() {
        return requiredClassOfProcessedResources;
    }

    /**
     * Sets value for {@link #getRequiredClassOfProcessedResources()}.
     * @param requiredClassOfProcessedResources see {@link #getRequiredClassOfProcessedResources()}
     */
    public void setRequiredClassOfProcessedResources(IRI requiredClassOfProcessedResources) {
        this.requiredClassOfProcessedResources = requiredClassOfProcessedResources;
    }

    @Override
    public ResolutionStrategy getDefaultResolutionStrategy() {
        return defaultResolutionStrategy;
    }

    /**
     * Setter for value of {@link #getDefaultResolutionStrategy()}.
     * @param strategy conflict resolution strategy
     */
    public void setDefaultResolutionStrategy(ResolutionStrategy strategy) {
        this.defaultResolutionStrategy = strategy;
    }

    @Override
    public Map<IRI, ResolutionStrategy> getPropertyResolutionStrategies() {
        return propertyResolutionStrategies;
    }

    /**
     * Setter for value of {@link #getDefaultResolutionStrategy()}.
     * @param strategies per-property conflict resolution strategies
     */
    public void setPropertyResolutionStrategies(Map<IRI, ResolutionStrategy> strategies) {
        this.propertyResolutionStrategies = strategies;
    }

    @Override
    public String getCanonicalURIsFileName() {
        return FTConfigConstants.CANONICAL_URI_FILE_NAME;
    }

    @Override
    public ParserConfig getParserConfig() {
        return parserConfig;
    }

    /**
     * Sets value for {@link #getParserConfig()}.
     * @param parserConfig see {@link #getParserConfig()}
     */
    public void setParserConfig(ParserConfig parserConfig) {
        this.parserConfig = parserConfig;
    }


    @Override
    public boolean getEnableFileCache() {
        return FTConfigConstants.ENABLE_FILE_CACHE;
    }

    @Override
    public boolean isLocalCopyProcessing() {
        return true;
    }

    @Override
    public Long getMemoryLimit() {
        return memoryLimit;
    }

    /**
     * Sets value for {@link #getMemoryLimit()}.
     * @param memoryLimit see {@link #getMemoryLimit()}
     */
    public void setMemoryLimit(Long memoryLimit) {
        this.memoryLimit = memoryLimit;
    }

    @Override
    public float getMaxFreeMemoryUsage() {
        return FTConfigConstants.MAX_FREE_MEMORY_USAGE;
    }

    @Override
    public Integer getQueryTimeout() {
        return FTConfigConstants.DEFAULT_QUERY_TIMEOUT;
    }

    @Override
    public boolean getWriteMetadata() {
        return FTConfigConstants.WRITE_METADATA;
    }

    @Override
    public Double getAgreeCoefficient() {
        return FTConfigConstants.AGREE_COEFFICIENT;
    }


    @Override
    public Double getScoreIfUnknown() {
        return FTConfigConstants.SCORE_IF_UNKNOWN;
    }

    @Override
    public Double getPublisherScoreWeight() {
        return FTConfigConstants.PUBLISHER_SCORE_WEIGHT;
    }

    @Override
    public Long getMaxDateDifference() {
        return FTConfigConstants.MAX_DATE_DIFFERENCE;
    }


    @Override
    public Collection<String> getPreferredCanonicalURIs() {
        return FTConfigConstants.DEFAULT_PREFERRED_CANONICAL_URIS;
    }

    @Override
    public Set<IRI> getSameAsLinkTypes() {
        return FTConfigConstants.SAME_AS_LINK_TYPES;
    }


    @Override
    public boolean isProfilingOn() {
        return isProfilingOn;
    }

    /**
     * Sets value for {@link #isProfilingOn()}.
     * @param isProfilingOn see {@link #isProfilingOn()}
     */
    public void setProfilingOn(boolean isProfilingOn) {
        this.isProfilingOn = isProfilingOn;
    }

    @Override
    public Long getMaxOutputTriples() {
        return null;
    }

    @Override
    public String getDataGraphSymbolicName() {
        return dataGraphSymbolicName;
    }

    public void setDataGraphSymbolicName(String dataGraphSymbolicName) {
        // FIXME: set from configuration
        this.dataGraphSymbolicName = dataGraphSymbolicName;
    }

    @Override
    public String getMetadataGraphSymbolicName() {
        return metadataGraphSymbolicName;
    }

    public void setMetadataGraphSymbolicName(String metadataGraphSymbolicName) {
        // FIXME: set from configuration
        this.metadataGraphSymbolicName = metadataGraphSymbolicName;
    }
}
