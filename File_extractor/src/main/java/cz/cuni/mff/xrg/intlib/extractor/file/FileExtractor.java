package cz.cuni.mff.xrg.intlib.extractor.file;

import cz.cuni.xrg.intlib.commons.data.DataUnitCreateException;
import cz.cuni.xrg.intlib.commons.data.DataUnitType;
import cz.cuni.xrg.intlib.commons.extractor.Extract;
import cz.cuni.xrg.intlib.commons.extractor.ExtractContext;
import cz.cuni.xrg.intlib.commons.extractor.ExtractException;
import cz.cuni.xrg.intlib.commons.module.dpu.ConfigurableBase;
import cz.cuni.xrg.intlib.commons.web.AbstractConfigDialog;
import cz.cuni.xrg.intlib.commons.web.ConfigDialogProvider;
import cz.cuni.xrg.intlib.rdf.enums.FileExtractType;
import cz.cuni.xrg.intlib.rdf.exceptions.RDFDataUnitException;
import cz.cuni.xrg.intlib.rdf.interfaces.RDFDataUnit;

/**
 *
 * @author Jiri Tomes
 * @author Petyr
 */
public class FileExtractor extends ConfigurableBase<FileExtractorConfig>
		implements Extract, ConfigDialogProvider<FileExtractorConfig> {

	public FileExtractor() {
		super(new FileExtractorConfig());
	}

	@Override
	public void extract(ExtractContext context)
			throws ExtractException,
			DataUnitCreateException {


		RDFDataUnit rdfDataUnit = (RDFDataUnit) context
				.addOutputDataUnit(DataUnitType.RDF, "output");

		final String baseURI = "";
		final FileExtractType extractType = config.fileExtractType;
		final String path = config.Path;
		final String fileSuffix = config.FileSuffix;
		final boolean onlyThisSuffix = config.OnlyThisSuffix;
		final boolean useStatisticHandler = config.UseStatisticalHandler;

		try {
			rdfDataUnit.extractFromFile(extractType, path, fileSuffix,
					baseURI, onlyThisSuffix, useStatisticHandler);
		} catch (RDFDataUnitException e) {
			throw new ExtractException(e.getMessage(), e);
		}
	}

	@Override
	public AbstractConfigDialog<FileExtractorConfig> getConfigurationDialog() {
		return new FileExtractorDialog();
	}
}
