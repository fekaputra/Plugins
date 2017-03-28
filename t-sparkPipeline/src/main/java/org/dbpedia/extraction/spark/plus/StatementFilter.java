package org.dbpedia.extraction.spark.plus;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.dbpedia.spark.core.*;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import scala.reflect.ClassTag$;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * Created by chile on 28.03.17.
 */
public class StatementFilter implements Transformer<Statement,Statement> {

    private List<String> subjects;
    private List<String> predicates;
    private List<String> objects;

    public StatementFilter(List<String> subjects, List<String> predicats, List<String> objects){
        this.subjects = subjects != null ? subjects : Collections.emptyList();
        this.predicates = predicats != null ? predicats : Collections.emptyList();
        this.objects = objects != null ? objects : Collections.emptyList();
    }

    @Override
    public URI uri() {
        return null;
    }

    @Override
    public RddLike<Statement> transform(RddLike<Statement> in) {
        JavaRDD<Statement> javardd = in.filter(x -> new Boolean(
                (this.subjects.isEmpty() || this.subjects.contains(x.getSubject().stringValue())) &&
                (this.predicates.isEmpty() || this.predicates.contains(x.getPredicate().stringValue())) &&
                        (this.objects.isEmpty() || this.objects.contains(x.getObject().stringValue()))));
        return RddLike$.MODULE$.fromJavaRDD(javardd, ClassTag$.MODULE$.<Statement>apply(Statement.class));
    }

    @Override
    public DatasourceProfile<Statement> requires() {
        return new DataFrameTemplate<Statement>();
    }

    @Override
    public DatasourceProfile<Statement> produces() {
        return new DataFrameTemplate<Statement>();
    }
}
