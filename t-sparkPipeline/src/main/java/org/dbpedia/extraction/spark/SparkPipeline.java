package org.dbpedia.extraction.spark;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.files.FilesHelper;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.storage.StorageLevel;
import org.dbpedia.extraction.spark.plus.AlignedPipelineSteps;
import org.dbpedia.extraction.spark.plus.FilePipeline;
import org.dbpedia.extraction.spark.plus.StatementFilter;
import org.dbpedia.spark.core.*;
import org.dbpedia.spark.core.datasources.MasterContext;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import scala.Function1;
import scala.reflect.ClassTag$;
import scala.reflect.api.TypeTags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


/**
 * This DPU initializes the SPARK context and runs a SPARK Pipeline
 *
 * @author Chile
 */
@DPU.AsTransformer
public class SparkPipeline extends AbstractDpu<SparkPipelineConfig_V1> {

    private static final Logger log = LoggerFactory.getLogger(SparkPipeline.class);

    @DataUnit.AsInput(name = "input")
    private FilesDataUnit input;

    @DataUnit.AsOutput(name = "output")
    private WritableFilesDataUnit output;

    private SparkPipelineConfig_V1 config;

	public SparkPipeline() {
		super(SparkPipelineVaadinDialog.class, ConfigHistory.noHistory(SparkPipelineConfig_V1.class));
		//init Config and MasterContext
        config = new SparkPipelineConfig_V1("TODO fake path");
	}

	public void outerExecute() throws DPUException {
	    innerExecute();
    }
		
    @Override
    protected void innerExecute() throws DPUException {

        //TODO revert ContextUtils.sendShortInfo(ctx, "SparkPipeline.message");

        ArrayList<String> files = new ArrayList<>();
        ArrayList<FilesDataUnit.Entry> outFiles = new ArrayList<>();

        try {
/*            for(FilesDataUnit.Entry entry : FilesHelper.getFiles(input)){
                //get all input files
                files.add(entry.getFileURIString());
                //create output file (based on input file names)
                outFiles.add(FilesHelper.createFile(output, "output-" + entry.getFileURIString()));
            }*/
            //TODO revert only for testing
            files.add(config.getInputFilePath());

            //load RDD
            RddLike<String> rdd = RddLike$.MODULE$.fromTextFiles(files);

            AlignedPipelineSteps steps = new AlignedPipelineSteps(new ParserConfig(), RDFFormat.TURTLE, new ValueFactoryImpl());
            //TODO this section has to be loaded dynamically via the config file -> see SparkPipelineConfig_V1
            RddLike<Statement> stmtStream = RddLike$.MODULE$.fromJavaRDD(steps.stringToStatement.transform(rdd).persist(StorageLevel.MEMORY_ONLY()), ClassTag$.MODULE$.<Statement>apply(Statement.class));

            StatementFilter typeFilter = new StatementFilter(null,
                    Arrays.asList("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                    Arrays.asList("https://w3id.org/dio#DesignRequirement",
                            "https://w3id.org/dio#DesignIntent",
                            "https://w3id.org/dio#MandatedSolution",
                            "https://w3id.org/dio#Argument",
                            "https://w3id.org/diopp#DeveloperIssue",
                            "https://w3id.org/diopp#SupportIssue",
                            "https://w3id.org/diopp#IdeaIssue",
                            "https://w3id.org/dio#Comment"));
            List<String> typeStatements = typeFilter.transform(stmtStream).collect().stream()
                    .<String>map(x -> x.getSubject().stringValue()).collect(Collectors.toList());
            Broadcast<List<String>> typesAllowed = config.getConfig().getJavaSparkContext().broadcast(typeStatements);

            StatementFilter predicateFilter = new StatementFilter(typesAllowed.value(), Arrays.asList("http://purl.org/dc/elements/1.1/title", "http://purl.org/dc/elements/1.1/description"), null);
            RddLike<String> results = steps.statementToString.transform(predicateFilter.transform(stmtStream));

            //TODO: saving files needs to be revisited: temporary approach
            results.saveAsTextFile(config.getOutputFilePath());

        } catch (Exception e) {
            throw new DPUException(e);
        }
    }
	
}
