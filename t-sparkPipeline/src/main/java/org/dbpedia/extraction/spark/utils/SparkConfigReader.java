package org.dbpedia.extraction.spark.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This class can be used to read simple Spark configuration files
 *
 * @author kay
 *
 */
public class SparkConfigReader {
    /** map which contains all configuration values */
    final Map<String, String> configurationParameters = new HashMap<>();

    /** path to config file */
    final String filePath;

    /** pattern can be used to get rid off all the white spaces */
    Pattern whiteSpace = Pattern.compile("\\s+");

    public SparkConfigReader(final String filePath) {
        this.filePath = filePath;

        // first try to load from the workspace context
        URL filePathUrl = SparkConfigReader.class.getResource(filePath);

        File configFile;
        if (null == filePathUrl) {
            // treat it as absolute path
            configFile = new File(filePath);
        } else {
            configFile = new File(filePathUrl.getFile());
        }

        if (!configFile.exists() || !configFile.isFile()) {
            throw new RuntimeException("Was not read config file: " + filePath);
        }

        try {
            this.readConfigParameters(configFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method can be used to read configuration parameters from a Spark config file
     *
     * @param configFile - config file instance
     * @throws IOException
     */
    protected void readConfigParameters(final File configFile) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(configFile));

        String line = null;
        while (null != (line = reader.readLine())) {
            String cleanLine = line.trim(); // ensure we do not have any white spaces left
            if (cleanLine.startsWith("#") || cleanLine.isEmpty()) {
                continue; // have found a comment
            }

            // get rid off all white space and replace with single space
            cleanLine = whiteSpace.matcher(cleanLine).replaceAll(" ");

            int indexWhiteSpace = cleanLine.indexOf(" ");
            if (0 > indexWhiteSpace) {
                new RuntimeException("Was not able to find end of key end in line: " + line);
            }

            // get key
            String key = cleanLine.substring(0, indexWhiteSpace);
            if (key.isEmpty()) {
                continue;
            }

            // check if we have another comment at the end of the parameter
            int endlineCommentIndex = cleanLine.lastIndexOf("#");

            // get parameter
            String parameter;
            if (0 < endlineCommentIndex) {
                parameter = cleanLine.substring(indexWhiteSpace + 1, endlineCommentIndex).trim();
            } else {
                parameter = cleanLine.substring(indexWhiteSpace + 1);
            }

            // make sure that we have something to save
            if (parameter.isEmpty()) {
                continue;
            }

            // store config values
            this.configurationParameters.put(key, parameter);
        }

        reader.close();
    }

    /**
     *
     * @return configuration parameters which were loaded from the input file
     */
    public Map<String, String> getConfigParameters() {
        return Collections.unmodifiableMap(this.configurationParameters);
    }

}
