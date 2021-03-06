package ServerSide;

import common.*;
import common.Exceptions.IllegalString;

import static common.DatabaseTables.*;
import static common.ProtocolKeywords.*;
import static java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * The heart and soul of the server program. Inspired heavily by week 7 and 8 exercises.
 * Listens for requests infinitely
 * @author Sophia Walsh Long
 */
class NetworkServer {
    /**
     * Port number (SHOULD MATCH THE ONE IN NETWORKSERVER)
     */
    private static final int PORT = 10000;
    /**
     * Socket timeout number
     */
    private static final int SOCKET_TIMEOUT = 100;
    /**
     * Atomic boolean used to shut down the server from another thread
     */
    private AtomicBoolean running = new AtomicBoolean(true);
    /**
     * Timer (initialised in the ServerGUI thread)
     */
    Timer tradeReconciliation;

    /**
     * JDBC connection, pulled upon construction from the DBConnection singleton
     */
    private final Connection connection;
    /**
     * Time of last trade reconciliation
     */
    LocalDateTime lastReconciliation;
    //Create Table scripts for all tables, using table and column names from the enum
    // week 7 address book exercise used as reference for syntax; "from stackoverflow" comment is from that
    private static final String CREATE_TABLE_UNIT =
            "CREATE TABLE IF NOT EXISTS " + UNIT.getName() + " ("
                    + UNIT.getColumns()[0] + " VARCHAR(30) PRIMARY KEY NOT NULL UNIQUE," //name
                    + UNIT.getColumns()[1] + " INTEGER" + ");"; //credits
    private static final String CREATE_TABLE_ASSET =
            "CREATE TABLE IF NOT EXISTS " + ASSET.getName() + " ("
                    + ASSET.getColumns()[0] + " INTEGER "
                    + "PRIMARY KEY /*!40101 AUTO_INCREMENT */ NOT NULL UNIQUE," //from https://stackoverflow.com/a/41028314
                    + ASSET.getColumns()[1] + " VARCHAR(60)" + ");"; //description
    private static final String CREATE_TABLE_USER =
            "CREATE TABLE IF NOT EXISTS " + USER.getName() + " ("
                    + USER.getColumns()[0] + " VARCHAR(30) PRIMARY KEY NOT NULL UNIQUE," //name
                    + USER.getColumns()[1] + " VARCHAR(128) NOT NULL," //hashed password
                    + USER.getColumns()[2] + " VARCHAR(41) NOT NULL," //salt string
                    + USER.getColumns()[3] + " VARCHAR(30)," //orgunit
                    + USER.getColumns()[4] + " BOOLEAN," //adminAccess
                    + "CONSTRAINT fk_user_orgunit FOREIGN KEY (" + USER.getColumns()[3]
                    + ") REFERENCES " + UNIT.getName() + " (" + UNIT.getColumns()[0]
                    + ") ON DELETE SET NULL ON UPDATE CASCADE" + ");";
    private static final String CREATE_TABLE_INV =
            "CREATE TABLE IF NOT EXISTS " + INV.getName() + " ("
                    + INV.getColumns()[0] + " VARCHAR(30) NOT NULL," //orgunit
                    + INV.getColumns()[1] + " INTEGER NOT NULL," //asset
                    + INV.getColumns()[2] + " INTEGER NOT NULL," //quantity
                    + "CONSTRAINT fk_inventories_orgunit FOREIGN KEY (" + INV.getColumns()[0]
                    + ") REFERENCES " + UNIT.getName() + " (" + UNIT.getColumns()[0]
                    + ") ON DELETE CASCADE ON UPDATE CASCADE,"
                    + "CONSTRAINT fk_inventories_asset FOREIGN KEY (" + INV.getColumns()[1]
                    + ") REFERENCES " + ASSET.getName() + " (" + ASSET.getColumns()[0]
                    + ") ON DELETE CASCADE ON UPDATE CASCADE,"
                    + "PRIMARY KEY(" + INV.getColumns()[0] + "," + INV.getColumns()[1] + ")"
                    + ");";
    private static final String CREATE_TABLE_SELL =
            "CREATE TABLE IF NOT EXISTS " + SELL.getName() + " ("
                    + SELL.getColumns()[0] + " INTEGER PRIMARY KEY /*!40101 AUTO_INCREMENT */ NOT NULL UNIQUE,"
                    + SELL.getColumns()[1] + " VARCHAR(30) NOT NULL," //unit
                    + SELL.getColumns()[2] + " INTEGER NOT NULL," //asset
                    + SELL.getColumns()[3] + " INTEGER NOT NULL," //quantity
                    + SELL.getColumns()[4] + " INTEGER NOT NULL," //price
                    + SELL.getColumns()[5] + " DATETIME NOT NULL," //datePlaced
                    + SELL.getColumns()[6] + " DATETIME," //dateResolved
                    + "CONSTRAINT fk_sell_unit FOREIGN KEY (" + SELL.getColumns()[1]
                    + ") REFERENCES " + UNIT.getName() + " (" + UNIT.getColumns()[0]
                    + ") ON DELETE CASCADE ON UPDATE CASCADE,"
                    + "CONSTRAINT fk_sell_asset FOREIGN KEY (" + SELL.getColumns()[2]
                    + ") REFERENCES " + ASSET.getName() + " (" + ASSET.getColumns()[0]
                    + ") ON DELETE CASCADE ON UPDATE CASCADE" + ");";
    private static final String CREATE_TABLE_BUY =
            "CREATE TABLE IF NOT EXISTS " + BUY.getName() + " ("
                    + BUY.getColumns()[0] + " INTEGER PRIMARY KEY /*!40101 AUTO_INCREMENT */ NOT NULL UNIQUE,"
                    + BUY.getColumns()[1] + " VARCHAR(30) NOT NULL," //unit
                    + BUY.getColumns()[2] + " INTEGER NOT NULL," //asset
                    + BUY.getColumns()[3] + " INTEGER NOT NULL," //quantity
                    + BUY.getColumns()[4] + " INTEGER NOT NULL," //price
                    + BUY.getColumns()[5] + " DATETIME NOT NULL," //datePlaced
                    + BUY.getColumns()[6] + " DATETIME," //dateResolved
                    + BUY.getColumns()[7] + " INTEGER," //boughtFrom
                    + "CONSTRAINT fk_buy_unit FOREIGN KEY (" + BUY.getColumns()[1]
                    + ") REFERENCES " + UNIT.getName() + " (" + UNIT.getColumns()[0]
                    + ") ON DELETE CASCADE ON UPDATE CASCADE,"
                    + "CONSTRAINT fk_buy_asset FOREIGN KEY (" + BUY.getColumns()[2]
                    + ") REFERENCES " + ASSET.getName() + " (" + ASSET.getColumns()[0]
                    + ") ON DELETE CASCADE ON UPDATE CASCADE,"
                    + "CONSTRAINT fk_buy_sell FOREIGN KEY (" + BUY.getColumns()[7]
                    + ") REFERENCES " + SELL.getName() + " (" + SELL.getColumns()[0]
                    + ") ON DELETE CASCADE ON UPDATE CASCADE" + ");";
    /*
        static final String CREATE_TABLES = CREATE_TABLE_UNIT + CREATE_TABLE_ASSET + CREATE_TABLE_USER +
                CREATE_TABLE_INV + CREATE_TABLE_SELL + CREATE_TABLE_BUY;
    */
    private static final String TOTAL_RECORDS_WRAPPER = "SELECT %s AS SUM;";
    private static final String TOTAL_RECORDS_INNER = "(SELECT COUNT(*) FROM %s)";
    private static final String[] CLEAR_DATA = {"DROP TABLE " + BUY.getName() + ";", "DROP TABLE " + SELL.getName()
            + ";", "DROP TABLE " + INV.getName() + ";", "DROP TABLE " + ASSET.getName() + ";",
            "DROP TABLE " + USER.getName() + ";", "DROP TABLE " + UNIT.getName() + ";"};

