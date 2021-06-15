package ClientSide;

import common.*;
import common.Exceptions.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;

class TradingAppData {
    TradingAppDataSource dataSource;
    Locale locale = Locale.ENGLISH;

    enum Intervals {
        DAYS(),
        WEEKS(),
        MONTHS(),
        YEARS()
    }

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
    static SellOrder testSellOrder;


    void addHistoricalPrice(int idUpTo, int assetID, String unitResponsible, int price, LocalDateTime dateTime) {
        SellOrder sell = new SellOrder(0, unitResponsible, assetID, 0, price, dateTime, dateTime);
        BuyOrder buy = new BuyOrder(0, unitResponsible, assetID, 0, price, dateTime, dateTime, idUpTo);
        dataSource.insertSellOrder(sell);
        dataSource.insertBuyOrder(buy);

    }

    void mockObjectsWithPrices() throws IllegalString, InvalidAmount, DoesNotExist, OrderException {
        mockObjects();
        int numdays = 365 * 2;
        LocalDateTime begin = LocalDateTime.now().minusDays(numdays);
        for (int i = 1; i <= numdays; i++) {
            LocalDateTime theDay = begin.plusDays(i);
            addHistoricalPrice(i*6-5, assetDev1.getId(), unitDev.getName(), 10, theDay);
            addHistoricalPrice(i*6-4, assetDev1.getId(), unitDev.getName(), 15, theDay);
            addHistoricalPrice(i*6-3, assetDev1.getId(), unitDev.getName(), 20, theDay);
            addHistoricalPrice(i*6-2, assetDev2.getId(), unitDev.getName(), 10, theDay);
            addHistoricalPrice(i*6-1, assetDev2.getId(), unitDev.getName(), 30, theDay);
            addHistoricalPrice(i*6, assetDev2.getId(), unitDev.getName(), 50, theDay);
        }
    }

    void mockObjects() throws IllegalString, InvalidAmount, OrderException, DoesNotExist {
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
        dataSource.insertUser(adminDev);
        dataSource.insertUser(userDev);
        dataSource.insertUser(userDev2);
        dataSource.insertUser(userDev3);
        dataSource.insertUser(userDev4);
        dataSource.insertAsset(assetDev1);
        dataSource.insertAsset(assetDev2);
        dataSource.insertOrUpdateInventory(new InventoryRecord(unitDev.getName(), assetDev1.getId(), 500));
        dataSource.insertOrUpdateInventory(new InventoryRecord(unitDev.getName(), assetDev2.getId(), 3500));
        testBuyOrder = new BuyOrder(unitDev, assetDev1, 20, 13);
        testSellOrder = new SellOrder(unitDev, assetDev1, 6, 47);
        placeBuyOrder(testBuyOrder);
        placeSellOrder(testSellOrder);
    }

    void deleteEverything() {
        dataSource.debugDeleteEverything();
        dataSource.recreate();
    }


    User login(String username, String password) throws IllegalString, DoesNotExist {
        User user = dataSource.userByKey(username);
        if (user == null) {
            throw new DoesNotExist("User does not exist", username);
        }
        String hashedInputPassword = User.hashPassword(password, user.getSalt());
        if (!hashedInputPassword.equals(user.getPassword())) {
            throw new DoesNotExist("Invalid password, please try again.");
        }
        return user;
    }

    ArrayList<User> getAllUsers() {
        return dataSource.allUsers();
    }
    ArrayList<OrgUnit> getAllUnits() {
        return dataSource.allOrgUnits();
    }
    ArrayList<Asset> getAllAssets() { return dataSource.allAssets();}
    ArrayList<InventoryRecord> getAllInventories() {return dataSource.inventoryList();}
    ArrayList<BuyOrder> getAllBuys() { return dataSource.allBuyOrders();}
    ArrayList<SellOrder> getAllSells() {return dataSource.allSellOrders();}

