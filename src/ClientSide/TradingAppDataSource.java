package ClientSide;

import common.*;

import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * Abstract class defining the operations of a data source for the app.
 */
abstract class TradingAppDataSource {
    int refreshDelay = 0;
    /**
     * Get all users
     * @return ArrayList of all users
     */
    abstract ArrayList<User> allUsers();

    /**
     * Get all OrgUnits
     * @return ArrayList of all OrgUnits
     */
    abstract ArrayList<OrgUnit> allOrgUnits();

    /**
     * Get all assets
     * @return ArrayList of all assets
     */
    abstract ArrayList<Asset> allAssets();

    /**
     * Get all inventory records
     * @return ArrayList of all inventory records
     */
    abstract ArrayList<InventoryRecord> allInventories();

    /**
     * Get all sell orders
     * @return ArrayList of all sell orders
     */
    abstract ArrayList<SellOrder> allSellOrders();

    /**
     * Get all buy orders
     * @return ArrayList of all buy orders
     */
    abstract ArrayList<BuyOrder> allBuyOrders();

    /**
     * Get all sell orders that meet the resolvedness filter
     * @param resolved True to get only resolved orders, false to get only unresolved orders, null equivalent to no param
     * @return ArrayList of all sell orders that meet the resolvedness filter
     */
    abstract ArrayList<SellOrder> allSellOrders(Boolean resolved);
    /**
     * Get all buy orders that meet the resolvedness filter
     * @param resolved True to get only resolved orders, false to get only unresolved orders, null equivalent to no param version
     * @return ArrayList of all buy orders that meet the resolvedness filter
     */
    abstract ArrayList<BuyOrder> allBuyOrders(Boolean resolved);
    /**
     * Get The InventoryRecord object with the requested keys
     * @param unit OrgUnit component of the composite key
     * @param asset Asset component of the composite key
     * @return The InventoryRecord object with the requested keys
     */
    abstract InventoryRecord inventoryRecordByKeys(String unit, int asset);
    /**
     * Get all sell orders placed by members of the requested unit that meet the resolvedness filter
     * @param unitName a username
     * @param resolved Flag describing resolvedness filter:
     *                 true for "resolved orders only", false for "unresolved orders only",
     *                 null for "include both resolved and unresolved orders"
     * @return ArrayList of all sell orders placed by members of the requested unit that meet the resolvedness filter
     */
    abstract ArrayList<SellOrder> sellOrdersByUnit(String unitName, Boolean resolved);

    /**
     * Get all sell orders for the requested asset that meet the resolvedness filter
     * @param assetID an asset ID
     * @param resolved Flag describing resolvedness filter:
     *                 true for "resolved orders only", false for "unresolved orders only",
     *                 null for "include both resolved and unresolved orders"
     * @return ArrayList of all sell orders for the requested asset that meet the resolvedness filter
     */
    abstract ArrayList<SellOrder> sellOrdersByAsset(int assetID, Boolean resolved);

    /**
     * Get all sell orders with DatePlaced in requested range that meet the resolvedness filter
     * @param start Start date
     * @param end End date
     * @param resolved Flag describing resolvedness filter:
     *                 true for "resolved orders only", false for "unresolved orders only",
     *                 null for "include both resolved and unresolved orders"
     * @return ArrayList of all sell orders with DatePlaced in requested range that meet the resolvedness filter
     */
    abstract ArrayList<SellOrder> sellOrdersPlacedBetween(Timestamp start, Timestamp end, Boolean resolved);

    /**
     * Get all sell orders with DateResolved in requested range
     * @param start Start date
     * @param end End date
     * @return ArrayList of all sell orders with DateResolved in requested range
     */
    abstract ArrayList<SellOrder> sellOrdersResolvedBetween(Timestamp start, Timestamp end);

    /**
     * Get all sell orders that were involved in transactions during the requested range
     * @param start Start date
     * @param end End date
     * @return ArrayList of all sell orders that were involved in transactions during the requested range
     * i.e. these sell orders were resolved in the range || are listed as BoughtFrom by buy orders resolved in that range
     * (both checks are needed because a sell order whose quantity was not reduced to 0 by the transaction will only be the latter
     * and a sell order whose quantity was reduced to 0 by a since-deleted buy order will only be the former)
     * The results of this query are a superset to sellOrdersResolvedBetween(start, end)
     */
    abstract ArrayList<SellOrder> sellOrdersReconciledBetween(Timestamp start, Timestamp end);

