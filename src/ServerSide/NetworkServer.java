package ServerSide;

import common.*;
import common.Exceptions.IllegalString;

import static common.DatabaseTables.*;
import static common.ProtocolKeywords.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @author Sophia Walsh Long
 */
class NetworkServer {

    private static final int PORT = 10000;
    private static final int SOCKET_TIMEOUT = 100;

    private AtomicBoolean running = new AtomicBoolean(true);
    //private JDBCDataSource dataSource;
    Timer tradeReconciliation;
    Connection getConnection() {
        return connection;
    }

    private final Connection connection;
    //Create Table scripts for all tables, using table and column names from the enum
    // week 7 address book exercise used as reference for syntax; "from stackoverflow" comment is from that
    static final String CREATE_TABLE_UNIT =
            "CREATE TABLE IF NOT EXISTS " + UNIT.getName() + " ("
                    + UNIT.getColumns()[0] + " VARCHAR(30) PRIMARY KEY NOT NULL UNIQUE," //name
                    + UNIT.getColumns()[1] + " INTEGER" + ");"; //credits
    static final String CREATE_TABLE_ASSET =
            "CREATE TABLE IF NOT EXISTS " + ASSET.getName() + " ("
                    + ASSET.getColumns()[0] + " INTEGER "
                    + "PRIMARY KEY /*!40101 AUTO_INCREMENT */ NOT NULL UNIQUE," //from https://stackoverflow.com/a/41028314
                    + ASSET.getColumns()[1] + " VARCHAR(60)" + ");"; //description
    static final String CREATE_TABLE_USER =
            "CREATE TABLE IF NOT EXISTS " + USER.getName() + " ("
                    + USER.getColumns()[0] + " VARCHAR(30) PRIMARY KEY NOT NULL UNIQUE," //name
                    + USER.getColumns()[1] + " VARCHAR(128) NOT NULL," //hashed password
                    + USER.getColumns()[2] + " VARCHAR(41) NOT NULL," //salt string
                    + USER.getColumns()[3] + " VARCHAR(30)," //orgunit
                    + USER.getColumns()[4] + " BOOLEAN," //adminAccess
                    + "CONSTRAINT fk_user_orgunit FOREIGN KEY (" + USER.getColumns()[3]
                    + ") REFERENCES " + UNIT.getName() + " (" + UNIT.getColumns()[0]
                    + ") ON DELETE SET NULL ON UPDATE CASCADE" + ");";
    static final String CREATE_TABLE_INV =
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
    static final String CREATE_TABLE_SELL =
            "CREATE TABLE IF NOT EXISTS " + SELL.getName() + " ("
                    + SELL.getColumns()[0] + " INTEGER PRIMARY KEY /*!40101 AUTO_INCREMENT */ NOT NULL UNIQUE,"
                    + SELL.getColumns()[1] + " VARCHAR(30) NOT NULL," //user
                    + SELL.getColumns()[2] + " INTEGER NOT NULL," //asset
                    + SELL.getColumns()[3] + " INTEGER NOT NULL," //quantity
                    + SELL.getColumns()[4] + " INTEGER NOT NULL," //price
                    + SELL.getColumns()[5] + " DATETIME NOT NULL," //datePlaced
                    + SELL.getColumns()[6] + " DATETIME," //dateResolved
                    + "CONSTRAINT fk_sell_user FOREIGN KEY (" + SELL.getColumns()[1]
                    + ") REFERENCES " + USER.getName() + " (" + USER.getColumns()[0]
                    + ") ON DELETE RESTRICT ON UPDATE CASCADE,"
                    + "CONSTRAINT fk_sell_asset FOREIGN KEY (" + SELL.getColumns()[2]
                    + ") REFERENCES " + ASSET.getName() + " (" + ASSET.getColumns()[0]
                    + ") ON DELETE CASCADE ON UPDATE CASCADE" + ");";
    static final String CREATE_TABLE_BUY =
            "CREATE TABLE IF NOT EXISTS " + BUY.getName() + " ("
                    + BUY.getColumns()[0] + " INTEGER PRIMARY KEY /*!40101 AUTO_INCREMENT */ NOT NULL UNIQUE,"
                    + BUY.getColumns()[1] + " VARCHAR(30) NOT NULL," //user
                    + BUY.getColumns()[2] + " INTEGER NOT NULL," //asset
                    + BUY.getColumns()[3] + " INTEGER NOT NULL," //quantity
                    + BUY.getColumns()[4] + " INTEGER NOT NULL," //price
                    + BUY.getColumns()[5] + " DATETIME NOT NULL," //datePlaced
                    + BUY.getColumns()[6] + " DATETIME," //dateResolved
                    + BUY.getColumns()[7] + " INTEGER," //boughtFrom
                    + "CONSTRAINT fk_buy_user FOREIGN KEY (" + BUY.getColumns()[1]
                    + ") REFERENCES " + USER.getName() + " (" + USER.getColumns()[0]
                    + ") ON DELETE RESTRICT ON UPDATE CASCADE,"
                    + "CONSTRAINT fk_buy_asset FOREIGN KEY (" + BUY.getColumns()[2]
                    + ") REFERENCES " + ASSET.getName() + " (" + ASSET.getColumns()[0]
                    + ") ON DELETE CASCADE ON UPDATE CASCADE,"
                    + "CONSTRAINT fk_buy_sell FOREIGN KEY (" + BUY.getColumns()[7]
                    + ") REFERENCES " + SELL.getName() + " (" + SELL.getColumns()[0]
                    + ") ON DELETE RESTRICT ON UPDATE CASCADE" + ");";
    /*
        static final String CREATE_TABLES = CREATE_TABLE_UNIT + CREATE_TABLE_ASSET + CREATE_TABLE_USER +
                CREATE_TABLE_INV + CREATE_TABLE_SELL + CREATE_TABLE_BUY;
    */
    static final String TOTAL_RECORDS_WRAPPER = "SELECT %s AS SUM;";
    static final String TOTAL_RECORDS_INNER = "(SELECT COUNT(*) FROM %s)";
    static final String[] CLEAR_DATA = {"DROP TABLE " + BUY.getName() + ";", "DROP TABLE " + SELL.getName()
            + ";", "DROP TABLE " + INV.getName() + ";", "DROP TABLE " + ASSET.getName() + ";",
            "DROP TABLE " + USER.getName() + ";", "DROP TABLE " + UNIT.getName() + ";"};

