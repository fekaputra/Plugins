package eu.unifiedviews.plugins.transformer.tabulartorelational;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.relational.WritableRelationalDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.fileshelper.FilesHelper;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import eu.unifiedviews.helpers.dpu.config.ConfigDialogProvider;
import eu.unifiedviews.helpers.dpu.config.ConfigurableBase;
import eu.unifiedviews.helpers.dpu.localization.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;

@DPU.AsTransformer
public class TabularToRelational extends ConfigurableBase<TabularToRelationalConfig_V1> implements ConfigDialogProvider<TabularToRelationalConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(TabularToRelational.class);

    @DataUnit.AsInput(name = "input")
    public FilesDataUnit inFilesData;

    @DataUnit.AsOutput(name = "output")
    public WritableRelationalDataUnit outRelationalData;

    private Messages messages;

    public TabularToRelational() {
        super(TabularToRelationalConfig_V1.class);
    }

    @Override
    public void execute(DPUContext context) throws DPUException, InterruptedException {
        this.messages = new Messages(context.getLocale(), this.getClass().getClassLoader());
        final Iterator<FilesDataUnit.Entry> filesIteration;
        try {
            filesIteration = FilesHelper.getFiles(inFilesData).iterator();
        } catch (DataUnitException ex) {
            context.sendMessage(DPUContext.MessageType.ERROR, messages.getString("errors.dpu.failed"), messages.getString("errors.file.iterator"), ex);
            return;
        }

        try {
            String createTableQuery = prepareCreateTableQuery();
            LOG.debug("Table create query: {}", createTableQuery);
            context.sendMessage(DPUContext.MessageType.INFO, "Creating new table", createTableQuery);
            // create table from user config
            executeQuery(createTableQuery);

            String tableName = processString(config.getTableName());
            this.outRelationalData.addExistingDatabaseTable(tableName, tableName);

            while (!context.canceled() && filesIteration.hasNext()) {
                final FilesDataUnit.Entry entry = filesIteration.next();
                LOG.debug("Adding file: {}", entry.getSymbolicName());
                String csvPath = entry.getFileURIString();
                // remove file prefix
                csvPath = csvPath.replaceFirst("file:/", "");

                String insertIntoQuery = prepareInsertIntoQuery(csvPath);
                LOG.debug("Insert into query: {}", insertIntoQuery);
                context.sendMessage(DPUContext.MessageType.INFO, String.format("Inserting file %s into table", entry.getSymbolicName()), insertIntoQuery);
                executeQuery(insertIntoQuery);
            }
        } catch (DataUnitException e) {
            context.sendMessage(DPUContext.MessageType.ERROR, messages.getString("errors.dpu.parse.failed"), "", e);
        }

        context.sendMessage(DPUContext.MessageType.INFO, "Parsing tabular data into relational database have been completed.", null);
    }

    private void executeQuery(String query) throws DataUnitException {
        Statement stmnt = null;
        Connection conn = null;
        try {
            conn = outRelationalData.getDatabaseConnection();
            stmnt = conn.createStatement();
            stmnt.executeUpdate(query);
            conn.commit();
        } catch (SQLException e) {
            LOG.error("Failed to create internal db table", e);
            DatabaseHelper.tryRollbackConnection(conn);
            throw new DataUnitException("Error creating internal database table");
        } finally {
            DatabaseHelper.tryCloseStatement(stmnt);
            DatabaseHelper.tryCloseConnection(conn);
        }
    }

    private String prepareInsertIntoQuery(String csvPath) {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");

        // add tablename
        String tableName = processString(config.getTableName());
        sb.append(tableName);

        sb.append("(SELECT ");
        // add maximum row limit
        sb.append("TOP ");
        sb.append(config.getRowsLimit());
        sb.append(" ");
        // columns
        sb.append(joinColumnNames(config.getColumnMapping(), ", "));

        sb.append(" FROM CSVREAD(");

        // add filename
        sb.append("'");
        sb.append(csvPath);
        sb.append("'");
        // column mapping
        sb.append(", '");
        sb.append(joinColumnNames(config.getColumnMapping(), config.getFieldSeparator()));
        sb.append("'");
        // add csv options
        sb.append(", ");
        sb.append(processCsvOptions(config));

        sb.append("))");

        return sb.toString();
    }

    private String prepareCreateTableQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ");

        // add tablename
        String tableName = processString(config.getTableName());
        sb.append(tableName);

        sb.append("(");
        StringBuilder primaryKeys  = new StringBuilder("PRIMARY KEY (");
        for(ColumnMappingEntry entry : config.getColumnMapping()) {
            // add column name
            sb.append(processString(entry.getColumnName()));

            sb.append(" ");
            // add data type
            sb.append(processString(entry.getDataType()));
            sb.append(", ");
            if(entry.isPrimaryKey()) {
                primaryKeys.append(processString(entry.getColumnName()));
                primaryKeys.append(", ");
            }
        }
        // remove last ", "
        if(primaryKeys.length() > 1) {
            primaryKeys.setLength(primaryKeys.length() - 2);
        }
        primaryKeys.append(")");
        sb.append(" ");
        sb.append(primaryKeys);
        sb.append(")");

        return sb.toString();
    }

    @Override
    public AbstractConfigDialog<TabularToRelationalConfig_V1> getConfigurationDialog() {
        return new TabularToRelationalVaadinDialog();
    }

    public static String processString(String input) {
        String output = (input == null) ? "" : input;
        output = output.trim();
        output = output.toUpperCase();
        return output;
    }

    public static String joinColumnNames(List<ColumnMappingEntry> list, String joiner){
        StringBuilder sb = new StringBuilder();
        for(int i=0; i < list.size(); i++) {
            if(i > 0) {
                sb.append(joiner);
            }
            sb.append(processString(list.get(i).getColumnName()));
        }
        return sb.toString();
    }

    public static String processCsvOptions(TabularToRelationalConfig_V1 config) {
        StringBuilder sb = new StringBuilder();
        sb.append("'");

        if(isNotEmpty(config.getEncoding())) {
            sb.append("charset=");
            sb.append(config.getEncoding());
            sb.append(" ");
        }

        if(isNotEmpty(config.getFieldDelimiter())) {
            sb.append("fieldDelimiter=");
            sb.append(config.getFieldDelimiter());
            sb.append(" ");
        }

        if(isNotEmpty(config.getFieldSeparator())) {
            sb.append("fieldSeparator=");
            sb.append(config.getFieldSeparator());
            sb.append(" ");
        }

        if(sb.charAt(sb.length() - 1) == ' ') { // if last char is space, remove it
            sb.setLength(sb.length() - 1);
        }
        sb.append("'");
        return sb.toString();
    }
}
