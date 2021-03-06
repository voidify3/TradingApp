package common;

import common.Exceptions.*;

import java.time.LocalDateTime;

/**
 * Class modelling a record of the Buyorder table
 */
public class BuyOrder extends Order {
    public Integer getBoughtFrom() {
        return boughtFrom;
    }

    private Integer boughtFrom = null;
    /**
     * Constructor for server-side use
     * @param id
     * @param user
     * @param asset
     * @param qty
     * @param price
     * @param placed
     * @param resolved
     * @param boughtFrom
     */
    public BuyOrder(int id, String user, int asset, int qty, int price, LocalDateTime placed,
                    LocalDateTime resolved, Integer boughtFrom) {
        super(id, user, asset, qty, price, placed, resolved);
        this.boughtFrom = boughtFrom;
    }

    public BuyOrder(String unit, int asset, int qty, int price) {
        super(unit, asset, qty, price);
    }

    @Override
    public int compareTo(Order o) {
        return super.compareTo(o);
    }

}
