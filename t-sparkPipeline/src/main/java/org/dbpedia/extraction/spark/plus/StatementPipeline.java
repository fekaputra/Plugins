package org.dbpedia.extraction.spark.plus;

import org.dbpedia.spark.core.DataFrame;
import org.dbpedia.spark.core.DatasourceProfile;
import org.dbpedia.spark.core.Pipeline;
import org.dbpedia.spark.core.PipelineStep;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;

import java.util.List;

/**
 * Created by chile on 23.03.17.
 * This pipeline transforms OpenRDF Statements fed by a RepositoryConnection
 */
public class StatementPipeline implements Pipeline<Statement, Statement> {
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
    public DataFrame<Statement> transform(DataFrame<Statement> in) {
        return null;
    }

    @Override
    public DatasourceProfile<Statement> requires() {
        return null;
    }

    @Override
    public DatasourceProfile<Statement> produces() {
        return null;
    }
}
