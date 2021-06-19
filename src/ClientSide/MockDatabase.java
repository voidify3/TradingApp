package ClientSide;

import common.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Mock database used by MockDataSource to imitate the behaviour of the server
 */
class MockDatabase {
    //Stub data structures
    private ArrayList<User> allUsers = new ArrayList<>();
    //ArrayList of all Units (Objects)
    private ArrayList<OrgUnit> allUnits = new ArrayList<>();
    //ArrayList of all Assets (Objects)
    private ArrayList<Asset> allAssets = new ArrayList<>();

    private ArrayList<InventoryRecord> inventories = new ArrayList<>();

    private ArrayList<SellOrder> sellOrders = new ArrayList<>();

    private ArrayList<BuyOrder> buyOrders = new ArrayList<>();
    private static int nextAssetID = 1;
    private static int nextBuyID = 1;
    private static int nextSellID = 1;
    //check if a LocalDateTime is between two others, inclusive
    private Boolean isBetween(LocalDateTime inQuestion, LocalDateTime start, LocalDateTime end) {
        if (inQuestion == null) return false;
        return ((inQuestion.isAfter(start) || inQuestion.isEqual(start))
                && (inQuestion.isBefore(end) || inQuestion.isEqual(end)));
    }

    /**
     * Construct the mock database and reset all auto increment keys
     */
    MockDatabase() {
        nextAssetID = 1;
        nextBuyID = 1;
        nextSellID = 1;
    }


    int deleteEverything() {
        int result = allUnits.size() + allAssets.size() + allUsers.size() + inventories.size() + sellOrders.size() + buyOrders.size();
        allUsers = new ArrayList<>();
        allUnits = new ArrayList<>();
        allAssets = new ArrayList<>();
        inventories = new ArrayList<>();
        sellOrders = new ArrayList<>();
        buyOrders = new ArrayList<>();
        nextAssetID = 1;
        nextBuyID = 1;
        nextSellID = 1;
        return result;
    }

    int addUser(User data) {
        //FK check
        if (data.getUnit() != null && getUnit(data.getUnit()) == null) return -1;
        //Check for duplicates
        for (User user : allUsers) {
            if (user.getUsername().equalsIgnoreCase(data.getUsername())) {
                return 0;
            }
        }

        allUsers.add(data);
        return 1;
    }
    int addUnit(OrgUnit data) {
        //Check for duplicates
        for (OrgUnit unit : allUnits) {
            if (unit.getName().equalsIgnoreCase(data.getName())) {
                return 0;
            }
        }
        allUnits.add(data);
        return 1;
    }

    int addAsset(Asset data) {
        allAssets.add(new Asset(nextAssetID++, data.getDescription()));
        return 1;
    }

    int addSellOrder(SellOrder order) {
        //Check that the FK values exist
        if (getAsset(order.getAsset()) == null || getUnit(order.getUnit()) == null) return -1;

        order.setId(nextSellID++);
        sellOrders.add(order);
        return 1;
    }

    int addBuyOrder(BuyOrder order) {
        //Check that the FK values exist
        if (getAsset(order.getAsset()) == null || getUnit(order.getUnit()) == null ||
                (order.getBoughtFrom() != null && getSell(order.getBoughtFrom()) == null)) return -1;

        order.setId(nextBuyID++);
        buyOrders.add(order);
        return 1;
    }


    int addOrReplaceInventory(InventoryRecord data) {
        //FK checks
        if (getAsset(data.getAssetID()) == null || getUnit(data.getUnitName()) == null) return -1;

        for (int i = 0; i < inventories.size(); i++) {
            if (inventories.get(i).getUnitName().equalsIgnoreCase(data.getUnitName())
                    && inventories.get(i).getAssetID() == data.getAssetID()) {
                inventories.set(i, data);
                return 2;
            }
        }
        inventories.add(data);
        return 1;

    }

