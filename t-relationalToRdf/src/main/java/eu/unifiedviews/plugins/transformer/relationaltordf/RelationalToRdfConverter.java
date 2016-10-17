package eu.unifiedviews.plugins.transformer.relationaltordf;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.relational.RelationalDataUnit;
import eu.unifiedviews.helpers.dpu.exec.UserExecContext;
import eu.unifiedviews.plugins.transformer.relationaltordf.mapper.TableToRdf;
import eu.unifiedviews.plugins.transformer.relationaltordf.mapper.TableToRdfConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RelationalToRdfConverter {

    private static final Logger LOG = LoggerFactory.getLogger(RelationalToRdfConverter.class);

    private final TableToRdf tableToRdf;

    private final UserExecContext context;

    private int rowNumber = 1;

    public RelationalToRdfConverter(TableToRdf tableToRdf, UserExecContext context) {
        this.tableToRdf = tableToRdf;
        this.context = context;
    }

    public void convertTable(RelationalDataUnit.Entry sqlTable, Connection conn) throws ConversionFailed {
        List<String> header = null;
        ResultSet rs = null;
        Statement stmnt = null;
        String tableName = null;
        try {
            tableName = sqlTable.getTableName();
            header = getHeaderForTable(sqlTable, conn);
            stmnt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = stmnt.executeQuery("SELECT * FROM " + tableName);

            int rowNumPerFile = 0;
            List<Object> dataRow = null;
            if (rs.next()) {
                dataRow = getRow(rs, header);
                rs.beforeFirst();
            }
            if (dataRow == null) {
                LOG.warn("No data found");
                return;
            }

            TableToRdfConfigurator.configure(this.tableToRdf, header, dataRow, 0);
            while (rs.next() && (dataRow=getRow(rs, header)) != null && !this.context.canceled()) {
                this.tableToRdf.paserRow(dataRow, this.rowNumber);
                this.rowNumber++;
                rowNumPerFile++;

                if ((rowNumPerFile % 1000) == 0) {
                    LOG.debug("Row number {} processed.", rowNumPerFile);
                }
            }
        } catch (Exception e) {
            LOG.error("Conversion of database table to RDF failed", e);
            throw new ConversionFailed("Conversion of database table to RDF failed", e);
        } finally {
            Utils.tryCloseDbResources(stmnt, rs);
        }
    }

    private static List<String> getHeaderForTable(RelationalDataUnit.Entry sqlTable, Connection conn) throws DataUnitException {
        ResultSet columns = null;
        List<String> columnsList = new ArrayList<>();
        String tableName = null;
        try {
            tableName = sqlTable.getTableName();
            DatabaseMetaData meta = conn.getMetaData();
            columns = meta.getColumns(null, null, tableName, null);
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                columnsList.add(columnName.toLowerCase());
            }
        } catch (Exception e) {
            LOG.error("Error occurred during obtaining column names from database table", e);
            throw new DataUnitException("Failed to obtain column names from database");
        } finally {
            try {
                if (columns != null) {
                    columns.close();
                }
            } catch (SQLException e) {
                LOG.warn("Failed to close result set", e);
            }
        }

        return columnsList;
    }

    private static List<Object> getRow(ResultSet rs, List<String> header) throws DataUnitException {
        List<Object> row = new ArrayList<>();
        try {
            for (String column : header) {
                row.add(rs.getObject(column));
            }
        } catch (SQLException e) {
            LOG.error("Failed to obtain row", e);
            throw new DataUnitException("Failed to obtain row");
        }

        return row;
    }

}
