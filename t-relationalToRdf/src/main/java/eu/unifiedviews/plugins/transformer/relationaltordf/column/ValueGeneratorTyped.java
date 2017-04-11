package eu.unifiedviews.plugins.transformer.relationaltordf.column;

import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;

import eu.unifiedviews.plugins.transformer.relationaltordf.ConversionFailed;

/**
 * Create value with given type.
 */
public class ValueGeneratorTyped extends ValueGeneratorReplace {

    private final String typeStr;

    private URI typeUri;

    public ValueGeneratorTyped(URI uri, String template, String typeStr) {
        super(uri, template);
        this.typeStr = typeStr;
    }

    @Override
    public Value generateValue(List<Object> row, ValueFactory valueFactory) {
        final String rawResult = super.process(row);
        if (rawResult == null) {
            return null;
        }

        return valueFactory.createLiteral(rawResult, this.typeUri);
    }

    @Override
    public void compile(Map<String, Integer> nameToIndex, ValueFactory valueFactory) throws ConversionFailed {
        super.compile(nameToIndex, valueFactory);
        this.typeUri = valueFactory.createURI(this.typeStr);
    }

}
