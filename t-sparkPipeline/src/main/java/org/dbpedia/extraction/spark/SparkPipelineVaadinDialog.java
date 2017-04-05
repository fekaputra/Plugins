package org.dbpedia.extraction.spark;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.*;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;
import org.dbpedia.extraction.spark.utils.SparkDpuConfig;

import java.util.*;

/**
 * Vaadin configuration dialog for SparkPipeline.
 *
 * @author Unknown
 */
public class SparkPipelineVaadinDialog extends AbstractDialog<SparkPipelineConfig_V1> {

    private SparkPipelineConfig_V1 config_v1;

    private SparkDpuConfig sparkMandatoryEntries;
    private SparkDpuConfig sparkOptionalEntries;
    private SparkDpuConfig useCaseMandatoryEntries;
    private SparkDpuConfig useCaseOptionalEntries;

    private List<String> mandatorySparkItems = Arrays.asList(
        "spark.app.name",
        "spark.master"
        //TODO complete!
    );

    private List<String> mandatoryUseCaseItems= Arrays.asList(
            ".filemanager.",
            ".pipelineInitialize",
            ".pairRddKeys"
    );

    private ObjectProperty<Integer> defaultTimeout = new ObjectProperty<Integer>(0);

    private ObjectProperty<Boolean> ignoreTlsErrors = new ObjectProperty<Boolean>(Boolean.FALSE);


    TextField txtDefaultTimeout;

    public SparkPipelineVaadinDialog() throws DPUConfigException {
        super(SparkPipeline.class);
        this.setConfiguration(new SparkPipelineConfig_V1());

    }

    @Override
    public void setConfiguration(SparkPipelineConfig_V1 c) throws DPUConfigException {
        this.config_v1 = c;

        //get all use case names
        Map<String, String> knownUseCaseNames = new HashMap<>();
        for(SparkDpuConfig.SparkConfigEntry ent : this.config_v1.getConfig().getItemIds()){
            if(ent.getKey().contains(".filemanager."))
                knownUseCaseNames.put(ent.getKey().substring(6, ent.getKey().indexOf(".filemanager.")), "");
        }
        try {
            this.sparkMandatoryEntries = (SparkDpuConfig)c.getConfig().clone();
            this.sparkMandatoryEntries.addContainerFilter(new Container.Filter() {
                @Override
                public boolean passesFilter(Object o, Item item) throws UnsupportedOperationException {
                    SparkDpuConfig.SparkConfigEntry entry = (SparkDpuConfig.SparkConfigEntry) o;
                    return mandatorySparkItems.contains(entry.getKey());
                }

                @Override
                public boolean appliesToProperty(Object o) {
                    return false; //TODO
                }
            });
            this.sparkOptionalEntries = (SparkDpuConfig)c.getConfig().clone();
            this.sparkOptionalEntries.addContainerFilter(new Container.Filter() {
                @Override
                public boolean passesFilter(Object o, Item item) throws UnsupportedOperationException {
                    SparkDpuConfig.SparkConfigEntry entry = (SparkDpuConfig.SparkConfigEntry) o;
                    String possibleUseCase = entry.getKey().substring(6);
                    if(possibleUseCase.indexOf('.') > 0)
                        possibleUseCase = possibleUseCase.substring(0, possibleUseCase.indexOf('.'));
                    return !mandatorySparkItems.contains(entry.getKey()) && !knownUseCaseNames.keySet().contains(possibleUseCase);
                }

                @Override
                public boolean appliesToProperty(Object o) {
                    return false; //TODO
                }
            });
            this.useCaseMandatoryEntries = (SparkDpuConfig)c.getConfig().clone();
            this.useCaseMandatoryEntries.addContainerFilter(new Container.Filter() {
                @Override
                public boolean passesFilter(Object o, Item item) throws UnsupportedOperationException {
                    SparkDpuConfig.SparkConfigEntry entry = (SparkDpuConfig.SparkConfigEntry) o;
                    boolean ret = false;
                        for(String test : mandatoryUseCaseItems)
                            if(entry.getKey().contains(test))
                                ret = true;
                    return ret && entry.getKey().contains(SparkPipelineVaadinDialog.this.config_v1.getConfig().getAppName());
                }

                @Override
                public boolean appliesToProperty(Object o) {
                    return false; //TODO
                }
            });
            this.useCaseOptionalEntries = (SparkDpuConfig)c.getConfig().clone();
            this.useCaseOptionalEntries.addContainerFilter(new Container.Filter() {
                @Override
                public boolean passesFilter(Object o, Item item) throws UnsupportedOperationException {
                    SparkDpuConfig.SparkConfigEntry entry = (SparkDpuConfig.SparkConfigEntry) o;
                    boolean ret = true;
                    for(String test : mandatoryUseCaseItems)
                        if(entry.getKey().contains(test))
                            ret = false;
                    return ret && entry.getKey().contains(SparkPipelineVaadinDialog.this.config_v1.getConfig().getAppName());
                }

                @Override
                public boolean appliesToProperty(Object o) {
                    return false; //TODO
                }
            });
        } catch (CloneNotSupportedException e) {
            throw new DPUConfigException(e);
        }

        //FIXME is this neccessary?
        buildDialogLayout();
    }

    @Override
    public SparkPipelineConfig_V1 getConfiguration() throws DPUConfigException {
        return this.config_v1;
    }

    @Override
    public void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);
        mainLayout.addComponent(new Label(ctx.tr("SparkPipeline.dialog.label")));

        final HorizontalLayout topMostSectionLeft = new HorizontalLayout();
        final HorizontalLayout topMostSectionRight = new HorizontalLayout();
        final Button addSparkProperty = new Button("+");
        addSparkProperty.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                SparkPipelineVaadinDialog.this.sparkMandatoryEntries.addItem(new SparkDpuConfig.SparkConfigEntry());
            }
        });


        addSparkProperty.setClickShortcut(ShortcutAction.KeyCode.INSERT);
        addSparkProperty.setDescription(ctx.tr("SparkPipeline.config.Insert"));

        final Button loadSparkConfig = new Button("+");
        loadSparkConfig.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                //TODO
            }
        });

        final Button exportSparkConfig = new Button("+");
        exportSparkConfig.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                //TODO
            }
        });

        // fill top-most section
        topMostSectionLeft.addComponents(addSparkProperty);
        topMostSectionLeft.setExpandRatio(addSparkProperty, 0.0f);
        topMostSectionRight.addComponents(loadSparkConfig, exportSparkConfig);

        final HorizontalSplitPanel splitTop = new HorizontalSplitPanel(topMostSectionLeft, topMostSectionRight);
        mainLayout.addComponent(splitTop);

        final Table table = new Table();
        table.addGeneratedColumn("remove", new Table.ColumnGenerator() {

            @Override
            public Object generateCell(Table source, Object itemId, Object columnId) {
                Button result = new Button("-");
                final Object itemIdFinal = itemId;

                result.addClickListener(new Button.ClickListener() {

                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        SparkPipelineVaadinDialog.this.sparkMandatoryEntries.removeItem(itemIdFinal);
                    }

                });

                return result;
            }

        });
        table.setContainerDataSource(this.sparkMandatoryEntries);
        table.setColumnHeaderMode(Table.ColumnHeaderMode.EXPLICIT);
        table.setColumnHeader("key", ctx.tr("SparkPipeline.config.key"));
        table.setColumnHeader("value", ctx.tr("SparkPipeline.config.Value"));
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
        table.setVisibleColumns("remove", "key", "value");
        mainLayout.addComponent(table);

        setCompositionRoot(mainLayout);
    }
}
