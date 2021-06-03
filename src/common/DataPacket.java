package common;

import java.io.Serializable;

/**
 * This type is used to serialise a query request to the server
 */
public class DataPacket implements Serializable {
    public DatabaseTables table;
    public String filter; //the WHERE clause of a SQL query, not including the WHERE keyword
    public DataObject object;
    public Boolean insertTypeFlag;

    public DataPacket(DatabaseTables table, String filter, DataObject object, Boolean insertTypeFlag) {
        this.table = table;
        this.filter = filter;
        this.object = object;
        this.insertTypeFlag = insertTypeFlag;
    }
}
