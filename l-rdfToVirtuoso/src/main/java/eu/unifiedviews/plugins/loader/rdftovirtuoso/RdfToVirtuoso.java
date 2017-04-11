package eu.unifiedviews.plugins.loader.rdftovirtuoso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.URIImpl;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.UpdateExecutionException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import virtuoso.sesame2.driver.VirtuosoRepository;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.copy.CopyHelper;
import eu.unifiedviews.helpers.dataunit.copy.CopyHelpers;
import eu.unifiedviews.helpers.dataunit.rdf.RDFHelper;
import eu.unifiedviews.helpers.dataunit.resource.Resource;
import eu.unifiedviews.helpers.dataunit.resource.ResourceHelper;
import eu.unifiedviews.helpers.dataunit.resource.ResourceHelpers;
import eu.unifiedviews.helpers.dataunit.virtualgraph.VirtualGraphHelper;
import eu.unifiedviews.helpers.dataunit.virtualgraph.VirtualGraphHelpers;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.exec.UserExecContext;

@DPU.AsLoader
public class RdfToVirtuoso extends AbstractDpu<RdfToVirtuosoConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(RdfToVirtuoso.class);

    @DataUnit.AsInput(name = "rdfInput")
    public RDFDataUnit rdfInput;

    @DataUnit.AsOutput(name = "rdfOutput", optional = true)
    public WritableRDFDataUnit rdfOutput;

    VirtuosoRepository virtuosoRepository = null;

    static final String CLEAR_QUERY = "CLEAR GRAPH <%s>";

    public static final String CONFIGURATION_VIRTUOSO_USERNAME = "dpu.l-rdfToVirtuoso.username";

    public static final String CONFIGURATION_VIRTUOSO_PASSWORD = "dpu.l-rdfToVirtuoso.password";

    public static final String CONFIGURATION_VIRTUOSO_JDBC_URL = "dpu.l-rdfToVirtuoso.jdbc.url";

    public RdfToVirtuoso() {
        super(RdfToVirtuosoVaadinDialog.class, ConfigHistory.noHistory(RdfToVirtuosoConfig_V1.class));
    }

    /**
     * Execute the main part - loading of data into virtuoso using supplied config instance
     * This method is public, since it is used from at least one outside place https://github.com/OpenDataNode/UVPlugin-rdfToVirtuosoAndCkan
     * Please take this into account when changing method interface.
     * 
     * @param ctx
     *            context
     * @param config
     *            DPU configuration
     * @throws DPUException
     */
    public void outerExecute(UserExecContext ctx, RdfToVirtuosoConfig_V1 config) throws DPUException {
        Map<String, String> environment = ctx.getExecMasterContext().getDpuContext().getEnvironment();
        String virtuosoJdbcUrl = environment.get(CONFIGURATION_VIRTUOSO_JDBC_URL);
        if (config.getVirtuosoUrl() == null || config.getVirtuosoUrl().isEmpty()) {
            config.setVirtuosoUrl(virtuosoJdbcUrl);
        }
        String username = environment.get(CONFIGURATION_VIRTUOSO_USERNAME);
        if (config.getUsername() == null || config.getUsername().isEmpty()) {
            config.setUsername(username);
        }
        String password = environment.get(CONFIGURATION_VIRTUOSO_PASSWORD);
        if (config.getPassword() == null || config.getPassword().isEmpty()) {
            config.setPassword(password);
        }
        final URI globalOutGraphURI = org.apache.commons.lang3.StringUtils.isEmpty(config.getTargetGraphName()) ? null : new URIImpl(config.getTargetGraphName());
        if (config.getThreadCount() == 0) {
            config.setThreadCount(1);
        }
        RepositoryConnection externalConnection = null;
        virtuosoRepository = new VirtuosoRepository(config.getVirtuosoUrl(), config.getUsername(), config.getPassword());
        try {
            virtuosoRepository.initialize();
        } catch (RepositoryException ex) {
            throw ContextUtils.dpuException(ctx, ex, "RdfToVirtuoso.executeInner.exception");
        }

        if (globalOutGraphURI != null) {
            try {
                externalConnection = virtuosoRepository.getConnection();
                if (config.isClearDestinationGraph()) {
                    LOG.info("Clearing destination graph");
                    Update update = externalConnection.prepareUpdate(QueryLanguage.SPARQL, String.format(CLEAR_QUERY, globalOutGraphURI));
                    update.execute();
                    LOG.info("Cleared destination graph");
                }
            } catch (RepositoryException | MalformedQueryException | UpdateExecutionException ex) {
                throw ContextUtils.dpuException(ctx, ex, "RdfToVirtuoso.executeInner.exception");
            } finally {
                if (externalConnection != null) {
                    try {
                        externalConnection.close();
                    } catch (RepositoryException ex) {
                        LOG.warn("Error closing repository connection", ex);
                    }
                }
            }
        }

        VirtualGraphHelper inVirtualGraphHelper = VirtualGraphHelpers.create(rdfInput);

        final ConcurrentLinkedQueue<Work> workQueue = new ConcurrentLinkedQueue<Work>();
        int all = 0;
        final AtomicInteger done = new AtomicInteger(0);
        try {
            for (RDFDataUnit.Entry inEntry : RDFHelper.getGraphs(rdfInput)) {
                Work work = new Work();
                work.inEntry = inEntry;
                if (globalOutGraphURI == null) {
                    URI outGraphURI = null;
                    String outGraphURIString = inVirtualGraphHelper.getVirtualGraph(inEntry.getSymbolicName());
                    if (outGraphURIString == null) {
                        outGraphURI = inEntry.getDataGraphURI();
                    } else {
                        outGraphURI = new URIImpl(outGraphURIString);
                    }
                    work.clearGraphBeforeLoad = config.isClearDestinationGraph();
                    work.outDataGraphURI = outGraphURI;
                    work.perGraphMode = true;
                    workQueue.offer(work);
                } else {
                    work.clearGraphBeforeLoad = false;
                    work.perGraphMode = false;
                    work.outDataGraphURI = globalOutGraphURI;
                    workQueue.offer(work);
                }
                all++;
            }
        } catch (DataUnitException ex) {
            throw ContextUtils.dpuException(ctx, ex, "RdfToVirtuoso.executeInner.exception");
        } finally {
            inVirtualGraphHelper.close();
        }

        ExecutorService executor = null;
        try {
            List<Future<Status>> futures = new ArrayList<>(config.getThreadCount());
            executor = Executors.newFixedThreadPool(config.getThreadCount());
            for (int i = 0; i < config.getThreadCount(); i++) {
                final String loggerName = LOG.getName() + ".Worker" + i;
                futures.add(executor.<Status> submit(new LoadWorker(loggerName,
                        workQueue,
                        done,
                        rdfInput,
                        rdfOutput,
                        virtuosoRepository,
                        config.isSkipOnError())));
            }
            executor.shutdown();
            LOG.info("Started {} load threads", config.getThreadCount());

            try {
                while (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    LOG.info("Processing {}/{} files", done.get(), all);
                    for (Future<Status> future : futures) {
                        try {
                            Status status = future.get(1, TimeUnit.SECONDS);
                            if (!status.skippedEntries.isEmpty()) {
                                LOG.warn("Worker " + future.toString() + " finished with " + status.skippedEntries.size() + " skipped entries");
                            }
                        } catch (TimeoutException ex) {
                            LOG.info("Timeout reached, {}", ex.getLocalizedMessage());
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            throw ContextUtils.dpuExceptionCancelled(ctx);
                        } catch (CancellationException | ExecutionException ex) {
                            throw ContextUtils.dpuException(ctx, ex, "RdfToVirtuoso.executeInner.exception");
                        }
                    }
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw ContextUtils.dpuExceptionCancelled(ctx);
            }
            LOG.info("Finished all threads");

            if (globalOutGraphURI != null) {
                final String outputSymbolicName = "RdfToVirtuosoGenerated";
                LOG.info("Output symbolic name: {}", outputSymbolicName);
                rdfOutput.addNewDataGraph(outputSymbolicName);
                Resource resource = ResourceHelpers.getResource(rdfOutput, outputSymbolicName);
                Date now = new Date();
                resource.setLast_modified(now);
                resource.setCreated(now);
                ResourceHelpers.setResource(rdfOutput, outputSymbolicName, resource);
                VirtualGraphHelpers.setVirtualGraph(rdfOutput, outputSymbolicName, globalOutGraphURI.stringValue());
            }
        } catch (DataUnitException ex) {
            throw ContextUtils.dpuException(ctx, ex, "RdfToVirtuoso.executeInner.exception");
        } finally {
            if (executor != null) {
                try {
                    if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                        executor.shutdownNow(); // Cancel currently executing tasks
                        // Wait a while for tasks to respond to being cancelled
                        if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                            LOG.warn("Pool did not terminate");
                        }
                    }
                } catch (InterruptedException ex) {
                    LOG.warn("Pool did not terminate", ex);
                }
            }
            inVirtualGraphHelper.close();
            if (virtuosoRepository != null) {
                try {
                    virtuosoRepository.shutDown();
                } catch (RepositoryException ex) {
                    LOG.warn("Error shutdown repository", ex);
                }
            }
        }
    }

    @Override
    protected void innerExecute() throws DPUException {
        this.outerExecute(ctx, config);
    }

    static class Status {
        Set<Work> skippedEntries = new HashSet<>();
    }

    static class Work {
        RDFDataUnit.Entry inEntry;

        URI outDataGraphURI;

        boolean clearGraphBeforeLoad;

        boolean perGraphMode;
    }

    static class LoadWorker implements Callable<Status> {
        final Logger LOG;

        ConcurrentLinkedQueue<Work> workQueue;

        AtomicInteger done;

        RDFDataUnit rdfInput;

        WritableRDFDataUnit rdfOutput;

        Repository virtuosoRepository;

        boolean skipOnError;

        public LoadWorker(String loggerName, ConcurrentLinkedQueue<Work> workQueue, AtomicInteger done,
                RDFDataUnit rdfInput, WritableRDFDataUnit rdfOutput, Repository virtuosoRepository, boolean skipOnError) {
            this.LOG = LoggerFactory.getLogger(loggerName);
            this.workQueue = workQueue;
            this.done = done;
            this.rdfInput = rdfInput;
            this.rdfOutput = rdfOutput;
            this.virtuosoRepository = virtuosoRepository;
            this.skipOnError = skipOnError;
        }

        @Override
        public Status call() throws InterruptedException, Exception {
            Work work = null;
            Status status = new RdfToVirtuoso.Status();
            RepositoryConnection inConnection = null;
            RepositoryConnection externalConnection = null;
            ResourceHelper outResourceHelper = ResourceHelpers.create(rdfOutput);
            CopyHelper copyHelper = CopyHelpers.create(rdfInput, rdfOutput);
            VirtualGraphHelper outVirtualGraphHelper = VirtualGraphHelpers.create(rdfOutput);
            try {
                inConnection = rdfInput.getConnection();
                externalConnection = virtuosoRepository.getConnection();
                while ((work = workQueue.poll()) != null) {
                    try {
                        if (Thread.interrupted()) {
                            throw new InterruptedException();
                        }
                        if (work.clearGraphBeforeLoad && work.perGraphMode) {
                            Update update = externalConnection.prepareUpdate(QueryLanguage.SPARQL, String.format(RdfToVirtuoso.CLEAR_QUERY, work.outDataGraphURI.stringValue()));
                            update.execute();
                        }

                        RepositoryResult<Statement> result = inConnection.getStatements(null, null, null, false, work.inEntry.getDataGraphURI());
                        while (result.hasNext()) {
                            externalConnection.add(result, work.outDataGraphURI);
                        }
                        if (work.perGraphMode) {
                            copyHelper.copyMetadata(work.inEntry.getSymbolicName());
                            Resource resource = outResourceHelper.getResource(work.inEntry.getSymbolicName());
                            Date now = new Date();
                            resource.setLast_modified(now);
                            outResourceHelper.setResource(work.inEntry.getSymbolicName(), resource);
                            outVirtualGraphHelper.setVirtualGraph(work.inEntry.getSymbolicName(), work.outDataGraphURI.stringValue());
                        }
                        done.incrementAndGet();
                    } catch (RepositoryException | DataUnitException | UpdateExecutionException | MalformedQueryException ex) {
                        if (skipOnError) {
                            LOG.warn("Skipping graph: '{}' because of error.", work.inEntry.toString(), ex);
                            status.skippedEntries.add(work);
                        } else {
                            throw ex;
                        }
                    }
                }
            } catch (DataUnitException | RepositoryException ex) {
                throw ex;
            } finally {
                outVirtualGraphHelper.close();
                outResourceHelper.close();
                copyHelper.close();
                if (externalConnection != null) {
                    try {
                        externalConnection.close();
                    } catch (RepositoryException ex) {
                        LOG.warn("Error closing repository connection", ex);
                    }
                }
                if (inConnection != null) {
                    try {
                        inConnection.close();
                    } catch (RepositoryException ex) {
                        LOG.warn("Error closing repository connection", ex);
                    }
                }
            }
            return status;
        }
    }
}
