package ServerSide;

import common.*;
import static common.DatabaseTables.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @author Sophia Walsh Long
 */
public class NetworkServer {

    private static final int PORT = 10000;
    private static final int SOCKET_TIMEOUT = 100;

    private AtomicBoolean running = new AtomicBoolean(true);
    //private JDBCDataSource dataSource;
    public Timer tradeReconciliation;
    public Connection getConnection() {
        return connection;
    }

    private final Connection connection;
    //Create Table scripts for all tables, using table and column names from the enum
    // week 7 address book exercise used as reference for syntax; "from stackoverflow" comment is from that
    static final String CREATE_TABLE_UNIT =
            "CREATE TABLE IF NOT EXISTS " + UNIT.getTableName() + " ("
                    + UNIT.getColumnNames()[0] + " VARCHAR(30) PRIMARY KEY NOT NULL UNIQUE," //name
                    + UNIT.getColumnNames()[1] + " INTEGER" + ");"; //credits
    static final String CREATE_TABLE_ASSET =
            "CREATE TABLE IF NOT EXISTS " + ASSET.getTableName() + " ("
                    + ASSET.getColumnNames()[0] + " INTEGER "
                    + "PRIMARY KEY /*!40101 AUTO_INCREMENT */ NOT NULL UNIQUE," //from https://stackoverflow.com/a/41028314
                    + ASSET.getColumnNames()[1] + " VARCHAR(60)" + ");"; //description
    static final String CREATE_TABLE_USER =
            "CREATE TABLE IF NOT EXISTS " + USER.getTableName() + " ("
                    + USER.getColumnNames()[0] + " VARCHAR(30) PRIMARY KEY NOT NULL UNIQUE," //name
                    + USER.getColumnNames()[1] + " VARCHAR(128) NOT NULL," //hashed password
                    + USER.getColumnNames()[2] + " VARCHAR(41) NOT NULL," //salt string
                    + USER.getColumnNames()[3] + " VARCHAR(30)," //orgunit
                    + "CONSTRAINT fk_user_orgunit FOREIGN KEY (" + USER.getColumnNames()[3]
                    + ") REFERENCES " + UNIT.getTableName() + " (" + UNIT.getColumnNames()[0]
                    + ") ON DELETE SET NULL ON UPDATE CASCADE" + ");";
    static final String CREATE_TABLE_INV =
            "CREATE TABLE IF NOT EXISTS " + INV.getTableName() + " ("
                    + INV.getColumnNames()[0] + " VARCHAR(30) NOT NULL," //orgunit
                    + INV.getColumnNames()[1] + " INTEGER NOT NULL," //asset
                    + INV.getColumnNames()[2] + " INTEGER NOT NULL," //quantity
                    + "CONSTRAINT fk_inventories_orgunit FOREIGN KEY (" + INV.getColumnNames()[0]
                    + ") REFERENCES " + UNIT.getTableName() + " (" + UNIT.getColumnNames()[0]
                    + ") ON DELETE CASCADE ON UPDATE CASCADE,"
                    + "CONSTRAINT fk_inventories_asset FOREIGN KEY (" + INV.getColumnNames()[1]
                    + ") REFERENCES " + ASSET.getTableName() + " (" + ASSET.getColumnNames()[0]
                    + ") ON DELETE CASCADE ON UPDATE CASCADE,"
                    + "PRIMARY KEY(" + INV.getColumnNames()[0] + "," + INV.getColumnNames()[1] + ")"
                    + ");";
    static final String CREATE_TABLE_SELL =
            "CREATE TABLE IF NOT EXISTS " + SELL.getTableName() + " ("
                    + SELL.getColumnNames()[0] + " INTEGER PRIMARY KEY /*!40101 AUTO_INCREMENT */ NOT NULL UNIQUE,"
                    + SELL.getColumnNames()[1] + " VARCHAR(30) NOT NULL," //user
                    + SELL.getColumnNames()[2] + " INTEGER NOT NULL," //asset
                    + SELL.getColumnNames()[3] + " INTEGER NOT NULL," //quantity
                    + SELL.getColumnNames()[4] + " INTEGER NOT NULL," //price
                    + SELL.getColumnNames()[5] + " DATETIME NOT NULL," //datePlaced
                    + SELL.getColumnNames()[6] + " DATETIME," //dateResolved
                    + "CONSTRAINT fk_sell_user FOREIGN KEY (" + SELL.getColumnNames()[1]
                    + ") REFERENCES " + USER.getTableName() + " (" + USER.getColumnNames()[0]
                    + ") ON DELETE RESTRICT ON UPDATE CASCADE,"
                    + "CONSTRAINT fk_sell_asset FOREIGN KEY (" + SELL.getColumnNames()[2]
                    + ") REFERENCES " + ASSET.getTableName() + " (" + ASSET.getColumnNames()[0]
                    + ") ON DELETE CASCADE ON UPDATE CASCADE" + ");";
    static final String CREATE_TABLE_BUY =
            "CREATE TABLE IF NOT EXISTS " + BUY.getTableName() + " ("
                    + BUY.getColumnNames()[0] + " INTEGER PRIMARY KEY /*!40101 AUTO_INCREMENT */ NOT NULL UNIQUE,"
                    + BUY.getColumnNames()[1] + " VARCHAR(30) NOT NULL," //user
                    + BUY.getColumnNames()[2] + " INTEGER NOT NULL," //asset
                    + BUY.getColumnNames()[3] + " INTEGER NOT NULL," //quantity
                    + BUY.getColumnNames()[4] + " INTEGER NOT NULL," //price
                    + BUY.getColumnNames()[5] + " DATETIME NOT NULL," //datePlaced
                    + BUY.getColumnNames()[6] + " DATETIME," //dateResolved
                    + BUY.getColumnNames()[7] + " INTEGER," //boughtFrom
                    + "CONSTRAINT fk_buy_user FOREIGN KEY (" + BUY.getColumnNames()[1]
                    + ") REFERENCES " + USER.getTableName() + " (" + USER.getColumnNames()[0]
                    + ") ON DELETE RESTRICT ON UPDATE CASCADE,"
                    + "CONSTRAINT fk_buy_asset FOREIGN KEY (" + BUY.getColumnNames()[2]
                    + ") REFERENCES " + ASSET.getTableName() + " (" + ASSET.getColumnNames()[0]
                    + ") ON DELETE CASCADE ON UPDATE CASCADE,"
                    + "CONSTRAINT fk_buy_sell FOREIGN KEY (" + BUY.getColumnNames()[7]
                    + ") REFERENCES " + SELL.getTableName() + " (" + SELL.getColumnNames()[0]
                    + ") ON DELETE RESTRICT ON UPDATE CASCADE" + ");";
    /*
        static final String CREATE_TABLES = CREATE_TABLE_UNIT + CREATE_TABLE_ASSET + CREATE_TABLE_USER +
                CREATE_TABLE_INV + CREATE_TABLE_SELL + CREATE_TABLE_BUY;
    */
    static final String[] CLEAR_DATA = {"DROP TABLE buyorder;", "DROP TABLE sellorder;", "DROP TABLE inventories;", "DROP TABLE asset;", "DROP TABLE user;", "DROP TABLE orgunit;"};

