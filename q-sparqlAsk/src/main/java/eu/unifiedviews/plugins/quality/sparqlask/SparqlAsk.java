package eu.unifiedviews.plugins.quality.sparqlask;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.DataUnitUtils;
import eu.unifiedviews.helpers.dataunit.copy.CopyHelper;
import eu.unifiedviews.helpers.dataunit.copy.CopyHelpers;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.rdf.sparql.SparqlProblemException;
import eu.unifiedviews.helpers.dpu.rdf.sparql.SparqlUtils;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.UpdateExecutionException;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@DPU.AsQuality
public class SparqlAsk extends AbstractDpu<SparqlAskConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(SparqlAsk.class);

    private static final String ADD_QUERY = "INSERT {?s ?p ?o } WHERE {?s ?p ?o}";

    @DataUnit.AsInput(name = "rdf")
    public RDFDataUnit rdfInData;

    @DataUnit.AsOutput(name = "rdf")
    public WritableRDFDataUnit rdfOutData;

    protected boolean emptyFound = false;

    public SparqlAsk() {
        super(SparqlAskVaadinDialog.class, ConfigHistory.noHistory(SparqlAskConfig_V1.class));
    }

    @Override
    protected void innerExecute() throws DPUException {

        boolean performanceOptimizationEnabled = false;

        if (ctx.isPerformanceOptimizationEnabled(rdfInData)) {
            //activated
            performanceOptimizationEnabled = true;
            ContextUtils.sendInfo(ctx, "rdfvalidation.dpu.optimistic.on", "rdfvalidation.dpu.optimistic.on.detailed");
        } else {
            //not activated
            ContextUtils.sendInfo(ctx, "rdfvalidation.dpu.optimistic.off", "rdfvalidation.dpu.optimistic.off.detailed");
        }

        RepositoryConnection connection = null;
        try {
            connection = rdfInData.getConnection();
        } catch (DataUnitException e) {
            throw new DPUException(e.getLocalizedMessage(), e);
        }

        if (performanceOptimizationEnabled) {
            // just copy metadata to the output, do not copy data

            // Get graphs.
            final List<RDFDataUnit.Entry> sourceEntries;
            try {
                sourceEntries = DataUnitUtils.getEntries(rdfInData, RDFDataUnit.Entry.class);
            } catch (DataUnitException e) {
                throw new DPUException(e.getLocalizedMessage(), e);
            }
            for (final RDFDataUnit.Entry sourceEntry : sourceEntries) {

                String sourceSymbolicName;
                try {
                    sourceSymbolicName = sourceEntry.getSymbolicName();
                } catch (DataUnitException e) {
                    throw new DPUException(e.getLocalizedMessage(), e);
                }

                CopyHelper copyHelper = CopyHelpers.create(rdfInData, rdfOutData);
                try {
                    copyHelper.copyMetadata(sourceSymbolicName);
                } catch (DataUnitException e) {
                    throw new DPUException(e.getLocalizedMessage(), e);
                }
            }
        }
        else {
            // Copy data to output.
            LOG.info("Copying input data to output ...");

            try {
                List<RDFDataUnit.Entry> inputs = DataUnitUtils.getMetadataEntries(rdfInData);
                RDFDataUnit.Entry output = DataUnitUtils.getWritableMetadataEntry(rdfOutData);
                // Prepare query.
                SparqlUtils.SparqlUpdateObject update = SparqlUtils.createInsert(ADD_QUERY, inputs, output);
                // Execute sparql.
                SparqlUtils.execute(connection, update);
            } catch (RepositoryException | MalformedQueryException | UpdateExecutionException | SparqlProblemException | DataUnitException e) {
                throw new DPUException(e.getLocalizedMessage(), e);
            }

            LOG.info("Copying input data to output ... done");
        }

        // Get input graphs.
        LOG.info("Reading input graphs ...");
        final List<RDFDataUnit.Entry> graphs;
        try {
            graphs = DataUnitUtils.getEntries(rdfInData, RDFDataUnit.Entry.class);
        } catch (DataUnitException e) {
            throw new DPUException(e.getLocalizedMessage(), e);
        }

        checkGraph(connection, graphs);

    }

    /**
     * Report failure ie. send user defined message.
     */
    private void reportFailure() {
        ContextUtils.sendMessage(ctx, config.getMessageType(), "rdfvalidation.finished.error", "rdfvalidation.constraintfailed", config.getAskQuery().replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
        emptyFound = true;
    }

    protected void checkGraph(RepositoryConnection connection, final List<RDFDataUnit.Entry> entries) throws DPUException {

        SparqlUtils.SparqlAskObject ask = null;
        try {
            ask = SparqlUtils.createAsk(config.getAskQuery(), entries);
            SparqlUtils.execute(connection, ask);
        } catch (RepositoryException | MalformedQueryException | UpdateExecutionException | QueryEvaluationException | SparqlProblemException | DataUnitException e) {
            throw new DPUException(e.getLocalizedMessage(), e);
        }

        if (!ask.result) {
            reportFailure();
        }
        else {
            //everything OK:
            ContextUtils.sendShortInfo(ctx, "rdfvalidation.finished.ok");
        }

    }

}
