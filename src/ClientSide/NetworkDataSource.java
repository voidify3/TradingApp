package ClientSide;

import common.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

import static java.lang.String.valueOf;

//Please ignore all the hideous ArrayList downcasts, I promise it's fine, server-side logic handles it so that
//all of these downcasts must be possible in the circumstances that they're attempted

public class NetworkDataSource implements TradingAppDataSource {
    private static final String HOSTNAME = "127.0.0.1";
    private static final int PORT = 10000;
    public static final String SELECT = "Select";
    public static final String UPDATE = "Update";
    public static final String INSERT = "Insert";
    public static final String DELETE = "Delete";
    //TODO: use an enum to store table and column names
    public static final String USER = "user";
    public static final String UNIT = "orgunits";
    public static final String ASSET = "asset";
    public static final String INV = "inventories";
    public static final String BUY = "buyorder";
    public static final String SELL = "sellorder";
    //temporary data source for keyColumnName
    public static final TreeMap<String, String> keys;
    static {
        TreeMap<String,String> temp = new TreeMap<>();
        temp.put(USER, "Username");
        temp.put(UNIT, "OrgUnitName");
        temp.put(ASSET, "AssetID");
        temp.put(SELL, "OrderID");
        temp.put(BUY, "OrderID");
        keys = (TreeMap<String, String>) Collections.unmodifiableMap(temp);
    } //initialise it


    public NetworkDataSource() {
        //Ping server, exception if fail
    }

    /**
     *
     * @return Whether the ping succeeded
     */
    @Override
    public Boolean ping() {
        return false;
    }
    //-------------ATOMIC QUERY EXECUTORS--------------
    //These methods are the ones that actually communicate with the server

    /**
     *
     * @param info DataPacket object describing request
     * @return ArrayList of query results
     */
    private ArrayList<DataObject> requestSelect(DataPacket info) {
        //use SELECT here
        return null;
    }

    /**
     *
     * @param keyword Query type keyword
     * @param info DataPacket object describing request
     * @return Number of rows affected by query
     */
    private int requestNonselect(String keyword, DataPacket info) {
        return 0;
    }



    //--------------QUERY TYPE HELPERS-----------------
    private ArrayList<DataObject> select(String table, String filter) {
        return requestSelect(new DataPacket(table, filter, null, null));
    }
    private int update(String table, DataObject data) {
        return requestNonselect(UPDATE, new DataPacket(table, null, data, null));
    }
    private int insert(String table, DataObject data) {
        return requestNonselect(INSERT, new DataPacket(table, null, data, false));
    }
    private int insertUpdateOnDupKey(String table, DataObject data) {
        return requestNonselect(INSERT, new DataPacket(table, null, data, true));
    }
    private int delete(String table, String filter) {
        return requestNonselect(DELETE, new DataPacket(table, filter, null, null));
    }


    //------------------SPECIAL CASE QUERY HELPERS---------
    private ArrayList<DataObject> selectByValue(String table, String column, String value) {
        return select(table, filterEquals(column, sqlFriendlyString(value)));
    }
    private ArrayList<DataObject> selectByValue(String table, String column, int value) {
        return select(table, filterEquals(column, valueOf(value)));
    }

    private DataObject selectByKey(String table, String keyValue) {
        ArrayList<DataObject> results = selectByValue(table, keyColumnName(table), keyValue);
        return results.get(0);
    }
    private DataObject selectByKey(String table, int keyValue) {
        ArrayList<DataObject> results = selectByValue(table, keyColumnName(table), keyValue);
        return results.get(0); //there should only be 1 result, so return it
    }

    private int deleteByKey(String table, String keyValue) {
        return delete(table, filterEquals(keyColumnName(table), sqlFriendlyString(keyValue)));
    }
    private int deleteByKey(String table, int keyValue) {
        return delete(table, filterEquals(keyColumnName(table), valueOf(keyValue)));
    }

    //-------------------STRING ESCAPING METHODS from https://stackoverflow.com/a/45294943 to protect against injection
    private String escapeStringForMySQL(String s) {
        return s.replace("\\", "\\\\")
                .replaceAll("\b","\\b")
                .replaceAll("\n","\\n")
                .replaceAll("\r", "\\r")
                .replaceAll("\t", "\\t")
                .replaceAll("\\x1A", "\\Z")
                .replaceAll("\\x00", "\\0")
                .replaceAll("'", "\\'")
                .replaceAll("\"", "\\\"");
    }

    private String escapeWildcardsForMySQL(String s) {
        return escapeStringForMySQL(s)
                .replaceAll("%", "\\%")
                .replaceAll("_","\\_");
    }

    //--------------------FILTER-FORMING HELPERS--------

    private String keyColumnName(String table) {
        return keys.get(table);
    }
    private String filterEquals(String column, String value) {
        return column + "=" + value;
    }
    private String filterBetween(String column, String value1, String value2) {
        return column + " BETWEEN " + value1 + " AND " + value2;
    }
    private String orderResolvedFilter(Boolean resolvedFlag) {
        if (resolvedFlag == null) return "";
        return " AND DateResolved IS " + (resolvedFlag? "NOT " : "") + "NULL";
    }

