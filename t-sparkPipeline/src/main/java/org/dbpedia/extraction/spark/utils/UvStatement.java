package org.dbpedia.extraction.spark.utils;

import org.openrdf.model.*;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;

import java.util.Arrays;
import java.util.List;

/**
 * Overriding StatementImpl to provide a N-Triple toString function and an apply method
 * -> parser/writer are not necessary anymore
 *
 * Created by chile on 28.03.17.
 */
public class UvStatement implements Statement {

    private Resource subject;
    private URI predicate;
    private Value object;
    private URI graph;

    public UvStatement(Resource subject, URI predicate, Value object) {
        this(subject, predicate, object, null);
    }

    public UvStatement(Resource subject, URI predicate, Value object, URI context){
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
        this.graph = context;
    }

    /**
     * function parses a string into a UvStatement
     * @param nTriple
     * @return
     */
    public static UvStatement apply(String nTriple){

        //empty lines
        if(nTriple.isEmpty())
            return null;

        nTriple = nTriple.trim();

        //comments
        if(nTriple.startsWith("#"))
            return null;

        int currentIndex = nTriple.indexOf('>');

        String subject = nTriple.substring(1, currentIndex);
        currentIndex = nTriple.indexOf('<', currentIndex)+1;

        String predicate = nTriple.substring(currentIndex, nTriple.indexOf('>', currentIndex));

        int tempIndex = nTriple.indexOf('"', currentIndex + predicate.length());
        currentIndex = tempIndex == -1 ? nTriple.indexOf('<', currentIndex + predicate.length()) : tempIndex;

        String object;

        //check if object is a literal or URI
        boolean isLiteral = true;

        if(nTriple.charAt(currentIndex) == '<') {
            object = nTriple.substring(currentIndex + 1, nTriple.indexOf('>', currentIndex+1));
            isLiteral = false;
        }
        else
            object = nTriple.substring(currentIndex+1, nTriple.lastIndexOf('"'));       //there can only be two unescaped '"' -> we take the first and last

        currentIndex = currentIndex + object.length() + 2;

        String datatype = nTriple.charAt(currentIndex) == '^' ? nTriple.substring(nTriple.indexOf('<', currentIndex)+1, nTriple.indexOf('>', currentIndex)) : null;

        String language = nTriple.charAt(currentIndex) == '@' ? nTriple.substring(currentIndex+1, currentIndex+4).trim() : null;

        currentIndex = currentIndex+ (datatype != null ? datatype.length()+4 : 0);
        currentIndex = currentIndex+ (language != null ? language.length()+1 : 0);

        //check for optional context
        String context = nTriple.indexOf('<', currentIndex) == -1 ? null : nTriple.substring(nTriple.indexOf('<', currentIndex)+1, nTriple.indexOf('>', currentIndex));

        //create correct object value
        Value value;

        if(isLiteral){
            if(datatype != null)
                value = new LiteralImpl(object.trim(), new URIImpl(datatype.trim()));
            else if(language != null)
                value = new LiteralImpl(object.trim(), language);
            else
                value = new LiteralImpl(object.trim());
        }
        else
            value = new URIImpl(object.trim());

        try {
            return new UvStatement(new URIImpl(subject.trim()), new URIImpl(predicate.trim()), value, context != null ? new URIImpl(context) : null);
        } catch (Exception e) {
            //TODO
            e.printStackTrace();
            return null;
        }
    }

    private static final List<String> forbiddenTypes = Arrays.asList("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString", "http://www.w3.org/2001/XMLSchema#string");

    @Override
    public String toString() {
        //subject
        StringBuilder buf = new StringBuilder("<" + this.getSubject().stringValue().trim() + ">");
        //predicate
        buf.append("\t<").append(this.getPredicate().stringValue().trim()).append(">");
        //value
        if(URI.class.isAssignableFrom(this.getObject().getClass()))
            buf.append("\t<").append(this.getObject().stringValue().trim()).append(">");
        else {
            buf.append("\t\"").append(this.getObject().stringValue().trim()).append("\"");
            URI type = ((Literal)(this.getObject())).getDatatype();
            if(type != null && !forbiddenTypes.contains(type.stringValue()))
                buf.append("^^<").append(((Literal) (this.getObject())).getDatatype().stringValue().trim()).append(">");
            else if(((Literal)(this.getObject())).getLanguage() != null)
                buf.append("@").append(((Literal) (this.getObject())).getLanguage().trim());
        }
        if(this.getContext() != null)
            buf.append("\t<").append(this.getContext().stringValue().trim()).append(">");

        buf.append(" .");
        return buf.toString();
    }

    @Override
    public Resource getSubject() {
        return this.subject;
    }

    @Override
    public URI getPredicate() {
        return this.predicate;
    }

    @Override
    public Value getObject() {
        return this.object;
    }

    @Override
    public Resource getContext() {
        return this.graph;
    }
}
