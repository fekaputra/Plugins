package eu.unifiedviews.plugins.transformer.rdftofiles;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.virtualpathhelper.VirtualPathHelpers;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import eu.unifiedviews.helpers.dpu.config.ConfigDialogProvider;
import eu.unifiedviews.helpers.dpu.config.ConfigurableBase;

import org.apache.commons.io.FileUtils;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Škoda Petr
 */
@DPU.AsTransformer
public class RdfToFiles extends ConfigurableBase<RdfToFilesConfiguration> implements
        ConfigDialogProvider<RdfToFilesConfiguration> {

    private static final Logger LOG = LoggerFactory.getLogger(RdfToFiles.class);

    private static final String FILE_ENCODE = "UTF-8";

    @DataUnit.AsInput(name = "input")
    public RDFDataUnit inRdfData;

    @DataUnit.AsOutput(name = "output")
    public WritableFilesDataUnit outFilesData;

    private DPUContext context;

    private RDFFormat rdfFormat;

    public RdfToFiles() {
        super(RdfToFilesConfiguration.class);
    }

    @Override
    public AbstractConfigDialog<RdfToFilesConfiguration> getConfigurationDialog() {
        return new RdfToFilesVaadinDialog();
    }

    @Override
    public void execute(DPUContext context) throws DPUException {
        this.context = context;
        rdfFormat = RDFFormat.valueOf(config.getRdfFileFormat());
        if (rdfFormat == null) {
            context.sendMessage(DPUContext.MessageType.ERROR, "Unknonw RDF format: " + config.getRdfFileFormat());
            return;
        }
        //
        // get input graph uris and symbolicNames
        //
        final Map<String, URI> graphUris = new HashMap<>();
        try (RDFDataUnit.Iteration iter = inRdfData.getIteration()) {
            while (iter.hasNext()) {
                final RDFDataUnit.Entry entry = iter.next();
                graphUris.put(entry.getSymbolicName(), entry.getDataGraphURI());
            }
        } catch (DataUnitException ex) {
            context.sendMessage(DPUContext.MessageType.ERROR,
                    "Failed to get graph names.", "", ex);
            return;
        }

        //
        // convert from rdf to files
        //
        try {
            // TODO export metadata graph ?!!
            if (config.isMergeGraphs()) {
                exportSingle(graphUris);
            } else {
                exportMultiple(graphUris);
            }

        } catch (DataUnitException ex) {
            context.sendMessage(DPUContext.MessageType.ERROR, "DPU Failed.", "Problem with DataUnit.", ex);
        } catch (ExportFailedException ex) {
            context.sendMessage(DPUContext.MessageType.ERROR, "DPU Failed.", "", ex);
        }
    }

    /**
     * Export all data as a single graph. Also take care about generation of
     * .graph file if needed. If generation of .graph file fails then notify
     * backend with ERROR message.
     * 
     * @param graphUris
     * @throws DataUnitException
     * @throws eu.unifiedviews.plugins.extractor.sparql.ExportFailedException
     */
    private void exportSingle(Map<String, URI> graphUris) throws DataUnitException, ExportFailedException {
        final RdfToFilesConfiguration.GraphToFileInfo info = config.getGraphToFileInfo().get(0);
        // export
        final URI[] toExport = graphUris.values().toArray(new URI[0]);
        final String outputSymbolicName = exportGraph(toExport, info.getOutFileName());
        // create graph name if needed
        if (config.isGenGraphFile()) {
            try {
                generateGraphFile(info.getOutFileName(), config.getOutGraphName());
            } catch (IOException ex) {
                context.sendMessage(DPUContext.MessageType.ERROR, "Failed to create .graph file", "", ex);
            }
        }
        // TODO transfer metadata + data about source?
    }

    /**
     * Export graphs based on options in {@link #config}.
     * 
     * @param graphUris
     * @throws DataUnitException
     * @throws eu.unifiedviews.plugins.extractor.sparql.ExportFailedException
     */
    private void exportMultiple(Map<String, URI> graphUris) throws DataUnitException, ExportFailedException {
        for (RdfToFilesConfiguration.GraphToFileInfo info : config.getGraphToFileInfo()) {
            //
            // get URIs (graphs) to export and transfer metadata
            //
            final List<URI> sourceUri = new LinkedList<>();
            final List<String> sourceSymbolicNames = new LinkedList<>();
            for (String item : graphUris.keySet()) {
                if (item.matches(info.getInSymbolicName())) {
                    // we need symbolic name for metadata
                    // and uri to transfer data
                    sourceUri.add(graphUris.get(item));
                    sourceSymbolicNames.add(item);
                }
            }
            //
            // export
            //
            final String outputSymbolicName = exportGraph(sourceUri.toArray(new URI[0]), info.getOutFileName());
            //
            // TODO transfer metadata
            //

            // check cancel
            if (context.canceled()) {
                return;
            }
        }
    }

    /**
     * Export content of given graphs into file of given name.
     * 
     * @param uris
     * @param fileName
     *            File name (suffix), virtual path.
     * @throws DataUnitException
     * @throws ExportFailedException
     * @return Symbolic name of output file.
     */
    private String exportGraph(URI[] uris, String fileName) throws DataUnitException, ExportFailedException {
        final String outputSymbolicName = fileName + rdfFormat.getDefaultFileExtension();
        final String outputFileUri = outFilesData.addNewFile(outputSymbolicName);

        final File outputFile = new File(java.net.URI.create(outputFileUri));
        // create parent
        outputFile.getParentFile().mkdirs();

        RepositoryConnection connection = null;
        try (FileOutputStream outStream = new FileOutputStream(outputFile); OutputStreamWriter outWriter = new OutputStreamWriter(outStream, Charset.forName(FILE_ENCODE))) {
            connection = inRdfData.getConnection();
            final RDFWriter writer = Rio.createWriter(rdfFormat, outWriter);
            // export
            connection.export(writer, uris);
        } catch (IOException ex) {
            throw new ExportFailedException("IO exception.", ex);
        } catch (RepositoryException | RDFHandlerException ex) {
            throw new ExportFailedException("Problem with RDF.", ex);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {
                    LOG.error("Failed to close repository connection.", ex);
                }
            }
        }

        // add metadata about virtual path
        VirtualPathHelpers.setVirtualPath(outFilesData, outputSymbolicName, fileName);
        return outputSymbolicName;
    }

    /**
     * Generate .graph file with give names.
     * 
     * @param graphName
     */
    private void generateGraphFile(String filePrefix, String graphName) throws DataUnitException, IOException {
        final String outputSymbolicName = filePrefix + ".graph";
        final String fileLocation = outFilesData.addNewFile(outputSymbolicName);
        // write into file
        LOG.debug("Writing .graph file into: {}", fileLocation.toString());
        FileUtils.writeStringToFile(new File(java.net.URI.create(fileLocation)), graphName);
        // add metadata about virtual path
        VirtualPathHelpers.setVirtualPath(outFilesData, outputSymbolicName, outputSymbolicName);
    }

}
