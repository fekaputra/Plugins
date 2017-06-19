package eu.unifiedviews.plugins.extractor.httprequest.rdfConfig;

import eu.unifiedviews.helpers.dpu.ontology.EntityDescription;
import eu.unifiedviews.plugins.extractor.httprequest.HttpRequestVocabulary;

/**
 * Created by tomasknap on 04/06/2017.
 */
@EntityDescription.Entity(type = HttpRequestVocabulary.STR_RAW_FORM_PARAM_CLASS)
public class FormParam {

    @EntityDescription.Property(uri = HttpRequestVocabulary.STR_RAW_FORM_PARAM)
    private String param;

    @EntityDescription.Property(uri = HttpRequestVocabulary.STR_RAW_FORM_PARAM_VALUE)
    private String value;

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return param + "=" + value;
    }
}
