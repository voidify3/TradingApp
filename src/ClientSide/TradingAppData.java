package ClientSide;

import common.*;
import common.Exceptions.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;

/**
 * Middle end of the client program; owns a data source and is owned by the GUI
 */
class TradingAppData {
    private final TradingAppDataSource dataSource;
    private static final Locale locale = Locale.ENGLISH;

    /**
     * Used for getHistoricalPrices
     */
    enum Intervals {
        DAYS(),
        WEEKS(),
        MONTHS(),
        YEARS()
    }

    /**
     * Initialise the data interface for the given data source
     * @param dataSource either of the TradingAppDataSource subtypes
     */
    TradingAppData(TradingAppDataSource dataSource) {
        this.dataSource = dataSource;
    }

    //DEV/HELPER CONTENT --------------------------------------------------------------------
    static User adminDev;
    static User userDev;
    static User userDev2;
    static User userDev3;
    static User userDev4;
    static OrgUnit unitDev;
    static OrgUnit unitDev2;
    static Asset assetDev1;
    static Asset assetDev2;
    static BuyOrder testBuyOrder;
    static BuyOrder testBuyOrder2;
    static SellOrder testSellOrder;
    static SellOrder testSellOrder2;


    /**
     * Add a dummy "historical" transaction
     * @param idUpTo ID to use for the buy order's BoughtFrom, assumed to be the sell ID
     * @param assetID Asset for the transaction
     * @param unitResponsible Unit to attach to both the buy and sell order
     * @param price Price to put for both orders
     * @param dateTime Datetime to put for both orders' dates placed and resolved
     */
    void addHistoricalPrice(int idUpTo, int assetID, String unitResponsible, int price, LocalDateTime dateTime) {
        SellOrder sell = new SellOrder(0, unitResponsible, assetID, 1, price, dateTime, dateTime);
        BuyOrder buy = new BuyOrder(0, unitResponsible, assetID, 1, price, dateTime, dateTime, idUpTo);
        dataSource.insertSellOrder(sell);
        dataSource.insertBuyOrder(buy);

    }

    /**
     * Populate test data, plus historical prices for two assets going back a specified number of days
     * starting at buy and sell ID 3
     * @param numdays How many days back to do the prices
     */
    void mockObjectsWithPrices(int numdays) {
        mockObjects();
        LocalDateTime begin = LocalDateTime.now().minusDays(numdays);
        for (int i = 1; i <= numdays; i++) {
            LocalDateTime theDay = begin.plusDays(i);
            addHistoricalPrice(i*6-3, assetDev1.getId(), unitDev.getName(), 10, theDay);
            addHistoricalPrice(i*6-2, assetDev1.getId(), unitDev.getName(), 15, theDay);
            addHistoricalPrice(i*6-1, assetDev1.getId(), unitDev.getName(), 20, theDay);
            addHistoricalPrice(i*6, assetDev2.getId(), unitDev.getName(), 10, theDay);
            addHistoricalPrice(i*6+1, assetDev2.getId(), unitDev.getName(), 30, theDay);
            addHistoricalPrice(i*6+2, assetDev2.getId(), unitDev.getName(), 50, theDay);
        }
    }

    /**
     * Set up two users so that the master keys will work even if the client has never been run with a TESTDATA arg
     * since the database was last reset
     */
    void initialUsers() {
        try {
            adminDev = new User("sophia", "bo$$", true, null);
            userDev = new User("scott", "scotty", false, null);
            if (dataSource.insertUser(adminDev) == 0) adminDev = dataSource.userByKey("sophia");
            if (dataSource.insertUser(userDev) == 0) userDev = dataSource.userByKey("scott");
        } catch (IllegalString illegalString) {
            illegalString.printStackTrace();
        }
    }

