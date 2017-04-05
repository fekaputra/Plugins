package org.dbpedia.extraction.spark.utils;

import com.vaadin.data.util.BeanItemContainer;
import eu.unifiedviews.helpers.dpu.ontology.EntityDescription;
import org.dbpedia.extraction.spark.SparkPipeline;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

/**
 * Created by chile on 27.03.17.
 *
 * Loads a SPARK config file as a Bean Container (so we can use it directly in the Vaadin Dialog....)
 */
public class SparkDpuConfig extends BeanItemContainer<SparkDpuConfig.SparkConfigEntry> {

    /** master value */
    private String master;

    /** rest endpoint */
    private String restApi;

    /** appName value */
    private String appName;

    private SparkDpuConfig(){
        super(SparkDpuConfig.SparkConfigEntry.class);
    }

    public SparkDpuConfig(final String resourceName) throws Exception {
        super(SparkDpuConfig.SparkConfigEntry.class);

        if (null != resourceName && !resourceName.isEmpty()) {
            //TODO the config loading
            InputStream configStream = SparkPipeline.class.getClassLoader().getResourceAsStream(resourceName);
            SparkConfigReader reader = new SparkConfigReader(configStream);

            // load config with loaded parameters
            Map<String, String> loadedConfigParameters = reader.getConfigParameters();
            for (String key : loadedConfigParameters.keySet()) {
                String parameter = loadedConfigParameters.get(key);

                String keyLowerCase = key.toLowerCase();
                if (keyLowerCase.endsWith("spark.master")) {
                    // found master config parameter
                    this.master = parameter;
                } else if (keyLowerCase.endsWith("spark.app.name")) {
                    // found appName config parameter
                    this.appName = parameter;
                } else if (keyLowerCase.endsWith("spark.restApi")) {
                    // found restApi config parameter
                    this.restApi = parameter;
                }

                this.addItem(new SparkConfigEntry(key, parameter));
            }
        } else
            throw new IllegalArgumentException("No SPARK config file was provided!");
    }

    public String getAppName() {
        return (null == this.appName ? "sparkpipeline" : this.appName);
    }

    public String getMasterUrl(){
        return (null == this.master ? "local[*]" : this.master);
    }

    public Optional<String> getProperty(String key){
        //TODO test this
        return Optional.ofNullable(this.getItem(key).getBean().getValue());
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getRestApiUri(){
        if(this.restApi == null){
            //infer from master using the default port
            this.restApi = this.master.substring(0, this.master.lastIndexOf(':')) + ":6066";
        }
        return this.restApi.replace("spark:", "http:");
    }

    public String getSparkOutputDir(String appName) throws Exception {
        Optional<String> zw = this.getProperty("spark." + appName + ".filemanager.outputdir");
        if(zw.isPresent())
            return zw.get();
        else
            throw new Exception("TODO"); //TODO
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        SparkDpuConfig clone = new SparkDpuConfig();
        for(SparkConfigEntry ent : this.getAllItemIds()) {
            clone.addItem(ent);
        }
        clone.master = this.master;
        clone.appName = this.appName;
        clone.restApi = this.restApi;
        return clone;
    }

    @EntityDescription.Entity(type = SparkDpuConfig.SPARK_CONFIG_ENTRY)
    public static class SparkConfigEntry implements Map.Entry<String, String> {

        public SparkConfigEntry(){
        }

        public SparkConfigEntry(String key, String value){
            this.key = key;
            this.value = value;
        }

        @EntityDescription.Property(uri = SparkDpuConfig.SPARK_CONFIG_VALUE)
        private String value;
        @EntityDescription.Property(uri = SparkDpuConfig.SPARK_CONFIG_KEY)
        private String key;

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public String getValue() {
            return this.value;
        }

        @Override
        public String setValue(String s) {
            this.value = s;
            return this.value;
        }
    }

    public static final String SPARK_CONFIG_PREFIX = "http://unifiedviews.eu/ontology/dpu/spark/config/";

    public static final String SPARK_CONFIG_ENTRY = SPARK_CONFIG_PREFIX + "sparkConfEntry";

    public static final String SPARK_CONFIG_KEY = SPARK_CONFIG_PREFIX + "sparConfkKey";

    public static final String SPARK_CONFIG_VALUE = SPARK_CONFIG_PREFIX + "sparkConfVal";
}