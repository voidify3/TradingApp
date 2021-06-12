package common;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author Alistair Ridge
 */
public class Order extends DataObject implements Comparable<Order> {
    private int id = 0;
    private String user;
    private int asset;
    private LocalDateTime datePlaced;
    private LocalDateTime dateResolved;
    private int qty;
    private int price;

    /**
     * This constructor sets the trade ID of the order and adds the trade specific information to the object.
     * @param user User object that placed the trade
     * @param asset Asset that is being traded
     * @param qty Amount of the asset that is being traded
     * @param price Price at which the asset is being traded
     */
    public Order(User user, Asset asset, int qty, int price) {
        this(user.getUsername(), asset.getId(), qty,price);
    }
    public Order(String user, int asset, int qty, int price) {

        // Set the time that the order was created.
        this.setDatePlaced(LocalDateTime.now());
        this.setDateResolved(null);

        // Update trade specific data
        this.setUser(user);
        this.setAsset(asset);
        this.setQty(qty);
        this.setPrice(price);
    }

    /**
     * Constructor for objects retrieved from the database
     * @param id ID
     * @param user user name who placed it
     * @param asset asset ID
     * @param qty quantity
     * @param price price
     * @param placed date placed
     * @param resolved date resolved
     */
    public Order(int id, String user, int asset, int qty, int price, LocalDateTime placed, LocalDateTime resolved) {
        this(user,asset,qty,price);
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
        return id == order.id && asset == order.asset && qty == order.qty && price == order.price && user.equals(order.user) && datePlaced.equals(order.datePlaced) && Objects.equals(dateResolved, order.dateResolved);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, asset, datePlaced, dateResolved, qty, price);
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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
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
