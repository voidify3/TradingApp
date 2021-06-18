package common;

import java.time.LocalDateTime;

/**
 * @author Alistair Ridge
 */
public class SellOrder extends Order {

    public SellOrder(int id, String unit, int asset, int qty, int price, LocalDateTime placed, LocalDateTime resolved) {
        super(id, unit, asset, qty, price, placed, resolved);
    }

    public SellOrder(String unit, int asset, int qty, int price) {
        super(unit, asset, qty, price);
    }

    @Override
    public int compareTo(Order o) {
        return super.compareTo(o);
    }
}
