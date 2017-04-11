package cz.cuni.mff.xrg.uv.transformer.tabular;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.ValueFactoryImpl;

/**
 *
 * @author Škoda Petr
 */
public class TabularOntology {

    private TabularOntology() {

    }

    public static final URI BLANK_CELL;

    public static final URI ROW_NUMBER;

    public static final URI RDF_ROW_LABEL;

    public static final URI TABLE_HAS_ROW;

    public static final URI TABLE_SYMBOLIC_NAME;

    public static final URI TABLE_CLASS;

    public static final URI ROW_CLASS;

    static {
        final ValueFactory valueFactory = ValueFactoryImpl.getInstance();

        BLANK_CELL = valueFactory.createURI("http://linked.opendata.cz/ontology/odcs/tabular/blank-cell");
        ROW_NUMBER = valueFactory.createURI("http://linked.opendata.cz/ontology/odcs/tabular/row");
        RDF_ROW_LABEL = valueFactory.createURI("http://www.w3.org/2000/01/rdf-schema#label");
        TABLE_HAS_ROW = valueFactory.createURI("http://linked.opendata.cz/ontology/odcs/tabular/hasRow");
        TABLE_SYMBOLIC_NAME = valueFactory.createURI("http://linked.opendata.cz/ontology/odcs/tabular/symbolicName");
        TABLE_CLASS = valueFactory.createURI("http://unifiedviews.eu/ontology/t-tabular/Table");
        ROW_CLASS = valueFactory.createURI("http://unifiedviews.eu/ontology/t-tabular/Row");
    }

}
