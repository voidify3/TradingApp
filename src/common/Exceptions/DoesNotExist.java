package common.Exceptions;

/**
 * Custom exception for when a database record doesn't exist
 */
public class DoesNotExist extends Exception {
    public DoesNotExist(String s, String itemThatDoesNotExist) {
        this(String.format(s, itemThatDoesNotExist));
    }

    public DoesNotExist(String s) {
        super(s);
    }

    public DoesNotExist(String s, int i) {
        this(String.format(s, i));
    }
}
