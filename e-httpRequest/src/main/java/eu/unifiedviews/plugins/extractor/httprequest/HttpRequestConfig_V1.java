package eu.unifiedviews.plugins.extractor.httprequest;

import eu.unifiedviews.helpers.dpu.ontology.EntityDescription;
import eu.unifiedviews.plugins.extractor.httprequest.rdfConfig.FormParamBody;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@EntityDescription.Entity(type = HttpRequestVocabulary.STR_CONFIG_CLASS)
public class HttpRequestConfig_V1 {

    public enum DataType {
        RAW_DATA, FORM_DATA, FILE, FORM_DATA_RDF;
    }

    public enum RequestType {
        GET, POST;
    }

    private DataType postRequestDataType = DataType.RAW_DATA;

    private RequestType requestType = RequestType.GET;

    private String charset = "UTF-8";

    private RequestContentType contentType = RequestContentType.TEXT;

    @EntityDescription.Property(uri = HttpRequestVocabulary.STR_URL)
    private String requestURL = "";

    @EntityDescription.Property(uri = HttpRequestVocabulary.STR_RAW_REQUEST_BODY)
    private String rawRequestBody = "";

    private String userName;

    private String password;

    private boolean useAuthentication;

    private Map<String, String> formDataRequestBody = new HashMap<>();

    private String fileName = "http_response";

    @EntityDescription.Property(uri = HttpRequestVocabulary.STR_RAW_FORM_PARAMS_BODIES)
    private List<FormParamBody> formParamBodies = new LinkedList<>();

    public List<FormParamBody> getFormParamBodies() {
        return formParamBodies;
    }

    public void setFormParamBodies(List<FormParamBody> formParamBodies) {
        this.formParamBodies = formParamBodies;
    }

    public DataType getPostRequestDataType() {
        return this.postRequestDataType;
    }

    public void setPostRequestDataType(DataType postRequestDataType) {
        this.postRequestDataType = postRequestDataType;
    }

    public RequestType getRequestType() {
        return this.requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public String getCharset() {
        return this.charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public RequestContentType getContentType() {
        return this.contentType;
    }

    public void setContentType(RequestContentType contentType) {
        this.contentType = contentType;
    }

    public String getRequestURL() {
        return this.requestURL;
    }

    public void setRequestURL(String requestURL) {
        this.requestURL = requestURL;
    }

    public String getRawRequestBody() {
        return this.rawRequestBody;
    }

    public void setRawRequestBody(String rawRequestBody) {
        this.rawRequestBody = rawRequestBody;
    }

    public Map<String, String> getFormDataRequestBody() {
        return this.formDataRequestBody;
    }

    public void setFormDataRequestBody(Map<String, String> formDataRequestBody) {
        this.formDataRequestBody = formDataRequestBody;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isUseAuthentication() {
        return this.useAuthentication;
    }

    public void setUseAuthentication(boolean useAuthentication) {
        this.useAuthentication = useAuthentication;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

}
