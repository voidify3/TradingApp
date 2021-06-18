package common.Exceptions;

/**
 * Custom exception for order placing
 */
public class OrderException extends Exception {
    public OrderException() {
        super();
    }

    public OrderException(String message) {
        super(message);
    }
    public OrderException(String message, Object... args) {
        super(String.format(message,args));
    }

    public OrderException(String message, Throwable cause) {
        super(message, cause);
    }

    public OrderException(String message, Throwable cause, boolean enableSuppression, boolean writeableStackTrace) {
        super(message, cause, enableSuppression, writeableStackTrace);
    }

    public OrderException(Throwable cause) {
        super(cause);
    }
}