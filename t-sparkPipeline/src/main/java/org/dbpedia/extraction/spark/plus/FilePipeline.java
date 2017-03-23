package org.dbpedia.extraction.spark.plus;

import org.dbpedia.spark.core.*;
import org.openrdf.model.URI;

import java.util.List;

/**
 * Created by chile on 23.03.17.
 * This Pipeline will be loaded by reading text files
 */
public class FilePipeline implements Pipeline<String, String> {

    public FilePipeline(){   }

    @Override
    public URI uri() {
        return null;
    }

    @Override
    public List<PipelineStep<Object, Object>> getStages() {
        return null;
    }

    @Override
    public void setStages(List<PipelineStep<?, ?>> stages) {

    }

    @Override
    public DataFrame<String> transform(DataFrame<String> in) {
        return Pipeline.super.transform(in);
    }

    @Override
    public DatasourceProfile<String> requires() {
        return null;
    }

    @Override
    public DatasourceProfile<String> produces() {
        return null;
    }

}
