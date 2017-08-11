package eu.unifiedviews.plugins.transformer.metadata;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.ValueFactoryImpl;

public class MetadataVocabulary {

    public static final String DCAT = "http://www.w3.org/ns/dcat#";

    public static final String VOID = "http://rdfs.org/ns/void#";

    public static final String QB = "http://purl.org/linked-data/cube#";

    public static final IRI QB_DATA_SET;

    public static final IRI DCAT_KEYWORD;

    public static final IRI DCAT_DISTRIBUTION;

    public static final IRI DCAT_DOWNLOAD_URL;

    public static final IRI DCAT_MEDIA_TYPE;

    public static final IRI DCAT_THEME;

    public static final IRI DCAT_DISTRO_CLASS;

    public static final IRI DCAT_DATASET_CLASS;

    public static final IRI XSD_DATE;

    public static final IRI VOID_DATASET_CLASS;

    public static final IRI VOID_TRIPLES;

    public static final IRI VOID_ENTITIES;

    public static final IRI VOID_CLASSES;

    public static final IRI VOID_PROPERTIES;

    public static final IRI VOID_D_SUBJECTS;

    public static final IRI VOID_D_OBJECTS;

    public static final IRI VOID_EXAMPLE_RESOURCE;

    static {
        final ValueFactory valueFactory = ValueFactoryImpl.getInstance();

        QB_DATA_SET = valueFactory.createIRI(QB + "DataSet");

        DCAT_KEYWORD = valueFactory.createIRI(DCAT + "keyword");
        DCAT_DISTRIBUTION = valueFactory.createIRI(DCAT + "distribution");
        DCAT_DOWNLOAD_URL = valueFactory.createIRI(DCAT + "downloadURL");
        DCAT_MEDIA_TYPE = valueFactory.createIRI(DCAT + "mediaType");
        DCAT_THEME = valueFactory.createIRI(DCAT + "theme");
        DCAT_DISTRO_CLASS = valueFactory.createIRI(DCAT + "Distribution");
        DCAT_DATASET_CLASS = valueFactory.createIRI(DCAT + "Dataset");

        XSD_DATE = valueFactory.createIRI("http://www.w3.org/2001/XMLSchema#date");

        VOID_DATASET_CLASS = valueFactory.createIRI(VOID + "Dataset");
        VOID_TRIPLES = valueFactory.createIRI(VOID + "triples");
        VOID_ENTITIES = valueFactory.createIRI(VOID + "entities");
        VOID_CLASSES = valueFactory.createIRI(VOID + "classes");
        VOID_PROPERTIES = valueFactory.createIRI(VOID + "properties");
        VOID_D_SUBJECTS = valueFactory.createIRI(VOID + "distinctSubjects");
        VOID_D_OBJECTS = valueFactory.createIRI(VOID + "distinctObjects");
        VOID_EXAMPLE_RESOURCE = valueFactory.createIRI(VOID + "exampleResource");
    }
}
