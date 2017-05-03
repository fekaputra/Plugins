package eu.unifiedviews.plugins.trandformer.sparqlselect;

import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.parser.QueryParserUtil;

import com.vaadin.data.Validator;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

public class SparqlSelectVaadinDialog extends AbstractDialog<SparqlSelectConfig> {

    private TextField txtTarget;

    private TextArea txtQuery;

    public SparqlSelectVaadinDialog() {
        super(SparqlSelect.class);
    }

    @Override
    protected void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setImmediate(false);
        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);

        txtTarget = new TextField();
        txtTarget.setWidth("100%");
        txtTarget.setHeight("-1px");
        txtTarget.setCaption(ctx.tr("SparqlSelectVaadinDialog.txtTarget"));
        txtTarget.setRequired(true);
        mainLayout.addComponent(txtTarget);
        mainLayout.setExpandRatio(txtTarget, 0);

        txtQuery = new TextArea();
        txtQuery.setSizeFull();
        txtQuery.setCaption(ctx.tr("SparqlSelectVaadinDialog.txtQuery"));
        txtQuery.addValidator(createSparqlQueryValidator());
        txtQuery.setImmediate(true);
        txtTarget.setRequired(true);
        mainLayout.addComponent(txtQuery);
        mainLayout.setExpandRatio(txtQuery, 1);

        setCompositionRoot(mainLayout);
    }

    @Override
    protected void setConfiguration(SparqlSelectConfig conf) throws DPUConfigException {
        txtTarget.setValue(conf.getTargetPath());
        txtQuery.setValue(conf.getQuery());
    }

    @Override
    protected SparqlSelectConfig getConfiguration() throws DPUConfigException {
        if (!txtTarget.isValid()) {
            throw new DPUConfigException(ctx.tr("SparqlSelectVaadinDialog.exception.pathFilled"));
        }
        if (!txtQuery.isValid()) {
            throw new DPUConfigException(ctx.tr("sparqlvalidator.invalidQuery"));
        }

        SparqlSelectConfig conf = new SparqlSelectConfig();
        conf.setTargetPath(txtTarget.getValue());
        conf.setQuery(txtQuery.getValue());
        return conf;
    }

    @Override
    public String getDescription() {
        return ctx.tr("SparqlSelectVaadinDialog.description", txtTarget.getValue());
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
