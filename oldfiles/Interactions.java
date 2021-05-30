package ClientSide;


import ServerSide.deprecated.MockDatabase;
import common.*;
import common.Exceptions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

/***
 * @authors Johnny Madigan & Scott Peachey
 */
public class Interactions {
    
    private MockDatabase db = new MockDatabase();

    // Dev users, units, and assets
    public static User adminDev;
    public static User userDev;
    public static OrgUnit unitDev;
    public static Asset assetDev1;
    public static Asset assetDev2;
    public static LocalDate today = LocalDate.now();


    public void mockObjects() throws IllegalString, AlreadyExists, InvalidPrice, InvalidDate {
        // Temporary users, unit and assets for testing as JUnit cannot test swing (comment out block to disable)
        adminDev = new User("johnny", "bo$$man", true);
        // remove all old versions of this user before adding
        while (db.getAllUsers().contains(adminDev)) {
            db.getAllUsers().remove(adminDev);
        }
        db.addUser(adminDev);

        userDev = new User("scott", "scotty", false);
        // remove all old versions of this user before adding
        while (db.getAllUsers().contains(userDev)) {
            db.getAllUsers().remove(userDev);
        }
        db.addUser(userDev);

        unitDev = new OrgUnit("Developers");
        // remove all old versions of this unit before adding
        while (db.getAllUnits().contains(unitDev)) {
            db.getAllUnits().remove(unitDev);
        }
        db.addUnit(unitDev);

        assetDev1 = new Asset(999, "Test asset for development!");
        // remove all old versions of this asset before adding
        while (db.getAllAssets().contains(assetDev1)) {
            db.getAllAssets().remove(assetDev1);
        }
        db.addAsset(assetDev1);
        for (int i = 0; i < 2 * 366; i++) {
            assetDev1.addHistoricalPrice(10, today.minusDays(i));
            assetDev1.addHistoricalPrice(15, today.minusDays(i));
            assetDev1.addHistoricalPrice(20, today.minusDays(i));
        }

        assetDev2 = new Asset(333, "Another test asset for development!");
        // remove all old versions of this asset before adding
        while (db.getAllAssets().contains(assetDev2)) {
            db.getAllAssets().remove(assetDev2);
        }
        db.addAsset(assetDev2);
        for (int i = 0; i < 2 * 366; i++) {
            assetDev2.addHistoricalPrice(10, today.minusDays(i));
            assetDev2.addHistoricalPrice(50, today.minusDays(i));
            assetDev2.addHistoricalPrice(30, today.minusDays(i));
        }

        //TODO add user to unit
        // make mock assets & add to unit
        // call method getAssets

        unitDev.addAssets(assetDev1, 1000);
        unitDev.addAssets(assetDev2, 3500);
        userDev.setUnit("Developers");
        adminDev.setUnit("Developers");
    }
    // LOGIN------------------------------------------------------------------------------------------------------------
    /***
     * After the user types in their username & password into the login form, this method will use the username
     * input to get the server-side to return the matching user object. The user object's SALT is then hashed
     * with the password input, which is how we verify hashed passwords as SHA-512 is a one-way encryption.
     * If successful, the user object will be stored in the system for the duration of the session.
     * @param usernameInput the username typed into the form
     * @param passwordInput the password typed into the form
     * @throws DoesNotExist if the username / password input does not match anything in the database
     * @throws IllegalString if the password does not meet requirements during hashing
     */
    public User login(String usernameInput, String passwordInput) throws DoesNotExist, IllegalString {
        // Get the DB user using the username input
        // so we can hash the password input with the matching SALT
        User user = db.getUser(usernameInput);
        String hashedInputPassword = User.hashPassword(passwordInput, user.getSalt());

        // Match the hashed password input with the password (case sensitive)
        if (!hashedInputPassword.equals(user.getPassword())) {
            throw new DoesNotExist("Invalid password, please try again.");
        }
        return user;
    }

    // USER METHODS-----------------------------------------------------------------------------------------------------
    public ArrayList<OrgUnit> getAllUnits() {
        return db.getAllUnits();
    }

