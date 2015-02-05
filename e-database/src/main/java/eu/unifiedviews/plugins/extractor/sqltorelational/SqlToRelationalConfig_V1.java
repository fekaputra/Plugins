package eu.unifiedviews.plugins.extractor.sqltorelational;

public class SqlToRelationalConfig_V1 {
	
	 private String databaseURL;

	 private String userName;

	 private String userPassword;
	 
	 private boolean useSSL;
	 
	 private String sqlQuery;
	 
	 // TODO: This should be generic. Now for test purposes, only Postgres is used
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

    public boolean isUseSSL() {
        return this.useSSL;
    }

    public void setUseSSL(boolean useSSL) {
        this.useSSL = useSSL;
    }

    public String getSqlQuery() {
        return this.sqlQuery;
    }

    public void setSqlQuery(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }

    public String getJdbcDriverName() {
        return this.jdbcDriverName;
    }

    public void setJdbcDriverName(String jdbcDriverName) {
        this.jdbcDriverName = jdbcDriverName;
    }

}
