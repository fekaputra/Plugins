package org.dbpedia.extraction.spark.plus;

import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;


/**
 * Created by chile on 22.03.17.
 */
public class StatementSupplier implements org.openrdf.rio.RDFHandler, Supplier<Statement>{

    private BlockingQueue<Statement> queue = new LinkedBlockingQueue<>(20);

    public void startRDF() throws RDFHandlerException {
        //something at the beginning of the stream
    }


    @Override
    public void endRDF() throws RDFHandlerException {
        //something after the source is empty
    }

    @Override
    public void handleNamespace(String s, String s1) throws RDFHandlerException {
        /**
         * TODO think about how to deal with new Namespaces in a distributed context
         */
    }

    @Override
    public void handleStatement(Statement statement) throws RDFHandlerException {
        try {
            queue.put(statement);
        } catch (InterruptedException e) {
            throw new RDFHandlerException(e);
        }
    }

    @Override
    public void handleComment(String s) throws RDFHandlerException {
        //nothing
    }

    @Override
    public Statement get() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            //TODO Exception  handling
            return null;
        }
    }
}