    /**
     * Populate some test data in all tables, storing values in the class's static variables
     */
    void mockObjects() {
        try {
            initialUsers();
            unitDev = new OrgUnit("Developers", 1000);
            unitDev2 = new OrgUnit("Marketing", 1000);
            adminDev = new User("sophia", "bo$$", true, unitDev2.getName());
            userDev = new User("scott", "scotty", false, unitDev.getName());
            userDev2 = new User("johnny", "john", false, unitDev.getName());
            userDev3 = new User("alistair", "allstar", false, unitDev2.getName());
            userDev4 = new User("nullman", "nothing", false, null);
            assetDev1 = new Asset(1, "Test asset for development!");
            assetDev2 = new Asset(2, "Another test asset for development!");
            dataSource.insertUnit(unitDev);
            dataSource.insertUnit(unitDev2);
            dataSource.updateUser(adminDev);
            dataSource.updateUser(userDev);
            dataSource.insertUser(userDev2);
            dataSource.insertUser(userDev3);
            dataSource.insertUser(userDev4);
            dataSource.insertAsset(assetDev1);
            dataSource.insertAsset(assetDev2);
            dataSource.insertOrUpdateInventory(new InventoryRecord(unitDev2.getName(), assetDev1.getId(), 800));
            dataSource.insertOrUpdateInventory(new InventoryRecord(unitDev.getName(), assetDev1.getId(), 500));
            dataSource.insertOrUpdateInventory(new InventoryRecord(unitDev.getName(), assetDev2.getId(), 3500));
            testBuyOrder = new BuyOrder(unitDev.getName(), assetDev1.getId(), 20, 13);
            testBuyOrder2 = new BuyOrder(unitDev2.getName(), assetDev2.getId(), 30, 15);
            testSellOrder = new SellOrder(unitDev.getName(), assetDev1.getId(), 6, 47);
            testSellOrder2 = new SellOrder(unitDev2.getName(), assetDev1.getId(), 29, 16);
            placeBuyOrder(testBuyOrder);
            placeBuyOrder(testBuyOrder2);
            placeSellOrder(testSellOrder);
            placeSellOrder(testSellOrder2);
        } catch (DoesNotExist | IllegalString | OrderException doesNotExist) {
            doesNotExist.printStackTrace();
        }
    }

    /**
     * Clear data (debug)
     */
    void deleteEverything() {
        dataSource.debugDeleteEverything();
        dataSource.recreate();
    }

    /**
     * Delegate for refreshDelay
     * @return the refresh
     */
    int getTradeDelay() {
        return dataSource.refreshDelay;
    }

    /**
     * Validate login-- determine first whether the input username exists, then whether the input password is correct
     * (by retrieving the user's salt string and hashed password, then hashing the input password with the salt string
     * and checking if it matches the input password)
     * @param username Username input string
     * @param password Password input string
     * @return User object of the now-logged-in user if the credentials were correct
     * @throws IllegalString if the input password was of an invalid format (meaning it must be incorrect)
     * @throws DoesNotExist if the user does not exist or the password is incorrect
     */
    User login(String username, String password) throws IllegalString, DoesNotExist {
        User user = dataSource.userByKey(username);
        if (user == null) {
            throw new DoesNotExist("User %s does not exist", username);
        }
        String hashedInputPassword = User.hashPassword(password, user.getSalt());
        if (!hashedInputPassword.equals(user.getPassword())) {
            throw new DoesNotExist("Invalid password, please try again.");
        }
        return user;
    }

    /**
     * Get an ArrayList of the entire user table
     * @return ArrayList of the entire user table
     */
    ArrayList<User> getAllUsers() {
        return dataSource.allUsers();
    }
    /**
     * Get an ArrayList of the entire orgunit table
     * @return ArrayList of the entire orgunit table
     */
    ArrayList<OrgUnit> getAllUnits() {
        return dataSource.allOrgUnits();
    }
    /**
     * Get an ArrayList of the entire asset table
     * @return ArrayList of the entire asset table
     */
    ArrayList<Asset> getAllAssets() { return dataSource.allAssets();}
    /**
     * Get an ArrayList of the entire inventories table
     * @return ArrayList of the entire inventories table
     */
    ArrayList<InventoryRecord> getAllInventories() {return dataSource.allInventories();}
    /**
     * Get an ArrayList of the entire buyorder table
     * @return ArrayList of the entire buyorder table
     */
    ArrayList<BuyOrder> getAllBuys() { return dataSource.allBuyOrders();}
    /**
     * Get an ArrayList of the entire sellorder table
     * @return ArrayList of the entire sellorder table
     */
    ArrayList<SellOrder> getAllSells() {return dataSource.allSellOrders();}