    int replaceUser(User u) {
        //constraint checks
        if (u.getUnit() != null && getUnit(u.getUnit()) == null) //the new unit does not exist
                 return -1;
        for (int i = 0; i < allUsers.size(); i++) {
            if (allUsers.get(i).getUsername().equalsIgnoreCase(u.getUsername())) {
                allUsers.set(i, u);
                return 1;
            }
        }
        return 0;
    }
    int replaceUnit(OrgUnit u) {
        for (int i = 0; i < allUnits.size(); i++) {
            if (allUnits.get(i).getName().equalsIgnoreCase(u.getName())) {
                allUnits.set(i, u);
                return 1;
            }
        }
        return 0;
    }
    int replaceAsset(Asset a ) {
        for (int i = 0; i < allAssets.size(); i++) {
            if (allAssets.get(i).getId() == a.getId()) {
                allAssets.set(i, a);
                return 1;
            }
        }
        return 0;
    }
    int replaceSellOrder(SellOrder s) {
        //FK checks
        if (getAsset(s.getAsset()) == null || getUnit(s.getUnit()) == null) return -1;

        for (int i = 0; i < sellOrders.size(); i++) {
            if (sellOrders.get(i).getId() == s.getId()) {
                sellOrders.set(i, s);
                return 1;
            }
        }
        return 0;
    }
    int replaceBuyOrder(BuyOrder b) {
        //FK checks
        if (getAsset(b.getAsset()) == null || getUnit(b.getUnit()) == null ||
                (b.getBoughtFrom() != null && getSell(b.getBoughtFrom()) == null)) return -1;

        for (int i = 0; i < buyOrders.size(); i++) {
            if (buyOrders.get(i).getId() == b.getId()) {
                buyOrders.set(i, b);
                return 1;
            }
        }
        return 0;
    }

    //DELETE METHODS---------------------------------------------
    int deleteUser(String user) {
        User toDelete = getUser(user);
        if (toDelete == null) return 0;
        //loop needed as a safety check
        while (allUsers.contains(toDelete)) {
            allUsers.remove(toDelete);
        }
        return 1;
    }

    int deleteUnit(String unit) {
        OrgUnit toDelete = getUnit(unit);
        if (toDelete == null) return 0;
        //obey dependencies: delete orders, set user values null, delete inventory records
        for (SellOrder s : sellOrdersByUnit(unit, null)) {
            cancelSellOrder(s.getId());
        }
        for (BuyOrder b : buyOrdersByUnit(unit, null)) {
            cancelBuyOrder(b.getId());
        }
        for (User u : unitMembers(unit)) {
            u.setUnit(null);
            replaceUser(u);
        }
        for (InventoryRecord i : unitInventory(unit)) {
            deleteInv(i.getUnitName(), i.getAssetID());
        }
        //loop needed as a safety check
        while (allUnits.contains(toDelete)) {
            allUnits.remove(toDelete);
        }
        return 1;
    }
    int deleteAsset(int asset) {
        Asset toDelete = getAsset(asset);
        if (toDelete == null) return 0;
        //obey dependencies: delete inventory records and orders
        ArrayList<SellOrder> sells = sellOrdersByAsset(asset, null);
        for (SellOrder s: sells) cancelSellOrder(s.getId());
        for (BuyOrder b : buyOrdersByAsset(asset, null)) cancelBuyOrder(b.getId());
        for (InventoryRecord i : assetInventory(asset)) deleteInv(i.getUnitName(), i.getAssetID());
        //loop needed as a safety check
        while (allAssets.contains(toDelete)) {
            allAssets.remove(toDelete);
        }
        return 1;
    }
    int deleteInv(String orgunit, int asset) {
        InventoryRecord toDelete = getInv(orgunit, asset);
        if (toDelete == null) return 0;
        while (inventories.contains(toDelete)) {
            inventories.remove(toDelete);
        }
        return 1;
    }
    int cancelSellOrder(int orderID) {
        SellOrder toDelete = getSell(orderID);
        if (toDelete == null) return 0;
        //obey dependencies: delete all buy orders that reference it
        for (BuyOrder b : customersOf(orderID)) {
            cancelBuyOrder(orderID);
        }
        while (sellOrders.contains(toDelete)) {
            sellOrders.remove(toDelete);
        }
        return 1;
    }
    int cancelBuyOrder(int orderID) {
        BuyOrder toDelete = getBuy(orderID);
        if (toDelete == null) return 0;
        while (buyOrders.contains(toDelete)) {
            buyOrders.remove(toDelete);
        }
        return 1;
    }



