package eu.unifiedviews.plugins.transformer.relational;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.relational.RelationalDataUnit;
import eu.unifiedviews.dataunit.relational.RelationalDataUnit.Entry;
import eu.unifiedviews.dataunit.relational.WritableRelationalDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.relational.RelationalHelper;
import eu.unifiedviews.helpers.dataunit.resource.Resource;
import eu.unifiedviews.helpers.dataunit.resource.ResourceHelpers;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.config.migration.ConfigurationUpdate;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;

/**
 * {@link Relational} transforms N input internal database tables into 1 output table using user typed SELECT SQL queries.
 * Output table is also stored in internal database to be accessible by other relational DPUs
 * <p/>
 * <b>WARNING:</b>This DPU is a part of optional UV relational functionality and relational DPUs currently do not fully follow UV philosophy as the user has
 * control of physical database table names
 * <p/>
 * The general philosophy of UV so far is, that DPU developer cannot influence the physical location of the internal files, graphs -- UV manages its internal
 * stores. As a result, such approach should be similar in relational data unit and DPU developer should NOT be able to set the target table name (currently,
 * there is a configuration option for extractor and transformer, which allows to set up the target table name). Target table name is ok for loader, but not for
 * extractor/transformer, where the target table is given by the data flow in the pipeline.
 * <p/>
 * Current implementation is a compromise to be able to provide a general, SQL query based transformer for users. If user did not know the real table name, he
 * would not be able to write SQL queries. Not without complex parsing of SQL queries. Further discussion is needed to solve this issue and this will be aim of
 * future releases.
 */
