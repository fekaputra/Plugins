package eu.unifiedviews.plugins.loader.database;

public class DatabaseConfig_V1 {

    private String databaseURL;

    private String userName;

    private String userPassword;

    private String tableName;

    private boolean dropTargetTable;

    private boolean clearTargetTable;

    private boolean useSSL;

    //TODO: For now only PostgreSQL is supported. Maybe database type can be user defined
    private String jdbcDriverName = "org.postgresql.Driver";

    public String getDatabaseURL() {
        return this.databaseURL;
    }

    public void setDatabaseURL(String databaseURL) {
        this.databaseURL = databaseURL;
    }

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

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
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

    public String getJDBCDriverName() {
        return this.jdbcDriverName;
    }

}
