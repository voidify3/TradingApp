package common.Exceptions;

/**
 * Custom exception for when an insertion failed due to pre-existence of key
 */
public class AlreadyExists extends Exception {
    public AlreadyExists(String s) {
        super(s);
    }
    public AlreadyExists(String s, Object... args) {
        this(String.format(s, args));
    }
    public AlreadyExists(String s, String s1) {
        this(String.format(s, s1));
    }

    public AlreadyExists(String s, String s1, String s2) {
        this(String.format(s, s1, s2));
    }

    public AlreadyExists(String s, int i) {
        this(String.format(s, i));
    }
}
