package eu.unifiedviews.plugins.transformer.fusiontool.testutils;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import org.eclipse.rdf4j.model.IRI;

/**
*
*/
public class MockEntry implements RDFDataUnit.Entry {
    private final IRI uri;

    public MockEntry(IRI uri) {
        this.uri = uri;
    }

    @Override
    public IRI getDataGraphURI() throws DataUnitException {
        return uri;
    }

    @Override
    public String getSymbolicName() throws DataUnitException {
        return uri.stringValue();
    }
}
