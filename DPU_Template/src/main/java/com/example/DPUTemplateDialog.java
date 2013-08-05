package com.example;

import cz.cuni.xrg.intlib.commons.configuration.ConfigException;
import cz.cuni.xrg.intlib.commons.module.dialog.BaseConfigDialog;

/**
 * DPU's configuration dialog. User can use this dialog to configure DPU
 * configuration.
 */
public class DPUTemplateDialog extends BaseConfigDialog<DPUTemplateConfig> {

	public DPUTemplateDialog() {
		super(new DPUTemplateConfig());
	}

	@Override
	public void setConfiguration(DPUTemplateConfig conf) throws ConfigException {
		// TODO : load configuration from function parameter into dialog
	}

	@Override
	public DPUTemplateConfig getConfiguration() throws ConfigException {
		// TODO : gather information from dialog and store them into configuration, then return it
		return null;
	}

}
