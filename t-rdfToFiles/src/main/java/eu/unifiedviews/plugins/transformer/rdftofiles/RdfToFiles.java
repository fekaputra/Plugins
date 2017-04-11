package eu.unifiedviews.plugins.transformer.rdftofiles;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext.MessageType;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.files.FilesDataUnitUtils;
import eu.unifiedviews.helpers.dataunit.files.FilesVocabulary;
import eu.unifiedviews.helpers.dataunit.metadata.MetadataUtils;
import eu.unifiedviews.helpers.dataunit.rdf.RdfDataUnitUtils;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.config.migration.ConfigurationUpdate;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultToleranceUtils;
import eu.unifiedviews.helpers.dpu.extension.rdf.RdfConfiguration;
import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;

@DPU.AsTransformer
public class RdfToFiles extends AbstractDpu<RdfToFilesConfig_V2> {

    private static final Logger LOG = LoggerFactory.getLogger(RdfToFiles.class);

    private static final String FILE_ENCODE = "UTF-8";

    @RdfConfiguration.ContainsConfiguration
    @DataUnit.AsInput(name = "config", optional = true)
    public RDFDataUnit rdfConfiguration;

    @DataUnit.AsInput(name = "input")
    public RDFDataUnit inRdfData;

    @DataUnit.AsOutput(name = "output")
    public WritableFilesDataUnit outFilesData;

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    @ExtensionInitializer.Init(param = "eu.unifiedviews.plugins.transformer.rdftofiles.RdfToFilesConfig__V1")
    public ConfigurationUpdate _ConfigurationUpdate;

    @ExtensionInitializer.Init
    public RdfConfiguration _rdfConfiguration;

    private RDFFormat rdfFormat;

    public RdfToFiles() {
        super(RdfToFilesVaadinDialog.class, ConfigHistory.history(RdfToFilesConfig_V1.class).addCurrent(RdfToFilesConfig_V2.class));
    }

    @Override
    protected void innerExecute() throws DPUException {
        rdfFormat = RDFFormat.valueOf(config.getRdfFileFormat());
        if (rdfFormat == null) {
            throw ContextUtils.dpuException(ctx, "rdfToFiles.error.rdfFortmat.null");
        }

        final List<RDFDataUnit.Entry> graphs = FaultToleranceUtils.getEntries(faultTolerance, inRdfData,
                RDFDataUnit.Entry.class);

        if (graphs.size() > 0) {

            // Create output file.
            final String outputFileName = config.getOutFileName() + "." + rdfFormat.getDefaultFileExtension();
            // Prepare output file entity.
            final FilesDataUnit.Entry outputFile = faultTolerance.execute(new FaultTolerance.ActionReturn<FilesDataUnit.Entry>() {

                @Override
                public FilesDataUnit.Entry action() throws Exception {
                    return FilesDataUnitUtils.createFile(outFilesData, outputFileName);
                }
            });

            exportGraph(graphs, outputFile);
        } else {
            //no data to be exported, no file being produced. 
            ContextUtils.sendMessage(ctx, MessageType.INFO, "rdfToFiles.nodata", "");
        }

    }

    /**
     * Export given graphs into given file. If needed .graph file is also created.
     * 
     * @param sources
     * @param target
     * @throws DPUException
     */
    private void exportGraph(final List<RDFDataUnit.Entry> sources, FilesDataUnit.Entry target) throws DPUException {
        final File targetFile = FaultToleranceUtils.asFile(faultTolerance, target);
        // Create parent directories.
        targetFile.getParentFile().mkdirs();
        // Prepare inputs.
        final URI[] sourceUris = faultTolerance.execute(new FaultTolerance.ActionReturn<URI[]>() {

            @Override
            public URI[] action() throws Exception {
                return RdfDataUnitUtils.asGraphs(sources);
            }
        });
        try (FileOutputStream outStream = new FileOutputStream(targetFile); OutputStreamWriter outWriter = new OutputStreamWriter(outStream, Charset.forName(FILE_ENCODE))) {
            faultTolerance.execute(inRdfData, new FaultTolerance.ConnectionAction() {

                @Override
                public void action(RepositoryConnection connection) throws Exception {
                    RDFWriter writer = Rio.createWriter(rdfFormat, outWriter);
                    // If we export as queads, we insert our own class to rename graphs.
                    if (rdfFormat.supportsContexts()) {
                        RdfWriterContextRenamer writerRenamer = new RdfWriterContextRenamer(writer);
                        final URI targetUri = connection.getValueFactory().createURI(config.getOutGraphName());
                        // Set fixed output context.
                        writerRenamer.setContext(targetUri);
                        writer = writerRenamer;
                    }
                    // Export data.
                    connection.export(writer, sourceUris);
                }
            });
        } catch (IOException ex) {
            throw ContextUtils.dpuException(ctx, ex, "rdfToFiles.error.output");
        }
        // Generate graph.
        if (config.isGenGraphFile() && !rdfFormat.supportsContexts()) {
            generateGraphFile(target, config.getOutGraphName());
        }
    }

    /**
     * Check if file graph should be generated and if so, then generate new graph file.
     * 
     * @param rdfFile
     * @param graphName
     *            Name of the graph, that will be written into .graph file.
     * @throws DPUException
     */
    private void generateGraphFile(final FilesDataUnit.Entry rdfFile, String graphName) throws DPUException {
        final FilesDataUnit.Entry graphFileEntry = faultTolerance.execute(new FaultTolerance.ActionReturn<FilesDataUnit.Entry>() {

            @Override
            public FilesDataUnit.Entry action() throws Exception {
                final String rdfFilePath = MetadataUtils.get(inRdfData, rdfFile, FilesVocabulary.UV_VIRTUAL_PATH);
                return FilesDataUnitUtils.createFile(outFilesData, rdfFilePath + ".graph");
            }
        });
        try {
            FileUtils.writeStringToFile(FaultToleranceUtils.asFile(faultTolerance, graphFileEntry), graphName);
        } catch (IOException ex) {
            throw ContextUtils.dpuException(ctx, ex, "rdfToFiles.error.graphFile");
        }
    }

}
