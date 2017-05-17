package eu.unifiedviews.plugins.transformer.sparql.update;

import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.parser.QueryParserUtil;

import com.vaadin.data.Validator;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

import cz.cuni.mff.xrg.uv.transformer.sparql.update.SparqlUpdateConfig_V1;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 * @author Škoda Petr
 */
public class SparqlUpdateVaadinDialog extends AbstractDialog<SparqlUpdateConfig_V1> {

    private TextArea txtQuery;

    private CheckBox checkPerGraph;

    public SparqlUpdateVaadinDialog() {
        super(SparqlUpdate.class);
    }

    @Override
    public void setConfiguration(SparqlUpdateConfig_V1 c) throws DPUConfigException {
        txtQuery.setValue(c.getQuery());
        checkPerGraph.setValue(c.isPerGraph());
    }

    @Override
    public SparqlUpdateConfig_V1 getConfiguration() throws DPUConfigException {
        final SparqlUpdateConfig_V1 c = new SparqlUpdateConfig_V1();
        if (txtQuery.getValue().isEmpty()) {
            throw new DPUConfigException(ctx.tr("sparqlUpdate.dialog.error.emptyQuery"));
        }
        if (!txtQuery.isValid()) {
            throw new DPUConfigException(ctx.tr("sparqlvalidator.invalidQuery"));
        }
        c.setQuery(txtQuery.getValue());
        c.setPerGraph(checkPerGraph.getValue());
        return c;
    }

    @Override
    protected void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);

        checkPerGraph = new CheckBox(ctx.tr("sparqlUpdate.dialog.perGraph"));
        checkPerGraph.setWidth("100%");
        mainLayout.addComponent(checkPerGraph);
        mainLayout.setExpandRatio(checkPerGraph, 0.0f);

        txtQuery = new TextArea(ctx.tr("sparqlUpdate.dialog.query"));
        txtQuery.setSizeFull();
        txtQuery.setRequired(true);
        txtQuery.addValidator(createSparqlUpdateQueryValidator());
        txtQuery.setImmediate(true);
        mainLayout.addComponent(txtQuery);
        mainLayout.setExpandRatio(txtQuery, 1.0f);

        setCompositionRoot(mainLayout);
    }

    private Validator createSparqlUpdateQueryValidator() {
        Validator validator = new Validator() {
            private static final long serialVersionUID = 1L;

            @Override
            public void validate(Object value) throws InvalidValueException {
                final String valueStr = (String) value;
                if (value == null || valueStr.isEmpty()) {
                    throw new InvalidValueException(ctx.tr("sparqlvalidator.emptyQuery"));
                }

                try {
                    QueryParserUtil.parseUpdate(QueryLanguage.SPARQL, valueStr, null);
                } catch (MalformedQueryException ex) {
                    throw new InvalidValueException(ctx.tr("sparqlvalidator.invalidQuery") + " " + ex.getMessage());
                }
            }
        };
        return validator;
    }

}
