package common;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author Alistair Ridge
 */
public class Order extends DataObject implements Comparable<Order> {
    public int id = 0;
    public static int nextId = 0;
    public String user;
    public int asset;
    public LocalDateTime datePlaced;
    public LocalDateTime dateResolved;
    public int qty;
    public int price;

    /**
     * This constructor sets the trade ID of the order and adds the trade specific information to the object.
     * @param user User object that placed the trade
     * @param asset Asset that is being traded
     * @param qty Amount of the asset that is being traded
     * @param price Price at which the asset is being traded
     */
    public Order(User user, Asset asset, int qty, int price) {
        // Increment the trade ID every time a new order is created so that all orders have a unique ID.
        this.id = nextId;
        nextId++;

        // Set the time that the order was created.
        this.datePlaced = LocalDateTime.now();
        this.dateResolved = null;

        // Update trade specific data
        this.user = user.getUsername();
        this.asset = asset.getId();
        this.qty = qty;
        this.price = price;
    }
    public Order(String user, int asset, int qty, int price) {
        // Increment the trade ID every time a new order is created so that all orders have a unique ID.
        this.id = nextId;
        nextId++;

        // Set the time that the order was created.
        this.datePlaced = LocalDateTime.now();
        this.dateResolved = null;

        // Update trade specific data
        this.user = user;
        this.asset = asset;
        this.qty = qty;
        this.price = price;
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
        this.id = id;

        // Set the time that the order was created.
        this.datePlaced = placed;
        this.dateResolved = resolved;

        // Update trade specific data
        this.user = user;
        this.asset = asset;
        this.qty = qty;
        this.price = price;
    }


    // Helper Functions ------------------------------------------------------------------------------------------------

    public String getUsername() {
        return this.user;
    }

    public int getAssetID() {
        return this.asset;
    }

    @Override
    public int compareTo(Order o) {
        if (this.dateResolved == null | o.dateResolved == null) {
            return this.datePlaced.compareTo(o.datePlaced);
        }
        else return this.dateResolved.compareTo(o.dateResolved);
    }
}