    /**
     * Gets orders to populate an OrderTablePage
     * @param unitName Unit name, used iff justMine
     * @param justMine True means "just get orders from unitName", false means "don't filter by org unit"
     * @param isBuy True means "get buy orders", false means "get sell orders"
     * @param resolvedFlag True means "get resolved orders", false means "get unresolved orders"
     * @return An ArrayList of either BuyOrders or SellOrders
     */
    ArrayList<Order> getOrdersForTable(String unitName, boolean justMine, boolean isBuy, boolean resolvedFlag) {
        if (justMine && isBuy) return (ArrayList) dataSource.buyOrdersByUnit(unitName,resolvedFlag);
        else if (justMine) return (ArrayList) dataSource.sellOrdersByUnit(unitName,resolvedFlag);
        else if (isBuy) return (ArrayList) dataSource.allBuyOrders(resolvedFlag);
        else return (ArrayList) dataSource.allSellOrders(resolvedFlag);
    }

    /**
     * Generate strings for a GuiSearch of users
     * @param users Results of a user qeury
     * @return ArrayList of their usernames
     */
    ArrayList<String> getUsernames(ArrayList<User> users) {
        ArrayList<String> output = new ArrayList<>();
        for (User u : users) {
            output.add(u.getUsername());
        }
        return output;
    }

    /**
     * Generate strings for a GuiSearch of orgunits
     * @param units Resultts of an orgunit query
     * @return ArrayList of their names
     */
    ArrayList<String> getUnitNames(ArrayList<OrgUnit> units) {
        ArrayList<String> output = new ArrayList<>();
        for (OrgUnit u : units) {
            output.add(u.getName());
        }
        return output;
    }

    /**
     * Get strings for a GuiSearch of assets formatted like "ID (description)"
     * @param assets Results of an asset query
     * @return ArrayList of strings formatted in that way
     */
    ArrayList<String> getAssetStrings(ArrayList<Asset> assets) {
        ArrayList<String> output = new ArrayList<>();
        for (Asset a : assets) {
            output.add(String.format("%d (%s)", a.getId(), a.getDescription()));
        }
        return output;
    }

    /**
     * Get all users in an org unit
     * @param unit Unit name
     * @return ArrayList of all users in the unit
     * @throws DoesNotExist if the unit does not exist
     */
    ArrayList<User> getMembers(String unit) throws DoesNotExist {
        getUnitByKey(unit);
        return dataSource.usersByUnit(unit);
    }

    /**
     * Query the user table by a specific username
     * @param key Username
     * @return User object of the user if they exist
     * @throws DoesNotExist if the user does not exist
     */
    User getUserByKey(String key) throws DoesNotExist {
        User result = dataSource.userByKey(key);
        if (result == null) throw new DoesNotExist("The user '%s' does not exist.", key);
        return result;
    }

    /**
     * Query the orgunit table by a specific name
     * @param unitName Name
     * @return OrgUnit object if it exists
     * @throws DoesNotExist if the unit does not exist
     */
    OrgUnit getUnitByKey(String unitName) throws DoesNotExist {
        // Convert username to User object while making sure it exists in the DB.
        OrgUnit result = dataSource.unitByKey(unitName);
        if (result == null) throw new DoesNotExist("The unit '%s' does not exist.", unitName);
        return result;
    }

    /**
     * Query the asset table by a specific key
     * @param key ID
     * @return Asset object if it exists
     * @throws DoesNotExist if the asset does not exist
     */
    Asset getAssetByKey(int key) throws DoesNotExist {
        Asset result = dataSource.assetByKey(key);
        if (result == null) throw new DoesNotExist("Asset '%s' does not exist.", key);
        return result;
    }

    /**
     * Query the sellorder table by a specific key
     * @param key ID
     * @return SellOrder object if it exists
     * @throws DoesNotExist if the order does not exist
     */
    SellOrder getSellByKey(int key) throws DoesNotExist {
        SellOrder result = dataSource.sellOrderByKey(key);
        if (result == null) throw new DoesNotExist("Sell order '%s' does not exist.", key);
        return result;
    }
    /**
     * Query the buyorder table by a specific key
     * @param key ID
     * @return BuyOrder object if it exists
     * @throws DoesNotExist if the order does not exist
     */
    BuyOrder getBuyByKey(int key) throws DoesNotExist {
        BuyOrder result = dataSource.buyOrderByKey(key);
        if (result == null) throw new DoesNotExist("Buy order '%s' does not exist.", key);
        return result;
    }

