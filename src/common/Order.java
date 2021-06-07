package common;

import java.time.LocalDateTime;

/**
 * @author Alistair Ridge
 */
public class Order extends DataObject implements Comparable<Order> {
    private int id = 0;
    public static int nextId = 0;
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
        // Increment the trade ID every time a new order is created so that all orders have a unique ID.
        this.setId(nextId);
        nextId++;

        // Set the time that the order was created.
        this.setDatePlaced(LocalDateTime.now());
        this.setDateResolved(null);

        // Update trade specific data
        this.setUser(user.getUsername());
        this.setAsset(asset.getId());
        this.setQty(qty);
        this.setPrice(price);
    }
    public Order(String user, int asset, int qty, int price) {
        // Increment the trade ID every time a new order is created so that all orders have a unique ID.
        this.setId(nextId);
        nextId++;

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
        // Increment the trade ID every time a new order is created so that all orders have a unique ID.
        this.setId(id);

        // Set the time that the order was created.
        this.setDatePlaced(placed);
        this.setDateResolved(resolved);

        // Update trade specific data
        this.setUser(user);
        this.setAsset(asset);
        this.setQty(qty);
        this.setPrice(price);
    }


    // Helper Functions ------------------------------------------------------------------------------------------------

    @Override
    public int compareTo(Order o) {
        if (this.dateResolved == null || o.getDateResolved() == null
                || this.dateResolved.compareTo(o.getDateResolved()) == 0) {
            return Integer.compare(this.id, o.getId());
        }
        else return this.getDateResolved().compareTo(o.getDateResolved());
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