    ArrayList<Order> getOrdersForTable(String unitName, boolean justMine, boolean isBuy, boolean resolvedFlag) {
        if (justMine && isBuy) return (ArrayList) dataSource.buyOrdersByUnit(unitName,resolvedFlag);
        else if (justMine) return (ArrayList) dataSource.sellOrdersByUnit(unitName,resolvedFlag);
        else if (isBuy) return (ArrayList) dataSource.allBuyOrders(resolvedFlag);
        else return (ArrayList) dataSource.allSellOrders(resolvedFlag);
    }

    ArrayList<String> getAllUsernames() {
        ArrayList<User> users = dataSource.allUsers();
        ArrayList<String> output = new ArrayList<>();
        for (User u : users) {
            output.add(u.getUsername());
        }
        return output;
    }
    ArrayList<String> getAllUnitNames() {
        ArrayList<OrgUnit> units = dataSource.allOrgUnits();
        ArrayList<String> output = new ArrayList<>();
        for (OrgUnit u : units) {
            output.add(u.getName());
        }
        return output;
    }
    ArrayList<String> getAllAssetStrings() {
        ArrayList<Asset> assets = dataSource.allAssets();
        ArrayList<String> output = new ArrayList<>();
        for (Asset a : assets) {
            output.add(String.format("%d (%s)", a.getId(), a.getDescription()));
        }
        return output;
    }

    ArrayList<User> getMembers(OrgUnit unit) throws DoesNotExist {
        return dataSource.usersByUnit(unit.getName());
    }

    ArrayList<User> getMembers(String unit) throws DoesNotExist {
        return dataSource.usersByUnit(unit);
    }

    User getUserByKey(String key) throws DoesNotExist {
        User result = dataSource.userByKey(key);
        if (result == null) throw new DoesNotExist("The user '%s' does not exist.", key);
        return result;
    }

    OrgUnit getUnitByKey(String unitName) throws DoesNotExist {
        // Convert username to User object while making sure it exists in the DB.
        OrgUnit result = dataSource.unitByKey(unitName);
        if (result == null) throw new DoesNotExist("The unit '%s' does not exist.", unitName);
        return result;
    }

    Asset getAssetByKey(int key) throws DoesNotExist {
        Asset result = dataSource.assetByKey(key);
        if (result == null) throw new DoesNotExist("Asset '%s' does not exist.", key);
        return result;
    }

    SellOrder getSellByKey(int key) throws DoesNotExist {
        SellOrder result = dataSource.sellOrderByKey(key);
        if (result == null) throw new DoesNotExist("Sell order '%s' does not exist.", key);
        return result;
    }

    BuyOrder getBuyByKey(int key) throws DoesNotExist {
        BuyOrder result = dataSource.buyOrderByKey(key);
        if (result == null) throw new DoesNotExist("Buy order '%s' does not exist.", key);
        return result;
    }

    InventoryRecord getInv(String unit, int asset) throws DoesNotExist {
        getUnitByKey(unit);
        getAssetByKey(asset);
        InventoryRecord result = dataSource.inventoryRecordByKeys(unit, asset);
        if (result == null) return new InventoryRecord(unit, asset, 0);
        //no record and a value of 0 are basically the same thing so no exception is needed
        return result;
    }

    ArrayList<InventoryRecord> getInventoriesByOrgUnit(String unitName) throws DoesNotExist {
        getUnitByKey(unitName);
        return dataSource.inventoriesByUnit(unitName);
    }

    ArrayList<InventoryRecord> getInventoriesByAsset(int assetID) throws DoesNotExist {
        getAssetByKey(assetID);
        return dataSource.inventoriesByAsset(assetID);
    }

    ArrayList<BuyOrder> getResolvedBuysByAsset(int assetID) throws DoesNotExist {
        getAssetByKey(assetID);
        return dataSource.buyOrdersByAsset(assetID, true);
    }

    ArrayList<BuyOrder> getUnresolvedBuysByAsset(int assetID) throws DoesNotExist {
        getAssetByKey(assetID);
        return dataSource.buyOrdersByAsset(assetID, false);
    }