    /**
     * Get an inventory record by its composite key
     * @param unit Unit name
     * @param asset Asset ID
     * @return InventoryRecord object if the unit and asset are real. If the unit and asset are real but have no inventory
     * record, it returns a new record with quantity 0 because that's the same thing
     * @throws DoesNotExist if the unit or asset do not exist
     */
    InventoryRecord getInv(String unit, int asset) throws DoesNotExist {
        getUnitByKey(unit);
        getAssetByKey(asset);
        InventoryRecord result = dataSource.inventoryRecordByKeys(unit, asset);
        if (result == null) return new InventoryRecord(unit, asset, 0);
        //no record and a value of 0 are basically the same thing so no exception is needed
        return result;
    }

    /**
     * Get all inventory records of a unit
     * @param unitName Name
     * @return ArrayList of all inventory records of the unit (may be empty)
     * @throws DoesNotExist if the unit does not exist
     */
    ArrayList<InventoryRecord> getInventoriesByOrgUnit(String unitName) throws DoesNotExist {
        getUnitByKey(unitName);
        return dataSource.inventoriesByUnit(unitName);
    }

    /**
     * Get all invantory records of an asset
     * @param assetID ID
     * @return ArrayList of all inventory records of the asset (may be empty)
     * @throws DoesNotExist if the asset does not exist
     */
    ArrayList<InventoryRecord> getInventoriesByAsset(int assetID) throws DoesNotExist {
        getAssetByKey(assetID);
        return dataSource.inventoriesByAsset(assetID);
    }

    /**
     * Get all resolved buy orders for an asset (for historical prices)
     * @param assetID ID
     * @return ArrayList of all resolved buy orders for the asset (may be empty)
     * @throws DoesNotExist If the asset does not exist
     */
    ArrayList<BuyOrder> getResolvedBuysByAsset(int assetID) throws DoesNotExist {
        getAssetByKey(assetID);
        return dataSource.buyOrdersByAsset(assetID, true);
    }
    /**
     * Get all unresolved buy orders for an asset
     * @param assetID ID
     * @return ArrayList of all unresolved buy orders for the asset (may be empty)
     * @throws DoesNotExist If the asset does not exist
     */
    ArrayList<BuyOrder> getUnresolvedBuysByAsset(int assetID) throws DoesNotExist {
        getAssetByKey(assetID);
        return dataSource.buyOrdersByAsset(assetID, false);
    }

    /**
     * Get all buy orders by a unit
     * @param unitName name
     * @return ArrayList of all buy orders by the unit
     * @throws DoesNotExist if unit does not eixst
     */
    ArrayList<BuyOrder> getBuysByUnit(String unitName) throws DoesNotExist {
        getUnitByKey(unitName); //throws DoesNotExist
        return dataSource.buyOrdersByUnit(unitName, null);
    }
    /**
     * Get all sell orders by a unit
     * @param unitName name
     * @return ArrayList of all sell orders by the unit
     * @throws DoesNotExist if unit does not eixst
     */
    ArrayList<SellOrder> getSellsByUnit(String unitName) throws DoesNotExist {
        getUnitByKey(unitName); //throws DoesNotExist
        return dataSource.sellOrdersByUnit(unitName, null);
    }

    /**
     * Get all unresolved sell orders for an asset
     * @param assetID asset ID
     * @return ArrayList of resolved sells for the asset
     * @throws DoesNotExist if the asset does not exist
     */
    ArrayList<SellOrder> getUnresolvedSellsByAsset(int assetID) throws DoesNotExist {
        getAssetByKey(assetID);
        return dataSource.sellOrdersByAsset(assetID, false);
    }

    /**
     * Get all resolved sell orders for an asset
     * @param assetID asset ID
     * @return ArrayList of resolved sells for the asset
     * @throws DoesNotExist if the asset does not exist
     */
    ArrayList<SellOrder> getResolvedSellsByAsset(int assetID) throws DoesNotExist {
        getAssetByKey(assetID);
        return dataSource.sellOrdersByAsset(assetID, false);
    }

    /**
     * Get all assets held by a unit
     * @param unitName name
     * @return ArrayList of assets held by the unit
     * @throws DoesNotExist if the unit does not exist
     */
    ArrayList<Asset> getHeldAssets(String unitName) throws DoesNotExist {
        ArrayList<Asset> results = new ArrayList<>();
        for (InventoryRecord i : getInventoriesByOrgUnit(unitName)) {
            if (i.getQuantity() > 0) results.add(getAssetByKey(i.getAssetID()));
        }
        return results;
    }

