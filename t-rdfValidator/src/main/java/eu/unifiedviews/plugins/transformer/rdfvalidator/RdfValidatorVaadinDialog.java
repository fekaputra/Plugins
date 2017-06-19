package eu.unifiedviews.plugins.transformer.rdfvalidator;

import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.parser.ParsedBooleanQuery;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.ParsedTupleQuery;
import org.eclipse.rdf4j.query.parser.QueryParserUtil;

import com.vaadin.data.Validator;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 * DPU's configuration dialog.
 */
public class RdfValidatorVaadinDialog extends AbstractDialog<RdfValidatorConfig_V2> {

    /**
     *
     */
    private static final long serialVersionUID = 518622055536470336L;

    private ObjectProperty<Boolean> failExecution = new ObjectProperty<Boolean>(Boolean.TRUE);

    private ObjectProperty<String> query = new ObjectProperty<String>("");
    TextArea txtQuery ;
    public RdfValidatorVaadinDialog() {
        super(RdfValidator.class);
    }

    @Override
    protected void buildDialogLayout() {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);
        mainLayout.setWidth("100%");
        mainLayout.setHeight("100%");

        txtQuery= new TextArea(ctx.tr("dialog.query"), query);
        txtQuery.setWidth("100%");
        txtQuery.setSizeFull();
        txtQuery.setNullRepresentation("");
        txtQuery.setNullSettingAllowed(true);
        txtQuery.setImmediate(true);
        txtQuery.addValidator(createSparqlQueryValidator());
        mainLayout.addComponent(txtQuery);
        mainLayout.setExpandRatio(txtQuery, 1.0f);

        VerticalLayout bottomLayout = new VerticalLayout();

        bottomLayout.addComponent(new CheckBox(ctx.tr("dialog.messageType.fail"), failExecution));

        mainLayout.addComponent(bottomLayout);
        mainLayout.setExpandRatio(bottomLayout, 0.1f);
        setCompositionRoot(mainLayout);
    }

    @Override
    public void setConfiguration(RdfValidatorConfig_V2 c) throws DPUConfigException {
        failExecution.setValue(c.isFailExecution());
        query.setValue(c.getQuery());
    }

    @Override
    public RdfValidatorConfig_V2 getConfiguration() throws DPUConfigException {
        if (!txtQuery.isValid()) {
            throw new DPUConfigException(ctx.tr("sparqlvalidator.invalidQuery"));
        }
        final RdfValidatorConfig_V2 c = new RdfValidatorConfig_V2();
        c.setFailExecution(failExecution.getValue());
        c.setQuery(query.getValue());
        return c;
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
                    ParsedQuery parsedQuery = QueryParserUtil.parseQuery(QueryLanguage.SPARQL, valueStr, null);
                    if (!(parsedQuery instanceof ParsedBooleanQuery)&&!(parsedQuery instanceof ParsedTupleQuery)) {
                        throw new InvalidValueException(ctx.tr("error.unsupported.query.type"));
                    }
                } catch (MalformedQueryException ex) {
                    throw new InvalidValueException(ctx.tr("sparqlvalidator.invalidQuery") + " " + ex.getMessage());
                }
            }
        };
        return validator;
    }
}
