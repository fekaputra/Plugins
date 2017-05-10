package org.dbpedia.extraction.spark.dialog;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import org.dbpedia.extraction.spark.SparkPipeline;
import org.dbpedia.extraction.spark.utils.SparkPipelineUtils;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by chile on 27.03.17.
 *
 * Loads a SPARK config file as a Bean Container (so we can use it directly in the Vaadin Dialog....)
 */
public class SparkDpuConfig extends BeanItemContainer<SparkConfigEntry> implements Serializable {

    /** master value */
    private String master;

    /** rest endpoint */
    private String restApi;

    /** appName value */
    private String appName;

    private static Map<String, SparkConfigEntry> defaultEntries;

    static{
        try {
            defaultEntries = SparkDpuConfig
                    .readConfigParameters(SparkPipeline.class.getClassLoader().getResource("spark.defaults.csv"))
                    .stream().collect(Collectors.toMap(SparkConfigEntry::getKey, item -> item));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //for each SparkPropertyCategory, add empty entry to defaults
        for(SparkPropertyCategory c : SparkPropertyCategory.values()) {
            defaultEntries.put(c.toString(), new SparkConfigEntry(
                    "",
                    "",
                    "",
                    c,
                    SparkPropertyType.String,
                    "",
                    "empty value for " + c.toString()));
        }
    }

    private List<String> KnownUseCaseNames = null;

    public SparkDpuConfig() throws IOException {
        super(SparkConfigEntry.class);
    }

    public SparkDpuConfig(final InputStream input) throws IOException {
        super(SparkConfigEntry.class);


        if (null != input) {
            // load config with loaded parameters
            Map<String, String> loadedConfigParameters = SparkDpuConfig.readSparkConfig(input);

            // detect all used usecase names in config
            ArrayList<String> knownUseCases = new ArrayList<>();
            for (String key : loadedConfigParameters.keySet()) {
                List<String> keyParts = Arrays.stream(key.split("\\.")).map(String::trim).collect(Collectors.toList());
                if(keyParts.size() > 2 && keyParts.get(2).equals("filemanager") && ! knownUseCases.contains(keyParts.get(1)))
                    knownUseCases.add(keyParts.get(1));
            }

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

                SparkConfigEntry def = this.defaultEntries.get(key);
                if(def != null) {
                    this.addItem(new SparkConfigEntry(key, parameter, def));
                }
                else{
                    if(key.lastIndexOf('.') > 5){
                        String newkey = "spark.usecase" + key.substring(key.indexOf('.', 6));
                        def = this.defaultEntries.get(newkey);
                        if(def != null)
                            this.addItem(new SparkConfigEntry(key, parameter, def));
                        else
                            this.addItem(new SparkConfigEntry(key, parameter, "", SparkPropertyCategory.UsecaseOptional, SparkPropertyType.String, "", "An use case specific Spark property. No generic information available."));
                    }
                    else{
                        this.addItem(new SparkConfigEntry(key, parameter, "", SparkPropertyCategory.SparkOptional, SparkPropertyType.String, "", "An unknown Spark property."));
                    }
                }
            }
        } else
            throw new IllegalArgumentException("No SPARK config file was provided!");
    }

    public String getAppName() {
        return this.appName;
    }

    public String getMasterUrl(){
        return (null == this.master ? "local[*]" : this.master);
    }

