package eu.unifiedviews.plugins.extractor.silklinker;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import eu.unifiedviews.helpers.dpu.config.ConfigDialogProvider;
import eu.unifiedviews.helpers.dpu.config.ConfigurableBase;

/**
 * Simple XSLT Extractor
 * 
 * @author tomasknap
 */
@DPU.AsExtractor
public class SilkLinker extends ConfigurableBase<SilkLinkerConfig_V1>
        implements DPU, ConfigDialogProvider<SilkLinkerConfig_V1> {

    private static final Logger log = LoggerFactory.getLogger(
            SilkLinker.class);

    @DataUnit.AsOutput(name = "links_confirmed")
    public WritableRDFDataUnit outputConfirmed;

    @DataUnit.AsOutput(name = "links_to_be_verified")
    public WritableRDFDataUnit outputToVerify;

    /**
     * Constructor
     */
    public SilkLinker() {
        super(SilkLinkerConfig_V1.class);
    }

    @Override
    public AbstractConfigDialog<SilkLinkerConfig_V1> getConfigurationDialog() {
        return new SilkLinkerVaadinDialog();
    }

    @Override
    public void execute(DPUContext context) throws DPUException {
        //inputs (sample config file is in the file module/Silk_Linker/be-sameAs.xml)

        //get Silk conf stored in String (from textarea)
        String configString = config.getSilkConf();

        if (configString == null || configString.isEmpty()) {
            log.error("No config file specifed");
            context.sendMessage(DPUContext.MessageType.ERROR, "No config file specifed: ");
            return;

        }
        log.info("Config file is: {}", configString);

        //prepare temp file where the configString is stored
        File workingDir = context.getWorkingDir();
        File configFile = null;
        try {
            configFile = new File(workingDir.getCanonicalPath() + "/conf.xml");
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(SilkLinker.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            DataUnitUtils.storeStringToTempFile(configString, configFile.getCanonicalPath());
        } catch (IOException ex) {
            log.error(ex.getLocalizedMessage());
        }

        //Adjust outputs 
        // 1) throw away the existing
        // 2) add our output section
        // 3) allow to modify the content (min/max)
        String confirmedLinks = context.getWorkingDir().getAbsolutePath() + File.separator + "confirmed.ttl";
        String toBeVerifiedLinks = context.getWorkingDir().getAbsolutePath() + File.separator + "verify.ttl";

        try {
            //load the document:
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document dom = db.parse(configFile);

            NodeList elementsByTagName = dom.getElementsByTagName("Interlink");

            for (int i = 0; i < elementsByTagName.getLength(); i++) {
                Node interlink = elementsByTagName.item(i);

                //for each link, adjust outputs section
                //remove Outputs section
                // loop the staff child node
                NodeList list = interlink.getChildNodes();

                //remove outputs sections
                for (int j = 0; j < list.getLength(); j++) {

                    Node node = list.item(j);

                    if ("Outputs".equals(node.getNodeName())) {
                        interlink.removeChild(node);
                    }

                }

                //create new elem - outputs section
//                   <Outputs>
//                <Output maxConfidence="0.9" type="file" >
//                  <Param name="file" value="suppliers_verify_links.xml"/>
//                  <Param name="format" value="ntriples"/>
//                </Output>
//                <Output minConfidence="0.9" type="file">
//                  <Param name="file" value="suppliers_accepted_links.xml"/>
//                  <Param name="format" value="ntriples"/>
//                </Output>
//            </Outputs>
                Element outputs = dom.createElement("Outputs");

                //confirmed
                Element output1 = dom.createElement("Output");
                output1.setAttribute("minConfidence", config.getMinConfirmedLinks());
                output1.setAttribute("type", "file");
                Element param1a = dom.createElement("Param");
                param1a.setAttribute("name", "file");
                param1a.setAttribute("value", confirmedLinks);
                output1.appendChild(param1a);
                Element param1b = dom.createElement("Param");
                param1b.setAttribute("name", "format");
                param1b.setAttribute("value", "ntriples");
                output1.appendChild(param1b);
                outputs.appendChild(output1);

                //to be verified
                Element output2 = dom.createElement("Output");
                output2.setAttribute("maxConfidence", config.getMinConfirmedLinks());
                output2.setAttribute("minConfidence", config.getMinLinksToBeVerified());
                output2.setAttribute("type", "file");
                Element param2a = dom.createElement("Param");
                param2a.setAttribute("name", "file");
                param2a.setAttribute("value", toBeVerifiedLinks);
                output2.appendChild(param2a);
                Element param2b = dom.createElement("Param");
                param2b.setAttribute("name", "format");
                param2b.setAttribute("value", "ntriples");
                output2.appendChild(param2b);
                outputs.appendChild(output2);

                //append
                interlink.appendChild(outputs);

            }

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(dom);
            StreamResult result = new StreamResult(configFile);
            transformer.transform(source, result);

        } catch (IOException | ParserConfigurationException | TransformerException | DOMException | SAXException e) {
            log.error(e.getLocalizedMessage());
        }
        try {
            String s = DataUnitUtils.readFile(configFile.getCanonicalPath());
            log.info("Adjusted config file is: {}", s);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(SilkLinker.class.getName()).log(Level.SEVERE, null, ex);
        }

//        Charset charset = StandardCharsets.UTF_8;
//
//        try (BufferedWriter writer = Files.newBufferedWriter(configFile.toPath(), charset)) {
//            writer.write(configString, 0, configString.length());
//        } catch (IOException x) {
//            log.error("IOException: %s%n", x);
//        }

        //File conf = new File(config.getSilkConf());

        //((SilkLinkerVaadinDialog)getConfigurationDialog()).setContext(context);

        //Execution of the Silk linker (xml conf is an important input!)
        //TODO Petr: solve the problem when loading XML conf
        //LOG.info("Silk is being launched");
        //Silk.executeFile(conf, null, Silk.DefaultThreads(), true);
        //LOG.info("Silk finished");

        log.info("Silk is about to be executed");
        try {
            //Process p = Runtime.getRuntime().exec("java -DconfigFile=" + configFile.getCanonicalPath() + " -jar /Users/tomasknap/Documents/PROJECTS/ETL-SWProj/intlib/tmp/silk_2.5.2/silk.jar");

            Process p = Runtime.getRuntime().exec("java -DconfigFile=" + configFile.getCanonicalPath() + " -jar /data/odcs/libs/silk_2.5.3/silk.jar");

            printProcessOutput(p);
        } catch (IOException ex) {
            log.error(ex.getLocalizedMessage());
            context.sendMessage(DPUContext.MessageType.ERROR, "Problem executing Silk: "
                    + ex.getMessage());
        }
        log.info("Silk was executed");

        log.info("Output 'confirmed links' is being prepared");
        RepositoryConnection connection = null;
        try {
            //File f = new File("/Users/tomasknap/.silk/output/confirmed.ttl");
            File f = new File(confirmedLinks);
            if (f.exists()) {
                log.info("File with confirmed links was generated {}", confirmedLinks);
            }
            else {
                log.error("File with confirmed links was NOT generated");
            }

            connection = outputConfirmed.getConnection();
            String baseURI = "";
            URI graph = outputConfirmed.addNewDataGraph("confirmed");
            connection.add(f, baseURI, RDFFormat.TURTLE, graph);

        } catch (IOException | RepositoryException | RDFParseException ex) {
            log.error(ex.getLocalizedMessage());
            context.sendMessage(DPUContext.MessageType.ERROR, "RDFException: "
                    + ex.getMessage());
        } catch (DataUnitException ex) {
            context.sendMessage(DPUContext.MessageType.ERROR, "DataUnitException: "
                    + ex.getMessage());
            throw new DPUException(ex);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {
                    context.sendMessage(DPUContext.MessageType.WARNING, ex.getMessage(), ex.fillInStackTrace().toString());
                }
            }
        }

        log.info("Output 'to verify links' is being prepared");
        RepositoryConnection connection2 = null;
        try {

            //File f = new File("/Users/tomasknap/.silk/output/verify.ttl");
            File f = new File(toBeVerifiedLinks);
            if (f.exists()) {
                log.info("File with links to be verfied was generated, {}", toBeVerifiedLinks);
            }
            else {
                log.error("File with links to be verfied was NOT generated");
            }

            connection2 = outputToVerify.getConnection();
            String baseURI = "";
            URI graph = outputToVerify.addNewDataGraph("toverify");
            connection2.add(f, baseURI, RDFFormat.TURTLE, graph);
        } catch (IOException | RepositoryException | RDFParseException ex) {
            log.error(ex.getLocalizedMessage());
            context.sendMessage(DPUContext.MessageType.ERROR, "RDFException: "
                    + ex.getMessage());
        } catch (DataUnitException ex) {
            context.sendMessage(DPUContext.MessageType.ERROR, "DataUnitException: "
                    + ex.getMessage());
            throw new DPUException(ex);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {
                    log.warn("Error when closing connection", ex);
                    // eat close exception, we cannot do anything clever here
                }
            }
        }

    }

    private static void printProcessOutput(Process process) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder errors = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                errors.append(line);
            }
            log.warn(errors.toString());
            in.close();

            in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder notes = new StringBuilder();

            while ((line = in.readLine()) != null) {
                notes.append(line);
            }
            log.debug(notes.toString());
            in.close();
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
        }
    }
}
