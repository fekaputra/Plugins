package eu.unifiedviews.plugins.extractor.sparqlendpoint;

import eu.unifiedviews.helpers.dpu.extension.rdf.validation.RdfValidation;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.DataUnitUtils;
import eu.unifiedviews.helpers.dataunit.rdf.RdfDataUnitUtils;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.config.migration.ConfigurationUpdate;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.rdf.profiler.RdfProfiler;
import eu.unifiedviews.helpers.dpu.extension.rdf.simple.WritableSimpleRdf;
import eu.unifiedviews.plugins.extractor.rdffromsparql.RdfFromSparqlEndpointConfig_V1;

/**
 * Main data processing unit class.
 * 
 */
@DPU.AsExtractor
public class SparqlEndpoint extends AbstractDpu<SparqlEndpointConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(SparqlEndpoint.class);

    @DataUnit.AsOutput(name = "output")
    public WritableRDFDataUnit rdfOutput;

    @ExtensionInitializer.Init(param = "rdfOutput")
    public WritableSimpleRdf output;

    @ExtensionInitializer.Init
    public RdfProfiler rdfProfiler;

    @ExtensionInitializer.Init
    public RdfValidation rdfValidator;

    @ExtensionInitializer.Init(param = "eu.unifiedviews.plugins.extractor.rdffromsparql.RdfFromSparqlEndpointConfig_V1")
    public ConfigurationUpdate _ConfigurationUpdate;

    public SparqlEndpoint() {
        super(SparqlEndpointVaadinDialog.class,
                ConfigHistory.history(SparqlEndpointConfig_V1.class)
                        .alternative(cz.cuni.mff.xrg.uv.extractor.sparqlendpoint.SparqlEndpointConfig_V1.class)
                        .alternative(RdfFromSparqlEndpointConfig_V1.class)
                        .addCurrent(SparqlEndpointConfig_V1.class));
    }

    @Override
    protected void innerExecute() throws DPUException {

        // Prepares output entry
        LOG.info("Preparing output data entry");
        RDFDataUnit.Entry outputEntry = null;
        try {
            outputEntry = RdfDataUnitUtils.addGraph(rdfOutput, DataUnitUtils.generateSymbolicName(this.getClass()));
        } catch (DataUnitException e) {
            LOG.error(e.getLocalizedMessage(), e.getStackTrace());
        }

        output.setOutput(outputEntry);

        LOG.info("Connecting to remote repository (to fetch the data)");
        final RemoteRdfDataUnit remote;
        try {
            remote = new RemoteRdfDataUnit(ctx.getExecMasterContext(), config.getEndpoint(), new IRI[0]);
            // Add for release at the end of execution.
            ctx.getExecMasterContext().getExtensions().add(remote);
        } catch (ExternalError ex) {
            throw ContextUtils.dpuException(ctx, ex, "SparqlEndpoint.exec.cantConnect ");
        }

        LOG.info("Fetching data from remote repository");
        RepositoryConnection remoteConnection = null;
        try {
            remoteConnection = remote.getConnection();
        } catch (DataUnitException e) {
            LOG.error(e.getLocalizedMessage(), e.getStackTrace());
        }
        if (config.getChunkSize() == null || config.getChunkSize() == -1) {
            //no chunks
            GraphQuery query = null;
            try {
                query = remoteConnection.prepareGraphQuery(QueryLanguage.SPARQL, config.getQuery());
            } catch (RepositoryException e) {
                LOG.error(e.getLocalizedMessage(), e.getStackTrace());
            } catch (MalformedQueryException e) {
                LOG.error(e.getLocalizedMessage(), e.getStackTrace());
            }
            try {
                LOG.info("Executing query for {}", config.getEndpoint());
                GraphQueryResult result = query.evaluate();
                LOG.info("Storing result to the working store");
                long counter = 0;

                while (result.hasNext()) {
                    final Statement st = result.next();
                    // Add to out output.
                    output.add(st.getSubject(), st.getPredicate(), st.getObject());
                    // Print info.
                    ++counter;
                    if (counter % 100000 == 0) {
                        LOG.info("{} triples extracted", counter);
                    }
                }
                LOG.info("Data fetched and stored from remote repository.");
            } catch (QueryEvaluationException e) {
                LOG.error(e.getLocalizedMessage(),e.getStackTrace());
            }
        } else {
            String origQuery = config.getQuery();
            LOG.debug("Original query: {}", origQuery);
            boolean returnedSomeTriples = true;
            long offset = 0;
            long limit = config.getChunkSize();
            long counter = 0;
            if (QueryPagingRewriter2.hasLimit(origQuery)) {
                ContextUtils.sendWarn(ctx, "SparqlEndpoint.exec.hasLimit", "SparqlEndpoint.exec.hasLimit");
            }
            if (QueryPagingRewriter2.isOrdered(origQuery)) {
                ContextUtils.sendWarn(ctx, "SparqlEndpoint.exec.isOrdered", "SparqlEndpoint.exec.isOrdered");
            }
            while (returnedSomeTriples) {
                returnedSomeTriples = false;
                String querySlice = QueryPagingRewriter2.rewriteQuery(origQuery, limit, offset);
                LOG.debug("Sliced query " + querySlice);
                GraphQuery query = null;
                try {
                    query = remoteConnection.prepareGraphQuery(QueryLanguage.SPARQL,
                            querySlice);
                } catch (RepositoryException e) {
                    LOG.error(e.getLocalizedMessage(), e.getStackTrace());
                } catch (MalformedQueryException e) {
                    LOG.error(e.getLocalizedMessage(), e.getStackTrace());
                }
                LOG.info("Executing query.");
                GraphQueryResult result = null;
                try {
                    result = query.evaluate();
                } catch (QueryEvaluationException e) {
                    LOG.error(e.getLocalizedMessage(), e.getStackTrace());
                }
                LOG.info("Storing result.");
                try {
                    if (result != null) {
                        while (result.hasNext()) {
                            returnedSomeTriples = true;
                            final Statement st = result.next();
                            // Add to out output.
                            output.add(st.getSubject(), st.getPredicate(), st.getObject());
                            // Print info.
                            ++counter;
                            if (counter % 100000 == 0) {
                                LOG.info("{} triples extracted", counter);
                            }
                        }
                    } else {
                        LOG.info("No result to be stored.");
                    }
                } catch (QueryEvaluationException e) {
                    LOG.error(e.getLocalizedMessage(), e.getStackTrace());
                }
                offset += limit;
            }
        }

        LOG.info("Flushing buffers.");
        output.flushBuffer();

        LOG.info("Get size of the extracted data");
        long size = 0;
        RepositoryConnection connection = null;
        try {
            connection = rdfOutput.getConnection();
            size = connection.size(outputEntry.getDataGraphURI());
        } catch (RepositoryException e) {
            LOG.error(e.getLocalizedMessage(), e.getStackTrace());
        } catch (DataUnitException e) {
            LOG.error(e.getLocalizedMessage(), e.getStackTrace());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException e) {
                    LOG.warn("Cannot close connection", e);
                }
            }
        }

        ContextUtils.sendShortInfo(ctx, "SparqlEndpoint.exec.extracted", size);


    }
}
