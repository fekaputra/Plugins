package eu.unifiedviews.plugins.transformer.filestordft;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.util.RDFInserter;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.helpers.dpu.exec.UserExecContext;

public class CancellableCommitSizeInserter extends RDFInserter {
    private static final Logger LOG = LoggerFactory.getLogger(CancellableCommitSizeInserter.class);
    
    private int commitSize = 50000;

    private boolean transactionOpen = false;

    private int statementCounter = 0;
    
    private long realStatementCounter = 0L;

    private final UserExecContext ctx;

    public CancellableCommitSizeInserter(RepositoryConnection con, int commitSize, UserExecContext ctx) {
        super(con);
        this.commitSize = commitSize;
        this.ctx = ctx;
    }

    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        if (!transactionOpen) {
            try {
                con.begin();
            } catch (RepositoryException e) {
                throw new RDFHandlerException(e);
            }
            transactionOpen = true;
        }
        super.handleStatement(st);
        statementCounter++;
        if (transactionOpen && (statementCounter == commitSize)) {
            if (ctx.canceled()) {
                throw new RDFHandlerException("Cancelled by user");
            }
            try {
                con.commit();
                if (LOG.isDebugEnabled()) {
                    realStatementCounter+= statementCounter;
                    LOG.debug("Commit {}", realStatementCounter);
                }
            } catch (RepositoryException e) {
                try {
                    con.rollback();
                } catch (RepositoryException e1) {
                    throw new RDFHandlerException(e1);
                }
                throw new RDFHandlerException(e);
            }
            statementCounter = 0;
            transactionOpen = false;
        }
    }

    @Override
    public void endRDF() throws RDFHandlerException {
        if (transactionOpen) {
            try {
                con.commit();
            } catch (RepositoryException e) {
                try {
                    con.rollback();
                } catch (RepositoryException e1) {
                    throw new RDFHandlerException(e1);
                }
                throw new RDFHandlerException(e);
            }
        }
        super.endRDF();
    }
}
