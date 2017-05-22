package eu.unifiedviews.plugins.extractor.httprequest;

/**
 * Example:
 * <http://localhost/resource/config>
 * <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://unifiedviews.eu/ontology/dpu/httpRequest/Config>;
 * <http://unifiedviews.eu/ontology/dpu/httpRequest/requestBody> """{"pageUrl" : "http://mytestt3.com","title" : "testt title updated", "username" : "test user", "spaceKey" : "test space", "text" : "testt text updated", "creationDate" : "2016-11-12" }""" .
 */
public class HttpRequestVocabulary {

    public static final String PREFIX = "http://unifiedviews.eu/ontology/dpu/httpRequest/";
    public static final String STR_CONFIG_CLASS = PREFIX + "Config";
    public static final String STR_RAW_REQUEST_BODY = PREFIX + "requestBody";
    public static final String STR_URL = PREFIX + "url";

}
