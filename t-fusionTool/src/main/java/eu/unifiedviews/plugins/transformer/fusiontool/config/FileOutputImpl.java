/**
 * 
 */
package eu.unifiedviews.plugins.transformer.fusiontool.config;

import cz.cuni.mff.odcleanstore.fusiontool.io.EnumSerializationFormat;
import org.eclipse.rdf4j.model.IRI;

import java.io.File;


/**
 * Container of settings for an file output of result data.
 * @author Jan Michelfeit
 */
public class FileOutputImpl implements FileOutput {
    private final File path;
    private final EnumSerializationFormat format;
    private IRI metadataContext;
    private IRI dataContext;
    
    /**
     * @param path output path
     * @param format file serialization format
     */
    public FileOutputImpl(File path, EnumSerializationFormat format) {
        this.path = path;
        this.format = format;
    }

    public FileOutputImpl(File path, EnumSerializationFormat format, IRI dataContext, IRI metadataContext) {
        this.path = path;
        this.format = format;
        this.dataContext = dataContext;
        this.metadataContext = metadataContext;
    }

    @Override
    public File getPath() {
        return path; 
    }
    
    @Override
    public EnumSerializationFormat getFormat() {
        return format; 
    }

    @Override
    public IRI getMetadataContext() {
        return metadataContext;
    }
    
    /**
     * Sets value for {@link #getMetadataContext()}.
     * @param metadataContext named graph IRI
     */
    public void setMetadataContext(IRI metadataContext) {
        this.metadataContext = metadataContext;
    }
    
    @Override
    public IRI getDataContext() {
        return dataContext;
    }
    
    /**
     * Sets value for {@link #getDataContext()}.
     * @param dataContext named graph IRI
     */
    public void setDataContext(IRI dataContext) {
        this.dataContext = dataContext;
    }
}
