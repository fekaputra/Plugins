package org.opendatanode.plugins.transformer.exceltocsv;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVWriter;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.FilesDataUnit.Entry;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.files.FilesHelper;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;

/**
 * Main data processing unit class.
 *
 * @author eea-edo
 */
@DPU.AsTransformer
public class ExcelToCsv extends AbstractDpu<ExcelToCsvConfig_V1> {
    
    private static class DpuFile {
        public String symbolicName;
        public File file;
        public DpuFile(String symbolicName, File file) {
            this.symbolicName = symbolicName;
            this.file = file;
        }
    }

    private static final Logger log = LoggerFactory.getLogger(ExcelToCsv.class);

    @DataUnit.AsInput(name = "input")
    public FilesDataUnit input;

    @DataUnit.AsOutput(name = "output")
    public WritableFilesDataUnit output;

    private Set<String> sheetNameSet;

    private SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm:ss");
    private SimpleDateFormat formatYearOnly = new SimpleDateFormat("yyyy");

    public ExcelToCsv() {
        super(ExcelToCsvVaadinDialog.class, ConfigHistory.noHistory(ExcelToCsvConfig_V1.class));
    }

    @Override
    protected void innerExecute() throws DPUException {

        ContextUtils.sendShortInfo(ctx, "ExcelToCsv.message");

        try {
            sheetNameSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER); // case-insensitive sheet names
            String sheetNames = config.getSheetNames();
            if (sheetNames != null && sheetNames.length() > 0) {
                sheetNameSet.addAll(Arrays.asList(sheetNames.split(Pattern.quote(":"))));
            }

            Set<FilesDataUnit.Entry> entries = FilesHelper.getFiles(input);
            for (FilesDataUnit.Entry entry : entries) {
                excelToCsv(entry);
            }
        } catch (DataUnitException | EncryptedDocumentException | InvalidFormatException | IOException ex) {
            throw ContextUtils.dpuException(ctx, ex, "ExcelToCsv.dpuFailed");
        }
    }

    private void excelToCsv(FilesDataUnit.Entry entry) throws EncryptedDocumentException, InvalidFormatException, IOException,
            DataUnitException {

        ContextUtils.sendShortInfo(ctx, "ExcelToCsv.excelTransformationStared", entry.getSymbolicName());

        File excelFile = FilesHelper.asFile(entry);

        try (Workbook wb = WorkbookFactory.create(OPCPackage.open(excelFile, PackageAccess.READ))) {
            for (int s = 0; s < wb.getNumberOfSheets(); s++) {
                Sheet sheet = wb.getSheetAt(s);
                ContextUtils.sendShortInfo(ctx, "ExcelToCsv.sheetName", sheet.getSheetName());
                if (sheetNameSet.isEmpty() || sheetNameSet.contains(sheet.getSheetName())) {
                    DpuFile csvFile = createCsvFile(entry.getSymbolicName(), sheet.getSheetName());
                    ContextUtils.sendShortInfo(ctx, "ExcelToCsv.csvGenerationStarted", csvFile.symbolicName);
                    sheetToCsv(sheet, csvFile.file);
                    ContextUtils.sendShortInfo(ctx, "ExcelToCsv.csvGenerationFinished", csvFile.symbolicName);
                } else {
                    ContextUtils.sendShortInfo(ctx, "ExcelToCsv.sheetIgnored", sheet.getSheetName());
                }
            }
        } catch (EncryptedDocumentException | InvalidFormatException | IOException ex) {
            ContextUtils.sendError(ctx, "ExcelToCsv.excelTransformationFailed", ex, "ExcelToCsv.excelTransformationFailed", entry.getSymbolicName());
            throw ex;
        }

        ContextUtils.sendShortInfo(ctx, "ExcelToCsv.excelTransformationFinishedSuccessfully", entry.getSymbolicName());
    }

    private DpuFile createCsvFile(String excelFileName, String sheetName) throws DataUnitException {
        excelFileName = ExcelToCsvUtils.getBaseFileName(excelFileName); // removes file extension

        String csvFileName = config.getCsvFileNamePattern().replace(ExcelToCsvConfig_V1.PLACEHOLDER_EXCEL_FILE_NAME,
                excelFileName);
        csvFileName = csvFileName.replace(ExcelToCsvConfig_V1.PLACEHOLDER_SHEET_NAME, sheetName);
        
        csvFileName = ExcelToCsvUtils.stripAccents(csvFileName);

        Entry newEntry = FilesHelper.createFile(output, csvFileName);
        File file = FilesHelper.asFile(newEntry);
        return new DpuFile(csvFileName, file);
    }

    private void sheetToCsv(Sheet sheet, File csvFile) throws IOException {
        final String[] empty_array = new String[]{};
        try (CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(new FileOutputStream(csvFile), "utf-8"))) {
            int minCellNumber = getMinCellNumber(sheet);
            for (int r = sheet.getFirstRowNum(); r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row != null) {
                    List<String> csvLine = new ArrayList<>();
                    for (int c = minCellNumber; c < row.getLastCellNum(); c++) {
                        Cell cell = row.getCell(c);
                        String value = getCellValue(cell);
                        csvLine.add(value);
                    }
                    csvWriter.writeNext(csvLine.toArray(empty_array));
                }
            }
        }
    }

    private int getMinCellNumber(Sheet sheet) {
        int minCellNumber = Integer.MAX_VALUE;
        for (int r = sheet.getFirstRowNum(); r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row != null) {
                minCellNumber = Math.min(minCellNumber, row.getFirstCellNum());
            }
        }
        return minCellNumber;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_BLANK:
                return null;
            case Cell.CELL_TYPE_BOOLEAN:
                return Boolean.valueOf(cell.getBooleanCellValue()).toString();
            case Cell.CELL_TYPE_ERROR:
                return "error cell";
            case Cell.CELL_TYPE_FORMULA:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return getDateCellValueAsString(cell);
                } else {
                    return Double.toString(cell.getNumericCellValue());
                }
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return getDateCellValueAsString(cell);
                } else {
                    return Double.toString(cell.getNumericCellValue());
                }
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            default:
                return null;
        }
    }

    private String getDateCellValueAsString(Cell poiCell) {
        // get date
        Date date = poiCell.getDateCellValue();

        //
        // * get date year."Time-only" values have date set to 31-Dec-1899 so if year is "1899" you can assume it is a
        // * "time-only" value
        //
        String dateStamp = formatYearOnly.format(date);

        if (dateStamp.equals("1899")) {
            // Return "Time-only" value as String HH:mm:ss
            return formatTime.format(date);
        } else {
            // here you may have a date-only or date-time value

            // get time as String HH:mm:ss
            String timeStamp = formatTime.format(date);

            if (timeStamp.equals("00:00:00")) {
                // if time is 00:00:00 you can assume it is a date only value (but it could be midnight)
                // return date-time value as "yyyy-dd-MM HH:mm:ss"
                return formatDate.format(date);
            } else {
                // return date-time value as "yyyy-dd-MM HH:mm:ss"
                return formatDate.format(date) + " " + timeStamp;
            }
        }
    }

}
