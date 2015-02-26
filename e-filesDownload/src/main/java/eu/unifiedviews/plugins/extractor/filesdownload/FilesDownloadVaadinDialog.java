package eu.unifiedviews.plugins.extractor.filesdownload;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.util.CryptorFactory;

import com.vaadin.data.Container;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.Table.ColumnHeaderMode;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.config.BaseConfigDialog;
import eu.unifiedviews.helpers.dpu.config.InitializableConfigDialog;
import eu.unifiedviews.helpers.dpu.localization.Messages;

@SuppressWarnings("serial")
public class FilesDownloadVaadinDialog extends BaseConfigDialog<FilesDownloadConfig_V1> implements InitializableConfigDialog {

    private final Container container = new BeanItemContainer<VfsFile>(VfsFile.class);

    private Messages messages;

    public FilesDownloadVaadinDialog() {
        super(FilesDownloadConfig_V1.class);
    }

    @Override
    public void initialize() {
        messages = new Messages(getContext().getLocale(), getClass().getClassLoader());

        Panel panel = new Panel();
        panel.setContent(buildMainLayout());
        panel.setSizeFull();

        setCompositionRoot(panel);
        setHeight("100%");
        setWidth("100%");
    }

    private VerticalLayout buildMainLayout() {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setHeight("-1px");
        mainLayout.setImmediate(false);
        mainLayout.setMargin(false);
        mainLayout.setSpacing(true);
        mainLayout.setWidth("100%");

        Table table = new Table();
        table.addGeneratedColumn("remove", new ColumnGenerator() {

            @Override
            public Object generateCell(Table source, Object itemId, Object columnId) {
                Button result = new Button("-");
                final Table sourceFinal = source;
                final Object itemIdFinal = itemId;

                result.addClickListener(new ClickListener() {

                    @Override
                    public void buttonClick(ClickEvent event) {
                        if (sourceFinal.size() > 1) {
                            container.removeItem(itemIdFinal);
                        }
                    }

                });

                return result;
            }

        });
        table.setContainerDataSource(container);
        table.setColumnHeaderMode(ColumnHeaderMode.EXPLICIT);
        table.setColumnHeader("uri", messages.getString("FilesDownloadVaadinDialog.uri"));
        table.setColumnHeader("username", messages.getString("FilesDownloadVaadinDialog.username"));
        table.setColumnHeader("password", messages.getString("FilesDownloadVaadinDialog.password"));
        table.setColumnHeader("fileName", messages.getString("FilesDownloadVaadinDialog.fileName"));
        table.setEditable(true);
        table.setHeight("270");
        table.setTableFieldFactory(new TableFieldFactory() {

            @Override
            public Field<?> createField(Container container, Object itemId, Object propertyId, Component uiContext) {
                AbstractTextField result = new TextField();

                if (propertyId.equals("uri")) {
                    result.setDescription(messages.getString("FilesDownloadVaadinDialog.uri.description"));
                    result.setWidth("400");
                } else if (propertyId.equals("password")) {
                    result = new PasswordField();
                } else if (propertyId.equals("fileName")) {
                    result.setDescription(messages.getString("FilesDownloadVaadinDialog.fileName.description"));
                }

                return result;
            }

        });
        table.setVisibleColumns("remove", "uri", "username", "password", "fileName");
        table.setWidth("100%");

        mainLayout.addComponent(table);

        Button addVfsFile = new Button("+");
        addVfsFile.addClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                container.addItem(new VfsFile());
            }

        });

        mainLayout.addComponent(addVfsFile);

        return mainLayout;
    }

    @Override
    protected FilesDownloadConfig_V1 getConfiguration() throws DPUConfigException {
        List<VfsFile> vfsFiles = new ArrayList<>();

        if (isContainerValid(true)) {
            try {
                for (Object itemId : container.getItemIds()) {
                    VfsFile vfsFile = new VfsFile((VfsFile) itemId);
                    URI uri = new URI(URIUtil.encodePathQuery(vfsFile.getUri(), "utf8"));

                    vfsFile.setUri(uri.toString());

                    if (StringUtils.isNotBlank(vfsFile.getPassword())) {
                        vfsFile.setPassword(CryptorFactory.getCryptor().encrypt(vfsFile.getPassword()));
                    }

                    vfsFiles.add(vfsFile);
                }
            } catch (NullPointerException | URISyntaxException | URIException e) {
                throw new DPUConfigException(messages.getString("FilesDownloadVaadinDialog.uri.invalid"), e);
            } catch (Exception e) {
                // Method org.apache.commons.vfs2.util.Cryptor.encrypt(String) throws exception with plain text input in the message.
                throw new DPUConfigException(messages.getString("FilesDownloadVaadinDialog.getConfiguration.exception"));
            }
        }

        FilesDownloadConfig_V1 result = new FilesDownloadConfig_V1();
        result.setVfsFiles(vfsFiles);

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

                    resultException = new DPUConfigException(messages.getString("FilesDownloadVaadinDialog.uri.required"));

                    break;
                } else if (StringUtils.isBlank(vfsFile.getUsername()) && StringUtils.isNotBlank(vfsFile.getPassword())) {
                    result = false;

                    resultException = new DPUConfigException(messages.getString("FilesDownloadVaadinDialog.username.required"));

                    break;
                } else if (StringUtils.isNotBlank(vfsFile.getUsername()) && StringUtils.isBlank(vfsFile.getPassword())) {
                    result = false;

                    resultException = new DPUConfigException(messages.getString("FilesDownloadVaadinDialog.password.required"));

                    break;
                }

                URI uri = new URI(URIUtil.encodePathQuery(vfsFile.getUri(), "utf8"));

                if (StringUtils.isNotBlank(vfsFile.getUsername()) && StringUtils.isBlank(uri.getHost())) {
                    result = false;

                    resultException = new DPUConfigException(messages.getString("FilesDownloadVaadinDialog.uri.invalid"));

                    break;
                }
            }
        } catch (Exception e) {
            result = false;

            resultException = new DPUConfigException(messages.getString("FilesDownloadVaadinDialog.uri.invalid"), e);
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

                    if (StringUtils.isNotBlank(vfsFile.getPassword())) {
                        vfsFileInContainer.setPassword(CryptorFactory.getCryptor().decrypt(vfsFile.getPassword()));
                    }

                    container.addItem(vfsFileInContainer);
                }
            } catch (UnsupportedOperationException | URIException e) {
                throw new DPUConfigException(messages.getString("FilesDownloadVaadinDialog.setConfiguration.exception"), e);
            } catch (Exception e) {
                throw new DPUConfigException(messages.getString("FilesDownloadVaadinDialog.setConfiguration.exception"));
            }
        }
    }

    @Override
    public String getDescription() {
        return messages.getString("FilesDownloadVaadinDialog.getDescription", new Object[] { container.getItemIds().size() });
    }

}
