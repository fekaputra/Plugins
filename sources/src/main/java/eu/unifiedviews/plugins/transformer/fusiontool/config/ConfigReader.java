/**
 * 
 */
package eu.unifiedviews.plugins.transformer.fusiontool.config;

import cz.cuni.mff.odcleanstore.conflictresolution.ResolutionStrategy;
import cz.cuni.mff.odcleanstore.conflictresolution.impl.ResolutionStrategyImpl;
import cz.cuni.mff.odcleanstore.core.ODCSUtils;
import eu.unifiedviews.plugins.transformer.fusiontool.config.xml.ConfigXml;
import eu.unifiedviews.plugins.transformer.fusiontool.config.xml.ConflictResolutionXml;
import eu.unifiedviews.plugins.transformer.fusiontool.config.xml.FileOutputXml;
import eu.unifiedviews.plugins.transformer.fusiontool.config.xml.ParamXml;
import eu.unifiedviews.plugins.transformer.fusiontool.config.xml.PrefixXml;
import eu.unifiedviews.plugins.transformer.fusiontool.config.xml.PropertyResolutionStrategyXml;
import eu.unifiedviews.plugins.transformer.fusiontool.config.xml.PropertyXml;
import eu.unifiedviews.plugins.transformer.fusiontool.config.xml.ResolutionStrategyXml;
import eu.unifiedviews.plugins.transformer.fusiontool.exceptions.InvalidInputException;
import eu.unifiedviews.plugins.transformer.fusiontool.io.EnumSerializationFormat;
import eu.unifiedviews.plugins.transformer.fusiontool.io.FileNameSanitizer;
import eu.unifiedviews.plugins.transformer.fusiontool.util.NamespacePrefixExpander;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Reads the XML configuration file and produces instances of configuration in a {@link ConfigContainer} instance.
 * @author Jan Michelfeit
 */
public final class ConfigReader {
    private static final ValueFactory VALUE_FACTORY = ValueFactoryImpl.getInstance();
    
    /**
     * Parses the given configuration file and produces returns the contained
     * configuration as an {@link ConfigContainer} instance.
     * @param configString configuration XMl file
     * @return parsed configuration
     * @throws InvalidInputException parsing error
     */
    public static ConfigContainer parseConfigXml(String configString) throws InvalidInputException {
        ConfigReader instance = new ConfigReader();
        return instance.parseConfigXmlImpl(configString);
    }

    private ConfigContainer parseConfigXmlImpl(String configString) throws InvalidInputException {
        ConfigContainerImpl config = new ConfigContainerImpl();
        if (ODCSUtils.isNullOrEmpty(configString.trim())) {
            return config;
        }
        
        Serializer serializer = new Persister();
        ConfigXml configXml;
        try {
            configXml = serializer.read(ConfigXml.class, configString);
        } catch (Exception e) {
            throw new InvalidInputException("Error parsing configuration file", e);
        }

        // Prefixes
        Map<String, String> prefixes;
        if (configXml.getPrefixes() != null) {
            prefixes = extractPrefixes(configXml.getPrefixes());
        } else {
            prefixes = new HashMap<String, String>();
        }
        config.setPrefixes(Collections.unmodifiableMap(prefixes));

        // Data processing settings
        if (configXml.getDataProcessing() != null) {
            config.setSeedResourceSparqlQuery(configXml.getDataProcessing().getSeedResourceSparqlQuery());
            List<ParamXml> params = configXml.getDataProcessing().getParams();
            if (params != null) {
                extractDataProcessingParams(params, config);
            }
        }
        
        // Conflict resolution settings
        NamespacePrefixExpander prefixExpander = new NamespacePrefixExpander(config.getPrefixes());
        if (configXml.getConflictResolution() != null) {
            ConflictResolutionXml crXml = configXml.getConflictResolution();
            if (crXml.getDefaultResolutionStrategy() != null) {
                config.setDefaultResolutionStrategy(extractResolutionStrategy(crXml.getDefaultResolutionStrategy()));
            } 
            config.setPropertyResolutionStrategies(extractPropertyResolutionStrategies(
                    crXml.getPropertyResolutionStrategies(),
                    prefixExpander));
        }

        // File outputs
        List<FileOutput> outputs = new LinkedList<>();
        if (configXml.getFileOutputs() != null) {
            for (FileOutputXml fileOutputXml : configXml.getFileOutputs().getFileOutputs()) {
                outputs.add(extractFileOutput(fileOutputXml));
            }
            if (configXml.getFileOutputs().getMaxResolvedQuads() != null) {
                config.setMaxOutputTriples(configXml.getFileOutputs().getMaxResolvedQuads());
            }
        }
        config.setFileOutputs(outputs);
        return config;
    }

