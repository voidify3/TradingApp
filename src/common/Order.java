package common;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Shared superclass for BuyOrder and SellOrder
 * @author Alistair Ridge
 */
public abstract class Order extends DataObject implements Comparable<Order> {
    private int id = 0;
    private String unit;
    private int asset;
    private LocalDateTime datePlaced;
    private LocalDateTime dateResolved;
    private int qty;
    private int price;

    /**
     * This constructor adds the trade specific information to the object.
     * @param unit OrgUnit object that placed the trade
     * @param asset Asset that is being traded
     * @param qty Amount of the asset that is being traded
     * @param price Price at which the asset is being traded
     */
    public Order(String unit, int asset, int qty, int price) {

        // Set the time that the order was created.
        this.setDatePlaced(LocalDateTime.now());
        this.setDateResolved(null);

        // Update trade specific data
        this.setUnit(unit);
        this.setAsset(asset);
        this.setQty(qty);
        this.setPrice(price);
    }

    /**
     * Constructor for objects retrieved from the database
     * @param id ID
     * @param unit unit name who placed it
     * @param asset asset ID
     * @param qty quantity
     * @param price price
     * @param placed date placed
     * @param resolved date resolved
     */
    public Order(int id, String unit, int asset, int qty, int price, LocalDateTime placed, LocalDateTime resolved) {
        this(unit,asset,qty,price);
        // Set the ID
        this.setId(id);
        // Set the date fields
        this.setDatePlaced(placed);
        this.setDateResolved(resolved);
    }


    // Helper Functions ------------------------------------------------------------------------------------------------

    /**
     * This compareTo override orders by DateResolved (and secondarily by ID) because this is useful for
     * the historical price system
     * @param o other order
     * @return For two resolved orders with different DateResolved values, the older transaction is less
     * Otherwise, the lower ID is less
     */
    @Override
    public int compareTo(Order o) {
        if (this.dateResolved == null || o.getDateResolved() == null
                || this.dateResolved.compareTo(o.getDateResolved()) == 0) {
            return Integer.compare(this.id, o.getId());
        }
        else return this.getDateResolved().compareTo(o.getDateResolved());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;
        Order order = (Order) o;
        return id == order.id && asset == order.asset && qty == order.qty && price == order.price && unit.equals(order.unit) && datePlaced.equals(order.datePlaced) && Objects.equals(dateResolved, order.dateResolved);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, unit, asset, datePlaced, dateResolved, qty, price);
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getAsset() {
        return asset;
    }

    public void setAsset(int asset) {
        this.asset = asset;
    }

    public LocalDateTime getDatePlaced() {
        return datePlaced;
    }

    public void setDatePlaced(LocalDateTime datePlaced) {
        this.datePlaced = datePlaced;
    }

    public LocalDateTime getDateResolved() {
        return dateResolved;
    }

    public void setDateResolved(LocalDateTime dateResolved) {
        this.dateResolved = dateResolved;
    }
}
