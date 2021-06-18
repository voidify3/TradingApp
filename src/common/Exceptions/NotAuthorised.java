package common.Exceptions;

/**
 * Custom exception for when a non-admin is attempting an admin-only action
 */
public class NotAuthorised extends Exception {
    public NotAuthorised(String s1, String s) {
        this(String.format(s1, s));
    }

    public NotAuthorised(String s1) {
        super(s1);
    }
}