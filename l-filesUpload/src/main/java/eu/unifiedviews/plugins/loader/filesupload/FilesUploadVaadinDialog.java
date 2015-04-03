package eu.unifiedviews.plugins.loader.filesupload;

import java.net.URI;

import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.lang3.StringUtils;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

@SuppressWarnings("serial")
public class FilesUploadVaadinDialog extends AbstractDialog<FilesUploadConfig_V1> {

    private TextField uri;

    private TextField username;

    private PasswordField password;

    private CheckBox softFail;

    private CheckBox moveFiles;

    public FilesUploadVaadinDialog() {
        super(FilesUpload.class);
    }

    @Override
    protected void buildDialogLayout() {
        setSizeFull();

        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);
        mainLayout.setWidth("100%");

        uri = new TextField(ctx.tr("FilesUploadVaadinDialog.uri"));
        uri.setDescription(ctx.tr("FilesUploadVaadinDialog.uri.description"));
        uri.setRequired(true);
        uri.setRequiredError(ctx.tr("FilesUploadVaadinDialog.uri.required"));
        uri.setWidth("100%");

        mainLayout.addComponent(uri);

        username = new TextField(ctx.tr("FilesUploadVaadinDialog.username"));
        username.setWidth("75%");
        mainLayout.addComponent(username);

        password = new PasswordField(ctx.tr("FilesUploadVaadinDialog.password"));
        password.setWidth("75%");
        mainLayout.addComponent(password);

        softFail = new CheckBox(ctx.tr("FilesUploadVaadinDialog.skip"));
        softFail.setWidth("100%");
        mainLayout.addComponent(softFail);

        moveFiles = new CheckBox(ctx.tr("FilesUploadVaadinDialog.move"));
        moveFiles.setWidth("100%");
        mainLayout.addComponent(moveFiles);

        setCompositionRoot(mainLayout);
    }

    @Override
    protected FilesUploadConfig_V1 getConfiguration() throws DPUConfigException {
        FilesUploadConfig_V1 result = new FilesUploadConfig_V1();

        try {
            if (StringUtils.isBlank(uri.getValue())) {
                throw new DPUConfigException(ctx.tr("FilesUploadVaadinDialog.uri.required"));
            } else if (StringUtils.isBlank(username.getValue()) && StringUtils.isNotBlank(password.getValue())) {
                throw new DPUConfigException(ctx.tr("FilesUploadVaadinDialog.username.required"));
            } else if (StringUtils.isNotBlank(username.getValue()) && StringUtils.isBlank(password.getValue())) {
                throw new DPUConfigException(ctx.tr("FilesUploadVaadinDialog.password.required"));
            }

            URI encodedUri = new URI(URIUtil.encodePathQuery(URIUtil.decode(uri.getValue(), "utf8"), "utf8"));

            if (StringUtils.isNotBlank(username.getValue()) && StringUtils.isBlank(encodedUri.getHost())) {
                throw new DPUConfigException(ctx.tr("FilesUploadVaadinDialog.uri.invalid"));
            }

            result.setUri(encodedUri.toString());
        } catch (DPUConfigException e) {
            throw e;
        } catch (Exception e) {
            throw new DPUConfigException(ctx.tr("FilesUploadVaadinDialog.uri.invalid"));
        }

        result.setUsername(username.getValue());
        result.setPassword(password.getValue());
        result.setSoftFail(softFail.getValue());
        result.setMoveFiles(moveFiles.getValue());

        return result;
    }

    @Override
    protected void setConfiguration(FilesUploadConfig_V1 config) throws DPUConfigException {
        try {
            uri.setValue(URIUtil.decode(config.getUri(), "utf8"));
        } catch (Exception e) {
            throw new DPUConfigException(ctx.tr("FilesUploadVaadinDialog.uri.invalid"), e);
        }

        username.setValue(config.getUsername());
        password.setValue(config.getPassword());
        softFail.setValue(config.isSoftFail());
        moveFiles.setValue(config.isMoveFiles());
    }

    @Override
    public String getDescription() {
        return ctx.tr("FilesUploadVaadinDialog.getDescription", uri.getValue());
    }

}
