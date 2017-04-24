package eu.unifiedviews.plugins;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 * Vaadin configuration dialog for MyDpu.
 *
 * @author Unknown
 */
public class RdfUnitDPUVaadinDialog extends AbstractDialog<RdfUnitDPUConfig_V1> {

    public RdfUnitDPUVaadinDialog() {
        super(RdfUnitDPU.class);
    }

    @Override
    public void setConfiguration(RdfUnitDPUConfig_V1 c) throws DPUConfigException {

    }

    @Override
    public RdfUnitDPUConfig_V1 getConfiguration() throws DPUConfigException {
        final RdfUnitDPUConfig_V1 c = new RdfUnitDPUConfig_V1();

        return c;
    }

    @Override
    public void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);

        mainLayout.addComponent(new Label(ctx.tr("MyDpu.dialog.label")));

        setCompositionRoot(mainLayout);
    }
}
