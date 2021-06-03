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
import java.sql.SQLException;
import java.util.ArrayList;

public class TestNetworkServer {
    public static final String DATE_RESOLVED_IS_NOT_NULL = "dateResolved IS NOT NULL";
    public static final String TEST_USER = "testUser";
    public static final String TEST_USER_2 = "testUser2";
    public static final String TEST_ORG_1 = "Devs";
    public static final String TEST_ORG_2 = "Marketing";
    public static final String TEST_ASSET_2 = "test2";
    public static final String TEST_ASSET_3 = "test3";
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
                    new DataPacket(UNIT, null, new OrgUnit(TEST_ORG_1), false))),
                ()-> assertEquals(1, server.simulateNonselect(INSERT,
                    new DataPacket(UNIT, null, new OrgUnit(TEST_ORG_2), false))),
                ()-> assertEquals(1, server.simulateNonselect(INSERT,
                new DataPacket(USER, null,
                        new User(TEST_USER, "password", false, TEST_ORG_1),
                        false))),
                ()->assertEquals(1, server.simulateNonselect(INSERT,
                new DataPacket(USER, null,
                        new User(TEST_USER_2, "password", false, TEST_ORG_2),
                        false))),
                ()->assertEquals(1, server.simulateNonselect(INSERT,
                new DataPacket(ASSET, null, new Asset("test"), false))),
                ()->assertEquals(1, server.simulateNonselect(INSERT,
                new DataPacket(ASSET, null, new Asset(TEST_ASSET_2), false))),
                ()->assertEquals(1, server.simulateNonselect(INSERT,
                new DataPacket(ASSET, null, new Asset(TEST_ASSET_3), false))));
        //should give 1
    }
    @Test @Order(2)
    public void failInsert() throws IOException {
//        server.simulateRequest(ProtocolKeywords.INSERT,
//                new DataPacket(UNIT, null, new OrgUnit("Devs"), false),
//                new ObjectOutputStream(System.out));
        //byte[] b = outputStream.toByteArray();
        assertEquals(0, server.simulateNonselect(INSERT,
                new DataPacket(UNIT, null, new OrgUnit(TEST_ORG_1), false)));
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
                        new User("testUser3", "password", false, "aaa"), false)));
        //should give 0
    }
    @Test @Order(3)
    public void oneRowInsertUOD() {
        assertEquals(1, server.simulateNonselect(INSERT,
                new DataPacket(INV, null,
                        new InventoryRecord(TEST_ORG_1, 1, 0), true)));
        //(no duplicate) assert returns 1
    }
    @Test @Order(4)
    public void twoRowsInsertUOD() {
        assertEquals(2, server.simulateNonselect(INSERT,
                new DataPacket(INV, null,
                        new InventoryRecord(TEST_ORG_1, 1, 5), true)));
        //(yes duplicate) assert returns 2
    }
    @Test @Order(5)
    public void successUpdate() {
        assertEquals(1, server.simulateNonselect(UPDATE,
                new DataPacket(ASSET, null, new Asset(1, TEST_ASSET_2), null)));
        //assert returns 1
    }
    @Test @Order(6)
    public void failUpdate() {
        assertEquals(0, server.simulateNonselect(UPDATE,
                new DataPacket(ASSET, null, new Asset(3, TEST_ASSET_3), null)));
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
                new DataPacket(UNIT, "", null, null)); //will be sortd on compareto
        assertAll(
                ()->assertNotEquals(null, results),
                ()->assertEquals(1, results.size()),
                ()->assertTrue(results.get(0) instanceof OrgUnit),
                ()->assertEquals(TEST_ORG_1, ((OrgUnit) results.get(0)).getName()),
                ()->assertEquals(TEST_ORG_2, ((OrgUnit) results.get(1)).getName())
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

    final int lastSelect = 11;
    final String NAME_EQUALS = "name = '%s'";
    final String INV_KEY_EQUALS = "orgunit = '%s' AND asset = %d";

    @Test @Order(lastSelect +1)
    public void tradeRecNoMatches() {
        server.simulateNonselect(INSERT,
                new DataPacket(SELL, null,
                        new SellOrder(TEST_USER, 2, 10, 5), false));
        server.simulateNonselect(INSERT,
                new DataPacket(BUY, null,
                        new BuyOrder(TEST_USER_2, 3, 10, 5), false));
        //no sellorder buyorder pairs with the same asset exist
        server.reconcileTrades();
        //reconcile and run a select query
        ArrayList resolvedSells = server.simulateSelect(
                new DataPacket(SELL, DATE_RESOLVED_IS_NOT_NULL, null, null));
        ArrayList resolvedBuys = server.simulateSelect(
                new DataPacket(BUY, DATE_RESOLVED_IS_NOT_NULL, null, null));
        assertAll(
                ()->assertTrue(resolvedSells.isEmpty()),
                ()->assertTrue(resolvedBuys.isEmpty()),
                ()->assertEquals(0, ((OrgUnit) server.simulateSelect(new DataPacket(UNIT,
                        String.format(NAME_EQUALS, TEST_ORG_1), null, null)).get(0)).getCredits()),
                ()-> {
                    ArrayList<DataObject> dataObjects = server.simulateSelect(new DataPacket(INV, String.format(INV_KEY_EQUALS, TEST_ORG_2,
                            3), null, null));
                    assertTrue(dataObjects.isEmpty() || ((InventoryRecord)dataObjects.get(0)).getQuantity()==0);
                }
        );
        //assert that select queries for resolved sellorders and buyorders both return empty,
        // and that the balances are unchanged
    }
    @Test @Order(lastSelect+2)
    public void tradeRecTrivialCase() {
//        int oldCredits = 0; /* ((OrgUnit) server.simulateSelect(new DataPacket(UNIT,
//                String.format(NAME_EQUALS, TEST_ORG_1), null, null)).get(0)).getCredits();
//                */
//        int oldStock = 0;
        server.simulateNonselect(INSERT,
                new DataPacket(SELL, null,
                        new SellOrder(TEST_USER, 3, 10, 5), false));
        //insert an order so there now exists a pair with equal qty and equal price
        server.reconcileTrades();
        //reconcile and run a select query
        ArrayList resolvedSells = server.simulateSelect(
                new DataPacket(SELL, DATE_RESOLVED_IS_NOT_NULL, null, null));
        ArrayList resolvedBuys = server.simulateSelect(
                new DataPacket(BUY, DATE_RESOLVED_IS_NOT_NULL, null, null));
        assertAll(
                ()->assertEquals(1, resolvedSells.size()),
                ()->assertEquals(1, resolvedBuys.size()),
                ()->assertTrue(resolvedBuys.get(0) instanceof BuyOrder),
                ()->assertTrue(resolvedSells.get(0) instanceof SellOrder),
                ()->assertEquals(TEST_USER, ((SellOrder)resolvedSells.get(0)).getUser()),
                ()->assertEquals(TEST_USER_2, ((BuyOrder)resolvedBuys.get(0)).getUser()),
                ()->assertEquals(((SellOrder) resolvedSells.get(0)).getId(), ((BuyOrder)resolvedBuys.get(0)).boughtFrom),
                ()->assertEquals(((SellOrder) resolvedSells.get(0)).getDateResolved(),
                        ((BuyOrder) resolvedBuys.get(0)).getDateResolved()),
                ()->assertEquals(50, ((OrgUnit) server.simulateSelect(new DataPacket(UNIT,
                        String.format(NAME_EQUALS, TEST_ORG_1), null, null)).get(0)).getCredits()),
                ()->assertEquals(10, ((InventoryRecord)server.simulateSelect(
                        new DataPacket(INV, String.format(INV_KEY_EQUALS, TEST_ORG_2, 3),
                                null, null)).get(0)).getQuantity())
        );
        //assert that the pair are the only resolved orders and they resolved to each other
    }
    @Test @Order(lastSelect+3)
    public void tradeRecPriceDifference() {
        //delete the resolved orders
        server.simulateNonselect(DELETE, new DataPacket(BUY, DATE_RESOLVED_IS_NOT_NULL, null, null));
        server.simulateNonselect(DELETE, new DataPacket(SELL, DATE_RESOLVED_IS_NOT_NULL, null, null));

        server.simulateNonselect(INSERT,
                new DataPacket(BUY, null,
                        new BuyOrder(TEST_USER_2, 2, 10, 4), false));
        server.simulateNonselect(INSERT,
                new DataPacket(SELL, null,
                        new SellOrder(TEST_USER_2, 3, 10, 6), false));
        server.simulateNonselect(INSERT,
                new DataPacket(BUY, null,
                        new BuyOrder(TEST_USER, 3, 10, 7), false));
        //now there's 2 pairs equal qty: sellPrice=buyPrice+1 for asset 2 and sellPrice=buyPrice-1 for asset 3
        server.reconcileTrades();
        //get unresolved orders
        ArrayList resolvedSells = server.simulateSelect(
                new DataPacket(SELL, DATE_RESOLVED_IS_NOT_NULL, null, null));
        ArrayList resolvedBuys = server.simulateSelect(
                new DataPacket(BUY, DATE_RESOLVED_IS_NOT_NULL, null, null));
        assertAll(
                ()->assertEquals(1, resolvedSells.size()),
                ()->assertEquals(1, resolvedBuys.size()),
                ()->assertTrue(resolvedBuys.get(0) instanceof BuyOrder),
                ()->assertTrue(resolvedSells.get(0) instanceof SellOrder),
                ()->assertEquals(TEST_USER_2, ((SellOrder)resolvedSells.get(0)).getUser()),
                ()->assertEquals(TEST_USER, ((BuyOrder)resolvedBuys.get(0)).getUser()),
                ()->assertEquals(((SellOrder) resolvedSells.get(0)).getId(), ((BuyOrder)resolvedBuys.get(0)).boughtFrom),
                ()->assertEquals(((SellOrder) resolvedSells.get(0)).getDateResolved(),
                        ((BuyOrder) resolvedBuys.get(0)).getDateResolved()),
                ()->assertEquals(60, ((OrgUnit) server.simulateSelect(new DataPacket(UNIT,
                        String.format(NAME_EQUALS, TEST_ORG_1), null, null)).get(0)).getCredits()),
                ()->assertEquals(60, ((OrgUnit) server.simulateSelect(new DataPacket(UNIT,
                        String.format(NAME_EQUALS, TEST_ORG_2), null, null)).get(0)).getCredits()),
                ()->assertEquals(10, ((InventoryRecord)server.simulateSelect(
                        new DataPacket(INV, String.format(INV_KEY_EQUALS, TEST_ORG_1, 3),
                                null, null)).get(0)).getQuantity())
        );
        //assert that the former pair are both unresolved and the latter pair are both resolved
        //and that the transaction went through including the refund of the difference to the buyer
    }
    @Test @Order(lastSelect+4)
    public void tradeRecQtyDifference() {
        //delete the resolved orders
        server.simulateNonselect(DELETE, new DataPacket(BUY, DATE_RESOLVED_IS_NOT_NULL, null, null));
        server.simulateNonselect(DELETE, new DataPacket(SELL, DATE_RESOLVED_IS_NOT_NULL, null, null));
        //update the too-expensive sell to now be a higher ty and same price
        SellOrder s = (SellOrder)(server.simulateSelect(
                new DataPacket(SELL, "", null, null)).get(0));
        s.setQty(11);
        s.setPrice(4);
        server.simulateNonselect(UPDATE, new DataPacket(SELL, null, s, null));
        server.simulateNonselect(INSERT,
                new DataPacket(SELL, null,
                        new SellOrder(TEST_USER_2, 3, 15, 5), false));
        server.simulateNonselect(INSERT,
                new DataPacket(BUY, null,
                        new BuyOrder(TEST_USER, 3, 16, 5), false));
        //now there's 2 pairs equal price: sellQty=buyQty+1 for asset 2 and sellQty=buyQty-1 for asset 3
        server.reconcileTrades();
        //get unresolved orders
        ArrayList resolvedSells = server.simulateSelect(
                new DataPacket(SELL, DATE_RESOLVED_IS_NOT_NULL, null, null));
        ArrayList resolvedBuys = server.simulateSelect(
                new DataPacket(BUY, DATE_RESOLVED_IS_NOT_NULL, null, null));
        ArrayList sellsFor2 = server.simulateSelect(new DataPacket(SELL, "asset = 2", null, null));
        assertAll(
                ()->assertTrue(resolvedSells.isEmpty()),
                ()->assertEquals(1, resolvedBuys.size()),
                ()->assertTrue(resolvedBuys.get(0) instanceof BuyOrder),
                ()->assertEquals(TEST_USER_2, ((BuyOrder)resolvedBuys.get(0)).getUser()),
                ()->assertEquals(1, ((SellOrder) sellsFor2.get(0)).getQty()),
                ()->assertEquals(((SellOrder) sellsFor2.get(0)).getId(), ((BuyOrder)resolvedBuys.get(0)).boughtFrom),
                ()-> assertNull(((SellOrder) sellsFor2.get(0)).getDateResolved()),
                ()->assertEquals(100, ((OrgUnit) server.simulateSelect(new DataPacket(UNIT,
                        String.format(NAME_EQUALS, TEST_ORG_1), null, null)).get(0)).getCredits()),
                ()->assertEquals(20, ((InventoryRecord)server.simulateSelect(
                        new DataPacket(INV, String.format(INV_KEY_EQUALS, TEST_ORG_2, 2),
                                null, null)).get(0)).getQuantity())
        );
        //assert that the former buy order is the only resolved one, and former sell order has new qty 1
        //and that the transaction went through
    }
    @Test @Order(lastSelect+5)
    public void tradeRecMultiMatch() {
        server.simulateNonselect(DELETE, new DataPacket(BUY, DATE_RESOLVED_IS_NOT_NULL, null, null));
        server.simulateNonselect(DELETE, new DataPacket(SELL, DATE_RESOLVED_IS_NOT_NULL, null, null));
        //edit the sell order that was left with qty 1 to fit the bill of sell 1
        ArrayList<DataObject> sells = server.simulateSelect(
                new DataPacket(SELL, "asset = 2", null, null));
        SellOrder s = (SellOrder)(sells.get(0));
        s.setAsset(3);
        s.setQty(10);
        server.simulateNonselect(UPDATE, new DataPacket(SELL, null, s, null));
        BuyOrder b = (BuyOrder) server.simulateSelect(
                new DataPacket(BUY, "", null, null)).get(0);
        b.setQty(3);
        server.simulateNonselect(UPDATE, new DataPacket(BUY, null, b, null));
        server.simulateNonselect(UPDATE, new DataPacket(BUY, null, b, null));

        //now asset 3 has these sells from oldest to newest: 1) qty 10, 2) qty 15
        //and these buys from oldest to newest (price immaterial): 1) qty 3, 2) qty 8, 3) qty 7, 4) qty 8


        ArrayList resolvedSells = server.simulateSelect(
                new DataPacket(SELL, DATE_RESOLVED_IS_NOT_NULL, null, null));
        ArrayList resolvedBuys = server.simulateSelect(
                new DataPacket(BUY, DATE_RESOLVED_IS_NOT_NULL, null, null));
        ArrayList unresolvedBuys = server.simulateSelect(
                new DataPacket(BUY, "dateResolved IS NULL", null, null));
        assertAll(
                ()->assertEquals(3, resolvedBuys.size()),
                ()->assertEquals(1, unresolvedBuys.size()),
                ()->assertEquals(1, resolvedSells.size()),
                ()->assertTrue(resolvedBuys.get(0) instanceof BuyOrder),
                ()->assertTrue(resolvedSells.get(0) instanceof SellOrder),
                ()->assertEquals(TEST_USER, ((SellOrder)resolvedSells.get(0)).getUser()),
                ()->assertEquals(8, ((BuyOrder)unresolvedBuys.get(0)).getQty())
                //assert two of the resolved buys were resolved to the resolved sell
                //assert transactions processed correctly
        );

        //reconcile and run a select query
        //assert: sell 2 & buy 4 remain unresolved, buys 1&3 bought from sell 1, buy 2 bought from sell 2
    }
    @AfterAll @Test
    static private void done() throws SQLException, IOException {
        server.resetEverything();
        server.shutdown();
    }


}
