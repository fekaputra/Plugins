package eu.unifiedviews.plugins.transformer.fusiontool.io;

import cz.cuni.mff.odcleanstore.fusiontool.config.LDFTConfigConstants;
import cz.cuni.mff.odcleanstore.fusiontool.conflictresolution.urimapping.UriMappingIterableImpl;
import cz.cuni.mff.odcleanstore.fusiontool.exceptions.LDFusionToolException;
import cz.cuni.mff.odcleanstore.fusiontool.util.CloseableRepositoryConnection;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import org.eclipse.rdf4j.OpenRDFException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class DataUnitSameAsLinkLoader {
    private static final Logger LOG = LoggerFactory.getLogger(DataUnitSameAsLinkLoader.class);
    private static RDFDataUnit dataUnit;
    private static Set<IRI> sameAsLinkTypes;

    public DataUnitSameAsLinkLoader(RDFDataUnit dataUnit, Set<IRI> sameAsLinkTypes) {
        this.dataUnit = dataUnit;
        this.sameAsLinkTypes = sameAsLinkTypes;
    }

    public void loadSameAsLinks(UriMappingIterableImpl uriMapping) throws LDFusionToolException {
        LOG.info("Loading sameAs links...");
        try (CloseableRepositoryConnection connection = new CloseableRepositoryConnection(dataUnit.getConnection())) {
            long startTime = System.currentTimeMillis();
            long loadedCount = loadFromConnection(uriMapping, connection.get());
            LOG.info(String.format("Loaded & resolved %,d sameAs links in %,d ms", loadedCount, System.currentTimeMillis() - startTime));
        } catch (OpenRDFException | DataUnitException e) {
            throw new LDFusionToolException("Error when loading owl:sameAs links from input", e);
        }
    }

    private long loadFromConnection(UriMappingIterableImpl uriMapping, RepositoryConnection connection)
            throws QueryEvaluationException, RepositoryException, MalformedQueryException {

        long loadedCount = 0;
        for (IRI link : sameAsLinkTypes) {
            String query = String.format("CONSTRUCT {?s <%1$s> ?o} WHERE {?s <%1$s> ?o}", link.stringValue());
            GraphQueryResult sameAsTriples = connection.prepareGraphQuery(QueryLanguage.SPARQL, query).evaluate();
            while (sameAsTriples.hasNext()) {
                uriMapping.addLink(sameAsTriples.next());
                loadedCount++;
                if (loadedCount % LDFTConfigConstants.LOG_LOOP_SIZE == 0) {
                    LOG.info("... loaded {} sameAs links", loadedCount);
                }
            }
        }
        return loadedCount;
    }
}
