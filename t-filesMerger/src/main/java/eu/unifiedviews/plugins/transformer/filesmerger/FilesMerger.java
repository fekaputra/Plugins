package eu.unifiedviews.plugins.transformer.filesmerger;

import java.util.List;

import org.eclipse.rdf4j.repository.RepositoryConnection;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.helpers.dataunit.DataUnitUtils;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.rdf.sparql.SparqlUtils;


/**
 * Main data processing unit class.
 *
 * @author Petr Škoda
 */
@DPU.AsTransformer
public class FilesMerger extends AbstractDpu<FilesMergerConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(FilesMerger.class);

    private static final String QUERY_COPY = "INSERT { ?s ?p ?o } WHERE { ?s ?p ?o }";

    @DataUnit.AsInput(name = "filesInput")
    public FilesDataUnit input;
    
    @DataUnit.AsOutput(name = "filesOutput")
    public WritableFilesDataUnit output;

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

	public FilesMerger() {
		super(FilesMergerVaadinDialog.class, ConfigHistory.noHistory(FilesMergerConfig_V1.class));
	}
		
    @Override
    protected void innerExecute() throws DPUException {
        final List<RDFDataUnit.Entry> source = faultTolerance.execute(new FaultTolerance.ActionReturn<List<RDFDataUnit.Entry>>() {

            @Override
            public List<RDFDataUnit.Entry> action() throws Exception {
                return DataUnitUtils.getMetadataEntries(input);
            }
        });
        final RDFDataUnit.Entry target = faultTolerance.execute(new FaultTolerance.ActionReturn<RDFDataUnit.Entry>() {

            @Override
            public RDFDataUnit.Entry action() throws Exception {
                return DataUnitUtils.getWritableMetadataEntry(output);
            }
        });
        // Use query to copy data.
        faultTolerance.execute(input, new FaultTolerance.ConnectionAction() {

            @Override
            public void action(RepositoryConnection connection) throws Exception {
                final SparqlUtils.SparqlUpdateObject update = SparqlUtils.createInsert(QUERY_COPY, source, target);
                // Copy data.
                SparqlUtils.execute(connection, update);

            }
        });
        
    }
	
}