    private String sqlFriendlyString(Timestamp value) {
        return "'" + value.toString() + "'";

    }
    private String sqlFriendlyString(String value) {
        return "'" + escapeWildcardsForMySQL(value) + "'";
    }

    //--------------------PUBLIC METHODS---------------------------------
    //---SELECT---

    //--Calling select

    /**
     *
     * @return ArrayList of all users
     */
    @Override
    public ArrayList<User> allUsers() {
        return (ArrayList) select(USER, "");
    }

    /**
     *
     * @return ArrayList of all OrgUnits
     */
    @Override
    public ArrayList<OrgUnit> allOrgUnits() {
        return (ArrayList)select(UNIT, "");
    }

    /**
     *
     * @return ArrayList of all assets
     */
    @Override
    public ArrayList<Asset> allAssets() {
        return (ArrayList) select(ASSET, "");
    }

    /**
     *
     * @return ArrayList of all inventory records
     */
    @Override
    public ArrayList<InventoryRecord> inventoryList() {
        return  (ArrayList) select(INV, "");
    }

    /**
     *
     * @param unit OrgUnit component of the composite key
     * @param asset Asset component of the composite key
     * @return The InventoryRecord object with the requested keys
     */
    @Override
    public InventoryRecord inventoryRecordByKeys(String unit, int asset) {
        ArrayList<InventoryRecord>  results = (ArrayList) select(INV,
                filterEquals("OrgUnit", sqlFriendlyString(unit)) +
                        " AND " + filterEquals("Asset", valueOf(asset)));
        if (results.isEmpty()) return null;
        return results.get(0);
    }

    /**
     *
     * @param username a username
     * @param resolved Flag describing resolvedness filter:
     *                 true for "resolved orders only", false for "unresolved orders only",
     *                 null for "include both resolved and unresolved orders"
     * @return ArrayList of all sell orders placed by the requested user that meet the resolvedness filter
     */
    @Override
    public ArrayList<SellOrder> sellOrdersByUser(String username, Boolean resolved) {
        return (ArrayList) select(SELL,
                filterEquals("User", sqlFriendlyString(username)) + orderResolvedFilter(resolved));
    }

    /**
     *
     * @param assetID an asset ID
     * @param resolved Flag describing resolvedness filter:
     *                 true for "resolved orders only", false for "unresolved orders only",
     *                 null for "include both resolved and unresolved orders"
     * @return ArrayList of all sell orders for the requested asset that meet the resolvedness filter
     */
    @Override
    public ArrayList<SellOrder> sellOrdersByAsset(int assetID, Boolean resolved) {
        return (ArrayList) select(SELL,
                filterEquals("Asset", valueOf(assetID)) + orderResolvedFilter(resolved));
    }

    /**
     *
     * @param start Start date
     * @param end End date
     * @param resolved Flag describing resolvedness filter:
     *                 true for "resolved orders only", false for "unresolved orders only",
     *                 null for "include both resolved and unresolved orders"
     * @return ArrayList of all sell orders with DatePlaced in requested range that meet the resolvedness filter
     */
    @Override
    public ArrayList<SellOrder> sellOrdersPlacedBetween(Timestamp start, Timestamp end, Boolean resolved) {
        return (ArrayList) select(SELL,
                filterBetween("DatePlaced", sqlFriendlyString(start), sqlFriendlyString(end))
                        + orderResolvedFilter(resolved));
    }

    /**
     *
     * @param start Start date
     * @param end End date
     * @return ArrayList of all sell orders with DateResolved in requested range
     */
    @Override
    public ArrayList<SellOrder> sellOrdersResolvedBetween(Timestamp start, Timestamp end) {
        return (ArrayList) select(SELL,
                filterBetween("DateResolved", sqlFriendlyString(start), sqlFriendlyString(end)));
    }

    /**
     *
     * @param username a username
     * @param resolved Flag describing resolvedness filter:
     *                 true for "resolved orders only", false for "unresolved orders only",
     *                 null for "include both resolved and unresolved orders"
     * @return ArrayList of all buy orders placed by the requested user that meet the resolvedness filter
     */
    @Override
    public ArrayList<BuyOrder> buyOrdersByUser(String username, Boolean resolved) {
        return (ArrayList) select(BUY,
                filterEquals("User", sqlFriendlyString(username)) + orderResolvedFilter(resolved));
    }

    /**
     *
     * @param assetID an asset ID
     * @param resolved Flag describing resolvedness filter:
     *                 true for "resolved orders only", false for "unresolved orders only",
     *                 null for "include both resolved and unresolved orders"
     * @return ArrayList of all buy orders for the requested asset that meet the resolvedness filter
     */
    @Override
    public ArrayList<BuyOrder> buyOrdersByAsset(int assetID, Boolean resolved) {
        return (ArrayList) select(BUY,
                filterEquals("Asset", valueOf(assetID)) + orderResolvedFilter(resolved));
    }

