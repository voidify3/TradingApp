package common;

/**
 * This type is used to serialise a query request to the server
 */
public class DataPacket {
    public String table;
    public String filter; //the WHERE clause of a SQL query, not including the WHERE keyword
    public DataObject object;
    public Boolean insertTypeFlag;

    public DataPacket(String table, String filter, DataObject object, Boolean insertTypeFlag) {
        this.table = table;
        this.filter = filter;
        this.object = object;
        this.insertTypeFlag = insertTypeFlag;
    }
}
