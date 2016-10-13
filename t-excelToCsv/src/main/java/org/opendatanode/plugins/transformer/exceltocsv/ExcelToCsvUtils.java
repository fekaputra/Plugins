package org.opendatanode.plugins.transformer.exceltocsv;

import java.text.Normalizer;

public class ExcelToCsvUtils {
    
    private ExcelToCsvUtils() {
    }

    /**
     * @param fileName File name, eg. example.xlsx
     * @return Base file name (file name without extension).
     */
    public static String getBaseFileName(String fileName) {
        // for "example.xlsx" parts is ["example", "xlsx"]
        String[] parts = fileName.split("\\.(?=[^\\.]+$)");
        return parts[0];
    }
    
    public static String stripAccents(String s) {
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return s;
    }

}
