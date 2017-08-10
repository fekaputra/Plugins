package eu.unifiedviews.plugins.extractor.filesdownload;

import com.vaadin.data.Container;
import com.vaadin.data.Validator;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.Table.ColumnHeaderMode;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.provider.UriParser;

import java.net.URI;
import java.util.*;

@SuppressWarnings("serial")
public class FilesDownloadVaadinDialog extends AbstractDialog<FilesDownloadConfig_V1> {

    private final Container container = new BeanItemContainer<>(VfsFile.class);

    private ObjectProperty<Integer> defaultTimeout = new ObjectProperty<Integer>(0);
    private ObjectProperty<Integer> waitBeetweenCallsMs = new ObjectProperty<Integer>(0);

    private ObjectProperty<Boolean> ignoreTlsErrors = new ObjectProperty<Boolean>(Boolean.FALSE);

    TextField txtDefaultTimeout;
    TextField txtWaitBetweenCallsMs;

    private CheckBox chkSoftFail;
    private CheckBox chkCheckDuplicates;

    public FilesDownloadVaadinDialog() {
        super(FilesDownload.class);
    }

    @Override
    protected void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setHeight("-1px");
        mainLayout.setWidth("100%");
        mainLayout.setImmediate(false);
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);

        final Button addVfsFile = new Button("+");
        addVfsFile.addClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                container.addItem(new VfsFile());
            }

        });

        final Table table = new Table();
        table.addGeneratedColumn("remove", new ColumnGenerator() {

            @Override
            public Object generateCell(Table source, Object itemId, Object columnId) {
                Button result = new Button("-");
                final Object itemIdFinal = itemId;

                result.addClickListener(new ClickListener() {

                    @Override
                    public void buttonClick(ClickEvent event) {
                        container.removeItem(itemIdFinal);
                    }

                });

                return result;
            }

        });
        table.setContainerDataSource(container);
        table.setColumnHeaderMode(ColumnHeaderMode.EXPLICIT);
        table.setColumnHeader("uri", ctx.tr("FilesDownloadVaadinDialog.uri"));
        table.setColumnHeader("username", ctx.tr("FilesDownloadVaadinDialog.username"));
        table.setColumnHeader("password", ctx.tr("FilesDownloadVaadinDialog.password"));
        table.setColumnHeader("fileName", ctx.tr("FilesDownloadVaadinDialog.fileName"));
        table.setEditable(true);
        table.setSizeFull();
        table.setTableFieldFactory(new TableFieldFactory() {

            @Override
            public Field<?> createField(Container container, Object itemId, Object propertyId, Component uiContext) {
                AbstractTextField result = new TextField();

                if (propertyId.equals("uri")) {
                    result.setDescription(ctx.tr("FilesDownloadVaadinDialog.uri.description"));
                } else if (propertyId.equals("password")) {
                    result = new PasswordField();
                } else if (propertyId.equals("fileName")) {
                    result.setDescription(ctx.tr("FilesDownloadVaadinDialog.fileName.description"));
                }

                result.setWidth("100%");

                return result;
            }

        });
        table.setVisibleColumns("remove", "uri", "username", "password", "fileName");
        mainLayout.addComponent(addVfsFile);
        addVfsFile.setClickShortcut(KeyCode.INSERT);
        addVfsFile.setDescription(ctx.tr("FilesDownloadVaadinDialog.addButton.description"));
        mainLayout.addComponent(table);
        mainLayout.setExpandRatio(addVfsFile, 0.0f);

        txtDefaultTimeout = new TextField(ctx.tr("FilesDownloadVaadinDialog.defaultTimeout.caption"), defaultTimeout);
        txtDefaultTimeout.setNullRepresentation("");
        txtDefaultTimeout.setConversionError(ctx.tr("FilesDownloadVaadinDialog.defaultTimeout.conversionError"));
        txtDefaultTimeout.setImmediate(true);
        txtDefaultTimeout.setLocale(ctx.getDialogMasterContext().getDialogContext().getLocale());
        txtDefaultTimeout.addValidator(new Validator() {

            @Override
            public void validate(Object value) throws InvalidValueException {
                if (value != null) {
                    if (value instanceof Integer) {
                        if (((Integer) value) < 0) {
                            throw new InvalidValueException(ctx.tr("FilesDownloadVaadinDialog.defaultTimeout.nonnegative"));
                        }
                    }
                }
            }
        });

        HorizontalLayout bottomLayout = new HorizontalLayout();
        bottomLayout.setWidth("100%");
        bottomLayout.addComponent(txtDefaultTimeout);

        CheckBox chkIgnoreTlsErrors = new CheckBox(ctx.tr("FilesDownloadVaadinDialog.ignoreTlsErrors.caption"), ignoreTlsErrors);
        chkIgnoreTlsErrors.setDescription(ctx.tr("FilesDownloadVaadinDialog.ignoreTlsErrors.description"));
        bottomLayout.addComponent(chkIgnoreTlsErrors);

        chkSoftFail = new CheckBox(ctx.tr("FilesDownloadVaadinDialog.softFail.caption"));
        chkSoftFail.setDescription(ctx.tr("FilesDownloadVaadinDialog.softFail.description"));
        bottomLayout.addComponent(chkSoftFail);

        chkCheckDuplicates = new CheckBox(ctx.tr("FilesDownloadVaadinDialog.chkCheckDuplicates.caption"));
        chkCheckDuplicates.setDescription(ctx.tr("FilesDownloadVaadinDialog.chkCheckDuplicates.description"));
        bottomLayout.addComponent(chkCheckDuplicates);

        txtWaitBetweenCallsMs = new TextField(ctx.tr("FilesDownloadVaadinDialog.waitBetweenCallsMs.caption"), waitBeetweenCallsMs);
        txtWaitBetweenCallsMs.setNullRepresentation("");
        txtWaitBetweenCallsMs.setConversionError(ctx.tr("FilesDownloadVaadinDialog.defaultTimeout.conversionError"));
        txtWaitBetweenCallsMs.setImmediate(true);
        txtWaitBetweenCallsMs.setLocale(ctx.getDialogMasterContext().getDialogContext().getLocale());
        txtWaitBetweenCallsMs.addValidator(new Validator() {

            @Override
            public void validate(Object value) throws InvalidValueException {
                if (value != null) {
                    if (value instanceof Integer) {
                        if (((Integer) value) < 0) {
                            throw new InvalidValueException(ctx.tr("FilesDownloadVaadinDialog.defaultTimeout.nonnegative"));
                        }
                    }
                } else {
                    throw new InvalidValueException(ctx.tr("FilesDownloadVaadinDialog.defaultTimeout.nonnegative"));
                }
            }
        });
        bottomLayout.addComponent(txtWaitBetweenCallsMs);

        mainLayout.addComponent(bottomLayout);

        setCompositionRoot(mainLayout);
    }

    private boolean checkURIProtocolSupported(String uri) {
        Map<String, String> environment = this.ctx.getDialogMasterContext().getDialogContext().getEnvironment();
        String supportedProtocols = environment.get(FilesDownload.SUPPORTED_PROTOCOLS);
        if (StringUtils.isEmpty(supportedProtocols)) {
            return true;
        }

        final String scheme = UriParser.extractScheme(uri);
        String[] supportedSchemes = supportedProtocols.trim().split(",");
        Set<String> supportedSet = new HashSet<>();
        for (String s : supportedSchemes) {
            supportedSet.add(s);
        }

        if (StringUtils.isEmpty(scheme) && !supportedSet.contains("file")) {
            return false;
        }

        return supportedSet.contains(scheme);
    }

    private List<String> getSupportedProtocols() {
        Map<String, String> environment = this.ctx.getDialogMasterContext().getDialogContext().getEnvironment();
        String supportedProtocols = environment.get(FilesDownload.SUPPORTED_PROTOCOLS);
        if (StringUtils.isEmpty(supportedProtocols)) {
            return null;
        }

        String[] supportedProtocolsArray = supportedProtocols.trim().split(",");
        List<String> protocols = new ArrayList<>();
        for (String protocol : supportedProtocolsArray) {
            protocols.add(protocol);
        }

        return protocols;
    }

    @Override
    protected FilesDownloadConfig_V1 getConfiguration() throws DPUConfigException {
        List<VfsFile> vfsFiles = new ArrayList<>();

        if (isContainerValid(true)) {
            try {
                for (Object itemId : container.getItemIds()) {
                    VfsFile vfsFile = new VfsFile((VfsFile) itemId);
                    URI uri = new URI(URIUtil.encodePathQuery(URIUtil.decode(vfsFile.getUri(), "utf8"), "utf8"));

                    vfsFile.setUri(uri.toString());

                    vfsFiles.add(vfsFile);
                }
            } catch (Exception e) {
                throw new DPUConfigException(ctx.tr("FilesDownloadVaadinDialog.getConfiguration.exception"), e);
            }
        }

        FilesDownloadConfig_V1 result = new FilesDownloadConfig_V1();
        result.setVfsFiles(vfsFiles);
        if (!txtDefaultTimeout.isValid()) {
            throw new DPUConfigException(ctx.tr("FilesDownloadVaadinDialog.getConfiguration.invalid"));
        }
        result.setDefaultTimeout(defaultTimeout.getValue());
        result.setIgnoreTlsErrors(ignoreTlsErrors.getValue());
        result.setSoftFail(chkSoftFail.getValue());
        result.setCheckForDuplicatedInputFiles(chkCheckDuplicates.getValue());
        if (!txtWaitBetweenCallsMs.isValid()) {
            throw new DPUConfigException(ctx.tr("FilesDownloadVaadinDialog.getConfiguration.invalid"));
        }
        result.setWaitBetweenCallsMs(waitBeetweenCallsMs.getValue());
        return result;
    }

    private boolean isContainerValid(boolean throwException) throws DPUConfigException {
        boolean result = true;
        DPUConfigException resultException = null;

        try {
            for (Object itemId : container.getItemIds()) {
                VfsFile vfsFile = (VfsFile) itemId;

                if (StringUtils.isBlank(vfsFile.getUri())) {
                    result = false;
                    resultException = new DPUConfigException(ctx.tr("FilesDownloadVaadinDialog.uri.required"));
                    break;
                } else if (StringUtils.isBlank(vfsFile.getUsername()) && StringUtils.isNotBlank(vfsFile.getPassword())) {
                    result = false;
                    resultException = new DPUConfigException(ctx.tr("FilesDownloadVaadinDialog.username.required"));
                    break;
                } else if (StringUtils.isNotBlank(vfsFile.getUsername()) && StringUtils.isBlank(vfsFile.getPassword())) {
                    result = false;
                    resultException = new DPUConfigException(ctx.tr("FilesDownloadVaadinDialog.password.required"));
                    break;
                }

                if (!checkURIProtocolSupported(vfsFile.getUri())) {
                    result = false;
                    resultException = new DPUConfigException(this.ctx.tr("FilesDownloadVaadinDialog.protocol.not.supported",
                            vfsFile.getUri(), getSupportedProtocols()));
                    break;
                }

                URI uri = new URI(URIUtil.encodePathQuery(vfsFile.getUri(), "utf8"));

                if (StringUtils.isNotBlank(vfsFile.getUsername()) && StringUtils.isBlank(uri.getHost())) {
                    result = false;
                    resultException = new DPUConfigException(ctx.tr("FilesDownloadVaadinDialog.uri.invalid"));
                    break;
                }
            }
        } catch (Exception e) {
            result = false;
            resultException = new DPUConfigException(ctx.tr("FilesDownloadVaadinDialog.uri.invalid"), e);
        }

        if (throwException && resultException != null) {
            throw resultException;
        }

        return result;
    }

    @Override
    protected void setConfiguration(FilesDownloadConfig_V1 config) throws DPUConfigException {
        if (isContainerValid(false)) {
            try {
                container.removeAllItems();

                for (VfsFile vfsFile : config.getVfsFiles()) {
                    VfsFile vfsFileInContainer = new VfsFile(vfsFile);
                    vfsFileInContainer.setUri(URIUtil.decode(vfsFile.getUri(), "utf8"));

                    container.addItem(vfsFileInContainer);
                }
            } catch (Exception e) {
                throw new DPUConfigException(ctx.tr("FilesDownloadVaadinDialog.setConfiguration.exception"), e);
            }
        }
        defaultTimeout.setValue(config.getDefaultTimeout());
        ignoreTlsErrors.setValue(config.isIgnoreTlsErrors());
        chkSoftFail.setValue(config.isSoftFail());
        chkCheckDuplicates.setValue(config.isCheckForDuplicatedInputFiles());
        waitBeetweenCallsMs.setValue(config.getWaitBetweenCallsMs());
    }

    @Override
    public String getDescription() {
        return ctx.tr("FilesDownloadVaadinDialog.getDescription", new Object[] { container.getItemIds().size() });
    }

}
