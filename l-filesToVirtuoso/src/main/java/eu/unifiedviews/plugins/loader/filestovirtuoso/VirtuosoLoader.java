package eu.unifiedviews.plugins.loader.filestovirtuoso;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.rdf4j.model.impl.URIImpl;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.UpdateExecutionException;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import virtuoso.sesame2.driver.VirtuosoRepository;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.resource.Resource;
import eu.unifiedviews.helpers.dataunit.resource.ResourceHelpers;
import eu.unifiedviews.helpers.dataunit.virtualgraph.VirtualGraphHelpers;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.config.migration.ConfigurationUpdate;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.extension.rdf.RdfConfiguration;

@DPU.AsLoader
public class VirtuosoLoader extends AbstractDpu<VirtuosoLoaderConfig_V2> {

    private static final Logger LOG = LoggerFactory.getLogger(VirtuosoLoader.class);

    public static final String PREDICATE_HAS_DISTRIBUTION = "http://comsode.eu/hasDistribution";

    @RdfConfiguration.ContainsConfiguration
    @DataUnit.AsInput(name = "config", optional = true)
    public RDFDataUnit rdfConfiguration;

    @DataUnit.AsOutput(name = "rdfOutput", optional = true)
    public WritableRDFDataUnit rdfOutput;

    private static final String LD_DIR = "ld_dir (?, ?, ?)";

    private static final String LD_DIR_ALL = "ld_dir_all (?, ?, ?)";

    private static final String NOW = "select now()";

    private static final String STOP = "rdf_load_stop()";

    private static final String STATUS_COUNT_DONE = "select count(*) from DB.DBA.load_list where ll_file like ? and ll_state = 2";

    private static final String STATUS_COUNT_PROCESSING = "select count(*) from DB.DBA.load_list where ll_file like ? and ll_state <> 2";

    private static final String STATUS_ERROR = "select * from DB.DBA.load_list where ll_file like ? and ll_error IS NOT NULL";

    private static final String DELETE = "delete from DB.DBA.load_list where ll_file like ?";

    private static final String CLEAR_QUERY = "DEFINE sql:log-enable 3 CLEAR GRAPH <%s>";

    private static final String RUN = "rdf_loader_run()";

    public static final String CONFIGURATION_VIRTUOSO_USERNAME = "dpu.l-filesToVirtuoso.username";

    public static final String CONFIGURATION_VIRTUOSO_PASSWORD = "dpu.l-filesToVirtuoso.password";

    public static final String CONFIGURATION_VIRTUOSO_JDBC_URL = "dpu.l-filesToVirtuoso.jdbc.url";

    public static final String CONFIGURATION_VIRTUOSO_LOAD_DIRECTORY_PATH = "dpu.l-filesToVirtuoso.load.directory.path";

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    @ExtensionInitializer.Init
    public RdfConfiguration _rdfConfiguration;

    @ExtensionInitializer.Init(param = "eu.unifiedviews.plugins.loader.filestovirtuoso.VirtuosoLoaderConfig__V2")
    public ConfigurationUpdate _ConfigurationUpdate;

    public VirtuosoLoader() {
        super(VirtuosoLoaderVaadinDialog.class, ConfigHistory.history(VirtuosoLoaderConfig_V1.class).addCurrent(VirtuosoLoaderConfig_V2.class));
    }

