package cz.cuni.mff.xrg.intlib.extractor.file;

import cz.cuni.xrg.intlib.commons.configuration.ConfigException;
import cz.cuni.xrg.intlib.commons.configuration.Configurable;
import cz.cuni.xrg.intlib.commons.data.DataUnitCreateException;
import cz.cuni.xrg.intlib.commons.data.DataUnitType;
import cz.cuni.xrg.intlib.commons.extractor.Extract;
import cz.cuni.xrg.intlib.commons.extractor.ExtractContext;
import cz.cuni.xrg.intlib.commons.extractor.ExtractException;
import cz.cuni.xrg.intlib.commons.web.AbstractConfigDialog;
import cz.cuni.xrg.intlib.commons.web.ConfigDialogProvider;
import cz.cuni.xrg.intlib.rdf.enums.FileExtractType;
import cz.cuni.xrg.intlib.rdf.exceptions.RDFException;
import cz.cuni.xrg.intlib.rdf.interfaces.RDFDataRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jiri Tomes
 * @author Petyr
 */
public class FileExtractor implements Extract,
		Configurable<FileExtractorConfig>, ConfigDialogProvider<FileExtractorConfig> {

	/**
	 * Logger class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(
			FileExtractor.class);

	private FileExtractorConfig config = new FileExtractorConfig();

	@Override
	public void extract(ExtractContext context) throws ExtractException, DataUnitCreateException {

		RDFDataRepository repository =
				(RDFDataRepository) context.addOutputDataUnit(DataUnitType.RDF,
				"output");

		final String baseURI = "";
		final FileExtractType extractType = config.fileExtractType;
		final String path = config.Path;
		final String fileSuffix = config.FileSuffix;
		final boolean onlyThisSuffix = config.OnlyThisSuffix;
		final boolean useStatisticHandler = config.UseStatisticalHandler;


		try {
			repository.extractfromFile(extractType,
					path, fileSuffix, baseURI, onlyThisSuffix,
					useStatisticHandler);
		} catch (RDFException e) {
			throw new ExtractException(e.getMessage(), e);
		}
	}

	@Override
	public AbstractConfigDialog<FileExtractorConfig> getConfigurationDialog() {
		return new FileExtractorDialog();
	}

	@Override
	public void configure(FileExtractorConfig c) throws ConfigException {
		config = c;
	}

	@Override
	public FileExtractorConfig getConfiguration() {
		return config;
	}
}