    private static final String GET_ASSETS = "SELECT * FROM asset;";
    private static final String RECONCILIATION_GET_SELLS =
            "SELECT sellorder.*, user.orgunit FROM sellorder" +
                    "LEFT JOIN user ON sellorder.user = user.name" +
                    "WHERE dateResolved IS NULL AND asset=? and user.orgunit IS NOT NULL" +
                    "ORDER BY datePlaced;";
    private static final String RECONCILIATION_GET_BUYS =
            "SELECT buyorder.*, user.orgunit FROM buyorder" +
                    "LEFT JOIN user ON buyorder.user = user.name" +
                    "WHERE dateResolved IS NULL AND asset=? and user.orgunit IS NOT NULL" +
                    "ORDER BY datePlaced;";
    private static final String RECONCILIATION_RESOLVE_BUY =
            "UPDATE buyorder" +
                    "SET price=?, dateResolved=?, boughtFrom=?" +
                    "WHERE idx=?;";
    private static final String RECONCILIATION_RESOLVE_SELL =
            "UPDATE sellorder" +
                    "SET quantity=?, dateResolved=?" +
                    "WHERE idx=?;";
    private static final String RECONCILIATION_ADJUST_BALANCE =
            "UPDATE orgunit" +
                    "SET credits=credits+?" +
                    "WHERE name=?;";

