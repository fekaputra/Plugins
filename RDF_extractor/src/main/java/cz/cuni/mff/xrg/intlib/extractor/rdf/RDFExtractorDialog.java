package cz.cuni.mff.xrg.intlib.extractor.rdf;


import com.vaadin.ui.*;

import cz.cuni.xrg.intlib.commons.configuration.*;
import cz.cuni.xrg.intlib.commons.web.AbstractConfigDialog;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.vaadin.data.*;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.*;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.shared.ui.combobox.FilteringMode;

/**
 * Configuration dialog for DPU SPARQL Extractor.
 *
 * @author Maria Kukhar
 *
 */
public class RDFExtractorDialog extends AbstractConfigDialog<RDFExtractorConfig> {

    private static final long serialVersionUID = 1L;
    private GridLayout mainLayout;
    private TabSheet tabSheet;
    private VerticalLayout verticalLayoutDetails;
    private CheckBox checkBoxFail;
    private Label labelOpt;
    private GridLayout gridLayoutConstr;
    private TextArea textAreaConstr;
    private Label labelConstr;
    private GridLayout gridLayoutCore;
    private GridLayout gridLayoutAdm;
    private Label labelGraph;
    private PasswordField passwordFieldPass;
    private Label labelPass;
    private TextField textFieldNameAdm;
    private Label labelNameAdm;
    private ComboBox comboBoxSparql;
    private Label labelSparql;
    private GridLayout gridLayoutGraph;
    private TextField textFieldGraph;
    private Button buttonGraphRem;
    private Button buttonGraphAdd;
	private CheckBox useHandler;  //Statistical handler
    int n = 1;

	/**
	 *  Basic constructor.
	 */
    public RDFExtractorDialog() {
        buildMainLayout();
        setCompositionRoot(mainLayout);
    }
    
    /**
     * IndexedContainer with the data for comboBoxSparql
     */
    public static IndexedContainer getFridContainer() {

        String[] visibleCols = new String[]{"endpoint"};
        IndexedContainer result = new IndexedContainer();

        for (String p : visibleCols) {
            result.addContainerProperty(p, String.class, "");
        }

        return result;
    }

	/**
	 * Builds main layout contains tabSheet with components.
	 */
    private GridLayout buildMainLayout() {
        // common part: create layout

        mainLayout = new GridLayout(1, 1);
        mainLayout.setImmediate(false);
        mainLayout.setWidth("100%");
        mainLayout.setHeight("100%");

        // top-level component properties
        setWidth("100%");
        setHeight("100%");

        // tabSheet
        tabSheet = buildTabSheet();
        mainLayout.addComponent(tabSheet, 0, 0);
        mainLayout.setComponentAlignment(tabSheet, Alignment.TOP_LEFT);

        return mainLayout;
    }

	/**
	 *  Builds tabSheet
	 */
    private TabSheet buildTabSheet() {
        // common part: create layout
        tabSheet = new TabSheet();
        tabSheet.setImmediate(true);
        tabSheet.setWidth("100%");
        tabSheet.setHeight("100%");

        // Core tab
        gridLayoutCore = buildGridLayoutCore();
        tabSheet.addTab(gridLayoutCore, "Core", null);

        // Details tab
        verticalLayoutDetails = buildVerticalLayoutDetails();
        tabSheet.addTab(verticalLayoutDetails, "Details", null);

        return tabSheet;
    }