    /**
     * Get all buy orders placed by members of the requested unit that meet the resolvedness filter
     * @param unitName the name of an organisational unit
     * @param resolved Flag describing resolvedness filter:
     *                 true for "resolved orders only", false for "unresolved orders only",
     *                 null for "include both resolved and unresolved orders"
     * @return ArrayList of all buy orders placed by members of the requested unit that meet the resolvedness filter
     */
    abstract ArrayList<BuyOrder> buyOrdersByUnit(String unitName, Boolean resolved);

    /**
     * Get all buy orders for the requested asset that meet the resolvedness filter
     * @param assetID an asset ID
     * @param resolved Flag describing resolvedness filter:
     *                 true for "resolved orders only", false for "unresolved orders only",
     *                 null for "include both resolved and unresolved orders"
     * @return ArrayList of all buy orders for the requested asset that meet the resolvedness filter
     */
    abstract ArrayList<BuyOrder> buyOrdersByAsset(int assetID, Boolean resolved);

    /**
     * Get all buy orders for the asset in question with DateResolved in requested range
     * @param assetID an asset ID
     * @param start Start date
     * @param end End date
     * @return ArrayList of all buy orders for the asset in question with DateResolved in requested range
     */
    abstract ArrayList<BuyOrder> buyOrdersByAssetResolvedBetween(int assetID, Timestamp start, Timestamp end);

    /**
     * Get all buy orders with DatePlaced in requested range that meet the resolvedness filter
     * @param start Start date
     * @param end End date
     * @param resolved Flag describing resolvedness filter:
     *                 true for "resolved orders only", false for "unresolved orders only",
     *                 null for "include both resolved and unresolved orders"
     * @return ArrayList of all buy orders with DatePlaced in requested range that meet the resolvedness filter
     */
    abstract ArrayList<BuyOrder> buyOrdersPlacedBetween(Timestamp start, Timestamp end, Boolean resolved);

    /**
     * Get all buy orders with DateResolved in requested range
     * @param start Start date
     * @param end End date
     * @return ArrayList of all buy orders with DateResolved in requested range
     */
    abstract ArrayList<BuyOrder> buyOrdersResolvedBetween(Timestamp start, Timestamp end);

    /**
     * Get all users with a specific organisational unit
     * @param unit Org unit name
     * @return ArrayList of all relevant User objects
     */
    abstract ArrayList<User> usersByUnit(String unit);
    /**
     * Get all inventory records with a specific organisational unit
     * @param unit Org unit name
     * @return ArrayList of all relevant InventoryRecord objects
     */
    abstract ArrayList<InventoryRecord> inventoriesByUnit(String unit);

    /**
     * Get all inventory records with a specific asset
     * @param asset Asset ID
     * @return ArrayList of all inventory records with a specific asset
     */
    abstract ArrayList<InventoryRecord> inventoriesByAsset(int asset);

    /**
     * Get all buy orders
     * @param sellOrderID
     * @return
     */
    abstract ArrayList<BuyOrder> buyOrdersByBoughtFrom(int sellOrderID);

    /**
     * Get user with the specified name
     * @param name Username
     * @return User object with the specified name, or null if no such user exists
     */
    abstract User userByKey(String name);
    /**
     * Get unit with the specified name
     * @param name Name
     * @return OrgUnit object with the specified name, or null if no such unit exists
     */
    abstract OrgUnit unitByKey(String name);

    /**
     * Get asset with the specified ID
     * @param ID Asset ID number
     * @return Matching Asset object, or null if nonexistent
     */
    abstract Asset assetByKey(int ID);
    /**
     * Get SellOrder with the specified ID
     * @param ID SellOrder ID number
     * @return Matching Sellorder object, or null if nonexistent
     */
    abstract SellOrder sellOrderByKey(int ID);
    /**
     * Get BuyOrder with the specified ID
     * @param ID BuyOrder ID number
     * @return Matching BuyOrder object, or null if nonexistent
     */
    abstract BuyOrder buyOrderByKey(int ID);

    //---INSERT---

