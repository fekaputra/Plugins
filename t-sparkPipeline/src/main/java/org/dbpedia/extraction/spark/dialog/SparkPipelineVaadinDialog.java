package org.dbpedia.extraction.spark.dialog;

import com.vaadin.data.*;
import com.vaadin.ui.*;
import de.steinwedel.messagebox.MessageBox;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;
import org.dbpedia.extraction.spark.SparkPipeline;
import org.dbpedia.extraction.spark.SparkPipelineConfig_V1;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Vaadin configuration dialog for SparkPipeline.
 *
 * @author Unknown
 */
public class SparkPipelineVaadinDialog extends AbstractDialog<SparkPipelineConfig_V1> {

    private SparkPipelineConfig_V1 config_v1;

    private SparkDpuConfig sparkMandatoryEntries;
    private SparkDpuConfig sparkRecommendedEntries;
    private SparkDpuConfig sparkOptionalEntries;
    private SparkDpuConfig useCaseMandatoryEntries;
    private SparkDpuConfig useCaseOptionalEntries;

    private SparkConfigEntry newSparkProperty;
    private SparkConfigEntry newUseCaseProperty;

    public SparkPipelineVaadinDialog() throws DPUConfigException {
        super(SparkPipeline.class);
        this.setConfiguration(new SparkPipelineConfig_V1());

        newSparkProperty = new SparkConfigEntry(SparkConfigEntry.SparkPropertyCategory.SparkOptional.toString(), "", config_v1.getConfig().GetEmptyEntry(SparkConfigEntry.SparkPropertyCategory.SparkOptional));
        newUseCaseProperty = new SparkConfigEntry(SparkConfigEntry.SparkPropertyCategory.UsecaseOptional.toString(), "", config_v1.getConfig().GetEmptyEntry(SparkConfigEntry.SparkPropertyCategory.UsecaseOptional));
    }

    /**
     * is necessary to prevent DPUWrapException(Messages.getString("DPURecordWrap.configure"), e) @ cz.cuni.mff.xrg.odcs.frontend.dpu.wrap.DPURecordWrap.configuredDialog()
     * FIXME solve this problem please @SWC
     * @param conf
     * @throws DPUConfigException
     */
    @Override
    public void setConfig(String conf) throws DPUConfigException {
        //not sure what im doing here :D
        setConfiguration(this.config_v1);
    }

    @Override
    public void setConfiguration(SparkPipelineConfig_V1 c) throws DPUConfigException {
        this.config_v1 = c;

        try {
            this.sparkMandatoryEntries = (SparkDpuConfig)c.getConfig().clone();
            this.sparkMandatoryEntries.addContainerFilter(getContainerFilter(Arrays.asList(SparkConfigEntry.SparkPropertyCategory.SparkMandatory), false));
            this.sparkRecommendedEntries = (SparkDpuConfig)c.getConfig().clone();
            this.sparkRecommendedEntries.addContainerFilter(getContainerFilter(Arrays.asList(SparkConfigEntry.SparkPropertyCategory.SparkRecommended), false));
            this.sparkOptionalEntries = (SparkDpuConfig)c.getConfig().clone();
            this.sparkOptionalEntries.addContainerFilter(getContainerFilter(Arrays.asList(SparkConfigEntry.SparkPropertyCategory.SparkOptional), false));
            this.useCaseMandatoryEntries = (SparkDpuConfig)c.getConfig().clone();
            this.useCaseMandatoryEntries.addContainerFilter(getContainerFilter(Arrays.asList(SparkConfigEntry.SparkPropertyCategory.UsecaseRecommended, SparkConfigEntry.SparkPropertyCategory.UsecaseMandatory), true));
            this.useCaseOptionalEntries = (SparkDpuConfig)c.getConfig().clone();
            this.useCaseOptionalEntries.addContainerFilter(getContainerFilter(Arrays.asList(SparkConfigEntry.SparkPropertyCategory.UsecaseOptional), true));
        } catch (CloneNotSupportedException e) {
            throw new DPUConfigException(e);
        }
    }

    @Override
    public SparkPipelineConfig_V1 getConfiguration() throws DPUConfigException {
        return this.config_v1;
    }

