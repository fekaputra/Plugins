package eu.unifiedviews.plugins.transformer.gunzipper;

import java.util.List;

import org.openrdf.repository.RepositoryConnection;
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
public class Gunzipper extends AbstractDpu<GunzipperConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(Gunzipper.class);

    private static final String QUERY_COPY = "INSERT { ?s ?p ?o } WHERE { ?s ?p ?o }";

    @DataUnit.AsInput(name = "filesInput")
    public FilesDataUnit input;
    
    @DataUnit.AsOutput(name = "filesOutput")
    public WritableFilesDataUnit output;

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

	public Gunzipper() {
		super(GunzipperVaadinDialog.class, ConfigHistory.noHistory(GunzipperConfig_V1.class));
	}
		
    @Override
    protected void innerExecute() throws DPUException {
        
    }
	
}
