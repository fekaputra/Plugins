package org.dbpedia.extraction.spark.dialog;

import com.vaadin.data.*;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.*;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import de.steinwedel.messagebox.MessageBox;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;
import org.dbpedia.extraction.spark.SparkPipeline;
import org.dbpedia.extraction.spark.SparkPipelineConfig_V1;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Vaadin configuration dialog for SparkPipeline.
 *
 * @author Unknown
 */
public class SparkPipelineVaadinDialog extends AbstractDialog<SparkPipelineConfig_V1> {

    private SparkPipelineConfig_V1 config_v1;
    private Map<SparkPropertyCategory, Table> tableMap = new HashMap<>();

    private SparkConfigEntry newSparkProperty;
    private SparkConfigEntry newUseCaseProperty;

    private ByteArrayOutputStream uploadedConfig = null;

    private Map<String, AbstractField> validationMap = new HashMap<>();
    final private VerticalLayout mainLayout = new VerticalLayout();

    public SparkPipelineVaadinDialog() throws DPUConfigException {
        super(SparkPipeline.class);
        this.setConfiguration(new SparkPipelineConfig_V1());

        createDefaultNewEntries();
    }

    private void createDefaultNewEntries() {
        this.newSparkProperty = new SparkConfigEntry(SparkPropertyCategory.SparkOptional.toString(), "", config_v1.getSparkConfig().getEmptyEntry(SparkPropertyCategory.SparkOptional));
        this.newUseCaseProperty = new SparkConfigEntry(SparkPropertyCategory.UsecaseOptional.toString(), "", config_v1.getSparkConfig().getEmptyEntry(SparkPropertyCategory.UsecaseOptional));
    }

    /**
     * is necessary to prevent DPUWrapException(Messages.getString("DPURecordWrap.configure"), e) @ cz.cuni.mff.xrg.odcs.frontend.dpu.wrap.DPURecordWrap.configuredDialog()
     * FIXME solve this problem please @SWC
     * @param conf
     * @throws DPUConfigException
     */
    @Override
    public void setConfig(String conf) throws DPUConfigException {
        //FIXME not sure what im doing here :D
    }

    public boolean validate() throws Validator.InvalidValueException {
        return validationMap.values().stream().map(AbstractField::isValid).allMatch(aBoolean -> aBoolean);
    }

    @Override
    public void setConfiguration(SparkPipelineConfig_V1 c) throws DPUConfigException {
        this.config_v1 = c;
    }

    @Override
    public SparkPipelineConfig_V1 getConfiguration() throws DPUConfigException {
        this.config_v1 = new SparkPipelineConfig_V1(this.config_v1.updateSparkConfig());
        return this.config_v1;
    }

    @Override
    public void buildDialogLayout() {

        //for optional item lists -> if no new item entry exists -> add it
        if(this.config_v1.getSparkOptionalEntries().getItemIds().stream().filter(x -> x.getKey().equals("")).count() == 0)
            this.config_v1.getSparkOptionalEntries().addItem(this.config_v1.getSparkConfig().getEmptyEntry(SparkPropertyCategory.SparkOptional));
        if(this.config_v1.getUseCaseOptionalEntries().getItemIds().stream().filter(x -> x.getKey().equals("")).count() == 0)
            this.config_v1.getUseCaseOptionalEntries().addItem(this.config_v1.getSparkConfig().getEmptyEntry(SparkPropertyCategory.UsecaseOptional));

        mainLayout.addComponents(getHeader());

        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);
        mainLayout.addComponent(getFormattedLabel(ctx.tr("SparkPipeline.dialog.label")));
        tableMap.put(SparkPropertyCategory.SparkMandatory, implementNewTable(this.config_v1.getSparkMandatoryEntries(), true));
        mainLayout.addComponent(tableMap.get(SparkPropertyCategory.SparkMandatory));
        mainLayout.addComponent(getFormattedLabel(ctx.tr("SparkPipeline.dialog.label")));
        tableMap.put(SparkPropertyCategory.SparkRecommended, implementNewTable(this.config_v1.getSparkRecommendedEntries(), false));
        mainLayout.addComponent(tableMap.get(SparkPropertyCategory.SparkRecommended));
        mainLayout.addComponent(getFormattedLabel(ctx.tr("SparkPipeline.dialog.label")));
        tableMap.put(SparkPropertyCategory.SparkOptional, implementNewTable(this.config_v1.getSparkOptionalEntries(), false));
        mainLayout.addComponent(tableMap.get(SparkPropertyCategory.SparkOptional));
        mainLayout.addComponent(getFormattedLabel(ctx.tr("SparkPipeline.dialog.label")));
        tableMap.put(SparkPropertyCategory.UsecaseMandatory, implementNewTable(this.config_v1.getUseCaseMandatoryEntries(), true));
        mainLayout.addComponent(tableMap.get(SparkPropertyCategory.UsecaseMandatory));
        mainLayout.addComponent(getFormattedLabel(ctx.tr("SparkPipeline.dialog.label")));
        tableMap.put(SparkPropertyCategory.UsecaseOptional, implementNewTable(this.config_v1.getUseCaseOptionalEntries(), false));
        mainLayout.addComponent(tableMap.get(SparkPropertyCategory.UsecaseOptional));

