package org.opendatanode.plugins.transformer.exceltocsv;

import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 * Vaadin configuration dialog for ExcelToCsv.
 *
 * @author eea-edo
 */
public class ExcelToCsvVaadinDialog extends AbstractDialog<ExcelToCsvConfig_V1> {
    
    private TextField txtSheetNames;

    private TextField txtCsvFileNamePattern;

    public ExcelToCsvVaadinDialog() {
        super(ExcelToCsv.class);
    }

    @Override
    public void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);


        txtSheetNames = new TextField(ctx.tr("ExcelToCsv.dialog.sheetNames"));
        txtSheetNames.setWidth("100%");
        txtSheetNames.setDescription(ctx.tr("ExcelToCsv.dialog.sheetNames.description"));
        mainLayout.addComponent(txtSheetNames);

        txtCsvFileNamePattern = new TextField(ctx.tr("ExcelToCsv.dialog.csvFileNamePattern"));
        txtCsvFileNamePattern.setWidth("100%");
        txtCsvFileNamePattern.setDescription(ctx.tr("ExcelToCsv.dialog.csvFileNamePattern.description"));
        mainLayout.addComponent(txtCsvFileNamePattern);

        setCompositionRoot(mainLayout);
    }

    @Override
    public void setConfiguration(ExcelToCsvConfig_V1 c) throws DPUConfigException {
        txtSheetNames.setValue(c.getSheetNames());
        txtCsvFileNamePattern.setValue(c.getCsvFileNamePattern());
    }

    @Override
    public ExcelToCsvConfig_V1 getConfiguration() throws DPUConfigException {
        final ExcelToCsvConfig_V1 c = new ExcelToCsvConfig_V1();

        c.setSheetNames(txtSheetNames.getValue());
        c.setCsvFileNamePattern(txtCsvFileNamePattern.getValue());
        
        return c;
    }

}