    /**
     * Get all assets NOT held by a unit
     * @param unitName name
     * @return ArrayList of assets with no inevtory info for this unit
     * @throws DoesNotExist if the unit deos not exist
     */
    ArrayList<Asset> getUnheldAssets(String unitName) throws DoesNotExist {
        ArrayList<Asset> heldAssets = getHeldAssets(unitName);
        ArrayList<Asset> results = new ArrayList<>();
        for (Asset a :  getAllAssets()) {
            if (!heldAssets.contains(a)) results.add(a);
        }
        return results;
    }

    /**
     * Get all units that have some of the asset
     * @param assetID ID
     * @return ArrayList of all units which have inventory info on the unit
     * @throws DoesNotExist if the asset does not exist
     */
    ArrayList<OrgUnit> getHoldingUnits(int assetID) throws DoesNotExist {
        ArrayList<OrgUnit> results = new ArrayList<>();
        for (InventoryRecord i : getInventoriesByAsset(assetID)) {
            if (i.getQuantity() > 0) results.add(getUnitByKey(i.getUnitName()));
        }
        return results;
    }
    /**
     * Get all units that have none of the asset
     * @param assetID ID
     * @return ArrayList of all units which have no inventory info on the unit
     * @throws DoesNotExist if the asset does not exist
     */
    ArrayList<OrgUnit> getUnholdingUnits(int assetID) throws DoesNotExist {
        ArrayList<OrgUnit> results = new ArrayList<>();
        ArrayList<OrgUnit> holdingUnits = getHoldingUnits(assetID);
        for (OrgUnit o : getAllUnits()) {
            if (!holdingUnits.contains(o)) results.add(o);
        }
        return results;
    }

    //DELETE METHODS

    /**
     * Delete asset from database
     * @param id asset ID to delete
     * @throws DoesNotExist if asset does not exist
     */
    void deleteAsset(int id) throws DoesNotExist {
        int i = dataSource.deleteAsset(id);
        if (i == 0) throw new DoesNotExist("Asset '%s' not found", id);
    }
    /**
     * Delete user from database
     * @param name username to delete
     * @throws DoesNotExist if user does not exist
     */
    void deleteUser(String name) throws DoesNotExist {
        int i = dataSource.deleteUser(name);
        if (i == 0) throw new DoesNotExist("User '%s' not found", name);
    }
    /**
     * Delete unit from database
     * @param name name to delete
     * @throws DoesNotExist if unit does not exist
     */
    void deleteUnit(String name) throws DoesNotExist {
        int i = dataSource.deleteUnit(name);
        if (i == 0) throw new DoesNotExist("Unit '%s' not found", name);
    }

    /**
     * Delete a sell order; if it's unresolved, return the leftover assets to the unit
     * @param id order ID to delete
     * @throws DoesNotExist if the order does not exist
     */
    void cancelSellOrder(int id) throws DoesNotExist {
        SellOrder s = getSellByKey(id); //this throws the doesnotexist if needed
        String unitToReturn = s.getUnit();
        int i = dataSource.deleteSellOrder(id);
        if (s.getDateResolved() == null) adjustInventory(unitToReturn, s.getAsset(), s.getQty());
        //return the assets if it's unresolved
    }
    /**
     * Delete a buy order; if it's unresolved, return the leftover credits
     * @param id order ID to delete
     * @throws DoesNotExist if the order does not exist
     * @throws InvalidAmount never, but the computer doesn't know that
     */
    void cancelBuyOrder(int id) throws DoesNotExist, InvalidAmount {
        BuyOrder b = getBuyByKey(id);
        String unitToReturn = b.getUnit();
        dataSource.deleteBuyOrder(id);
        if (b.getDateResolved() == null) adjustUnitBalance(unitToReturn, b.getPrice()*b.getQty());
    }

    /**
     * Delete an inventory record
     * @param unit unit name
     * @param asset asset ID
     * @throws DoesNotExist if the record did not exist
     */
    void deleteInventoryRecord(String unit, int asset) throws DoesNotExist {
        if (dataSource.deleteInventoryRecord(unit, asset) == 0) {
            throw new DoesNotExist("Inventory information for this asset and unit '%s' does not exist", unit);
        }
    }

