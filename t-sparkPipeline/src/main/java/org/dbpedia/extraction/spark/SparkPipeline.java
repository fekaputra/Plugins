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
import org.dbpedia.extraction.spark.plus.FilePipeline;
import org.dbpedia.spark.core.*;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import scala.Function1;
import scala.reflect.ClassTag$;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


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

	public SparkPipeline() {
		super(SparkPipelineVaadinDialog.class, ConfigHistory.noHistory(SparkPipelineConfig_V1.class));
	}
		
    @Override
    protected void innerExecute() throws DPUException {

        ContextUtils.sendShortInfo(ctx, "SparkPipeline.message");

        ArrayList<String> files = new ArrayList<>();
        ArrayList<FilesDataUnit.Entry> outFiles = new ArrayList<>();

        try {
            for(FilesDataUnit.Entry entry : FilesHelper.getFiles(input)){
                //get all input files
                files.add(entry.getFileURIString());
                //create output file (based on input file names)
                outFiles.add(FilesHelper.createFile(output, "output-" + entry.getFileURIString()));
            }

            //load RDD
            RddLike<String> rdd = RddLike$.MODULE$.fromTextFiles(files);

            //TODO this section has to be loaded dynamically via the config file -> see SparkPipelineConfig_V1
            PipelineStep <String, String> step1 = new PipelineStep<String, String>(new Transformer<String, String>() {
                @Override
                public DataFrame<String> transform(DataFrame<String> in) {
                    JavaRDD<String> javardd = ((JavaRDD<String>) in).<String>map(x -> x);
                    return RddLike$.MODULE$.fromJavaRDD(javardd, ClassTag$.MODULE$.<String>apply(String.class));
                }

                @Override
                public DatasourceProfile<String> requires() {
                    return null;
                }

                @Override
                public DatasourceProfile<String> produces() {
                    return null;
                }

                @Override
                public URI uri() {
                    return null;
                }
            });

            PipelineStep <String, String> step2 = new PipelineStep<String, String>(new Transformer<String, String>() {
                @Override
                public DataFrame<String> transform(DataFrame<String> in) {
                    JavaRDD<String> javardd = ((JavaRDD<String>) in).<String>map(x -> x);
                    return RddLike$.MODULE$.fromJavaRDD(javardd, ClassTag$.MODULE$.<String>apply(String.class));
                }

                @Override
                public DatasourceProfile<String> requires() {
                    return null;
                }

                @Override
                public DatasourceProfile<String> produces() {
                    return null;
                }

                @Override
                public URI uri() {
                    return null;
                }
            });


            //create pipeline
            FilePipeline pipeline = new FilePipeline();
            pipeline.setStages(Arrays.asList(step1, step2));

            //transform (run SPARK pipeline
            rdd = (RddLike<String>) pipeline.transform(rdd);

            //TODO: saving files needs to be revisited: temporary approach
            rdd.saveAsTextFile(outFiles.get(0).getFileURIString());

        } catch (DataUnitException e) {
            throw new DPUException(e);
        }
    }
	
}