@DPU.AsTransformer
public class Relational extends AbstractDpu<RelationalConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(Relational.class);

    private static final String DB_USER_BASE_NAME = "tRelational_";

    private static final String DB_PASSWORD = "dummy123";

    @DataUnit.AsInput(name = "inputTables")
    public RelationalDataUnit inputTables;

    @DataUnit.AsOutput(name = "outputTable")
    public WritableRelationalDataUnit outputTable;

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    @ExtensionInitializer.Init(param = "eu.unifiedviews.plugins.transformer.relational.RelationalConfig__V1")
    public ConfigurationUpdate _ConfigurationUpdate;

    private DPUContext dpuContext;

    public Relational() {
        super(RelationalVaadinDialog.class, ConfigHistory.noHistory(RelationalConfig_V1.class));
    }

    @Override
    protected void innerExecute() throws DPUException {
        this.dpuContext = this.ctx.getExecMasterContext().getDpuContext();
        String targetTableName = this.config.getTargetTableName().toUpperCase();
        final String symbolicName = targetTableName;
        Set<RelationalDataUnit.Entry> tables = null;

        try {
            tables = RelationalHelper.getTables(this.inputTables);
        } catch (DataUnitException ex) {
            ContextUtils.sendError(this.ctx, "errors.dpu.failed", ex, "errors.tables.iterator");
            return;
        }

        if (tables.isEmpty()) {
            throw ContextUtils.dpuException(this.ctx, "errors.tables.input");
        }

        Connection conn = null;
        Statement stmnt = null;
        ResultSet rs = null;
        ResultSetMetaData meta = null;
        String dbUserName = DB_USER_BASE_NAME + this.dpuContext.getDpuInstanceId();
        try {
            if (checkTableExists(targetTableName)) {
                throw ContextUtils.dpuException(this.ctx, "errors.db.tableunique.long");
            }

            LOG.debug("Going to create database user {} who will execute SELECT queries in internal DB", dbUserName);
            executeSqlQueryInInternalDatabaseAsAdmin(DatabaseHelper.createDropUserQuery(dbUserName),inputTables);
            executeSqlQueryInInternalDatabaseAsAdmin(DatabaseHelper.createDatabaseUserQuery(dbUserName, DB_PASSWORD),inputTables);
            LOG.debug("Database user {} successfully created", dbUserName);
            grantSelectOnAllInputTables(tables, dbUserName, inputTables);

            LOG.debug("Going to create SQL connection to internal database for user {}", dbUserName);
            conn = this.inputTables.getDatabaseConnectionForUser(dbUserName, DB_PASSWORD);
            LOG.debug("SQL connection to internal database for user {} successfully created", dbUserName);
            LOG.debug("Executing SQL query in internal database");
            stmnt = conn.createStatement();

            try {
                LOG.info("Executing query " + this.config.getSqlQuery());
                rs = stmnt.executeQuery(this.config.getSqlQuery());
                meta = rs.getMetaData();
                LOG.debug("SQL query executed successfully");
            } catch (SQLException e) {
                ContextUtils.sendError(this.ctx, "errors.db.query", "");
                throw e;
            }

            List<ColumnDefinition> tableColumns = DatabaseHelper.getTableColumnsFromMetaData(meta);
            String createTableQuery = DatabaseHelper.getCreateTableQueryFromMetaData(tableColumns, targetTableName);

            LOG.debug("Creating internal db representation as " + createTableQuery);
            executeSqlQueryInInternalDatabaseAsAdmin(createTableQuery, outputTable);
            LOG.debug("Database table in internal database successfully created");

            // For now, symbolic name and real table name are the same - user inserted
            this.outputTable.addExistingDatabaseTable(symbolicName, targetTableName);
            LOG.debug("New database table {} added to relational data unit", targetTableName);

            LOG.debug("Inserting data from source table into internal table");
            insertDataFromSelect(tableColumns, rs, targetTableName);
            LOG.debug("Inserting data from source table into internal table successful");

            // Create primary keys
            if (this.config.getPrimaryKeyColumns() == null || this.config.getPrimaryKeyColumns().isEmpty()) {
                LOG.debug("No primary keys defined, nothing to do");
            } else {
                LOG.debug("Going to create primary keys for table {}", targetTableName);
                for (String primaryKey : this.config.getPrimaryKeyColumns()) {
                    executeSqlQueryInInternalDatabaseAsAdmin(DatabaseHelper.createAlterColumnSetNotNullQuery(targetTableName, primaryKey), outputTable);
                }
                String alterTablesQuery = DatabaseHelper.createPrimaryKeysQuery(targetTableName, this.config.getPrimaryKeyColumns());
                executeSqlQueryInInternalDatabaseAsAdmin(alterTablesQuery, outputTable);
            }

            // Create indexes
            if (this.config.getIndexedColumns() == null || this.config.getIndexedColumns().isEmpty()) {
                LOG.debug("No indexed columns defined for target table, nothing to do");
            } else {
                LOG.debug("Going to create indexes for table {}", targetTableName);
                for (String indexedColumn : this.config.getIndexedColumns()) {
                    String indexQuery = DatabaseHelper.getCreateIndexQuery(targetTableName, indexedColumn);
                    executeSqlQueryInInternalDatabaseAsAdmin(indexQuery, outputTable);
                }
            }
            this.faultTolerance.execute(new FaultTolerance.Action() {

                @Override
                public void action() throws Exception {
                    Resource resource = ResourceHelpers.getResource(Relational.this.outputTable, symbolicName);
                    Date now = new Date();
                    resource.setCreated(now);
                    resource.setLast_modified(now);
                    ResourceHelpers.setResource(Relational.this.outputTable, symbolicName, resource);
                }
            });
            LOG.debug("Resource parameters for table updated");

        } catch (SQLTransformException e) {
            switch (e.getErrorCode()) {
                case DUPLICATE_COLUMN_NAME:
                    ContextUtils.sendError(ctx, "errors.db.duplicate.column.short", e, "errors.db.duplicate.column.long");
                    return;
                default:
                    throw ContextUtils.dpuException(ctx, e, "errors.db.transformfailed");
            }
        } catch (SQLException se) {
            LOG.error("Database error occured during transforming database tables", se);
            DatabaseHelper.tryRollbackConnection(conn);
            throw ContextUtils.dpuException(this.ctx, se, "errors.db.transformfailed");
        } catch (Exception e) {
            LOG.error("Error occured during transforming database tables", e);
            throw ContextUtils.dpuException(this.ctx, e, "errors.transformfailed");
        } finally {
            DatabaseHelper.tryCloseResultSet(rs);
            DatabaseHelper.tryCloseStatement(stmnt);
            DatabaseHelper.tryCloseConnection(conn);
            try {
                executeSqlQueryInInternalDatabaseAsAdmin(DatabaseHelper.createDropUserQuery(dbUserName), inputTables);
            } catch (DataUnitException e) {
                LOG.warn("Failed to drop DPU database user");
            }
        }
    }

    private void grantSelectOnAllInputTables(Set<Entry> tables, String dbUserName, RelationalDataUnit du) throws DataUnitException {
        LOG.debug("Going to GRANT SELECT on all input tables to user {}", dbUserName);
        for (RelationalDataUnit.Entry table : tables) {
            String grantSelectQuery = DatabaseHelper.createGrantSelectOnTableQuery(table.getTableName(), dbUserName);
            executeSqlQueryInInternalDatabaseAsAdmin(grantSelectQuery, du);
        }
        LOG.debug("GRANT SELECT on input tables successful");
    }

    private boolean checkTableExists(String tableName) throws DataUnitException {
        boolean result = false;
        Connection conn = null;
        try {
            conn = getOutputConnectionInternal();
            result = DatabaseHelper.checkTableExists(conn, tableName);
        } catch (SQLException e) {
            DatabaseHelper.tryRollbackConnection(conn);
            throw new DataUnitException("Error executing statement in internal dataunit database", e);
        } finally {
            DatabaseHelper.tryCloseConnection(conn);
        }

        return result;
    }

    private void insertDataFromSelect(List<ColumnDefinition> tableColumns, ResultSet rs, String tableName) throws SQLException, DataUnitException {
        PreparedStatement ps = null;
        Connection conn = null;
        try {
            conn = getOutputConnectionInternal();
            String insertQuery = DatabaseHelper.getInsertQueryForPreparedStatement(tableColumns, tableName);
            LOG.debug("Insert query for inserting into internal DB table: {}", insertQuery);
            ps = conn.prepareStatement(insertQuery);
            LOG.debug("Prepared statement for inserting data created");
            while (rs.next()) {
                fillInsertData(ps, tableColumns, rs);
                ps.execute();
            }
            conn.commit();
        } catch (SQLException | DataUnitException e) {
            LOG.error("Failed to load data into internal table", e);
            throw e;
        } finally {
            DatabaseHelper.tryCloseConnection(conn);
            DatabaseHelper.tryCloseStatement(ps);
        }
    }

    private void executeSqlQueryInInternalDatabaseAsAdmin(String query, RelationalDataUnit du) throws DataUnitException {
        Statement stmnt = null;
        Connection conn = null;
        try {
            conn = du.getDatabaseConnection();
            stmnt = conn.createStatement();
            stmnt.executeUpdate(query);
            conn.commit();
        } catch (SQLException e) {
            DatabaseHelper.tryRollbackConnection(conn);
            throw new DataUnitException("Error executing statement in internal dataunit database", e);
        } finally {
            DatabaseHelper.tryCloseStatement(stmnt);
            DatabaseHelper.tryCloseConnection(conn);
        }
    }

    private void fillInsertData(PreparedStatement ps, List<ColumnDefinition> columns, ResultSet rs) throws SQLException {
        int index = 1;
        for (ColumnDefinition column : columns) {
            Object sourceValue = rs.getObject(column.getColumnName());
            ps.setObject(index, sourceValue);
            index++;
        }
    }

    private Connection getOutputConnectionInternal() throws DataUnitException {
        return this.outputTable.getDatabaseConnection();
    }

}
