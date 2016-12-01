package eu.unifiedviews.plugins.extractor.httprequest;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;
import eu.unifiedviews.plugins.extractor.httprequest.HttpRequestConfig_V1.DataType;
import eu.unifiedviews.plugins.extractor.httprequest.HttpRequestConfig_V1.RequestType;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Main DPU configuration dialog
 */
public class HttpRequestVaadinDialog extends AbstractDialog<HttpRequestConfig_V1> {

    private static final long serialVersionUID = 1L;

    private VerticalLayout mainLayout;

    private TextField urlField;

    private TextField fileNameField;

    private TextField userNameField;

    private PasswordField passwordField;

    private TextArea rawDataArea;

    private Table formDataTable;

    private CheckBox basicAuthenticationChckbox;

    private NativeSelect requestMethodSelect;

    private NativeSelect contentTypeSelect;

    private NativeSelect charsetSelect;

    private NativeSelect dataTypeSelect;

    private VerticalLayout requestDataLayout;

    private VerticalLayout formDataLayout;

    private Button testButton;

    public HttpRequestVaadinDialog() {
        super(HttpRequest.class);
    }

    @Override
    protected void buildDialogLayout() {
        setWidth("100%");
        setHeight("100%");

        this.mainLayout = new VerticalLayout();
        this.mainLayout.setWidth("100%");
        this.mainLayout.setHeight("-1px");
        this.mainLayout.setSpacing(true);
        this.mainLayout.setMargin(true);

        HorizontalLayout urlLayout = new HorizontalLayout();
        urlLayout.setSpacing(true);

        this.requestMethodSelect = new NativeSelect();
        this.requestMethodSelect.setCaption(this.ctx.tr("dialog.request.type"));
        this.requestMethodSelect.addItems(Arrays.asList(RequestType.values()));
        this.requestMethodSelect.setNullSelectionAllowed(false);
        this.requestMethodSelect.select(RequestType.GET);
        this.requestMethodSelect.setWidth("100px");
        this.requestMethodSelect.setHeight("30px");
        this.requestMethodSelect.addValueChangeListener(createValueChangeListenerForRequestMethod());
        urlLayout.addComponent(this.requestMethodSelect);

        this.urlField = new TextField();
        this.urlField.setCaption(this.ctx.tr("dialog.url"));
        this.urlField.setWidth("600px");
        this.urlField.setHeight("40px");
        this.urlField.setInputPrompt(this.ctx.tr("dialog.url.prompt"));
        this.urlField.setRequired(true);
        urlLayout.addComponent(this.urlField);

        this.testButton = new Button();
        this.testButton.setCaption(this.ctx.tr("dialog.test"));
        this.testButton.setDescription(this.ctx.tr("dialog.test.description"));
        this.testButton.addClickListener(createTestClickListener());
        urlLayout.addComponent(this.testButton);
        urlLayout.setComponentAlignment(this.testButton, Alignment.MIDDLE_CENTER);

        this.mainLayout.addComponent(urlLayout);

        this.fileNameField = new TextField();
        this.fileNameField.setRequired(true);
        this.fileNameField.setCaption(this.ctx.tr("dialog.file.name"));
        this.fileNameField.setDescription(this.ctx.tr("dialog.file.name.description"));
        this.fileNameField.setWidth("50%");
        this.mainLayout.addComponent(this.fileNameField);

        buildAuthenticationDialog();
        buildDataLayout();

        setCompositionRoot(this.mainLayout);
    }

    /**
     * Creates ValueChangeListener for HTTP method selection; When method is POST, additional
     * configuration options are shown
     */
    private ValueChangeListener createValueChangeListenerForRequestMethod() {
        ValueChangeListener listener = new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("unqualified-field-access")
            @Override
            public void valueChange(ValueChangeEvent event) {
                RequestType newRequestType = (RequestType) event.getProperty().getValue();
                switch (newRequestType) {
                    case GET:
                        hideComponents(requestDataLayout);
                        break;
                    case POST:
                        showComponents(requestDataLayout);
                        break;
                    default:
                        break;
                }
            }
        };

