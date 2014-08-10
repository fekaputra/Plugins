package eu.unifiedviews.plugins.transformer.fusiontool;

import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.config.BaseConfigDialog;
import eu.unifiedviews.plugins.transformer.fusiontool.config.ConfigReader;
import eu.unifiedviews.plugins.transformer.fusiontool.exceptions.InvalidInputException;

/**
 * DPU's configuration dialog. User can use this dialog to configure DPU
 * configuration.
 * @author Jan Michelfeit
 */
public class FusionToolDialog extends
        BaseConfigDialog<FusionToolConfig> {

    private static final long serialVersionUID = 1L;

    private GridLayout mainLayout;

    private TextArea configTextArea;

    private Label labelUpQuer;
    
    private String lastValidationError = "";

    /**
     * Initializes a new instance of the class.
     */
    public FusionToolDialog() {
        super(FusionToolConfig.class);
        buildMainLayout();
        setCompositionRoot(mainLayout);
    }

    @Override
    public void setConfiguration(FusionToolConfig conf)
            throws DPUConfigException {
        configTextArea.setValue(conf.getXmlConfig());
    }

    @Override
    public FusionToolConfig getConfiguration() throws DPUConfigException {
        if (!configTextArea.isValid()) {
            throw new DPUConfigException("Invalid configuration: " + lastValidationError);
        } else {
            FusionToolConfig conf = new FusionToolConfig(configTextArea.getValue().trim());
            return conf;
        }
    }

    @Override
    public String getToolTip() {
        return super.getToolTip();
    }

    @Override
    public String getDescription() {
        return super.getDescription();
    }

    /**
     * Builds main layout with all dialog components.
     * 
     * @return mainLayout GridLayout with all components of configuration
     *         dialog.
     */
    private GridLayout buildMainLayout() {
        // common part: create layout
        mainLayout = new GridLayout(2, 1);
        mainLayout.setImmediate(false);
        mainLayout.setWidth("100%");
        mainLayout.setHeight("100%");
        mainLayout.setMargin(false);

        // top-level component properties
        setWidth("100%");
        setHeight("100%");

        // labelUpQuer
        labelUpQuer = new Label();
        labelUpQuer.setImmediate(false);
        labelUpQuer.setWidth("68px");
        labelUpQuer.setHeight("-1px");
        labelUpQuer.setValue("Configuration");
        mainLayout.addComponent(labelUpQuer, 0, 0);

        // SPARQL Update Query textArea
        configTextArea = new TextArea();

        configTextArea.addValidator(new com.vaadin.data.Validator() {
            private static final long serialVersionUID = 1L;

            @Override
            public void validate(Object value) throws InvalidValueException {
                try {
                    ConfigReader.parseConfigXml(value.toString());
                } catch (InvalidInputException e) {
                    String message = "Invalid XML configuration: " + e.getMessage();
                    lastValidationError = message;
                    throw new InvalidValueException(message);
                }
            }
        });

        // configTextArea.setNullRepresentation("");
        configTextArea.setImmediate(true);
        configTextArea.setWidth("100%");
        configTextArea.setHeight("211px");
        configTextArea.setInputPrompt("<?xml version=\"1.0\"?>\n<config>\n</config>");

        mainLayout.addComponent(configTextArea, 1, 0);
        // CHECKSTYLE:OFF
        mainLayout.setColumnExpandRatio(0, 0.00001f);
        mainLayout.setColumnExpandRatio(1, 0.99999f);
        // CHECKSTYLE:ON

        return mainLayout;
    }
}
