package module;

import gui.ConfigDialog;

import com.vaadin.ui.CustomComponent;

import cz.cuni.xrg.intlib.commons.DPUExecutive;
import cz.cuni.xrg.intlib.commons.DpuType;
import cz.cuni.xrg.intlib.commons.configuration.Config;
import cz.cuni.xrg.intlib.commons.configuration.ConfigException;
import cz.cuni.xrg.intlib.commons.data.DataUnitType;
import cz.cuni.xrg.intlib.commons.data.rdf.RDFDataRepository;
import cz.cuni.xrg.intlib.commons.extractor.ExtractContext;
import cz.cuni.xrg.intlib.commons.extractor.ExtractException;
import cz.cuni.xrg.intlib.commons.message.MessageType;
import cz.cuni.xrg.intlib.commons.web.GraphicalExtractor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jiri Tomes
 * @author Petyr
 */
public class RDF_extractor implements GraphicalExtractor, DPUExecutive {

    /**
     * Config component.
     */
    private gui.ConfigDialog configDialog = null;
    /**
     * DPU configuration.
     */
    private Config config = null;
    /**
     * Logger class.
     */
    private Logger logger = LoggerFactory.getLogger(RDF_extractor.class);

    @Override
    public void saveConfigurationDefault(Config configuration) {
        configuration.setValue(Config.SPARQL_endpoint.name(), "http://");
        configuration.setValue(Config.Host_name.name(), "");
        configuration.setValue(Config.Password.name(), "");
        configuration.setValue(Config.GraphsUri.name(), new LinkedList<String>());
        configuration.setValue(Config.SPARQL_query.name(), "CONSTRUCT {?s ?p ?o} where {?s ?p ?o}");
    }

    @Override
    public DpuType getType() {
        return DpuType.EXTRACTOR;
    }

    @Override
    public CustomComponent getConfigurationComponent(Config configuration) {
        // does dialog exist?
        if (this.configDialog == null) {
            // create it
            this.configDialog = new ConfigDialog();
            this.configDialog.setConfiguration(configuration);
        }
        return this.configDialog;
    }

    @Override
    public void loadConfiguration(Config configuration)
            throws ConfigException {

        if (this.configDialog == null) {
        } else {
            this.configDialog.setConfiguration(configuration);
        }
    }

    @Override
    public void saveConfiguration(Config configuration) {

        this.config = configuration;
        if (this.configDialog == null) {
        } else {
            this.configDialog.getConfiguration(this.config);
        }
    }

    /**
     * Implementation of module functionality here.
     *
     */
    private String getSPARQLEndpoinURLAsString() {
        String endpoint = (String) config.getValue(Config.SPARQL_endpoint.name());
        return endpoint;
    }

    private String getHostName() {
        String hostName = (String) config.getValue(Config.Host_name.name());

        if (hostName == null) {
            hostName = "";
        }
        return hostName;
    }

    private String getPassword() {
        String password = (String) config.getValue(Config.Password.name());

        if (password == null) {
            password = "";
        }
        return password;
    }

    private List<String> getGraphsURI() {
        List<String> graphs = (List<String>) config.getValue(Config.GraphsUri.name());

        if (graphs == null) {
            graphs = new LinkedList<>();
        }
        return graphs;
    }

    private String getQuery() {
        String query = (String) config.getValue(Config.SPARQL_query.name());
        return query;
    }

    @Override
    public void extract(ExtractContext context) throws ExtractException {
        RDFDataRepository repository;
        // create repository
        repository = (RDFDataRepository) context.getDataUnitFactory().create(DataUnitType.RDF);

        if (repository == null) {
            throw new ExtractException("DataUnitFactory returned null.");
        }

        context.addOutputDataUnit(repository);

        final String endpoint = getSPARQLEndpoinURLAsString();
        try {
            final URL endpointURL = new URL(endpoint);
            final String hostName = getHostName();
            final String password = getPassword();
            final List<String> defaultGraphsUri = getGraphsURI();
            final String query = getQuery();

            logger.debug("configuration:");
            logger.debug("endpointURL: " + endpointURL.toString());
            logger.debug("hostName: " + hostName);

            repository.extractfromSPARQLEndpoint(endpointURL, defaultGraphsUri, query, hostName, password);

        } catch (MalformedURLException ex) {
            context.sendMessage(MessageType.ERROR, "MalformedURLException: " + ex.getMessage());
            throw new ExtractException(ex);
        }
    }
}
