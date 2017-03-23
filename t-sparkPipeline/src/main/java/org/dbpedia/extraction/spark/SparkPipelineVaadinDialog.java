package org.dbpedia.extraction.spark;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 * Vaadin configuration dialog for SparkPipeline.
 *
 * @author Unknown
 */
public class SparkPipelineVaadinDialog extends AbstractDialog<SparkPipelineConfig_V1> {

    public SparkPipelineVaadinDialog() {
        super(SparkPipeline.class);
    }

    @Override
    public void setConfiguration(SparkPipelineConfig_V1 c) throws DPUConfigException {

    }

    @Override
    public SparkPipelineConfig_V1 getConfiguration() throws DPUConfigException {
        final SparkPipelineConfig_V1 c = new SparkPipelineConfig_V1();

        return c;
    }

    @Override
    public void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);

        mainLayout.addComponent(new Label(ctx.tr("SparkPipeline.dialog.label")));

        setCompositionRoot(mainLayout);
    }
}