    //TODO: prepared statements for each valid query type
    private static final String INSERT_OR_UPDATE_INV =
            "INSERT INTO inventories (orgunit, asset, quantity)" +
                    "VALUES (?, ?, ?)" +
                    "ON DUPLICATE KEY UPDATE " +
                    "orgunit=values(orgunit), asset=values(asset), quantity=values(quantity);";
    private static final String INSERT_OR_ADJUST_INV =
            "INSERT INTO inventories (orgunit, asset, quantity)" +
                    "VALUES (?, ?, ?)" +
                    "ON DUPLICATE KEY UPDATE " +
                    "orgunit=values(orgunit), asset=values(asset), quantity=quantity + values(quantity);";

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
    public NetworkServer() {
        tradeReconciliation = new Timer();
        connection = DBConnection.getInstance();
        try {
            Statement st = connection.createStatement();
            st.addBatch(CREATE_TABLE_UNIT);
            st.addBatch(CREATE_TABLE_ASSET);
            st.addBatch(CREATE_TABLE_USER);
            st.addBatch(CREATE_TABLE_INV);
            st.addBatch(CREATE_TABLE_SELL);
            st.addBatch(CREATE_TABLE_BUY);
            st.executeBatch();
            connection.commit();
            //INITIALISATION OF PREPARED STATEMENTS
            getAssets = connection.prepareStatement(GET_ASSETS);
            insertUpdateInv = connection.prepareStatement(INSERT_OR_UPDATE_INV);
            insertAdjustInv = connection.prepareStatement(INSERT_OR_ADJUST_INV);
            getSellsReconciliation = connection.prepareStatement(RECONCILIATION_GET_SELLS);
            getBuysReconciliation = connection.prepareStatement(RECONCILIATION_GET_BUYS);
            resolveBuy = connection.prepareStatement(RECONCILIATION_RESOLVE_BUY);
            resolveSell = connection.prepareStatement(RECONCILIATION_RESOLVE_SELL);
            adjustBalance = connection.prepareStatement(RECONCILIATION_ADJUST_BALANCE);
            //TODO: the rest
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Handles the connection received from ServerSocket
     * @param socket The socket used to communicate with the currently connected client
     */
    private void handleConnection(Socket socket) throws IOException, ClassNotFoundException {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream()))
        {
            ProtocolKeywords command = (ProtocolKeywords) objectInputStream.readObject();
            DataPacket info =  (DataPacket) objectInputStream.readObject();

            try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());)
            {
                handleRequest(command, info, objectOutputStream);
            }

        }
    }

    private void handleRequest(ProtocolKeywords keyword, DataPacket info, ObjectOutputStream out) throws IOException {
        if (keyword == ProtocolKeywords.SELECT) {
            out.writeObject(handleSelect(info));
        }
        else {
            out.writeObject(handleNonselect(keyword,info));
        }
    }

    private ArrayList<DataObject> handleSelect(DataPacket info) {
        return null;
    }

    private int handleNonselect(ProtocolKeywords keyword, DataPacket info) {
        return 0;
    }


    /**
     * Returns the port the server is configured to use
     *
     * @return The port number
     */
    public static int getPort() {
        return PORT;
    }

    /**
     * Starts the server running on the default port, code borrowed from week 7 exercise
     */
    public void start() throws IOException {
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
    public void shutdown() {
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

    public ResultSet executeSelectQuery(PreparedStatement statement) throws SQLException , SQLTimeoutException {
        return statement.executeQuery();

    }
    public int executeModificationQuery(PreparedStatement statement) throws SQLException, SQLTimeoutException {
        int returnval = statement.executeUpdate();
        connection.commit();
        return returnval;
        //TODO: make sure a failed INSERT returns 0 instead of exceptioning
        // This can be done at the statement level by having "on duplicate key update id=id"
    }

    public void reconcileTrades() {
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
                        if (buyQty <= sellQty && buyPrice >= sellPrice) {
                            System.out.printf("Resolving sell order %d and buy order %d...", sellID, buyID);
                            int newSellQty = sellQty - buyQty;
                            int priceDiff = buyPrice - sellPrice;
                            int totalPrice = buyQty * sellPrice;
                            resolveBuy.setInt(1, sellPrice);
                            resolveBuy.setTimestamp(2, now);
                            resolveBuy.setInt(3, sellID);
                            resolveBuy.setInt(4, buyID);
                            resolveBuy.execute();

                            resolveSell.setInt(1,newSellQty);
                            if (newSellQty == 0) resolveSell.setTimestamp(2,now);
                            else resolveSell.setNull(2,Types.TIMESTAMP);
                            resolveSell.setInt(3,sellID);
                            resolveSell.execute();

                            adjustBalance.setInt(1, totalPrice);
                            adjustBalance.setString(2, sellUnit);
                            adjustBalance.addBatch();
                            if (priceDiff < 0){ //refund the difference if needed
                                adjustBalance.setInt(1, priceDiff);
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
                            System.out.println(" Done!");
                            resolutionCount++;
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

    /**
     * Wrapper to simulate a request sent over the connection, exists for test/debug purposes.
     * Should use the same algorithm as a real one
     * @param keyword Query keyword
     * @param info Info packet
     */
    void simulateRequest(ProtocolKeywords keyword, DataPacket info, ObjectOutputStream o) throws IOException {
        handleRequest(keyword, info, o);

    }

    ArrayList<DataObject> simulateSelect(DataPacket info) {
        return handleSelect(info);
    }

    int simulateNonselect(ProtocolKeywords keyword, DataPacket info) {
        return handleNonselect(keyword, info);
    }

    /**
     * Empty the database. Only exists for test and debug convenience, may deprecate later due to unsafeness
     */
    public void resetEverything() throws SQLException {
        for (String drop : CLEAR_DATA) {
            connection.prepareStatement(drop).execute();
        }
        connection.commit();
        System.out.println("All tables dropped");
    }
}
