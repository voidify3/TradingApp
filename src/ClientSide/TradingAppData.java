package ClientSide;

import common.*;
import common.Exceptions.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.chrono.ChronoLocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;

public class TradingAppData {
    public TradingAppDataSource dataSource;
    public static enum Intervals {
        DAYS,
        WEEKS,
        MONTHS,
        YEARS
    }

    //DEV/HELPER CONTENT --------------------------------------------------------------------
    public static User adminDev;
    public static User userDev;
    public static OrgUnit unitDev;
    public static Asset assetDev1;
    public static Asset assetDev2;


    public void addHistoricalPrice(int assetID, String userResponsible, int price, LocalDateTime dateTime) {
        SellOrder sell = new SellOrder(Order.nextId++, userResponsible, assetID, 0, price, dateTime, dateTime);
        BuyOrder buy = new BuyOrder(Order.nextId++, userResponsible, assetID, 0, price, dateTime, dateTime, sell.getId());
        dataSource.insertSellOrder(sell);
        dataSource.insertBuyOrder(buy);
    }

    public void mockObjectsWithPrices() throws IllegalString {
        LocalDateTime today = LocalDateTime.now();
        adminDev = new User("johnny", "bo$$man", true);
        userDev = new User("scott", "scotty", false);
        unitDev = new OrgUnit("Developers");
        assetDev1 = new Asset(999, "Test asset for development!");
        assetDev2 = new Asset(333, "Another test asset for development!");
        dataSource.deleteUnit(unitDev.getName());
        dataSource.insertUnit(unitDev);
        userDev.setUnit(unitDev.getName());
        adminDev.setUnit(unitDev.getName());
        dataSource.deleteUser(adminDev.getUsername());
        dataSource.insertUser(adminDev);
        dataSource.deleteUser(userDev.getUsername());
        dataSource.insertUser(userDev);
        dataSource.deleteAsset(assetDev1.getId());
        dataSource.deleteAsset(assetDev2.getId());
        dataSource.insertAsset(assetDev1);
        dataSource.insertAsset(assetDev2);
        for (int i = 0; i < 4 * 365; i++) {
            addHistoricalPrice(assetDev1.getId(), adminDev.getUsername(), 10, today.minusDays(i));
            addHistoricalPrice(assetDev1.getId(), adminDev.getUsername(), 15, today.minusDays(i));
            addHistoricalPrice(assetDev1.getId(), adminDev.getUsername(), 20, today.minusDays(i));
            addHistoricalPrice(assetDev2.getId(), adminDev.getUsername(), 10, today.minusDays(i));
            addHistoricalPrice(assetDev2.getId(), adminDev.getUsername(), 30, today.minusDays(i));
            addHistoricalPrice(assetDev2.getId(), adminDev.getUsername(), 50, today.minusDays(i));
        }
        dataSource.insertOrUpdateInventory(new InventoryRecord(unitDev.getName(), assetDev1.getId(), 1000));
        dataSource.insertOrUpdateInventory(new InventoryRecord(unitDev.getName(), assetDev2.getId(), 3500));
    }
    public void mockObjects() throws IllegalString {
        LocalDateTime today = LocalDateTime.now();
        adminDev = new User("johnny", "bo$$man", true);
        userDev = new User("scott", "scotty", false);
        unitDev = new OrgUnit("Developers");
        assetDev1 = new Asset(999, "Test asset for development!");
        assetDev2 = new Asset(333, "Another test asset for development!");
        dataSource.deleteUnit(unitDev.getName());
        dataSource.insertUnit(unitDev);
        userDev.setUnit(unitDev.getName());
        adminDev.setUnit(unitDev.getName());
        dataSource.deleteUser(adminDev.getUsername());
        dataSource.insertUser(adminDev);
        dataSource.deleteUser(userDev.getUsername());
        dataSource.insertUser(userDev);
        dataSource.deleteAsset(assetDev1.getId());
        dataSource.deleteAsset(assetDev2.getId());
        dataSource.insertAsset(assetDev1);
        dataSource.insertAsset(assetDev2);
        dataSource.insertOrUpdateInventory(new InventoryRecord(unitDev.getName(), assetDev1.getId(), 1000));
        dataSource.insertOrUpdateInventory(new InventoryRecord(unitDev.getName(), assetDev2.getId(), 3500));
    }

    public void deleteEverything() {
        dataSource.debugDeleteEverything();
    }

