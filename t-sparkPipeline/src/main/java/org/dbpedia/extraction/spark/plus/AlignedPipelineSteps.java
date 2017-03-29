package org.dbpedia.extraction.spark.plus;

import org.apache.spark.api.java.JavaRDD;
import org.dbpedia.extraction.spark.utils.UvStatement;
import org.dbpedia.spark.core.*;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;

import java.io.*;


/**
 * Created by chile on 27.03.17.
 * This is the collection of hard coded steps simulating the Aligned Use Case
 */
public class AlignedPipelineSteps implements Serializable{


    public static PipelineStep<Statement, String> StatementToString = new PipelineStep<Statement, String>(new Transformer<Statement, String>() {

        @Override
        public RddLike<String> transform(RddLike<Statement> in) {
            JavaRDD<String> javardd = in.map(x -> x.toString());
            return RddLike$.MODULE$.fromJavaRDD(javardd);
        }

        @Override
        public DatasourceProfile<Statement> requires() {
            return new DataFrameTemplate<Statement>();
        }

        @Override
        public DatasourceProfile<String> produces() {
            return new DataFrameTemplate<String>();
        }

        @Override
        public URI uri() {
            return null;
        }
    });

    public static PipelineStep <String, Statement> StringToStatement = new PipelineStep<String, Statement>(new Transformer<String, Statement>() {


        @Override
        public RddLike<Statement> transform(RddLike<String> in) {

            JavaRDD<Statement> javardd = ((JavaRDD<String>) in).<Statement>map(UvStatement::apply);
            return RddLike$.MODULE$.fromJavaRDD(javardd);
        }


        @Override
        public DatasourceProfile<String> requires() {
            return new DataFrameTemplate<String>();
        }

        @Override
        public DatasourceProfile<Statement> produces() {
            return new DataFrameTemplate<Statement>();
        }

        @Override
        public URI uri() {
            return null;
        }
    });
}
