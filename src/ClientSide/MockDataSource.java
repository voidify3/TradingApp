package ClientSide;

import ServerSide.MockDatabase;
import common.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

public class MockDataSource implements TradingAppDataSource {
    MockDatabase db;

    public MockDataSource() {
        db = new MockDatabase();
    }

    @Override
    public ArrayList<User> allUsers() {
        return db.getAllUsers();
    }

    @Override
    public ArrayList<OrgUnit> allOrgUnits() {
        return db.getAllUnits();
    }

    @Override
    public ArrayList<Asset> allAssets() {
        return db.getAllAssets();
    }

    @Override
    public ArrayList<InventoryRecord> inventoryList() {
        return db.getInventories();
    }

    @Override
    public ArrayList<SellOrder> allSellOrders() {
        return db.getSellOrders();
    }

    @Override
    public ArrayList<BuyOrder> allBuyOrders() {
        return db.getBuyOrders();
    }

    @Override
    public InventoryRecord inventoryRecordByKeys(String unit, int asset) {
        return db.getInv(unit, asset);
    }

    @Override
    public ArrayList<SellOrder> allSellOrders(Boolean resolved) {
        ArrayList<SellOrder> output = new ArrayList<>();
        for (SellOrder s : db.getSellOrders()) {
            if (resolved ==(s.getDateResolved() == null)) output.add(s);
        }
        return  output;
    }

    @Override
    public ArrayList<BuyOrder> allBuyOrders(Boolean resolved) {
        ArrayList<BuyOrder> output = new ArrayList<>();
        for (BuyOrder s : db.getBuyOrders()) {
            if (resolved ==(s.getDateResolved() == null)) output.add(s);
        }
        return  output;
    }

    @Override
    public ArrayList<SellOrder> sellOrdersByUser(String username, Boolean resolved) {
        return db.sellOrdersByUser(username, resolved);
    }

    @Override
    public ArrayList<SellOrder> sellOrdersByAsset(int assetID, Boolean resolved) {
        return db.sellOrdersByAsset(assetID, resolved);
    }

    @Override
    public ArrayList<SellOrder> sellOrdersPlacedBetween(Timestamp start, Timestamp end, Boolean resolved) {
        return db.sellOrdersPlacedBetween(start, end, resolved);
    }

    @Override
    public ArrayList<SellOrder> sellOrdersResolvedBetween(Timestamp start, Timestamp end) {
        return db.sellOrdersResolvedBetween(start, end);
    }

    @Override
    public ArrayList<SellOrder> sellOrdersReconciledBetween(Timestamp start, Timestamp end) {
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
    public ArrayList<BuyOrder> buyOrdersByUser(String username, Boolean resolved) {
        return db.buyOrdersByUser(username, resolved);
    }

    @Override
    public ArrayList<BuyOrder> buyOrdersByAsset(int assetID, Boolean resolved) {
       return db.buyOrdersByAsset(assetID, resolved);
    }

    @Override
    public ArrayList<BuyOrder> buyOrdersByAssetResolvedBetween(int assetID, Timestamp start, Timestamp end) {
        return db.buyOrdersByAssetResolvedBetween(assetID, start, end);
    }

    @Override
    public ArrayList<BuyOrder> buyOrdersPlacedBetween(Timestamp start, Timestamp end, Boolean resolved) {
        return db.buyOrdersPlacedBetween(start, end, resolved);
    }

    @Override
    public ArrayList<BuyOrder> buyOrdersResolvedBetween(Timestamp start, Timestamp end) {
        return db.buyOrdersResolvedBetween(start, end);
    }

    @Override
    public ArrayList<User> usersByUnit(String unit) {
        return db.unitMembers(unit);
    }

    @Override
    public ArrayList<InventoryRecord> inventoriesByUnit(String unit) {
        return db.unitInventory(unit);
    }

    @Override
    public ArrayList<InventoryRecord> inventoriesByAsset(int asset) {
        return db.assetInventory(asset);
    }

    @Override
    public ArrayList<BuyOrder> buyOrdersByBoughtFrom(int sellOrderID) {
        return db.customersOf(sellOrderID);
    }

    @Override
    public User userByKey(String name) {
        return db.getUser(name);
    }

    @Override
    public OrgUnit unitByKey(String name) {
        return db.getUnit(name);
    }

    @Override
    public Asset assetByKey(int ID) {
        return db.getAsset(ID);
    }

    @Override
    public SellOrder sellOrderByKey(int ID) {
        return db.getSell(ID);
    }

    @Override
    public BuyOrder buyOrderByKey(int ID) {
        return db.getBuy(ID);
    }

    @Override
    public int insertOrUpdateInventory(InventoryRecord i) {
        return db.addOrReplaceInventory(i);
    }

    @Override
    public int insertUser(User u) {
        return db.addUser(u);
    }

    @Override
    public int insertUnit(OrgUnit u) {
        return db.addUnit(u);
    }

    @Override
    public int insertAsset(Asset a) {
        return db.addAsset(a);
    }

    @Override
    public int insertSellOrder(SellOrder s) {
        return db.addSellOrder(s);
    }

    @Override
    public int insertBuyOrder(BuyOrder b) {
        return db.addBuyOrder(b);
    }

    @Override
    public int updateUser(User u) {
        return db.replaceUser(u);
    }

    @Override
    public int updateUnit(OrgUnit u) {
        return db.replaceUnit(u);
    }

    @Override
    public int updateAsset(Asset a) {
        return db.replaceAsset(a);
    }

    @Override
    public int updateSellOrder(SellOrder s) {
        return db.replaceSellOrder(s);
    }

    @Override
    public int updateBuyOrder(BuyOrder b) {
        return db.replaceBuyOrder(b);
    }

    @Override
    public int debugDeleteEverything() {
        return db.deleteEverything();
    }

    @Override
    public int deleteInventoryRecord(String unit, int asset) {
        return db.deleteInv(unit, asset);
    }

    @Override
    public int deleteUser(String key) {
        return db.deleteUser(key);
    }

    @Override
    public int deleteUnit(String key) {
        return db.deleteUnit(key);
    }

    @Override
    public int deleteAsset(int key) {
        return db.deleteAsset(key);
    }

    @Override
    public int deleteBuyOrder(int key) {
        return db.cancelBuyOrder(key);
    }

    @Override
    public int deleteSellOrder(int key) {
        return db.cancelSellOrder(key);
    }

    @Override
    public void recreate() {

    }
}