	/**
	 * Builds layout contains Core tab components
	 */
    private GridLayout buildGridLayoutCore() {

        // common part: create layout
    	gridLayoutCore = new GridLayout();
    	gridLayoutCore.setImmediate(false);
    	gridLayoutCore.setWidth("100%");
    	gridLayoutCore.setHeight("100%");
    	gridLayoutCore.setMargin(true);
    	gridLayoutCore.setColumns(2);
    	gridLayoutCore.setRows(4);
    	gridLayoutCore.setColumnExpandRatio(0, 0.10f);
    	gridLayoutCore.setColumnExpandRatio(1, 0.90f);

        // labelSparql
        labelSparql = new Label();
        labelSparql.setImmediate(false);
        labelSparql.setWidth("-1px");
        labelSparql.setHeight("-1px");
        labelSparql.setValue("SPARQL endpoint:");
        gridLayoutCore.addComponent(labelSparql, 0, 0);
        gridLayoutCore.setComponentAlignment(labelSparql, Alignment.TOP_LEFT);

        // SPARQL endpoint ComboBox
        Container cont = getFridContainer();
        comboBoxSparql = new ComboBox();
        comboBoxSparql.setContainerDataSource(cont);
        comboBoxSparql.setImmediate(false);
        comboBoxSparql.setWidth("100%");
        comboBoxSparql.setHeight("-1px");
        comboBoxSparql.setNewItemsAllowed(true);
        comboBoxSparql.setTextInputAllowed(true);
        comboBoxSparql.setItemCaptionPropertyId("endpoint");
        comboBoxSparql.setInputPrompt("http://example:8894/sparql");
        comboBoxSparql
                .setItemCaptionMode(AbstractSelect.ItemCaptionMode.PROPERTY);

        comboBoxSparql.setFilteringMode(FilteringMode.CONTAINS);
        comboBoxSparql.setImmediate(true);

        // Disallow null selections
        comboBoxSparql.setNullSelectionAllowed(false);

        // Check if the caption for new item already exists in the list of item
        // captions before approving it as a new item.

        comboBoxSparql.setNewItemHandler(new AbstractSelect.NewItemHandler() {
            @Override
            public void addNewItem(final String newItemCaption) {
                boolean newItem = true;
                for (final Object itemId : comboBoxSparql.getItemIds()) {
                    if (newItemCaption.equalsIgnoreCase(comboBoxSparql
                            .getItemCaption(itemId))) {
                        newItem = false;
                        break;
                    }
                }
                if (newItem) {
                    // Adds new option
                    if (comboBoxSparql.addItem(newItemCaption) != null) {
                        final Item item = comboBoxSparql
                                .getItem(newItemCaption);
                        item.getItemProperty("endpoint").setValue(
                                newItemCaption);
                        comboBoxSparql.setValue(newItemCaption);
                    }
                }
            }
        });
        //comboBoxSparql is mandatory fields
        comboBoxSparql.addValidator(new Validator() {
			@Override
			public void validate(Object value) throws InvalidValueException {
				if (value!=null) {
					return;
				}
				throw new InvalidValueException("SPARQL endpoint must be filled!");
			}
		});
        gridLayoutCore.addComponent(comboBoxSparql, 1, 0);

        // labelNameAdm
        labelNameAdm = new Label();
        labelNameAdm.setImmediate(false);
        labelNameAdm.setWidth("-1px");
        labelNameAdm.setHeight("-1px");
        labelNameAdm.setValue("Name:");
        gridLayoutCore.addComponent(labelNameAdm, 0, 1);

        // Name textField 
        textFieldNameAdm = new TextField();
        textFieldNameAdm.setNullRepresentation("");
        textFieldNameAdm.setImmediate(false);
        textFieldNameAdm.setWidth("100%");
        textFieldNameAdm.setHeight("-1px");
        textFieldNameAdm.setInputPrompt("username to connect to SPARQL endpoints");
        gridLayoutCore.addComponent(textFieldNameAdm, 1, 1);

        // labelPass
        labelPass = new Label();
        labelPass.setImmediate(false);
        labelPass.setWidth("-1px");
        labelPass.setHeight("-1px");
        labelPass.setValue("Password:");
        gridLayoutCore.addComponent(labelPass, 0, 2);

        //  Password field
        passwordFieldPass = new PasswordField();
        passwordFieldPass.setNullRepresentation("");
        passwordFieldPass.setImmediate(false);
        passwordFieldPass.setWidth("100%");
        passwordFieldPass.setHeight("-1px");
        passwordFieldPass.setInputPrompt("password");
        gridLayoutCore.addComponent(passwordFieldPass, 1, 2);

        // labelGraph
        labelGraph = new Label();
        labelGraph.setImmediate(false);
        labelGraph.setWidth("-1px");
        labelGraph.setHeight("-1px");
        labelGraph.setValue("Named Graph:");
        gridLayoutCore.addComponent(labelGraph, 0, 3);

        //Named Graph component
        initializeNamedGraphList();
        gridLayoutCore.addComponent(gridLayoutGraph, 1, 3);

  
        return gridLayoutCore;
    }


