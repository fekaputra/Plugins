package eu.unifiedviews.plugins.extractor.relationalfromsql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.relational.WritableRelationalDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.resource.Resource;
import eu.unifiedviews.helpers.dataunit.resource.ResourceHelpers;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.config.migration.ConfigurationUpdate;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;

/**
 * {@link RelationalFromSql} extracts data from external source relational database (currently PostgreSQL supported)
 * and stores the data in the internal database table so other relational DPU can access them.
 * In current implementation, user typed SQL queries are used to extract data
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
@DPU.AsExtractor
public class RelationalFromSql extends AbstractDpu<RelationalFromSqlConfig_V2> {

    private static final Logger LOG = LoggerFactory.getLogger(RelationalFromSql.class);

    private DPUContext context;

    @DataUnit.AsOutput(name = "outputTables")
    public WritableRelationalDataUnit outputTables;

    @ExtensionInitializer.Init(param = "eu.unifiedviews.plugins.extractor.relationalfromsql.RelationalFromSqlConfig__V2")
    public ConfigurationUpdate _ConfigurationUpdate;

    public RelationalFromSql() {
        super(RelationalFromSqlVaadinDialog.class, ConfigHistory.noHistory(RelationalFromSqlConfig_V2.class));
    }

    @Override
    protected void innerExecute() throws DPUException {
        String shortMessage = this.getClass().getSimpleName() + " starting.";
        String longMessage = String.format("Configuration: DatabaseHost: %s, username: %s, password: %s, "
                + "useSSL: %s, SQL query: %s",
                this.config.getDatabaseHost(), this.config.getUserName(), "***",
                this.config.isUseSSL(), this.config.getSqlQuery());
        LOG.info(shortMessage + " " + longMessage);

        try {
            Class.forName(SqlDatabase.getDatabaseInfo(this.config.getDatabaseType()).getJdbcDriverName());
        } catch (ClassNotFoundException e) {
            throw ContextUtils.dpuException(ctx, e, "errors.driver.loadfailed");
        }

        Connection conn = null;
        Statement stmnt = null;
        ResultSet rs = null;
        ResultSetMetaData meta = null;

        try {
            String tableName = this.config.getTargetTableName().toUpperCase();
            final String symbolicName = tableName;

            try {
                if (checkInternalTableExists(tableName)) {
                    this.context.sendMessage(DPUContext.MessageType.ERROR, ctx.tr("errors.db.tableunique.short", tableName),
                            ctx.tr("errors.db.tableunique.long"));
                    return;
                }
            } catch (SQLException | DataUnitException e) {
                throw ContextUtils.dpuException(ctx, "errors.db.internaltable");
            }

            try {
                LOG.debug("Connecting to the source database");
                conn = RelationalFromSqlHelper.createConnection(this.config);
                LOG.debug("Connected to the source database");
            } catch (SQLException e) {
                throw ContextUtils.dpuException(ctx, e, "errors.db.connectionfailed");
            }

            try {
                stmnt = conn.createStatement();
                LOG.info("Executing query " + this.config.getSqlQuery());
                rs = stmnt.executeQuery(this.config.getSqlQuery());
                meta = rs.getMetaData();
            } catch (SQLException e) {
                throw ContextUtils.dpuException(ctx, e, "errors.db.queryfailed");
            }

            try {
                List<ColumnDefinition> tableColumns = RelationalFromSqlHelper.getTableColumnsFromMetaData(meta);
                String createTableQuery = QueryBuilder.getCreateTableQueryFromMetaData(tableColumns, tableName);

                LOG.debug("Creating internal db representation as " + createTableQuery);
                executeSqlQueryInInternalDatabase(createTableQuery);
                LOG.info("Database table in internal database successfully created");

                // For now, symbolic name and real table name are the same - user inserted
                this.outputTables.addExistingDatabaseTable(symbolicName, tableName);
                LOG.info("New database table {} added to relational data unit", tableName);

                LOG.debug("Inserting data from source table into internal table");
                insertDataFromSelect(tableColumns, rs, tableName);
                LOG.info("Inserting data from source table into internal table successful");

                if (this.config.getPrimaryKeyColumns() == null || this.config.getPrimaryKeyColumns().isEmpty()) {
                    LOG.info("Primary keys: No primary keys defined for table");
                } else {
                    LOG.info("Primary keys: Going to add primary keys to the output database table");
                    String alterQuery = QueryBuilder.getPrimaryKeysQuery(tableName, this.config.getPrimaryKeyColumns());
                    executeSqlQueryInInternalDatabase(alterQuery);
                    LOG.info("Primary keys: Successfully added to the output table");
                }
                if (this.config.getIndexedColumns() == null || this.config.getIndexedColumns().isEmpty()) {
                    LOG.info("No indexed columns defined for target table, nothing to do");
                } else {
                    LOG.info("Going to create indexes in internal database table");
                    for (String indexColumn : this.config.getIndexedColumns()) {
                        String indexQuery = QueryBuilder.getCreateIndexQuery(tableName, indexColumn);
                        executeSqlQueryInInternalDatabase(indexQuery);
                    }
                    LOG.info("Indexes created successfully");
                }

                Resource resource = ResourceHelpers.getResource(outputTables, symbolicName);
                Date now = new Date();
                resource.setCreated(now);
                resource.setLast_modified(now);
                ResourceHelpers.setResource(outputTables, symbolicName, resource);

                LOG.debug("Resource parameters for table updated");
            } catch (SQLTransformException e) {
                switch (e.getErrorCode()) {
                    case DUPLICATE_COLUMN_NAME:
                        ContextUtils.sendError(ctx, "errors.db.duplicate.column.short", e, "errors.db.duplicate.column.long");
                        return;
                    default:
                        throw ContextUtils.dpuException(ctx, e, "errors.db.transformfailed");
                }
            } catch (Exception e) {
                throw ContextUtils.dpuException(ctx, e, "errors.db.transformfailed");
            }
        } finally {
            RelationalFromSqlHelper.tryCloseDbResources(conn, stmnt, rs);
        }
    }

    private void executeSqlQueryInInternalDatabase(String query) throws DataUnitException {
        Statement stmnt = null;
        Connection conn = null;
        try {
            conn = getConnectionInternal();
            stmnt = conn.createStatement();
            stmnt.executeUpdate(query);
            conn.commit();
        } catch (SQLException e) {
            RelationalFromSqlHelper.tryRollbackConnection(conn);
            throw new DataUnitException("Error executing statement in internal dataunit database", e);
        } finally {
            RelationalFromSqlHelper.tryCloseStatement(stmnt);
            RelationalFromSqlHelper.tryCloseConnection(conn);
        }
    }

    private void insertDataFromSelect(List<ColumnDefinition> tableColumns, ResultSet rs, String tableName) throws DataUnitException {
        PreparedStatement ps = null;
        Connection conn = null;
        try {
            conn = getConnectionInternal();
            String insertQuery = QueryBuilder.getInsertQueryForPreparedStatement(tableColumns, tableName);
            LOG.debug("Insert query for inserting into internal DB table: {}", insertQuery);
            ps = conn.prepareStatement(insertQuery);
            LOG.debug("Prepared statement for inserting data created");
            while (rs.next()) {
                fillInsertData(ps, tableColumns, rs);
                ps.execute();
                conn.commit();
            }
        } catch (SQLException e) {
            LOG.error("Failed to load data into internal table", e);
            RelationalFromSqlHelper.tryRollbackConnection(conn);
            throw new DataUnitException("Error loading SQL data into internal database");
        } finally {
            RelationalFromSqlHelper.tryCloseStatement(ps);
            RelationalFromSqlHelper.tryCloseConnection(conn);
        }
    }

    private boolean checkInternalTableExists(String targetTableName) throws SQLException, DataUnitException {
        Connection conn = null;
        boolean bTableExists = false;
        DatabaseMetaData dbm = null;
        ResultSet tables = null;
        try {
            conn = getConnectionInternal();
            dbm = conn.getMetaData();
            tables = dbm.getTables(null, null, targetTableName, null);
            if (tables.next()) {
                bTableExists = true;
            }
        } finally {
            RelationalFromSqlHelper.tryCloseResultSet(tables);
            RelationalFromSqlHelper.tryCloseConnection(conn);
        }
        return bTableExists;
    }

    private static void fillInsertData(PreparedStatement ps, List<ColumnDefinition> columns, ResultSet rs) throws SQLException,
            DataUnitException {
        int index = 1;
        for (ColumnDefinition column : columns) {
            Object sourceValue = rs.getObject(column.getColumnName());
            ps.setObject(index, sourceValue);
            index++;
        }
    }

    private Connection getConnectionInternal() throws DataUnitException {
        return this.outputTables.getDatabaseConnection();
    }

}
