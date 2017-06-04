package eu.unifiedviews.plugins.extractor.httprequest.rdfConfig;

import eu.unifiedviews.helpers.dpu.ontology.EntityDescription;
import eu.unifiedviews.plugins.extractor.httprequest.HttpRequestVocabulary;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by tomasknap on 04/06/2017.
 */
@EntityDescription.Entity(type = HttpRequestVocabulary.STR_RAW_FORM_PARAMS_BODY_CLASS)
public class FormParamBody {

    @EntityDescription.Property(uri = HttpRequestVocabulary.STR_RAW_FORM_PARAMS)
    private List<FormParam> formParams = new LinkedList<>();

    public List<FormParam> getFormParams() {
        return formParams;
    }

    public void setFormParams(List<FormParam> formParams) {
        this.formParams = formParams;
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        for (FormParam p: formParams) {
            output.append(p);
            output.append(",");
        }
        return output.toString();
    }
}
