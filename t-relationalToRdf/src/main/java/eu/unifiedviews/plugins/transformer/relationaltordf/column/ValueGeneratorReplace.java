package eu.unifiedviews.plugins.transformer.relationaltordf.column;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;

import eu.unifiedviews.plugins.transformer.relationaltordf.ConversionFailed;
import eu.unifiedviews.plugins.transformer.relationaltordf.Utils;

public abstract class ValueGeneratorReplace implements ValueGenerator {

    private abstract class Token {

        public abstract String process(List<Object> row);

    }

    /**
     * Return fixed string.
     */
    private class TokenString extends Token {

        private final String string;

        public TokenString(String string) {
            this.string = string;
        }

        @Override
        public String process(List<Object> row) {
            return this.string;
        }

    }

    /**
     * Return value from given index in data row.
     */
    private class TokenReplace extends Token {

        /**
         * Index from row to pick up.
         */
        private final int index;

        public TokenReplace(int index) {
            this.index = index;
        }

        @Override
        public String process(List<Object> row) {
            final Object object = row.get(this.index);
            if (object == null) {
                return null;
            } else {
                final String value = object.toString();
                return value;
            }
        }

    }

    /**
     * As {@link TokenReplace} but also encode the value so it can be used in
     * URI.
     */
    private class TokenReplaceUri extends TokenReplace {

        public TokenReplaceUri(int index) {
            super(index);
        }

        @Override
        public String process(List<Object> row) {
            final String replaced = super.process(row);
            if (replaced == null) {
                return null;
            } else {
                return Utils.convertStringToURIPart(replaced);
            }
        }

    }

    /**
     * Used URI for column.
     */
    private final URI uri;

    /**
     * Filed reference according to
     * http://w3c.github.io/csvw/csv2rdf/#dfn-field-reference.
     */
    private final String template;

    /**
     * Contains information how to construct
     */
    private final List<Token> tokens = new LinkedList<>();

    /**
     * @param uri
     * @param template
     *            Replace template without type, language or \\ < > chars
     */
    protected ValueGeneratorReplace(URI uri, String template) {
        this.uri = uri;
        this.template = template;
    }

    @Override
    public void compile(Map<String, Integer> nameToIndex,
            ValueFactory valueFactory) throws ConversionFailed {
        this.tokens.clear();
        // parse inner pattern
        String toParse = this.template;
        while (!toParse.isEmpty()) {
            int left = indexOfUnescape(toParse, '{');
            int right = indexOfUnescape(toParse, '}');

            if (left == -1 && right == -1) {
                this.tokens.add(new TokenString(toParse));
                break;
            }
            // there is { or }

            if (right == -1 || (left != -1 && left < right)) {
                // { -> string
                final String value = toParse.substring(0, left);
                toParse = toParse.substring(left + 1);
                //
                if (!value.isEmpty()) {
                    this.tokens.add(new TokenString(value));
                } else {
                    // it can be empty if for example
                    // string starts with { or there is }{ as substring
                }
            } else if (left == -1 || (right != -1 && right < left)) {
                // } --> name
                String name = toParse.substring(0, right);
                // revert escaping
                name = name.replaceAll("\\\\\\{", "\\{").replaceAll("\\\\}", "\\}");

                toParse = toParse.substring(right + 1);
                //
                boolean isUri = false;
                if (name.startsWith("+")) {
                    name = name.substring(1);
                    isUri = true;
                }
                // translate name to index
                final Integer nameIndex = nameToIndex.get(name);
                if (nameIndex == null) {
                    throw new ConversionFailed("Unknown column name: " + name);
                }
                // create token reprezentaion
                if (isUri) {
                    this.tokens.add(new TokenReplaceUri(nameIndex));
                } else {
                    this.tokens.add(new TokenReplace(nameIndex));
                }
            } else {
                throw new ConversionFailed("Failed to parse: " + this.template);
            }
        }
    }

    @Override
    public URI getUri() {
        return this.uri;
    }

    /**
     * Return index of first unescape occurrence of given character in given
     * string.
     *
     * @param str
     * @param toFind
     * @return
     */
    private static int indexOfUnescape(String str, char toFind) {
        for (int i = 0; i < str.length(); ++i) {
            final char current = str.charAt(i);
            if (current == toFind) {
                // we find the one
                return i;
            } else if (current == '\\') {
                // skip next
                i++;
            }
            // continue the search
        }
        // not founded
        return -1;
    }

    /**
     * Assemble value based on given {@link #template} and data.
     *
     * @param row
     * @return
     */
    protected String process(List<Object> row) {
        final StringBuilder result = new StringBuilder(20);
        for (Token token : this.tokens) {
            String newString = token.process(row);
            if (newString == null) {
                // if anyone return null, then we do not publish
                // TODO update according to http://w3c.github.io/csvw/csv2rdf/#
                return null;
            } else {
                result.append(newString);
            }
        }
        return result.toString();
    }

    /**
     * Create replace based {@link ValueGenerator}.
     * 
     * @param uri
     * @param template
     * @return
     * @throws ConversionFailed
     */
    public static ValueGeneratorReplace create(URI uri, String template)
            throws ConversionFailed {
        if (template.startsWith("\"")) {
            // string
            if (template.contains("\"@")) {
                // language tag
                return new ValueGeneratorString(uri,
                        template.substring(1, template.lastIndexOf("\"@")),
                        template.substring(template.lastIndexOf("\"@") + 2));
            }
            if (template.contains("\"^^")) {
                // type
                return new ValueGeneratorTyped(uri,
                        template.substring(1, template.lastIndexOf("\"^^")),
                        template.substring(template.lastIndexOf("\"^^") + 3));
            }
            // string without nothing
            return new ValueGeneratorString(uri,
                    template.substring(1, template.length() - 1), null);
        }
        if (template.startsWith("<")) {
            // uri
            return new ValueGeneratorUri(uri,
                    template.substring(1, template.length() - 1));
        }
        throw new ConversionFailed("Can't parse tempalte: " + template);
    }

}
