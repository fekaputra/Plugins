package eu.unifiedviews.plugins.transformer.sparql.construct.editor;

import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractJavaScriptComponent;
import com.vaadin.ui.JavaScriptFunction;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.Serializable;
import java.util.ArrayList;

@JavaScript({"http://localhost:8080/editor/connector.js", "http://localhost:8080/editor/jquery-v1.11.1.min.js", "http://localhost:8080/editor/yasqe.bundled-v2.2.min.js"})
public class SparqlEditorComponent extends AbstractJavaScriptComponent {

    public interface ValueChangeListener extends Serializable {
        void valueChange();
    }

    ArrayList<ValueChangeListener> listeners =
            new ArrayList<ValueChangeListener>();
    public void addValueChangeListener(
            ValueChangeListener listener) {
        listeners.add(listener);
    }

//    public void setValue(String value) {
//        getState().value = value;
//    }
//
//    public String getValue() {
//        return getState().value;
//    }

    public void setQuery(String query) {
//        callFunction("setq");
        getState().query = query;
    }

    public String getQuery() {
        //get query from the editor
//        callFunction("getq");
        return getState().query;
    }

    @Override
    protected SparqlEditorState getState() {
        return (SparqlEditorState) super.getState();
    }


    public SparqlEditorComponent() {
        addFunction("onClick", new JavaScriptFunction() {
            @Override
            public void call(JSONArray arguments) {
                try {
                    getState().query = arguments.getString(0);
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
                for (ValueChangeListener listener: listeners)
                    listener.valueChange();
            }
        });
    }

}