    @Override
    public void buildDialogLayout() {

        //for optional item lists -> if no new item entry exists -> add it
        if(this.sparkOptionalEntries.getItemIds().stream().filter(x -> x.getKey().equals("")).count() == 0)
            this.sparkOptionalEntries.addItem(this.config_v1.getConfig().GetEmptyEntry(SparkConfigEntry.SparkPropertyCategory.SparkOptional));
        if(this.useCaseOptionalEntries.getItemIds().stream().filter(x -> x.getKey().equals("")).count() == 0)
            this.useCaseOptionalEntries.addItem(this.config_v1.getConfig().GetEmptyEntry(SparkConfigEntry.SparkPropertyCategory.UsecaseOptional));

        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);
        mainLayout.addComponent(getFormattedLabel(ctx.tr("SparkPipeline.dialog.label")));
        mainLayout.addComponent(implementNewTable(this.sparkMandatoryEntries));
        mainLayout.addComponent(getFormattedLabel(ctx.tr("SparkPipeline.dialog.label")));
        mainLayout.addComponent(implementNewTable(this.sparkRecommendedEntries));
        mainLayout.addComponent(getFormattedLabel(ctx.tr("SparkPipeline.dialog.label")));
        mainLayout.addComponent(implementNewTable(this.sparkOptionalEntries));
        mainLayout.addComponent(getFormattedLabel(ctx.tr("SparkPipeline.dialog.label")));
        mainLayout.addComponent(implementNewTable(this.useCaseMandatoryEntries));
        mainLayout.addComponent(getFormattedLabel(ctx.tr("SparkPipeline.dialog.label")));
        mainLayout.addComponent(implementNewTable(this.useCaseOptionalEntries));

