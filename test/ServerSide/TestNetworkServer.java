package ServerSide;

import static common.ProtocolKeywords.*;
import static common.DatabaseTables.*;
import common.*;
import common.Exceptions.IllegalString;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Order;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.ArrayList;

public class TestNetworkServer {
    private static NetworkServer server;
    ByteArrayOutputStream outputStream;
    @BeforeAll @Test //beforeAll not beforeEach because update and delete and select tests need some data
    // to exist, so it's simpler to let the insert tests be some of that data
    static void setup() throws SQLException, IOException {
        server = new NetworkServer();
        //server.start();
    }
//    @BeforeEach
//    void setOutputStream() {
//        outputStream = new ByteArrayOutputStream();
//    }
    @Test @Order(1)
    public void successInsert() throws IOException, IllegalString {
//        server.simulateRequest(ProtocolKeywords.INSERT,
//                new DataPacket(UNIT, null, new OrgUnit("Devs"), false),
//                new ObjectOutputStream(System.out));
        //byte[] b = outputStream.toByteArray();
        assertAll(
                ()->assertEquals(1, server.simulateNonselect(INSERT,
                    new DataPacket(UNIT, null, new OrgUnit("Devs"), false))),
                ()-> assertEquals(1, server.simulateNonselect(INSERT,
                    new DataPacket(UNIT, null, new OrgUnit("Marketing"), false))),
                ()-> assertEquals(1, server.simulateNonselect(INSERT,
                new DataPacket(USER, null,
                        new User("testUser", "password", false, "Devs"),
                        false))),
                ()->assertEquals(1, server.simulateNonselect(INSERT,
                new DataPacket(USER, null,
                        new User("testUser2", "password", false, "Marketing"),
                        false))),
                ()->assertEquals(1, server.simulateNonselect(INSERT,
                new DataPacket(ASSET, null, new Asset(0, "test"), false))),
                ()->assertEquals(1, server.simulateNonselect(INSERT,
                new DataPacket(ASSET, null, new Asset(0, "test2"), false))),
                ()->assertEquals(1, server.simulateNonselect(INSERT,
                new DataPacket(ASSET, null, new Asset(0, "test3"), false))));
        //should give 1
    }
    @Test @Order(2)
    public void failInsert() throws IOException {
//        server.simulateRequest(ProtocolKeywords.INSERT,
//                new DataPacket(UNIT, null, new OrgUnit("Devs"), false),
//                new ObjectOutputStream(System.out));
        //byte[] b = outputStream.toByteArray();
        assertEquals(0, server.simulateNonselect(INSERT,
                new DataPacket(UNIT, null, new OrgUnit("Devs"), false)));
        //should give 0
    }
    @Test @Order(2)
    public void constrainedInsert() throws IOException, IllegalString {
//        server.simulateRequest(ProtocolKeywords.INSERT,
//                new DataPacket(UNIT, null, new OrgUnit("Devs"), false),
//                new ObjectOutputStream(System.out));
        //byte[] b = outputStream.toByteArray();
        assertEquals(-1, server.simulateNonselect(INSERT,
                new DataPacket(USER, null,
                        new User("testUser2", "password", false, "aaa"), false)));
        //should give 0
    }
    @Test @Order(3)
    public void oneRowInsertUOD() {
        assertEquals(1, server.simulateNonselect(INSERT,
                new DataPacket(INV, null,
                        new InventoryRecord("Devs", 1, 0), true)));
        //(no duplicate) assert returns 1
    }
    @Test @Order(4)
    public void twoRowsInsertUOD() {
        assertEquals(2, server.simulateNonselect(INSERT,
                new DataPacket(INV, null,
                        new InventoryRecord("Devs", 1, 5), true)));
        //(yes duplicate) assert returns 2
    }
    @Test @Order(5)
    public void successUpdate() {
        assertEquals(1, server.simulateNonselect(UPDATE,
                new DataPacket(ASSET, null, new Asset(1, "test2"), null)));
        //assert returns 1
    }
    @Test @Order(6)
    public void failUpdate() {
        assertEquals(0, server.simulateNonselect(UPDATE,
                new DataPacket(ASSET, null, new Asset(3, "test3"), null)));
    }
    @Test @Order(7)
    public void successDelete() {
        assertEquals(1, server.simulateNonselect(DELETE,
                new DataPacket(ASSET, ASSET.getColumnNames()[0] + "=1", null, null)));
    }
    @Test @Order(8)
    public void failDelete() {
        assertEquals(0, server.simulateNonselect(DELETE,
                new DataPacket(ASSET, ASSET.getColumnNames()[0] + "=1", null, null)));
    }
    @Test @Order(9)
    public void constrainedDelete() {
        assertEquals(-1, server.simulateNonselect(DELETE,
                new DataPacket(UNIT, UNIT.getColumnNames()[0] + "=1", null, null)));
        //since test user exists, delete will fail for constraint reasons
    }
    @Test @Order(10)
    public void successSelectBlankFilter() {
        ArrayList<DataObject> results = server.simulateSelect(
                new DataPacket(UNIT, "", null, null));
        assertAll(
                ()->assertNotEquals(null, results),
                ()->assertEquals(1, results.size()),
                ()->assertTrue(results.get(0) instanceof OrgUnit),
                ()->assertEquals("Devs", ((OrgUnit) results.get(0)).getName()),
                ()->assertEquals("Marketing", ((OrgUnit) results.get(1)).getName())
        );
    }
    @Test @Order(11)
    public void failSelectBlankFilter() {
        ArrayList<DataObject> results = server.simulateSelect(
                new DataPacket(INV, "", null, null));
        assertAll(
                ()->assertNotEquals(null, results),
                ()->assertTrue(results.isEmpty())
        );
    }
    //TODO more select tests
    @Test
    public void tradeRecNoMatches() {
        server.simulateNonselect(INSERT,
                new DataPacket(SELL, null,
                        new SellOrder("testUser", 2, 10, 5), false));
        server.simulateNonselect(INSERT,
                new DataPacket(BUY, null,
                        new BuyOrder("testUser2", 3, 10, 5), false));
        //no sellorder buyorder pairs with the same asset exist
        server.reconcileTrades();
        //reconcile and run a select query
        ArrayList resolvedSells = server.simulateSelect(
                new DataPacket(SELL, "dateResolved IS NOT NULL", null, null));
        ArrayList resolvedBuys = server.simulateSelect(
                new DataPacket(BUY, "dateResolved IS NOT NULL", null, null));
        assertAll(
                ()->assertTrue(resolvedSells.isEmpty()),
                ()->assertTrue(resolvedBuys.isEmpty())
        );
        //assert that select queries for resolved sellorders and buyorders both return empty
    }
    @Test
    public void tradeRecTrivialCase() {
        server.simulateNonselect(INSERT,
                new DataPacket(SELL, null,
                        new SellOrder("testUser", 3, 10, 5), false));
        //insert an order so there now exists a pair with equal qty and equal price
        server.reconcileTrades();
        //reconcile and run a select query
        ArrayList resolvedSells = server.simulateSelect(
                new DataPacket(SELL, "dateResolved IS NOT NULL", null, null));
        ArrayList resolvedBuys = server.simulateSelect(
                new DataPacket(BUY, "dateResolved IS NOT NULL", null, null));
        assertAll(
                ()->assertEquals(1, resolvedSells.size()),
                ()->assertEquals(1, resolvedBuys.size()),
                ()->assertTrue(resolvedBuys.get(0) instanceof BuyOrder),
                ()->assertTrue(resolvedSells.get(0) instanceof SellOrder),
                ()->assertEquals("testUser", ((SellOrder)resolvedSells.get(0)).getUsername()),
                ()->assertEquals("testUser2", ((BuyOrder)resolvedBuys.get(0)).getUsername()),
                ()->assertEquals(((SellOrder)resolvedSells.get(0)).id, ((BuyOrder)resolvedBuys.get(0)).boughtFrom),
                ()->assertEquals(((SellOrder)resolvedSells.get(0)).dateResolved,
                        ((BuyOrder)resolvedBuys.get(0)).dateResolved)
        );
        //assert that the pair are the only resolved orders and they resolved to each other
    }
    @Test
    public void tradeRecPriceDifference() {
        //delete the resolved orders
        server.simulateNonselect(DELETE, new DataPacket(BUY, "dateResolved IS NOT NULL", null, null))
        server.simulateNonselect(DELETE, new DataPacket(SELL, "dateResolved IS NOT NULL", null, null))
        server.simulateNonselect(INSERT,
                new DataPacket(BUY, null,
                        new BuyOrder("testUser2", 2, 10, 4), false));
        server.simulateNonselect(INSERT,
                new DataPacket(BUY, null,
                        new SellOrder("testUser", 3, 10, 5), false));
        server.simulateNonselect(INSERT,
                new DataPacket(SELL, null,
                        new SellOrder("testUser2", 3, 10, 4), false));
        //now there's 2 pairs equal qty: sellPrice=buyPrice+1 for asset1 and sellPrice=buyPrice-1 for asset2
        server.reconcileTrades();
        //reconcile and run a select query
        ArrayList resolvedSells = server.simulateSelect(
                new DataPacket(SELL, "dateResolved IS NOT NULL", null, null));
        ArrayList resolvedBuys = server.simulateSelect(
                new DataPacket(BUY, "dateResolved IS NOT NULL", null, null));
        assertAll(
                ()->assertEquals(1, resolvedSells.size()),
                ()->assertEquals(1, resolvedBuys.size()),
                ()->assertTrue(resolvedBuys.get(0) instanceof BuyOrder),
                ()->assertTrue(resolvedSells.get(0) instanceof SellOrder),
                ()->assertEquals("testUser", ((SellOrder)resolvedSells.get(0)).getUsername()),
                ()->assertEquals("testUser2", ((BuyOrder)resolvedBuys.get(0)).getUsername()),
                ()->assertEquals(((SellOrder)resolvedSells.get(0)).id, ((BuyOrder)resolvedBuys.get(0)).boughtFrom),
                ()->assertEquals(((SellOrder)resolvedSells.get(0)).dateResolved,
                        ((BuyOrder)resolvedBuys.get(0)).dateResolved)
        );
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
    static private void done() throws SQLException, IOException {
        server.resetEverything();
        server.shutdown();
    }


}