    ArrayList<BuyOrder> getBuysByUnit(String unitName) throws DoesNotExist {
        getUnitByKey(unitName); //throws DoesNotExist
        return dataSource.buyOrdersByUnit(unitName, null);
    }
    ArrayList<SellOrder> getSellsByUnit(String unitName) throws DoesNotExist {
        getUnitByKey(unitName); //throws DoesNotExist
        return dataSource.sellOrdersByUnit(unitName, null);
    }

    //TODO: methods to get the buys resolved in the last session
    // and the sells affected by it


    //DELETE METHODS


    void deleteAsset(int id) throws DoesNotExist {
        int i = dataSource.deleteAsset(id);
        if (i == 0) throw new DoesNotExist("Asset '%s' not found", id);
    }

    void deleteUser(String name) throws DoesNotExist {
        int i = dataSource.deleteUser(name);
        if (i == 0) throw new DoesNotExist("User '%s' not found", name);
    }

    void deleteUnit(String name) throws DoesNotExist {
        int i = dataSource.deleteUnit(name);
        if (i == 0) throw new DoesNotExist("Unit '%s' not found", name);
    }

    void cancelSellOrder(int id) throws DoesNotExist {
        SellOrder s = getSellByKey(id); //this throws the doesnotexist if needed
        String unitToReturn = s.getUnit();
        int i = dataSource.deleteSellOrder(id);
        if (s.getDateResolved() == null) adjustInventory(unitToReturn, s.getAsset(), s.getQty());
        //return the assets if it's unresolved
    }

    void cancelBuyOrder(int id) throws DoesNotExist, InvalidAmount {
        BuyOrder b = getBuyByKey(id);
        String unitToReturn = b.getUnit();
        dataSource.deleteBuyOrder(id);
        if (b.getDateResolved() == null) adjustUnitBalance(unitToReturn, b.getPrice()*b.getQty());
    }

    void deleteInventoryRecord(String unit, int asset) throws DoesNotExist {
        if (dataSource.deleteInventoryRecord(unit, asset) == 0) {
            throw new DoesNotExist("Inventory information for this asset and unit '%s' does not exist", unit);
        }
    }

    //INSERT METHODS-----------------------------------------------------

    void placeSellOrder(SellOrder s) throws OrderException, DoesNotExist, InvalidAmount {
        //OrgUnit unitInQuestion = dataSource.unitByKey(dataSource.userByKey(s.getUsername()).getUnit());
        InventoryRecord inventoryRecord = getInv(s.getUnit(), s.getAsset());
        int quantity = inventoryRecord.getQuantity();
        if (s.getQty() <= 0) throw new InvalidAmount("Quantity %d is invalid; must be greater than 0", s.getQty());
        if (s.getPrice() <= 0) throw new InvalidAmount("Price %d is invalid; must be greater than 0", s.getPrice());
        if (quantity < s.getQty()) {
            throw new OrderException("Insufficient quantity of asset- unit %s has %d but %d are needed to" +
                    "place this order", inventoryRecord.getUnitName(), quantity, s.getQty());
        } else {
            inventoryRecord.setQuantity(inventoryRecord.getQuantity() - s.getQty());
            dataSource.insertOrUpdateInventory(inventoryRecord);
            dataSource.insertSellOrder(s);
        }
    }

    void placeBuyOrder(BuyOrder s) throws OrderException, InvalidAmount {
        OrgUnit unitInQuestion = dataSource.unitByKey(s.getUnit());
        int neededCredits = s.getQty() * s.getPrice();
        if (s.getQty() <= 0) throw new InvalidAmount("Quantity %d is invalid; must be greater than 0", s.getQty());
        if (s.getPrice() <= 0) throw new InvalidAmount("Price %d is invalid; must be greater than 0", s.getPrice());
        if (unitInQuestion.getCredits() < neededCredits) {
            throw new OrderException("Insufficient credits- unit %s has %d but %d are needed to" +
                            "place this buy order",
                    unitInQuestion.getName(), unitInQuestion.getCredits(), neededCredits);
        } else {
            unitInQuestion.adjustBalance(-s.getQty());
            dataSource.updateUnit(unitInQuestion);
            dataSource.insertBuyOrder(s);
        }
    }