    //INSERT METHODS-----------------------------------------------------

    /**
     * Place sell order
     * @param s SellOrder object
     * @throws OrderException if the unit doesn't have enough of the asset
     * @throws DoesNotExist if the unit or asset referenced by the order do not exist
     */
    void placeSellOrder(SellOrder s) throws OrderException, DoesNotExist {
        InventoryRecord inventoryRecord = getInv(s.getUnit(), s.getAsset());
        int quantity = inventoryRecord.getQuantity();
        if (quantity < s.getQty()) {
            throw new OrderException("Insufficient quantity of asset- unit %s has %d but %d are needed to " +
                    "place this sell order", inventoryRecord.getUnitName(), quantity, s.getQty());
        } else {
            inventoryRecord.adjustQuantity(-1*s.getQty());
            dataSource.insertOrUpdateInventory(inventoryRecord);
            dataSource.insertSellOrder(s);
        }
    }
    /**
     * Place buy order
     * @param s Buyorder object
     * @throws OrderException if the unit doesn't have enough credits
     * @throws DoesNotExist if the unit or asset do not exist
     */
    void placeBuyOrder(BuyOrder s) throws OrderException, DoesNotExist {
        getAssetByKey(s.getAsset());
        OrgUnit unitInQuestion = getUnitByKey(s.getUnit());
        int neededCredits = s.getQty() * s.getPrice();
        try{
            unitInQuestion.adjustBalance(-1*neededCredits);
            dataSource.updateUnit(unitInQuestion);
            dataSource.insertBuyOrder(s);
        } catch (InvalidAmount i) {
            throw new OrderException("Insufficient credits- unit %s has %d but %d are needed to " +
                    "place this buy order",
                    unitInQuestion.getName(), unitInQuestion.getCredits(), neededCredits);
        }
    }

    /**
     * Place order of unknown subtype
     * @param o Order object that could be either subclass
     * @throws OrderException if there was an insufficient amount
     * @throws DoesNotExist if the unit/asset do not exist
     */
    void placeOrder(Order o) throws OrderException, DoesNotExist {
        if (o instanceof BuyOrder) placeBuyOrder((BuyOrder) o);
        else placeSellOrder((SellOrder) o);
    }

    /**
     * Add user to database
     * @param u User object
     * @throws AlreadyExists if username already exists
     * @throws DoesNotExist if org unit does not exist
     */
    void addUser(User u) throws AlreadyExists, DoesNotExist {
        int result = dataSource.insertUser(u);
        if (result == 0)
            throw new AlreadyExists("User '%s' already exists. Please try a different username.", u.getUsername());
        else if (result == -1) throw new DoesNotExist("Could not create user- org unit %s does not exist", u.getUnit());
    }

    /**
     * Add orgunit to database
     * @param u OrgUnit object
     * @throws AlreadyExists If unit name already exists
     */
    void addUnit(OrgUnit u) throws AlreadyExists {
        if (dataSource.insertUnit(u) == 0)
            throw new AlreadyExists("Unit '%s' already exists. Please try a different unit name.", u.getName());
    }

    /**
     * Add asset to database
     * @param a Asset object
     * @throws AlreadyExists if asset ID is duplicate (though this should never happen)
     */
    void addAsset(Asset a) throws AlreadyExists {
        if (dataSource.insertAsset(a) == 0) throw new AlreadyExists("Asset '%d' already exists.", a.getId());
    }

    /**
     * Set inventory quantity (new record if nonexistent, update existing record if existent)
     * @param i InventoryRecord object
     * @throws DoesNotExist if unit or asset do not exist
     */
    void setInventory(InventoryRecord i) throws DoesNotExist {
        if (dataSource.insertOrUpdateInventory(i) == -1) {
            throw new DoesNotExist("Unit %s and/or asset %d not found.", i.getUnitName(), i.getAssetID());
        }
    }

    /**
     * Adjust inventory quantity
     * @param unit Unit name
     * @param asset Asset ID
     * @param adjustment Adjustment to add to the current quantity (if no record exists it acts as if 0 is quantity)
     * @throws DoesNotExist if the unit or asset don't exist
     */
    void adjustInventory(String unit, int asset, int adjustment) throws DoesNotExist {
        InventoryRecord i = new InventoryRecord(unit, asset, adjustment);
        i.adjustQuantity(getInv(unit,asset).getQuantity()); //done this way to just set it to the value if no record exists
        if (dataSource.insertOrUpdateInventory(i) == -1) {
            throw new DoesNotExist("Unit %s and/or asset %d not found.", i.getUnitName(), i.getAssetID());
        }
    }


