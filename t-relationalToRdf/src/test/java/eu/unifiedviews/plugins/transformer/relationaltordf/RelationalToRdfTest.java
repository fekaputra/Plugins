package eu.unifiedviews.plugins.transformer.relationaltordf;

import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dataunit.relational.WritableRelationalDataUnit;
import eu.unifiedviews.helpers.dpu.test.config.ConfigurationBuilder;
import eu.unifiedviews.plugins.transformer.relationaltordf.column.ColumnInfo_V1;
import eu.unifiedviews.plugins.transformer.relationaltordf.column.ColumnType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.URI;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class RelationalToRdfTest {

    private static final String TABLE_NAME_USERS = "USER_DATA";

    private static final String[] TABLE_USER_COLUMNS = new String[] { "id", "name", "surname", "age" };

    private static final String[] TABLE_USER_COLUMN_TYPES = new String[] { "INTEGER", "VARCHAR(255)", "VARCHAR(255)", "INTEGER" };

    private static TestEnvironment testEnv;

    private static WritableRelationalDataUnit relationalInput;

    private static WritableRDFDataUnit rdfOutput;

    private static RelationalToRdf dpu;

    private static Connection dataUnitConn;

    @BeforeClass
    public static void before() throws Exception {
        dpu = new RelationalToRdf();
        testEnv = new TestEnvironment();

        relationalInput = testEnv.createRelationalInput("tablesInput");
        rdfOutput = testEnv.createRdfOutput("rdfOutput", false);

        dataUnitConn = relationalInput.getDatabaseConnection();
        createAndFillUsersSourceTable(dataUnitConn);
        relationalInput.addExistingDatabaseTable(TABLE_NAME_USERS,TABLE_NAME_USERS);
    }

    @AfterClass
    public static void after() {
        try {
            if (dataUnitConn != null) {
                dataUnitConn.close();
            }
        } catch (SQLException e) {
            // ignore
        }
        testEnv.release();
    }

    @Test
    public void convertTableGenerateAllColumns() throws Exception {
        RelationalToRdfConfig_V1 config = new RelationalToRdfConfig_V1();
        config.setGenerateNew(true);
        this.dpu.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());

        this.testEnv.run(this.dpu);

        final Set<URI> graphsURIs = this.rdfOutput.getMetadataGraphnames();
        assertEquals(1, graphsURIs.size());
    }

    @Test
    public void convertTableGenerateCustomColumns() throws Exception {
        RelationalToRdfConfig_V1 config = new RelationalToRdfConfig_V1();
        config.setGenerateNew(false);

        ColumnInfo_V1 nameColumn = new ColumnInfo_V1("http://relationalToRdfTest/users/name", ColumnType.String);
        ColumnInfo_V1 surnameColumn = new ColumnInfo_V1("http://relationalToRdfTest/users/surname", ColumnType.Auto);
        config.getColumnsInfo().put("name", nameColumn);
        config.getColumnsInfo().put("surname", surnameColumn);

        this.dpu.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());

        this.testEnv.run(this.dpu);

        final Set<URI> graphsURIs = this.rdfOutput.getMetadataGraphnames();
        assertEquals(1, graphsURIs.size());

    }

    private static void createAndFillUsersSourceTable(Connection conn) throws SQLException {
        createTable(conn, TABLE_NAME_USERS, TABLE_USER_COLUMNS, TABLE_USER_COLUMN_TYPES);
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement("INSERT INTO " + TABLE_NAME_USERS + " (id, name, surname, age) VALUES (?, ?, ?, ?)");

            ps.setInt(1, 1);
            ps.setString(2, "Tom");
            ps.setString(3, "Black");
            ps.setInt(4, 25);
            ps.execute();

            ps.setInt(1, 2);
            ps.setString(2, "Jim");
            ps.setString(3, "Carey");
            ps.setInt(4, 33);
            ps.execute();

            conn.commit();

        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ignore) {
                // ignore exception
            }
        }
    }

    private static final void createTable(Connection conn, String tableName, String[] columnNames, String[] columnTypes)
            throws SQLException {
        StringBuilder query = new StringBuilder("CREATE TABLE ");
        query.append(tableName);
        query.append(" (");
        for (int i = 0; i < columnNames.length; i++) {
            query.append(columnNames[i]);
            query.append(" ");
            query.append(columnTypes[i]);
            query.append(", ");
        }
        query.setLength(query.length() - 2);
        query.append(")");

        Statement stmnt = null;
        try {
            stmnt = conn.createStatement();
            stmnt.execute(query.toString());
            conn.commit();
        } finally {
            try {
                if (stmnt != null) {
                    stmnt.close();
                }
            } catch (SQLException ignore) {
                // ignore exception
            }
        }
    }

}
