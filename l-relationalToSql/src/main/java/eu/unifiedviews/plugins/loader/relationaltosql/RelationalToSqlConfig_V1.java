package eu.unifiedviews.plugins.loader.relationaltosql;

public class RelationalToSqlConfig_V1 {

    private String databaseHost;

    private int databasePort;

    private String databaseName;

    private SqlDatabase.DatabaseType databaseType;

    private String sqlQuery;

    private String targetTableName;

    private String instanceName;

    private String truststoreLocation;

    private String truststorePassword;

    public String getDatabaseHost() {
        return this.databaseHost;
    }

    public void setDatabaseHost(String databaseHost) {
        this.databaseHost = databaseHost;
    }

    public int getDatabasePort() {
        return this.databasePort;
    }

    public void setDatabasePort(int databasePort) {
        this.databasePort = databasePort;
    }

    public String getDatabaseName() {
        return this.databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public SqlDatabase.DatabaseType getDatabaseType() {
        return this.databaseType;
    }

    public void setDatabaseType(SqlDatabase.DatabaseType databaseType) {
        this.databaseType = databaseType;
    }

    public String getSqlQuery() {
        return this.sqlQuery;
    }

    public void setSqlQuery(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }

    public String getTargetTableName() {
        return this.targetTableName;
    }

    public void setTargetTableName(String targetTableName) {
        this.targetTableName = targetTableName;
    }

    public String getInstanceName() {
        return this.instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getTruststoreLocation() {
        return this.truststoreLocation;
    }

    public void setTruststoreLocation(String truststoreLocation) {
        this.truststoreLocation = truststoreLocation;
    }

    public String getTruststorePassword() {
        return this.truststorePassword;
    }

    public void setTruststorePassword(String truststorePassword) {
        this.truststorePassword = truststorePassword;
    }

    private String userName;

    private String userPassword;

    private String tableNamePrefix;

    private boolean dropTargetTable;

    private boolean clearTargetTable;

    private boolean useSSL;

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return this.userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getTableNamePrefix() {
        return this.tableNamePrefix;
    }

    public void setTableNamePrefix(String tableName) {
        this.tableNamePrefix = tableName;
    }

    public boolean isDropTargetTable() {
        return this.dropTargetTable;
    }

    public void setDropTargetTable(boolean dropTargetTable) {
        this.dropTargetTable = dropTargetTable;
    }

    public boolean isClearTargetTable() {
        return this.clearTargetTable;
    }

    public void setClearTargetTable(boolean clearTargetTable) {
        this.clearTargetTable = clearTargetTable;
    }

    public boolean isUseSSL() {
        return this.useSSL;
    }

    public void setUseSSL(boolean useSSL) {
        this.useSSL = useSSL;
    }

}
