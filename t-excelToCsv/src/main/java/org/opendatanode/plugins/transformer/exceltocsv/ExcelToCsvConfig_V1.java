package org.opendatanode.plugins.transformer.exceltocsv;


/**
 * Configuration class for ExcelToCsv.
 *
 * @author eea-edo
 */
public class ExcelToCsvConfig_V1 {

    public static final String PLACEHOLDER_EXCEL_FILE_NAME = "${excelFileName}";
    public static final String PLACEHOLDER_SHEET_NAME = "${sheetName}";

    private String sheetNames = "list1:list2:list3";

    private String csvFileNamePattern = PLACEHOLDER_EXCEL_FILE_NAME + "_" + PLACEHOLDER_SHEET_NAME + ".csv";

    public ExcelToCsvConfig_V1() {

    }

    public String getSheetNames() {
        return sheetNames;
    }

    public void setSheetNames(String sheetNames) {
        this.sheetNames = sheetNames;
    }

    public String getCsvFileNamePattern() {
        return csvFileNamePattern;
    }

    public void setCsvFileNamePattern(String csvFileNamePattern) {
        this.csvFileNamePattern = csvFileNamePattern;
    }

}
