package eu.unifiedviews.plugins.transformer.tabular.column;

import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

import eu.unifiedviews.plugins.transformer.tabular.parser.ParseFailed;

/**
 * Generate value for given template.
 *
 * @author Škoda Petr
 */
public interface ValueGenerator {

    /**
     * Prepare {@link ValueGenerator} to use.
     *
     * @param nameToIndex Mapping from names to indexes in row.
     * @param valueFactory
     * @throws eu.unifiedviews.plugins.transformer.tabular.parser.ParseFailed
     */
    public void compile(Map<String, Integer> nameToIndex, ValueFactory valueFactory) throws ParseFailed;


    /**
     * Generate value based on stored information.
     *
     * @param row
     * @param valueFactory
     * @return
     */
    public Value generateValue(List<Object> row, ValueFactory valueFactory);

    /**
     *
     * @return URI for generated value.
     */
    public URI getUri();

}
