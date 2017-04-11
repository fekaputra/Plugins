package eu.unifiedviews.plugins.transformer.rdftofiles;

import java.util.Collection;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.rio.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Škoda Petr
 */
public class RdfWriterContextRenamer implements RDFWriter {

    /**
     * Wrap for single statement used to change context.
     */
    protected class StatementWrap implements Statement {

        protected Statement statement;

        @Override
        public Resource getSubject() {
            return statement.getSubject();
        }

        @Override
        public URI getPredicate() {
            return statement.getPredicate();
        }

        @Override
        public Value getObject() {
            return statement.getObject();
        }

        @Override
        public Resource getContext() {
            // TODO translate original context based on configuration
            return context;
        }

    }

    private static final Logger LOG = LoggerFactory.getLogger(RdfWriterContextRenamer.class);
    
    private static final int LOG_EACH_STATEMENTS = 10000;

    /**
     * Underlying rdf writer.
     */
    private final RDFWriter writer;

    /**
     * Context used for graphs.
     */
    private Resource context;

    /**
     * Wrap used to change context in statements.
     */
    private final StatementWrap statementWrap = new StatementWrap();

    private int counter = 0;
    
    private int counterNext = 0;

    public RdfWriterContextRenamer(RDFWriter writer) {
        this.writer = writer;
    }

    /**
     * Set context (graph URI) used in output for all statements.
     * 
     * @param context
     */
    public void setContext(Resource context) {
        this.context = context;
    }

    @Override
    public RDFFormat getRDFFormat() {
        return writer.getRDFFormat();
    }

    @Override
    public void setWriterConfig(WriterConfig wc) {
        writer.setWriterConfig(wc);
    }

    @Override
    public WriterConfig getWriterConfig() {
        return writer.getWriterConfig();
    }

    @Override
    public Collection<RioSetting<?>> getSupportedSettings() {
        return writer.getSupportedSettings();
    }

    @Override
    public void startRDF() throws RDFHandlerException {
        writer.startRDF();
    }

    @Override
    public void endRDF() throws RDFHandlerException {
        writer.endRDF();
    }

    @Override
    public void handleNamespace(String string, String string1) throws RDFHandlerException {
        writer.handleNamespace(string, string1);
    }

    @Override
    public void handleStatement(Statement stmnt) throws RDFHandlerException {
        // Replace context = use our statement wrap.
        statementWrap.statement = stmnt;
        // Call original function.
        writer.handleStatement(statementWrap);
        // Logging.
        counter++;
        if (counter > counterNext) {
            counterNext += LOG_EACH_STATEMENTS;
            LOG.info("Statement processed: {}", counter);
        }
    }

    @Override
    public void handleComment(String string) throws RDFHandlerException {
        writer.handleComment(string);
    }

}