    private static final String GET_ASSETS = "SELECT * FROM asset;";
    private static final String RECONCILIATION_GET_SELLS =
            "SELECT * FROM sellorder " +
                    "WHERE dateResolved IS NULL AND asset=? " +
                    "ORDER BY datePlaced, idx;";
    private static final String RECONCILIATION_GET_BUYS =
            "SELECT * FROM buyorder " +
                    "WHERE dateResolved IS NULL AND asset=? " +
                    "ORDER BY datePlaced, idx;";
    private static final String RECONCILIATION_ADJUST_BALANCE =
            "UPDATE orgunit " +
                    "SET credits=credits+? " +
                    "WHERE name=?;";
    private static final String INSERT_OR_UPDATE_INV =
            "INSERT INTO inventories (orgunit, asset, quantity)" +
                    "VALUES (?, ?, ?)" +
                    "ON DUPLICATE KEY UPDATE quantity=values(quantity);";
    private static final String INSERT_OR_ADJUST_INV =
            "INSERT INTO inventories (orgunit, asset, quantity)" +
                    "VALUES (?, ?, ?)" +
                    "ON DUPLICATE KEY UPDATE quantity=quantity + values(quantity);";
    private static final String GENERIC_SELECT = "SELECT * FROM %s WHERE %s ORDER BY %s, %s;";
    private static final String GENERIC_DELETE = "DELETE FROM %s WHERE %s;";
    private static final String GENERIC_INSERT = "INSERT INTO %s (%s) VALUES (%s);";
    private static final String GENERIC_UPDATE = "UPDATE %s SET %s WHERE %s=?;";

