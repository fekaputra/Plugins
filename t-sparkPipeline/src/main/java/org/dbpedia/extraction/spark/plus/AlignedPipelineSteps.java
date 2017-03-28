package org.dbpedia.extraction.spark.plus;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.storage.StorageLevel;
import org.dbpedia.spark.core.*;
import org.dbpedia.spark.core.datasources.MasterContext;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.apache.spark.api.java.function.Function;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.rio.*;
import org.openrdf.rio.helpers.NTriplesParserSettings;
import scala.reflect.ClassTag$;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by chile on 27.03.17.
 * This is the collection of hard coded steps simulating the Aligned Use Case
 */
public class AlignedPipelineSteps implements Serializable{

    private ParserConfig parserConfig;
    private RDFFormat dataFormat;
    private ValueFactory valueFactory;


    public AlignedPipelineSteps(ParserConfig pc, RDFFormat df, ValueFactory vf){
         this.dataFormat = df;
         this.parserConfig = pc;
         this.valueFactory = vf;
    }




    public PipelineStep<Statement, String> statementToString = new PipelineStep<Statement, String>(new Transformer<Statement, String>() {

        @Override
        public RddLike<String> transform(RddLike<Statement> in) {
            JavaRDD<String> javardd = in.map(x -> x.toString());
            return RddLike$.MODULE$.fromJavaRDD(javardd, ClassTag$.MODULE$.<String>apply(String.class));
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

    public PipelineStep <String, Statement> stringToStatement = new PipelineStep<String, Statement>(new Transformer<String, Statement>() {

        Statement current = null;
        //create simple RDFHandler which updates current for each new line
        RDFHandler h = new RDFHandler() {
            @Override
            public void startRDF() throws RDFHandlerException {
            }

            @Override
            public void endRDF() throws RDFHandlerException {
            }

            @Override
            public void handleNamespace(String s, String s1) throws RDFHandlerException {
                current = null;
            }

            @Override
            public void handleStatement(Statement statement) throws RDFHandlerException {
                current = statement;
            }

            @Override
            public void handleComment(String s) throws RDFHandlerException {
                current = null;
            }
        };

        @Override
        public RddLike<Statement> transform(RddLike<String> in) {
            RDFParser rdfParser = Rio.createParser(dataFormat, valueFactory);
            rdfParser.setParserConfig(AlignedPipelineSteps.this.parserConfig);
            rdfParser.setRDFHandler(h);

            JavaRDD<Statement> javardd = ((JavaRDD<String>) in).map(new Function<String, Statement>() {
                @Override
                public Statement call(String line) throws Exception {
                    //rdfParser.parse(new ByteArrayInputStream(line.getBytes(StandardCharsets.UTF_8)), "");
                    return null;
                }
            });
            return RddLike$.MODULE$.fromJavaRDD(javardd, ClassTag$.MODULE$.<Statement>apply(Statement.class));
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
