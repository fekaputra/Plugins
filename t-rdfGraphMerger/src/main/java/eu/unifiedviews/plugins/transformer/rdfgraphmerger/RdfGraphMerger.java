package eu.unifiedviews.plugins.transformer.rdfgraphmerger;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.rdf.RdfDataUnitUtils;
import eu.unifiedviews.helpers.dataunit.resource.Resource;
import eu.unifiedviews.helpers.dataunit.resource.ResourceHelpers;
import eu.unifiedviews.helpers.dataunit.virtualgraph.VirtualGraphHelpers;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultToleranceUtils;
import eu.unifiedviews.helpers.dpu.rdf.sparql.SparqlUtils;

@DPU.AsTransformer
public class RdfGraphMerger extends AbstractDpu<RdfGraphMergerConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(RdfGraphMerger.class);

    private static final String COPY_QUERY = "INSERT { ?s ?p ?o } WHERE { ?s ?p ?o }";

    @DataUnit.AsInput(name = "input")
    public RDFDataUnit rdfInput;

    @DataUnit.AsOutput(name = "output", optional = true)
    public WritableRDFDataUnit rdfOutput;

    @ExtensionInitializer.Init
    public RdfProfiler rdfProfiler;

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    public RdfGraphMerger() {
        super(RdfGraphMergerVaadinDialog.class, ConfigHistory.noHistory(RdfGraphMergerConfig_V1.class));
    }

    @Override
    protected void innerExecute() throws DPUException {
        // Get list of input graphs.
        final List<RDFDataUnit.Entry> inputEntries = FaultToleranceUtils.getEntries(faultTolerance, rdfInput,
                RDFDataUnit.Entry.class);
        // Prepare output graph.
        final RDFDataUnit.Entry outputEntry = faultTolerance.execute(new FaultTolerance.ActionReturn<RDFDataUnit.Entry>() {

            @Override
            public RDFDataUnit.Entry action() throws Exception {
                return RdfDataUnitUtils.addGraph(rdfOutput, generateOutputSymbolicName());
            }
        });
        // Per-graph execution.
        int counter = 0;
        for (final RDFDataUnit.Entry entry : inputEntries) {
            LOG.info("Processing {}/{}", ++counter, inputEntries.size());
            faultTolerance.execute(rdfInput, new FaultTolerance.ConnectionAction() {

                @Override
                public void action(RepositoryConnection connection) throws Exception {
                    final SparqlUtils.SparqlUpdateObject update =
                            SparqlUtils.createInsert(COPY_QUERY, Arrays.asList(entry), outputEntry);
                    // Copy statementes.
                    SparqlUtils.execute(connection, update);
                }
            });
        }
        faultTolerance.execute(new FaultTolerance.ActionReturn<Boolean>() {

            @Override
            public Boolean action() throws Exception {
                Resource resource = ResourceHelpers.getResource(rdfOutput, outputEntry.getSymbolicName());
                Date now = new Date();
                resource.setLast_modified(now);
                resource.setCreated(now);
                ResourceHelpers.setResource(rdfOutput, outputEntry.getSymbolicName(), resource);
                if (config.getVirtualGraph() != null) {
                    VirtualGraphHelpers.setVirtualGraph(rdfOutput, outputEntry.getSymbolicName(), config.getVirtualGraph().toString());
                }
                return Boolean.TRUE;
            }
        });
    }

    /**
     * @return New and unique output graph name.
     */
    private String generateOutputSymbolicName() {
        return "GraphMerge/output/generated-" + Long.toString((new Date()).getTime());
    }

}
