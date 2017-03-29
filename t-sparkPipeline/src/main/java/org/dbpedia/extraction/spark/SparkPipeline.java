package org.dbpedia.extraction.spark;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.files.FilesHelper;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import org.apache.commons.io.FileUtils;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.storage.StorageLevel;
import org.dbpedia.extraction.spark.plus.AlignedPipelineSteps;
import org.dbpedia.extraction.spark.plus.StatementFilter;
import org.dbpedia.extraction.spark.utils.SparkDpuContext;
import org.dbpedia.extraction.spark.utils.UvStatement;
import org.dbpedia.spark.core.RddLike;
import org.dbpedia.spark.core.RddLike$;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * This DPU initializes the SPARK context and runs a SPARK Pipeline
 *
 * @author Chile
 */
@DPU.AsTransformer
public class SparkPipeline extends AbstractDpu<SparkPipelineConfig_V1> {

    private static final Logger log = LoggerFactory.getLogger(SparkPipeline.class);

    @DataUnit.AsInput(name = "sparkinput")
    public FilesDataUnit input;

    @DataUnit.AsOutput(name = "sparkoutput")
    public WritableFilesDataUnit output;

    final private SparkPipelineConfig_V1 config;
    final private SparkDpuContext sparkDpuContext;
    final private boolean isDebug;

	public SparkPipeline() {
		super(SparkPipelineVaadinDialog.class, ConfigHistory.noHistory(SparkPipelineConfig_V1.class));
		//init Config and MasterContext
        //TODO solve source of path parameter
        config = new SparkPipelineConfig_V1("/home/chile/unifiedviews/spark.config");
        this.sparkDpuContext = new SparkDpuContext(config.getConfig());
        this.isDebug = new Boolean(config.getSparkEntry("spark.uv-piepeline.debug"));
	}

	public void outerExecute() throws DPUException {
	    innerExecute();
    }
		