    //SELECT METHODS-----------------------------------------------------

    ArrayList<SellOrder> sellOrdersByUnit(String unitName, Boolean resolved) {
        ArrayList<SellOrder> output = new ArrayList<>();
        for (SellOrder s : sellOrders) {
            if ((resolved == null ||  (resolved == (s.getDateResolved() != null)))  //resolved matches the truth value of `s.dateResolved != null`
                    &&  (s.getUnit().equalsIgnoreCase(unitName))) {
                output.add(s);
            }
        }
        return output;
    }

    ArrayList<SellOrder> sellOrdersByAsset(int assetID, Boolean resolved) {
        ArrayList<SellOrder> output = new ArrayList<>();
        for (SellOrder s : sellOrders) {
            if ((resolved == null ||  (resolved == (s.getDateResolved() != null)))  //resolved matches the truth value of `s.dateResolved != null`
                    &&  (s.getAsset() == assetID)) {
                output.add(s);
            }
        }
        return output;
    }
    ArrayList<SellOrder> sellOrdersPlacedBetween(Timestamp start, Timestamp end, Boolean resolved) {
        ArrayList<SellOrder> output = new ArrayList<>();
        for (SellOrder s : sellOrders) {
            if ((resolved == null ||  (resolved == (s.getDateResolved() != null)))  //resolved matches the truth value of `s.dateResolved != null`
                    &&  isBetween(s.getDatePlaced(), start.toLocalDateTime(), end.toLocalDateTime())) {
                output.add(s);
            }
        }
        return output;
    }
    ArrayList<SellOrder> sellOrdersResolvedBetween(Timestamp start, Timestamp end) {
        ArrayList<SellOrder> output = new ArrayList<>();
        for (SellOrder s : sellOrders) {
            if (isBetween(s.getDateResolved(), start.toLocalDateTime(), end.toLocalDateTime())) {
                output.add(s);
            }
        }
        return output;
    }

    ArrayList<BuyOrder> buyOrdersByUnit(String unitName, Boolean resolved) {
        ArrayList<BuyOrder> output = new ArrayList<>();
        for (BuyOrder s : buyOrders) {
            if ((resolved == null ||  (resolved == (s.getDateResolved() != null)))  //resolved matches the truth value of `s.dateResolved != null`
                    &&  (s.getUnit().equalsIgnoreCase(unitName))) {
                output.add(s);
            }
        }
        return output;
    }

    ArrayList<BuyOrder> buyOrdersByAsset(int assetID, Boolean resolved) {
        ArrayList<BuyOrder> output = new ArrayList<>();
        for (BuyOrder s : buyOrders) {
            if ((resolved == null ||  (resolved == (s.getDateResolved() != null)))  //resolved matches the truth value of `s.dateResolved != null`
                    &&  (s.getAsset() == assetID)) {
                output.add(s);
            }
        }
        return output;
    }

    ArrayList<BuyOrder> buyOrdersByAssetResolvedBetween(int assetID, Timestamp start, Timestamp end) {
        ArrayList<BuyOrder> output = new ArrayList<>();
        for (BuyOrder s : buyOrders) {
            if (isBetween(s.getDateResolved(), start.toLocalDateTime(), end.toLocalDateTime())
                    &&  (s.getAsset() == assetID)) {
                output.add(s);
            }
        }
        return output;
    }

