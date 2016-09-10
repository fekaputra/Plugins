package eu.unifiedviews.plugins.loader.relationaltosql;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yyz on 10/09/16.
 */
public class SqlDatatype {
    private static final SqlDatatype BIGINT = new SqlDatatype("BIGINT", BigDecimal.class.getName(), Types.BIGINT);
    private static final SqlDatatype BIT = new SqlDatatype("BIT", Boolean.class.getName(), Types.BIT);
    private static final SqlDatatype BOOLEAN = new SqlDatatype("BOOLEAN", Boolean.class.getName(), Types.BOOLEAN);
    private static final SqlDatatype CHAR = new SqlDatatype("CHAR", String.class.getName(), Types.CHAR);
    private static final SqlDatatype DATE = new SqlDatatype("DATE", Date.class.getName(), Types.DATE);
    private static final SqlDatatype DECIMAL = new SqlDatatype("DECIMAL", BigDecimal.class.getName(), Types.DECIMAL);
    private static final SqlDatatype DOUBLE = new SqlDatatype("DOUBLE", Double.class.getName(), Types.DOUBLE);
    private static final SqlDatatype FLOAT = new SqlDatatype("FLOAT", Float.class.getName(), Types.FLOAT);
    private static final SqlDatatype INTEGER = new SqlDatatype("INTEGER", Integer.class.getName(), Types.INTEGER);
    private static final SqlDatatype NUMBER = new SqlDatatype("NUMBER", BigDecimal.class.getName(), Types.NUMERIC);
    private static final SqlDatatype NUMERIC = new SqlDatatype("NUMERIC", BigDecimal.class.getName(), Types.NUMERIC);
    private static final SqlDatatype REAL = new SqlDatatype("REAL", BigDecimal.class.getName(), Types.REAL);
    private static final SqlDatatype ROWID = new SqlDatatype("ROWID", String.class.getName(), Types.VARCHAR);
    private static final SqlDatatype SMALLINT = new SqlDatatype("SMALLINT", Integer.class.getName(), Types.SMALLINT);
    private static final SqlDatatype TIMESTAMP = new SqlDatatype("TIMESTAMP", Timestamp.class.getName(), Types.TIMESTAMP);
    private static final SqlDatatype TIME = new SqlDatatype("TIME", Time.class.getName(), Types.TIME);
    private static final SqlDatatype TINYINT = new SqlDatatype("TINYINT", Integer.class.getName(), Types.TINYINT);
    private static final SqlDatatype VARCHAR = new SqlDatatype("VARCHAR", String.class.getName(), Types.VARCHAR);
    private static final SqlDatatype VARCHAR2 = new SqlDatatype("VARCHAR2", String.class.getName(), Types.VARCHAR);
    private static final SqlDatatype NCHAR = new SqlDatatype("NCHAR", String.class.getName(), Types.NCHAR);
    private static final SqlDatatype NVARCHAR2 = new SqlDatatype("NVARCHAR2", String.class.getName(), Types.NVARCHAR);
    private static final SqlDatatype CLOB = new SqlDatatype("CLOB", Byte[].class.getName(), Types.CLOB);
    private static final SqlDatatype NCLOB = new SqlDatatype("NCLOB", Byte[].class.getName(), Types.NCLOB);
    private static final SqlDatatype BLOB = new SqlDatatype("BLOB", Byte[].class.getName(), Types.BLOB);
    private static final SqlDatatype BFILE = new SqlDatatype("BFILE", Byte[].class.getName(), Types.BLOB);
    private static final SqlDatatype BINARY = new SqlDatatype("BINARY", Byte[].class.getName(), Types.BINARY);
    private static final SqlDatatype VARBINARY = new SqlDatatype("VARBINARY", Byte[].class.getName(), Types.VARBINARY);
    private static final SqlDatatype BINARY_DOUBLE = new SqlDatatype("BINARY DOUBLE", Double.class.getName(), Types.DOUBLE);
    private static final SqlDatatype BINARY_FLOAT = new SqlDatatype("BINARY FLOAT", Float.class.getName(), Types.FLOAT);
    private static final SqlDatatype DOUBLE_PRECISION = new SqlDatatype("DOUBLE PRECISION", Double.class.getName(), Types.DOUBLE);
    private static final SqlDatatype TEXT = new SqlDatatype("TEXT", String.class.getName(), Types.LONGVARCHAR);