    //UPDATE METHODS--------------------------------------------------------------------

    /**
     * Update, replacing the details for the user record wiht this username
     * @param u User object
     * @throws DoesNotExist if the user does not exist
     * @throws ConstraintException if the new org unit does not exist
     */
    void updateUser(User u) throws DoesNotExist, ConstraintException {
        int result = dataSource.updateUser(u);
        if (result == 0) throw new DoesNotExist("User '%s' not found.", u.getUsername());
        else if (result == -1) throw new ConstraintException("Unit %s not found.", u.getUnit());
    }

    /**
     * Update, replacing the details for the unit record wiht this username
     * @param u OrgUnit object
     * @throws DoesNotExist if the unit name does not exist
     */
    void updateUnit(OrgUnit u) throws DoesNotExist {
        if (dataSource.updateUnit(u) == 0) throw new DoesNotExist("Unit '%s' not found.", u.getName());
    }

    /**
     * Update, replacing the details for the asset record with this ID
     * @param a Asset object
     * @throws DoesNotExist if asset ID does not exist
     */
    void updateAsset(Asset a) throws DoesNotExist {
        if (dataSource.updateAsset(a) == 0) throw new DoesNotExist("Asset '%d' not found.", a.getId());
    }

    /**
     * Update, replacing the details for the BuyOrder record wiht this ID
     * @param o BuyOrder object
     * @throws DoesNotExist if ID does not exist
     * @throws ConstraintException if unit or asset don't exist
     */
    void updateBuyOrder(BuyOrder o) throws DoesNotExist, ConstraintException {
        int result = dataSource.updateBuyOrder(o);
        if (result == 0) throw new DoesNotExist("Buy order '%d' not found.", o.getId());
        else if (result == -1) throw new ConstraintException("Unit and/or asset not found.");
    }

    /**
     * Update, replacing the details for SellOrder record with this ID
     * @param o SellOrder object
     * @throws DoesNotExist if ID does not exist
     * @throws ConstraintException if unit or asset don't exist
     */
    void updateSellOrder(SellOrder o) throws DoesNotExist, ConstraintException {
        int result = dataSource.updateSellOrder(o);
        if (result == 0) throw new DoesNotExist("Sell order '%d' not found.", o.getId());
        else if (result == -1) throw new ConstraintException("Unit and/or asset not found.");
    }

    /**
     * Update, changing the balance of org unit
     * @param unitName Unit name
     * @param newBalance new balance
     * @throws DoesNotExist if unit doesn't exist
     * @throws InvalidAmount if new balance is less than 0
     */
    void setUnitBalance(String unitName, int newBalance) throws DoesNotExist, InvalidAmount {
        OrgUnit unitInQuestion = getUnitByKey(unitName);
        unitInQuestion.setBalance(newBalance);
        updateUnit(unitInQuestion);
    }
    /**
     * Update, adjusting the balance of org unit
     * @param unitName Unit name
     * @param amount number to add to balance
     * @throws DoesNotExist if unit doesn't exist
     * @throws InvalidAmount if new balance is less than 0
     */
    void adjustUnitBalance(String unitName, int amount) throws DoesNotExist, InvalidAmount {
        OrgUnit unitInQuestion = getUnitByKey(unitName);
        unitInQuestion.adjustBalance(amount);
        updateUnit(unitInQuestion);
    }

