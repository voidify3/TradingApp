package ServerSide;

import java.sql.*;

/**
 * Class dealing with the database
 * Structure based on Week 7 Address book exercise
 */
public class JDBCDataSource {

    public Connection getConnection() {
        return connection;
    }

    private Connection connection;
    static final String CREATE_TABLES =
                    "CREATE TABLE IF NOT EXISTS orgunit ("
                    + "name VARCHAR(30) PRIMARY KEY NOT NULL UNIQUE,"
                    + "credits INTEGER," + ");" +

                    "CREATE TABLE IF NOT EXISTS asset ("
                    + "idx INTEGER PRIMARY KEY /*!40101 AUTO_INCREMENT */ NOT NULL UNIQUE," // from https://stackoverflow.com/a/41028314
                    + "description VARCHAR(60)," + ");" +

                    "CREATE TABLE IF NOT EXISTS user ("
                    + "name VARCHAR(30) PRIMARY KEY NOT NULL UNIQUE,"
                    + "passhash VARCHAR(128) NOT NULL,"
                    + "salt VARCHAR(41) NOT NULL,"
                    + "orgunit VARCHAR(30),"
                    + "FOREIGN KEY (orgunit) REFERENCES orgunit (name)"
                    + "ON DELETE SET NULL ON UPDATE CASCADE" + ");" +

                    "CREATE TABLE IF NOT EXISTS inventories ("
                    + "orgunit VARCHAR(30) NOT NULL,"
                    + "asset INTEGER NOT NULL,"
                    + "quantity INTEGER NOT NULL,"
                    + "FOREIGN KEY (orgunit) REFERENCES orgunit (name)"
                    + "ON DELETE CASCADE ON UPDATE CASCADE,"
                    + "FOREIGN KEY (asset) REFERENCES asset (idx)"
                    + "ON DELETE CASCADE ON UPDATE CASCADE,"
                    + "PRIMARY KEY(orgunit, asset)" + ");" +

                    "CREATE TABLE IF NOT EXISTS sellorder ("
                    + "idx INTEGER PRIMARY KEY /*!40101 AUTO_INCREMENT */ NOT NULL UNIQUE," // from https://stackoverflow.com/a/41028314
                    + "user VARCHAR(30) NOT NULL,"
                    + "asset INTEGER NOT NULL,"
                    + "quantity INTEGER NOT NULL,"
                    + "price INTEGER NOT NULL,"
                    + "datePlaced DATETIME NOT NULL,"
                    + "dateResolved DATETIME,"
                    + "FOREIGN KEY (user) REFERENCES user (name)"
                    + "ON DELETE RESTRICT ON UPDATE CASCADE,"
                    + "FOREIGN KEY (asset) REFERENCES asset (idx)"
                    + "ON DELETE CASCADE ON UPDATE CASCADE" + ");" +

                    "CREATE TABLE IF NOT EXISTS buyorder ("
                    + "idx INTEGER PRIMARY KEY /*!40101 AUTO_INCREMENT */ NOT NULL UNIQUE," // from https://stackoverflow.com/a/41028314
                    + "user VARCHAR(30) NOT NULL,"
                    + "asset INTEGER NOT NULL,"
                    + "quantity INTEGER NOT NULL,"
                    + "price INTEGER NOT NULL,"
                    + "datePlaced DATETIME NOT NULL,"
                    + "dateResolved DATETIME,"
                    + "boughtFrom INTEGER,"
                    + "FOREIGN KEY (user) REFERENCES user (name)"
                    + "ON DELETE RESTRICT ON UPDATE CASCADE,"
                    + "FOREIGN KEY (asset) REFERENCES asset (idx)"
                    + "ON DELETE CASCADE ON UPDATE CASCADE,"
                    + "FOREIGN KEY (boughtFrom) REFERENCES sellorder (idx)"
                    + "ON DELETE RESTRICT ON UPDATE CASCADE" + ");"
            ;
    static final String CLEAR_DATA = "DELETE FROM buyorder; DELETE FROM sellorder;"
        + "DELETE FROM inventories; DELETE FROM asset; DELETE FROM user; DELETE FROM orgunit;";
    public JDBCDataSource() {
        connection = DBConnection.getInstance();
        try {
            Statement st = connection.createStatement();
            st.execute(CREATE_TABLES);
            //INITIALISATION OF PREPARED STATEMENTS
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    public void close() {
        try {
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public ResultSet executeSelectQuery(PreparedStatement statement) throws SQLException , SQLTimeoutException {
        return statement.executeQuery();

    }
    public int executeModificationQuery(PreparedStatement statement) throws SQLException, SQLTimeoutException {
        return statement.executeUpdate();
        //TODO: make sure a failed INSERT returns 0 instead of exceptioning
        // This can be done at the statement level by having "on duplicate key update id=id"
    }
}
