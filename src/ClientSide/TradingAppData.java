package ClientSide;

import common.*;
import common.Exceptions.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZoneId;
import java.time.chrono.ChronoLocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;

public class TradingAppData {
    public TradingAppDataSource dataSource;
    Locale locale = Locale.ENGLISH;

    public enum Intervals {
        DAYS(),
        WEEKS(),
        MONTHS(),
        YEARS()
    }

    public TradingAppData(TradingAppDataSource dataSource) {
        this.dataSource = dataSource;
    }

    //DEV/HELPER CONTENT --------------------------------------------------------------------
    public static User adminDev;
    public static User userDev;
    public static User userDev2;
    public static User userDev3;
    public static User userDev4;
    public static OrgUnit unitDev;
    public static OrgUnit unitDev2;
    public static Asset assetDev1;
    public static Asset assetDev2;
    public static BuyOrder testBuyOrder;
    public static SellOrder testSellOrder;


    public void addHistoricalPrice(int idUpTo, int assetID, String userResponsible, int price, LocalDateTime dateTime) {
        SellOrder sell = new SellOrder(0, userResponsible, assetID, 0, price, dateTime, dateTime);
        BuyOrder buy = new BuyOrder(0, userResponsible, assetID, 0, price, dateTime, dateTime, idUpTo);
        dataSource.insertSellOrder(sell);
        dataSource.insertBuyOrder(buy);

    }

    public void mockObjectsWithPrices() throws IllegalString, InvalidAmount, DoesNotExist, OrderException {
        mockObjects();
        int numdays = 20;
        LocalDateTime begin = LocalDateTime.now().minusDays(numdays);
        for (int i = 1; i <= numdays; i++) {
            LocalDateTime theDay = begin.plusDays(i);
            addHistoricalPrice(i*6-5, assetDev1.getId(), adminDev.getUsername(), 10, theDay);
            addHistoricalPrice(i*6-4, assetDev1.getId(), adminDev.getUsername(), 15, theDay);
            addHistoricalPrice(i*6-3, assetDev1.getId(), adminDev.getUsername(), 20, theDay);
            addHistoricalPrice(i*6-2, assetDev2.getId(), adminDev.getUsername(), 10, theDay);
            addHistoricalPrice(i*6-1, assetDev2.getId(), adminDev.getUsername(), 30, theDay);
            addHistoricalPrice(i*6, assetDev2.getId(), adminDev.getUsername(), 50, theDay);
        }
    }

    public void mockObjects() throws IllegalString, InvalidAmount, OrderException, DoesNotExist {
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
        testBuyOrder = new BuyOrder(userDev, assetDev1, 20, 13);
        testSellOrder = new SellOrder(userDev, assetDev1, 6, 47);
        placeBuyOrder(testBuyOrder);
        placeSellOrder(testSellOrder);
    }

    public void deleteEverything() {
        dataSource.debugDeleteEverything();
        dataSource.recreate();
    }