        return listener;
    }

    /**
     * Builds authentication part of configuration dialog: user name and password fields, use authentication check box
     */
    private void buildAuthenticationDialog() {
        final HorizontalLayout authLayout = new HorizontalLayout();
        authLayout.setSpacing(true);
        authLayout.setVisible(false);

        this.userNameField = new TextField();
        this.userNameField.setCaption(this.ctx.tr("dialog.authentication.user"));
        this.userNameField.setNullRepresentation("");
        this.userNameField.setWidth("250px");
        authLayout.addComponent(this.userNameField);

        this.passwordField = new PasswordField();
        this.passwordField.setCaption(this.ctx.tr("dialog.authentication.password"));
        this.passwordField.setNullRepresentation("");
        this.passwordField.setWidth("250px");
        authLayout.addComponent(this.passwordField);

        this.basicAuthenticationChckbox = new CheckBox();
        this.basicAuthenticationChckbox.setCaption(this.ctx.tr("dialog.authentication"));
        this.basicAuthenticationChckbox.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("unqualified-field-access")
            @Override
            public void valueChange(ValueChangeEvent event) {
                authLayout.setVisible(basicAuthenticationChckbox.getValue());
            }
        });

        this.mainLayout.addComponent(this.basicAuthenticationChckbox);
        this.mainLayout.addComponent(authLayout);
    }

    /**
     * Builds data input part of configuration dialog
     */
    private void buildDataLayout() {
        this.requestDataLayout = new VerticalLayout();
        this.requestDataLayout.setSpacing(true);
        this.requestDataLayout.setWidth("100%");
        this.requestDataLayout.setVisible(false);

        final HorizontalLayout horizontalMenu = new HorizontalLayout();
        horizontalMenu.setSpacing(true);

        this.dataTypeSelect = new NativeSelect(this.ctx.tr("dialog.data.type"), Arrays.asList(DataType.values()));
        this.dataTypeSelect.setMultiSelect(false);
        this.dataTypeSelect.setWidth("150px");
        this.dataTypeSelect.setItemCaption(DataType.FORM_DATA, this.ctx.tr("dialog.data.type.form"));
        this.dataTypeSelect.setItemCaption(DataType.RAW_DATA, this.ctx.tr("dialog.data.type.raw"));
        this.dataTypeSelect.setItemCaption(DataType.FILE, this.ctx.tr("dialog.data.type.file"));
        this.dataTypeSelect.select(DataType.RAW_DATA);
        this.dataTypeSelect.setNullSelectionAllowed(false);
        this.dataTypeSelect.addValueChangeListener(createValueChangeListenerForPostDataType());
        horizontalMenu.addComponent(this.dataTypeSelect);

        this.contentTypeSelect = new NativeSelect();
        this.contentTypeSelect.setCaption(this.ctx.tr("dialog.content.type"));
        this.contentTypeSelect.addItems(Arrays.asList(RequestContentType.values()));
        this.contentTypeSelect.select(RequestContentType.TEXT);
        this.contentTypeSelect.setNullSelectionAllowed(false);
        setContentTypesDescription();
        horizontalMenu.addComponent(this.contentTypeSelect);

        this.charsetSelect = new NativeSelect();
        this.charsetSelect.setCaption(this.ctx.tr("dialog.charset.select"));
        this.charsetSelect.setNullSelectionAllowed(false);
        this.charsetSelect.addItems(Arrays.asList(HttpRequest.CHARSETS));
        this.charsetSelect.select("UTF-8");
        horizontalMenu.addComponent(this.charsetSelect);

        this.rawDataArea = new TextArea();
        this.rawDataArea.setWidth("65%");
        this.rawDataArea.setHeight("350px");
        this.rawDataArea.setInputPrompt(this.ctx.tr("dialog.data.raw.prompt"));
        buildFormDataTable();

        this.requestDataLayout.addComponent(horizontalMenu);
        this.requestDataLayout.addComponent(this.rawDataArea);
        this.requestDataLayout.addComponent(this.formDataLayout);

        this.mainLayout.addComponent(this.requestDataLayout);
    }

    /**
     * Create ValueChangeListener for POST data type select.
     * For each data type different configuration options are shown
     */
    private ValueChangeListener createValueChangeListenerForPostDataType() {
        ValueChangeListener listener = new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("unqualified-field-access")
            @Override
            public void valueChange(ValueChangeEvent event) {
                DataType newDataType = (DataType) event.getProperty().getValue();
                switch (newDataType) {
                    case RAW_DATA:
                        showComponents(contentTypeSelect, charsetSelect, rawDataArea, testButton);
                        hideComponents(formDataLayout);
                        fileNameField.setCaption(ctx.tr("dialog.file.name"));
                        fileNameField.setDescription(ctx.tr("dialog.file.name.description"));
                        break;
                    case FORM_DATA:
                        showComponents(formDataLayout, testButton);
                        hideComponents(contentTypeSelect, charsetSelect, rawDataArea);
                        fileNameField.setCaption(ctx.tr("dialog.file.name"));
                        fileNameField.setDescription(ctx.tr("dialog.file.name.description"));
                        break;
                    case FILE:
                        hideComponents(formDataLayout, rawDataArea, testButton);
                        fileNameField.setCaption(ctx.tr("dialog.file.name.file"));
                        fileNameField.setDescription(ctx.tr("dialog.file.name.file.description"));
                    default:
                        break;
                }
            }
        };

        return listener;
    }

    /**
     * Set pretty names for HTTP content types
     */
    private void setContentTypesDescription() {
        this.contentTypeSelect.setItemCaption(RequestContentType.TEXT_DEFAULT, "Text");
        this.contentTypeSelect.setItemCaption(RequestContentType.TEXT, "Text (text/plain)");
        this.contentTypeSelect.setItemCaption(RequestContentType.TEXT_XML, "XML (text/xml)");
        this.contentTypeSelect.setItemCaption(RequestContentType.APPLICATION_XML, "XML (application/xml)");
        this.contentTypeSelect.setItemCaption(RequestContentType.JSON, "JSON (application/json)");
        this.contentTypeSelect.setItemCaption(RequestContentType.TEXT_HTML, "HTML (text/html)");
        this.contentTypeSelect.setItemCaption(RequestContentType.SOAP, "SOAP (application/soap+xml)");
    }

    /**
     * Build table for multipart data input
     * Editable table with two columns: key, value
     */
    private void buildFormDataTable() {
        this.formDataLayout = new VerticalLayout();
        this.formDataLayout.setSpacing(true);
        this.formDataLayout.setVisible(false);

        this.formDataTable = new Table(this.ctx.tr("dialog.form.table.label"));
        this.formDataTable.setPageLength(6);
        this.formDataTable.addContainerProperty("key", String.class, null);
        this.formDataTable.addContainerProperty("value", String.class, null);
        this.formDataTable.setColumnHeaders(this.ctx.tr("dialog.form.table.key"), this.ctx.tr("dialog.form.table.value"));
        this.formDataTable.setImmediate(true);
        this.formDataTable.setEditable(true);
        this.formDataTable.setWidth("60%");

        this.formDataLayout.addComponent(this.formDataTable);

        final HorizontalLayout tableButtonsLayout = new HorizontalLayout();
        tableButtonsLayout.setSpacing(true);
        Button addRowButton = new Button("+");
        addRowButton.addClickListener(new Button.ClickListener() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("unqualified-field-access")
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                formDataTable.addItem(new Object[] { "", "" }, null);
            }
        });
        tableButtonsLayout.addComponent(addRowButton);

        Button removeRowButton = new Button("-");
        removeRowButton.addClickListener(new Button.ClickListener() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("unqualified-field-access")
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                if (formDataTable.size() > 1) {
                    HttpRequestVaadinDialog.this.formDataTable.removeItem(HttpRequestVaadinDialog.this.formDataTable.lastItemId());
                }
            }
        });
        tableButtonsLayout.addComponent(removeRowButton);
        this.formDataLayout.addComponent(tableButtonsLayout);
    }

    /**
     * Create click listener for test button
     * When clicked, HTTP request is executed and result is shown in pop up window
     * Available only for raw and multipart data
     */
    private ClickListener createTestClickListener() {
        ClickListener listener = new ClickListener() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("unqualified-field-access")
            @Override
            public void buttonClick(ClickEvent event) {
                try {
                    ResponsePreviewWindow preview = new ResponsePreviewWindow(ctx, getConfiguration());
                    UI.getCurrent().addWindow(preview);
                    preview.focus();
                } catch (DPUConfigException e) {
                    // nothing to do
                }
            }
        };

        return listener;
    }

    @Override
    protected void setConfiguration(HttpRequestConfig_V1 config) throws DPUConfigException {
        this.urlField.setValue(config.getRequestURL());
        this.requestMethodSelect.setValue(config.getRequestType());
        this.basicAuthenticationChckbox.setValue(config.isUseAuthentication());
        this.userNameField.setValue(config.getUserName());
        this.passwordField.setValue(config.getPassword());
        this.fileNameField.setValue(config.getFileName());

        if (config.getRequestType() == RequestType.POST) {
            this.dataTypeSelect.select(config.getPostRequestDataType());

            if (config.getPostRequestDataType() == DataType.RAW_DATA) {
                this.contentTypeSelect.select(config.getContentType());
                this.charsetSelect.select(config.getCharset());
                this.rawDataArea.setValue(config.getRawRequestBody());
            } else if (config.getPostRequestDataType() == DataType.FILE) {
                this.contentTypeSelect.select(config.getContentType());
                this.charsetSelect.select(config.getCharset());
            } else if (config.getPostRequestDataType() == DataType.FORM_DATA) {
                for (String key : config.getFormDataRequestBody().keySet()) {
                    this.formDataTable.addItem(new Object[] { key, config.getFormDataRequestBody().get(key) }, null);
                }
            }
        }
    }

    //TODO: dokoncit kontrolu konfiguracie
    @SuppressWarnings("rawtypes")
    @Override
    protected HttpRequestConfig_V1 getConfiguration() throws DPUConfigException {
        HttpRequestConfig_V1 config = new HttpRequestConfig_V1();
        config.setRequestURL(this.urlField.getValue());
        config.setFileName(this.fileNameField.getValue());

        checkAuthenticationConfiguration();
        config.setUseAuthentication(this.basicAuthenticationChckbox.getValue());
        config.setUserName(this.userNameField.getValue());
        config.setPassword(this.passwordField.getValue());
        config.setRequestType((RequestType) this.requestMethodSelect.getValue());

        if (config.getRequestType() == RequestType.POST) {
            config.setPostRequestDataType((DataType) this.dataTypeSelect.getValue());
            if (config.getPostRequestDataType() == DataType.RAW_DATA) {
                checkRawDataConfiguration();
                config.setCharset((String) this.charsetSelect.getValue());
                config.setContentType((RequestContentType) this.contentTypeSelect.getValue());
                config.setRawRequestBody(this.rawDataArea.getValue());
            } else if (config.getPostRequestDataType() == DataType.FILE) {
                config.setCharset((String) this.charsetSelect.getValue());
                config.setContentType((RequestContentType) this.contentTypeSelect.getValue());
            } else if (config.getPostRequestDataType() == DataType.FORM_DATA) {
                checkFormDataConfiguration();
                Map<String, String> formData = new HashMap<>();
                for (Iterator i = this.formDataTable.getItemIds().iterator(); i.hasNext();) {
                    Integer id = (Integer) i.next();
                    formData.put((String) this.formDataTable.getContainerProperty(id, "key").getValue(),
                            (String) this.formDataTable.getContainerProperty(id, "value").getValue());
                }
                config.setFormDataRequestBody(formData);
            }
        }

        return config;
    }

    private void checkFormDataConfiguration() throws DPUConfigException {
        // TODO Auto-generated method stub

    }

    private void checkRawDataConfiguration() throws DPUConfigException {
        // TODO Auto-generated method stub

    }

    private void checkAuthenticationConfiguration() throws DPUConfigException {
        if (this.basicAuthenticationChckbox.getValue()) {
            if (StringUtils.isEmpty(this.userNameField.getValue()) || StringUtils.isEmpty(this.passwordField.getValue())) {
                throw new DPUConfigException("dialog.errors.authentication");
            }
        }
    }

    private static void showComponents(Component... components) {
        for (Component c : components) {
            c.setVisible(true);
        }
    }

    private static void hideComponents(Component... components) {
        for (Component c : components) {
            c.setVisible(false);
        }
    }

}
