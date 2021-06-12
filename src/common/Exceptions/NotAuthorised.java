package common.Exceptions;

public class NotAuthorised extends Throwable {
    public NotAuthorised(String s1, String s) {
        this(String.format(s1, s));
    }

    public NotAuthorised(String s1) {
        super(s1);
    }
}