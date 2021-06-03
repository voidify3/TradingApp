package ServerSide;

import static common.ProtocolKeywords.*;
import static common.DatabaseTables.*;
import common.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.SQLException;

public class TestNetworkServer {
    private static NetworkServer server;
    ByteArrayOutputStream outputStream;
    @BeforeAll @Test //beforeAll not beforeEach because update and delete and select tests need some data
    // to exist, so it's simpler to let the insert tests be some of that data
    static void setup() throws SQLException, IOException {
        server = new NetworkServer();
        server.start();
    }
    @BeforeEach
    void setOutputStream() {
        outputStream = new ByteArrayOutputStream();
    }
    @Test
    public void successInsert() throws IOException {
        server.simulateRequest(ProtocolKeywords.INSERT,
                new DataPacket(UNIT, null, new OrgUnit("Devs"), false),
                new ObjectOutputStream(System.out));
        //byte[] b = outputStream.toByteArray();
        //should give 1
    }
    @Test
    public void failInsert() throws IOException {
        server.simulateRequest(ProtocolKeywords.INSERT,
                new DataPacket(UNIT, null, new OrgUnit("Devs"), false),
                new ObjectOutputStream(System.out));
        //byte[] b = outputStream.toByteArray();
        //should give 0
    }
    @Test
    public void oneRowInsertUOD() {
        //(no duplicate) assert returns 1
    }
    @Test
    public void twoRowsInsertUOD() {
        //(yes duplicate) assert returns 2
    }
    @Test
    public void successUpdate() {
        //assert returns 1
    }
    @Test
    public void failUpdate() {
        //assert returns 0
    }
    @Test
    public void successDelete() {
        //assert returns 1
    }
    @Test
    public void failDelete() {
        //assert returns 0
    }
    @Test
    public void successSelect() {
        //assert returns expected arraylist
    }
    @Test
    public void failSelect() {
        //assert returns empty arraylist
    }
    //TODO maybe make multiple of some above tests for different tables??
    @Test
    public void tradeRecNoMatches() {

        //no sellorder buyorder pairs with the same asset exist
        //reconcile and run a select query
        //assert that select queries for resolved sellorders and buyorders both return empty
    }
    @Test
    public void tradeRecTrivialCase() {
        //insert an order so there now exists a pair with equal qty and equal price
        //reconcile and run a select query
        //assert that the pair are the only resolved orders
    }
    @Test
    public void tradeRecPriceDifference() {
        //now there's 2 pairs equal qty: sellPrice=buyPrice+1 for asset1 and sellPrice=buyPrice-1 for asset2
        //reconcile and run a select query
        //assert that the former pair are both unresolved and the latter pair are both resolved
    }
    @Test
    public void tradeRecQtyDifference() {
        //now there's 2 pairs equal price: sellQty=buyQty+1 for asset1 and sellQty=buyQty-1 for asset2
        //reconcile and run a select query
        //assert that the former buy order is the only resolved one, and former sell order has new qty 1
    }
    @Test
    public void tradeRecMultiMatch() {
        //now asset1 has these sells from oldest to newest: 1) qty 10, 2) qty 15
        //and these buys from oldest to newest (price immaterial): 1) qty 3, 2) qty 8, 3) qty 7, 4) qty 8
        //reconcile and run a select query
        //assert: sell 2 & buy 4 remain unresolved, buys 1&3 bought from sell 1, buy 2 bought from sell 2
    }
    @Test
    public void tradeRecMixedCase() {
        //TODO: figure out table setup where all aspects of reconciliation logic are relevant
    }
    @AfterAll @Test
    private void done() throws SQLException, IOException {
        server.resetEverything();
        server.shutdown();
    }


}