    public Optional<Property> getProperty(String key){
        SparkConfigEntry ent = this.getByStringKey(key);
        if(ent == null)
            return Optional.empty();
        return Optional.ofNullable(ent.getValue());
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public void setRestApi(String restApi) {
        this.restApi = restApi;
    }

    public static void setDefaultEntries(Map<String, SparkConfigEntry> defaultEntries) {
        SparkDpuConfig.defaultEntries = defaultEntries;
    }

    public void setKnownUseCaseNames(List<String> knownUseCaseNames) {
        KnownUseCaseNames = knownUseCaseNames;
    }

    public String getRestApiUri(){
        if(this.restApi == null){
            //infer from master using the default port
            this.restApi = this.master.substring(0, this.master.lastIndexOf(':')) + ":6066";
        }
        return this.restApi.replace("spark:", "http:");
    }

    public String getSparkOutputDir(String appName) throws Exception {
        Optional<Property> zw = this.getProperty("spark." + appName + ".filemanager.outputdir");
        if(zw.isPresent())
            return zw.get().toString();
        else
            throw new Exception("TODO"); //TODO
    }

    public SparkConfigEntry getByStringKey(String key){
        for(SparkConfigEntry ent : this.getItemIds()){
            if(ent.getKey().trim().equals(key.trim()))
                return ent;
        }
        return null;
    }

    public List<SparkConfigEntry> getFilteredIds(){
        return this.getFilteredItemIds();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        SparkDpuConfig clone = null;
        try {
            clone = new SparkDpuConfig();
        } catch (IOException e) {
            throw new CloneNotSupportedException(e.getMessage());
        }
        for(SparkConfigEntry ent : this.getAllItemIds()) {
            clone.addItem(ent);
        }
        clone.master = this.master;
        clone.appName = this.appName;
        clone.restApi = this.restApi;
        return clone;
    }

    public SparkConfigEntry getEmptyEntry(SparkPropertyCategory ofType){
        return this.defaultEntries.get(ofType.toString());
    }

    public SparkConfigEntry getDefaultEntry(String key){

        return this.defaultEntries.get(key);
    }

    public static final String SPARK_CONFIG_PREFIX = "http://unifiedviews.eu/ontology/dpu/spark/config/";

    public static final String SPARK_CONFIG_ENTRY = SPARK_CONFIG_PREFIX + "sparkConfEntry";

    public static final String SPARK_CONFIG_KEY = SPARK_CONFIG_PREFIX + "sparConfkKey";

    public static final String SPARK_CONFIG_VALUE = SPARK_CONFIG_PREFIX + "sparkConfVal";

    private static Map<String, String> readSparkConfig(final InputStream configStream) throws IOException {
        Pattern whiteSpace = Pattern.compile("\\s+");
        Map<String, String> ret = new HashMap<>();

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(configStream))) {
            String line = null;
            while (null != (line = reader.readLine())) {
                String cleanLine = line.trim(); // ensure we do not have any white spaces left
                if (cleanLine.startsWith("#") || cleanLine.isEmpty()) {
                    continue; // have found a comment
                }

                // get rid off all white space and replace with single space
                cleanLine = whiteSpace.matcher(cleanLine).replaceAll(" ");

                int indexWhiteSpace = cleanLine.indexOf(" ");
                if (0 > indexWhiteSpace) {
                    new RuntimeException("Was not able to find end of key end in line: " + line);
                }

                // get key
                String key = cleanLine.substring(0, indexWhiteSpace);
                if (key.isEmpty()) {
                    continue;
                }

                // check if we have another comment at the end of the parameter
                int endlineCommentIndex = cleanLine.lastIndexOf("#");

                // get parameter
                String parameter;
                if (0 < endlineCommentIndex) {
                    parameter = cleanLine.substring(indexWhiteSpace + 1, endlineCommentIndex).trim();
                } else {
                    parameter = cleanLine.substring(indexWhiteSpace + 1);
                }

                // make sure that we have something to save
                if (parameter.isEmpty()) {
                    continue;
                }

                // store config values
                ret.put(key, parameter);
            }
        }catch(Exception e){
            throw new IllegalArgumentException("The provided configuration file was not a valid SPARK configuration.", e);
        }

