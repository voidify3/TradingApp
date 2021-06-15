package common;

import java.util.Arrays;

//encodes table and column names
public enum DatabaseTables {
    UNIT("orgunit", new String[]{"name", "credits"}),
    ASSET("asset", new String[]{"idx", "description"}),
    USER("user", new String[]{"name", "passhash", "salt", "orgunit", "adminAccess"}),
    INV("inventories", new String[]{"orgunit", "asset", "quantity"}),
    SELL("sellorder", new String[]{"idx","orgunit","asset","quantity","price","datePlaced","dateResolved"}),
    BUY("buyorder",
            new String[]{"idx","orgunit","asset","quantity","price","datePlaced","dateResolved","boughtFrom"});

    DatabaseTables(String name, String[] columns) {
        this.name = name;
        this.columns = columns;
    }
    private final String name;
    private final String[] columns;

    private String[] ignoreAutoIncrement() { //cut off the first element for auto increment tables
        if (this == USER || this == UNIT || this == INV) {
            return columns;
        }
        else return Arrays.copyOfRange(columns,1, columns.length);
    }
    public String getName() {
        return name;
    }
    public String[] getColumns() {
        return columns;
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
