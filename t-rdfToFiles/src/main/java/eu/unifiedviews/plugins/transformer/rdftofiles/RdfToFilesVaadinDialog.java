package eu.unifiedviews.plugins.transformer.rdftofiles;

import com.vaadin.data.Property;
import com.vaadin.ui.*;
import eu.unifiedviews.dpu.config.DPUConfigException;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

public class RdfToFilesVaadinDialog extends AbstractDialog<RdfToFilesConfig_V2> {

    private static final Logger LOG = LoggerFactory.getLogger(RdfToFilesVaadinDialog.class);

    private VerticalLayout mainLayout;

    private NativeSelect selectRdfFormat;

    private Panel panelSingleGraph;

    private CheckBox checkGenGraphFile;

    private TextField txtOutGraphName;

    private TextField txtSingleFileOutputSymbolicName;

    public RdfToFilesVaadinDialog() {
        super(RdfToFiles.class);
    }

    @Override
    protected void buildDialogLayout() {
        setSizeFull();

        mainLayout = new VerticalLayout();
        mainLayout.setImmediate(false);
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);

        buildPanelSingleGraph();
        mainLayout.addComponent(panelSingleGraph);

        setCompositionRoot(mainLayout);
    }

    private void buildPanelSingleGraph() {
        final VerticalLayout layout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setSpacing(true);

        selectRdfFormat = new NativeSelect(ctx.tr("rdfToFiles.dialog.format"));
        for (RDFFormat item : RDFFormat.values()) {
            selectRdfFormat.addItem(item);
            selectRdfFormat.setItemCaption(item, item.getName());
        }
        selectRdfFormat.setNullSelectionAllowed(false);
        selectRdfFormat.setImmediate(true);
        mainLayout.addComponent(selectRdfFormat);

        checkGenGraphFile = new CheckBox(ctx.tr("rtfToFiles.dialog.graphFile"));
        mainLayout.addComponent(checkGenGraphFile);

        txtOutGraphName = new TextField(ctx.tr("rdfToFiles.dialog.outputGraph"));
        txtOutGraphName.setWidth("100%");
        mainLayout.addComponent(txtOutGraphName);

        txtSingleFileOutputSymbolicName = new TextField(ctx.tr("rdfToFiles.dialog.outputFile"));
        txtSingleFileOutputSymbolicName.setWidth("100%");
        mainLayout.addComponent(txtSingleFileOutputSymbolicName);

        panelSingleGraph = new Panel();
        panelSingleGraph.setContent(layout);

        checkGenGraphFile.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                txtOutGraphName.setEnabled((Boolean) event.getProperty().getValue());
            }
        });

        selectRdfFormat.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                final RDFFormat format = (RDFFormat) event.getProperty().getValue();
                if (format.supportsContexts()) {
                    // graph will be exported as a part of output file
                    // we wil force user to specify graph
                    checkGenGraphFile.setValue(true);
                    checkGenGraphFile.setEnabled(false);
                } else {
                    // enable standalone graph file
                    checkGenGraphFile.setEnabled(true);
                }
            }
        });
    }

    @Override
    protected void setConfiguration(RdfToFilesConfig_V2 c) throws DPUConfigException {
        selectRdfFormat.setValue(RDFFormat.valueOf(c.getRdfFileFormat()));
        checkGenGraphFile.setValue(c.isGenGraphFile());
        if (c.isGenGraphFile()) {
            txtOutGraphName.setValue(c.getOutGraphName());
            txtOutGraphName.setEnabled(true);
        } else {
            txtOutGraphName.setEnabled(false);
        }
        txtSingleFileOutputSymbolicName.setValue(c.getOutFileName());
    }

    @Override
    protected RdfToFilesConfig_V2 getConfiguration() throws DPUConfigException {
        RdfToFilesConfig_V2 cnf = new RdfToFilesConfig_V2();

        final RDFFormat format = (RDFFormat) selectRdfFormat.getValue();

        cnf.setRdfFileFormat(format.getName());

        cnf.setGenGraphFile(checkGenGraphFile.getValue());
        // set always even if not used, to be friendly to user
        cnf.setOutGraphName(txtOutGraphName.getValue());
        // check for text box - as it's required for context aware file formats
        if (format.supportsContexts()) {
            // graph uri must be provided
            final String graphName = txtOutGraphName.getValue();
            if (graphName == null || graphName.isEmpty()) {
                throw new DPUConfigException(ctx.tr("rdfToFiles.dialog.missing.graphName"));
            }
        }
        cnf.setOutFileName(txtSingleFileOutputSymbolicName.getValue());

        return cnf;
    }

    @Override
    public String getDescription() {
        StringBuilder desc = new StringBuilder();

        desc.append(ctx.tr("rdfToFiles.dialog.description"));
        desc.append(txtSingleFileOutputSymbolicName);
        desc.append(".");
        desc.append(((RDFFormat) selectRdfFormat.getValue()).getDefaultFileExtension());

        return desc.toString();
    }

}