        setCompositionRoot(mainLayout);
    }

    private void refreshTables(){
        validationMap.clear();
        Table oldTable = tableMap.get(SparkPropertyCategory.SparkMandatory);
        tableMap.put(SparkPropertyCategory.SparkMandatory, implementNewTable(this.config_v1.getSparkMandatoryEntries(), true));
        mainLayout.replaceComponent(oldTable, tableMap.get(SparkPropertyCategory.SparkMandatory));
        oldTable = tableMap.get(SparkPropertyCategory.SparkRecommended);
        tableMap.put(SparkPropertyCategory.SparkRecommended, implementNewTable(this.config_v1.getSparkRecommendedEntries(), false));
        mainLayout.replaceComponent(oldTable, tableMap.get(SparkPropertyCategory.SparkRecommended));
        oldTable = tableMap.get(SparkPropertyCategory.SparkOptional);
        tableMap.put(SparkPropertyCategory.SparkOptional, implementNewTable(this.config_v1.getSparkOptionalEntries(), false));
        mainLayout.replaceComponent(oldTable, tableMap.get(SparkPropertyCategory.SparkOptional));
        oldTable = tableMap.get(SparkPropertyCategory.UsecaseMandatory);
        tableMap.put(SparkPropertyCategory.UsecaseMandatory, implementNewTable(this.config_v1.getUseCaseMandatoryEntries(), true));
        mainLayout.replaceComponent(oldTable, tableMap.get(SparkPropertyCategory.UsecaseMandatory));
        oldTable = tableMap.get(SparkPropertyCategory.UsecaseOptional);
        tableMap.put(SparkPropertyCategory.UsecaseOptional, implementNewTable(this.config_v1.getUseCaseOptionalEntries(), false));
        mainLayout.replaceComponent(oldTable, tableMap.get(SparkPropertyCategory.UsecaseOptional));
    }

    private Component getHeader(){
        HorizontalLayout buttonBar = new HorizontalLayout();
        buttonBar.setStyleName("dpuDetailButtonBar");
        buttonBar.setSpacing(true);
        buttonBar.setWidth("100%");

        Button exportButton = new Button("Export SPARK configuration");
        FileDownloader fileDownloader = new FileDownloader(new StreamResource(
                (StreamResource.StreamSource) () -> {
                    if(validate())
                        return new ByteArrayInputStream(this.config_v1.updateSparkConfig().getSerializedSparkConfig().getBytes());
                    else
                        Notification.show("Validation failed for at least one configuration field!", Notification.Type.ERROR_MESSAGE);
                    return null;
                },
                this.config_v1.getSparkConfig().getAppName() + "-spark.config"));
        fileDownloader.extend(exportButton);

        exportButton.setWidth("200px");
        buttonBar.addComponent(exportButton);
        buttonBar.setExpandRatio(exportButton, 1.0f);
        buttonBar.setComponentAlignment(exportButton, Alignment.MIDDLE_RIGHT);


        Upload uploadConfig = new Upload(null, (Upload.Receiver) (filename, mimeType) -> {
            uploadedConfig = new ByteArrayOutputStream();
            return uploadedConfig;
        });
        uploadConfig.setImmediate(true);
        uploadConfig.setButtonCaption("Import SPARK configuration");
        uploadConfig.addSucceededListener((Upload.SucceededListener) event -> {
            try {
                this.setConfiguration(new SparkPipelineConfig_V1(new SparkDpuConfig(new ByteArrayInputStream(this.uploadedConfig.toByteArray()))));
                refreshTables();
                Notification.show("Upload Successful", "SPARK configuration updated.", Notification.Type.HUMANIZED_MESSAGE);
            } catch (Exception e) {
                Notification.show("Upload Error", "The provided file caused an error: " + e.getMessage(), Notification.Type.ERROR_MESSAGE);
            }
        });

        uploadConfig.setWidth("200px");
        buttonBar.addComponent(uploadConfig);
        buttonBar.setComponentAlignment(uploadConfig, Alignment.MIDDLE_RIGHT);

        return buttonBar;
    }

    private Table implementNewTable(SparkDpuConfig source, boolean required) {
        Table table = new Table();

        table.addGeneratedColumn("addremove", new Table.ColumnGenerator() {
            @Override
            public Object generateCell(final Table source, final Object itemId, final Object columnId) {
                SparkConfigEntry id = ((SparkConfigEntry) itemId);

                //if mandatory table -> return no Button!
                if(id.getSparkPropertyCategory() == SparkPropertyCategory.SparkMandatory || id.getSparkPropertyCategory() == SparkPropertyCategory.UsecaseMandatory)
                    return null;
                //if current item is last item -> add row, else reduce it
                Button result = new Button("".equals(id.getKey()) ? "+" : "-");
                result.addClickListener((Button.ClickListener) event -> {
                    if("".equals(id.getKey())) {
                        if (id.getSparkPropertyCategory() == SparkPropertyCategory.SparkOptional) {
                            //delete empty- (default/add) item
                            table.removeItem(SparkPipelineVaadinDialog.this.config_v1.getSparkConfig().getEmptyEntry(SparkPropertyCategory.SparkOptional));
                            //insert new item
                            table.addItem(SparkPipelineVaadinDialog.this.newSparkProperty);
                            //reinsert empty- (default/add) item as last
                            table.addItem(SparkPipelineVaadinDialog.this.config_v1.getSparkConfig().getEmptyEntry(SparkPropertyCategory.SparkOptional));
                        }
                        else if(id.getSparkPropertyCategory() == SparkPropertyCategory.UsecaseOptional) {
                            table.removeItem(SparkPipelineVaadinDialog.this.config_v1.getSparkConfig().getEmptyEntry(SparkPropertyCategory.UsecaseOptional));
                            table.addItem(SparkPipelineVaadinDialog.this.newUseCaseProperty);
                            table.addItem(SparkPipelineVaadinDialog.this.config_v1.getSparkConfig().getEmptyEntry(SparkPropertyCategory.UsecaseOptional));
                        }
                        id.setValue(new ObjectProperty(""));
                        // now create new dummy entries
                        createDefaultNewEntries();
                    }
                    else    //remove button
                        {
                        MessageBox
                                .createQuestion()
                                .withCaption("Removing " + id.getKey())
                                .withMessage(ctx.tr("SparkPipeline.config.remove") + " " + id.getKey() + (id.getDefaultValue().isEmpty() ? ""
                                        : "\n" + ctx.tr("SparkPipeline.config.remove.defaultValue") + ": " + id.getDefaultValue() + "."))
                                .withYesButton(() -> { table.removeItem(itemId); table.refreshRowCache();})
                                .withNoButton()
                                .open();
                    }
                });
                //result.setWidth("25px");
                return result;
            }
        });

        table.addGeneratedColumn("key", new Table.ColumnGenerator() {
            @Override
            public Object generateCell(final Table source, final Object itemId, final Object columnId) {
                SparkConfigEntry id = ((SparkConfigEntry) itemId);
                TextField result = new TextField();
                result.setStyleName("v-button-caption");
                result.setWidth("100%");
                result.setHeight("25px");
                result.setValue(id.getKey());
                if(!id.getKey().equals("")) //not!
                    result.setReadOnly(true);
                result.addValueChangeListener((Property.ValueChangeListener) event -> {
                    if(result.isValid()) {
                        result.setStyleName("v-button-caption");
                        String newKey = event.getProperty().getValue().toString().trim();
                        SparkConfigEntry def = SparkPipelineVaadinDialog.this.config_v1.getSparkConfig().getDefaultEntry(newKey);
                        SparkConfigEntry newEntry = null;
                        if (id.getSparkPropertyCategory() == SparkPropertyCategory.SparkOptional)
                            newEntry = SparkPipelineVaadinDialog.this.newSparkProperty;
                        else if (id.getSparkPropertyCategory() == SparkPropertyCategory.UsecaseOptional)
                            newEntry = SparkPipelineVaadinDialog.this.newUseCaseProperty;

                        if(def != null)
                            newEntry = new SparkConfigEntry(
                                    event.getProperty().getValue().toString().trim(),
                                    newEntry.getValue().toString(),
                                    def  );
                        else
                            newEntry = new SparkConfigEntry(
                                    event.getProperty().getValue().toString().trim(),
                                    newEntry.getValue().toString(),
                                    "",
                                    id.getSparkPropertyCategory(),
                                    SparkPropertyType.String,
                                    "",
                                    "");

                        if (id.getSparkPropertyCategory() == SparkPropertyCategory.SparkOptional)
                            SparkPipelineVaadinDialog.this.newSparkProperty = newEntry;
                        else if (id.getSparkPropertyCategory() == SparkPropertyCategory.UsecaseOptional)
                            SparkPipelineVaadinDialog.this.newUseCaseProperty = newEntry;
                    }
                    else {
                        result.setStyleName("loginError");
                    }
                });
                if(id.getSparkPropertyCategory() == SparkPropertyCategory.SparkMandatory
                        || id.getSparkPropertyCategory() == SparkPropertyCategory.SparkOptional
                        || id.getSparkPropertyCategory() == SparkPropertyCategory.SparkRecommended)
                    result.addValidator(Validators.SparkKeyValidator);
                if(id.getSparkPropertyCategory() == SparkPropertyCategory.UsecaseMandatory
                        || id.getSparkPropertyCategory() == SparkPropertyCategory.UsecaseOptional
                        || id.getSparkPropertyCategory() == SparkPropertyCategory.UsecaseRecommended)
                    result.addValidator(Validators.GetUseCaseKeyValidator(SparkPipelineVaadinDialog.this.config_v1.getSparkConfig().getAppName()));
                validationMap.put("key." + id.getKey(), result);
                return result;
            }
        });

        table.addGeneratedColumn("value", new Table.ColumnGenerator() {
            @Override
            public Object generateCell(final Table source, final Object itemId, final Object columnId) {
                SparkConfigEntry id = ((SparkConfigEntry) itemId);

                /* create special components for certain types: */
                if(id.getSparkPropertyType() == SparkPropertyType.Boolean)
                    return SparkPipelineVaadinDialog.this.getCheckBox(id);
                if(id.getSparkPropertyType() == SparkPropertyType.Enum)
                    return SparkPipelineVaadinDialog.this.getComboBox(id);

                /* else -> create TextField */
                TextField result = new TextField();
                result.setNullRepresentation("");
                result.setImmediate(true);
                result.setRequired(required);
                result.setHeight("25px");
                result.setWidth("100%");
                if(id.getSparkPropertyType() == SparkPropertyType.Uri)
                    result.setConverter(Converters.StringToUriConverter);
                if(id.getSparkPropertyType() == SparkPropertyType.StringList)
                    result.setConverter(Converters.StringToStringListConverter);
                if(id.getSparkPropertyType().getClazz().equals(Integer.class))
                    result.setConverter(Converters.StringToIntegerConverter);
                result.setPropertyDataSource(id.getValue());
                result.addValueChangeListener((Property.ValueChangeListener) event -> {
                    if(result.isValid()) {
                        result.setStyleName("v-textfield");
                        if (id.getSparkPropertyCategory() == SparkPropertyCategory.SparkOptional
                                || id.getSparkPropertyCategory() == SparkPropertyCategory.SparkRecommended
                                || id.getSparkPropertyCategory() == SparkPropertyCategory.SparkMandatory)
                            SparkPipelineVaadinDialog.this.newSparkProperty.setValue(new ObjectProperty(event.getProperty().getValue()));
                        else if(id.getSparkPropertyCategory() == SparkPropertyCategory.UsecaseRecommended
                                || id.getSparkPropertyCategory() == SparkPropertyCategory.UsecaseMandatory
                                || id.getSparkPropertyCategory() == SparkPropertyCategory.UsecaseOptional){
                            SparkPipelineVaadinDialog.this.newUseCaseProperty.setValue(new ObjectProperty(event.getProperty().getValue()));
                        }
                    }
                    else {
                        //TODO InvalidAllowed? check every entry before export
                        result.setStyleName("loginError");
                    }
                });

                result.addValidator(Validators.GetValueValidator(id));
                validationMap.put("value." + id.getKey(), result);
                return result;
            }
        });

        table.addGeneratedColumn("description", new Table.ColumnGenerator() {
            @Override
            public Object generateCell(final Table source, final Object itemId, final Object columnId) {
                SparkConfigEntry id = ((SparkConfigEntry) itemId);

                Button result = new Button("?");
                result.addClickListener((Button.ClickListener) event -> {
                    MessageBox
                            .createInfo()
                            .withCaption(id.getKey())
                            .withMessage(id.getDescription())
                            .withOkButton()
                            .withWidth("500px")
                            .open();
                });
                //result.setWidth("25px");
                return result;
            }
        });

        table.setContainerDataSource(source);
        table.setColumnHeaderMode(Table.ColumnHeaderMode.EXPLICIT);
        table.setColumnHeader("key", ctx.tr("SparkPipeline.config.key"));
        table.setColumnHeader("value", ctx.tr("SparkPipeline.config.Value"));
        table.setPageLength(source.size());
        table.setVisibleColumns("addremove", "key", "value", "description");
        table.setWidth("100%");
        table.setColumnWidth("addremove", 30);
        table.setColumnWidth("description", 30);
        table.setColumnWidth("key", 350);
        table.setSelectable(true);
        table.setEditable(true);
        table.removeStyleName("v-scrollable");
        return table;
    }

    private CheckBox getCheckBox(SparkConfigEntry prop){
        if(prop.getSparkPropertyType() != SparkPropertyType.Boolean)
            throw new IllegalArgumentException("Only Boolean values can be displayed as CheckBox.");
        CheckBox cb = new CheckBox("", prop.getValue());
        cb.setConvertedValue(prop.getDefaultValue().isEmpty() ? null : new Boolean(prop.getDefaultValue()));
        cb.setHeight("25px");
        cb.setWidth("100%");
        return cb;
    }

    private ComboBox getComboBox(SparkConfigEntry prop){
        if(prop.getSparkPropertyType() != SparkPropertyType.Enum)
            throw new IllegalArgumentException("Only Enums can be displayed as ComboBoxes.");
        //we take the regex as source for our options -> splitting up by alternatives: ^(x|y|z)$
        List<String> options = Arrays.asList(prop.getRegex().pattern().replaceAll("(\\^|\\(|\\)|\\$)", "").split("\\s*\\|\\s*"));
        ComboBox cb = new ComboBox("", options);
        //set change listener
        cb.addValueChangeListener((Property.ValueChangeListener) event -> {
            prop.getValue().setValue(event.getProperty().getValue().toString());
        });
        //set default value if available
        cb.select(prop.getDefaultValue().isEmpty() ? null : prop.getDefaultValue());
        //no null value selection if default value exists
        if(!prop.getDefaultValue().isEmpty())  //not!
            cb.setNullSelectionAllowed(false);

        cb.setHeight("25px");
        cb.setWidth("100%");
        cb.setTextInputAllowed(false);

        return cb;
    }

    private Label getFormattedLabel(String label){
        Label lab = new Label(label);
        lab.setStyleName("v-button-caption");  //TODO
        return lab;
    }
}