    /**
     * Method to get the average price of an asset between a start date and end date.
     * @param startDate the date at which the user wants to start reading data.
     * @param endDate the date at which the user wants to finish reading data.
     * @param asset Asset ID
     * @return returns a double of the average price.
     */
    double getAveragePrice(LocalDate startDate, LocalDate endDate, int asset) throws InvalidDate, DoesNotExist {

        LocalDate earliestDate;
        LocalDate today = LocalDate.now();
        try{
            //get the date of the earliest resolved BuyOrder for the asset
            earliestDate = (getResolvedBuysByAsset(asset).stream().min(BuyOrder::compareTo).orElseThrow().getDateResolved()).toLocalDate();
        }
        catch (NoSuchElementException e) {
            return 0;
        }
        if (endDate.isBefore(earliestDate)) {
            throw new InvalidDate("End date is out of range");
        }
        if (startDate.isAfter(today)) {
            throw new InvalidDate("Start date is out of range");
        }
        if (startDate.isAfter(endDate)) throw new InvalidDate("Start date cannot be later than end date");
        //Early start or late end will be fine with the code below

        int sum = 0;
        int count = 0;
        ArrayList<BuyOrder> transactions = dataSource.buyOrdersByAssetResolvedBetween(asset,
                Timestamp.valueOf(startDate.atStartOfDay()), Timestamp.valueOf(endDate.atTime(23,59,59)));
        for (BuyOrder b : transactions) {
            sum += b.getPrice();
            count++;
        }
        if (count == 0) return 0;
        return (double)sum / count;
    }

    /**
     * Method that collects average prices between specified intervals for the entire data set and places them into a
     * TreeMap. This may be used to create a price history graph.
     * @param timeInterval the time interval in which the data will be split before calculating the averages of each
     *                     interval. Constants are provided as days, 3 days, weeks, months and years.
     * @return returns a TreeMap with each intervals start date as a key, with its value being the corresponding average.
     */
    TreeMap<LocalDate, Double> getHistoricalPrices(int a, Intervals timeInterval) throws InvalidDate, DoesNotExist {
        ArrayList<BuyOrder> priceHistory = getResolvedBuysByAsset(a);
        if (priceHistory.isEmpty()) {
            System.out.println("No historical prices");
            return new TreeMap<>();
        }
        Optional<BuyOrder> earliest = priceHistory.stream().min(BuyOrder::compareTo);
        Optional<BuyOrder> latest = priceHistory.stream().max(BuyOrder::compareTo);
        LocalDate earliestDate = earliest.get().getDateResolved().toLocalDate();
        LocalDate latestDate = latest.get().getDateResolved().toLocalDate();
        LocalDate endDate;
        // Create new TreeMap for the averages
        TreeMap<LocalDate, Double> averages = new TreeMap<>();


        switch (timeInterval) {
            case DAYS -> {
                endDate = latestDate.plusDays(1);
                System.out.println("Daily average prices");
                for (LocalDate current = earliestDate; current.isBefore(endDate); current = current.plusDays(1)) {
                    double currentAvg = getAveragePrice(current, current, a);
                    averages.put(current, currentAvg);
                    System.out.println(current + " = " + currentAvg);
                }
            }
            case WEEKS -> {
                endDate = latestDate.with(TemporalAdjusters.previousOrSame(WeekFields.of(locale).getFirstDayOfWeek())).plusWeeks(1);
                System.out.println("Weekly average prices between:");
                for (LocalDate current = earliestDate.with(TemporalAdjusters.previousOrSame(WeekFields.of(locale).getFirstDayOfWeek()));
                     current.isBefore(endDate); current = current.plusWeeks(1)) {
                    LocalDate endOfWeek = current.plusDays(6);
                    double currentAvg = getAveragePrice(current, endOfWeek, a);
                    averages.put(current, currentAvg);
                    System.out.println(current + " - " + endOfWeek + " = " + currentAvg);
                }
            }
            case MONTHS -> {
                endDate = latestDate.withDayOfMonth(1).plusMonths(1);
                System.out.println("Monthly average prices between:");
                for (LocalDate current = earliestDate.withDayOfMonth(1); current.isBefore(endDate);
                     current = current.plusMonths(1)) {
                    LocalDate endOfMonth = current.plusMonths(1).minusDays(1);
                    double currentAvg = getAveragePrice(current, endOfMonth, a);
                    averages.put(current, currentAvg);
                    System.out.println(current + " - " + endOfMonth + " = " + currentAvg);
                }
            }
            case YEARS -> {
                endDate = latestDate.withDayOfYear(1).plusYears(1);
                System.out.println("Yearly average prices between:");
                for (LocalDate current = earliestDate.withDayOfYear(1); current.isBefore(endDate);
                     current = current.plusYears(1)) {
                    LocalDate endOfYear = current.plusYears(1).minusDays(1);
                    double currentAvg = getAveragePrice(current, endOfYear, a);
                    averages.put(current, currentAvg);
                    System.out.println(current + " - " + endOfYear + " = " + currentAvg);
                }
            }
        }
        return averages;
    }
}
