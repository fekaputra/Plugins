package eu.unifiedviews.plugins.loader.database.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.unifiedviews.plugins.loader.database.ColumnDefinition;
import eu.unifiedviews.plugins.loader.database.QueryBuilder;

public class QueryBuilderTest {

    private static final String[] FILE_HEADERS_1 = new String[] { "column1", "column2", "column3" };

    private static final ColumnDefinition COLUMN_1 = new ColumnDefinition("column1", "integer", 4);

    private static final ColumnDefinition COLUMN_2 = new ColumnDefinition("column2", "varchar", 12, 255);

    private static final ColumnDefinition COLUMN_3 = new ColumnDefinition("column3", "varchar", 12, 255);

    private static final String TABLE_NAME_1 = "uv_data_1";

    private static final String EXPECTED_INSERT_QUERY_1 = "INSERT INTO uv_data_1 (column1, column2, column3) VALUES (?,?,?)";

    private static final String EXPECTED_DROP_TABLE_QUERY_1 = "DROP TABLE " + TABLE_NAME_1;

    private static final String EXPECTED_TRUNCATE_TABLE_QUERY_1 = "TRUNCATE TABLE " + TABLE_NAME_1;

    private static final String EXPECTED_CREATE_TABLE_QUERY_1 = "CREATE TABLE " + TABLE_NAME_1 + " (id SERIAL PRIMARY KEY, column1 INTEGER, column2 VARCHAR(255), column3 VARCHAR(255))";

    @Test
    public void getInsertQueryForPreparedStatement() {
        List<ColumnDefinition> columns = new ArrayList<>();
        columns.add(COLUMN_1);
        columns.add(COLUMN_2);
        columns.add(COLUMN_3);
        String query = QueryBuilder.getInsertQueryForPreparedStatement(TABLE_NAME_1, columns);
        Assert.assertEquals(EXPECTED_INSERT_QUERY_1.toUpperCase(), query.toUpperCase());
    }

    @Test
    public void getQueryForCreateTableTest() {
        List<ColumnDefinition> columns = new ArrayList<>();
        columns.add(COLUMN_1);
        columns.add(COLUMN_2);
        columns.add(COLUMN_3);
        String query = QueryBuilder.getQueryForCreateTable(TABLE_NAME_1, columns);
        Assert.assertEquals(EXPECTED_CREATE_TABLE_QUERY_1.toUpperCase(), query.toUpperCase());
    }

    @Test
    public void getQueryForTruncateTableTest() {
        String query = QueryBuilder.getQueryForTruncateTable(TABLE_NAME_1);
        Assert.assertEquals(EXPECTED_TRUNCATE_TABLE_QUERY_1.toUpperCase(), query.toUpperCase());
    }

    @Test
    public void getQueryForDropTableTest() {
        String query = QueryBuilder.getQueryForDropTable(TABLE_NAME_1);
        Assert.assertEquals(EXPECTED_DROP_TABLE_QUERY_1.toUpperCase(), query.toUpperCase());
    }
}
