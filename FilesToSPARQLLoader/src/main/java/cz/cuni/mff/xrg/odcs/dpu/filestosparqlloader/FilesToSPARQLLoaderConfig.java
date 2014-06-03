package cz.cuni.mff.xrg.odcs.dpu.filestosparqlloader;

import java.util.Set;

import cz.cuni.mff.xrg.odcs.commons.module.config.DPUConfigObjectBase;

public class FilesToSPARQLLoaderConfig extends DPUConfigObjectBase {
    /**
     * 
     */
    private static final long serialVersionUID = -3161162556703740405L;

    private String queryEndpointUrl;

    private String updateEndpointUrl;

    private int commitSize = 10000;
    
    private Set<String> targetContexts;

    // DPUTemplateConfig must provide public non-parametric constructor
    public FilesToSPARQLLoaderConfig() {
    }

    public String getQueryEndpointUrl() {
        return queryEndpointUrl;
    }

    public void setQueryEndpointUrl(String queryEndpointUrl) {
        this.queryEndpointUrl = queryEndpointUrl;
    }

    public String getUpdateEndpointUrl() {
        return updateEndpointUrl;
    }

    public void setUpdateEndpointUrl(String updateEndpointUrl) {
        this.updateEndpointUrl = updateEndpointUrl;
    }

    public int getCommitSize() {
        return commitSize;
    }

    public void setCommitSize(int commitSize) {
        this.commitSize = commitSize;
    }

    public Set<String> getTargetContexts() {
        return targetContexts;
    }

    public void setTargetContexts(Set<String> targetContexts) {
        this.targetContexts = targetContexts;
    }

}
