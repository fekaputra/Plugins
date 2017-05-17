package eu.unifiedviews.plugins.transformer.fusiontool.testutils;

import cz.cuni.mff.odcleanstore.core.ODCSUtils;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.ValueFactoryImpl;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility methods for JUnit tests.
 */
public final class FTDPUTestUtils {
    private static final ValueFactory VF = ValueFactoryImpl.getInstance();

    /** Hide constructor for a utility class. */
    private FTDPUTestUtils() {
    }

    private static AtomicLong uriCounter = new AtomicLong(0);

    /** Returns a IRI unique within a test run. @return IRI as a string */
    public static String getUniqueURIString() {
        return "http://example.com/" + Long.toString(uriCounter.incrementAndGet());
    }
    
    /** Returns a IRI unique within a test run. @return IRI */
    public static IRI getUniqueURI() {
        return VF.createURI(getUniqueURIString());
    }

    /** Resets the IRI counter used by {@link #getUniqueURIString()}. */
    public static void resetURICounter() {
        uriCounter.set(0);
    }

    /**
     * Create a new quad with the given subject, predicate and object with a unique named graph IRI.
     * @param subjectURI subject IRI
     * @param predicateURI predicate IRI
     * @param objectURI object IRI
     * @return quad
     */
    public static Statement createStatement(String subjectURI, String predicateURI, String objectURI) {
        return VF.createStatement(
                VF.createURI(subjectURI),
                VF.createURI(predicateURI),
                VF.createURI(objectURI),
                VF.createURI(getUniqueURIString()));
    }
    
    /**
     * Create a new quad with the given subject, predicate and object with a unique named graph IRI.
     * @param subjectURI subject IRI
     * @param predicateURI predicate IRI
     * @param objectURI object IRI
     * @return quad
     */
    public static Statement createStatement(IRI subjectURI, IRI predicateURI, IRI objectURI) {
        return VF.createStatement(
                (subjectURI),
                (predicateURI),
                (objectURI),
                (getUniqueURI()));
    }
    
    /** Create a new unique quad. @return quad */
    public static Statement createStatement() {
        return VF.createStatement(
                VF.createURI(getUniqueURIString()),
                VF.createURI(getUniqueURIString()),
                VF.createURI(getUniqueURIString()),
                VF.createURI(getUniqueURIString()));
    }
    
    /**
     * Create a new quad with the given subject, predicate, object and named graph IRI.
     * @param subjectURI subject IRI
     * @param predicateURI predicate IRI
     * @param objectURI object IRI
     * @param namedGraphURI named graph IRI
     * @return quad
     */
    public static Statement createStatement(String subjectURI, String predicateURI, String objectURI, String namedGraphURI) {
        return VF.createStatement(
                VF.createURI(subjectURI),
                VF.createURI(predicateURI),
                VF.createURI(objectURI),
                VF.createURI(namedGraphURI));
    }

    /**
     * Compare two triples for equality; null-proof.
     * @param statement1 a triple
     * @param statement2 a triple
     * @return true iff the two triples are equal
     */
    public static boolean statementsEqual(Statement statement1, Statement statement2) {
        if (statement1 == null || statement2 == null) {
            return statement1 == statement2;
        }
        return statement1.equals(statement2) 
                && ODCSUtils.nullProofEquals(statement1.getContext(), statement2.getContext());
    }

    public static IRI createHttpUri(String uriPart) {
        return VF.createURI("http://" + uriPart);
    }

    public static Statement createHttpStatement(String subjectUri, String predicateUri, String objectUri) {
        return createHttpStatement(subjectUri, predicateUri, objectUri, null);
    }

    public static Statement createHttpStatement(String subjectUri, String predicateUri, String objectUri, String contextUri) {
        return VF.createStatement(
                createHttpUri(subjectUri),
                createHttpUri(predicateUri),
                createHttpUri(objectUri),
                contextUri != null ? createHttpUri(contextUri) : null);
    }

    public static Statement setContext(Statement statement, Resource context) {
        return VF.createStatement(
                statement.getSubject(),
                statement.getPredicate(),
                statement.getObject(),
                context);
    }

    public static Statement setSubject(Statement statement, Resource subject) {
        return VF.createStatement(
                subject,
                statement.getPredicate(),
                statement.getObject(),
                statement.getContext());
    }

    public static Repository createRepository(Collection<Statement> statements) throws RepositoryException {
        Repository repository = new SailRepository(new MemoryStore());
        repository.initialize();
        RepositoryConnection connection = repository.getConnection();
        connection.add(statements);
        connection.close();
        return repository;
    }

    public static List<Statement> getAllStatements(Repository repository) throws RepositoryException {
        RepositoryConnection connection = repository.getConnection();
        try {
            List<Statement> result = new ArrayList<>();
            RepositoryResult<Statement> repositoryResult = connection.getStatements(null, null, null, false);
            try {
                while (repositoryResult.hasNext()) {
                    result.add(repositoryResult.next());
                }
            } finally {
                repositoryResult.close();
            }
            return result;
        } finally {
            connection.close();
        }
    }
}
