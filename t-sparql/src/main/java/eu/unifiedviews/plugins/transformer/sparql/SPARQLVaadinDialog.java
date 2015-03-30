package eu.unifiedviews.plugins.transformer.sparql;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.vaadin.data.Validator;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.config.BaseConfigDialog;
import eu.unifiedviews.helpers.dpu.localization.Messages;

/**
 * Configuration dialog for DPU SPARQL Transformer.
 *
 * @authod Petr Škoda
 */
public class SPARQLVaadinDialog extends BaseConfigDialog<SPARQLConfig_V1> {

    private ObjectProperty<String> outputGraphSymbolicName = new ObjectProperty<String>("");

    private enum QueryType {
        INVALID,
        CONSTRUCT,
        UPDATE
    };

    private TabSheet tabsheet;

    private final LinkedList<TextArea> queries = new LinkedList<>();

    /**
     * Is valid only after isValid is called on all components in queries.
     */
    private final HashMap<TextArea, QueryType> queryTypes = new HashMap<>();

    private Messages messages;

    public SPARQLVaadinDialog() {
        super(SPARQLConfig_V1.class);
        init();
    }

    private void init() {
        messages = new Messages(getContext().getLocale(), this.getClass().getClassLoader());
        this.setSizeFull();

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);

        HorizontalLayout topLineLayout = new HorizontalLayout();
        topLineLayout.setSizeUndefined();
        topLineLayout.setSpacing(true);

        Button btnAddQuery = new Button();
        btnAddQuery.setCaption("Add query tab");
        btnAddQuery.setSizeUndefined();
        btnAddQuery.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                addGraph("CONSTRUCT { ?s ?p ?o } WHERE {?s ?p ?o }");
            }
        });
        topLineLayout.addComponent(btnAddQuery);

        mainLayout.addComponent(topLineLayout);
        mainLayout.setExpandRatio(topLineLayout, 0);

        tabsheet = new TabSheet();
        tabsheet.setSizeFull();
        mainLayout.addComponent(tabsheet);
        mainLayout.setExpandRatio(tabsheet, 1);

        mainLayout.addComponent(new TextField(messages.getString("SPARQLVaadinDialog.outputGraph"), outputGraphSymbolicName));
        CheckBox rewriteConstructToInsertCheckbox = new CheckBox(messages.getString("SPARQLVaadinDialog.rewrite"), true);
        rewriteConstructToInsertCheckbox.setEnabled(false);
        mainLayout.addComponent(rewriteConstructToInsertCheckbox);

        setCompositionRoot(mainLayout);
    }

    private void addGraph(String query) {
        VerticalLayout subLayout = new VerticalLayout();
        subLayout.setSizeFull();
        subLayout.setMargin(true);

        final TextArea txtQuery = new TextArea();
        txtQuery.setSizeFull();
        txtQuery.setValue(query);
        txtQuery.setSizeFull();

        subLayout.addComponent(txtQuery);

        // add to main component list
        this.queries.add(txtQuery);
        this.queryTypes.put(txtQuery, QueryType.INVALID);

        final Tab tab = this.tabsheet.addTab(subLayout, "Query");
        this.tabsheet.getTab(subLayout).setClosable(true);
        txtQuery.addValidator(new Validator() {

            @Override
            public void validate(Object value) throws InvalidValueException {
                final String query = value.toString();

                if (query.isEmpty()) {
                    throw new InvalidValueException(
                            messages.getString("SPARQLVaadinDialog.empty"));
                }

                QueryValidator updateValidator =
                        new SPARQLUpdateValidator(query);
                SPARQLQueryValidator constructValidator =
                        new SPARQLQueryValidator(query, SPARQLQueryType.CONSTRUCT);

                // also store type in case of sucessful validation
                if (constructValidator.isQueryValid()) {
                    queryTypes.put(txtQuery, QueryType.CONSTRUCT);
                    return;
                }

                if (updateValidator.isQueryValid()) {
                    queryTypes.put(txtQuery, QueryType.UPDATE);
                    return;
                }

                queryTypes.put(txtQuery, QueryType.INVALID);

                // return message based on query type
                if (constructValidator.hasSameType()) {
                    throw new InvalidValueException(
                            constructValidator.getErrorMessage());
                } else {
                    throw new InvalidValueException(
                            updateValidator.getErrorMessage());
                }
            }
        });

        tabsheet.setSelectedTab(tab);
    }

    /**
     * Load values from configuration object implementing {@link DPUConfig} interface and configuring DPU into the dialog
     * where the configuration object may be edited.
     *
     * @throws DPUConfigException
     *             Exception not used in current implementation of
     *             this method.
     * @param conf
     *            Object holding configuration which is used to initialize
     *            fields in the configuration dialog.
     */
    @Override
    public void setConfiguration(SPARQLConfig_V1 conf) throws DPUConfigException {
        queries.clear();
        queryTypes.clear();
        tabsheet.removeAllComponents();

        for (SPARQLQueryPair pair : conf.getQueryPairs()) {
            addGraph(pair.getSPARQLQuery());
        }

        outputGraphSymbolicName.setValue(conf.getOutputGraphSymbolicName());
    }

    /**
     * Set values from from dialog where the configuration object may be edited
     * to configuration object implementing {@link DPUConfigObject} interface
     * and configuring DPU
     *
     * @throws DPUConfigException
     *             Exception which might be thrown when any of
     *             SPARQL queries are invalid.
     * @return conf Object holding configuration which is used in {@link #setConfiguration} to initialize fields in the
     *         configuration dialog.
     */
    @Override
    public SPARQLConfig_V1 getConfiguration() throws DPUConfigException {

        SPARQLConfig_V1 conf = new SPARQLConfig_V1();
        List<SPARQLQueryPair> queryPairs = conf.getQueryPairs();

        for (int i = 0; i < queries.size(); i++) {
            TextArea txtQuery = queries.get(i);
            if (!txtQuery.isValid()) {
                throw new DPUConfigException(messages.getString("SPARQLVaadinDialog.invalidQuery"));
            }
            // add to conf
            final boolean isConstruct = queryTypes.get(txtQuery) == QueryType.CONSTRUCT;
            queryPairs.add(new SPARQLQueryPair(txtQuery.getValue(), isConstruct));
        }

        conf.setOutputGraphSymbolicName(outputGraphSymbolicName.getValue());
        return conf;
    }

}
