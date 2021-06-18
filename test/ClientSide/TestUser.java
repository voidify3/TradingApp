package ClientSide;

// class imports
import common.*;
import common.Exceptions.*;
// JUnit imports
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/***
 * @author jmadigan Madigan
 */
public class TestUser {

    User mockGenericUser;
    User mockAdminUser;
    User mockGuy; // kept empty before tests so we can test creation exceptions
    OrgUnit mockUnit;
    //TradingAppData i = new TradingAppData(new MockDataSource());

    // BEFORE & AFTER EACH TEST-----------------------------------------------------------------------------------------
    /***
     * Fresh mock users before each test.
//     * @throws AlreadyExists if the user being created already exists
//     * @throws IllegalString if the username OR password is invalid
     */
    @BeforeEach
    @DisplayName("Before each... create new mock users & a unit")
    public void newMockUsers() throws AlreadyExists, IllegalString {
        mockAdminUser = TradingAppData.adminDev; //sophia
        mockGenericUser = TradingAppData.userDev; //scott
        mockUnit = new OrgUnit("devs", 0);
    }

//    /***
//     * Leave no trace of the mock users.
//     */
//    @AfterEach
//    @DisplayName("After each... delete mock users & the unit")
//    public void clearMockUsers() throws DoesNotExist, ConstraintException {
//        i.deleteEverything();
//    }

    // TEST CONSTRUCTOR, GETTERS, & SETTERS-----------------------------------------------------------------------------
    /***
     * Tests if user constructor compiles, which indirectly tests the getters as well.
     * Also indirectly tests the helper functions "generateSALT" & "hashPasswords".
     */
    @Test
    @DisplayName("Users constructor assigns the data correctly")
    public void testUserConstructor() {
        assertAll(
                // the username is assigned correctly
                () -> assertEquals("sophia", mockAdminUser.getUsername()),
                // the admin access is true
                () -> assertTrue(mockAdminUser.getAdminAccess()),
                // the salt string is made unique by concatenating with the username
                () -> assertTrue(mockAdminUser.getSalt().contains(mockAdminUser.getUsername())),
                // the salt string is generated as alphanumeric
                () -> assertTrue(mockAdminUser.getSalt().matches("[A-Za-z0-9]+")),
                // the password has been hashed (SHA-512 gives a hashed string 128 chars in length)
                () -> assertEquals(128, mockAdminUser.getPassword().length()));
    }

    /***
     * Tests if user setters & getters for admin bool & org unit work correctly.
     * DOES NOT test the setter for passwords (see "testUserConstructor" as
     * it tests the password is valid and hashed with SHA-512 correctly.
     */
    @Test
    @DisplayName("Setters and Getters for admin bool & org unit work correctly")
    public void testSettersAndGetters() {
        mockAdminUser.setAdminAccess(false);
        mockGenericUser.setUnit("devs");
        assertAll(
                () -> assertFalse(mockAdminUser.getAdminAccess()),
                () -> assertEquals(mockUnit.getName(), mockGenericUser.getUnit()));
    }

    // TESTS USING DIFFERENT USERNAMES & PASSWORDS----------------------------------------------------------------------
    /***
     * Tests if an exception is thrown when trying to create a user with an invalid username.
     * This test creates a user WITHOUT adding them to the database, as the test focuses on
     * how the User constructor performs.
     */
    @Test
    @DisplayName("Exception is thrown if the username is invalid in any ways")
    public void usernameInvalid() {
        assertAll(() -> assertThrows(IllegalString.class, () ->
                        mockGuy = new User("guy123","abc123", false)),
                () -> assertThrows(IllegalString.class, () ->
                        mockGuy = new User("g u y","abc123", false)));
    }

    /***
     * Tests if an exception is thrown when trying to create a user with an invalid password. This calls upon
     * the securePassword method which is used to hash ALL passwords. Therefore, if this test passes it implies
     * all other methods that change a user's passwords will also pass in terms of preventing invalid passwords.
     *
     * This test creates a user WITHOUT adding them to the database, as the test focuses on
     * how the User constructor performs.
     */
    @Test
    @DisplayName("Exception is thrown if the password is invalid in any way")
    public void passwordInvalid() {
        assertThrows(IllegalString.class, () ->
                mockGuy = new User("guy","a b c 1 2 3", false));
    }

    /***
     * Tests if a user can change their own password.
     */
    @Test
    @DisplayName("Users can change their own passwords with all sorts of different passwords")
    public void differentPasswords() {
        assertAll(() -> assertTrue(mockGenericUser.changePassword("apples")),
                () -> assertTrue(mockGenericUser.changePassword("@pples")),
                () -> assertTrue(mockAdminUser.changePassword("@APPles")),
                () -> assertTrue(mockAdminUser.changePassword("@APPL3s")));
    }

}
