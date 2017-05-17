package eu.unifiedviews.plugins.transformer.rdftofiles;

import java.util.Arrays;
import java.util.List;
import org.eclipse.rdf4j.rio.RDFFormat;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.config.VersionedConfig;

public class RdfToFilesConfig_V1 implements VersionedConfig<RdfToFilesConfig_V2> {
    public class GraphToFileInfo {

        /**
         * Graph symbolic name. If {@link #mergeGraphs} is true the this value
         * is ignored.
         */
        private String inSymbolicName;

        /**
         * Output file name/path without extension.
         */
        private String outFileName;

        public GraphToFileInfo() {
        }

        public GraphToFileInfo(String inSymbolicName, String outFileName,
                String outputGraphName) {
            this.inSymbolicName = outFileName;
            this.outFileName = outFileName;
            // TODO Use outputGraphName as a graph name.
        }

        public String getInSymbolicName() {
            return inSymbolicName;
        }

        public void setInSymbolicName(String symbolicName) {
            this.inSymbolicName = symbolicName;
        }

        public String getOutFileName() {
            return outFileName;
        }

        public void setOutFileName(String outputFileName) {
            this.outFileName = outputFileName;
        }

    }

    /**
     * Format of output data files.
     */
    private String rdfFileFormat = RDFFormat.TURTLE.getName();

    /**
     * If true then .graph file is generated.
     */
    private boolean genGraphFile = true;

    /**
     * If false then we work on graph level. If true we work with triples and
     * only one record from {@link #graphToFileInfo} is used.
     */
    private boolean mergeGraphs = true;

    /**
     * Used only if {@link #genGraphFile} is true.
     */
    private String outGraphName = "";

    /**
     * How convert graphs
     */
    private List<GraphToFileInfo> graphToFileInfo = Arrays.asList(new GraphToFileInfo("", "data", ""));

    public RdfToFilesConfig_V1() {
    }

    public String getRdfFileFormat() {
        return rdfFileFormat;
    }

    public void setRdfFileFormat(String rdfFileFormat) {
        this.rdfFileFormat = rdfFileFormat;
    }

    public boolean isGenGraphFile() {
        return genGraphFile;
    }

    public void setGenGraphFile(boolean genGraphFile) {
        this.genGraphFile = genGraphFile;
    }

    public boolean isMergeGraphs() {
        return mergeGraphs;
    }

    public void setMergeGraphs(boolean mergeGraphs) {
        this.mergeGraphs = mergeGraphs;
    }

    public String getOutGraphName() {
        return outGraphName;
    }

    public void setOutGraphName(String graphName) {
        this.outGraphName = graphName;
    }

    public List<GraphToFileInfo> getGraphToFileInfo() {
        return graphToFileInfo;
    }

    public void setGraphToFileInfo(List<GraphToFileInfo> graphToFileInfo) {
        this.graphToFileInfo = graphToFileInfo;
    }

    @Override
    public RdfToFilesConfig_V2 toNextVersion() throws DPUConfigException {
        final RdfToFilesConfig_V2 config = new RdfToFilesConfig_V2();

        config.setGenGraphFile(this.genGraphFile);
        config.setOutFileName(this.graphToFileInfo.get(0).outFileName);
        config.setOutGraphName(this.outGraphName);
        config.setRdfFileFormat(this.rdfFileFormat);

        return config;
    }

}
