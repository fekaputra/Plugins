package cz.cuni.mff.xrg.uv.transformer.xslt;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import eu.unifiedviews.helpers.dpu.ontology.EntityDescription;
import eu.unifiedviews.plugins.transformer.xslt.XsltVocabulary;

/**
 * 
 * @author Škoda Petr
 */
@EntityDescription.Entity(type = XsltVocabulary.STR_XSLT_CLASS)
public class XsltConfig_V2 {

    @EntityDescription.Entity(type = XsltVocabulary.STR_XSLT_PARAM_CLASS)
    public static class Parameter {

        @EntityDescription.Property(uri = XsltVocabulary.STR_XSLT_PARAM_NAME_PREDICATE)
        private String key = null;

        @EntityDescription.Property(uri = XsltVocabulary.STR_XSLT_PARAM_VALUE_PREDICATE)
        private String value = null;

        public Parameter() {
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

    }

    @EntityDescription.Entity(type = XsltVocabulary.STR_XSLT_FILEINFO_CLASS)
    public static class FileInformations {

        @EntityDescription.Property(uri = XsltVocabulary.STR_XSLT_FILEINFO_PARAM_PREDICATE)
        private List<Parameter> parameters = new LinkedList<>();

        @EntityDescription.Property(uri = XsltVocabulary.STR_XSLT_FILEINFO_SYMBOLICNAME_PREDICATE)
        private String symbolicName;

        public FileInformations() {
        }

        public FileInformations(String symbolicName) {
            this.symbolicName = symbolicName;
        }

        public List<Parameter> getParameters() {
            return parameters;
        }

        public void setParameters(List<Parameter> parameters) {
            this.parameters = parameters;
        }

        public String getSymbolicName() {
            return symbolicName;
        }

        public void setSymbolicName(String symbolicName) {
            this.symbolicName = symbolicName;
        }
        
    }

    private String xsltTemplate = "";

    private String xsltTemplateName = "Empty";

    private boolean failOnError = true;

    /**
     * If null then extension does not change. Extension is added to current file name.
     */
    @EntityDescription.Property(uri = XsltVocabulary.STR_XSLT_OUTPUT_FILE_EXTENSION)
    private String outputFileExtension = "";

    @EntityDescription.Property(uri = XsltVocabulary.STR_XSLT_FILEINFO_PREDICATE)
    private List<FileInformations> filesParameters = new LinkedList<>();

    private int numberOfExtraThreads = 0;

    public XsltConfig_V2() {

    }

    public String getXsltTemplate() {
        return xsltTemplate;
    }

    public void setXsltTemplate(String xsltTemplate) {
        this.xsltTemplate = xsltTemplate;
    }

    public String getXsltTemplateName() {
        return xsltTemplateName;
    }

    public void setXsltTemplateName(String xsltTemplateName) {
        this.xsltTemplateName = xsltTemplateName;
    }

    public boolean isFailOnError() {
        return failOnError;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    public List<FileInformations> getFilesParameters() {
        return filesParameters;
    }

    public void setFilesParameters(List<FileInformations> filesParameters) {
        this.filesParameters = filesParameters;
    }

    public String getOutputFileExtension() {
        return outputFileExtension;
    }

    public void setOutputFileExtension(String outputFileExtension) {
        this.outputFileExtension = outputFileExtension;
    }

    public int getNumberOfExtraThreads() {
        return numberOfExtraThreads;
    }

    public void setNumberOfExtraThreads(int numberOfExtraThreads) {
        this.numberOfExtraThreads = numberOfExtraThreads;
    }

    /**
     *
     * @param symbolicName
     * @return Empty new class if no parameters for given symbolicName exists.
     */
    public List<Parameter> getFilesParameters(String symbolicName) {
        for (FileInformations fileInfo : filesParameters) {
            if (fileInfo.symbolicName.equals(symbolicName)) {
                return fileInfo.getParameters();
            }
        }
        // Return empty parameter list.
        return Arrays.asList();
    }

}
