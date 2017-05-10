package org.dbpedia.extraction.spark.dialog;

import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.converter.Converter;
import eu.unifiedviews.helpers.dpu.ontology.EntityDescription;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by chile on 26.04.17.
 */
@EntityDescription.Entity(type = SparkDpuConfig.SPARK_CONFIG_ENTRY)
public class SparkConfigEntry implements Map.Entry<String, Property>, Serializable {

    public SparkConfigEntry(String key, String value, SparkConfigEntry defaultEntry) {
        this(   key,
                value,
                defaultEntry.getDefaultValue(),
                defaultEntry.getSparkPropertyCategory(),
                defaultEntry.getSparkPropertyType(),
                defaultEntry.getRegex().toString(),
                defaultEntry.getDescription());
        this.setFloatMax(defaultEntry.floatMax);
        this.setFloatMin(defaultEntry.floatMin);
        for(String sch : defaultEntry.uriSchemes)
            this.addUriSchemes(sch);
    }

    public SparkConfigEntry(String key, Property value, SparkConfigEntry defaultEntry) {
        this(   key,
                defaultEntry.getDefaultValue(),    //value is entered below
                defaultEntry.getDefaultValue(),
                defaultEntry.getSparkPropertyCategory(),
                defaultEntry.getSparkPropertyType(),
                defaultEntry.getRegex().toString(),
                defaultEntry.getDescription());
        this.setValue(value);
        this.setFloatMax(defaultEntry.floatMax);
        this.setFloatMin(defaultEntry.floatMin);
        for(String sch : defaultEntry.uriSchemes)
            this.addUriSchemes(sch);
    }

    public SparkConfigEntry(String key, String value, String defaultValue, SparkPropertyCategory category, SparkPropertyType type, String regex, String description) {
        //if(value == null)
        //    throw new IllegalArgumentException("Provided value for SparkConfigEntry " + key + " was null!");

        this.key = key;
        this.value = new ObjectProperty(SparkConfigEntry.toObject(type.getClazz(), value), type.getClazz());
        this.defaultValue = defaultValue;
        this.sparkPropertyCategory = category;
        this.sparkPropertyType = type;
        this.regex = Pattern.compile(regex);
        this.description = description;
    }

    @EntityDescription.Property(uri = SparkDpuConfig.SPARK_CONFIG_VALUE)
    private Property value;
    @EntityDescription.Property(uri = SparkDpuConfig.SPARK_CONFIG_KEY)
    private String key;

    private SparkPropertyCategory sparkPropertyCategory = SparkPropertyCategory.UsecaseOptional;
    private String defaultValue = "";
    private SparkPropertyType sparkPropertyType = SparkPropertyType.String;
    private Pattern regex = null;
    private String description;
    private Float floatMin = Float.MIN_VALUE;
    private Float floatMax = Float.MAX_VALUE;
    private List<String> uriSchemes = new ArrayList<>();

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public Property getValue() {
        return this.value;
    }

    @Override
    public Property setValue(Property s) {
        this.value = s;
        return this.value;
    }

    public SparkPropertyType getSparkPropertyType() {        return sparkPropertyType;    }

    public Pattern getRegex() {
        return regex;
    }

    public String getDescription() {
        return description;
    }

    public Float getFloatMin() {
        return floatMin;
    }

    void setFloatMin(Float floatMin) {
        this.floatMin = floatMin;
    }

    public Float getFloatMax() {
        return floatMax;
    }

    void setFloatMax(Float floatMax) {
        this.floatMax = floatMax;
    }

    public List<String> getUriSchemes() {
        return Collections.unmodifiableList(this.uriSchemes);
    }

    void addUriSchemes(String uriScheme) {
        this.uriSchemes.add(uriScheme);
    }

    void setKey(String key) {
        this.key = key;
    }

    void setSparkPropertyCategory(SparkPropertyCategory sparkPropertyCategory) {
        this.sparkPropertyCategory = sparkPropertyCategory;
    }

    void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    void setSparkPropertyType(SparkPropertyType sparkPropertyType) {
        this.sparkPropertyType = sparkPropertyType;
    }

    void setRegex(Pattern regex) {
        this.regex = regex;
    }

    void setDescription(String description) {
        this.description = description;
    }

    public SparkPropertyCategory getSparkPropertyCategory() {
        return sparkPropertyCategory;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String toString() {
        Converter <String, ? super Object> converter = Converters.getConverter(this.getSparkPropertyType().getClazz());
        if(converter != null)
            return converter.convertToPresentation(this.getValue().getValue(), String.class, Locale.getDefault());
        else
            return this.getValue().getValue().toString();
    }

    private static Object toObject(Class clazz, String value) {
        if(value == null || value.isEmpty())
            return null;
        if( Boolean.class == clazz ) return Boolean.parseBoolean( value );
        else if( Integer.class == clazz ) return Integer.parseInt( value );
        else if( Long.class == clazz ) return Long.parseLong( value );
        else if( Float.class == clazz ) return Float.parseFloat( value );
        else if( Double.class == clazz ) return Double.parseDouble( value );
        else if( URI.class == clazz ) try {
            return new URI(value);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        else if( List.class == clazz || Enum.class == clazz) return Arrays.stream(value.split(",")).map(String::trim).collect(Collectors.toList());
        return value;
    }
}
