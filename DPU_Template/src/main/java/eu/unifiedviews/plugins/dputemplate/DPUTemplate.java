package eu.unifiedviews.plugins.dputemplate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.Set;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.rdfhelper.RDFHelper;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import eu.unifiedviews.helpers.dpu.config.ConfigDialogProvider;
import eu.unifiedviews.helpers.dpu.config.ConfigurableBase;

/**
 * We choose the type of this {@link DPU}, it can be {@link DPU.AsExtractor}, {@link DPU.AsLoader}, {@link DPU.AsTransformer}.
 * For this tutorial, we will program transformer DPU.
 *
 * If your {@link DPU} does not have any configuration dialog, you can declare is simply by
 * <p><blockquote><pre>
 * public class {@link DPUTemplate} implements {@link DPU}
 * </pre></blockquote></p>
 */
@DPU.AsTransformer
public class DPUTemplate extends ConfigurableBase<DPUTemplateConfig_V1> implements ConfigDialogProvider<DPUTemplateConfig_V1> {
    /**
     * We use slf4j for logging
     */
    private static final Logger LOG = LoggerFactory.getLogger(DPUTemplate.class);

    /**
     * We define one data unit on input, containing RDF graphs ({@link RDFDataUnit})
     * The name in {@link DataUnit.AsInput} has to be designed carefully, as once user creates pipelines with
     * your {@link DPU}, you can not change the name (it would break the pipeline graph).
     */
    @DataUnit.AsInput(name = "rdfIinput")
    public RDFDataUnit rdfInput;

    /**
     * We define one data unit on output, containing files ({@link FilesDataUnit}).
     */
    @DataUnit.AsOutput(name = "filesOutput")
    public WritableFilesDataUnit filesOutput;

    /**
     * Public non-parametric constructor has to call super constructor in {@link ConfigurableBase}
     */
    public DPUTemplate() {
        super(DPUTemplateConfig_V1.class);
    }

    /**
     * Simple getter which is used by container to obtain configuration dialog instance.
     */
    @Override
    public AbstractConfigDialog<DPUTemplateConfig_V1> getConfigurationDialog() {
        return new DPUTemplateVaadinDialog();
    }

    /**
     * We implement the main method called "execute", which is being called when the {@link DPU} is launched.
     *
     * DPU's configuration is accessible under 'this.config'
     * DPU's context is accessible under 'dpuContext'
     *
     * Let's write simple RDF graph to file transformer DPU
     * It will export each RDF data graph from rdfInput to single RDF+XML file on the filesOutput
     * Copy any metadata from graph to file to be neat to others using them
     * And finally, we will generate one new file, which name is configured by user in dialog
     * and it will contain list of symbolicName;graphUri;fileLocation for each graph-file pair on each line
     * it is of no practical meaning, just to show the API
     *
     */
    @Override
    public void execute(DPUContext dpuContext) throws DPUException {
        /**
         * Lets be nice and log that we are starting and the configuration we have.
         */
        String shortMessage = this.getClass().getSimpleName() + " starting.";
        String longMessage = String.valueOf(config);
        /**
         * You can send messages for user to read, up to ~10 messages per whole {@link DPU} execution.
         * Messages have only informative value for the user, stating that work is being done
         * or updating progress information. Only most relevant events should be sent as messages.
         */
        dpuContext.sendMessage(DPUContext.MessageType.INFO, shortMessage, longMessage);

        /**
         * Obtain all input graphs into one Set
         * This is suitable up to ~100 000 graphs,
         * if you need to process more graphs, consult {@link RDFDataUnit.Iteration} documentation.
         */
        Set<RDFDataUnit.Entry> inputGraphs = null;
        try {
            inputGraphs = RDFHelper.getGraphs(rdfInput);
        } catch (DataUnitException ex) {
            /**
             * There is nothing we can do if container fails to provide us data.
             */
            throw new DPUException("Could not obtain input graphs", ex);
        }

        /**
         * Obtain connection to internal RDF storage and use it to export data to files.
         */
        RepositoryConnection connection = null;
        try {
            /**
             * Get the connection
             */
            connection = rdfInput.getConnection();
            /**
             * Iterate all input graphs
             */
            for (RDFDataUnit.Entry inputGraph : inputGraphs) {
                FileWriter fileWriter = null;
                try {
                    /**
                     * We export each graph into one file. So we are polite and copy all metadata we can about one symbolicName
                     * on input.
                     * The outputFileURIString will be something in form
                     * file:/tmp/42198412907
                     */
                    String outputFileURIString = filesOutput.addNewFile(inputGraph.getSymbolicName());
                    /**
                     * Lets create {@link File} object, notice we have to create {@link URI} from
                     * the outputFileURIString before providing it to {@link File} constructor.
                     */
                    File outputFile = new File(URI.create(outputFileURIString));

                    fileWriter = new FileWriter(outputFile);
                    connection.export(Rio.createWriter(RDFFormat.RDFXML, fileWriter));
                } catch (IOException | DataUnitException | RepositoryException | RDFHandlerException | UnsupportedRDFormatException ex) {
                    /**
                     * Should we just skip the graph or should we fail execution
                     */
                    if (Boolean.TRUE.equals(config.getSkipGraphOnError())) {
                        /**
                         * Lets log the problem.
                         * Please, do not send messages using {@link DPUContext#sendMessage} is similar situations,
                         * since number of graphs or files on input can be large (~100000), sending messages for each graph/file
                         * would fill the database and cause performance penalty.
                         */
                        LOG.warn("Unable to process graph {} into file.", inputGraph, ex);
                    } else {
                        /**
                         * Do not skip graph, so lets fail execution by re-throwing the exception
                         */
                        throw new DPUException("Unable to process graph " + inputGraph + " into file. Not set to skip graph, failing execution.", ex);
                    }
                } finally {
                    if (fileWriter != null) {
                        try {
                            fileWriter.close();
                        } catch (IOException ex) {
                            LOG.warn("Error in close", ex);
                        }
                    }
                }
            }
        } catch (DataUnitException ex) {
            /**
             * There is nothing we can do, if the container is not providing us data
             */
            throw new DPUException("Error in execution", ex);
        } finally {
            /**
             * We have to close the connection after we have used it
             */
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {
                    /**
                     * There is nothing we can do if close throws exception, just log it
                     */
                    LOG.warn("Error in close", ex);
                }
            }
        }

    }
}
