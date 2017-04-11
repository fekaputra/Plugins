package eu.unifiedviews.plugins.transformer.metadata;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.ValueFactoryImpl;

public class MetadataVocabulary {

    public static final String DCAT = "http://www.w3.org/ns/dcat#";

    public static final String VOID = "http://rdfs.org/ns/void#";

    public static final String QB = "http://purl.org/linked-data/cube#";

    public static final URI QB_DATA_SET;

    public static final URI DCAT_KEYWORD;

    public static final URI DCAT_DISTRIBUTION;

    public static final URI DCAT_DOWNLOAD_URL;

    public static final URI DCAT_MEDIA_TYPE;

    public static final URI DCAT_THEME;

    public static final URI DCAT_DISTRO_CLASS;

    public static final URI DCAT_DATASET_CLASS;

    public static final URI XSD_DATE;

    public static final URI VOID_DATASET_CLASS;

    public static final URI VOID_TRIPLES;

    public static final URI VOID_ENTITIES;

    public static final URI VOID_CLASSES;

    public static final URI VOID_PROPERTIES;

    public static final URI VOID_D_SUBJECTS;

    public static final URI VOID_D_OBJECTS;

    public static final URI VOID_EXAMPLE_RESOURCE;

    static {
        final ValueFactory valueFactory = ValueFactoryImpl.getInstance();

        QB_DATA_SET = valueFactory.createURI(QB + "DataSet");

        DCAT_KEYWORD = valueFactory.createURI(DCAT + "keyword");
        DCAT_DISTRIBUTION = valueFactory.createURI(DCAT + "distribution");
        DCAT_DOWNLOAD_URL = valueFactory.createURI(DCAT + "downloadURL");
        DCAT_MEDIA_TYPE = valueFactory.createURI(DCAT + "mediaType");
        DCAT_THEME = valueFactory.createURI(DCAT + "theme");
        DCAT_DISTRO_CLASS = valueFactory.createURI(DCAT + "Distribution");
        DCAT_DATASET_CLASS = valueFactory.createURI(DCAT + "Dataset");

        XSD_DATE = valueFactory.createURI("http://www.w3.org/2001/XMLSchema#date");

        VOID_DATASET_CLASS = valueFactory.createURI(VOID + "Dataset");
        VOID_TRIPLES = valueFactory.createURI(VOID + "triples");
        VOID_ENTITIES = valueFactory.createURI(VOID + "entities");
        VOID_CLASSES = valueFactory.createURI(VOID + "classes");
        VOID_PROPERTIES = valueFactory.createURI(VOID + "properties");
        VOID_D_SUBJECTS = valueFactory.createURI(VOID + "distinctSubjects");
        VOID_D_OBJECTS = valueFactory.createURI(VOID + "distinctObjects");
        VOID_EXAMPLE_RESOURCE = valueFactory.createURI(VOID + "exampleResource");
    }
}
