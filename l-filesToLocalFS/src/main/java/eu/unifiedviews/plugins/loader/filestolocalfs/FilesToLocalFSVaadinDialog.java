package eu.unifiedviews.plugins.loader.filestolocalfs;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextField;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.config.BaseConfigDialog;

/**
 * DPU's configuration dialog. User can use this dialog to configure DPU
 * configuration.
 */
public class FilesToLocalFSVaadinDialog extends BaseConfigDialog<FilesToLocalFSConfig_V1> {

    private static final long serialVersionUID = -5668436075836909428L;

    private ObjectProperty<String> destination = new ObjectProperty<String>("");

    private ObjectProperty<Boolean> moveFiles = new ObjectProperty<Boolean>(false);

    private ObjectProperty<Boolean> replaceExisting = new ObjectProperty<Boolean>(false);

    private ObjectProperty<Boolean> skipOnError = new ObjectProperty<Boolean>(false);

    public FilesToLocalFSVaadinDialog() {
        super(FilesToLocalFSConfig_V1.class);

    }

    @Override
    public void initialize() {
        FormLayout mainLayout = new FormLayout();

        // top-level component properties
        setWidth("100%");
        setHeight("100%");

        TextField txtPath = new TextField(messages.getString("dialog.tlfs.destination"), destination);
        txtPath.setWidth("100%");

        mainLayout.addComponent(txtPath);
        mainLayout.addComponent(new CheckBox(messages.getString("dialog.tlfs.move"), moveFiles));
        mainLayout.addComponent(new CheckBox(messages.getString("dialog.tlfs.replace"), replaceExisting));
        mainLayout.addComponent(new CheckBox(messages.getString("dialog.tlfs.skip"), skipOnError));

        setCompositionRoot(mainLayout);
    }

    @Override
    public void setConfiguration(FilesToLocalFSConfig_V1 conf)
            throws DPUConfigException {

        destination.setValue(conf.getDestination());
        moveFiles.setValue(conf.isMoveFiles());
        replaceExisting.setValue(conf.isReplaceExisting());
        skipOnError.setValue(conf.isSkipOnError());
    }

    @Override
    public FilesToLocalFSConfig_V1 getConfiguration()
            throws DPUConfigException {
        FilesToLocalFSConfig_V1 conf = new FilesToLocalFSConfig_V1();
        conf.setDestination(destination.getValue());
        conf.setMoveFiles(moveFiles.getValue());
        conf.setReplaceExisting(replaceExisting.getValue());
        conf.setSkipOnError(skipOnError.getValue());
        return conf;
    }

    @Override
    public String getDescription() {
        return destination.getValue();
    }

}
