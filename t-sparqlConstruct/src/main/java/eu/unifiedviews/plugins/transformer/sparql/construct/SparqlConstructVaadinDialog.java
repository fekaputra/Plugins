package eu.unifiedviews.plugins.transformer.sparql.construct;

import com.vaadin.ui.Notification;
import eu.unifiedviews.plugins.transformer.sparql.construct.editor.SparqlEditorComponent;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.parser.QueryParserUtil;

import com.vaadin.data.Validator;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

import cz.cuni.mff.xrg.uv.transformer.sparql.construct.SparqlConstructConfig_V1;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 * @author Škoda Petr
 */
public class SparqlConstructVaadinDialog extends AbstractDialog<SparqlConstructConfig_V1> {
    private static final long serialVersionUID = 1L;

    private TextArea txtQuery;

    private CheckBox checkPerGraph;

    private SparqlEditorComponent sparqlEditorComponent;

    public SparqlConstructVaadinDialog() {
        super(SparqlConstruct.class);
    }

    @Override
    public void setConfiguration(SparqlConstructConfig_V1 c) throws DPUConfigException {
        txtQuery.setValue(c.getQuery());
        checkPerGraph.setValue(c.isPerGraph());

        sparqlEditorComponent.setQuery(c.getQuery());
    }

    @Override
    public SparqlConstructConfig_V1 getConfiguration() throws DPUConfigException {
        final SparqlConstructConfig_V1 c = new SparqlConstructConfig_V1();
        if (txtQuery.getValue().isEmpty()) {
            throw new DPUConfigException(ctx.tr("SparqlConstructVaadinDialog.emptyQuery"));
        }
        if (!txtQuery.isValid()) {
            throw new DPUConfigException(ctx.tr("sparqlvalidator.invalidQuery"));
        }
        c.setQuery(txtQuery.getValue());
        c.setPerGraph(checkPerGraph.getValue());

        c.setQuery(sparqlEditorComponent.getQuery());
        return c;
    }

    @Override
    protected void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);

        checkPerGraph = new CheckBox(ctx.tr("SparqlConstructVaadinDialog.perGraphMode"));
        checkPerGraph.setWidth("100%");
        mainLayout.addComponent(checkPerGraph);
        mainLayout.setExpandRatio(checkPerGraph, 0.0f);


        sparqlEditorComponent = new SparqlEditorComponent();

        //mycomponent.setWidth("100%");
        sparqlEditorComponent.setSizeFull();
//        mycomponent.setImmediate(true);

        // Set the value from server-side
        //sparqlEditorComponent.setValue("Server-side value");

        // Process a value input by the user from the client-side
        sparqlEditorComponent.addValueChangeListener(
                new SparqlEditorComponent.ValueChangeListener() {
                    @Override
                    public void valueChange() {
                        //Notification.show("Value: " + sparqlEditorComponent.getQuery());
                    }
                });

        mainLayout.addComponent(sparqlEditorComponent);
        mainLayout.setExpandRatio(sparqlEditorComponent, 1.0f);

        txtQuery = new TextArea(ctx.tr("SparqlConstructVaadinDialog.constructQuery"));
        txtQuery.setSizeFull();
        txtQuery.setRequired(true);
        txtQuery.addValidator(createSparqlQueryValidator());
        txtQuery.setImmediate(true);
//        mainLayout.addComponent(txtQuery);
//        mainLayout.setExpandRatio(txtQuery, 1.0f);

        setCompositionRoot(mainLayout);
    }

    private Validator createSparqlQueryValidator() {
        Validator validator = new Validator() {
            private static final long serialVersionUID = 1L;

            @Override
            public void validate(Object value) throws InvalidValueException {
                final String valueStr = (String) value;
                if (value == null || valueStr.isEmpty()) {
                    throw new InvalidValueException(ctx.tr("sparqlvalidator.emptyQuery"));
                }

                try {
                    QueryParserUtil.parseQuery(QueryLanguage.SPARQL, valueStr, null);
                } catch (MalformedQueryException ex) {
                    throw new InvalidValueException(ctx.tr("sparqlvalidator.invalidQuery") + " " + ex.getMessage());
                }
            }
        };
        return validator;
    }

}
