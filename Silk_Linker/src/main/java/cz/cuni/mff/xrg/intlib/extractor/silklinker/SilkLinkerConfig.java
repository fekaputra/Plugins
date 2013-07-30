package cz.cuni.mff.xrg.intlib.extractor.silklinker;

import cz.cuni.xrg.intlib.commons.configuration.DPUConfigObject;

/**
 *
 * Put your DPU's configuration here.
 *
 */
public class SilkLinkerConfig implements DPUConfigObject {
    
    //path to the config file driving the execution of Silk
    private final String confFile;


    public SilkLinkerConfig(String confFile) {
        this.confFile = confFile;

    }

    String getSilkConf() {
        return confFile;
    }
    

}