        return ret;
    }


    /**
     * Will read the input as a .csv file and fill a list of default entries
     * @param resourceUrl
     * @throws IOException
     */
    private static List<SparkConfigEntry> readConfigParameters(final URL resourceUrl) throws IOException {

        List<SparkConfigEntry> entries = new ArrayList<>();

        // we expect a line of this structure: key,category,type,regex,defaultValue,description
        InputStream configStream = resourceUrl.openStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(configStream));

        String line = reader.readLine();  //will discard first line (headers)
        int linecount = 1;

        while (null != (line = reader.readLine())) {
            linecount++;
            // split line on the comma only if that comma has zero, or an even number of quotes ahead of it.
            List<String> cells = Arrays.stream(line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1)).map( x-> x.replace("\"", "")).collect(Collectors.toList());

            if(cells.size() == 0 || cells.get(0).isEmpty() || !cells.get(0).startsWith("spark."))
                throw new IllegalArgumentException("The following line did not provide a key starting with 'spark.': " + linecount);
            if(cells.size() < 6)
                throw new IllegalArgumentException("The following line did not at least have 6 cells: " + linecount + ". Is the description missing?");

            for(int i = 6; i < cells.size(); i++)
                cells.set(5, cells.get(5) + cells.get(i));

            List<String> typeParameters = cells.get(2).contains("(") ? Arrays.asList(
                    cells.get(2).trim().substring(cells.get(2).trim().indexOf("(")+1, cells.get(2).trim().length()-1).split(","))
                    : new ArrayList<String>();

            String type = cells.get(2).contains("(") ? cells.get(2).trim().substring(0, cells.get(2).trim().indexOf("(")) : cells.get(2).trim();

            //create new entries containing only the default values
            SparkConfigEntry ent = new SparkConfigEntry(
                    cells.get(0).trim(),
                    "",
                    cells.get(4).trim(),
                    Enum.valueOf(SparkPropertyCategory.class, cells.get(1).trim()),
                    Enum.valueOf(SparkPropertyType.class, type),
                    cells.get(3).trim(),
                    cells.get(5).trim()
            );

            if(ent.getSparkPropertyType() == SparkPropertyType.Float && typeParameters.size() > 1) {
                ent.setFloatMax(Float.parseFloat(typeParameters.get(1).trim()));
                ent.setFloatMin(Float.parseFloat(typeParameters.get(0).trim()));
            }

            if(ent.getSparkPropertyType() == SparkPropertyType.Uri) {
                for(String scheme : typeParameters)
                    ent.addUriSchemes(scheme);
            }

            entries.add(ent);
        }
        return entries;
    }

    public String getSerializedSparkConfig(){
        StringBuilder sb = new StringBuilder("# This SPARK configuration was exported... TODO\n\n");  //TODO some preamble comment

        this.getAllItemIds().sort(new Comparator<SparkConfigEntry>() {
            @Override
            public int compare(SparkConfigEntry t0, SparkConfigEntry t1) {
                return Comparator.<String>naturalOrder().compare(t0.getKey(), t1.getKey());
            }
        });

        int tabDistance = calcTabDistance();

        for(SparkConfigEntry sce : this.getAllItemIds()){
            sb = sb.append(sce.getKey());
            sb = sb.append(SparkPipelineUtils.padLeft("", tabDistance - (int) Math.ceil(sce.getKey().length()/4d), '\t'));
            sb = sb.append(sce.toString());
            sb = sb.append("\n");
        }
        return sb.toString();
    }

    public String getJsonSparkConfig(){
        StringBuilder sb = new StringBuilder("{\n");

        this.getAllItemIds().sort(new Comparator<SparkConfigEntry>() {
            @Override
            public int compare(SparkConfigEntry t0, SparkConfigEntry t1) {
                return Comparator.<String>naturalOrder().compare(t0.getKey(), t1.getKey());
            }
        });

        for(SparkConfigEntry sce : this.getAllItemIds()){
            //filter out use case specific properties, not belonging to current use case
            String[] keySplits = sce.getKey().split("\\.");
            if(!keySplits[1].equals(this.getAppName()) && KnownUseCaseNames.contains(keySplits[1]))
                continue;
            sb = sb.append("\t\"" + sce.getKey());
            sb = sb.append("\" : \"");
            sb = sb.append(sce.toString());
            sb = sb.append("\",\n");
        }
        sb = sb.deleteCharAt(sb.length()-2);
        sb = sb.append("}");
        return sb.toString();
    }

    private int calcTabDistance(){
        int d = 0;
        for(SparkConfigEntry sce : this.getAllItemIds()){
            int z = Math.round(sce.getKey().length()/4f) + 1;
            if(z > d)
                d = z;
        }
        return d;
    }


    public List<String> getKnownUseCaseNames() {
        if(KnownUseCaseNames == null) {
            this.KnownUseCaseNames = new ArrayList<>();
            for (SparkConfigEntry sce : this.getAllItemIds()) {
                String[] keySplits = sce.getKey().split("\\.");
                if (keySplits.length > 2 && keySplits[2].equals("filemanager"))
                    KnownUseCaseNames.add(keySplits[1]);
            }
        }
        return KnownUseCaseNames;
    }
}