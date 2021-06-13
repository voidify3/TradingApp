package ClientSide;

import common.*;

import java.sql.Timestamp;
import java.util.ArrayList;

public interface TradingAppDataSource {

    /**
     *
     * @return ArrayList of all users
     */
    ArrayList<User> allUsers();

    /**
     *
     * @return ArrayList of all OrgUnits
     */
    ArrayList<OrgUnit> allOrgUnits();

    /**
     *
     * @return ArrayList of all assets
     */
    ArrayList<Asset> allAssets();

    /**
     *
     * @return ArrayList of all inventory records
     */
    ArrayList<InventoryRecord> inventoryList();

    /**
     *
     * @return ArrayList of all sell orders
     */
    ArrayList<SellOrder> allSellOrders();

    /**
     *
     * @return ArrayList of all buy orders
     */
    ArrayList<BuyOrder> allBuyOrders();

    /**
     *
     * @param unit OrgUnit component of the composite key
     * @param asset Asset component of the composite key
     * @return The InventoryRecord object with the requested keys
     */
    InventoryRecord inventoryRecordByKeys(String unit, int asset);

    ArrayList<SellOrder> allSellOrders(Boolean resolved);

    ArrayList<BuyOrder> allBuyOrders(Boolean resolved);
    /**
     *
     * @param username a username
     * @param resolved Flag describing resolvedness filter:
     *                 true for "resolved orders only", false for "unresolved orders only",
     *                 null for "include both resolved and unresolved orders"
     * @return ArrayList of all sell orders placed by the requested user that meet the resolvedness filter
     */
    ArrayList<SellOrder> sellOrdersByUser(String username, Boolean resolved);

    /**
     *
     * @param assetID an asset ID
     * @param resolved Flag describing resolvedness filter:
     *                 true for "resolved orders only", false for "unresolved orders only",
     *                 null for "include both resolved and unresolved orders"
     * @return ArrayList of all sell orders for the requested asset that meet the resolvedness filter
     */
    ArrayList<SellOrder> sellOrdersByAsset(int assetID, Boolean resolved);

    /**
     *
     * @param start Start date
     * @param end End date
     * @param resolved Flag describing resolvedness filter:
     *                 true for "resolved orders only", false for "unresolved orders only",
     *                 null for "include both resolved and unresolved orders"
     * @return ArrayList of all sell orders with DatePlaced in requested range that meet the resolvedness filter
     */
    ArrayList<SellOrder> sellOrdersPlacedBetween(Timestamp start, Timestamp end, Boolean resolved);

    /**
     *
     * @param start Start date
     * @param end End date
     * @return ArrayList of all sell orders with DateResolved in requested range
     */
    ArrayList<SellOrder> sellOrdersResolvedBetween(Timestamp start, Timestamp end);

    /**
     *
     * @param start Start date
     * @param end End date
     * @return ArrayList of all sell orders that were involved in transactions during the requested range
     * i.e. these sell orders are listed as BoughtFrom by buy orders resolved in that range, even if their quantity
     * was not reduced to zero by the transaction.
     * The results of this query are a superset to sellOrdersResolvedBetween(start, end)
     */
    ArrayList<SellOrder> sellOrdersReconciledBetween(Timestamp start, Timestamp end);

    /**
     *
     * @param username a username
     * @param resolved Flag describing resolvedness filter:
     *                 true for "resolved orders only", false for "unresolved orders only",
     *                 null for "include both resolved and unresolved orders"
     * @return ArrayList of all buy orders placed by the requested user that meet the resolvedness filter
     */
    ArrayList<BuyOrder> buyOrdersByUser(String username, Boolean resolved);

    /**
     *
     * @param assetID an asset ID
     * @param resolved Flag describing resolvedness filter:
     *                 true for "resolved orders only", false for "unresolved orders only",
     *                 null for "include both resolved and unresolved orders"
     * @return ArrayList of all buy orders for the requested asset that meet the resolvedness filter
     */
    ArrayList<BuyOrder> buyOrdersByAsset(int assetID, Boolean resolved);

    /**
     *
     * @param assetID an asset ID
     * @param start Start date
     * @param end End date
     * @return ArrayList of all buy orders for the asset in question with DateResolved in requested range
     */
    ArrayList<BuyOrder> buyOrdersByAssetResolvedBetween(int assetID, Timestamp start, Timestamp end);

    /**
     *
     * @param start Start date
     * @param end End date
     * @param resolved Flag describing resolvedness filter:
     *                 true for "resolved orders only", false for "unresolved orders only",
     *                 null for "include both resolved and unresolved orders"
     * @return ArrayList of all buy orders with DatePlaced in requested range that meet the resolvedness filter
     */
    ArrayList<BuyOrder> buyOrdersPlacedBetween(Timestamp start, Timestamp end, Boolean resolved);

    /**
     *
     * @param start Start date
     * @param end End date
     * @return ArrayList of all buy orders with DateResolved in requested range
     */
    ArrayList<BuyOrder> buyOrdersResolvedBetween(Timestamp start, Timestamp end);

