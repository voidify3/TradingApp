package common.Exceptions;

public class InvalidAmount extends Throwable {
    public InvalidAmount(String s, Integer amount) {
        super(s);
    }
}