    @Override
    protected void innerExecute() throws DPUException {
        Map<String, String> environment = ctx.getExecMasterContext().getDpuContext().getEnvironment();
        String username = environment.get(CONFIGURATION_VIRTUOSO_USERNAME);
        if (config.getUsername() == null || config.getUsername().isEmpty()) {
            config.setUsername(username);
        }
        String password = environment.get(CONFIGURATION_VIRTUOSO_PASSWORD);
        if (config.getPassword() == null || config.getPassword().isEmpty()) {
            config.setPassword(password);
        }
        String virtuosoJdbcUrl = environment.get(CONFIGURATION_VIRTUOSO_JDBC_URL);
        if (config.getVirtuosoUrl() == null || config.getVirtuosoUrl().isEmpty()) {
            config.setVirtuosoUrl(virtuosoJdbcUrl);
        }
        String virtuosoLoadDirectoryPath = environment.get(CONFIGURATION_VIRTUOSO_LOAD_DIRECTORY_PATH);
        if (config.getLoadDirectoryPath() == null || config.getLoadDirectoryPath().isEmpty()) {
            config.setLoadDirectoryPath(virtuosoLoadDirectoryPath);
        }
        final String longMessage = String.format("Configuration: VirtuosoUrl: %s\nusername: %s\nloadDirectoryPath: %s\n"
                + "includeSubdirectories: %s\ntargetContext: %s\nstatusUpdateInterval: %s\nthreadCount: %s",
                config.getVirtuosoUrl(), config.getUsername(), config.getLoadDirectoryPath(),
                config.isIncludeSubdirectories(), config.getTargetContext(), config.getStatusUpdateInterval(),
                config.getThreadCount());
        ContextUtils.sendInfo(ctx, "Configuration", longMessage);
        try {
            Class.forName("virtuoso.jdbc4.Driver");
        } catch (ClassNotFoundException ex) {
            throw new DPUException("Error loading driver", ex);
        }

        VirtuosoRepository virtuosoRepository = null;
        RepositoryConnection repositoryConnection = null;
        try {
            virtuosoRepository = new VirtuosoRepository(config.getVirtuosoUrl(), config.getUsername(), config.getPassword());
            virtuosoRepository.initialize();
            repositoryConnection = virtuosoRepository.getConnection();
            if (config.isClearDestinationGraph()) {
                LOG.info("Clearing destination graph");
                Update update = repositoryConnection.prepareUpdate(QueryLanguage.SPARQL, String.format(CLEAR_QUERY, config.getTargetContext()));
                update.execute();
                LOG.info("Cleared destination graph");
            }
        } catch (MalformedQueryException | RepositoryException | UpdateExecutionException ex) {
            throw new DPUException("Error working with Virtuoso using Repository API", ex);
        } finally {
            if (repositoryConnection != null) {
                try {
                    repositoryConnection.close();
                } catch (RepositoryException ex) {
                    LOG.warn("Error closing repository connection", ex);
                }
            }
            if (virtuosoRepository != null) {
                try {
                    virtuosoRepository.shutDown();
                } catch (RepositoryException ex) {
                    LOG.warn("Error shutdown repository", ex);
                }
            }
        }

        Connection connection = null;
        boolean started = false;
        ExecutorService executor = null;
        RepositoryConnection outputMetadataConnection = null;
        try {
            connection = DriverManager.getConnection(config.getVirtuosoUrl(), config.getUsername(), config.getPassword());
            Statement statementNow = connection.createStatement();
            ResultSet resultSetNow = statementNow.executeQuery(NOW);
            resultSetNow.next();
            Timestamp startTimestamp = resultSetNow.getTimestamp(1);
            resultSetNow.close();
            statementNow.close();
            LOG.info("Start time {}", startTimestamp);

            PreparedStatement statementLdDir = connection.prepareStatement(config.isIncludeSubdirectories() ? LD_DIR_ALL : LD_DIR);
            statementLdDir.setString(1, config.getLoadDirectoryPath());
            statementLdDir.setString(2, config.getLoadFilePattern());
            statementLdDir.setString(3, config.getTargetContext());
            ResultSet resultSetLdDir = statementLdDir.executeQuery();
            resultSetLdDir.close();
            statementLdDir.close();
            LOG.info("Executed " + (config.isIncludeSubdirectories() ? "LD_DIR_ALL" : "LD_DIR"));

            PreparedStatement statementStatusCountProcessing = connection.prepareStatement(STATUS_COUNT_PROCESSING);
            PreparedStatement statementStatusCountDone = connection.prepareStatement(STATUS_COUNT_DONE);
            statementStatusCountProcessing.setString(1, config.getLoadDirectoryPath() + "%");
            statementStatusCountDone.setString(1, config.getLoadDirectoryPath() + "%");

            ResultSet resultSetProcessing = statementStatusCountProcessing.executeQuery();
            resultSetProcessing.next();
            int all = resultSetProcessing.getInt(1);
            LOG.info("Load list holds {} files to process", all);
            resultSetProcessing.close();
            if (all == 0) {
                LOG.info("Nothing to do. Stopping.");
                return;
            }

            executor = Executors.newFixedThreadPool(config.getThreadCount());
            for (int i = 0; i < config.getThreadCount(); i++) {
                executor.execute(new Runnable() {

                    @Override
                    public void run() {
                        Connection connection = null;
                        try {
                            connection = DriverManager.getConnection(config.getVirtuosoUrl(), config.getUsername(), config.getPassword());
                            Statement statementRun = connection.createStatement();
                            ResultSet resultSetRun = statementRun.executeQuery(RUN);
                            resultSetRun.close();
                            statementRun.close();
                        } catch (SQLException ex) {
                            LOG.error("Error in worker", ex);
                        } finally {
                            if (connection != null) {
                                try {
                                    connection.close();
                                } catch (SQLException ex) {
                                    LOG.warn("Error closing connection", ex);
                                }
                            }
                        }
                    }
                });
            }
            executor.shutdown();
            started = true;
            LOG.info("Started {} load threads", config.getThreadCount());

            int done = 0;
            boolean shouldContinue = !ctx.canceled();
            try {
                while ((shouldContinue) && (!executor.awaitTermination(config.getStatusUpdateInterval(), TimeUnit.SECONDS))) {
                    ResultSet resultSetDoneLoop = statementStatusCountDone.executeQuery();
                    resultSetDoneLoop.next();
                    done = resultSetDoneLoop.getInt(1);
                    resultSetDoneLoop.close();

                    LOG.info("Processing {}/{} files", done, all);
                    shouldContinue = !ctx.canceled();
                }
            } catch (InterruptedException ex) {
                throw ContextUtils.dpuExceptionCancelled(ctx);
            }
            LOG.info("Finished all threads");

            ResultSet resultSetDoneLoop = statementStatusCountDone.executeQuery();
            resultSetDoneLoop.next();
            done = resultSetDoneLoop.getInt(1);
            resultSetDoneLoop.close();
            LOG.info("Processed {}/{} files", done, all);

            PreparedStatement statementsErrorRows = connection.prepareStatement(STATUS_ERROR);
            statementsErrorRows.setString(1, config.getLoadDirectoryPath() + "%");
            ResultSet resultSetErrorRows = statementsErrorRows.executeQuery();
            while (resultSetErrorRows.next()) {
                if (config.isSkipOnError()) {
                    ContextUtils.sendWarn(ctx, "Error processing file: " + resultSetErrorRows.getString(1),
                            "Error: " + resultSetErrorRows.getString(8));
                } else {
                    ContextUtils.sendError(ctx, "Error processing file: " + resultSetErrorRows.getString(1),
                            "Error: " + resultSetErrorRows.getString(8));
                }
            }
            resultSetErrorRows.close();
            statementsErrorRows.close();

            final String outputSymbolicName = config.getTargetContext();

            rdfOutput.addExistingDataGraph(outputSymbolicName, new URIImpl(outputSymbolicName));
            faultTolerance.execute(new FaultTolerance.Action() {

                @Override
                public void action() throws Exception {
                    VirtualGraphHelpers.setVirtualGraph(rdfOutput, outputSymbolicName, config.getTargetContext());
                }
            });
            faultTolerance.execute(new FaultTolerance.Action() {

                @Override
                public void action() throws Exception {
                    final Resource resource = ResourceHelpers.getResource(rdfOutput, outputSymbolicName);
                    resource.setLast_modified(new Date());
                    ResourceHelpers.setResource(rdfOutput, outputSymbolicName, resource);
                }
            });

            LOG.info("Done.");
        } catch (DataUnitException | SQLException ex) {
            throw new DPUException("Error executing query", ex);
        } finally {
            LOG.info("User cancelled.");
            if (connection != null && started) {
                try {
                    Statement stop = connection.createStatement();
                    stop.executeQuery(STOP).close();
                    stop.close();
                } catch (SQLException ex1) {
                    LOG.error("Error executing query", ex1);
                }
            }
            if (executor != null && started) {
                try {
                    if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                        executor.shutdownNow(); // Cancel currently executing tasks
                        // Wait a while for tasks to respond to being cancelled
                        if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                            LOG.error("Pool did not terminate");
                        }
                    }
                } catch (InterruptedException ex) {
                    throw new DPUException("Interrupted.", ex);
                }
            }
            try {
                PreparedStatement delete = connection.prepareStatement(DELETE);
                delete.setString(1, config.getLoadDirectoryPath() + "%");
                delete.executeUpdate();
                delete.close();
                LOG.info("Deleted rows");
            } catch (SQLException ex) {
                LOG.error("Error deleting rows", ex);
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    LOG.warn("Error closing connection", ex);
                }
            }
            if (outputMetadataConnection != null) {
                try {
                    outputMetadataConnection.close();
                } catch (RepositoryException ex) {
                    LOG.warn("Error in close", ex);
                }
            }
        }
    }
}
