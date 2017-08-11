package eu.unifiedviews.plugins.transformer.fusiontool.util;

import com.google.common.collect.ImmutableSet;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.plugins.transformer.fusiontool.testutils.FTDPUTestUtils;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MockRDFDataUnit implements WritableRDFDataUnit, AutoCloseable {
    private final Repository repository;

    private IRI metadataGraphURI;

    private IRI dataGraphURI;

    private Map<String, IRI> addedGraphs;

    private Set<IRI> iterationGraphs;

    {
        metadataGraphURI = FTDPUTestUtils.getUniqueURI();
        dataGraphURI = FTDPUTestUtils.getUniqueURI();
        addedGraphs = new HashMap<>();
        iterationGraphs = new HashSet<>();
        iterationGraphs.add(dataGraphURI);
    }

    public MockRDFDataUnit() throws RepositoryException {
        this.repository = FTDPUTestUtils.createRepository(ImmutableSet.<Statement> of());
    }

    public MockRDFDataUnit(Collection<Statement> initialStatements) throws RepositoryException {
        this.repository = FTDPUTestUtils.createRepository(initialStatements);
    }

    @Override
    public RepositoryConnection getConnection() throws DataUnitException {
        try {
            return repository.getConnection();
        } catch (RepositoryException e) {
            throw new DataUnitException(e);
        }
    }

    @Override
    public Set<IRI> getMetadataGraphnames() throws DataUnitException {
        return ImmutableSet.of(metadataGraphURI);
    }

    @Override
    public RDFDataUnit.Iteration getIteration() throws DataUnitException {
        return new MockIteration();
    }

    @Override
    public void close() throws Exception {
        repository.shutDown();
    }

    @Override
    public IRI getBaseDataGraphURI() throws DataUnitException {
        return dataGraphURI;
    }

    @Override
    public void addExistingDataGraph(String s, IRI uri) throws DataUnitException {
        addedGraphs.put(s, uri);
        iterationGraphs.add(uri);
    }

    @Override
    public IRI addNewDataGraph(String s) throws DataUnitException {
        IRI uri = FTDPUTestUtils.createHttpUri("mockDU/" + s);
        addExistingDataGraph(s, uri);
        return uri;
    }

    @Override
    public void addEntry(String s) throws DataUnitException {

    }

    @Override
    public IRI getMetadataWriteGraphname() throws DataUnitException {
        return metadataGraphURI;
    }

    //public IRI getMetadataGraphURI() {
    //    return metadataGraphURI;
    //}

    public IRI getDataGraphURI() {
        return dataGraphURI;
    }

    public void setDataGraphURI(IRI newDataGraphURI) {
        iterationGraphs.remove(dataGraphURI);
        dataGraphURI = newDataGraphURI;
        iterationGraphs.add(newDataGraphURI);
    }

    public Map<String, IRI> getAddedGraphs() {
        return addedGraphs;
    }

    public List<Statement> getAllStatements() throws Exception {
        return FTDPUTestUtils.getAllStatements(repository);
    }

    @Override
    public void updateExistingDataGraph(String string, IRI uri) throws DataUnitException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private class MockIteration implements RDFDataUnit.Iteration {
        private final Iterator<IRI> iterator;

        private MockIteration() {
            this.iterator = iterationGraphs.iterator();
        }

        @Override
        public boolean hasNext() throws DataUnitException {
            return iterator.hasNext();
        }

        @Override
        public RDFDataUnit.Entry next() throws DataUnitException {
            return new MockEntry(iterator.next());
        }

        @Override
        public void close() throws DataUnitException {

        }
    }

    private static class MockEntry implements RDFDataUnit.Entry {
        private final IRI uri;

        private MockEntry(IRI uri) {
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
}
