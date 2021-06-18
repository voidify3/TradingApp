package ClientSide;

import common.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;

import static common.DatabaseTables.*;
import static common.ProtocolKeywords.*;
import static java.lang.String.valueOf;

/**
 * Back end of the client program, interacting with the server over the host and port
 */
class NetworkDataSource extends TradingAppDataSource {
    private static final String HOSTNAME = "127.0.0.1";
    private static final int PORT = 10000;
    static final String BLANK_FILTER = "1=1";


    NetworkDataSource() {
        refreshDelay = requestSpecial(TRADE_DELAY_PASSWORD);
        //Send special request type 3
        //Save the number
    }

    //-------------ATOMIC QUERY EXECUTORS--------------
    //These methods are the ones that actually communicate with the server

    /**
     *
     * @param info DataPacket object describing request
     * @return ArrayList of query results
     */
    private ArrayList<DataObject> requestSelect(DataPacket info) {
        try {
            Socket socket = new Socket(HOSTNAME, PORT);

            try (
                    ObjectOutputStream objectOutputStream =
                            new ObjectOutputStream(socket.getOutputStream());
            ) {
                objectOutputStream.writeObject(SELECT);
                objectOutputStream.writeObject(info);
                objectOutputStream.flush();

                try (
                        ObjectInputStream objectInputStream =
                                new ObjectInputStream(socket.getInputStream());
                ) {
                    return (ArrayList<DataObject>) objectInputStream.readObject();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     *
     * @param keyword Query type keyword
     * @param info DataPacket object describing request
     * @return Number of rows affected by query
     */
    private int requestNonselect(ProtocolKeywords keyword, DataPacket info) {
        try {
            Socket socket = new Socket(HOSTNAME, PORT);
            try (
                    ObjectOutputStream objectOutputStream =
                            new ObjectOutputStream(socket.getOutputStream());
            ) {
                objectOutputStream.writeObject(keyword);
                objectOutputStream.writeObject(info);
                objectOutputStream.flush();

                try (
                        ObjectInputStream objectInputStream =
                                new ObjectInputStream(socket.getInputStream());
                ) {
                    Object o = objectInputStream.readObject();
                    return (int) o;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }
    //--------------QUERY TYPE HELPERS-----------------

    private int requestSpecial(String info) {
        return requestNonselect(SPECIAL, new DataPacket(null, info, null, null));
    }
    private ArrayList<DataObject> select(DatabaseTables table, String filter) {
        return requestSelect(new DataPacket(table, filter, null, null));
    }
    private int update(DatabaseTables table, DataObject data) {
        return requestNonselect(ProtocolKeywords.UPDATE, new DataPacket(table, null, data, null));
    }
    private int insert(DatabaseTables table, DataObject data) {
        return requestNonselect(ProtocolKeywords.INSERT, new DataPacket(table, null, data, false));
    }
    private int insertUpdateOnDupKey(DatabaseTables table, DataObject data) {
        return requestNonselect(ProtocolKeywords.INSERT, new DataPacket(table, null, data, true));
    }
    private int delete(DatabaseTables table, String filter) {
        return requestNonselect(ProtocolKeywords.DELETE, new DataPacket(table, filter, null, null));
    }


    //------------------SPECIAL CASE QUERY HELPERS---------
    private ArrayList<DataObject> selectByValue(DatabaseTables table, String column, String value) {
        return select(table, filterEquals(column, sqlFriendlyString(value)));
    }
    private ArrayList<DataObject> selectByValue(DatabaseTables table, String column, int value) {
        return select(table, filterEquals(column, valueOf(value)));
    }

    private DataObject selectByKey(DatabaseTables table, String keyValue) {
        ArrayList<DataObject> results = selectByValue(table, keyColumnName(table), keyValue);
        return extractElement(results);
    }
    private DataObject selectByKey(DatabaseTables table, int keyValue) {
        ArrayList<DataObject> results = selectByValue(table, keyColumnName(table), keyValue);
        return extractElement(results); //there should only be 1 result, so return it
    }

    private int deleteByKey(DatabaseTables table, String keyValue) {
        return delete(table, filterEquals(keyColumnName(table), sqlFriendlyString(keyValue)));
    }
    private int deleteByKey(DatabaseTables table, int keyValue) {
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

    //--------------------MISC HELPERS--------

    private String keyColumnName(DatabaseTables table) {
        return table.getColumns()[0];
    }
    private String filterEquals(String column, String value) {
        return column + "=" + value;
    }
    private String filterBetween(String column, String value1, String value2) {
        return String.format("%s BETWEEN %s AND %s", column, value1, value2);
        //return column + " BETWEEN " + value1 + " AND " + value2;
    }
    private String orderResolvedFilter(Boolean resolvedFlag) {
        if (resolvedFlag == null) return "";
        return String.format(" AND %s IS %s NULL", SELL.getColumns()[6], (resolvedFlag? "NOT " : ""));
        //return " AND " + SELL.getColumns()[6] + " IS " + (resolvedFlag? "NOT " : "") + "NULL";
    }

    private String sqlFriendlyString(Timestamp value) {
        return "'" + value.toString() + "'";

    }
    private String sqlFriendlyString(String value) {
        return "'" + escapeWildcardsForMySQL(value) + "'";
    }

    private String innerQuery(String tablename, String column, String filter) {
        return String.format("(SELECT %s FROM %s WHERE %s)", column, tablename, filter);
    }

    private DataObject extractElement(ArrayList<DataObject> a) {
        if (a.isEmpty()) return null;
        else return a.get(0);
    }

    //--------------------METHODS---------------------------------
    //---SELECT---

    //--Calling select

    @Override
    ArrayList<User> allUsers() {
        return (ArrayList) select(DatabaseTables.USER, BLANK_FILTER);
    }
    @Override
    ArrayList<OrgUnit> allOrgUnits() {
        return (ArrayList) select(UNIT, BLANK_FILTER);
    }
    @Override
    ArrayList<Asset> allAssets() {
        return (ArrayList) select(ASSET, BLANK_FILTER);
    }
    @Override
    ArrayList<InventoryRecord> allInventories() {
        return  (ArrayList) select(INV, BLANK_FILTER);
    }
    @Override
    ArrayList<SellOrder> allSellOrders() { return allSellOrders(null); }
    @Override
    ArrayList<BuyOrder> allBuyOrders() { return allBuyOrders(null); }

    @Override
    InventoryRecord inventoryRecordByKeys(String unit, int asset) {
        ArrayList<DataObject> results = select(INV,
                filterEquals(INV.getColumns()[0], sqlFriendlyString(unit)) +
                        " AND " + filterEquals(INV.getColumns()[1], valueOf(asset)));
        return (InventoryRecord) extractElement(results); }

    @Override
    ArrayList<SellOrder> allSellOrders(Boolean resolved) {
        return (ArrayList) select(SELL, BLANK_FILTER + orderResolvedFilter(resolved));
    }

    @Override
    ArrayList<BuyOrder> allBuyOrders(Boolean resolved) {
        return (ArrayList) select(BUY, BLANK_FILTER + orderResolvedFilter(resolved));
    }

    @Override
    ArrayList<SellOrder> sellOrdersByUnit(String unitName, Boolean resolved) {
        return (ArrayList) select(SELL,
                filterEquals(SELL.getColumns()[1], sqlFriendlyString(unitName)) + orderResolvedFilter(resolved)); }
    @Override
    ArrayList<SellOrder> sellOrdersByAsset(int assetID, Boolean resolved) {
        return (ArrayList) select(SELL,
                filterEquals(SELL.getColumns()[2], valueOf(assetID)) + orderResolvedFilter(resolved)); }
    @Override
    ArrayList<SellOrder> sellOrdersPlacedBetween(Timestamp start, Timestamp end, Boolean resolved) {
        return (ArrayList) select(SELL,
                filterBetween(SELL.getColumns()[5], sqlFriendlyString(start), sqlFriendlyString(end))
                        + orderResolvedFilter(resolved)); }
    @Override
    ArrayList<SellOrder> sellOrdersResolvedBetween(Timestamp start, Timestamp end) {
        return (ArrayList) select(SELL,
                filterBetween(SELL.getColumns()[6], sqlFriendlyString(start), sqlFriendlyString(end))); }
    @Override
    ArrayList<SellOrder> sellOrdersReconciledBetween(Timestamp start, Timestamp end) {
        return (ArrayList) select(SELL, SELL.getColumns()[0] + " IN " + innerQuery(BUY.getName(), BUY.getColumns()[7],
                filterBetween(BUY.getColumns()[6], sqlFriendlyString(start), sqlFriendlyString(end))) +
                " OR " + filterBetween(SELL.getColumns()[6], sqlFriendlyString(start), sqlFriendlyString(end))
        ); }
    @Override
    ArrayList<BuyOrder> buyOrdersByUnit(String unitName, Boolean resolved) {
        return (ArrayList) select(BUY,
                filterEquals(BUY.getColumns()[1], sqlFriendlyString(unitName))
                        + orderResolvedFilter(resolved)); }
    @Override
    ArrayList<BuyOrder> buyOrdersByAsset(int assetID, Boolean resolved) {
        return (ArrayList) select(BUY,
                filterEquals(BUY.getColumns()[2], valueOf(assetID)) + orderResolvedFilter(resolved)); }
    @Override
    ArrayList<BuyOrder> buyOrdersByAssetResolvedBetween(int assetID, Timestamp start, Timestamp end) {
        return (ArrayList) select(BUY,
                filterEquals(BUY.getColumns()[2], valueOf(assetID)) + " AND "
                        + filterBetween(BUY.getColumns()[6], sqlFriendlyString(start), sqlFriendlyString(end))); }
    @Override
    ArrayList<BuyOrder> buyOrdersPlacedBetween(Timestamp start, Timestamp end, Boolean resolved) {
        return (ArrayList) select(BUY,
                filterBetween(BUY.getColumns()[5], sqlFriendlyString(start), sqlFriendlyString(end))
                        + orderResolvedFilter(resolved)); }
    @Override
    ArrayList<BuyOrder> buyOrdersResolvedBetween(Timestamp start, Timestamp end) {
        return (ArrayList) select(BUY,
                filterBetween(BUY.getColumns()[6], sqlFriendlyString(start), sqlFriendlyString(end))); }

    //--Calling selectByValue
    @Override
    ArrayList<User> usersByUnit(String unit) {
        return (ArrayList) selectByValue(USER, USER.getColumns()[3], unit); }
    @Override
    ArrayList<InventoryRecord> inventoriesByUnit(String unit) {
        return (ArrayList) selectByValue(INV, INV.getColumns()[0], unit); }
    @Override
    ArrayList<InventoryRecord> inventoriesByAsset(int asset) {
        return (ArrayList) selectByValue(INV, INV.getColumns()[1], asset); }
    @Override
    ArrayList<BuyOrder> buyOrdersByBoughtFrom(int sellOrderID) {
        return (ArrayList) selectByValue(BUY, BUY.getColumns()[7], sellOrderID); }

    //--Calling selectByKey
    @Override
    User userByKey(String name) {
        return (User) selectByKey(USER, name);
    }
    @Override
    OrgUnit unitByKey(String name) {
        return (OrgUnit) selectByKey(UNIT, name);
    }
    @Override
    Asset assetByKey(int ID) {
        return (Asset) selectByKey(ASSET, ID);
    }
    @Override
    SellOrder sellOrderByKey(int ID) {
        return (SellOrder) selectByKey(SELL, ID);
    }
    @Override
    BuyOrder buyOrderByKey(int ID) {
        return (BuyOrder) selectByKey(BUY, ID);
    }

    //---INSERT---
    @Override
    int insertOrUpdateInventory(InventoryRecord i) {
        return insertUpdateOnDupKey(INV, i);
    }
    @Override
    int insertUser(User u) {
        return insert(USER, u);
    }
    @Override
    int insertUnit(OrgUnit u) {
        return insert(UNIT, u);
    }
    @Override
    int insertAsset(Asset a) {
        return insert(ASSET, a);
    }
    @Override
    int insertSellOrder(SellOrder s) {
        return insert(SELL, s);
    }
    @Override
    int insertBuyOrder(BuyOrder b) {
        return insert(BUY, b);
    }

    //---UPDATE---
    @Override
    int updateUser(User u) {
        return update(USER, u);
    }
    @Override
    int updateUnit(OrgUnit u) {
        return update(UNIT, u);
    }
    @Override
    int updateAsset(Asset a) {
        return update(ASSET, a);
    }
    @Override
    int updateSellOrder(SellOrder s) {
        return update(SELL, s);
    }
    @Override
    int updateBuyOrder(BuyOrder b) {
        return update(BUY, b);
    }

    //---DELETE---
    @Override
    int debugDeleteEverything() {
        return requestSpecial(DROP_PASSWORD);
    }
    @Override
    void recreate() {
        requestSpecial(RECREATE_PASSWORD);
    }

    @Override
    int deleteInventoryRecord(String unit, int asset) {
        return delete(INV,filterEquals(INV.getColumns()[0], sqlFriendlyString(unit)) +
                " AND " + filterEquals(INV.getColumns()[1], valueOf(asset)));
    }
    @Override
    int deleteUser(String key) {
        return deleteByKey(USER, key);
    }
    @Override
    int deleteUnit(String key) {
        return deleteByKey(UNIT, key);
    }
    @Override
    int deleteAsset(int key) {
        return deleteByKey(ASSET, key);
    }
    @Override
    int deleteBuyOrder(int key) {
        return deleteByKey(BUY, key);
    }
    @Override
    int deleteSellOrder(int key) {
        return deleteByKey(SELL, key);
    }
}
