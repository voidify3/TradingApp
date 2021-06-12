package common.Exceptions;

public class AlreadyExists extends Throwable {
    public AlreadyExists(String s) {
        super(s);
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
