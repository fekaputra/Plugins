package cz.cuni.mff.xrg.uv.transformer.xslt;

import eu.unifiedviews.helpers.dataunit.metadata.MetadataVocabulary;

/**
 *
 * @author Škoda Petr
 */
public interface XsltVocabulary {

    /**
     * Class form main configuration object.
     */
    public static final String STR_XSLT_CLASS
            = "http://linked.opendata.cz/ontology/uv/dpu/xslt/Config";

    /**
     * Class to associate certain symbolic name with set of XSLT parameter.
     */
    public static final String STR_XSLT_FILEINFO_CLASS
            = "http://linked.opendata.cz/ontology/uv/dpu/xslt/FileInfo";

    /**
     * Class contains a single XSLT parameter.
     */
    public static final String STR_XSLT_PARAM_CLASS
            = "http://linked.opendata.cz/ontology/uv/dpu/xslt/Param";

    /**
     * Predicate to associate certain symbolic name with an XSLT parameter
     */
    public static final String STR_XSLT_FILEINFO_PREDICATE
            = "http://linked.opendata.cz/ontology/uv/dpu/xslt/fileInfo";

    /**
     * Predicate to an XSLT parameter.
     */
    public static final String STR_XSLT_FILEINFO_PARAM_PREDICATE
            = "http://linked.opendata.cz/ontology/uv/dpu/xslt/param";

    /**
     * Predicate to symbolic name.
     */
    public static final String STR_XSLT_FILEINFO_SYMBOLICNAME_PREDICATE
            = MetadataVocabulary.STR_UV_SYMBOLIC_NAME;

    /**
     * XSLT parameter's name
     */
    public static final String STR_XSLT_PARAM_NAME_PREDICATE
            = "http://linked.opendata.cz/ontology/uv/dpu/xslt/param/name";

    /**
     * XSLT parameter's value
     */
    public static final String STR_XSLT_PARAM_VALUE_PREDICATE
            = "http://linked.opendata.cz/ontology/uv/dpu/xslt/param/value";


    /**
     * Extension given set in VirtualPath if it's presented.
     */
    public static final String STR_XSLT_OUTPUT_FILE_EXTENSION
            = "http://linked.opendata.cz/ontology/uv/dpu/xslt/outputFileExtension";

}
