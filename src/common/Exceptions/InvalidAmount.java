package common.Exceptions;

/**
 * Custom exception for invalid amounts
 */
public class InvalidAmount extends Exception {
    public InvalidAmount(String s, Integer amount) {
        super(String.format(s, amount));
    }
}
