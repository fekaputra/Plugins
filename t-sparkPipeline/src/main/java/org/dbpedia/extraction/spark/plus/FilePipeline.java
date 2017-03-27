package org.dbpedia.extraction.spark.plus;

import org.apache.commons.lang.NotImplementedException;
import org.dbpedia.spark.core.*;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chile on 23.03.17.
 * This Pipeline will be loaded by reading text files
 */
public class FilePipeline extends AbstractPipeline<String, String> {

    private List<PipelineStep<?, ?>> stages = new ArrayList<>();

    public FilePipeline(){   }

    @Override
    public URI uri() {
        //TODO
        return new URIImpl("http://example.org/path");
    }

    @Override
    public List<PipelineStep<Object, Object>> getStages() {
        throw new NotImplementedException("");
    }

    @Override
    public void setStages(List<PipelineStep<?, ?>> stages) {
        this.stages = stages;
    }

    @Override
    public DatasourceProfile<String> requires() {

        throw new NotImplementedException("");
    }

    @Override
    public DatasourceProfile<String> produces() {

        throw new NotImplementedException("");
    }

}
