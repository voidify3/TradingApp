package common;

import common.Exceptions.*;

import java.time.LocalDateTime;

/**
 * @author Alistair Ridge
 */
public class BuyOrder extends Order {
    public int boughtFrom;

    public BuyOrder(User user, Asset asset, int qty, int price) {
        super(user, asset, qty, price);
    }

    public BuyOrder(int id, String user, int asset, int qty, int price, LocalDateTime placed, LocalDateTime resolved, int boughtFrom) {
        super(id, user, asset, qty, price, placed, resolved);
        this.boughtFrom = boughtFrom;
    }

    public BuyOrder(String user, int asset, int qty, int price) {
        super(user, asset, qty, price);
    }

    @Override
    public int compareTo(Order o) {
        return super.compareTo(o);
    }
}
