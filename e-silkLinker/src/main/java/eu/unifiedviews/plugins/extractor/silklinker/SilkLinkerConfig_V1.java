package eu.unifiedviews.plugins.extractor.silklinker;

/**
 * Configuration for SilkLinker
 * 
 * @author tomasknap
 */
public class SilkLinkerConfig_V1 {

    /**
     * Path to the config file driving the execution of Silk.
     */
    private String confFile = "";

    private String minConfirmedLinks = "0.9";

    private String minLinksToBeVerified = "0.0";

    private String confFileLabel = "";

    private String silkLibraryLocation = "";

    public String getSilkLibraryLocation() {
        return silkLibraryLocation;
    }

    public void setSilkLibraryLocation(String silkLibraryLocation) {
        this.silkLibraryLocation = silkLibraryLocation;
    }

    /**
     * Constructor
     */
    public SilkLinkerConfig_V1() {
        confFile = null;
    }

    /**
     * Constructor
     * 
     * @param confFile
     *            Configuration file for Silk
     * @param confFileLabel
     *            Label for the configuration file from which the
     *            configuration was created
     * @param minConfirmed
     *            Minimum score for the links to be considered as
     *            confirmed
     * @param minToBeVerified
     *            Minimum score for the links to be considered as
     *            "to be verified"
     */
    public SilkLinkerConfig_V1(String confFile, String confFileLabel,
            String minConfirmed, String minToBeVerified, String silkLibraryLocation) {
        this.confFile = confFile;
        this.confFileLabel = confFileLabel;
        this.minConfirmedLinks = minConfirmed;
        this.minLinksToBeVerified = minToBeVerified;
        this.silkLibraryLocation = silkLibraryLocation;
    }

    /**
     * Constructor
     * 
     * @param confFile
     *            Configuration file for Silk
     */
    public SilkLinkerConfig_V1(String confFile) {
        this.confFile = confFile;

    }

    /**
     * Gets configuration file
     * 
     * @return configuration file
     */
    String getSilkConf() {
        return confFile;
    }

    /**
     * Gets label for the configuration file from which the configuration was
     * created
     * 
     * @return Label for the configuration file from which the configuration was
     *         created
     */
    public String getConfFileLabel() {
        return confFileLabel;
    }

    public boolean isValid() {
        return confFile != null;
    }

    /**
     * Gets minimum score for the links to be considered as confirmed
     * 
     * @return Minimum score for the links to be considered as confirmed
     */
    public String getMinConfirmedLinks() {
        return minConfirmedLinks;
    }

    /**
     * Gets minimum score for the links to be considered as "to be verified"
     * 
     * @return Minimum score for the links to be considered as "to be verified"
     */
    public String getMinLinksToBeVerified() {
        return minLinksToBeVerified;
    }

    public String getConfFile() {
        return confFile;
    }

    public void setConfFile(String confFile) {
        this.confFile = confFile;
    }

}
