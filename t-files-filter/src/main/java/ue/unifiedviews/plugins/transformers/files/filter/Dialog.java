package ue.unifiedviews.plugins.transformers.files.filter;

import com.vaadin.data.Property;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.config.BaseConfigDialog;
import java.net.URISyntaxException;

public class Dialog extends BaseConfigDialog<Configuration> {

    private static final int OPTION_SYMBOLIC_NAME = 1;

    private static final int OPTION_VIRTUAL_PATH = 2;

    private static final int OPTION_CUSTOM = 3;

    private VerticalLayout mainLayout;

    private OptionGroup optType;

    private TextField txtPredicate;

    private TextField txtObject;

    private CheckBox checkUseRegExp;

    public Dialog() {
        super(Configuration.class);
        buildMainLayout();
    }

    private void buildMainLayout() {
        setWidth("100%");
        setHeight("100%");

        mainLayout = new VerticalLayout();
        mainLayout.setImmediate(false);
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setSpacing(true);

        optType = new OptionGroup("Used filter:");
        optType.addItem(OPTION_SYMBOLIC_NAME);
        optType.setItemCaption(OPTION_SYMBOLIC_NAME, "symbolic name");

        optType.addItem(OPTION_VIRTUAL_PATH);
        optType.setItemCaption(OPTION_VIRTUAL_PATH, "virtual path");

        optType.addItem(OPTION_CUSTOM);
        optType.setItemCaption(OPTION_CUSTOM, "custom");

        mainLayout.addComponent(optType);

        txtPredicate = new TextField("Custom predicate:");
        txtPredicate.setWidth("100%");
        txtPredicate.setRequired(true);
        mainLayout.addComponent(txtPredicate);

        txtObject = new TextField("Custom predicate:");
        txtObject.setWidth("100%");
        txtObject.setRequired(true);
        mainLayout.addComponent(txtObject);

        checkUseRegExp = new CheckBox("Use regular expression:");
        mainLayout.addComponent(checkUseRegExp);

        // action listener
        optType.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                final Integer selected = (Integer) event.getProperty()
                        .getValue();
                txtPredicate.setEnabled(selected == OPTION_CUSTOM);
            }
        });

        setCompositionRoot(mainLayout);
    }

    @Override
    protected void setConfiguration(Configuration c) throws DPUConfigException {

        if (c.isCustomPredicate()) {
            optType.setValue(OPTION_CUSTOM);
            txtPredicate.setValue(c.getPredicate());
        } else {
            if (c.getPredicate().compareTo(FixedPredicates.SYMBOLIC_NAME) == 0) {
                optType.setValue(OPTION_SYMBOLIC_NAME);
            } else {
                // TODO We can be more save here ..
                optType.setValue(OPTION_VIRTUAL_PATH);
            }
        }

        txtObject.setValue(c.getObject());
        checkUseRegExp.setValue(c.isUseRegExp());
    }

    @Override
    protected Configuration getConfiguration() throws DPUConfigException {
        if (!txtObject.isValid()
                || (!txtPredicate.isValid()
                && (Integer) optType.getValue() == OPTION_CUSTOM)) {
            throw new DPUConfigException("All fields must be filled.");
        }

        final Configuration cnf = new Configuration();
        final Integer selected = (Integer) optType.getValue();
        switch (selected) {
            case OPTION_SYMBOLIC_NAME:
                cnf.setCustomPredicate(false);
                cnf.setPredicate(FixedPredicates.SYMBOLIC_NAME);
                break;
            case OPTION_VIRTUAL_PATH:
                cnf.setCustomPredicate(false);
                cnf.setPredicate(FixedPredicates.VIRTUAL_PATH);
                break;
            case OPTION_CUSTOM:
                cnf.setCustomPredicate(true);
                final String predicate = txtPredicate.getValue();
                try {
                    // just try to create
                    new java.net.URI(predicate);
                } catch (URISyntaxException ex) {
                    throw new DPUConfigException(
                            "The predicate must be a valied URI.");
                }
                cnf.setPredicate(predicate);
                break;
            default:
                throw new DPUConfigException(
                        "The dialog is broken - unknown selection.");
        }
        cnf.setObject(txtObject.getValue());
        cnf.setUseRegExp(checkUseRegExp.getValue());
        return cnf;
    }

    @Override
    public String getDescription() {
        StringBuilder desc = new StringBuilder();

        desc.append("Filter by : ");

        final Integer selected = (Integer) optType.getValue();
        switch (selected) {
            case OPTION_SYMBOLIC_NAME:
                desc.append("symbolic name");
                break;
            case OPTION_VIRTUAL_PATH:
                desc.append("virtual path");
                break;
            case OPTION_CUSTOM:
                desc.append("<");
                desc.append(txtPredicate.getValue());
                desc.append(">");
                break;
        }
        
        desc.append(" for ");
        desc.append(txtObject.getValue());

        return desc.toString();
    }

}
