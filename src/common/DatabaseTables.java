package common;

import java.util.Arrays;

//encodes table and column names
public enum DatabaseTables {
    UNIT("orgunit", new String[]{"name", "credits"}),
    ASSET("asset", new String[]{"idx", "description"}),
    USER("user", new String[]{"name", "passhash", "salt", "orgunit", "adminAccess"}),
    INV("inventories", new String[]{"orgunit", "asset", "quantity"}),
    SELL("sellorder", new String[]{"idx","user","asset","quantity","price","datePlaced","dateResolved"}),
    BUY("buyorder",
            new String[]{"idx","user","asset","quantity","price","datePlaced","dateResolved","boughtFrom"});

    DatabaseTables(String tableName, String[] columnNames) {
        this.tableName=tableName;
        this.columnNames=columnNames;
    }
    private final String tableName;
    private final String[] columnNames;

    private String[] ignoreAutoIncrement() { //cut off the first element for auto increment tables
        if (this == USER || this == UNIT || this == INV) {
            return columnNames;
        }
        else return Arrays.copyOfRange(columnNames,1,columnNames.length);
    }
    public String getTableName() {
        return tableName;
    }
    public String[] getColumnNames() {
        return columnNames;
    }
    public String colNamesForInsert() {
        return String.join(", ", ignoreAutoIncrement());
    }
    public String valuesForInsert() {
        String s = new String(new char[ignoreAutoIncrement().length-1]).replace("\0", "?, ");
        return s+"?";
    }
    public String templateForUpdate() {
        return String.join("=?,", ignoreAutoIncrement()) + "=?";
    }
}