    private PreparedStatement getAssets;
    private PreparedStatement getSellsReconciliation;
    private PreparedStatement getBuysReconciliation;
    private PreparedStatement adjustBalance;
    private PreparedStatement insertUpdateInv;
    private PreparedStatement insertAdjustInv;

    /**
     * Constructor. Gets connection, sets up tables, prepares statements
     */
    NetworkServer() {
        tradeReconciliation = new Timer();
        connection = DBConnection.getInstance();
        try {
            setupTables();
            //INITIALISATION OF PREPARED STATEMENTS
            getAssets = connection.prepareStatement(GET_ASSETS);
            insertUpdateInv = connection.prepareStatement(INSERT_OR_UPDATE_INV);
            insertAdjustInv = connection.prepareStatement(INSERT_OR_ADJUST_INV);
            getSellsReconciliation = connection.prepareStatement(RECONCILIATION_GET_SELLS, TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            getBuysReconciliation = connection.prepareStatement(RECONCILIATION_GET_BUYS, TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            adjustBalance = connection.prepareStatement(RECONCILIATION_ADJUST_BALANCE);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Run all create table scripts
     * @throws SQLException if a SQL error occurred (shouldn't happen)
     */
    void setupTables() throws SQLException {
        Statement st = connection.createStatement();
        st.addBatch(CREATE_TABLE_UNIT);
        st.addBatch(CREATE_TABLE_ASSET);
        st.addBatch(CREATE_TABLE_USER);
        st.addBatch(CREATE_TABLE_INV);
        st.addBatch(CREATE_TABLE_SELL);
        st.addBatch(CREATE_TABLE_BUY);
        st.executeBatch();
        connection.commit();
        System.out.println("Create table script executed");
    }

    /**
     * Returns the port the server is configured to use
     *
     * @return The port number
     */
    static int getPort() {
        return PORT;
    }

    /**
     * Starts the server running on the default port, code borrowed from week 7 exercise
     */
    void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            serverSocket.setSoTimeout(SOCKET_TIMEOUT);
            for (;;) {
                if (!running.get()) {
                    // The server is no longer running
                    break;
                }
                try {
                    Socket socket = serverSocket.accept();
                    handleConnection(socket);
                } catch (SocketTimeoutException ignored) {
                    // Do nothing. A timeout is normal- we just want the socket to
                    // occasionally timeout so we can check if the server is still running
                } catch (Exception e) {
                    // We will report other exceptions by printing the stack trace, but we
                    // will not shut down the server. A exception can happen due to a
                    // client malfunction (or malicious client)
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            // If we get an error starting up, show an error dialog then exit
            e.printStackTrace();
            System.exit(1);
        }

        // Close down the server
        System.exit(0);
    }

    /**
     * Requests the server to shut down
     */
    void shutdown() {
        System.out.println("Shutting down server");
        //Stop firing trade reconciliation
        tradeReconciliation.cancel();
        //Close the database connection
        //dataSource.close();
        try {
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        // Shut the server down
        running.set(false);
    }

    /**
     * Handles the connection received from ServerSocket
     * @param socket The socket used to communicate with the currently connected client
     */
    private void handleConnection(Socket socket) throws IOException, ClassNotFoundException {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream()))
        {
            ProtocolKeywords command = (ProtocolKeywords) objectInputStream.readObject();
            DataPacket info = (DataPacket)objectInputStream.readObject();

            try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());)
            {

                handleRequest(command, info, objectOutputStream);
            }
            catch (SQLException e) {
                e.printStackTrace();
            }

        }
    }
    /**
     * Do trade reconciliation. Both the outer loop over sell orders and the inner loop over buy orders are conducted
     * from oldest to newest. Feedback is printed to the console
     */
    void reconcileTrades() {
        int resolutionCount = 0;
        try {
            lastReconciliation = LocalDateTime.now();
            Timestamp now = Timestamp.valueOf(lastReconciliation);
            ResultSet assets = executeSelectQuery(getAssets);

            while (assets.next()) {
                int assetid = assets.getInt(1);
                String assetdesc = assets.getString(2);
                getSellsReconciliation.setInt(1, assetid);
                getBuysReconciliation.setInt(1, assetid);
                ResultSet sells = executeSelectQuery(getSellsReconciliation);
                ResultSet buys = executeSelectQuery(getBuysReconciliation);
                ArrayList<Integer> ignoredBuys = new ArrayList<>();
                while (sells.next()) {
                    buys.beforeFirst();
                    int sellID = sells.getInt(1);
                    int sellQty = sells.getInt(4);
                    int sellPrice = sells.getInt(5);
                    String sellUnit = sells.getString(2);
                    while (buys.next()) {
                        int buyID = buys.getInt(1);
                        if (ignoredBuys.contains(buyID)) continue;
                        int buyQty = buys.getInt(4);
                        int buyPrice = buys.getInt(5);
                        String buyUnit = buys.getString(2);
                        System.out.printf("Comparing sell order %d (%d, $%d) and buy order %d (%d, $%d)...",
                                sellID, sellQty, sellPrice, buyID, buyQty, buyPrice);
                        if (buyQty <= sellQty && buyPrice >= sellPrice) {
                            //System.out.printf("Resolving sell order %d and buy order %d...", sellID, buyID);
                            int refundAmount = (buyPrice - sellPrice) * buyQty;
                            buys.updateInt(5, sellPrice);
                            buys.updateTimestamp(7, now);
                            buys.updateInt(8, sellID);
                            buys.updateRow();

                            sellQty -= buyQty;
                            sells.updateInt(4, sellQty);
                            if (sellQty==0) sells.updateTimestamp(7, now);
                            sells.updateRow();

                            int totalPrice = buyQty * sellPrice;
                            adjustBalance.setInt(1, totalPrice); //total credits gained by seller
                            adjustBalance.setString(2, sellUnit);
                            adjustBalance.addBatch();
                            if (buyPrice > sellPrice){
                                //credits were deducted upon buy order placement, so refund the difference if needed
                                adjustBalance.setInt(1, refundAmount);
                                adjustBalance.setString(2, buyUnit);
                                adjustBalance.addBatch();
                            }
                            adjustBalance.executeBatch();

                            insertAdjustInv.setString(1, buyUnit);
                            insertAdjustInv.setInt(2, assetid);
                            insertAdjustInv.setInt(3, buyQty);
                            insertAdjustInv.execute();
                            connection.commit();
                            ignoredBuys.add(buyID);
                            System.out.println(" Resolved!");
                            resolutionCount++;
                        }
                        else {
                            System.out.println(" Didn't resolve.");
                        }
                        if (sellQty == 0) break;
                    }
                }
                System.out.printf("Reconciliation done for asset %d (%s)\n", assetid, assetdesc);
            }
            System.out.printf("Trade reconciliation complete! %d transactions processed\n", resolutionCount);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            System.out.printf("Trade reconciliation interrupted by an error. %d transactions were processed.",
                    resolutionCount);
            try {
                connection.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private int totalRecordCount() throws SQLException {
        return totalRecordCount(DatabaseTables.values());
    }
    private int totalRecordCount(DatabaseTables[] d) throws SQLException {
        String[] s = new String[d.length];
        for (int i = 0; i<d.length;i++) {
            s[i]=d[i].getName();
        }
        return totalRecordCount(s);
    }
    private int totalRecordCount(String[] d) throws SQLException {
        ArrayList<String> inners = new ArrayList<>();
        for (String t : d) {
            inners.add(String.format(TOTAL_RECORDS_INNER, t));
        }
        String query = String.format(TOTAL_RECORDS_WRAPPER, String.join(" + ", inners));
        ResultSet rs = connection.prepareStatement(query).executeQuery();
        rs.next();
        return rs.getInt(1);
    }

    /**
     * Non-private wrapper for testing SELECT queries
     * @param info DataPacket of query
     * @return ArrayList of results
     */
    ArrayList<DataObject> simulateSelect(DataPacket info) throws SQLException {
        return handleSelect(info);
    }

    /**
     * Non-private wrapper for testing INSERT, UPDATE, DELETE queries
     * @param keyword Query type
     * @param info DataPacket of query
     * @return Query status number (1 for "success", 0 for "failure due to existence/nonexistence of matching record",
     * -1 for "failure due to constraints", 2 for "INSERT ON DUPLICATE KEY UPDATE query didn't insert but updated")
     */
    int simulateNonselect(ProtocolKeywords keyword, DataPacket info) throws SQLException {
        return handleNonselect(keyword, info);
    }

    /**
     * Empty the database. Only exists for test and debug convenience, may deprecate later due to unsafeness
     */
    int resetEverything() throws SQLException {
        int count = totalRecordCount();
        for (String drop : CLEAR_DATA) {
            connection.prepareStatement(drop).execute();
        }
        connection.commit();
        System.out.println("All tables dropped");
        return count;
    }

    private void handleRequest(ProtocolKeywords keyword, DataPacket info, ObjectOutputStream out) throws IOException, SQLException {
        if (keyword == ProtocolKeywords.SELECT) out.writeObject(handleSelect(info));
        else out.writeObject(handleNonselect(keyword, info));
    }

    private ArrayList<DataObject> handleSelect(DataPacket info) throws SQLException {
        ArrayList results = new ArrayList<>();
        String queryString = String.format(GENERIC_SELECT, info.table.getName(), info.filter,
                info.table.getColumns()[0], info.table.getColumns()[1]);
        ResultSet queryResults = executeSelectQuery(connection.prepareStatement(queryString));
        switch(info.table) {
            case UNIT -> results = populateUnits(queryResults);
            case ASSET -> results = populateAssets(queryResults);
            case INV -> results = populateInv(queryResults);
            case USER -> results = populateUsers(queryResults);
            case BUY -> results = populateBuys(queryResults);
            case SELL -> results = populateSells(queryResults);
        }
        return results;
    }
    private ArrayList<OrgUnit> populateUnits(ResultSet r) throws SQLException {
        ArrayList<OrgUnit> output = new ArrayList<>();
        while (r.next()) {
            output.add(DataObjectFactory.newOrgUnitNaive(r.getString(1), r.getInt(2)));
        }
        return output;
    }
    private ArrayList<Asset> populateAssets(ResultSet r) throws SQLException {
        ArrayList<Asset> output = new ArrayList<>();
        while (r.next()) {
            output.add(new Asset(r.getInt(1), r.getString(2)));
        }
        return output;
    }
    private ArrayList<InventoryRecord> populateInv(ResultSet r) throws SQLException {
        ArrayList<InventoryRecord> output = new ArrayList<>();
        while (r.next()) {
            output.add(new InventoryRecord(r.getString(1), r.getInt(2), r.getInt(3)));
        }
        return output;
    }
    private ArrayList<User> populateUsers(ResultSet r) throws SQLException {
        ArrayList<User> output = new ArrayList<>();
        while (r.next()) {
            output.add(new User(r.getString(1), r.getString(2),
                    r.getString(3), r.getString(4), r.getBoolean(5)));
        }
        return output;
    }
    private ArrayList<BuyOrder> populateBuys(ResultSet r) throws SQLException {
        ArrayList<BuyOrder> output = new ArrayList<>();
        while (r.next()) {
            LocalDateTime resolved;
            Timestamp re = r.getTimestamp(7);
            if (r.wasNull()) resolved = null;
            else resolved = re.toLocalDateTime();
            output.add(new BuyOrder(r.getInt(1), r.getString(2), r.getInt(3),
                    r.getInt(4), r.getInt(5), r.getTimestamp(6).toLocalDateTime(),
                    resolved, r.getInt(8)));
        }
        return output;
    }
    private ArrayList<SellOrder> populateSells(ResultSet r) throws SQLException {
        ArrayList<SellOrder> output = new ArrayList<>();
        while (r.next()) {
            LocalDateTime resolved;
            Timestamp re = r.getTimestamp(7);
            if (r.wasNull()) resolved = null;
            else resolved = re.toLocalDateTime();
            output.add(new SellOrder(r.getInt(1), r.getString(2), r.getInt(3),
                    r.getInt(4), r.getInt(5), r.getTimestamp(6).toLocalDateTime(),
                    resolved));
        }
        return output;
    }

    private int handleNonselect(ProtocolKeywords keyword, DataPacket info) throws SQLException {
        PreparedStatement statement;
        boolean isUpdate = false;
        if (keyword == SPECIAL) {
            String s = info.filter;
            switch (s) {
                case DROP_PASSWORD:
                    return resetEverything();
                case RECREATE_PASSWORD:
                    setupTables();
                    return 6;
                case TRADE_DELAY_PASSWORD:
                    //get the milliseconds since the last reconciliation
                    long since = ChronoUnit.MILLIS.between(lastReconciliation, LocalDateTime.now());
                    return RECONCILIATION_INTERVAL - (int) since;
                //the number of milliseconds until the next reconciliation
                default:
                    return 0;
            }
        }
        else if (keyword == DELETE) {
            statement = connection.prepareStatement(String.format(GENERIC_DELETE, info.table.getName(), info.filter));
        }
        else {
            if (keyword == INSERT) {
                if (info.table == INV && info.insertTypeFlag) {
                    statement = connection.prepareStatement(INSERT_OR_UPDATE_INV);
                }
                else statement = connection.prepareStatement(
                    String.format(GENERIC_INSERT, info.table.getName(), info.table.colNamesForInsert(),
                            info.table.valuesForInsert()));
            }
            else /*if (keyword == UPDATE)*/ {
                statement = connection.prepareStatement(
                        String.format(GENERIC_UPDATE, info.table.getName(),
                                info.table.templateForUpdate(), info.table.getColumns()[0]));
                isUpdate = true;
            }

            switch (info.table) {
                case UNIT -> {
                    OrgUnit x = (OrgUnit) info.object;
                    statement.setString(1, x.getName());
                    statement.setInt(2,x.getCredits());
                    if (isUpdate) statement.setString(3, x.getName());
                }
                case ASSET -> {
                    Asset x = (Asset) info.object;
                    statement.setString(1, x.getDescription());
                    if (isUpdate) statement.setInt(2, x.getId());
                }
                case USER -> {
                    User x = (User) info.object;
                    statement.setString(1, x.getUsername());
                    statement.setString(2,x.getPassword());
                    statement.setString(3,x.getSalt());
                    String unit = x.getUnit();
                    if (unit != null) statement.setString(4, unit);
                    else statement.setNull(4, Types.VARCHAR);
                    statement.setBoolean(5, x.getAdminAccess());
                    if (isUpdate) statement.setString(6, x.getUsername());
                }
                case INV -> {
                    InventoryRecord x = (InventoryRecord) info.object;
                    statement.setString(1,x.getUnitName());
                    statement.setInt(2,x.getAssetID());
                    statement.setInt(3,x.getQuantity());
                    //this case should never happen
                }
                case SELL -> {
                    SellOrder x = (SellOrder) info.object;
                    statement.setString(1,x.getUnit());
                    statement.setInt(2,x.getAsset());
                    statement.setInt(3,x.getQty());
                    statement.setInt(4,x.getPrice());
                    LocalDateTime datePlaced = x.getDatePlaced();
                    statement.setTimestamp(5, Timestamp.valueOf(datePlaced));
                    LocalDateTime dateResolved = x.getDateResolved();
                    if (dateResolved != null) statement.setTimestamp(6, Timestamp.valueOf(dateResolved));
                    else statement.setNull(6, Types.TIMESTAMP);
                    if (isUpdate) statement.setInt(7,x.getId());
                }
                case BUY -> {
                    BuyOrder x = (BuyOrder) info.object;
                    statement.setString(1,x.getUnit());
                    statement.setInt(2,x.getAsset());
                    statement.setInt(3,x.getQty());
                    statement.setInt(4,x.getPrice());
                    LocalDateTime datePlaced = x.getDatePlaced();
                    statement.setTimestamp(5, Timestamp.valueOf(datePlaced));
                    LocalDateTime dateResolved = x.getDateResolved();
                    if (dateResolved != null) statement.setTimestamp(6, Timestamp.valueOf(dateResolved));
                    else statement.setNull(6, Types.TIMESTAMP);
                    Integer boughtFrom = x.getBoughtFrom();
                    if (boughtFrom != null) statement.setInt(7, boughtFrom);
                    else statement.setNull(7,Types.INTEGER);
                    if (isUpdate) statement.setInt(8,x.getId());
                }
            }
        }
        try {
            return executeModificationQuery(statement);
        }
        catch (SQLIntegrityConstraintViolationException i) {
            //if this exception was thrown, either this is an insert query that hit a duplicate key (we want 0)
            //or an insert/update that was stopped by a FK constraint (we want -1)
            String message = i.getMessage();
            //System.err.println(message);
            if (message.contains("Duplicate")) return 0;
            else return -1;
            //yes this is a bodge but it distinguishes between the possible cases perfectly as far as i can tell
        }
    }

    private ResultSet executeSelectQuery(PreparedStatement statement) throws SQLException , SQLTimeoutException {
        return statement.executeQuery();

    }
    private int executeModificationQuery(PreparedStatement statement) throws SQLException, SQLTimeoutException {
        int returnval = statement.executeUpdate();
        connection.commit();
        return returnval;
    }

}