    public static final Map<String, SqlDatatype> ORACLE_DATATYPE = new HashMap<>();
    public static final Map<String, SqlDatatype> POSTGRESQL_DATATYPE = new HashMap<>();

    static {
        ORACLE_DATATYPE.put(CHAR.getDatatypeName(), CHAR);
        ORACLE_DATATYPE.put(NCHAR.getDatatypeName(), NCHAR);
        ORACLE_DATATYPE.put(VARCHAR2.getDatatypeName(), VARCHAR2);
        ORACLE_DATATYPE.put(NVARCHAR2.getDatatypeName(), NVARCHAR2);
        ORACLE_DATATYPE.put(DATE.getDatatypeName(), DATE);
        ORACLE_DATATYPE.put(TIMESTAMP.getDatatypeName(), TIMESTAMP);
        ORACLE_DATATYPE.put(BINARY_DOUBLE.getDatatypeName(), BINARY_DOUBLE);
        ORACLE_DATATYPE.put(BINARY_FLOAT.getDatatypeName(), BINARY_FLOAT);
        ORACLE_DATATYPE.put(FLOAT.getDatatypeName(), FLOAT);
        ORACLE_DATATYPE.put(NUMBER.getDatatypeName(), NUMBER);

        POSTGRESQL_DATATYPE.put(SMALLINT.getDatatypeName(), SMALLINT);
        POSTGRESQL_DATATYPE.put(INTEGER.getDatatypeName(), INTEGER);
        POSTGRESQL_DATATYPE.put(BIGINT.getDatatypeName(), BIGINT);
        POSTGRESQL_DATATYPE.put(DECIMAL.getDatatypeName(), DECIMAL);
        POSTGRESQL_DATATYPE.put(NUMERIC.getDatatypeName(), NUMERIC);
        POSTGRESQL_DATATYPE.put(REAL.getDatatypeName(), REAL);
        POSTGRESQL_DATATYPE.put(DOUBLE_PRECISION.getDatatypeName(), DOUBLE_PRECISION);
        POSTGRESQL_DATATYPE.put(VARCHAR.getDatatypeName(), VARCHAR);
        POSTGRESQL_DATATYPE.put(TEXT.getDatatypeName(), TEXT);
        POSTGRESQL_DATATYPE.put(CHAR.getDatatypeName(), CHAR);
        POSTGRESQL_DATATYPE.put(BOOLEAN.getDatatypeName(), BOOLEAN);
        POSTGRESQL_DATATYPE.put(DATE.getDatatypeName(), DATE);
        POSTGRESQL_DATATYPE.put(TIMESTAMP.getDatatypeName(), TIMESTAMP);
        POSTGRESQL_DATATYPE.put(TIME.getDatatypeName(), TIME);

    }
    
    private String datatypeName;
    private String javaClassName;
    private int sqlTypeId;

    public SqlDatatype(String datatypeName, String javaClassName, int sqlTypeId) {
        this.datatypeName = datatypeName;
        this.javaClassName = javaClassName;
        this.sqlTypeId = sqlTypeId;
    }

    public String getDatatypeName() {
        return datatypeName;
    }

    public String getJavaClassName() {
        return javaClassName;
    }

    public int getSqlTypeId() {
        return sqlTypeId;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SqlDatatype && ((SqlDatatype) o).getSqlTypeId() == this.getSqlTypeId() ;
    }

}