    @Override
    public ArrayList<BuyOrder> buyOrdersByAssetResolvedBetween(int assetID, Timestamp start, Timestamp end) {
        return (ArrayList) select(BUY,
                filterEquals("Asset", valueOf(assetID)) + " AND "
                        + filterBetween("DateResolved", sqlFriendlyString(start), sqlFriendlyString(end)));
    }

    /**
     *
     * @param start Start date
     * @param end End date
     * @param resolved Flag describing resolvedness filter:
     *                 true for "resolved orders only", false for "unresolved orders only",
     *                 null for "include both resolved and unresolved orders"
     * @return ArrayList of all buy orders with DatePlaced in requested range that meet the resolvedness filter
     */
    @Override
    public ArrayList<BuyOrder> buyOrdersPlacedBetween(Timestamp start, Timestamp end, Boolean resolved) {
        return (ArrayList) select(BUY,
                filterBetween("DatePlaced", sqlFriendlyString(start), sqlFriendlyString(end))
                        + orderResolvedFilter(resolved));
    }

    /**
     *
     * @param start Start date
     * @param end End date
     * @return ArrayList of all buy orders with DateResolved in requested range
     */
    @Override
    public ArrayList<BuyOrder> buyOrdersResolvedBetween(Timestamp start, Timestamp end) {
        return (ArrayList) select(BUY,
                filterBetween("DateResolved", sqlFriendlyString(start), sqlFriendlyString(end)));
    }

    //--Calling selectByValue
    @Override
    public ArrayList<User> usersByUnit(String unit) {
        return (ArrayList) selectByValue(USER, "OrgUnit", unit);
    }
    @Override
    public ArrayList<InventoryRecord> inventoriesByUnit(String unit) {
        return (ArrayList) selectByValue(INV, "OrgUnit", unit);
    }
    @Override
    public ArrayList<InventoryRecord> inventoriesByAsset(int asset) {
        return (ArrayList) selectByValue(INV, "Asset", asset);
    }
    @Override
    public ArrayList<BuyOrder> buyOrdersByBoughtFrom(int sellOrderID) {
        return (ArrayList) selectByValue(INV, "BoughtFrom", sellOrderID);
    }

    //--Calling selectByKey
    @Override
    public User userByKey(String name) {
        return (User) selectByKey(USER, name);
    }
    @Override
    public OrgUnit unitByKey(String name) {
        return (OrgUnit) selectByKey(UNIT, name);
    }
    @Override
    public Asset assetByKey(int ID) {
        return (Asset) selectByKey(ASSET, ID);
    }

    @Override
    public SellOrder sellOrderByKey(int ID) {
        return (SellOrder) selectByKey(SELL, ID);
    }

    @Override
    public BuyOrder buyOrderByKey(int ID) {
        return (BuyOrder) selectByKey(BUY, ID);
    }

    //---INSERT---
    @Override
    public int insertOrUpdateInventory(InventoryRecord i) {
        return insertUpdateOnDupKey(INV, i);
    }
    @Override
    public int insertUser(User u) {
        return insert(USER, u);
    }
    @Override
    public int insertUnit(OrgUnit u) {
        return insert(UNIT, u);
    }
    @Override
    public int insertAsset(Asset a) {
        return insert(ASSET, a);
    }
    @Override
    public int insertSellOrder(SellOrder s) {
        return insert(SELL, s);
    }
    @Override
    public int insertBuyOrder(BuyOrder b) {
        return insert(BUY, b);
    }

    //---UPDATE---
    @Override
    public int updateUser(User u) {
        return update(USER, u);
    }
    @Override
    public int updateUnit(OrgUnit u) {
        return update(UNIT, u);
    }
    @Override
    public int updateAsset(Asset a) {
        return update(ASSET, a);
    }
    @Override
    public int updateSellOrder(SellOrder s) {
        return update(SELL, s);
    }
    @Override
    public int updateBuyOrder(BuyOrder b) {
        return update(BUY, b);
    }

    //---DELETE---
    @Override
    public int debugDeleteEverything() {
        return 0;
    }

    /**
     *
     * @param unit OrgUnit name component of composite key
     * @param asset Asset ID component of composite key
     * @return Number of rows affected
     */
    @Override
    public int deleteInventoryRecord(String unit, int asset) {
        return delete(INV,filterEquals("OrgUnit", sqlFriendlyString(unit)) +
                " AND " + filterEquals("Asset", valueOf(asset)));
    }

    /**
     *
     * @param key
     * @return Number of rows affected
     */
    @Override
    public int deleteUser(String key) {
        return deleteByKey(USER, key);
    }

    /**
     *
     * @param key
     * @return Number of rows affected
     */
    @Override
    public int deleteUnit(String key) {
        return deleteByKey(UNIT, key);
    }

    /**
     *
     * @param key
     * @return Number of rows affected
     */
    @Override
    public int deleteAsset(int key) {
        return deleteByKey(ASSET, key);
    }

    /**
     *
     * @param key
     * @return Number of rows affected
     */
    @Override
    public int deleteBuyOrder(int key) {
        return deleteByKey(BUY, key);
    }

    /**
     *
     * @param key
     * @return Number of rows affected
     */
    @Override
    public int deleteSellOrder(int key) {
        return deleteByKey(SELL, key);
    }
}
