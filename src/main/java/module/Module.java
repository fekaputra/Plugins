package module;

import gui.ConfigDialog;

import com.vaadin.ui.CustomComponent;

import cz.cuni.xrg.intlib.commons.Type;
import cz.cuni.xrg.intlib.commons.configuration.Configuration;
import cz.cuni.xrg.intlib.commons.configuration.ConfigurationException;
import cz.cuni.xrg.intlib.commons.extractor.ExtractContext;
import cz.cuni.xrg.intlib.commons.extractor.ExtractException;
import cz.cuni.xrg.intlib.commons.module.*;

/** 
 * TODO Change super class to desired one, you can choose from the following:
 *	GraphicalExtractor, GraphicalLoader, GraphicalTransformer
 */
public class Module implements GraphicalExtractor {

	/**
	 * Configuration component.
	 */
	private gui.ConfigDialog configDialog = null;
	
	/**
	 * DPU configuration.
	 */
	private Configuration config = new Configuration();

	public Module() {
		// set initial configuration
		/**
		 * TODO Set default (possibly empty but better valid) configuration for your DPU. 
		 */		
		this.config.setValue(Config.Url.name(), "");
		this.config.setValue(Config.Login.name(), "");
		this.config.setValue(Config.Password.name(), "");
		this.config.setValue(Config.Query.name(), "CONSTRUCT {?s ?p ?o} where {?s ?p ?o}");
	}
	
	public void extract(ExtractContext context) throws ExtractException {
		/**
		 * TODO Implement module functionality here. Use context to access input, output graphs and
		 *	other context.
		 */
	}

	public Type getType() {
		return Type.EXTRACTOR;
		/**
		 * TODO Change type according to implemented interface.
		 */
	}

	public CustomComponent getConfigurationComponent() {
		// does dialog exist?
		if (this.configDialog == null) {
			// create it
			this.configDialog = new ConfigDialog();
			this.configDialog.setConfiguration(this.config);
		}
		return this.configDialog;
	}

	public Configuration getSettings() throws ConfigurationException {
		if (this.configDialog == null) {			
		} else {
			// get configuration from dialog
			Configuration conf = this.configDialog.getConfiguration();
			if (conf == null) {
				// in dialog is invalid configuration .. 
				return null;
			}
			else
			{
				this.config = conf;
			}
		}
		return this.config;
	}

	public void setSettings(Configuration configuration) {
		this.config = configuration;
		if (this.configDialog == null) {			
		} else {
			// also set configuration for dialog
			this.configDialog.setConfiguration(this.config); 
		}
	}

}
