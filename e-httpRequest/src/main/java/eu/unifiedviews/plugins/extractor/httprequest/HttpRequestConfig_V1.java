package eu.unifiedviews.plugins.extractor.httprequest;

import java.util.HashMap;
import java.util.Map;

public class HttpRequestConfig_V1 {

    public enum DataType {
        RAW_DATA, FORM_DATA, FILE;
    }

    public enum RequestType {
        GET, POST;
    }

    private DataType postRequestDataType = DataType.RAW_DATA;

    private RequestType requestType = RequestType.GET;

    private String charset = "UTF-8";

    private RequestContentType contentType = RequestContentType.TEXT;

    private String requestURL = "";

    private String rawRequestBody = "";

    private String userName;

    private String password;

    private boolean useAuthentication;

    private Map<String, String> formDataRequestBody = new HashMap<>();

    private static final String FILE_NAME_DEFAULT = "http_response";

    private String fileName = FILE_NAME_DEFAULT;

    private String customHeaderName;

    private String customHeaderValue;

    public String getCustomHeaderName() {
        return customHeaderName;
    }

    public void setCustomHeaderName(String customHeaderName) {
        this.customHeaderName = customHeaderName;
    }

    public String getCustomHeaderValue() {
        return customHeaderValue;
    }

    public void setCustomHeaderValue(String customHeaderValue) {
        this.customHeaderValue = customHeaderValue;
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
