package eu.unifiedviews.plugins.extractor.sparqlendpoint;

import  org.eclipse.rdf4j.query.MalformedQueryException;
import  org.eclipse.rdf4j.query.QueryLanguage;
import  org.eclipse.rdf4j.query.parser.QueryParserUtil;

import com.vaadin.data.Validator;
import com.vaadin.ui.AbstractSelect.NewItemHandler;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;
import eu.unifiedviews.helpers.dpu.vaadin.validator.UrlValidator;

/**
 * Vaadin configuration dialog for SparqlEndpoint.
 */
public class SparqlEndpointVaadinDialog extends AbstractDialog<SparqlEndpointConfig_V1> {

    private TextField txtEndpoint;

    private ComboBox cmbChunkSize;

    private TextArea txtQuery;

    public SparqlEndpointVaadinDialog() {
        super(SparqlEndpoint.class);
    }

    @Override
    public void setConfiguration(SparqlEndpointConfig_V1 c) throws DPUConfigException {
        txtEndpoint.setValue(c.getEndpoint());
        txtQuery.setValue(c.getQuery());
        if (c.getChunkSize() == null) {
            cmbChunkSize.select(new Integer(-1));
        } else {
            if (!cmbChunkSize.containsId(c.getChunkSize())) {
                cmbChunkSize.addItem(c.getChunkSize());
            }
            cmbChunkSize.select(c.getChunkSize());
        }
    }

    @Override
    public SparqlEndpointConfig_V1 getConfiguration() throws DPUConfigException {
        if (!txtEndpoint.isValid()) {
            throw new DPUConfigException(ctx.tr("SparqlEndpoint.dialog.error.wrongEndpoint"));
        }
        if (!txtQuery.isValid()) {
            throw new DPUConfigException(ctx.tr("SparqlEndpoint.query.invalid"));
        }
        if (!cmbChunkSize.isValid()) {
            throw new DPUConfigException(ctx.tr("SparqlEndpoint.chunkSize.invalid"));
        }
        final SparqlEndpointConfig_V1 c = new SparqlEndpointConfig_V1();

        c.setEndpoint(txtEndpoint.getValue());
        c.setQuery(txtQuery.getValue());
        if (Integer.valueOf(-1).equals(cmbChunkSize.getValue())) {
            c.setChunkSize(null);
        } else {
            c.setChunkSize((Integer) cmbChunkSize.getValue());
        }
        return c;
    }

    @Override
    public void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);

        txtEndpoint = new TextField(ctx.tr("SparqlEndpoint.dialog.endpoint"));
        txtEndpoint.setWidth("100%");
        txtEndpoint.setRequired(true);
        txtEndpoint.addValidator(new UrlValidator(false));
        mainLayout.addComponent(txtEndpoint);
        mainLayout.setExpandRatio(txtEndpoint, 0);

        txtQuery = new TextArea(ctx.tr("SparqlEndpoint.dialog.query"));
        txtQuery.setSizeFull();
        txtQuery.setRequired(true);
        mainLayout.addComponent(txtQuery);
        mainLayout.setExpandRatio(txtQuery, 1.0f);

        txtQuery.addValidator(new Validator() {

            @Override
            public void validate(Object value) throws Validator.InvalidValueException {
                final String valueStr = (String) value;
                if (value == null || valueStr.isEmpty()) {
                    throw new InvalidValueException(ctx.tr("SparqlEndpoint.query.empty"));
                }

                try {
                    QueryParserUtil.parseQuery(QueryLanguage.SPARQL, valueStr, null);
                } catch (MalformedQueryException ex) {
                    throw new InvalidValueException(ctx.tr("SparqlEndpoint.query.invalid") + " " + ex.getMessage());
                }
            }
        });

        cmbChunkSize = new ComboBox(ctx.tr("SparqlEndpoint.dialog.chunksize"));
        cmbChunkSize.addItem(new Integer(-1));
        cmbChunkSize.setNullSelectionItemId(new Integer(-1));
        cmbChunkSize.setItemCaption(new Integer(-1), ctx.tr("SparqlEndpoint.dialog.chunksize.disabled"));
        cmbChunkSize.addItem(new Integer(1000));
        cmbChunkSize.addItem(new Integer(5000));
        cmbChunkSize.addItem(new Integer(10000));
        cmbChunkSize.addItem(new Integer(50000));
        cmbChunkSize.addItem(new Integer(10000));
        cmbChunkSize.addItem(new Integer(100000));
        cmbChunkSize.addItem(new Integer(500000));
        //cmbChunkSize.setWidth("30%");
        cmbChunkSize.setDescription(ctx.tr("SparqlEndpoint.dialog.chunksize.description"));
        cmbChunkSize.setImmediate(true);
        cmbChunkSize.setInvalidAllowed(false);
        cmbChunkSize.setNullSelectionAllowed(true);
        cmbChunkSize.setNewItemsAllowed(true);
        cmbChunkSize.setNewItemHandler(new NewItemHandler() {

            @Override
            public void addNewItem(String newItemCaption) {
                try {
                    Integer newItem = Integer.valueOf(newItemCaption);
                    cmbChunkSize.getContainerDataSource().addItem(newItem);
                    cmbChunkSize.select(newItem);
                } catch (NumberFormatException ex) {
                    //in case of issues, select -1 (default choice)
                    cmbChunkSize.select(new Integer(-1));
                }
            }
        });
        mainLayout.addComponent(cmbChunkSize);

        setCompositionRoot(mainLayout);
    }
}