    ArrayList<BuyOrder> buyOrdersPlacedBetween(Timestamp start, Timestamp end, Boolean resolved) {
        ArrayList<BuyOrder> output = new ArrayList<>();
        for (BuyOrder s : buyOrders) {
            if ((resolved == null ||  (resolved == (s.getDateResolved() != null))) //resolved matches the truth value of `s.dateResolved != null`
                    &&  isBetween(s.getDatePlaced(), start.toLocalDateTime(), end.toLocalDateTime())) {
                output.add(s);
            }
        }
        return output;
    }

    ArrayList<BuyOrder> buyOrdersResolvedBetween(Timestamp start, Timestamp end) {
        ArrayList<BuyOrder> output = new ArrayList<>();
        for (BuyOrder s : buyOrders) {
            if (isBetween(s.getDateResolved(), start.toLocalDateTime(), end.toLocalDateTime())) {
                output.add(s);
            }
        }
        return output;
    }

    ArrayList<User> unitMembers(String unitName) {
        ArrayList<User> results = new ArrayList<>();
        for (User current : allUsers) {
            if (current.getUnit() != null && current.getUnit().equalsIgnoreCase(unitName)) {
                results.add(current);
            }
        }
        return results;
    }

    ArrayList<InventoryRecord> unitInventory(String unitName) {
        ArrayList<InventoryRecord> results = new ArrayList<>();
        for (InventoryRecord current : inventories) {
            if (current.getUnitName() == unitName) {
                results.add(current);
            }
        }
        return results;
    }

    ArrayList<InventoryRecord> assetInventory(int assetID) {
        ArrayList<InventoryRecord> results = new ArrayList<>();
        for (InventoryRecord current : inventories) {
            if (current.getAssetID() == assetID) {
                results.add(current);
            }
        }
        return results;
    }

    ArrayList<BuyOrder> customersOf(Integer sellOrderID) {
        ArrayList<BuyOrder> results = new ArrayList<>();
        for (BuyOrder current : buyOrders) {
            if (current.getBoughtFrom() != null && current.getBoughtFrom().equals(sellOrderID)) {
                results.add(current);
            }
        }
        return results;
    }

    /***
     * Get an OrgUnit object by its name
     * @param name
     * @return
     */
    OrgUnit getUnit(String name) {
        for (OrgUnit current : allUnits) {
            if (current.getName().equalsIgnoreCase(name)) {
                return current;
            }
        }
        return null;
    }
    User getUser(String name) {
        for (User current : allUsers) {
            if (current.getUsername().equalsIgnoreCase(name)) {
                return current;
            }
        }
        return null;
    }
    Asset getAsset(int assetID) {
        for (Asset current : allAssets) {
            if (current.getId() == assetID) {
                return current;
            }
        }
        return null;
    }

    InventoryRecord getInv(String unit, int asset) {
        for (InventoryRecord x : inventories) {
            if (x.getUnitName().equalsIgnoreCase(unit) && x.getAssetID() == asset) {
                return x;
            }
        }
        return null;
    }

    SellOrder getSell(int ID) {
        for (SellOrder o : sellOrders) {
            if (o.getId() == ID) {
                return o;
            }
        }
        return null;
    }

    BuyOrder getBuy(int ID) {
        for (BuyOrder o : buyOrders) {
            if (o.getId() == ID) {
                return o;
            }
        }
        return null;
    }

    ArrayList<OrgUnit> getAllUnits() {
        return allUnits;
    }

    ArrayList<User> getAllUsers() {
        return allUsers;
    }

    ArrayList<Asset> getAllAssets() {
        return allAssets;
    }

    ArrayList<InventoryRecord> getInventories() {
        return inventories;
    }
    ArrayList<SellOrder> getSellOrders() {
        return sellOrders;
    }

    ArrayList<BuyOrder> getBuyOrders() {
        return buyOrders;
    }



}

