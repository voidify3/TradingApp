package common;

import java.time.LocalDateTime;

/**
 * @author Alistair Ridge
 */
public class SellOrder extends Order {
    public SellOrder(User user, Asset asset, int qty, int price) {
        super(user, asset, qty, price);
    }

    public SellOrder(int id, String user, int asset, int qty, int price, LocalDateTime placed, LocalDateTime resolved) {
        super(id, user, asset, qty, price, placed, resolved);
    }

    public SellOrder(String user, int asset, int qty, int price) {
        super(user, asset, qty, price);
    }

    @Override
    public int compareTo(Order o) {
        return super.compareTo(o);
    }
}
