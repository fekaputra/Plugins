package eu.unifiedviews.plugins.loader.filestoparliament;

import java.nio.charset.Charset;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.rio.RDFFormat;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

@SuppressWarnings("serial")
public class FilesToParliamentVaadinDialog extends AbstractDialog<FilesToParliamentConfig_V1> {
    private ObjectProperty<String> endpointURL = new ObjectProperty<String>("");

    private NativeSelect selectRdfFormat;
    private ObjectProperty<Boolean> clearDestinationGraph = new ObjectProperty<Boolean>(false);
    private static Set<RDFFormat> supportedRdfFormats;
    private ObjectProperty<String> targetGraphName = new ObjectProperty<String>("");
    private ObjectProperty<Boolean> perGraph = new ObjectProperty<Boolean>(
            false);    
    private static RDFFormat auto;
    static {
        auto = new RDFFormat("Auto", "text/plain",
                Charset.forName("US-ASCII"), "auto", false, false);
        supportedRdfFormats = new LinkedHashSet<>();
        supportedRdfFormats.add(auto);
        supportedRdfFormats.add(RDFFormat.N3);
        supportedRdfFormats.add(RDFFormat.TURTLE);
        supportedRdfFormats.add(RDFFormat.NTRIPLES);
        supportedRdfFormats.add(RDFFormat.RDFXML);
    }

    public FilesToParliamentVaadinDialog() {
        super(FilesToParliament.class);
    }

    @Override
    protected void buildDialogLayout() {
        setSizeFull();

        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);
        mainLayout.setWidth("100%");

        mainLayout.addComponent(createTextField(ctx.tr("FilesToParliamentVaadinDialog.endpointURL"), endpointURL));
        selectRdfFormat = new NativeSelect(ctx.tr("FilesToParliamentVaadinDialog.dialog.format"));
        for (RDFFormat item : supportedRdfFormats) {
            selectRdfFormat.addItem(item);
            selectRdfFormat.setItemCaption(item, item.getName());
        }
        selectRdfFormat.setNullSelectionAllowed(false);
        selectRdfFormat.setImmediate(true);
        mainLayout.addComponent(selectRdfFormat);
        
        final TextField targerGraphNameTextField = createTextField(ctx.tr("FilesToParliamentVaadinDialog.targetGraphName"), targetGraphName);
        final CheckBox perGraphCheckbox = new CheckBox(ctx.tr("FilesToParliamentVaadinDialog.perGraph"), perGraph);
        perGraphCheckbox.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 60440618645464919L;

            @Override
            public void valueChange(ValueChangeEvent event) {
                targerGraphNameTextField.setEnabled(!perGraphCheckbox.getValue());
            }
        });
        mainLayout.addComponent(new CheckBox(ctx.tr("FilesToParliamentVaadinDialog.clearDestinationGraph"), clearDestinationGraph));
        
        mainLayout.addComponent(perGraphCheckbox);
        mainLayout.addComponent(targerGraphNameTextField);
        setCompositionRoot(mainLayout);
    }

    @Override
    protected FilesToParliamentConfig_V1 getConfiguration() throws DPUConfigException {
        FilesToParliamentConfig_V1 result = new FilesToParliamentConfig_V1();
        result.setEndpointURL(endpointURL.getValue());
        final RDFFormat format = (RDFFormat) selectRdfFormat.getValue();
        result.setRdfFileFormat(format.getName());
        if (perGraph.getValue()) {
            result.setTargetGraphName("");
        } else {
            result.setTargetGraphName(targetGraphName.getValue());
        }
        result.setClearDestinationGraph(clearDestinationGraph.getValue());

        return result;
    }

    @Override
    protected void setConfiguration(FilesToParliamentConfig_V1 config) throws DPUConfigException {
        endpointURL.setValue(config.getEndpointURL());
        String format = config.getRdfFileFormat();
        if (auto.getName().equals(format)) {
            selectRdfFormat.setValue(auto);    
        } else {
            selectRdfFormat.setValue(RDFFormat.valueOf(format));    
        }        
        clearDestinationGraph.setValue(config.isClearDestinationGraph());
        perGraph.setValue(StringUtils.isEmpty(config.getTargetGraphName()));
        targetGraphName.setValue(config.getTargetGraphName());
    }

    private <T> TextField createTextField(String caption, ObjectProperty<T> property) {
        final TextField result = new TextField(caption, property);
        result.setWidth("100%");
        return result;
    }
}
