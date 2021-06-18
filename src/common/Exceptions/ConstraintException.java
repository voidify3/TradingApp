package common.Exceptions;

/**
 * Custom exception for when a constraint prevented an insertion or update
 */
public class ConstraintException extends Exception {

    public ConstraintException(String message) {
        super(message);
    }
    public ConstraintException(String message, String s1) {
        this(String.format(message, s1));
    }
    public ConstraintException(String message, int i) {
        this(String.format(message, i));
    }
}
