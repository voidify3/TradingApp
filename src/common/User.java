package common;

// class imports
import common.Exceptions.*;
// Java imports
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/***
 * @author Johnny
 */
public class User extends DataObject {

    // INSTANCE VARIABLES------------------------------------------------------------------------------------------------
    private final String name;
    private final String salt;
    private String password;
    private boolean adminAccess;
    private String unit;
    private final static String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

    // CONSTRUCTOR------------------------------------------------------------------------------------------------------
    /***
     * User constructor.
     * @param username the user's unique username (cannot already exist in DB)
     * @param password the user's password (which will be encrypted)
     * @param adminAccess the access bool for the user (to use certain methods)
     * @throws IllegalString if the username is an invalid string
     */
    public User(String username, String password, boolean adminAccess) throws IllegalString {
        String usernameLC;
        if (username.equals("")) throw new IllegalString("Username must not be empty");
        // Check if the username is letters only and make lowercase if so
        if (username.matches("[a-zA-Z]+") && !(username.contains(" "))) {
            usernameLC = username.toLowerCase();
        } else {
            throw new IllegalString("Username '%s' must be letters only. Please try again.", username);
        }
        if (usernameLC.length() > 30) throw new IllegalString("Username '%s' is too long. " +
                "Maximum username length is 30 characters. Please try again.", username);

        this.name = usernameLC;
        this.adminAccess = adminAccess;

        // Hash the password with a one-time unique salt string
        this.salt = generateSALT(usernameLC);
        this.password = hashPassword(password, this.salt);

    }
    public User(String username, String password, boolean adminAccess, String orgunit) throws IllegalString {
        this(username, password, adminAccess);
        this.setUnit(orgunit);
    }

    /**
     * Constructor for server-side use when retrieving users from the database
     * @param username  Username
     * @param passhash Pre-hashed password
     * @param salt Preexisting salt string
     * @param orgunit Org unit
     * @param adminAccess Admin access value
     */
    public User(String username, String passhash, String salt, String orgunit, boolean adminAccess) {
        this.name = username;
        this.salt = salt;
        this.password = passhash;
        this.unit = orgunit;
        this.adminAccess = adminAccess;
    }

    // GETTERS & SETTERS------------------------------------------------------------------------------------------------
    /***
     * Getter for the username.
     * NO setter as the username is unique & decided upon user creation (hence final).
     * @return username string
     */
    public String getUsername() {
        return this.name;
    }

    /***
     * Getter for the user's randomly generated salt.
     * NO setter as the salt is unique and randomly generated ONCE during user creation (hence final).
     * @return salt string
     */
    public String getSalt() {
        return this.salt;
    }

    /***
     * Getter for the hashed password.
     * The login method needs this to match the password.
     * @return hashed password string
     */
    public String getPassword() {
        return this.password;
    }

    /***
     * Setter for the password. Private because all non-private password setting goes through the
     * non-straightforward setter
     */
    private void setPassword(String hashedPassword) {
        this.password = hashedPassword;
    }

    /***
     * Getter for the admin access.
     * @return admin access bool
     */
    public boolean getAdminAccess() {
        return this.adminAccess;
    }

    /***
     * Setter for the admin access.
     * @param newAccess the access to set
     */
    public void setAdminAccess(boolean newAccess) {
        this.adminAccess = newAccess;
    }

    /***
     * Getter for this user's unit.
     */
    public String getUnit () {
        return this.unit;
    }

    /***
     * Setter for this user's unit.
     * @param unit to set
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }

    // ALL USER METHODS-------------------------------------------------------------------------------------------------
    /***
     * Changes the password of the User object
     * @param newPassword to change to
     */
    public boolean changePassword(String newPassword) throws IllegalString {
        String hashedPassword = hashPassword(newPassword, this.getSalt());
        this.setPassword(hashedPassword);
        return true;
    }

    // HELPER FUNCTIONS-------------------------------------------------------------------------------------------------
    /***
     * Private helper method that generates a random salt string.
     * The salt string is then made globally unique by concatenating the user's unique username on the end.
     * This will later be converted into bytes to hash with the corresponding user's password.
     * @return random alphanumeric salt string.
     * @see <a href="https://stackoverflow.com/a/20536597">Inspired from stack overflow</a>
     */
    protected static String generateSALT(String username) {
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 11) { // length of the random string
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }

        // most crucial step to ensure salt is globally unique
        // combining the salt string with the unique user's username
        String saltStr = salt.toString();
        saltStr = saltStr.concat(username);
        return saltStr;
    }

    /***
     * After using the cryptographic salt generator upon User creation, this method can now
     * begin to encrypted passwords so that even if 2 user's choose/given the same password,
     * their saved passwords in the DB will be drastically different.
     *
     * This method breaks the password & salt down into bytes before hashing into a string of 128 chars.
     * Furthermore, this method is deterministic. So it can also be used for authenticating user's when logging in.
     * When a user types their username, the program will query for their username and use the corresponding salt string
     * (see login method) and then hash whatever password typed before checking if this newly hashed password
     * matches the one stored in the DB.
     *
     * Demonstrating the multi-use of this method for encrypting and authenticating.
     * @param password the password pre-hash
     * @param salt the string which will become bytes before hashing the password
     * @return the hashed password
     * @see <a href="https://stackoverflow.com/a/33085670">Inspired from stack overflow</a>
     */
    public static String hashPassword(String password, String salt) throws IllegalString {
        String hashedPassword = null;
        String checkedPassword;

        // Check if the password contains white spaces
        if (password.contains(" ")) {
            throw new IllegalString("Password must not contain white-spaces. Please try again.", password);
        } else if (password == null || password.equals("")) throw new IllegalString("Password must not be empty");
        else {
            checkedPassword = password;
        }

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] bytes = md.digest(checkedPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }
            hashedPassword = sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
        return hashedPassword;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return adminAccess == user.adminAccess && name.equals(user.name) && salt.equals(user.salt) && password.equals(user.password) && Objects.equals(unit, user.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, salt, password, adminAccess, unit);
    }
}