    /***
     * Getter method for the org unit's members.
     * @return list of users that are members of this unit
     */
    public ArrayList<String> getMembers(OrgUnit unit) throws DoesNotExist {
        return db.unitMembers(unit.getName());
    }
    //TODO update this to the server side class' final method to retrieve all members
    // with the same org

    public OrgUnit getUnit(String unitName) {
        try {
            // Convert username to User object while making sure it exists in the DB.
            return db.getUnit(unitName);
        } catch (DoesNotExist ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public OrgUnit getUnit(User user) {
        try {
            // Convert username to User object while making sure it exists in the DB.
            return db.getUnit(user.getUnit());
        } catch (DoesNotExist ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public User getUser(String username) {
        try {
            // Convert username to User object while making sure it exists in the DB.
            return db.getUser(username);
        } catch (DoesNotExist ex) {
            ex.printStackTrace();
            return null;
        }
    }

    // TESTING johnny testing pls don't touch------------
    /***
     * Method to get a list of all user's usernames to use via search.
     * @return ArrayList of the asset & quantity
     */
    public ArrayList<String> getAllUsers() {
        ArrayList<User> allUserObjects = db.getAllUsers();
        ArrayList<String> names = new ArrayList<>();
        for (User u: allUserObjects) {
            names.add(u.getUsername());
        }
        return names;
    }

    /***
     * General user method for placing a sell order.
     * @param order SellOrder object
     * @throws DoesNotExist if the user is not part of an unit when attempting to get user's unit's assets
     * @throws OrderException if the order conditions are not met
     */
    public  void placeSellOrder(SellOrder order) throws DoesNotExist, OrderException {
        String userName = order.getUsername();
        int assetID = order.getAssetID();
        String orgUnitID = db.getUser(order.user).getUnit();

        HashMap<Asset, Integer> availableAssets = this.getUnit(order.user).getAssets();

        if (availableAssets.containsKey(order.asset)) {
            if (availableAssets.get(order.asset) >= order.qty) {
                // Add sell order to the order queue to be reconciled
                db.addSellOrder(order);
            } else {
                throw new OrderException("Order quantity exceeds the amount of the specified asset held by the users organisation.");
            }
        } else {
            throw new OrderException("Asset not in Organisations Stock");
        }
    }

    /***
     * General user method for placing a buy order.
     * @param assetName asset to buy
     * @param qty quantity to buy
     * @param price price to buy at
     * @throws DoesNotExist if the user is not part of an unit when attempting to get user's unit's assets
     * @throws OrderException if the order conditions are not met
     */
    public  void placeBuyOrder(BuyOrder order) throws DoesNotExist, OrderException {
        String userName = order.getUsername();
        int assetID = order.getAssetID();
        String orgUnitID = order.user.getUnit();

        ArrayList<Asset> availableAssets = db.getAllAssets();
        int totalPrice = order.qty * order.price;
        int funds = this.getUnit(order.user).getCredits();


        if (availableAssets.contains(order.asset)) {
            if (totalPrice <= funds) {
                // Add buy order to the order queue to be reconciled
                db.addBuyOrder(order);
            } else {
                throw new OrderException("Order price exceeds the total funds held by the users organisation.");
            }
        } else {
            throw new DoesNotExist("Asset has not yet been added to the database.");
        }
    }

    /***
     * General user method to cancel an order
     * @param orderID Id of the order that is being cancelled. Used as a unique reference to search the database.
     */
    public void cancelOrder(int orderID) {
        //TODO call Order class method
    }

    /***
     * Method to get a user's unit's assets.
     * @return TreeMap of the asset & quantity
     * @throws DoesNotExist if the user is not part of an org unit yet
     */
    public HashMap<Asset, Integer> getAssets() throws DoesNotExist {
        OrgUnit usersUnit = getUnit(Gui.user);
        if (usersUnit == null) {
            throw new DoesNotExist("You are not part of an organisational unit yet.");
        }
        return (usersUnit.getAssets());
    }

    /***
     * Method to get credits of the org unit the user is a member of.
     * @return user's org unit's credits
     * @throws DoesNotExist if the user is not part of an org unit yet
     */
    public int getCredits(String name) throws DoesNotExist {
        // convert username string to User object & make sure it exists in the DB.
        User userSelected = db.getUser(name);
        if (getUnit(userSelected) == null) {
            throw new DoesNotExist("You are not currently part of any Organisational Unit.");
        } else {
            return getUnit(Gui.user).getCredits();
        }
    }

    // ADMIN METHODS FOR THE REST OF THE FILE---------------------------------------------------------------------------
    /***
     * Admin ability to change any user's password.
     * @param newPassword to change to
     * @param username the corresponding username
     * @throws DoesNotExist if the user does not exist
     * @throws NotAuthorised if the user is not an admin
     */
    public boolean changePasswordAdmin(String username, String newPassword, User user) throws DoesNotExist, NotAuthorised, IllegalString {
        if (user.getAdminAccess()) {
            User userSelected;
            // convert username string to User object & make sure it exists in the DB.
            userSelected = db.getUser(username);

            String hashedPassword = User.hashPassword(newPassword, userSelected.getSalt());
            userSelected.setPassword(hashedPassword);
            return true;
        } else {
            throw new NotAuthorised("You do not have permission to: change a user's password.");
        }
    }

    /***
     * Admin method to change a user's access level.
     * @param username the user whose access level wishes to be changed
     * @param newAccess the new access level
     * @throws NotAuthorised if user is not an admin
     */
    public boolean changeAccess(String username, boolean newAccess, User user) throws DoesNotExist, NotAuthorised {
        if (user.getAdminAccess()) {
            // Find the user object in the DB. Will throw an exception if the user does not exist.
            User UserToFind = db.getUser(username);
            UserToFind.setAdminAccess(newAccess);
            return true;
        } else {
            throw new NotAuthorised("You do not have permission to: change a user's access.");
        }
    }

    // ADD & DELETE USERS, UNITS, & ASSETS
    /***
     * Admin method to create a new user object.
     * @param username the user's unique username
     * @param password the user's password
     * @param adminAccess the initial access level for the user
     * @return true if the user was created
     * @throws AlreadyExists if the user already exists in the DB
     * @throws NotAuthorised if user is not an admin
     * @throws IllegalString if the username OR password cannot be used
     */
    public boolean newUser(String username, String password, boolean adminAccess, User user) throws NotAuthorised, IllegalString, AlreadyExists {
        if (user.getAdminAccess()) {
            User newUser = new User(username, password, adminAccess);
            db.addUser(newUser);
            return true;
        } else {
            throw new NotAuthorised("You do not have permission to: create a new user.");
        }
    }

    /***
     * Admin method to create a new unit object.
     * @param unitName unit's name
     * @return true if the unit was created
     * @throws NotAuthorised
     * @throws AlreadyExists
     */
    public boolean newUnit(String unitName, User user) throws NotAuthorised, AlreadyExists {
        if (user.getAdminAccess()) {
            db.addUnit(new OrgUnit(unitName));
            return true;
        } else {
            throw new NotAuthorised("You do not have permission to: create a new unit.");
        }
    }

    /***
     * Admin method to create a new asset object.
     * @return true if the asset was created
     * @throws NotAuthorised
     */
    public boolean newAsset(String assetName, User user) throws NotAuthorised {
        if (user.getAdminAccess()) {
            //TODO update DB with the new asset
            return true;
        } else {
            throw new NotAuthorised("You do not have permission to: create a new asset.");
        }
    }

    // DEVELOPMENT ONLY to ensure tests work (creating new users and units should check if the logged in
    // user has admin rights, however, for tests we need to be able to directly add users, units and assets
    // to the database... hence these methods
    public boolean devAddUser(String username, String password, boolean adminAccess) throws IllegalString, AlreadyExists {
        User newUser = new User(username, password, adminAccess);
        db.addUser(newUser);
        return true;
    }

    public boolean devAddUnit(String unitName) throws AlreadyExists {
        db.addUnit(new OrgUnit(unitName));
        return true;
    }



    /***
     * Delete users from DB (check if admin then call server-side method)
     * @param user
     * @return
     */
    public boolean deleteUser(String user) {
        try {
            db.deleteUser(user);

        } catch (DoesNotExist ex) {
            ex.printStackTrace();
        }
        return true;
    }


    /***
     * Delete units from DB (check if admin then call server-side method)
     * @param unit
     * @return
     */
    public boolean deleteUnit(String unit) {
        try {
            db.deleteUnit(unit);

        } catch (DoesNotExist ex) {
            ex.printStackTrace();
        }
        return true;
    }

    /***
     * Delete assets from DB (check if admin then call server-side method)
     * @param asset
     * @return
     */
    public boolean deleteAsset(Asset asset) {
        db.deleteAsset(asset);
        return true;
    }

    // ADD & REMOVE FROM UNITS
    /***
     * Admin method to add a user to an org unit.
     * @param username user to add to unit
     * @param unitName unit to add user to
     * @throws DoesNotExist if the user / organisational unit does not exist
     * @throws AlreadyExists if the user is already associated with a unit
     * @throws NotAuthorised if the user is not an admin
     */
    public boolean addUserToUnit(String username, String unitName, User user) throws AlreadyExists, DoesNotExist, NotAuthorised {
        if (user.getAdminAccess()) {
            User userSelected;
            OrgUnit unitSelected;

            // Convert username to User object while making sure it exists in the DB.
            userSelected = db.getUser(username);

            // Prevent the user from being added to multiple units
            if (!(getUnit(userSelected) == null)) {
                throw new AlreadyExists("User '%s' is already part of a unit '%s' " +
                        "Please remove them from the unit before adding to a new unit.", username, getUnit(userSelected).getName());
            }

            // Check if the unit exists & store the object.
            // An exception is thrown if the unit does not exist.
            unitSelected = db.getUnit(unitName);

            // If no exception was thrown above, update the DB and store the user's unit locally.
            userSelected.setUnit(unitSelected.getName());
            return true;
        } else {
            throw new NotAuthorised("You do not have permission to: add users to units.");
        }
    }

    /***
     * Admin method to add a user to an org unit.
     * Method overloading with polymorphism.
     * @param usernames user array list to loop through and add users to unit
     * @param unitName unit to add user to
     * @throws DoesNotExist if the user / organisational unit does not exist
     * @throws AlreadyExists if the user is already associated with a unit
     * @throws NotAuthorised if the user is not an admin
     */
    public boolean addUserToUnit(String[] usernames, String unitName, User user) throws DoesNotExist, NotAuthorised, AlreadyExists {
        if (user.getAdminAccess()) {
            User userSelected;
            OrgUnit unitSelected;

            for (String username : usernames) {
                // Convert each username to User object while making sure it exists in the DB.
                userSelected = db.getUser(username);

                // Prevent the user from being added to multiple units
                if (!(getUnit(userSelected) == null)) {
                    throw new AlreadyExists("User '%s' is already part of a unit '%s' " +
                            "Please remove them from the unit before adding to a new unit.", username, getUnit(userSelected).getName());
                }

                // Check if the unit exists & store the object.
                // An exception is thrown if the unit does not exist.
                unitSelected = db.getUnit(unitName);

                // If no exception was thrown above, update the DB and store the user's unit locally.
                userSelected.setUnit(unitSelected.getName());
            }
            return true;
        } else {
            throw new NotAuthorised("You do not have permission to: add users to units.");
        }
    }

    /***
     * Method to remove users from a unit (set user's orgUnit to null)
     * @param username
     * @param unitName
     * @return
     */
    public boolean removeUserFromUnit(String username, String unitName) {
        //TODO
        return true;
    }

    /***
     * Admin method. Adds an asset to a unit.
     * @param unitName unit to add the asset to
     * @param assetName the asset to add
     * @param qty quantity of the asset
     * @throws NotAuthorised if the user is not an admin
     */
    public boolean addInventory(String unitName, Asset assetName, int qty, User user) throws NotAuthorised, DoesNotExist {
        if (user.getAdminAccess()) {
            OrgUnit unitObject = db.getUnit(unitName);
            //TODO call asset method to add the asset
            return true;
        } else {
            throw new NotAuthorised("You do not have permission to: add an asset to a unit.");
        }
    }

    /***
     * Admin method. Removes an asset from a unit entirely.
     * @param unitName unit to remove asset from
     * @param assetName asset to remove
     * @throws NotAuthorised if the user is not an admin
     */
    public boolean removeAsset(String unitName, Asset assetName, User user) throws NotAuthorised, DoesNotExist {
        if (user.getAdminAccess()) {
            OrgUnit unitObject = db.getUnit(unitName);
            //TODO call asset method to remove the asset
            return true;
        } else {
            throw new NotAuthorised("You do not have permission to: remove a unit's asset.");
        }
    }

    // SET & ADJUST ASSETS & BALANCES
    /***
     * Admin method. Sets a unit's asset quantity.
     * @param unitName unit that holds the asset
     * @param assetName the asset to change quantity
     * @param amount the amount to set
     * @throws NotAuthorised if the user is not an admin
     */
    public boolean setAssetQuantity(String unitName, Asset assetName, int amount, User user) throws NotAuthorised, DoesNotExist {
        if (user.getAdminAccess()) {
            OrgUnit unitObject = db.getUnit(unitName);
            //TODO call asset method to set the qty, checks if new amount > 0
            return true;
        } else {
            throw new NotAuthorised("You do not have permission to: set an asset's quantity.");
        }
    }

    /***
     * Admin method. Increases a unit's asset quantity.
     * @param unitName unit that holds the asset
     * @param assetName the asset to change quantity
     * @param amount the amount to add
     * @throws NotAuthorised if the user is not an admin
     */
    public boolean adjustAssetQuantity(String unitName, Asset assetName, int amount, User user) throws NotAuthorised, DoesNotExist {
        if (user.getAdminAccess()) {
            OrgUnit unitObject = db.getUnit(unitName);
            //TODO call asset method to add (+/-)qty, checks if amount will cause qty to be less than 0
            return true;
        } else {
            throw new NotAuthorised("You do not have permission to: increase an asset's quantity.");
        }
    }

    /***
     * Admin method to set an unit's balance.
     * @param unitName unit to change balance
     * @param amount to set
     * @throws NotAuthorised if the user is not an admin
     * @throws DoesNotExist if the unit does not exist
     * @throws InvalidAmount if the amount to set is less than 0
     */
    public boolean setBalance(String unitName, int amount, User user) throws NotAuthorised, DoesNotExist, InvalidAmount {
        if (user.getAdminAccess()) {
            // Check if the unit exists in the DB. Throw an exception if it doesn't.
            OrgUnit unitObject = db.getUnit(unitName);
            unitObject.setBalance(amount);
            return true;
        } else {
            throw new NotAuthorised("You do not have permission to: change a unit's credit balance.");
        }
    }

    /***
     * Admin method to increase a unit's balance.
     * @param unitName unit to change balance
     * @param amount amount to add
     * @throws NotAuthorised if the user is not an admin
     * @throws DoesNotExist if the unit does not exist
     * @throws InvalidAmount if the amount to add is less than 0
     */
    public boolean adjustBalance(String unitName, int amount, User user) throws NotAuthorised, DoesNotExist, InvalidAmount {
        if (user.getAdminAccess()) {
            // Check if the unit exists in the DB. Throw an exception if it doesn't.
            OrgUnit unitObject = db.getUnit(unitName);
            unitObject.adjustBalance(amount);
            return true;
        } else {
            throw new NotAuthorised("You do not have permission to: change a unit's credit balance.");
        }
    }



}