    public TradingAppData() {
        dataSource = new MockDataSource();
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
    public InventoryRecord getInv(String unit, int asset) {
        InventoryRecord result = dataSource.inventoryRecordByKeys(unit,asset);
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




    public void deleteAsset(int id) throws DoesNotExist, ConstraintException {
        int i = dataSource.deleteAsset(id);
        if (i==0)  throw new DoesNotExist("Asset '%s' not found", id);
        else if (i==-1) throw new ConstraintException("Asset '%s' could not be safely deleted."
                + "Delete any resolved buy orders for the asset and try again", id);
    }
    public void deleteUser(String name) throws DoesNotExist, ConstraintException {
        int i = dataSource.deleteUser(name);
        if (i==0)  throw new DoesNotExist("User '%s' not found", name);
        else if (i==-1) throw new ConstraintException("User '%s' could not be safely deleted."
                + "Delete all buy and sell orders placed by the user and try again", name);
    }
    public void deleteUnit(String name) throws DoesNotExist {
        if (dataSource.deleteUnit(name) == 0) throw new DoesNotExist("Unit '%s' not found", name);
    }
    public void cancelSellOrder(int id) throws ConstraintException, DoesNotExist {
        int i = dataSource.deleteSellOrder(id);
        if (i==0)  throw new DoesNotExist("Sell order '%i' not found", id);
        else if (i==-1) throw new ConstraintException("Sell order '%i' could not be safely deleted."
                + "Delete any buy orders which have been reconciled with the order and try again", id);
    }
    public void cancelBuyOrder(int id) throws DoesNotExist {
        if (dataSource.deleteBuyOrder(id)  == 0) throw new DoesNotExist("Buy order '%s' not found", id);
    }
    public void deleteInventoryRecord(String unit, int asset) throws DoesNotExist {
        if (dataSource.deleteInventoryRecord(unit, asset)  == 0) {
            throw new DoesNotExist("Inventory information for this asset and unit '%s' does not exist", unit);
        }
    }

    //INSERT METHODS-----------------------------------------------------
    public void placeSellOrder(SellOrder s) throws OrderException, DoesNotExist {
        //OrgUnit unitInQuestion = dataSource.unitByKey(dataSource.userByKey(s.getUsername()).getUnit());
        InventoryRecord inventoryRecord = getInv(
                getUserByKey(s.getUser()).getUnit(), s.getAsset());
        if (inventoryRecord.getQuantity() < s.getQty()) {
            throw new OrderException("Insufficient quantity of asset");
        }
        else {
            inventoryRecord.setQuantity(inventoryRecord.getQuantity() - s.getQty());
            dataSource.insertOrUpdateInventory(inventoryRecord);
            dataSource.insertSellOrder(s);
        }
    }
    public void placeBuyOrder(BuyOrder s) throws OrderException, InvalidAmount {
        OrgUnit unitInQuestion = dataSource.unitByKey(dataSource.userByKey(s.getUser()).getUnit());
        if (unitInQuestion.getCredits() < s.getQty() * s.getPrice()) {
            throw new OrderException("Insufficient credits");
        }
        else {
            unitInQuestion.adjustBalance(-s.getQty());
            dataSource.updateUnit(unitInQuestion);
            dataSource.insertBuyOrder(s);
        }
    }
    //TODO:properly set up lookaheads for FKs
    public void addUser(User u) throws AlreadyExists, DoesNotExist {
        int result = dataSource.insertUser(u);
        if (result==0) throw new AlreadyExists("User '%s' already exists. Please try a different username.", u.getUsername());
        else if (result ==-1) throw new DoesNotExist("Org unit %s does not exist");
    }
    public void addUnit(OrgUnit u) throws AlreadyExists {
        if (dataSource.insertUnit(u)==0) throw new AlreadyExists("Unit '%s' already exists. Please try a different unit name.", u.getName());
    }
    public void addAsset(Asset a) throws AlreadyExists {
        if (dataSource.insertAsset(a) == 0) throw new AlreadyExists("Asset '%i' already exists.", a.getId());
    }
    public void setInventory(InventoryRecord i) throws DoesNotExist {
        if (dataSource.insertOrUpdateInventory(i)==-1) {
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
        else if (result==-1) throw new DoesNotExist("User and/or asset not found.");
    }
    public void updateSellOrder(SellOrder o) throws DoesNotExist {
        int result = dataSource.updateSellOrder(o);
        if (result == 0) throw new DoesNotExist("Sell order '%i' not found.", o.getId());
        else if (result==-1) throw new DoesNotExist("User and/or asset not found.");
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
     * @return returns an int of the average price.
     */
    public int getAveragePrice(LocalDate startDate, LocalDate endDate, Asset asset) throws InvalidDate, DoesNotExist {

        LocalDate earliestDate;
        LocalDate today = LocalDate.now();
        try{
            //explanation of this logic: get the date of the earliest resolved BuyOrder for the asset
        earliestDate = (getResolvedBuysByAsset(asset.getId()).stream().min(BuyOrder::compareTo).orElseThrow().getDateResolved()).toLocalDate();}
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
        ArrayList<BuyOrder> transactions = dataSource.buyOrdersByAssetResolvedBetween(asset.getId(),
                Timestamp.valueOf(startDate.atStartOfDay()), Timestamp.valueOf(endDate.atTime(23,59,59)));
        for (BuyOrder b : transactions) {
            sum += b.getPrice();
            count++;
        }
        if (count == 0) return 0;
        return sum / count;
    }

    /***
     * Method that collects average prices between specified intervals for the entire data set and places them into a
     * TreeMap. This may be used to create a price history graph.
     * @param timeInterval the time interval in which the data will be split before calculating the averages of each
     *                     interval. Constants are provided as days, 3 days, weeks, months and years.
     * @return returns a TreeMap with each intervals start date as a key, with its value being the corresponding average.
     */
    public TreeMap<LocalDate, Integer> getHistoricalPrices(Asset a, Intervals timeInterval) throws InvalidDate, DoesNotExist {
        ArrayList<BuyOrder> priceHistory = getResolvedBuysByAsset(a.getId());
        Optional<BuyOrder> earliest = priceHistory.stream().min(BuyOrder::compareTo);
        LocalDateTime earliestDate = earliest.get().getDateResolved();
        Optional<BuyOrder> latest = priceHistory.stream().max(BuyOrder::compareTo);
        LocalDateTime latestDate = latest.get().getDateResolved();
        // Create new TreeMap for the averages
        TreeMap<LocalDate, Integer> averages = new TreeMap<>();

        // Calculate number of intervals for each interval
        long days = ChronoUnit.DAYS.between(earliestDate, latestDate);
        long weeks = ChronoUnit.WEEKS.between(earliestDate, latestDate);
        long months = ChronoUnit.MONTHS.between(earliestDate, latestDate);
        long years = ChronoUnit.YEARS.between(earliestDate, latestDate);
        Locale locale = Locale.ENGLISH;
        ZoneId timeZone = ZoneId.of("Australia/Sydney");

        //TODO: this seems like unnecessary repetition!!!!
        switch (timeInterval) {
            case DAYS -> {
                LocalDate endDate = LocalDate.now();
                LocalDate startDate = endDate;
                System.out.println("Daily average prices");
                for (int i = 0; i < days + 1; i++) {
                    averages.put(startDate, getAveragePrice(startDate, endDate, a));
                    System.out.println(startDate + " = " + averages.get(startDate));
                    endDate = startDate.minusDays(1);
                    startDate = endDate;
                }
            }
            case WEEKS -> {

                LocalDate endDate = LocalDate.now();
                LocalDate startDate = LocalDate.now(timeZone).
                        with(TemporalAdjusters.previousOrSame(WeekFields.of(locale).getFirstDayOfWeek()));
                System.out.println("Weekly average prices between:");
                for (int i = 0; i <= weeks + 1; i++) {
                    averages.put(startDate, getAveragePrice(startDate, endDate, a));
                    System.out.println(startDate + " - " + endDate + " = " + averages.get(startDate));

                    if (startDate.minusDays(1).isBefore(ChronoLocalDate.from(earliestDate)))
                        break;

                    if (startDate.minusWeeks(1).isAfter(ChronoLocalDate.from(earliestDate))
                            || startDate.minusDays(1).isAfter(ChronoLocalDate.from(earliestDate)))
                        endDate = startDate.minusDays(1);
                    else
                        endDate = LocalDate.from(earliestDate);

                    if (endDate.minusWeeks(1).isAfter(ChronoLocalDate.from(earliestDate)))
                        startDate = endDate.minusWeeks(1).plusDays(1);
                    else
                        startDate = LocalDate.from(earliestDate);
                }
            }
            case MONTHS -> {
                LocalDate endDate = LocalDate.now();
                LocalDate startDate = LocalDate.now().withDayOfMonth(1);
                System.out.println("Monthly average prices between:");
                for (int i = 0; i <= months + 1; i++) {
                    averages.put(startDate, getAveragePrice(startDate, endDate, a));
                    System.out.println(startDate + " - " + endDate + " = " + averages.get(startDate));

                    if (startDate.minusDays(1).isBefore(ChronoLocalDate.from(earliestDate)))
                        break;

                    if (startDate.minusMonths(1).isAfter(ChronoLocalDate.from(earliestDate))
                            || startDate.minusDays(1).isAfter(ChronoLocalDate.from(earliestDate)))
                        endDate = startDate.minusDays(1);
                    else
                        endDate = LocalDate.from(earliestDate);

                    if (endDate.minusMonths(1).isAfter(ChronoLocalDate.from(earliestDate)))
                        startDate = endDate.withDayOfMonth(1);
                    else
                        startDate = LocalDate.from(earliestDate);
                }
            }
            case YEARS -> {
                LocalDate endDate = LocalDate.now();
                LocalDate startDate = LocalDate.now().withDayOfYear(1);
                System.out.println("Yearly average prices between:");
                for (int i = 0; i <= years + 1; i++) {
                    averages.put(startDate, getAveragePrice(startDate, endDate, a));
                    System.out.println(startDate + " - " + endDate + " = " + averages.get(startDate));

                    if (startDate.minusDays(1).isBefore(ChronoLocalDate.from(earliestDate)))
                        break;

                    if (startDate.minusYears(1).isAfter(ChronoLocalDate.from(earliestDate))
                            || startDate.minusDays(1).isAfter(ChronoLocalDate.from(earliestDate)))
                        endDate = startDate.minusDays(1);
                    else
                        endDate = LocalDate.from(earliestDate);

                    if (endDate.minusYears(1).isAfter(ChronoLocalDate.from(earliestDate)))
                        startDate = endDate.withDayOfYear(1);
                    else
                        startDate = LocalDate.from(earliestDate);
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
            throws NotAuthorised, AlreadyExists {
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
