package ClientSide;

import common.*;

import java.sql.Timestamp;
import java.util.ArrayList;

abstract class TradingAppDataSource {
    int refreshDelay = 0;
    /**
     *
     * @return ArrayList of all users
     */
    abstract ArrayList<User> allUsers();

    /**
     *
     * @return ArrayList of all OrgUnits
     */
    abstract ArrayList<OrgUnit> allOrgUnits();

    /**
     *
     * @return ArrayList of all assets
     */
    abstract ArrayList<Asset> allAssets();

    /**
     *
     * @return ArrayList of all inventory records
     */
    abstract ArrayList<InventoryRecord> allInventories();

    /**
     *
     * @return ArrayList of all sell orders
     */
    abstract ArrayList<SellOrder> allSellOrders();

    /**
     *
     * @return ArrayList of all buy orders
     */
    abstract ArrayList<BuyOrder> allBuyOrders();

    /**
     *
     * @param unit OrgUnit component of the composite key
     * @param asset Asset component of the composite key
     * @return The InventoryRecord object with the requested keys
     */
    abstract InventoryRecord inventoryRecordByKeys(String unit, int asset);

    abstract ArrayList<SellOrder> allSellOrders(Boolean resolved);

    abstract ArrayList<BuyOrder> allBuyOrders(Boolean resolved);
    /**
     *
     * @param unitName a username
     * @param resolved Flag describing resolvedness filter:
     *                 true for "resolved orders only", false for "unresolved orders only",
     *                 null for "include both resolved and unresolved orders"
     * @return ArrayList of all sell orders placed by members of the requested unit that meet the resolvedness filter
     */
    abstract ArrayList<SellOrder> sellOrdersByUnit(String unitName, Boolean resolved);

    /**
     *
     * @param assetID an asset ID
     * @param resolved Flag describing resolvedness filter:
     *                 true for "resolved orders only", false for "unresolved orders only",
     *                 null for "include both resolved and unresolved orders"
     * @return ArrayList of all sell orders for the requested asset that meet the resolvedness filter
     */
    abstract ArrayList<SellOrder> sellOrdersByAsset(int assetID, Boolean resolved);

    /**
     *
     * @param start Start date
     * @param end End date
     * @param resolved Flag describing resolvedness filter:
     *                 true for "resolved orders only", false for "unresolved orders only",
     *                 null for "include both resolved and unresolved orders"
     * @return ArrayList of all sell orders with DatePlaced in requested range that meet the resolvedness filter
     */
    abstract ArrayList<SellOrder> sellOrdersPlacedBetween(Timestamp start, Timestamp end, Boolean resolved);

    /**
     *
     * @param start Start date
     * @param end End date
     * @return ArrayList of all sell orders with DateResolved in requested range
     */
    abstract ArrayList<SellOrder> sellOrdersResolvedBetween(Timestamp start, Timestamp end);

    /**
     *
     * @param start Start date
     * @param end End date
     * @return ArrayList of all sell orders that were involved in transactions during the requested range
     * i.e. these sell orders are listed as BoughtFrom by buy orders resolved in that range, even if their quantity
     * was not reduced to zero by the transaction.
     * The results of this query are a superset to sellOrdersResolvedBetween(start, end)
     */
    abstract ArrayList<SellOrder> sellOrdersReconciledBetween(Timestamp start, Timestamp end);

    /**
     *
     * @param unitName the name of an organisational unit
     * @param resolved Flag describing resolvedness filter:
     *                 true for "resolved orders only", false for "unresolved orders only",
     *                 null for "include both resolved and unresolved orders"
     * @return ArrayList of all buy orders placed by members of the requested unit that meet the resolvedness filter
     */
    abstract ArrayList<BuyOrder> buyOrdersByUnit(String unitName, Boolean resolved);

    /**
     *
     * @param assetID an asset ID
     * @param resolved Flag describing resolvedness filter:
     *                 true for "resolved orders only", false for "unresolved orders only",
     *                 null for "include both resolved and unresolved orders"
     * @return ArrayList of all buy orders for the requested asset that meet the resolvedness filter
     */
    abstract ArrayList<BuyOrder> buyOrdersByAsset(int assetID, Boolean resolved);

    /**
     *
     * @param assetID an asset ID
     * @param start Start date
     * @param end End date
     * @return ArrayList of all buy orders for the asset in question with DateResolved in requested range
     */
    abstract ArrayList<BuyOrder> buyOrdersByAssetResolvedBetween(int assetID, Timestamp start, Timestamp end);

    /**
     *
     * @param start Start date
     * @param end End date
     * @param resolved Flag describing resolvedness filter:
     *                 true for "resolved orders only", false for "unresolved orders only",
     *                 null for "include both resolved and unresolved orders"
     * @return ArrayList of all buy orders with DatePlaced in requested range that meet the resolvedness filter
     */
    abstract ArrayList<BuyOrder> buyOrdersPlacedBetween(Timestamp start, Timestamp end, Boolean resolved);