        setCompositionRoot(mainLayout);
    }


    private Table implementNewTable(SparkDpuConfig source) {
        Table table = new Table();

        table.addGeneratedColumn("addremove", new Table.ColumnGenerator() {
            @Override
            public Object generateCell(final Table source, final Object itemId, final Object columnId) {
                SparkConfigEntry id = ((SparkConfigEntry) itemId);

                //if mandatory table -> return no Button!
                if(id.getSparkPropertyCategory() == SparkConfigEntry.SparkPropertyCategory.SparkMandatory || id.getSparkPropertyCategory() == SparkConfigEntry.SparkPropertyCategory.UsecaseMandatory)
                    return null;
                //if current item is last item -> add row, else reduce it
                Button result = new Button("".equals(id.getKey()) ? "+" : "-");
                result.addClickListener((Button.ClickListener) event -> {
                    if("".equals(id.getKey())) {
                        if (id.getSparkPropertyCategory() == SparkConfigEntry.SparkPropertyCategory.SparkOptional) {
                            //delete empty- (default/add) item
                            table.removeItem(SparkPipelineVaadinDialog.this.config_v1.getConfig().GetEmptyEntry(SparkConfigEntry.SparkPropertyCategory.SparkOptional));
                            //insert new item
                            table.addItem(SparkPipelineVaadinDialog.this.newSparkProperty);
                            //reinsert empty- (default/add) item as last
                            table.addItem(SparkPipelineVaadinDialog.this.config_v1.getConfig().GetEmptyEntry(SparkConfigEntry.SparkPropertyCategory.SparkOptional));
                        }
                        else if(id.getSparkPropertyCategory() == SparkConfigEntry.SparkPropertyCategory.UsecaseOptional) {
                            table.removeItem(SparkPipelineVaadinDialog.this.config_v1.getConfig().GetEmptyEntry(SparkConfigEntry.SparkPropertyCategory.UsecaseOptional));
                            table.addItem(SparkPipelineVaadinDialog.this.newUseCaseProperty);
                            table.addItem(SparkPipelineVaadinDialog.this.config_v1.getConfig().GetEmptyEntry(SparkConfigEntry.SparkPropertyCategory.UsecaseOptional));
                        }
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
                        if (id.getSparkPropertyCategory() == SparkConfigEntry.SparkPropertyCategory.SparkOptional)
                            SparkPipelineVaadinDialog.this.newSparkProperty = new SparkConfigEntry(
                                    event.getProperty().getValue().toString().trim(),
                                    SparkPipelineVaadinDialog.this.newSparkProperty.getValue(),
                                    SparkPipelineVaadinDialog.this.config_v1.getConfig().GetDefaultEntry(event.getProperty().getValue().toString().trim()));
                        else if (id.getSparkPropertyCategory() == SparkConfigEntry.SparkPropertyCategory.UsecaseOptional)
                            SparkPipelineVaadinDialog.this.newUseCaseProperty = new SparkConfigEntry(
                                    event.getProperty().getValue().toString().trim(),
                                    SparkPipelineVaadinDialog.this.newUseCaseProperty.getValue(),
                                    SparkPipelineVaadinDialog.this.config_v1.getConfig().GetDefaultEntry(event.getProperty().getValue().toString().trim()));
                    }
                    else {
                        result.setStyleName("loginError");
                    }
                });
                if(id.getSparkPropertyCategory() == SparkConfigEntry.SparkPropertyCategory.SparkMandatory
                        || id.getSparkPropertyCategory() == SparkConfigEntry.SparkPropertyCategory.SparkOptional
                        || id.getSparkPropertyCategory() == SparkConfigEntry.SparkPropertyCategory.SparkRecommended)
                    result.addValidator(Validators.SparkKeyValidator);
                if(id.getSparkPropertyCategory() == SparkConfigEntry.SparkPropertyCategory.UsecaseMandatory
                        || id.getSparkPropertyCategory() == SparkConfigEntry.SparkPropertyCategory.UsecaseOptional
                        || id.getSparkPropertyCategory() == SparkConfigEntry.SparkPropertyCategory.UsecaseRecommended)
                    result.addValidator(Validators.GetUseCaseKeyValidator(SparkPipelineVaadinDialog.this.config_v1.getConfig().getAppName()));
                //result.validate();
                return result;
            }
        });

        table.addGeneratedColumn("value", new Table.ColumnGenerator() {
            @Override
            public Object generateCell(final Table source, final Object itemId, final Object columnId) {
                SparkConfigEntry id = ((SparkConfigEntry) itemId);

                /* create special components for certain types: */
                if(id.getSparkPropertyType() == SparkConfigEntry.SparkPropertyType.Boolean)
                    return SparkPipelineVaadinDialog.this.getCheckBox(id);
                if(id.getSparkPropertyType() == SparkConfigEntry.SparkPropertyType.Enum)
                    return SparkPipelineVaadinDialog.this.getComboBox(id);

                /* else -> create TextField */
                TextField result = new TextField();
                result.setNullRepresentation("");
                result.setImmediate(true);
                result.setHeight("25px");
                result.setWidth("100%");
                if(id.getSparkPropertyType().getClazz().equals(URI.class))
                    result.setConverter(Converters.StringToUriConverter);
                if(id.getSparkPropertyType().getClazz().equals(List.class))
                    result.setConverter(Converters.StringToStringListConverter);
                if(id.getSparkPropertyType().getClazz().equals(Integer.class))
                    result.setConverter(Converters.StringToIntegerConverter);
                result.setPropertyDataSource(id.getValue());
                result.setValue(id.getValue().toString());
                result.addValueChangeListener((Property.ValueChangeListener) event -> {
                    if(id.getSparkPropertyCategory() == SparkConfigEntry.SparkPropertyCategory.SparkOptional)
                        SparkPipelineVaadinDialog.this.newSparkProperty.setValue(event.getProperty());
                    else if(id.getSparkPropertyCategory() == SparkConfigEntry.SparkPropertyCategory.UsecaseOptional)
                        SparkPipelineVaadinDialog.this.newUseCaseProperty.setValue(event.getProperty());

                    if(result.isValid()) {
                        result.setStyleName("v-textfield");
                        if (id.getSparkPropertyCategory() == SparkConfigEntry.SparkPropertyCategory.SparkOptional
                                || id.getSparkPropertyCategory() == SparkConfigEntry.SparkPropertyCategory.SparkRecommended
                                || id.getSparkPropertyCategory() == SparkConfigEntry.SparkPropertyCategory.SparkMandatory)
                            SparkPipelineVaadinDialog.this.newSparkProperty.setValue(event.getProperty());
                        else if(id.getSparkPropertyCategory() == SparkConfigEntry.SparkPropertyCategory.UsecaseRecommended
                                || id.getSparkPropertyCategory() == SparkConfigEntry.SparkPropertyCategory.UsecaseMandatory
                                || id.getSparkPropertyCategory() == SparkConfigEntry.SparkPropertyCategory.UsecaseOptional){
                            SparkPipelineVaadinDialog.this.newUseCaseProperty.setValue(event.getProperty());
                        }
                    }
                    else {
                        //TODO InvalidAllowed? check every entry before export
                        result.setStyleName("loginError");
                    }
                });

                result.addValidator(Validators.GetValueValidator(id));
                //result.validate();
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
        table.setColumnWidth("addremove", 40);
        table.setColumnWidth("description", 40);
        table.setColumnWidth("key", 300);
        table.setSelectable(true);
        table.setEditable(true);
        return table;
    }

    private CheckBox getCheckBox(SparkConfigEntry prop){
        CheckBox cb = new CheckBox("", prop.getValue());
        cb.setValue(prop.getDefaultValue().isEmpty() ? null : new Boolean(prop.getDefaultValue()));
        cb.setHeight("25px");
        cb.setWidth("100%");
        return cb;
    }

    private ComboBox getComboBox(SparkConfigEntry prop){
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

    private Container.Filter getContainerFilter(List<SparkConfigEntry.SparkPropertyCategory> categories, boolean testUseCase){
        return new Container.Filter() {
            @Override
            public boolean passesFilter(Object o, Item item) throws UnsupportedOperationException {
                String possibleUseCase = ((SparkConfigEntry) o).getKey();
                if(possibleUseCase.length() > 7 && possibleUseCase.indexOf('.', 6) >= 0)
                    possibleUseCase = possibleUseCase.substring(6, possibleUseCase.indexOf('.', 6));
                return categories.contains(((SparkConfigEntry) o).getSparkPropertyCategory())
                        && (!testUseCase || SparkPipelineVaadinDialog.this.config_v1.getConfig().getAppName().equals(possibleUseCase));
            }

            @Override
            public boolean appliesToProperty(Object o) {
                return false; //TODO
            }
        };
    }
}
