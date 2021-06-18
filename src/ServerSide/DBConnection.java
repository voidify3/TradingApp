package ServerSide;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Entire class borrowed from the Week 7 Address Book Exercise. Singleton database connection.
 * This is the class where db.props is read
 */
class DBConnection {
    /**
     * The singleton instance of the database connection.
     */
    private static Connection instance = null;

    /**
     * Constructor intializes the connection.
     */
    private DBConnection(Boolean autoCommit) {
        Properties props = new Properties();
        FileInputStream in = null;
        try {
            System.out.println(System.getProperty("user.dir"));
            in = new FileInputStream("src/db.props");
            props.load(in);
            in.close();

            // specify the data source, username and password
            String url = props.getProperty("jdbc.url");
            String username = props.getProperty("jdbc.username");
            String password = props.getProperty("jdbc.password");
            String schema = props.getProperty("jdbc.schema");

            // get a connection
            instance = DriverManager.getConnection(url + "/" + schema, username,
                    password);
            instance.setAutoCommit(autoCommit);
        } catch (SQLException sqle) {
            System.err.println(sqle);
        } catch (FileNotFoundException fnfe) {
            System.err.println(fnfe);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Provides global access to the singleton instance of the Connection.
     *
     * @return a handle to the singleton instance of the Connection.
     */
    public static Connection getInstance() {
        if (instance == null) {
            new DBConnection(false);
        }
        return instance;
    }
}
