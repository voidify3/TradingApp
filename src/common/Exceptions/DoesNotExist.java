package common.Exceptions;

public class DoesNotExist extends Throwable {
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