    void addUser(User u) throws AlreadyExists, DoesNotExist {
        int result = dataSource.insertUser(u);
        if (result == 0)
            throw new AlreadyExists("User '%s' already exists. Please try a different username.", u.getUsername());
        else if (result == -1) throw new DoesNotExist("Could not create user- org unit %s does not exist", u.getUnit());
    }

    void addUnit(OrgUnit u) throws AlreadyExists {
        if (dataSource.insertUnit(u) == 0)
            throw new AlreadyExists("Unit '%s' already exists. Please try a different unit name.", u.getName());
    }

    void addAsset(Asset a) throws AlreadyExists {
        if (dataSource.insertAsset(a) == 0) throw new AlreadyExists("Asset '%i' already exists.", a.getId());
    }

    void setInventory(InventoryRecord i) throws DoesNotExist {
        if (dataSource.insertOrUpdateInventory(i) == -1) {
            throw new DoesNotExist("Unit %s and/or asset %i not found.");
        }
    }

    void adjustInventory(String unit, int asset, int adjustment) throws DoesNotExist {
        InventoryRecord i = new InventoryRecord(unit, asset, adjustment);
        i.adjustQuantity(getInv(unit,asset).getQuantity()); //done this way to just set it to the value if no record exists
        if (dataSource.insertOrUpdateInventory(i) == -1) {
            throw new DoesNotExist("Unit %s and/or asset %i not found.");
        }
    }


    //UPDATE METHODS--------------------------------------------------------------------
    void updateUser(User u) throws DoesNotExist, ConstraintException {
        int result = dataSource.updateUser(u);
        if (result == 0) throw new DoesNotExist("User '%s' not found.", u.getUsername());
        else if (result == -1) throw new DoesNotExist("Unit %s not found.", u.getUnit());
    }

    void updateUnit(OrgUnit u) throws DoesNotExist {
        if (dataSource.updateUnit(u) == 0) throw new DoesNotExist("Unit '%s' not found.", u.getName());
    }

    void updateAsset(Asset a) throws DoesNotExist {
        if (dataSource.updateAsset(a) == 0) throw new DoesNotExist("Asset '%i' not found.", a.getId());
    }

    void updateBuyOrder(BuyOrder o) throws DoesNotExist {
        int result = dataSource.updateBuyOrder(o);
        if (result == 0) throw new DoesNotExist("Buy order '%i' not found.", o.getId());
        else if (result == -1) throw new DoesNotExist("Unit and/or asset not found.");
    }

    void updateSellOrder(SellOrder o) throws DoesNotExist {
        int result = dataSource.updateSellOrder(o);
        if (result == 0) throw new DoesNotExist("Sell order '%i' not found.", o.getId());
        else if (result == -1) throw new DoesNotExist("Unit and/or asset not found.");
    }

    void setUnitBalance(String unitName, int newBalance) throws DoesNotExist, InvalidAmount {
        OrgUnit unitInQuestion = getUnitByKey(unitName);
        unitInQuestion.setBalance(newBalance);
        updateUnit(unitInQuestion);
    }

    void adjustUnitBalance(String unitName, int amount) throws DoesNotExist, InvalidAmount {
        OrgUnit unitInQuestion = getUnitByKey(unitName);
        unitInQuestion.adjustBalance(amount);
        updateUnit(unitInQuestion);
    }
    /***
     * Method to get the average price of an asset between a start date and end date.
     * @param startDate the date at which the user wants to start reading data.
     * @param endDate the date at which the user wants to finish reading data.
     * @return returns a double of the average price.
     */
    double getAveragePrice(LocalDate startDate, LocalDate endDate, Asset asset) throws InvalidDate, DoesNotExist {
        return getAveragePrice(startDate, endDate, asset.getId());
    }
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

    TreeMap<LocalDate, Double> getHistoricalPrices(Asset a, Intervals timeInterval) throws InvalidDate, DoesNotExist {
        return getHistoricalPrices(a.getId(), timeInterval);
    }
    /***
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
