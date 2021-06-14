package ClientSide;

import common.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

class MockDataSource extends TradingAppDataSource {
    MockDatabase db;

    MockDataSource() {
        db = new MockDatabase();
    }

    @Override
    ArrayList<User> allUsers() {
        return db.getAllUsers();
    }

    @Override
    ArrayList<OrgUnit> allOrgUnits() {
        return db.getAllUnits();
    }

    @Override
    ArrayList<Asset> allAssets() {
        return db.getAllAssets();
    }

    @Override
    ArrayList<InventoryRecord> inventoryList() {
        return db.getInventories();
    }

    @Override
    ArrayList<SellOrder> allSellOrders() {
        return db.getSellOrders();
    }

    @Override
    ArrayList<BuyOrder> allBuyOrders() {
        return db.getBuyOrders();
    }

    @Override
    InventoryRecord inventoryRecordByKeys(String unit, int asset) {
        return db.getInv(unit, asset);
    }

    @Override
    ArrayList<SellOrder> allSellOrders(Boolean resolved) {
        ArrayList<SellOrder> output = new ArrayList<>();
        for (SellOrder s : db.getSellOrders()) {
            if (resolved==null || resolved ==(s.getDateResolved() != null)) output.add(s);
        }
        return  output;
    }

    @Override
    ArrayList<BuyOrder> allBuyOrders(Boolean resolved) {
        ArrayList<BuyOrder> output = new ArrayList<>();
        for (BuyOrder s : db.getBuyOrders()) {
            if (resolved==null || resolved ==(s.getDateResolved() != null)) output.add(s);
        }
        return  output;
    }

    @Override
    ArrayList<SellOrder> sellOrdersByUnit(String unitName, Boolean resolved) {
        return db.sellOrdersByUnit(unitName, resolved);
    }

    @Override
    ArrayList<SellOrder> sellOrdersByAsset(int assetID, Boolean resolved) {
        return db.sellOrdersByAsset(assetID, resolved);
    }

    @Override
    ArrayList<SellOrder> sellOrdersPlacedBetween(Timestamp start, Timestamp end, Boolean resolved) {
        return db.sellOrdersPlacedBetween(start, end, resolved);
    }

    @Override
    ArrayList<SellOrder> sellOrdersResolvedBetween(Timestamp start, Timestamp end) {
        return db.sellOrdersResolvedBetween(start, end);
    }

    @Override
    ArrayList<SellOrder> sellOrdersReconciledBetween(Timestamp start, Timestamp end) {
        ArrayList<BuyOrder> b = buyOrdersResolvedBetween(start, end);
        //using a set because they ignore duplicates
        Set<Integer> ids = new TreeSet<>();
        for (BuyOrder o : b) {
            ids.add(o.getBoughtFrom());
        }
        ArrayList<SellOrder> output = new ArrayList<>();
        for (int i : ids) {
            output.add(sellOrderByKey(i));
        }
        return output;
    }

    @Override
    ArrayList<BuyOrder> buyOrdersByUnit(String unitName, Boolean resolved) {
        return db.buyOrdersByUnit(unitName, resolved);
    }

    @Override
    ArrayList<BuyOrder> buyOrdersByAsset(int assetID, Boolean resolved) {
       return db.buyOrdersByAsset(assetID, resolved);
    }

    @Override
    ArrayList<BuyOrder> buyOrdersByAssetResolvedBetween(int assetID, Timestamp start, Timestamp end) {
        return db.buyOrdersByAssetResolvedBetween(assetID, start, end);
    }

    @Override
    ArrayList<BuyOrder> buyOrdersPlacedBetween(Timestamp start, Timestamp end, Boolean resolved) {
        return db.buyOrdersPlacedBetween(start, end, resolved);
    }

    @Override
    ArrayList<BuyOrder> buyOrdersResolvedBetween(Timestamp start, Timestamp end) {
        return db.buyOrdersResolvedBetween(start, end);
    }

    @Override
    ArrayList<User> usersByUnit(String unit) {
        return db.unitMembers(unit);
    }

    @Override
    ArrayList<InventoryRecord> inventoriesByUnit(String unit) {
        return db.unitInventory(unit);
    }

    @Override
    ArrayList<InventoryRecord> inventoriesByAsset(int asset) {
        return db.assetInventory(asset);
    }

    @Override
    ArrayList<BuyOrder> buyOrdersByBoughtFrom(int sellOrderID) {
        return db.customersOf(sellOrderID);
    }

    @Override
    User userByKey(String name) {
        return db.getUser(name);
    }

    @Override
    OrgUnit unitByKey(String name) {
        return db.getUnit(name);
    }

    @Override
    Asset assetByKey(int ID) {
        return db.getAsset(ID);
    }

    @Override
    SellOrder sellOrderByKey(int ID) {
        return db.getSell(ID);
    }

    @Override
    BuyOrder buyOrderByKey(int ID) {
        return db.getBuy(ID);
    }

    @Override
    int insertOrUpdateInventory(InventoryRecord i) {
        return db.addOrReplaceInventory(i);
    }

    @Override
    int insertUser(User u) {
        return db.addUser(u);
    }

    @Override
    int insertUnit(OrgUnit u) {
        return db.addUnit(u);
    }

    @Override
    int insertAsset(Asset a) {
        return db.addAsset(a);
    }

    @Override
    int insertSellOrder(SellOrder s) {
        return db.addSellOrder(s);
    }

    @Override
    int insertBuyOrder(BuyOrder b) {
        return db.addBuyOrder(b);
    }

    @Override
    int updateUser(User u) {
        return db.replaceUser(u);
    }

    @Override
    int updateUnit(OrgUnit u) {
        return db.replaceUnit(u);
    }

    @Override
    int updateAsset(Asset a) {
        return db.replaceAsset(a);
    }

    @Override
    int updateSellOrder(SellOrder s) {
        return db.replaceSellOrder(s);
    }

    @Override
    int updateBuyOrder(BuyOrder b) {
        return db.replaceBuyOrder(b);
    }

    @Override
    int debugDeleteEverything() {
        return db.deleteEverything();
    }

    @Override
    int deleteInventoryRecord(String unit, int asset) {
        return db.deleteInv(unit, asset);
    }

    @Override
    int deleteUser(String key) {
        return db.deleteUser(key);
    }

    @Override
    int deleteUnit(String key) {
        return db.deleteUnit(key);
    }

    @Override
    int deleteAsset(int key) {
        return db.deleteAsset(key);
    }

    @Override
    int deleteBuyOrder(int key) {
        return db.cancelBuyOrder(key);
    }

    @Override
    int deleteSellOrder(int key) {
        return db.cancelSellOrder(key);
    }

    @Override
    void recreate() {

    }
}
