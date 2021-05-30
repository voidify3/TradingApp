package ClientSideTests;

import ClientSide.*;
import common.*;
import common.Exceptions.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class TestInteractions {

    User mockAdminUser;
    User mockGenericUser;
    OrgUnit mockUnit;
    Interactions i = new Interactions();

    /***
     * Create new mock users users before each test.
     */
    @BeforeEach
    public void newMockUsers() throws IllegalString, AlreadyExists {

        i.devAddUser("dude", "dud3", false);
        i.devAddUser("jmadigan", "bo$$man", true);
        i.devAddUnit("devs");

        mockAdminUser = i.getUser("jmadigan");
        mockGenericUser = i.getUser("dude");
        mockUnit = i.getUnit("devs");
    }

    /***
     * Remove all remnants of mock users
     */
    @AfterEach
    public void clearMockUsers() throws DoesNotExist {
        i.deleteUser(mockAdminUser.getUsername());
        i.deleteUser(mockGenericUser.getUsername());
        i.deleteUnit(mockUnit.getName());
    }

    /***
     * Test if the entered password is hashed and successfully matches the
     * hashed password that belongs to the User with the corresponding username.
     * @throws DoesNotExist if the username does not match a user in the DB
     * @throws IllegalString if the password contains white-spaces
     */
    @Test
    public void testLoginSuccess() throws DoesNotExist, IllegalString, DoesNotExist {
        //print the values to see how the password looks after being hashed and how it matches
        System.out.println("Original password / password to login: bo$$man");
        System.out.println("Salt: " + mockAdminUser.getSalt());
        System.out.println("Hashed password: " + mockAdminUser.getPassword());
        String hashedLoginPassword = mockAdminUser.hashPassword("bo$$man", mockAdminUser.getSalt());
        System.out.println("Hashed input password: " + hashedLoginPassword);

        assertNotNull(i.login("johnny","bo$$man"));
    }

    /***
     * Test if the entered username does not match any user account.
     * @throws DoesNotExist if the username does not match a user in the DB
     * @throws IllegalString if the password contains white-spaces
     */
    @Test
    public void testLoginUsernameFail() throws DoesNotExist, IllegalString {
        Assertions.assertThrows(DoesNotExist.class, () ->
                i.login("lol","bo$$man"));
    }

    /***
     * Test if the entered password is incorrect for the username.
     * @throws DoesNotExist if the username does not match a user in the DB
     * @throws IllegalString if the password contains white-spaces
     */
    @Test
    public void testLoginPasswordFail() throws DoesNotExist, IllegalString {
        Assertions.assertThrows(DoesNotExist.class, () ->
                i.login("johnny","lol"));
    }


    /***
     * Tests if the generic user's password was successfully changed by the admin.
     * No need to test for fail as this method will always return true unless an exception is thrown
     * and these exceptions are thrown in helper methods to reduce code replication.
     * @throws NotAuthorised if the user is not an admin
     * @throws DoesNotExist if the queried username does not exist
     * @throws IllegalString if the password contains white-spaces
     */
    @Test
    public void testChangePasswordAdmin() throws NotAuthorised, DoesNotExist, IllegalString {
        Assertions.assertTrue(i.changePasswordAdmin(mockGenericUser.getUsername(), "apples", mockAdminUser));
    }

    /***
     * TESTS IF THE USER IS NOT AN ADMIN (re-used helper method)
     *
     * Tests if an exception is thrown when a generic user attempts to change another user's password.
     * The changePasswordAdmin method uses a helper method that checks if the user is an admin before executing.
     * This test indirectly tests that helper method and since it's efficiently been re-used, all other admin methods
     * will check for admin access correctly if this test passes.
     */
    @Test
    public void testNotAdmin() {
        Assertions.assertThrows(NotAuthorised.class, () ->
                i.changePasswordAdmin(mockAdminUser.getUsername(), "thisISaPASSWORD;)", mockGenericUser));
    }

    /***
     * TESTS IF THE USER DOES NOT EXIST (re-used helper method)
     *
     * Tests if an exception is thrown when trying to change a password for a user that does not exist.
     * The changePasswordAdmin method uses a helper method that checks if the user exists in the DB before
     * constructing and returning an object. This test indirectly tests that helper method and since it's been
     * efficiently re-used, all other user methods that take a username as a param will check if the user
     * exists correctly.
     */
    @Test
    public void testUserDoesNotExist() {
        Assertions.assertThrows(DoesNotExist.class, () ->
                i.changePasswordAdmin("idontexist", "thisISaPASSWORD;)", mockAdminUser));
    }

    /***
     * Tests if an admin user successful constructs a new user. No need to test if
     * the username is taken or illegal & if the password is illegal since the User
     * class constructor checks this and throws exceptions.
     *
     * Furthermore, no need to check an exception is thrown if the user is a non-admin as
     * this happens within a helper function which has already been tested (see testNotAdmin).
     * @throws NotAuthorised if the user is not an admin
     * @throws IllegalString if the username OR password cannot be used
     */
    @Test
    public void testAdminCreatesNewUserSuccess() throws NotAuthorised, IllegalString, AlreadyExists {
        Assertions.assertTrue(i.newUser("guy","abc123", false, mockAdminUser));
    }

    /***
     * Tests if an admin user successful constructs a new unit.
     *
     * Furthermore, no need to check an exception is thrown if the user is a non-admin as
     * this happens within a helper function which has already been tested (see testNotAdmin).
     * @throws NotAuthorised if the user is not an admin
     * @throws AlreadyExists if the user already exists in the DB
     * @throws IllegalString if the username OR password cannot be used
     */
    @Test
    public void testAdminCreatesNewUnitSuccess() throws AlreadyExists, IllegalString, NotAuthorised {
        Assertions.assertTrue(i.newUnit("myUnit", mockAdminUser));
    }

    /***
     * Tests if an admin user successful constructs a new asset.
     *
     * Furthermore, no need to check an exception is thrown if the user is a non-admin as
     * this happens within a helper function which has already been tested (see testNotAdmin).
     * @throws NotAuthorised if the user is not an admin
     * @throws AlreadyExists if the user already exists in the DB
     * @throws IllegalString if the username OR password cannot be used
     */
    @Test
    public void testAdminCreatesNewAssetSuccess() throws NotAuthorised, AlreadyExists, IllegalString {
        Assertions.assertTrue(i.newAsset("macbook", mockAdminUser));
    }

    /***
     * Tests if an admin user successful constructs a new user. No need to test if
     * the username is taken or illegal & if the password is illegal since the User
     * class constructor checks this and throws exceptions.
     *
     * Furthermore, no need to check an exception is thrown if the user is a non-admin as
     * this happens within a helper function which has already been tested (see testNotAdmin)
     * and therefore working for all methods that utilise it for code re-usability (a.k.a this method).
     * @throws NotAuthorised if the user is not an admin
     * @throws DoesNotExist if the queried user does not exist
     */
    @Test
    public void testAdminChangesAccess() throws NotAuthorised, DoesNotExist {
        Assertions.assertTrue(i.changeAccess(mockGenericUser.getUsername(), true, mockAdminUser));
    }

    /***
     * Tests if the queried unit cannot be found. Indirectly tests the helper function for
     * using a unit's name to return the unit object throws an exception here.
     *
     * No need to test if user is an admin as this is handled by a helper function
     * for code re-usability and tested earlier.
     * @throws NotAuthorised if the user is not an admin
     * @throws DoesNotExist if the queried unit does not exist
     * @throws AlreadyExists if the user is already a member of another unit
     */
    @Test
    public void testUnitDoesNotExist() throws NotAuthorised, DoesNotExist, AlreadyExists {
        Assertions.assertThrows(DoesNotExist.class, () ->
                i.addUserToUnit("johnny", "idontexist", mockAdminUser));
    }

    /***
     * Tests if the user being added is already part of a unit.
     *
     * No need to test if user is an admin as this is handled by a helper function
     * for code re-usability and tested earlier.
     * @throws NotAuthorised if the user is not an admin
     * @throws DoesNotExist if the queried unit does not exist
     * @throws AlreadyExists if the user is already a member of another unit
     */
    @Test
    public void testAddToUnitFail() throws NotAuthorised, DoesNotExist {
        i.addUserToUnit("johnny", "developers", mockAdminUser);
        Assertions.assertThrows(AlreadyExists.class, () ->
                i.addUserToUnit("johnny", "developers", mockAdminUser));
    }

    /***
     * Tests if the chosen username is not unique preventing a duplicate user
     * @throws AlreadyExists if the username already exists in the DB
     * @throws IllegalString if the username is not purely alphabet chars
     */
    @Test
    public void testUsernameTaken() throws AlreadyExists, IllegalString {
        Assertions.assertThrows(AlreadyExists.class, () ->
                i.newUser("johnny","y33t", false, mockAdminUser));
    }

    /***
     * Tests if admins can add users to units. Indirectly tests the if the helper functions
     * If this test passes, it confirms that the helper method successful returns the object.
     *
     * No need to test if user is an admin as this is handled by a helper function
     * for code re-usability and tested earlier.
     * @throws NotAuthorised if the user is not an admin
     * @throws DoesNotExist if the queried unit does not exist
     * @throws AlreadyExists if the user is already a member of another unit
     */
    @Test
    public void testAddSingleUser() throws NotAuthorised, DoesNotExist, AlreadyExists {
        Assertions.assertTrue(i.addUserToUnit("johnny", "developers", mockAdminUser));
    }

    /***
     * Tests polymorphism method overloading for adding users to a unit. Instead of adding a single user,
     * admins have the ability to overload the method and add multiple users at once.
     * Indirectly tests the if the helper function if this test passes, it confirms that
     * the helper method successful returns the object.
     *
     * No need to test if user is an admin as this is handled by a helper function
     * for code re-usability and tested earlier.
     * @throws NotAuthorised if the user is not an admin
     * @throws DoesNotExist if the queried unit does not exist
     * @throws AlreadyExists if the user is already a member of another unit
     */
    @Test
    void testAddMultipleUsers() throws NotAuthorised, DoesNotExist, AlreadyExists {
        String[] usersToAdd = {"johnny", "dude"};
        Assertions.assertTrue(i.addUserToUnit(usersToAdd, "developers", mockAdminUser));
    }

    /***
     * Tests if an exception is thrown for a user trying to get assets when they are not part of a unit yet
     * @throws DoesNotExist if the user is not part of a unit yet
     */
    @Test
    @Disabled("disabled")
    public void testGetAssetsNoUnit() throws DoesNotExist {
        Assertions.assertThrows(DoesNotExist.class, () -> i.getAssets());
    }

    /***
     * Tests if a user is able to get their unit's assets correctly
     * @throws DoesNotExist if the user / unit does not exist in the DB
     * @throws AlreadyExists if the user is already associated with a unit
     * @throws NotAuthorised if the user is not an admin (not allowed to add users to units)
     */
    @Test
    public void testGetAssets() throws DoesNotExist, AlreadyExists, NotAuthorised {
        // Add our mock user to the mock unit
        i.addUserToUnit("johnny", "developers", mockAdminUser);

        // Create a new mock asset
        Asset mockAss = new Asset(99999, "test");
        mockUnit.addAssets(mockAss, 99999);

        Assertions.assertTrue(i.getAssets().containsKey(mockAss));
        //TODO when asset class stores new assets in the DB,
        // make sure this asset is removed after this test
    }

    /***
     * Tests if a user's unit's credits return correctly.
     * @throws DoesNotExist if the unit does not exist
     * @throws AlreadyExists if the user is already a member of a unit
     * @throws NotAuthorised if the user is not an admin
     * @throws InvalidAmount if the amount to set is invalid
     */
    @Test
    public void testGetCredits() throws DoesNotExist, AlreadyExists, NotAuthorised, InvalidAmount {
        // Add our test dummy to org unit & adjust credits
        i.addUserToUnit("johnny", "developers", mockAdminUser);
        i.setBalance("developers", 500, mockAdminUser);

        // Test if the credits adjusted properly.
        Assertions.assertEquals(500, i.getCredits(mockAdminUser.getUsername()));
    }

    // NO NEED TO TEST THE BELOW
    // the below methods only use helper functions (queriedUnit to object & check if admin) before
    // calling methods from the ASSET, ORDER & ORG UNIT classes. these helper functions have been tested multiple
    // times in the tests above, whereas the functionality of these methods happen in the ASSET, ORDER & ORG UNIT
    // classes which will have their own tests

    /*
        -TEST ADD ASSET
        -TEST REMOVE ASSET
        -TEST SET ASSET QTY
        -TEST INCREASE ASSET QTY
        -TEST DECREASE ASSET QTY
        -TEST SET BALANCE
        -TEST INCREASE BALANCE
        -TEST DECREASE BALANCE
        -TEST SELL ORDER
        -TEST BUY ORDER
        -TEST CANCEL ORDER
     */





}