    //--Calling selectByValue
    ArrayList<User> usersByUnit(String unit);

    ArrayList<InventoryRecord> inventoriesByUnit(String unit);

    ArrayList<InventoryRecord> inventoriesByAsset(int asset);

    ArrayList<BuyOrder> buyOrdersByBoughtFrom(int sellOrderID);

    //--Calling selectByKey
    User userByKey(String name);

    OrgUnit unitByKey(String name);

    Asset assetByKey(int ID);

    SellOrder sellOrderByKey(int ID);

    BuyOrder buyOrderByKey(int ID);

    //---INSERT---

    /**
     *
     * @param i inventoryrecord object to send
     * @return 1 if insert succeeded, 2 if insert failed but update succeeded,
     * -1 if query failed due to nonexistence of unit or asset
     */
    int insertOrUpdateInventory(InventoryRecord i);

    /**
     *
     * @param u user object to send
     * @return 1 if insert succeeded, 0 if insert failed due to duplicate key,
     * -1 if insert failed due to nonexistence of unit
     */
    int insertUser(User u);

    /**
     *
     * @param u orgunit object to send
     * @return 1 if insert succeeded, 0 if insert failed
     */
    int insertUnit(OrgUnit u);

    /**
     *
     * @param a asset object to send
     * @return 1 if insert succeeded, 0 if insert failed
     */
    int insertAsset(Asset a);

    /**
     *
     * @param s sellorder object to send
     * @return 1 if insert succeeded, 0 if insert failed due to duplicate key (should never happen though),
     * -1 if insert failed due to nonexistence of asset or user
     */
    int insertSellOrder(SellOrder s);

    /**
     *
     * @param b buyorder object to send
     * @return 1 if insert succeeded, 0 if insert failed (should never happen though),
     * -1 if insert failed due to nonexistence of asset or user
     */
    int insertBuyOrder(BuyOrder b);

    //---UPDATE---

    /**
     *
     * @param u user object to send
     * @return 1 if update succeeded, 0 if update failed due to nonexistent key,
     * -1 if update failed due to nonexistence of unit, or an attempt to set the unit null when orders exist
     */
    int updateUser(User u);

    /**
     *
     * @param u orgunit object to send
     * @return 1 if update succeeded, 0 if update failed due to nonexistent key
     */
    int updateUnit(OrgUnit u);

    /**
     *
     * @param a asset object to send
     * @return 1 if update succeeded, 0 if update failed due to nonexistent key
     */
    int updateAsset(Asset a);

    /**
     *
     * @param s sellorder object to send
     * @return 1 if update succeeded, 0 if update failed due to nonexistent key,
     * -1 if update failed due to nonexistence of asset or user
     */
    int updateSellOrder(SellOrder s);

    /**
     *
     * @param b buyorder object to send
     * @return 1 if update succeeded, 0 if update failed due to nonexistent key,
     *  -1 if update failed due to nonexistence of asset or user or BoughtFrom sell order
     */
    int updateBuyOrder(BuyOrder b);

    //---DELETE---

    /**
     * Reset the data-- delete everything and reset auto-increment keys to 1. Exists for test purposes
     * @return The total number of records in all tables before the request was executed
     */
    int debugDeleteEverything();

    /**
     * Recreate the tables. Does nothing if debugDeleteEverything wasn't just called. Exists for test purposes
     */
    void recreate();
    /**
     *
     * @param unit Unit name of intended record
     * @param asset Asset ID of intended record
     * @return 1 if deletion succeeded, 0 if deletion failed
     */
    int deleteInventoryRecord(String unit, int asset);

    /**
     *
     * @param key username to delete
     * @return 1 if deletion succeeded, 0 if deletion failed due to nonexistent key,
     * -1 if deletion was prevented by constraints
     * (i.e. if buy orders or sell orders placed by the user exist)
     */
    int deleteUser(String key);

    /**
     *
     * @param key name to delete
     * @return 1 if deletion succeeded, 0 if deletion failed due to nonexistent key,
     * -1 if deletion was prevented by constraints
     * (i.e. if any members of the unit have buy orders or sell orders)
     */
    int deleteUnit(String key);

    /**
     *
     * @param key asset ID to delete
     * @return 1 if deletion succeeded, 0 if deletion failed due to nonexistent key,
     * -1 if deletion was prevented by constraints
     * (i.e. if the deletion of any SellOrders for this asset was prevented by constraints)
     */
    int deleteAsset(int key);

    /**
     *
     * @param key order ID to delete
     * @return 1 if deletion succeeded, 0 if deletion failed due to nonexistent key
     */
    int deleteBuyOrder(int key);

    /**
     *
     * @param key order ID to delete
     * @return 1 if deletion succeeded, 0 if deletion failed due to nonexistent key,
     * -1 if deletion was prevented by constraints
     * (i.e. if any BuyOrders reference this SellOrder as BoughtFrom)
     */
    int deleteSellOrder(int key);

}
