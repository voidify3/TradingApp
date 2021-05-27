package ServerSide;

import common.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class NetworkServer {

    private static final int PORT = 10000;
    private static final int SOCKET_TIMEOUT = 100;

    private AtomicBoolean running = new AtomicBoolean(true);
    private JDBCDataSource dataSource;
    public Timer tradeReconciliation;
    //STATEMENT TEMPLATE STRINGS AND PREPARED STATEMENT VARS

    public NetworkServer(JDBCDataSource dataSource) {
        this.dataSource = dataSource;
        tradeReconciliation = new Timer();
    }


    /**
     * Handles the connection received from ServerSocket
     * @param socket The socket used to communicate with the currently connected client
     */
    private void handleConnection(Socket socket) throws IOException, ClassNotFoundException {

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
     * Starts the server running on the default port
     */
    public void start() throws IOException {

    }

    /**
     * Requests the server to shut down
     */
    public void shutdown() {
        //Stop firing trade reconciliation
        tradeReconciliation.cancel();
        //Close the database connection
        dataSource.close();
        // Shut the server down
        running.set(false);
    }

    public void reconcileTrades() {
        //TODO: implement algorithm from design doc
        //Ignore orders from users with null orgunit

    }

    /**
     * Public wrapper to simulate a request sent over the connection, exists for test/debug purposes.
     * Should use the same algorithm as a real one
     * @param keyword Query keyword
     * @param info Info packet
     */
    public void simulateRequest(String keyword, DataPacket info) {

    }

    /**
     * Empty the database. Only exists for test and debug convenience, may deprecate later due to unsafeness
     */
    public void resetEverything() throws SQLException {
        //Execute on the data source a statement or series of statements
        // With the result of all the tables being empty at the end of the method
    }
}
