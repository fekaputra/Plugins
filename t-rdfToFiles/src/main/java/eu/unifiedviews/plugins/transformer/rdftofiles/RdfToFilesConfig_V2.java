package eu.unifiedviews.plugins.transformer.rdftofiles;

import eu.unifiedviews.helpers.dpu.ontology.EntityDescription;
import org.eclipse.rdf4j.rio.RDFFormat;

@EntityDescription.Entity(type = RdfToFilesVocabulary.STR_CONFIG_CLASS)
public class RdfToFilesConfig_V2 {

    @EntityDescription.Property(uri = RdfToFilesVocabulary.STR_CONFIG_FILE_NAME)
    private String outFileName = "output";

    /**
     * Format of output data files.
     */
    @EntityDescription.Property(uri = RdfToFilesVocabulary.STR_CONFIG_FILE_FORMAT)
    private String rdfFileFormat = RDFFormat.TURTLE.getName();

    /**
     * If true then .graph file is generated.
     */
    private boolean genGraphFile = true;

    /**
     * Used only if {@link #genGraphFile} is true.
     */
    @EntityDescription.Property(uri = RdfToFilesVocabulary.STR_CONFIG_GRAPH_URI)
    private String outGraphName = "http://localhost/resource/output";

    public RdfToFilesConfig_V2() {

    }

    public String getOutFileName() {
        return outFileName;
    }

    public void setOutFileName(String outFileName) {
        this.outFileName = outFileName;
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

    public String getOutGraphName() {
        return outGraphName;
    }

    public void setOutGraphName(String outGraphName) {
        this.outGraphName = outGraphName;
    }

}