    public User login(String username, String password) throws IllegalString, DoesNotExist {
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

    public ArrayList<User> getAllUsers() {
        return dataSource.allUsers();
    }

    public ArrayList<String> getAllUsernames() {

        ArrayList<User> users = dataSource.allUsers();
        ArrayList<String> output = new ArrayList<>();
        for (User u : users) {
            output.add(u.getUsername());
        }
        return output;
    }

    public ArrayList<OrgUnit> getAllUnits() {
        return dataSource.allOrgUnits();
    }

    public ArrayList<BuyOrder> getAllBuys() { return dataSource.allBuyOrders();}

    public ArrayList<SellOrder> getAllSells() {return dataSource.allSellOrders();}

    public ArrayList<User> getMembers(OrgUnit unit) throws DoesNotExist {
        return dataSource.usersByUnit(unit.getName());
    }

    public ArrayList<User> getMembers(String unit) throws DoesNotExist {
        return dataSource.usersByUnit(unit);
    }

    public User getUserByKey(String key) throws DoesNotExist {
        User result = dataSource.userByKey(key);
        if (result == null) throw new DoesNotExist("The user '%s' does not exist.", key);
        return result;
    }

    public OrgUnit getUnitByKey(String unitName) throws DoesNotExist {
        // Convert username to User object while making sure it exists in the DB.
        OrgUnit result = dataSource.unitByKey(unitName);
        if (result == null) throw new DoesNotExist("The unit '%s' does not exist.", unitName);
        return result;
    }

    public Asset getAssetByKey(int key) throws DoesNotExist {
        Asset result = dataSource.assetByKey(key);
        if (result == null) throw new DoesNotExist("Asset '%s' does not exist.", key);
        return result;
    }

    public SellOrder getSellByKey(int key) throws DoesNotExist {
        SellOrder result = dataSource.sellOrderByKey(key);
        if (result == null) throw new DoesNotExist("Sell order '%s' does not exist.", key);
        return result;
    }

    public BuyOrder getBuyByKey(int key) throws DoesNotExist {
        BuyOrder result = dataSource.buyOrderByKey(key);
        if (result == null) throw new DoesNotExist("Buy order '%s' does not exist.", key);
        return result;
    }

    public InventoryRecord getInv(String unit, int asset) throws DoesNotExist {
        getUnitByKey(unit);
        getAssetByKey(asset);
        InventoryRecord result = dataSource.inventoryRecordByKeys(unit, asset);
        if (result == null) return new InventoryRecord(unit, asset, 0);
        //no record and a value of 0 are basically the same thing so no exception is needed
        return result;
    }

    public ArrayList<InventoryRecord> getInventoriesByOrgUnit(String unitName) throws DoesNotExist {
        getUnitByKey(unitName);
        return dataSource.inventoriesByUnit(unitName);
    }

    public ArrayList<InventoryRecord> getInventoriesByAsset(int assetID) throws DoesNotExist {
        getAssetByKey(assetID);
        return dataSource.inventoriesByAsset(assetID);
    }

    public ArrayList<BuyOrder> getResolvedBuysByAsset(int assetID) throws DoesNotExist {
        getAssetByKey(assetID);
        return dataSource.buyOrdersByAsset(assetID, true);
    }

    public ArrayList<BuyOrder> getUnresolvedBuysByAsset(int assetID) throws DoesNotExist {
        getAssetByKey(assetID);
        return dataSource.buyOrdersByAsset(assetID, false);
    }

    public ArrayList<BuyOrder> getBuysByUser(String username) throws DoesNotExist {
        getUserByKey(username);
        return dataSource.buyOrdersByUser(username, null);
    }

    //TODO: methods to get the buys resolved in the last session
    // and the sells affected by it


    //DELETE METHODS


    public void deleteAsset(int id) throws DoesNotExist, ConstraintException {
        int i = dataSource.deleteAsset(id);
        if (i == 0) throw new DoesNotExist("Asset '%s' not found", id);
        else if (i == -1) throw new ConstraintException("Asset '%s' could not be safely deleted."
                + "Delete any resolved buy orders for the asset and try again", id);
    }

    public void deleteUser(String name) throws DoesNotExist, ConstraintException {
        int i = dataSource.deleteUser(name);
        if (i == 0) throw new DoesNotExist("User '%s' not found", name);
        else if (i == -1) throw new ConstraintException("User '%s' could not be safely deleted."
                + "Delete all buy and sell orders placed by the user and try again", name);
    }

    public void deleteUnit(String name) throws DoesNotExist, ConstraintException {
        int i = dataSource.deleteUnit(name);
        if (i == 0) throw new DoesNotExist("Unit '%s' not found", name);
        else if (i == -1) throw new ConstraintException("Unit '%s' could not be safely deleted."
                + "Delete all buy and sell orders placed by members of the unit and try again", name);

    }

    public void cancelSellOrder(int id) throws ConstraintException, DoesNotExist {
        SellOrder s = getSellByKey(id); //this throws the doesnotexist if needed
        String unitToReturn = getUserByKey(s.getUser()).getUnit();
        int i = dataSource.deleteSellOrder(id);
        if (i == -1) throw new ConstraintException("Sell order '%i' could not be safely deleted."
                + "Delete any buy orders which have been reconciled with the order and try again", id);
        if (s.getDateResolved() == null) adjustInventory(unitToReturn, s.getAsset(), s.getQty());
        //return the assets if it's unresolved
    }

    public void cancelBuyOrder(int id) throws DoesNotExist, InvalidAmount {
        BuyOrder b = getBuyByKey(id);
        String unitToReturn = getUserByKey(b.getUser()).getUnit();

        dataSource.deleteBuyOrder(id);
        if (b.getDateResolved() == null) adjustUnitBalance(unitToReturn, b.getPrice()*b.getQty());
        //if (dataSource.deleteBuyOrder(id) == 0) throw new DoesNotExist("Buy order '%s' not found", id);
    }

    public void deleteInventoryRecord(String unit, int asset) throws DoesNotExist {
        if (dataSource.deleteInventoryRecord(unit, asset) == 0) {
            throw new DoesNotExist("Inventory information for this asset and unit '%s' does not exist", unit);
        }
    }

    //INSERT METHODS-----------------------------------------------------

    //TODO: prevent negative price or quantity
    public void placeSellOrder(SellOrder s) throws OrderException, DoesNotExist {
        //OrgUnit unitInQuestion = dataSource.unitByKey(dataSource.userByKey(s.getUsername()).getUnit());
        InventoryRecord inventoryRecord = getInv(
                getUserByKey(s.getUser()).getUnit(), s.getAsset());
        if (inventoryRecord.getQuantity() < s.getQty()) {
            throw new OrderException("Insufficient quantity of asset");
        } else {
            inventoryRecord.setQuantity(inventoryRecord.getQuantity() - s.getQty());
            dataSource.insertOrUpdateInventory(inventoryRecord);
            dataSource.insertSellOrder(s);
        }
    }

    public void placeBuyOrder(BuyOrder s) throws OrderException, InvalidAmount {
        OrgUnit unitInQuestion = dataSource.unitByKey(dataSource.userByKey(s.getUser()).getUnit());
        int neededCredits = s.getQty() * s.getPrice();
        if (unitInQuestion.getCredits() < neededCredits) {
            throw new OrderException(String.format("Insufficient credits- unit %s has %d but %d are needed to" +
                            "place this buy order",
                    unitInQuestion.getName(), unitInQuestion.getCredits(), neededCredits));
        } else {
            unitInQuestion.adjustBalance(-s.getQty());
            dataSource.updateUnit(unitInQuestion);
            dataSource.insertBuyOrder(s);
        }
    }

    public void addUser(User u) throws AlreadyExists, DoesNotExist {
        int result = dataSource.insertUser(u);
        if (result == 0)
            throw new AlreadyExists("User '%s' already exists. Please try a different username.", u.getUsername());
        else if (result == -1) throw new DoesNotExist("Could not create user- org unit %s does not exist");
    }

    public void addUnit(OrgUnit u) throws AlreadyExists {
        if (dataSource.insertUnit(u) == 0)
            throw new AlreadyExists("Unit '%s' already exists. Please try a different unit name.", u.getName());
    }

    public void addAsset(Asset a) throws AlreadyExists {
        if (dataSource.insertAsset(a) == 0) throw new AlreadyExists("Asset '%i' already exists.", a.getId());
    }

    public void setInventory(InventoryRecord i) throws DoesNotExist {
        if (dataSource.insertOrUpdateInventory(i) == -1) {
            throw new DoesNotExist("Unit %s and/or asset %i not found.");
        }
    }

    public void adjustInventory(String unit, int asset, int adjustment) throws DoesNotExist {
        InventoryRecord i = new InventoryRecord(unit, asset, adjustment);
        i.adjustQuantity(getInv(unit,asset).getQuantity()); //done this way to just set it to the value if no record exists
        if (dataSource.insertOrUpdateInventory(i) == -1) {
            throw new DoesNotExist("Unit %s and/or asset %i not found.");
        }
    }


    //UPDATE METHODS--------------------------------------------------------------------
    public void updateUser(User u) throws DoesNotExist {
        int result = dataSource.updateUser(u);
        if (result == 0) throw new DoesNotExist("User '%s' not found.", u.getUsername());
        else if (result == -1) throw new DoesNotExist("Unit %s not found.", u.getUnit());
    }

    public void updateUnit(OrgUnit u) throws DoesNotExist {
        if (dataSource.updateUnit(u) == 0) throw new DoesNotExist("Unit '%s' not found.", u.getName());
    }

    public void updateAsset(Asset a) throws DoesNotExist {
        if (dataSource.updateAsset(a) == 0) throw new DoesNotExist("Asset '%i' not found.", a.getId());
    }

    public void updateBuyOrder(BuyOrder o) throws DoesNotExist {
        int result = dataSource.updateBuyOrder(o);
        if (result == 0) throw new DoesNotExist("Buy order '%i' not found.", o.getId());
        else if (result == -1) throw new DoesNotExist("User and/or asset not found.");
    }

    public void updateSellOrder(SellOrder o) throws DoesNotExist {
        int result = dataSource.updateSellOrder(o);
        if (result == 0) throw new DoesNotExist("Sell order '%i' not found.", o.getId());
        else if (result == -1) throw new DoesNotExist("User and/or asset not found.");
    }


    //SPECIAL CASE METHODS---------------------------------------------------------
    public void updateUserUnit(String username, String unitName) throws DoesNotExist {
        //Retrieve objects to ensure they exist
        User userInQuestion = getUserByKey(username);
        OrgUnit unitInQuestion = getUnitByKey(unitName);
        userInQuestion.setUnit(unitName);
        updateUser(userInQuestion);
    }

    public void updateUserAccess(String username, Boolean newAccess) throws DoesNotExist {
        //Retrieve object to ensure existence
        User userInQuestion = getUserByKey(username);
        userInQuestion.setAdminAccess(newAccess);
        updateUser(userInQuestion);
    }

    public void changeUserPassword(String username, String newPass) throws IllegalString, DoesNotExist {
        //Retrieve object to ensure existence
        User userInQuestion = getUserByKey(username);
        userInQuestion.changePassword(newPass);
        updateUser(userInQuestion);
    }

    public void changeUnitBalance(String unitName, int newBalance) throws DoesNotExist, InvalidAmount {
        OrgUnit unitInQuestion = getUnitByKey(unitName);
        unitInQuestion.setBalance(newBalance);
        updateUnit(unitInQuestion);
    }

    public void adjustUnitBalance(String unitName, int amount) throws DoesNotExist, InvalidAmount {
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
    public double getAveragePrice(LocalDate startDate, LocalDate endDate, Asset asset) throws InvalidDate, DoesNotExist {
        return getAveragePrice(startDate, endDate, asset.getId());
    }
    public double getAveragePrice(LocalDate startDate, LocalDate endDate, int asset) throws InvalidDate, DoesNotExist {

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

    public TreeMap<LocalDate, Double> getHistoricalPrices(Asset a, Intervals timeInterval) throws InvalidDate, DoesNotExist {
        return getHistoricalPrices(a.getId(), timeInterval);
    }
    /***
     * Method that collects average prices between specified intervals for the entire data set and places them into a
     * TreeMap. This may be used to create a price history graph.
     * @param timeInterval the time interval in which the data will be split before calculating the averages of each
     *                     interval. Constants are provided as days, 3 days, weeks, months and years.
     * @return returns a TreeMap with each intervals start date as a key, with its value being the corresponding average.
     */
    public TreeMap<LocalDate, Double> getHistoricalPrices(int a, Intervals timeInterval) throws InvalidDate, DoesNotExist {
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


    //USER METHODS ---------------------------------------------------------------------------

    /**
     * User client method to get an ArrayList of all OrgUnits in the database
     * @return
     */
    public ArrayList<OrgUnit> clientGetAllUnits() {
        return getAllUnits();
    }

    /**
     * User client method to get an ArrayList of all the users in a specific unit
     * @param unit
     * @return
     */
    public ArrayList<User> clientGetUnitMembers(OrgUnit unit) {
        try {
            return getMembers(unit);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * User client method to get an ArrayList of the names of the members of a specific unit
     * @param unit
     * @return
     */
    public ArrayList<String> clientGetUnitMemberNames(OrgUnit unit) {
        ArrayList<String> results = new ArrayList<>();
        try {
            ArrayList<User> users = getMembers(unit);
            for (User u : users) results.add(u.getUsername());
            return results;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * User client method to get an ArrayList of all users in the same unit as `loggedin`
     * @param loggedin
     * @return
     */
    public ArrayList<User> clientGetUnitMates(User loggedin) {
        try {
            return getMembers(loggedin.getUnit());
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * User client method to query for a user by their name
     * @param name
     * @return
     */
    public User clientGetUser(String name) {
        try {
            return getUserByKey(name);
        }
        catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * Place a new buy order with the parameters passed, with user set to loggedin
     * @param loggedin
     * @param asset
     * @param qty
     * @param price
     */
    public void clientPlaceNewBuyOrder(User loggedin, int asset, int qty, int price) {
        BuyOrder order = new BuyOrder(loggedin.getUsername(),asset, qty, price);
        try{placeBuyOrder(order);}
        catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * Place a new sell order with the parameters passed, with user set to loggedin
     * @param loggedin
     * @param asset
     * @param qty
     * @param price
     */
    public void clientPlaceNewSellOrder(User loggedin, int asset, int qty, int price) {
        SellOrder order = new SellOrder(loggedin.getUsername(),asset, qty, price);
        try{placeSellOrder(order);}
        catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * Cancel buy order identified by orderID (loggedIn is passed in case it needs checked)
     * @param loggedin
     * @param orderID
     */
    public void clientCancelBuyOrder(User loggedin, int orderID) {
        //Uncomment and finish throw statement if non-admin users should be unable to cancel other users' buy orders
//        if ((getBuyByKey(orderID).getUsername() != loggedin.getUsername()) && !loggedin.getAdminAccess()) {
//            throw new //some sort of exception here
//        }
        try {
            cancelBuyOrder(orderID);
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }

    /**
     * Cancel sell order identified by orderID (loggedIn is passed in case it needs checked)
     * @param loggedin
     * @param orderID
     */
    public void clientCancelSellOrder(User loggedin, int orderID) {
        //Uncomment and finish throw statement if non-admin users should be unable to cancel other users' buy orders
//        if ((getBuyByKey(orderID).getUsername() != loggedin.getUsername()) && !loggedin.getAdminAccess()) {
//            throw new //some sort of exception here
//        }
        try {
            cancelSellOrder(orderID);
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }

    /**
     * Change password of loggedIn to newPass
     * @param loggedin
     * @param newPass
     */
    public void clientChangeOwnPassword(User loggedin, String newPass) {
        try {
            changeUserPassword(loggedin.getUsername(), newPass);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    //ADMIN METHODS ------------------------------------------------

    //Helper to throw NotAuthorised
    private void failIfNotAdmin(String thingAttempted, User loggedin) throws NotAuthorised {
        if (!loggedin.getAdminAccess()) throw new NotAuthorised("You do not have permission to: " + thingAttempted);
    }
    //iff loggedIn is an admin, change `username`'s password to `newpass`
    public boolean changePassIfAdmin(String username, String newPass, User loggedin)
            throws NotAuthorised, IllegalString, DoesNotExist {
        failIfNotAdmin("change a user's password", loggedin);
        changeUserPassword(username, newPass);
        return true;
    }
    //iff loggedIn is an admin, change `username`'s access to `newAccess`
    public boolean changeAccessIfAdmin(String username, Boolean newAccess, User loggedin)
            throws NotAuthorised, IllegalString, DoesNotExist {
        failIfNotAdmin("change a user's admin access", loggedin);
        updateUserAccess(username, newAccess);
        return true;
    }
    //iff loggedIn is an admin, create new user as specified and add to DB
    public boolean createUserIfAdmin(String username, String password, boolean adminAccess, User loggedin)
            throws NotAuthorised, IllegalString, AlreadyExists, DoesNotExist {
        failIfNotAdmin("create a new user", loggedin);
        User newUser = new User(username, password, adminAccess);
        addUser(newUser);
        return true;
    }
    //iff loggedIn is an admin, create new asset as specified and add to DB
    public boolean createAssetIfAdmin(int assetID, String assetDesc, User loggedin)
            throws NotAuthorised, AlreadyExists {
        failIfNotAdmin("create a new asset", loggedin);
        Asset newAsset = new Asset(assetID, assetDesc);
        addAsset(newAsset);
        return true;
    }
    //iff loggedIn is an admin, create new unit as specified and add to DB
    public boolean createUnitIfAdmin(String name, User loggedin)
            throws NotAuthorised, AlreadyExists, IllegalString {
        failIfNotAdmin("create a new organisational unit", loggedin);
        OrgUnit newUnit = new OrgUnit(name);
        addUnit(newUnit);
        return true;
    }
    //iff loggedIn is an admin, delete `username` user
    public boolean deleteUserIfAdmin(String username, User loggedin) throws NotAuthorised, DoesNotExist, ConstraintException {
        failIfNotAdmin("delete a user", loggedin);
        deleteUser(username);
        return true;
    }
    //iff loggedIn is an admin, delete `unitname` unit
    public boolean deleteUnitIfAdmin(String unitname, User loggedin) throws NotAuthorised, DoesNotExist, ConstraintException {
        failIfNotAdmin("delete a unit", loggedin);
        deleteUnit(unitname);
        return true;
    }
    //iff loggedIn is an admin, delete asset `id`
    public boolean deleteAssetIfAdmin(int id, User loggedin) throws NotAuthorised, DoesNotExist, ConstraintException {
        failIfNotAdmin("delete an asset", loggedin);
        deleteAsset(id);
        return true;
    }
    //iff loggedIn is an admin, set the unit of `username` to null
    public boolean nullifyUserUnitIfAdmin(String username, User loggedin) throws DoesNotExist, NotAuthorised {
        failIfNotAdmin("remove a user from their unit", loggedin);
        updateUserUnit(username, null); //currently this does not throw an error if already null
        return true;
    }
    //iff loggedIn is an admin, set the unit of `username` to `unit`, if their unit is currently null
    public boolean setUserUnitIfAdmin(String username, String unit, User loggedin)
            throws NotAuthorised, DoesNotExist, AlreadyExists {
        failIfNotAdmin("add a user to a unit", loggedin);
        String currentUnit = getUserByKey(username).getUnit();
        if (currentUnit != null) throw new AlreadyExists("User '%s' is already part of a unit '%s' " +
                "Please remove them from the unit before adding to a new unit.", username, currentUnit);
        updateUserUnit(username, unit);
        return true;
    }

    //iff loggedIn is an admin, add or update an inventory record such that one exists matching the specifications
    //(these two operations were different in Interactions, but they share a query so I've merged them)
    public boolean setInventoryQtyIfAdmin(String unit, int asset, int qty, User loggedin) throws NotAuthorised, InvalidAmount, DoesNotExist {
        failIfNotAdmin("set an asset's quantity", loggedin);
        if (qty < 0) throw new InvalidAmount("Cannot set quantity of asset to %i% as this is less than 0", qty);
        setInventory(new InventoryRecord(unit, asset, qty));
        return true;
    }
    //iff loggedIn is an admin, adjust the quantity of an inventory record according to the specifications
    //(`change` can be negative; invalid amount is checked in called methods)
    public boolean adjustInventoryQtyIfAdmin(String unit, int asset, int change, User loggedin) throws NotAuthorised, InvalidAmount, DoesNotExist {
        int currentqty = getInv(unit, asset).getQuantity();
        setInventoryQtyIfAdmin(unit, asset, currentqty + change, loggedin);
        return true;
    }
    //iff loggedIn is an admin, delete the inventory record matching the specifications
    public boolean deleteInventoryRecordIfAdmin(String unit, int asset, User loggedin)
            throws NotAuthorised, DoesNotExist {
        failIfNotAdmin("remove asset inventory information", loggedin);
        deleteInventoryRecord(unit, asset);
        return true;
    }
    //iff loggedIn is an admin, set `unit`'s balance to `newbalance`
    public boolean setUnitBalanceIfAdmin(String unit, int newbalance, User loggedin)
            throws NotAuthorised, InvalidAmount, DoesNotExist {
        failIfNotAdmin("set a unit's credit balance", loggedin);
        changeUnitBalance(unit, newbalance);
        return true;
    }
    //iff loggedIn is an admin, adjust `unit`'s balance by `amount` (can be negative)
    public boolean adjustUnitBalanceIfAdmin(String unit, int amount, User loggedin)
            throws NotAuthorised, InvalidAmount, DoesNotExist {
        failIfNotAdmin("set a unit's credit balance", loggedin);
        adjustUnitBalance(unit, amount);
        return true;
    }

}
