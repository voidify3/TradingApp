package common.Exceptions;

public class IllegalString extends Throwable {
    public IllegalString(String s) {
        super(s);
    }

    public IllegalString(String s, String cause) {
        this(String.format(s, cause));
    }
}
