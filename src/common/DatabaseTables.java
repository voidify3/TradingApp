package common;

//encodes table and column names
public enum DatabaseTables {
    UNIT("orgunit", new String[]{"name", "credits"}),
    ASSET("asset", new String[]{"idx", "description"}),
    USER("user", new String[]{"name", "passhash", "salt", "orgunit"}),
    INV("inventories", new String[]{"orgunit", "asset", "quantity"}),
    SELL("sellorder", new String[]{"idx","user","asset","quantity","price","datePlaced","dateResolved"}),
    BUY("buyorder",
            new String[]{"idx","user","asset","quantity","price","datePlaced","dateResolved","boughtFrom"});

    DatabaseTables(String tableName, String[] columnNames) {
        this.tableName=tableName;
        this.columnNames=columnNames;
    }
    private String tableName;

    public String getTableName() {
        return tableName;
    }

    public String[] getColumnNames() {
        return columnNames;
    }

    private String[] columnNames;
}