    /**
     * Insert an inventory record, overwriting any existing inventory record for the same unit and asset.
     * Fails if the unit string or asset ID of the record don't match any real unit or asset
     * @param i inventoryrecord object to send
     * @return 1 if insert succeeded, 2 if insert failed but update succeeded,
     * -1 if query failed due to nonexistence of unit or asset
     */
    abstract int insertOrUpdateInventory(InventoryRecord i);

    /**
     * Insert a user; fails if the username already exists or the user's unit string isn't a real org unit
     * @param u user object to send
     * @return 1 if insert succeeded, 0 if insert failed due to duplicate key,
     * -1 if insert failed due to nonexistence of unit
     */
    abstract int insertUser(User u);

    /**
     * Insert a unit; fails if the name already exists
     * @param u orgunit object to send
     * @return 1 if insert succeeded, 0 if insert failed
     */
    abstract int insertUnit(OrgUnit u);

    /**
     * Insert an asset; fails if the ID already exists (though this should never happen)
     * @param a asset object to send
     * @return 1 if insert succeeded, 0 if insert failed
     */
    abstract int insertAsset(Asset a);

    /**
     * Insert a sellorder; fails if the ID already exists (though this should never happen) or if the
     * unit or asset do not exist
     * @param s sellorder object to send
     * @return 1 if insert succeeded, 0 if insert failed due to duplicate key (should never happen though),
     * -1 if insert failed due to nonexistence of asset or unit
     */
    abstract int insertSellOrder(SellOrder s);

    /**
     * Insert a buyorder; fails if the ID already exists (though this should never happen) or if the
     * unit or asset do not exist
     * @param b buyorder object to send
     * @return 1 if insert succeeded, 0 if insert failed (should never happen though),
     * -1 if insert failed due to nonexistence of asset or unit
     */
    abstract int insertBuyOrder(BuyOrder b);

    //---UPDATE---

    /**
     * Update a user; fails if the username does not exist in the database or if the unit string does not exist
     * @param u user object to send
     * @return 1 if update succeeded, 0 if update failed due to nonexistent key,
     * -1 if update failed due to nonexistence of unit
     */
    abstract int updateUser(User u);

    /**
     * Update a unit; fails if the name does not exist in the database
     * @param u orgunit object to send
     * @return 1 if update succeeded, 0 if update failed due to nonexistent key
     */
    abstract int updateUnit(OrgUnit u);

    /**
     * Update an asset; fails if the ID does not exist in the database
     * @param a asset object to send
     * @return 1 if update succeeded, 0 if update failed due to nonexistent key
     */
    abstract int updateAsset(Asset a);

    /**
     * Update a sell order; fails if the ID does not exist or if the
     * unit or asset do not exist
     * @param s sellorder object to send
     * @return 1 if update succeeded, 0 if update failed due to nonexistent key,
     * -1 if update failed due to nonexistence of asset or unit
     */
    abstract int updateSellOrder(SellOrder s);

    /**
     * Update a buy order; fails if the ID does not exist or if the
     * unit or asset or boughtFrom sell order do not exist
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
     * Delete an inventory record by its composite key
     * @param unit Unit name of intended record
     * @param asset Asset ID of intended record
     * @return 1 if deletion succeeded, 0 if deletion failed
     */
    abstract int deleteInventoryRecord(String unit, int asset);

    /**
     * Delete a user by their key
     * @param key username to delete
     * @return 1 if deletion succeeded, 0 if deletion failed due to nonexistent key
     */
    abstract int deleteUser(String key);

    /**
     * Delete a unit by its key
     * @param key name to delete
     * @return 1 if deletion succeeded, 0 if deletion failed
     */
    abstract int deleteUnit(String key);

    /**
     * Delete an asset by its key
     * @param key asset ID to delete
     * @return 1 if deletion succeeded, 0 if deletion failed
     */
    abstract int deleteAsset(int key);

    /**
     * Delete a buy order by its key
     * @param key order ID to delete
     * @return 1 if deletion succeeded, 0 if deletion failed due to nonexistent key
     */
    abstract int deleteBuyOrder(int key);

    /**
     * Delete a sell order by its key
     * @param key order ID to delete
     * @return 1 if deletion succeeded, 0 if deletion failed due to nonexistent key
     */
    abstract int deleteSellOrder(int key);

}
