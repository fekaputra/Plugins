package eu.unifiedviews.plugins;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.files.FilesHelper;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Set;


/**
 * Main data processing unit class.
 *
 * @author Unknown
 */
@DPU.AsTransformer
public class JsonToXml extends AbstractDpu<eu.unifiedviews.plugins.JsonToXmlConfig_V1> {

    private static final Logger log = LoggerFactory.getLogger(JsonToXml.class);

    public JsonToXml() {
        super(eu.unifiedviews.plugins.JsonToXmlVaadinDialog.class, ConfigHistory.noHistory(eu.unifiedviews.plugins.JsonToXmlConfig_V1.class));
    }

    @DataUnit.AsInput(name = "input")
    public FilesDataUnit input;

    @DataUnit.AsOutput(name = "output")
    public WritableFilesDataUnit output;

    @Override
    protected void innerExecute() throws DPUException {

        int totalNumberOfFiles = 0;
        int totalNumberOfCorrectlyProcessedFiles = 0;

        try {
            Set<FilesDataUnit.Entry> files = FilesHelper.getFiles(input);

            for (FilesDataUnit.Entry entry : files) {
                totalNumberOfFiles++;
                String sn = entry.getSymbolicName();
                String uri = entry.getFileURIString();
                String inputString = null;

                log.info("Processing file: {}, uri: {}", sn, uri );

                try {
                    inputString = readFile(FilesHelper.asFile(entry).getAbsolutePath());
                    if (inputString != null) {
                        log.info("Input (first 1000 chars): {}", inputString.substring(0,1000));
                    }
                } catch (IOException e) {
                    log.error(e.getLocalizedMessage(),e);
                }

                JSONObject json = new JSONObject(inputString);
                String outputString = XML.toString(json);

                outputString = "<root>" + outputString + "</root>";

                //prepare output data unit (with the same symbolic name)
                FilesDataUnit.Entry createdEntry = FilesHelper.createFile(output, sn);

                try {
                    writeFile(FilesHelper.asFile(createdEntry).getAbsolutePath(),outputString);
                    if (outputString!= null) {
                        log.info("Output (first 1000 chars): {}", outputString.substring(0,1000));
                    }
                } catch (IOException e) {
                    log.error(e.getLocalizedMessage(),e);
                }

                totalNumberOfCorrectlyProcessedFiles++;

                //TODO copy any input metadata
            }
        } catch (DataUnitException ex) {
            throw ContextUtils.dpuException(ctx, ex, "dpuName.error");
        }

        ContextUtils.sendShortInfo(ctx, "JsonToXml.stats.processedentries", totalNumberOfCorrectlyProcessedFiles, totalNumberOfFiles);



//        String str = "{\"menu\": {\n" +
//                "  \"id\": \"file\",\n" +
//                "  \"value\": \"File\",\n" +
//                "  \"popup\": {\n" +
//                "    \"menuitem\": [\n" +
//                "      {\"value\": \"New\", \"onclick\": \"CreateNewDoc()\"},\n" +
//                "      {\"value\": \"Open\", \"onclick\": \"OpenDoc()\"},\n" +
//                "      {\"value\": \"Close\", \"onclick\": \"CloseDoc()\"}\n" +
//                "    ]\n" +
//                "  }\n" +
//                "}}";
//        JSONObject json = new JSONObject(str);
//        String xml = XML.toString(json);
//
//        log.info(xml);
//
//        ContextUtils.sendShortInfo(ctx, "JsonToXml.message");


    }

//    public static String convert(String json, String root) throws JSONException
//    {
//        org.json.JSONObject jsonFileObject = new org.json.JSONObject(json);
//        String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-15\"?>\n<"+root+">"
//                + org.json.XML.toString(jsonFileObject) + "</"+root+">";
//        return xml;
//    }

    public static String readFile(String filepath) throws FileNotFoundException, IOException
    {

        StringBuilder sb = new StringBuilder();
        InputStream in = new FileInputStream(filepath);
        Charset encoding = Charset.defaultCharset();

        Reader reader = new InputStreamReader(in, encoding);

        int r = 0;
        while ((r = reader.read()) != -1)//Note! use read() rather than readLine()
        //Can process much larger files with read()
        {
            char ch = (char) r;
            sb.append(ch);
        }

        in.close();
        reader.close();

        return sb.toString();
    }

    public static void writeFile(String filepath, String output) throws FileNotFoundException, IOException
    {
        FileWriter ofstream = new FileWriter(filepath);
        try (BufferedWriter out = new BufferedWriter(ofstream)) {
            out.write(output);
        }
    }

}
