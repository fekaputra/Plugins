package eu.unifiedviews.plugins;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 * Vaadin configuration dialog for JsonToXml.
 *
 * @author Unknown
 */
public class JsonToXmlVaadinDialog extends AbstractDialog<JsonToXmlConfig_V1> {

    public JsonToXmlVaadinDialog() {
        super(JsonToXml.class);
    }

    @Override
    public void setConfiguration(JsonToXmlConfig_V1 c) throws DPUConfigException {

    }

    @Override
    public JsonToXmlConfig_V1 getConfiguration() throws DPUConfigException {
        final JsonToXmlConfig_V1 c = new JsonToXmlConfig_V1();

        return c;
    }

    @Override
    public void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);

        mainLayout.addComponent(new Label(ctx.tr("JsonToXml.dialog.label")));

        setCompositionRoot(mainLayout);
    }
}
