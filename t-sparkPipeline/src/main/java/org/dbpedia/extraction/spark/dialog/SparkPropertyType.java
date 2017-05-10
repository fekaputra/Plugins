package org.dbpedia.extraction.spark.dialog;

import java.net.URI;
import java.util.List;

/**
 * Created by chile on 10.05.17.
 */
public enum SparkPropertyType{
    Integer(Integer.class),
    NonNegativeInteger(Integer.class),
    Float(Float.class),
    Boolean(Boolean.class),
    Duration(String.class),
    ByteSize(String.class),
    String(String.class),
    StringList(List.class),
    Uri(URI.class),
    Enum(String.class);

    private Class clazz = null;

    SparkPropertyType(Class className) {
        this.clazz = className;
    }

    public Class getClazz() {
        return clazz;
    }
}