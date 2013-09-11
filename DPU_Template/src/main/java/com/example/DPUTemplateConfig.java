package com.example;

import cz.cuni.xrg.intlib.commons.module.config.DPUConfigObjectBase;

/**
 * Put your DPU's configuration here.
 * 
 * You can optionally implement {@link #isValid()} to provide possibility
 * to validate the configuration.
 */
public class DPUTemplateConfig extends DPUConfigObjectBase {

    private int width;
    
    private int height;	
	
	// DPUTemplateConfig must provide public non-parametric constructor
    public DPUTemplateConfig() {
        width = 100;
        height = 100;
    }
    
    public DPUTemplateConfig(int w, int h) {
        width = w;
        height = h;
    }
        
    public int getWidth() {
        return width;    
    }
    
     public int getHeight() {
        return height;    
    }

}
