package eu.unifiedviews.plugins.loader.relationaltosql.test;

import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dataunit.relational.RelationalDataUnit;
import eu.unifiedviews.dataunit.relational.WritableRelationalDataUnit;
import eu.unifiedviews.helpers.dpu.test.config.ConfigurationBuilder;
import eu.unifiedviews.plugins.loader.relationaltosql.RelationalToSql;
import eu.unifiedviews.plugins.loader.relationaltosql.RelationalToSqlConfig_V1;
import org.apache.commons.io.IOUtils;
import org.junit.*;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.turtle.TurtleWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by yyz on 14/09/16.
 */
@Ignore("Not implemented yet")
public class SqlUpdateTest {
    private static RelationalToSql loader;
    private static TestEnvironment env;
    private static WritableRelationalDataUnit input;
    private static Properties properties;
    private static RepositoryConnection connection;
    private static RelationalToSqlConfig_V1 config;

    @BeforeClass
    public static void before() throws Exception {
        loader = new RelationalToSql();
        env = new TestEnvironment();

        properties = new Properties();
        properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("oracle.properties"));

        config = new RelationalToSqlConfig_V1();
    }

    @AfterClass
    public static void after() throws Exception {
        env.release();
    }
}
