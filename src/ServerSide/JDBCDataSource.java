package ServerSide;

import java.sql.*;

/**
 * Class dealing with the database
 * Structure based on Week 7 Address book exercise
 * Deprecated- structure was simplified 30/5 by moving all uses to NetworkServer
 */
@Deprecated
class JDBCDataSource {

    public Connection getConnection() {
        return connection;
    }

    private Connection connection;
    static final String CREATE_TABLE_UNIT =
            "CREATE TABLE IF NOT EXISTS orgunit ("
                    + "name VARCHAR(30) PRIMARY KEY NOT NULL UNIQUE,"
                    + "credits INTEGER" + ");\n";
    static final String CREATE_TABLE_ASSET =
            "CREATE TABLE IF NOT EXISTS asset ("
                    + "idx INTEGER PRIMARY KEY /*!40101 AUTO_INCREMENT */ NOT NULL UNIQUE," // from https://stackoverflow.com/a/41028314
                    + "description VARCHAR(60)" + ");\n";
    static final String CREATE_TABLE_USER =
            "CREATE TABLE IF NOT EXISTS user ("
                    + "name VARCHAR(30) PRIMARY KEY NOT NULL UNIQUE,"
                    + "passhash VARCHAR(128) NOT NULL,"
                    + "salt VARCHAR(41) NOT NULL,"
                    + "orgunit VARCHAR(30),"
                    + "CONSTRAINT fk_user_orgunit FOREIGN KEY (orgunit) REFERENCES orgunit (name)"
                    + "ON DELETE SET NULL ON UPDATE CASCADE" + ");\n";
    static final String CREATE_TABLE_INV =
            "CREATE TABLE IF NOT EXISTS inventories ("
                    + "orgunit VARCHAR(30) NOT NULL,"
                    + "asset INTEGER NOT NULL,"
                    + "quantity INTEGER NOT NULL,"
                    + "CONSTRAINT fk_inv_orgunit FOREIGN KEY (orgunit) REFERENCES orgunit (name)"
                    + "ON DELETE CASCADE ON UPDATE CASCADE,"
                    + "CONSTRAINT fk_inv_asset FOREIGN KEY (asset) REFERENCES asset (idx)"
                    + "ON DELETE CASCADE ON UPDATE CASCADE,"
                    + "PRIMARY KEY(orgunit, asset)" + ");\n";
    static final String CREATE_TABLE_SELL =
            "CREATE TABLE IF NOT EXISTS sellorder ("
                    + "idx INTEGER PRIMARY KEY /*!40101 AUTO_INCREMENT */ NOT NULL UNIQUE," // from https://stackoverflow.com/a/41028314
                    + "user VARCHAR(30) NOT NULL,"
                    + "asset INTEGER NOT NULL,"
                    + "quantity INTEGER NOT NULL,"
                    + "price INTEGER NOT NULL,"
                    + "datePlaced DATETIME NOT NULL,"
                    + "dateResolved DATETIME,"
                    + "CONSTRAINT fk_sell_user FOREIGN KEY (user) REFERENCES user (name)"
                    + "ON DELETE RESTRICT ON UPDATE CASCADE,"
                    + "CONSTRAINT fk_sell_asset FOREIGN KEY (asset) REFERENCES asset (idx)"
                    + "ON DELETE CASCADE ON UPDATE CASCADE" + ");\n";
    static final String CREATE_TABLE_BUY =
            "CREATE TABLE IF NOT EXISTS buyorder ("
                    + "idx INTEGER PRIMARY KEY /*!40101 AUTO_INCREMENT */ NOT NULL UNIQUE," // from https://stackoverflow.com/a/41028314
                    + "user VARCHAR(30) NOT NULL,"
                    + "asset INTEGER NOT NULL,"
                    + "quantity INTEGER NOT NULL,"
                    + "price INTEGER NOT NULL,"
                    + "datePlaced DATETIME NOT NULL,"
                    + "dateResolved DATETIME,"
                    + "boughtFrom INTEGER,"
                    + "CONSTRAINT fk_buy_user FOREIGN KEY (user) REFERENCES user (name)"
                    + "ON DELETE RESTRICT ON UPDATE CASCADE,"
                    + "CONSTRAINT fk_buy_asset FOREIGN KEY (asset) REFERENCES asset (idx)"
                    + "ON DELETE CASCADE ON UPDATE CASCADE,"
                    + "CONSTRAINT fk_buy_sell FOREIGN KEY (boughtFrom) REFERENCES sellorder (idx)"
                    + "ON DELETE RESTRICT ON UPDATE CASCADE" + ");";
/*
    static final String CREATE_TABLES = CREATE_TABLE_UNIT + CREATE_TABLE_ASSET + CREATE_TABLE_USER +
            CREATE_TABLE_INV + CREATE_TABLE_SELL + CREATE_TABLE_BUY;
*/
    static final String CLEAR_DATA = "DROP TABLE buyorder; DROP TABLE sellorder; DROP TABLE inventories; DROP TABLE asset; DROP TABLE user; DROP TABLE orgunit;";
    public JDBCDataSource() {
        connection = DBConnection.getInstance();
        try {
            Statement st = connection.createStatement();
            st.execute(CREATE_TABLE_UNIT);
            st.execute(CREATE_TABLE_ASSET);
            st.execute(CREATE_TABLE_USER);
            st.execute(CREATE_TABLE_INV);
            st.execute(CREATE_TABLE_SELL);
            st.execute(CREATE_TABLE_BUY);
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
