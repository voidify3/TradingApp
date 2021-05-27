package ClientSide;

import common.*;

import java.sql.Timestamp;
import java.util.ArrayList;

public interface TradingAppDataSource {
    Boolean ping();

    ArrayList<User> allUsers();

    ArrayList<OrgUnit> allOrgUnits();

    ArrayList<Asset> allAssets();

    ArrayList<InventoryRecord> inventoryList();

    InventoryRecord inventoryRecordByKeys(String unit, int asset);

    ArrayList<SellOrder> sellOrdersByUser(String username, Boolean resolved);

    ArrayList<SellOrder> sellOrdersByAsset(int assetID, Boolean resolved);

    ArrayList<SellOrder> sellOrdersPlacedBetween(Timestamp start, Timestamp end, Boolean resolved);

    ArrayList<SellOrder> sellOrdersResolvedBetween(Timestamp start, Timestamp end);

    ArrayList<BuyOrder> buyOrdersByUser(String username, Boolean resolved);

    ArrayList<BuyOrder> buyOrdersByAsset(int assetID, Boolean resolved);

    ArrayList<BuyOrder> buyOrdersByAssetResolvedBetween(int assetID, Timestamp start, Timestamp end);

    ArrayList<BuyOrder> buyOrdersPlacedBetween(Timestamp start, Timestamp end, Boolean resolved);

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
     * @param i
     * @return 1 if insert succeeded, 2 if insert failed but update succeeded
     */
    int insertOrUpdateInventory(InventoryRecord i);

    /**
     *
     * @param u
     * @return 1 if insert succeeded, 0 if insert failed
     */
    int insertUser(User u);

    /**
     *
     * @param u
     * @return 1 if insert succeeded, 0 if insert failed
     */
    int insertUnit(OrgUnit u);

    /**
     *
     * @param a
     * @return 1 if insert succeeded, 0 if insert failed
     */
    int insertAsset(Asset a);

    /**
     *
     * @param s
     * @return 1 if insert succeeded, 0 if insert failed
     */
    int insertSellOrder(SellOrder s);

    /**
     *
     * @param b
     * @return 1 if insert succeeded, 0 if insert failed
     */
    int insertBuyOrder(BuyOrder b);

    //---UPDATE---

    /**
     *
     * @param u
     * @return 1 if update succeeded, 0 if update failed
     */
    int updateUser(User u);

    /**
     *
     * @param u
     * @return 1 if update succeeded, 0 if update failed
     */
    int updateUnit(OrgUnit u);

    /**
     *
     * @param a
     * @return 1 if update succeeded, 0 if update failed
     */
    int updateAsset(Asset a);

    /**
     *
     * @param s
     * @return 1 if update succeeded, 0 if update failed
     */
    int updateSellOrder(SellOrder s);

    /**
     *
     * @param b
     * @return 1 if update succeeded, 0 if update failed
     */
    int updateBuyOrder(BuyOrder b);

    //---DELETE---
    int debugDeleteEverything();

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
     * @return 1 if deletion succeeded, 0 if deletion failed, -1 if deletion was prevented by constraints
     * (i.e. if buy orders or sell orders placed by the user exist)
     */
    int deleteUser(String key);

    /**
     *
     * @param key
     * @return 1 if deletion succeeded, 0 if deletion failed
     */
    int deleteUnit(String key);

    /**
     *
     * @param key
     * @return 1 if deletion succeeded, 0 if deletion failed, -1 if deletion was prevented by constraints
     * (i.e. if the deletion of any SellOrders for this asset was prevented by constraints)
     */
    int deleteAsset(int key);

    /**
     *
     * @param key
     * @return 1 if deletion succeeded, 0 if deletion failed
     */
    int deleteBuyOrder(int key);

    /**
     *
     * @param key
     * @return 1 if deletion succeeded, 0 if deletion failed, -1 if deletion was prevented by constraints
     * (i.e. if any BuyOrders reference this SellOrder as BoughtFrom)
     */
    int deleteSellOrder(int key);
}