    private static final String GET_ASSETS = "SELECT * FROM asset;";
    private static final String RECONCILIATION_GET_SELLS =
            "SELECT sellorder.*, user.orgunit FROM sellorder " +
                    "LEFT JOIN user ON sellorder.user = user.name " +
                    "WHERE dateResolved IS NULL AND asset=? " +
                    "AND user.orgunit IS NOT NULL " +
                    "ORDER BY datePlaced, idx;";
    private static final String RECONCILIATION_GET_BUYS =
            "SELECT buyorder.*, user.orgunit FROM buyorder " +
                    "LEFT JOIN user ON buyorder.user = user.name " +
                    "WHERE dateResolved IS NULL AND asset=? " +
                    "AND user.orgunit IS NOT NULL " +
                    "ORDER BY datePlaced, idx;";
    private static final String RECONCILIATION_RESOLVE_BUY =
            "UPDATE buyorder " +
                    "SET price=?, dateResolved=?, boughtFrom=? " +
                    "WHERE idx=?;";
    private static final String RECONCILIATION_RESOLVE_SELL =
            "UPDATE sellorder " +
                    "SET quantity=?, dateResolved=? " +
                    "WHERE idx=?;";
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
    private PreparedStatement resolveBuy;
    private PreparedStatement resolveSell;
    private PreparedStatement adjustBalance;
    private PreparedStatement insertUpdateInv;
    private PreparedStatement insertAdjustInv;

/*
    public NetworkServer(JDBCDataSource dataSource) {
        this.dataSource = dataSource;
        tradeReconciliation = new Timer();
        Connection conn = dataSource.getConnection();
        try {
            getAssets = conn.prepareStatement(GET_ASSETS);
            getSellsReconciliation = conn.prepareStatement(RECONCILIATION_GET_SELLS);
            getBuysReconciliation = conn.prepareStatement(RECONCILIATION_GET_BUYS);
        }
        catch (SQLException ex){
            ex.printStackTrace();
        }
    }
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
            getSellsReconciliation = connection.prepareStatement(RECONCILIATION_GET_SELLS);
            getBuysReconciliation = connection.prepareStatement(RECONCILIATION_GET_BUYS);
            resolveBuy = connection.prepareStatement(RECONCILIATION_RESOLVE_BUY);
            resolveSell = connection.prepareStatement(RECONCILIATION_RESOLVE_SELL);
            adjustBalance = connection.prepareStatement(RECONCILIATION_ADJUST_BALANCE);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

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
            catch (SQLException | IllegalString e) {
                e.printStackTrace();
            }

        }
    }

    private void handleRequest(ProtocolKeywords keyword, DataPacket info, ObjectOutputStream out) throws IOException, SQLException, IllegalString {
        if (keyword == ProtocolKeywords.SELECT) out.writeObject(handleSelect(info));
        else out.writeObject(handleNonselect(keyword, info));
    }

    private ArrayList<DataObject> handleSelect(DataPacket info) throws SQLException, IllegalString {
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
    private ArrayList<OrgUnit> populateUnits(ResultSet r) throws SQLException, IllegalString {
        ArrayList<OrgUnit> output = new ArrayList<>();
        while (r.next()) {
            output.add(new OrgUnit(r.getString(1), r.getInt(2)));
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
            if (s.equals(DROP_PASSWORD)) {
                return resetEverything();
            }
            else if (s.equals(RECREATE_PASSWORD)) {
                setupTables();
                return 6;
            }
            else {
                return 0;
            }
        }
        else if (keyword == DELETE) {
            statement = connection.prepareStatement(String.format(GENERIC_DELETE, info.table.getName(), info.filter));
            if (info.table == UNIT) {
                //here, we need to use an indirect check to avoid situations where a user with null unit
                // has active orders. so attempt and then roll back a USER delete on all members of would-be-deleted units
                // and if an exception was thrown some users have buy/sell orders so cancel the whole deletion
                try {
                    String query = String.format(GENERIC_DELETE, USER.getName(), USER.getColumns()[3] +
                            " IN (" + String.format(GENERIC_SELECT, UNIT.getName(), info.filter, UNIT.getColumns()[0], UNIT.getColumns()[1])
                            .replace(";", "") + ")").replace("*", UNIT.getColumns()[0]);
                    //System.out.println(query);
                    connection.prepareStatement(query).executeUpdate();
                    connection.rollback();
                }
                catch (SQLIntegrityConstraintViolationException e) {
                    return -1;
                }
            }
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
                    else {
                        statement.setNull(4, Types.VARCHAR);
                        //here, we need to use an indirect check to avoid situations where a user with null unit
                        // has active orders. Get the number of
                        if (isUpdate) {
                            int orderCount = totalRecordCount(new String[]{
                                    BUY.getName() + " WHERE " + BUY.getColumns()[1] + "= '" + x.getUsername() + "'",
                                    SELL.getName() + " WHERE " + SELL.getColumns()[1] + "= '" + x.getUsername() + "'",
                            });
                            if (orderCount > 0) return -1;
                        }
                    }
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
                    statement.setString(1,x.getUser());
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
                    statement.setString(1,x.getUser());
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
            //or the query was stopped by a FK constraint (for all types of this we want -1)
            String message = i.getMessage();
            //System.err.println(message);
            if (message.contains("Duplicate")) return 0;
            else return -1;
            //yes this is a bodge but it distinguishes between the possible cases perfectly as far as i can tell
        }
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

    private ResultSet executeSelectQuery(PreparedStatement statement) throws SQLException , SQLTimeoutException {
        return statement.executeQuery();

    }
    private int executeModificationQuery(PreparedStatement statement) throws SQLException, SQLTimeoutException {
        int returnval = statement.executeUpdate();
        connection.commit();
        return returnval;
    }

    void reconcileTrades() {
        try {
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            ResultSet assets = executeSelectQuery(getAssets);
            int resolutionCount = 0;
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
                    String sellUnit = sells.getString(8);
                    while (buys.next()) {
                        int buyID = buys.getInt(1);
                        if (ignoredBuys.contains(buyID)) continue;
                        int buyQty = buys.getInt(4);
                        int buyPrice = buys.getInt(5);
                        String buyUnit = buys.getString(9);
                        System.out.printf("Comparing sell order %d (%d, $%d) and buy order %d (%d, $%d)...",
                                sellID, sellQty, sellPrice, buyID, buyQty, buyPrice);
                        if (buyQty <= sellQty && buyPrice >= sellPrice) {
                            //System.out.printf("Resolving sell order %d and buy order %d...", sellID, buyID);
                            int newSellQty = sellQty - buyQty;
                            int refundAmount = (buyPrice - sellPrice)*buyQty;
                            int totalPrice = buyQty * sellPrice;
                            resolveBuy.setInt(1, sellPrice);
                            resolveBuy.setTimestamp(2, now);
                            resolveBuy.setInt(3, sellID);
                            resolveBuy.setInt(4, buyID);
                            resolveBuy.execute();

                            sellQty = newSellQty;

                            resolveSell.setInt(1,newSellQty);
                            if (newSellQty == 0) resolveSell.setTimestamp(2,now);
                            else resolveSell.setNull(2,Types.TIMESTAMP);
                            resolveSell.setInt(3,sellID);
                            resolveSell.execute();

                            adjustBalance.setInt(1, totalPrice);
                            adjustBalance.setString(2, sellUnit);
                            adjustBalance.addBatch();
                            if (refundAmount > 0){ //refund the difference if needed
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
                            System.out.println(" Failed to resolve.");
                        }
                    }
                }
                System.out.printf("Reconciliation done for asset %d (%s)\n", assetid, assetdesc);
            }
            System.out.printf("Trade reconciliation complete! %d transactions processed\n", resolutionCount);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    int totalRecordCount() throws SQLException {
        return totalRecordCount(DatabaseTables.values());
    }
    int totalRecordCount(DatabaseTables[] d) throws SQLException {
        String[] s = new String[d.length];
        for (int i = 0; i<d.length;i++) {
            s[i]=d[i].getName();
        }
        return totalRecordCount(s);
    }
    int totalRecordCount(String[] d) throws SQLException {
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
    ArrayList<DataObject> simulateSelect(DataPacket info) throws SQLException, IllegalString {
        return handleSelect(info);
    }

    /**
     * Non-private wrapper for testing INSERT, UPDATE, DELETE queries
     * @param keyword Query type
     * @param info DataPacket of query
     * @return Query status number (1 for "success", 0 for "failure due to existence/nonexistence of matching record",
     * -1 for "failure due to constraints", 2 for "INSERT ON DUPLICATE KEY UPDATE query didn't insert but updated"
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
}
