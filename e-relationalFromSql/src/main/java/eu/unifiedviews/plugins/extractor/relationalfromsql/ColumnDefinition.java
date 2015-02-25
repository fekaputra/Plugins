package eu.unifiedviews.plugins.extractor.relationalfromsql;

public class ColumnDefinition {

    private String columnName;

    private String columnTypeName;

    private int columnType;

    private boolean columnNotNull;

    private int columnSize;

    public ColumnDefinition(String columnName, String columnTypeName, int columnType, boolean columnNotNull, int columnSize) {
        this.columnName = columnName;
        this.columnTypeName = columnTypeName;
        this.columnType = columnType;
        this.columnNotNull = columnNotNull;
        this.columnSize = columnSize;
    }

    public ColumnDefinition(String columnName, String columnTypeName, int columnType, boolean columnNotNull) {
        this(columnName, columnTypeName, columnType, columnNotNull, -1);
    }

    public String getColumnName() {
        return this.columnName;
    }

    public String getColumnTypeName() {
        return this.columnTypeName;
    }

    public int getColumnType() {
        return this.columnType;
    }

    public boolean isNotNull() {
        return this.columnNotNull;
    }

    public int getColumnSize() {
        return this.columnSize;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ColumnDefinition)) {
            return false;
        }
        ColumnDefinition cd = (ColumnDefinition) o;
        if (this.columnName.equals(cd.getColumnName()) && this.columnType == cd.getColumnType()) {
            return true;
        } else {
            return false;
        }
    }

}
