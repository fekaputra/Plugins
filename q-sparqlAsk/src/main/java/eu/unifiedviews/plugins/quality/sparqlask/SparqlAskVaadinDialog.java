package eu.unifiedviews.plugins.quality.sparqlask;

import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 * DPU's configuration dialog.
 */
public class SparqlAskVaadinDialog extends AbstractDialog<SparqlAskConfig_V1> {

    //private CheckBox checkPerGraph;

    private OptionGroup optMessageType;

    private TextField txtMessage;

    private TextArea txtAskQuery;

    public SparqlAskVaadinDialog() {
        super(SparqlAsk.class);
    }

    @Override
    public void setConfiguration(SparqlAskConfig_V1 c) throws DPUConfigException {
        //checkPerGraph.setValue(c.isPerGraph());
        optMessageType.setValue(c.getMessageType());
//        txtMessage.setValue(c.getMessage());
        txtAskQuery.setValue(c.getAskQuery());
    }

    @Override
    public SparqlAskConfig_V1 getConfiguration() throws DPUConfigException {
        final SparqlAskConfig_V1 c = new SparqlAskConfig_V1();
        c.setMessageType((DPUContext.MessageType) optMessageType.getValue());
        c.setAskQuery(txtAskQuery.getValue());

        return c;
    }

    @Override
    protected void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);

        optMessageType = new OptionGroup(ctx.tr("rdfvalidation.dialog.optMessageType.caption"));
        optMessageType.addItem(DPUContext.MessageType.ERROR);
        optMessageType.addItem(DPUContext.MessageType.WARNING);
        optMessageType.setValue(optMessageType.getItem(0));
        mainLayout.addComponent(optMessageType);

        txtAskQuery = new TextArea(ctx.tr("rdfvalidation.dialog.txtAskQuery.caption"));
        txtAskQuery.setSizeFull();
        txtAskQuery.setNullRepresentation("");
        txtAskQuery.setNullSettingAllowed(true);
        mainLayout.addComponent(txtAskQuery);
        mainLayout.setExpandRatio(txtAskQuery, 1.0f);

        setCompositionRoot(mainLayout);
    }
}
