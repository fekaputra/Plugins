package eu.unifiedviews.plugins.transformer.tabular.column;

import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;

import cz.cuni.mff.xrg.uv.transformer.tabular.column.ValueGeneratorReplace;

/**
 * Create URI.
 *
 * @author Škoda Petr
 */
public class ValueGeneratorUri extends ValueGeneratorReplace {

    public ValueGeneratorUri(URI uri, String template) {
        super(uri, template);
    }

    @Override
    public Value generateValue(List<Object> row, ValueFactory valueFactory) {
        final String rawResult = super.process(row);
        if (rawResult == null) {
            return null;
        }

        // the replace thing is done as a part of ValueGeneratorReplace
        return valueFactory.createURI(rawResult);
    }

}