    @Override
    protected void innerExecute() throws DPUException {

        ArrayList<String> files = new ArrayList<>();
        ArrayList<FilesDataUnit.Entry> outFiles = new ArrayList<>();


        if(!this.isDebug) {
            ContextUtils.sendShortInfo(ctx, "Running SPARK pipeline");

            try {
                for (FilesDataUnit.Entry entry : FilesHelper.getFiles(input)) {
                    //get all input files
                    files.add(entry.getFileURIString());
                }
            } catch (DataUnitException e) {
                throw new DPUException(e);
            }
        }
        else
            files.add(config.getSparkEntry("spark.uv-piepeline.inputfile"));

        //load RDD
       RddLike<String> rdd = RddLike$.MODULE$.fromTextFiles(files);

       /**first SPARQL Construct DPU **/
        //transform each line into a Statement
        RddLike<Statement> stmtStream = AlignedPipelineSteps.StringToStatement.transform(rdd);

        //cache this state so we can use it multiple times
        stmtStream = RddLike$.MODULE$.fromJavaRDD(stmtStream.persist(StorageLevel.MEMORY_ONLY()));

        //create statement filter for allowed types (see config)
        StatementFilter typeFilter = new StatementFilter(null,
                Arrays.asList("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                Arrays.asList(this.config.getSparkEntry("spark.uv-piepeline.typefilter").split(",")));

        //run filter and collect resulting subjects into a list
        List<String> typeStatements = typeFilter.transform(stmtStream).collect().stream()
                .<String>map(x -> x.getSubject().stringValue()).collect(Collectors.toList());

        //broadcast this list to every worker node (so they can use it without transferring this list inefficiently)
        Broadcast<List<String>> typesAllowed = this.sparkDpuContext.getJavaSparkContext().broadcast(typeStatements);

        //create predicate filter (for descriptions and titles)
        StatementFilter predicateFilter = new StatementFilter(typesAllowed.value(),
                Arrays.asList(this.config.getSparkEntry("spark.uv-piepeline.predicatefilter").split(",")),null);

        //run this filter which is also our result of the first DPU
        RddLike<String> results = AlignedPipelineSteps.StatementToString.transform(predicateFilter.transform(stmtStream));

        /** load results of second (PPX) DPU **/
        RddLike<String> ppx = RddLike$.MODULE$.fromTextFiles(Collections.singletonList(config.getSparkEntry("spark.uv-piepeline.ppx")));

        //transform into RDD<Statement>
        stmtStream = AlignedPipelineSteps.StringToStatement.transform(ppx);

        //cache this state so we can use it multiple times
        stmtStream = RddLike$.MODULE$.fromJavaRDD(stmtStream.persist(StorageLevel.MEMORY_ONLY()));

        /** second SPARQL Construct DPU **/
        //create Statement filter for predicate http://schema.semantic-web.at/ppx/descriptionIsTaggedBy and collect results into Tuple-List
        List<Tuple2<String, String>> descriptionIsTaggedBy = new StatementFilter(null, Arrays.asList("http://schema.semantic-web.at/ppx/descriptionIsTaggedBy"),null)
                .transform(stmtStream).collect().stream().map(x -> new Tuple2<String, String>(x.getSubject().stringValue(), x.getObject().stringValue())).collect(Collectors.toList());

        //create the collection of valid subjects for the next filter
        List<String> subjectFilter = descriptionIsTaggedBy.stream().map(Tuple2::_2).collect(Collectors.toList());
        //create Statement filter for predicate http://commontag.org/ns#tagged and collect results into Tuple-List
        List<Tuple2<String, String>>  tagged = new StatementFilter(subjectFilter, Arrays.asList("http://commontag.org/ns#tagged"),null)
            .transform(stmtStream).collect().stream().map(x -> new Tuple2<String, String>(x.getSubject().stringValue(), x.getObject().stringValue())).collect(Collectors.toList());

        //create the collection of valid subjects for the next filter
        subjectFilter = tagged.stream().map(Tuple2::_2).collect(Collectors.toList());
        //create Statement filter for predicate http://commontag.org/ns#means and collect results into Tuple-List
        List<Tuple2<String, String>>  means = new StatementFilter(subjectFilter, Arrays.asList("http://commontag.org/ns#means"),null)
            .transform(stmtStream).collect().stream().map(x -> new Tuple2<String, String>(x.getSubject().stringValue(), x.getObject().stringValue())).collect(Collectors.toList());

        Map<String, List<String>> projects = new HashMap<String, List<String>>();

        //calculate a Map between projects and concepts from the temporary results
        for(Tuple2<String, String> project : descriptionIsTaggedBy){
            ArrayList<String> zw = new ArrayList<>();
            for(Tuple2<String, String> tag : tagged.stream().filter(x -> x._1().equals(project._2)).collect(Collectors.toList())){
                for(Tuple2<String, String> mean : means.stream().filter(y -> y._1().equals(tag._2)).collect(Collectors.toList()))
                    zw.add(mean._2);
            }
            projects.put(project._1, zw);
        }

        //create the collection of valid subjects (the concept) for the next filters (in this case all valid concepts -> flatmap values of map)
        subjectFilter = projects.values().stream().flatMap(List::stream).collect(Collectors.toList());
        //create Statement filter for predicate http://www.w3.org/2004/02/skos/core#inScheme and collect results into Tuple-List
        List<Tuple2<String, String>> schemes = new StatementFilter(subjectFilter, Arrays.asList("http://www.w3.org/2004/02/skos/core#inScheme"),null)
            .transform(stmtStream).collect().stream().map(x -> new Tuple2<String, String>(x.getSubject().stringValue(), x.getObject().stringValue())).collect(Collectors.toList());

        //create prefLabel map
        Map<String, String> prefLabel = new StatementFilter(subjectFilter, Arrays.asList("http://www.w3.org/2004/02/skos/core#prefLabel"),null)
            .transform(stmtStream).collect().stream().collect(Collectors.<Statement, String, String>toMap(e -> e.getSubject().stringValue(), e -> e.getObject().stringValue()));

        //create Statement filter for predicate http://www.w3.org/2004/02/skos/core#altLabel and collect results into Tuple-List
        List<Tuple2<String, String>> altLabel = new StatementFilter(subjectFilter, Arrays.asList("http://www.w3.org/2004/02/skos/core#altLabel"),null)
            .transform(stmtStream).collect().stream().map(x -> new Tuple2<String, String>(x.getSubject().stringValue(), x.getObject().stringValue())).collect(Collectors.toList());

        ArrayList<Statement> resultList = new ArrayList<>();

        //combine all temporary results in a collection of Statements
        for(String project : projects.keySet()){
            for(String concept: projects.get(project)) {
                for (String scheme : schemes.stream().filter(x -> x._1().equals(concept)).map(x -> x._2()).collect(Collectors.toList())) {
                    resultList.add(new UvStatement(new URIImpl(project), new URIImpl(scheme), new URIImpl(concept)));
                    resultList.add(new UvStatement(new URIImpl(concept), new URIImpl("http://www.w3.org/2004/02/skos/core#prefLabel"), new LiteralImpl(prefLabel.get(concept), "en")));
                    for(String label : altLabel.stream().filter(x -> x._1().equals(concept)).map(x -> x._2()).collect(Collectors.toList()))
                        resultList.add(new UvStatement(new URIImpl(concept), new URIImpl("http://www.w3.org/2004/02/skos/core#altLabel"), new LiteralImpl(label, "en")));
                }
            }
        }

        //tranform into RDD<Statement>
        JavaRDD<Statement> preResults = this.sparkDpuContext.getJavaSparkContext().<Statement>parallelize(resultList);

        //TODO: saving files needs to be revisited: temporary approach
        //save RDD to file (

        //coalesce multiple partitions into one -> save one output file
        preResults.coalesce(1, true).saveAsTextFile(this.config.getSparkEntry("spark.uv-piepeline.output"));


        if(!this.isDebug) {
            String outputFileName = "spark-result.nt";
            File tempOutFile = new File(config.getSparkEntry("spark.uv-piepeline.output").replace("file:", "") + "/part-00000");
            File outputFile = new File(ctx.getExecMasterContext().getDpuContext().getWorkingDir() + "/" + outputFileName);

            if (tempOutFile.exists())
                try {
                    FileUtils.copyFile(tempOutFile, outputFile);
                    FilesHelper.addFile(output, outputFile, outputFile.toURI().toString());
                } catch (IOException | DataUnitException e) {
                    throw new DPUException(e);
                }
            else
                throw new DPUException("SPARK output files was not found!");
        }

    }
	
}
