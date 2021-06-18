package common.Exceptions;

/**
 * Custom exception for illegal strings
 */
public class IllegalString extends Exception {
    public IllegalString(String s) {
        super(s);
    }

    public IllegalString(String s, String cause) {
        this(String.format(s, cause));
    }
}