    private List<String> griddata = initializeGridData();

    /**
     * Initializes data of the Named Graph component
     */
    private static List<String> initializeGridData() {
        List<String> result = new LinkedList<>();
        result.add("");

        return result;

    }

    /**
     * Add new data to Named Graph component
     * @param newData. String that will be added
     */
    private void addDataToGridData(String newData) {
        griddata.add(newData);
    }

    
    /**
     * Remove row from Named Graph component. Only if component contain more then 1 row.
     * @param  row that will be removed. 
     */
    private void removeDataFromGridData(Integer row) {
        int index = row;
        if (griddata.size() > 1) {
            griddata.remove(index);
        }
    }
    private List<TextField> listedEditText = null;


    /**
     * Save edited texts in the Named Graph component
     */
    private void saveEditedTexts() {
        griddata = new LinkedList<>();
        for (TextField editText : listedEditText) {
            griddata.add(editText.getValue());
        }
    }


    /**
     * Refresh data of the Named Graph component
     */
    private void refreshNamedGraphData() {
        gridLayoutGraph.removeAllComponents();
        int row = 0;
        listedEditText = new ArrayList<>();
        if (griddata.size() < 1) {
            griddata.add("");
        }
        gridLayoutGraph.setRows(griddata.size() + 1);
        for (String item : griddata) {
            textFieldGraph = new TextField();
            listedEditText.add(textFieldGraph);
            
            //text field for the graph
            textFieldGraph.setWidth("100%");
            textFieldGraph.setData(row);
            textFieldGraph.setValue(item);
            textFieldGraph.setInputPrompt("http://ld.opendata.cz/source1");

            //remove button
            buttonGraphRem = new Button();
            buttonGraphRem.setWidth("55px");
            buttonGraphRem.setCaption("-");
            buttonGraphRem.setData(row);
            buttonGraphRem.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    saveEditedTexts();
                    Button senderButton = event.getButton();
                    Integer row = (Integer) senderButton.getData();
                    removeDataFromGridData(row);
                    refreshNamedGraphData();
                }
            });
            gridLayoutGraph.addComponent(textFieldGraph, 0, row);
            gridLayoutGraph.addComponent(buttonGraphRem, 1, row);
            gridLayoutGraph.setComponentAlignment(buttonGraphRem,
                    Alignment.TOP_RIGHT);
            row++;
        }
        //add button
        buttonGraphAdd = new Button();
        buttonGraphAdd.setCaption("+");
        buttonGraphAdd.setImmediate(true);
        buttonGraphAdd.setWidth("55px");
        buttonGraphAdd.setHeight("-1px");
        buttonGraphAdd.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                saveEditedTexts();
                addDataToGridData(" ");
                refreshNamedGraphData();
            }
        });
        gridLayoutGraph.addComponent(buttonGraphAdd, 0, row);

    }

    /**
     *  Initializes Named Graph component
     */
    private void initializeNamedGraphList() {

        gridLayoutGraph = new GridLayout();
        gridLayoutGraph.setImmediate(false);
        gridLayoutGraph.setWidth("100%");
        gridLayoutGraph.setHeight("100%");
        gridLayoutGraph.setMargin(false);
        gridLayoutGraph.setColumns(2);
        gridLayoutGraph.setColumnExpandRatio(0, 0.95f);
        gridLayoutGraph.setColumnExpandRatio(1, 0.05f);
        
        refreshNamedGraphData();

    }

	/**
	 * Builds layout contains Details tab components
	 */
    private VerticalLayout buildVerticalLayoutDetails() {
        // common part: create layout
        verticalLayoutDetails = new VerticalLayout();
        verticalLayoutDetails.setImmediate(false);
        verticalLayoutDetails.setWidth("100%");
        verticalLayoutDetails.setHeight("-1px");
        verticalLayoutDetails.setMargin(true);
        verticalLayoutDetails.setSpacing(true);

        // SPARQL Construct component
        gridLayoutConstr = buildGridLayoutConstr();
        verticalLayoutDetails.addComponent(gridLayoutConstr);

        // labelOpt
        labelOpt = new Label();
        labelOpt.setImmediate(false);
        labelOpt.setWidth("-1px");
        labelOpt.setHeight("-1px");
        labelOpt.setValue("Options:");
        verticalLayoutDetails.addComponent(labelOpt);

        // CheckBox Extraction fails if there is no triple extracted.
        checkBoxFail = new CheckBox();
        checkBoxFail
                .setCaption("Extraction fails if there is no triple extracted.");
        checkBoxFail.setImmediate(false);
        checkBoxFail.setWidth("-1px");
        checkBoxFail.setHeight("-1px");
        verticalLayoutDetails.addComponent(checkBoxFail);
		
		//Statistical handler
		//TODO MARIA - set parameters and placement for this component
		useHandler = new CheckBox("Use statistical handler");
		useHandler.setWidth("-1px");
		useHandler.setHeight("-1px");
		verticalLayoutDetails.addComponent(useHandler);

        return verticalLayoutDetails;
    }
    
	/**
	 * Builds layout contains SPARQL Construct component
	 */
    private GridLayout buildGridLayoutConstr() {
        // common part: create layout
        gridLayoutConstr = new GridLayout();
        gridLayoutConstr.setImmediate(false);
        gridLayoutConstr.setWidth("100%");
        gridLayoutConstr.setHeight("-1px");
        gridLayoutConstr.setMargin(false);
        gridLayoutConstr.setSpacing(true);
        gridLayoutConstr.setColumns(2);
        gridLayoutConstr.setColumnExpandRatio(0, 0.20f);
        gridLayoutConstr.setColumnExpandRatio(1, 0.80f);

        // labelConstr
        labelConstr = new Label();
        labelConstr.setImmediate(false);
        labelConstr.setWidth("100%");
        labelConstr.setHeight("-1px");
        labelConstr.setValue("SPARQL  Construct:");
        gridLayoutConstr.addComponent(labelConstr, 0, 0);

        // textAreaConstr
        textAreaConstr = new TextArea();
        textAreaConstr.setNullRepresentation("");
        textAreaConstr.setImmediate(false);
        textAreaConstr.setWidth("100%");
        textAreaConstr.setHeight("100px");
        textAreaConstr.setInputPrompt("CONSTRUCT {<http://dbpedia.org/resource/Prague> ?p ?o} where {<http://dbpedia.org/resource/Prague> ?p ?o } LIMIT 100");
        gridLayoutConstr.addComponent(textAreaConstr, 1, 0);

        return gridLayoutConstr;
    }

    
	/**
	 * Set values from from dialog to configuration.
	 */
	@Override
	public RDFExtractorConfig getConfiguration() throws ConfigException {
		if (!comboBoxSparql.isValid()) {
			throw new ConfigException();
		} else {
			saveEditedTexts();
			RDFExtractorConfig config = new RDFExtractorConfig();		
			config.SPARQL_endpoint = (String) comboBoxSparql.getValue();
			config.Host_name = textFieldNameAdm.getValue();
			config.Password = passwordFieldPass.getValue();
			config.SPARQL_query = textAreaConstr.getValue();
			config.GraphsUri = griddata;
			config.ExtractFail = checkBoxFail.getValue();
			config.UseStatisticalHandler = useHandler.getValue();
						
			return config;
		}
	}
	
	
	/**
	 * Load values from configuration into dialog.
	 *
	 * @throws ConfigException
	 * @param conf
	 */
	@Override
    public void setConfiguration(RDFExtractorConfig conf) {
        try {
            String endp = conf.SPARQL_endpoint;

            if ((endp!=null)&& (comboBoxSparql.addItem(endp) != null)) {
                final Item item = comboBoxSparql.getItem(endp);
                item.getItemProperty("endpoint").setValue(endp);
                comboBoxSparql.setValue(endp);
            }
            textFieldNameAdm.setValue(conf.Host_name);
            passwordFieldPass.setValue(conf.Password);
            textAreaConstr.setValue(conf.SPARQL_query);
            checkBoxFail.setValue(conf.ExtractFail);
			useHandler.setValue(conf.UseStatisticalHandler);

            griddata = conf.GraphsUri;
            if (griddata == null) {
                griddata = new LinkedList<>();
            }
            refreshNamedGraphData();
        } catch (UnsupportedOperationException | Property.ReadOnlyException | Converter.ConversionException ex) {
            // throw setting exception
            throw new ConfigException();
        }
    }	
}