    private Map<URI, ResolutionStrategy> extractPropertyResolutionStrategies(
            List<PropertyResolutionStrategyXml> propertyResolutionStrategies,
            NamespacePrefixExpander prefixExpander)
            throws InvalidInputException {
        Map<URI, ResolutionStrategy> result = new HashMap<URI, ResolutionStrategy>(propertyResolutionStrategies.size());
        for (PropertyResolutionStrategyXml strategyXml : propertyResolutionStrategies) {
            ResolutionStrategy strategy = extractResolutionStrategy(strategyXml);
            for (PropertyXml propertyXml : strategyXml.getProperties()) {
                URI uri = VALUE_FACTORY.createURI(prefixExpander.expandPrefix(propertyXml.getId()));
                result.put(uri, strategy);
            }
        }
        return result;
    }

    private ResolutionStrategy extractResolutionStrategy(ResolutionStrategyXml strategyXml) {
        ResolutionStrategyImpl strategy = new ResolutionStrategyImpl();
        strategy.setResolutionFunctionName(strategyXml.getResolutionFunctionName());
        strategy.setCardinality(strategyXml.getCardinality());
        strategy.setAggregationErrorStrategy(strategyXml.getAggregationErrorStrategy());
        if (strategyXml.getParams() != null) {
            strategy.setParams(extractAllParams(strategyXml.getParams()));
        }
        return strategy;
    }
    

    private FileOutput extractFileOutput(FileOutputXml fileOutputXml) throws InvalidInputException {
        File path = new File(fileOutputXml.getPath());
        if (path.getAbsoluteFile() == null || path.getAbsoluteFile().getParentFile() == null) {
            throw new InvalidInputException("Path '" + fileOutputXml.getPath() + "' is not a valid file path."); 
        }
        
        EnumSerializationFormat format = EnumSerializationFormat.parseFormat(fileOutputXml.getFormat());
        if (format == null) {
            throw new InvalidInputException("Unknown file output format '" + fileOutputXml.getFormat() + "'");
        }
        
        FileOutputImpl output = new FileOutputImpl(path, format);
        
        String metadataContextString = fileOutputXml.getMetadataContext();
        if (!ODCSUtils.isNullOrEmpty(metadataContextString)) {
            URI context = convertToURI(metadataContextString, "metadataContext is not a valid URI");
            output.setMetadataContext(context);
        }
        
        String dataContextString = fileOutputXml.getDataContext();
        if (!ODCSUtils.isNullOrEmpty(dataContextString)) {
            URI context = convertToURI(dataContextString, "dataContext is not a valid URI");
            output.setDataContext(context);
        }

        return output;
    }
    
    private URI convertToURI(String str, String errorMessage) throws InvalidInputException {
        try {
            return ValueFactoryImpl.getInstance().createURI(str);
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException(errorMessage, e);
        }
    }

    private Map<String, String> extractAllParams(List<ParamXml> params) {
        Map<String, String> result = new HashMap<String, String>(params.size());
        for (ParamXml param : params) {
            result.put(param.getName(), param.getValue());
        }
        return result;
    }

    private Map<String, String> extractPrefixes(List<PrefixXml> prefixes) {
        Map<String, String> prefixMap = new HashMap<String, String>();
        for (PrefixXml prefixXml : prefixes) {
            prefixMap.put(prefixXml.getId(), prefixXml.getNamespace());
        }
        return prefixMap;
    }

    private void extractDataProcessingParams(List<ParamXml> params, ConfigContainerImpl config) throws InvalidInputException {
        for (ParamXml param : params) {
            if (param.getValue() == null) {
                continue;
            } else {
                throw new InvalidInputException("Unknown parameter " + param.getName()
                        + " used in conflict resolution parameters");
            }
        }
    }

    private ConfigReader() {
    }
}
