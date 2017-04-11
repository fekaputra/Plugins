package eu.unifiedviews.plugins.transformer.rdfvalidator;

import java.io.StringWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.UnsupportedQueryLanguageException;
import org.eclipse.rdf4j.query.UpdateExecutionException;
import org.eclipse.rdf4j.query.parser.ParsedBooleanQuery;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.ParsedTupleQuery;
import org.eclipse.rdf4j.query.parser.QueryParserUtil;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext.MessageType;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.DataUnitUtils;
import eu.unifiedviews.helpers.dataunit.dataset.DatasetBuilder;
import eu.unifiedviews.helpers.dataunit.rdf.RDFHelper;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.config.migration.ConfigurationUpdate;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.rdf.sparql.SparqlProblemException;
import eu.unifiedviews.helpers.dpu.rdf.sparql.SparqlUtils;

@DPU.AsTransformer
public class RdfValidator extends AbstractDpu<RdfValidatorConfig_V2> {
    public static final Integer FIRST_N_PROBLEMATIC_MAPPINGS = 100;

    public static final String QUERY_COPY = "INSERT { ?s ?p ?o } WHERE { ?s ?p ?o }";

    private static final Logger LOG = LoggerFactory.getLogger(RdfValidator.class);

    @DataUnit.AsInput(name = "rdfInput")
    public RDFDataUnit rdfInput;

    @DataUnit.AsOutput(name = "rdfCopyOfInput", optional = true)
    public WritableRDFDataUnit rdfCopyOfInput;

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    @ExtensionInitializer.Init(param = "eu.unifiedviews.plugins.transformer.rdfvalidator.RdfValidatorConfig_V2")
    public ConfigurationUpdate _ConfigurationUpdate;

    public RdfValidator() {
        super(RdfValidatorVaadinDialog.class, ConfigHistory.history(RdfValidatorConfig_V2.class)
                .alternative(RdfValidatorConfig_V1.class).addCurrent(RdfValidatorConfig_V2.class));
    }

    @Override
    protected void innerExecute() throws DPUException {
        ContextUtils.sendMessage(ctx, MessageType.INFO, "status.starting", "");
        if (StringUtils.isBlank(config.getQuery())) {
            throw ContextUtils.dpuException(ctx, "error.invalidConfiguration.queryEmpty");
        }
        ParsedQuery parsedQuery;
        try {
            parsedQuery = QueryParserUtil.parseQuery(QueryLanguage.SPARQL, config.getQuery(), (String) null);
            if (!(parsedQuery instanceof ParsedTupleQuery) && !(parsedQuery instanceof ParsedBooleanQuery)) {
                throw ContextUtils.dpuException(ctx, "error.unsupported.query.type");
            }
        } catch (UnsupportedQueryLanguageException | MalformedQueryException ex) {
            throw ContextUtils.dpuException(ctx, ex, "error.query.parse");
        }

        // Get input graphs.
        LOG.info("Reading input graphs ...");
        final Set<RDFDataUnit.Entry> graphs;
        try {
            graphs = RDFHelper.getGraphs(rdfInput);
        } catch (DataUnitException ex) {
            throw ContextUtils.dpuException(ctx, "error.dataunit.graphList");
        }
        LOG.info("Reading input graphs ... done");

        RepositoryConnection connection = null;
        try {
            connection = rdfInput.getConnection();
            Set<URI> graphURIs = new HashSet<>();
            for (RDFDataUnit.Entry entry : graphs) {
                graphURIs.add(entry.getDataGraphURI());
            }

            // Prepare query.
            if (parsedQuery instanceof ParsedTupleQuery) {
                TupleQuery query;
                TupleQueryResult result = null;
                try {
                    query = connection.prepareTupleQuery(QueryLanguage.SPARQL, config.getQuery());
                    query.setDataset(new DatasetBuilder().withDefaultGraphs(graphURIs).build());
                    result = query.evaluate();
                    if (result.hasNext()) {
                        StringBuilder sb = new StringBuilder();
                        int i = 0;
                        while (result.hasNext() && i < FIRST_N_PROBLEMATIC_MAPPINGS) {
                            sb.append(result.next().toString());
                            sb.append(" ; ");
                        }
                        sb.delete(sb.length() - 3, sb.length());
                        String logMessage = sb.toString();
                        if (config.isFailExecution()) {
                            LOG.error(logMessage);
                            throw ContextUtils.dpuException(ctx, "error.data.validation.mappings", logMessage);
                        } else {
                            LOG.warn(logMessage);
                            ContextUtils.sendMessage(ctx, MessageType.WARNING, "error.data.validation.mappings", logMessage);
                        }
                    }
                } catch (RepositoryException | MalformedQueryException | QueryEvaluationException ex) {
                    throw ContextUtils.dpuException(ctx, "error.query.execution");
                } finally {
                    if (result != null) {
                        try {
                            result.close();
                        } catch (QueryEvaluationException ex) {
                            LOG.warn("Error in close", ex);
                        }
                    }
                }
            } else if (parsedQuery instanceof ParsedBooleanQuery) {
                BooleanQuery query;
                try {
                    query = connection.prepareBooleanQuery(QueryLanguage.SPARQL, config.getQuery());
                    query.setDataset(new DatasetBuilder().withDefaultGraphs(graphURIs).build());
                    if (query.evaluate()) {
                        if (config.isFailExecution()) {
                            throw ContextUtils.dpuException(ctx, "error.data.validation");
                        } else {
                            ContextUtils.sendMessage(ctx, MessageType.WARNING, "error.data.validation", "");
                        }
                    }
                } catch (RepositoryException | MalformedQueryException | QueryEvaluationException ex) {
                    throw ContextUtils.dpuException(ctx, "error.query.execution");
                }
            }

            if (rdfCopyOfInput != null) {
                List<RDFDataUnit.Entry> source;
                try {
                    source = DataUnitUtils.getMetadataEntries(rdfInput);
                    final RDFDataUnit.Entry target = DataUnitUtils.getWritableMetadataEntry(rdfCopyOfInput);
                    final SparqlUtils.SparqlUpdateObject update = SparqlUtils.createInsert(QUERY_COPY, source, target);
                    SparqlUtils.execute(connection, update);
                } catch (DataUnitException | SparqlProblemException | RepositoryException | MalformedQueryException | UpdateExecutionException ex) {
                    throw ContextUtils.dpuException(ctx, "error.data.copy");
                }

            }
        } catch (DataUnitException ex) {
            throw ContextUtils.dpuException(ctx, ex, "error.dataunit");
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {
                    LOG.info("Error in close", ex);
                }
            }
        }
    }
}
