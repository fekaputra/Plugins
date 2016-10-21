package org.opendatanode.plugins.transformer.exceltocsv;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Iterator;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import eu.unifiedviews.dataunit.files.FilesDataUnit.Entry;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.helpers.dataunit.files.FilesHelper;
import eu.unifiedviews.helpers.dpu.test.config.ConfigurationBuilder;

/**
 * Test generates csv files from example of excel files. It does not checks content of generated csv files, it only print
 * content of csv files to log.
 * 
 * @author eea-edo
 */
public class ExcelToCsvTest {

    private static final Logger log = LoggerFactory.getLogger(ExcelToCsvTest.class);

    @Test
    public void execute() throws Exception {
        // Prepare config.
        ExcelToCsvConfig_V1 config = new ExcelToCsvConfig_V1();
        config.setSheetNames("list1:list2:list3");
        config.setCsvFileNamePattern("${excelFileName}_${sheetName}.csv");

        // Prepare DPU.
        ExcelToCsv excelToCsv = new ExcelToCsv();
        excelToCsv.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());

        // Prepare test environment.
        TestEnvironment environment = new TestEnvironment();

        // Prepare data unit.
        WritableFilesDataUnit filesInput = environment.createFilesInput("input");
        WritableFilesDataUnit filesOutput = environment.createFilesOutput("output");

        try {
          
            {
                URL url = getClass().getClassLoader().getResource("example1.xlsx");
                FilesHelper.addFile(filesInput, new File(url.toURI()));
            }
            {
                URL url = getClass().getClassLoader().getResource("example2.xlsx");
                FilesHelper.addFile(filesInput, new File(url.toURI()));
            }

            // Run.
            environment.run(excelToCsv);

            // Get file iterator.
            Iterator<Entry> outputEntries = FilesHelper.getFiles(filesOutput).iterator();

            // Iterate over files.
            while (outputEntries.hasNext()) {
                Entry outputEntry = outputEntries.next();
                File csvFile = new File(new URI(outputEntry.getFileURIString()));
                log.info("----------------");
                log.info(outputEntry.getSymbolicName());
                log.info("----------------");
                for (String line : Files.readAllLines(csvFile.toPath(), Charset.forName("UTF-8"))) {
                    log.info(line);
                }
                log.info("----------------");

                // stores file to the disk
                /*
                 * File outFile = new File("c:/_eea/UnifiedViews/test-results", outputEntry.getSymbolicName());
                 * log.info("out file: {}", outFile.getAbsolutePath()); FileUtils.copyFile(csvFile, outFile);
                 */
            }
        } finally {
            // Release resources.
            environment.release();
        }
    }

}