    /**
     *
     * @param start Start date
     * @param end End date
     * @return ArrayList of all buy orders with DateResolved in requested range
     */
    abstract ArrayList<BuyOrder> buyOrdersResolvedBetween(Timestamp start, Timestamp end);

    abstract ArrayList<User> usersByUnit(String unit);

    abstract ArrayList<InventoryRecord> inventoriesByUnit(String unit);

    abstract ArrayList<InventoryRecord> inventoriesByAsset(int asset);

    abstract ArrayList<BuyOrder> buyOrdersByBoughtFrom(int sellOrderID);

    abstract User userByKey(String name);

    abstract OrgUnit unitByKey(String name);

    abstract Asset assetByKey(int ID);

    abstract SellOrder sellOrderByKey(int ID);

    abstract BuyOrder buyOrderByKey(int ID);

    //---INSERT---

    /**
     *
     * @param i inventoryrecord object to send
     * @return 1 if insert succeeded, 2 if insert failed but update succeeded,
     * -1 if query failed due to nonexistence of unit or asset
     */
    abstract int insertOrUpdateInventory(InventoryRecord i);

    /**
     *
     * @param u user object to send
     * @return 1 if insert succeeded, 0 if insert failed due to duplicate key,
     * -1 if insert failed due to nonexistence of unit
     */
    abstract int insertUser(User u);

    /**
     *
     * @param u orgunit object to send
     * @return 1 if insert succeeded, 0 if insert failed
     */
    abstract int insertUnit(OrgUnit u);

    /**
     *
     * @param a asset object to send
     * @return 1 if insert succeeded, 0 if insert failed
     */
    abstract int insertAsset(Asset a);

    /**
     *
     * @param s sellorder object to send
     * @return 1 if insert succeeded, 0 if insert failed due to duplicate key (should never happen though),
     * -1 if insert failed due to nonexistence of asset or unit
     */
    abstract int insertSellOrder(SellOrder s);

    /**
     *
     * @param b buyorder object to send
     * @return 1 if insert succeeded, 0 if insert failed (should never happen though),
     * -1 if insert failed due to nonexistence of asset or unit
     */
    abstract int insertBuyOrder(BuyOrder b);

    //---UPDATE---

    /**
     *
     * @param u user object to send
     * @return 1 if update succeeded, 0 if update failed due to nonexistent key,
     * -1 if update failed due to nonexistence of unit
     */
    abstract int updateUser(User u);

    /**
     *
     * @param u orgunit object to send
     * @return 1 if update succeeded, 0 if update failed due to nonexistent key
     */
    abstract int updateUnit(OrgUnit u);

    /**
     *
     * @param a asset object to send
     * @return 1 if update succeeded, 0 if update failed due to nonexistent key
     */
    abstract int updateAsset(Asset a);

    /**
     *
     * @param s sellorder object to send
     * @return 1 if update succeeded, 0 if update failed due to nonexistent key,
     * -1 if update failed due to nonexistence of asset or unit
     */
    abstract int updateSellOrder(SellOrder s);

    /**
     *
     * @param b buyorder object to send
     * @return 1 if update succeeded, 0 if update failed due to nonexistent key,
     *  -1 if update failed due to nonexistence of asset or unit or BoughtFrom sell order
     */
    abstract int updateBuyOrder(BuyOrder b);

    //---DELETE---

    /**
     * Reset the data-- delete everything and reset auto-increment keys to 1. Exists for test purposes
     * @return The total number of records in all tables before the request was executed
     */
    abstract int debugDeleteEverything();

    /**
     * Recreate the tables. Does nothing if debugDeleteEverything wasn't just called. Exists for test purposes
     */
    abstract void recreate();


    /**
     *
     * @param unit Unit name of intended record
     * @param asset Asset ID of intended record
     * @return 1 if deletion succeeded, 0 if deletion failed
     */
    abstract int deleteInventoryRecord(String unit, int asset);

    /**
     *
     * @param key username to delete
     * @return 1 if deletion succeeded, 0 if deletion failed due to nonexistent key
     */
    abstract int deleteUser(String key);

    /**
     *
     * @param key name to delete
     * @return 1 if deletion succeeded, 0 if deletion failed
     */
    abstract int deleteUnit(String key);

    /**
     *
     * @param key asset ID to delete
     * @return 1 if deletion succeeded, 0 if deletion failed
     */
    abstract int deleteAsset(int key);

    /**
     *
     * @param key order ID to delete
     * @return 1 if deletion succeeded, 0 if deletion failed due to nonexistent key
     */
    abstract int deleteBuyOrder(int key);

    /**
     *
     * @param key order ID to delete
     * @return 1 if deletion succeeded, 0 if deletion failed due to nonexistent key
     */
    abstract int deleteSellOrder(int key);

}
