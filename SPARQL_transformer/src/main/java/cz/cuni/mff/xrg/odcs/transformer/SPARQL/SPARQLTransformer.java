package cz.cuni.mff.xrg.odcs.transformer.SPARQL;

import cz.cuni.mff.xrg.odcs.commons.data.DataUnitException;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUException;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.AsTransformer;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.InputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.OutputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.module.dpu.ConfigurableBase;
import cz.cuni.mff.xrg.odcs.commons.web.AbstractConfigDialog;
import cz.cuni.mff.xrg.odcs.commons.web.ConfigDialogProvider;
import cz.cuni.mff.xrg.odcs.rdf.exceptions.RDFDataUnitException;
import cz.cuni.mff.xrg.odcs.rdf.interfaces.RDFDataUnit;

/**
 *
 * @author Jiri Tomes
 * @author Petyr
 * @author tknap
 */
@AsTransformer
public class SPARQLTransformer
		extends ConfigurableBase<SPARQLTransformerConfig>
		implements ConfigDialogProvider<SPARQLTransformerConfig> {

	@InputDataUnit
	public RDFDataUnit intputDataUnit;
	
	@OutputDataUnit
	public RDFDataUnit outputDataUnit;	
	
	public SPARQLTransformer() {
		super(SPARQLTransformerConfig.class);
	}

	@Override
	public void execute(DPUContext context)
			throws DPUException, DataUnitException {
		
		outputDataUnit.merge(intputDataUnit);

		final String updateQuery = config.SPARQL_Update_Query;
		try {
			outputDataUnit.transform(updateQuery);
		} catch (RDFDataUnitException ex) {
			throw new DPUException(ex.getMessage(), ex);
		}

	}
	@Override
	public AbstractConfigDialog<SPARQLTransformerConfig> getConfigurationDialog() {
		return new SPARQLTransformerDialog();
	}
	
}
