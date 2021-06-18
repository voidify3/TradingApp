package ServerSide;

import static common.ProtocolKeywords.*;
import static common.DatabaseTables.*;
import common.*;
import common.Exceptions.IllegalString;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.TreeMap;

//TODO: make airtight

public class TestNetworkServer {
    public static final String DATE_RESOLVED_IS_NOT_NULL = "dateResolved IS NOT NULL";
    public static final String TEST_USER = "testUser";
    public static final String TEST_USER_2 = "testUserTwo";
    public static final String TEST_ORG_1 = "Devs";
    public static final String TEST_ORG_2 = "Marketing";
    public static final String TEST_ASSET_2 = "test2";
    public static final String TEST_ASSET_3 = "test3";
    public static final String BLANK_FILTER = "1 = 1";
    public static final String DATE_RESOLVED_IS_NULL = "dateResolved IS NULL";
    private static NetworkServer server;
    ByteArrayOutputStream outputStream;
    @BeforeEach
    void setupAndSuccessInsert() {
        server = new NetworkServer();
        assertAll(
                ()->assertEquals(1, server.simulateNonselect(INSERT,
                        new DataPacket(UNIT, null, DataObjectFactory.newOrgUnitValidated(TEST_ORG_1), false))),
                ()-> assertEquals(1, server.simulateNonselect(INSERT,
                        new DataPacket(UNIT, null, DataObjectFactory.newOrgUnitValidated(TEST_ORG_2), false))),
                ()-> assertEquals(1, server.simulateNonselect(INSERT,
                        new DataPacket(USER, null,
                                new User(TEST_USER, "password", false, TEST_ORG_1),
                                false))),
                ()->assertEquals(1, server.simulateNonselect(INSERT,
                        new DataPacket(USER, null,
                                new User(TEST_USER_2, "password", false, TEST_ORG_2),
                                false))),
                ()->assertEquals(1, server.simulateNonselect(INSERT,
                        new DataPacket(ASSET, null, new Asset(0, "test"), false))),
                ()->assertEquals(1, server.simulateNonselect(INSERT,
                        new DataPacket(ASSET, null, new Asset(0, TEST_ASSET_2), false))),
                ()->assertEquals(1, server.simulateNonselect(INSERT,
                        new DataPacket(ASSET, null, new Asset(0, TEST_ASSET_3), false)))
        );
        //server.start();
    }
    @AfterEach
    void done() throws SQLException, IOException {
        server.resetEverything();
        //server.shutdown();
    }
    @Test
    void failInsert() throws SQLException, IllegalString {
        assertEquals(0, server.simulateNonselect(INSERT,
                new DataPacket(UNIT, null, DataObjectFactory.newOrgUnitValidated(TEST_ORG_1), false)));
        //should give 0
    }
    @Test
    void constrainedInsert() throws IllegalString, SQLException {
        //test that you can't insert a row with a FK value that points nowhere
        assertEquals(-1, server.simulateNonselect(INSERT,
                new DataPacket(USER, null,
                        new User("testUserThree", "password", false, "aaa"), false)));

    }
    @Test
    void oneRowInsertUOD() throws SQLException {
        assertEquals(1, server.simulateNonselect(INSERT,
                new DataPacket(INV, null,
                        new InventoryRecord(TEST_ORG_1, 1, 0), true)));
        //(no duplicate) assert returns 1
    }
    @Test
    void twoRowsInsertUOD() throws SQLException {
        assertEquals(1, server.simulateNonselect(INSERT,
                new DataPacket(INV, null,
                        new InventoryRecord(TEST_ORG_1, 1, 0), true)));
        assertEquals(2, server.simulateNonselect(INSERT,
                new DataPacket(INV, null,
                        new InventoryRecord(TEST_ORG_1, 1, 5), true)));
        //(yes duplicate) assert returns 2
    }
    @Test
    void constrainedInsertUOD() throws SQLException {
        //test that you can't insert an inventory record with keys that point nowhere
        assertEquals(-1, server.simulateNonselect(INSERT,
                new DataPacket(INV, null,
                        new InventoryRecord("aaa", 1, 0), true)));
        assertEquals(-1, server.simulateNonselect(INSERT,
                new DataPacket(INV, null,
                        new InventoryRecord(TEST_ORG_1, 25, 0), true)));
    }
    @Test
    void successUpdate() throws SQLException {
        assertEquals(1, server.simulateNonselect(UPDATE,
                new DataPacket(ASSET, null, new Asset(1, TEST_ASSET_2), null)));
        //assert returns 1
    }
    @Test
    void failUpdate() throws SQLException {
        assertEquals(0, server.simulateNonselect(UPDATE,
                new DataPacket(ASSET, null, new Asset(4, TEST_ASSET_3), null)));
    }
    @Test
    void constrainedUpdate() {
        //test that you can't update a FK to point nowhere
        //(user unit is the only fk that can be directly edited by a client request)
        assertAll(
                ()-> assertEquals(-1, server.simulateNonselect(UPDATE,
                        new DataPacket(USER, null, new User(TEST_USER, "password", false, "aaa"), null)))
                );
    }
    @Test
    void successDelete() throws SQLException {
        assertEquals(1, server.simulateNonselect(DELETE,
                new DataPacket(ASSET, ASSET.getColumns()[0] + "=1", null, null)));
    }
    @Test
    void failDelete() throws SQLException {
        assertEquals(0, server.simulateNonselect(DELETE,
                new DataPacket(ASSET, ASSET.getColumns()[0] + "=4", null, null)));
    }
    @Test
    void applyConstraintsUnit() throws SQLException {
        //Test to make sure the OrgUnit FK in User is ON DELETE SET NULL
        // and the ones in Inventories, BuyOrder and SellOrder are all ON DELETE CASCADE
        server.simulateNonselect(INSERT,
                new DataPacket(INV, null,
                        new InventoryRecord(TEST_ORG_1, 1, 0), true));
        server.simulateNonselect(INSERT,
                new DataPacket(BUY, null,
                        new BuyOrder(TEST_ORG_1, 1, 10, 5), false));
        server.simulateNonselect(INSERT,
                new DataPacket(SELL, null,
                        new SellOrder(TEST_ORG_1, 1, 10, 5), false));
        String filterpart = "='" + TEST_ORG_1 + "'";
        assertAll(()->assertEquals(1, server.simulateNonselect(DELETE, new DataPacket(UNIT,
                        UNIT.getColumns()[0] + filterpart, null, null))),
                ()-> assertEquals(0, server.simulateSelect(new DataPacket(UNIT, UNIT.getColumns()[0]
                        + filterpart, null, null)).size()),
                ()-> assertNull(((User) server.simulateSelect(new DataPacket(USER, UNIT.getColumns()[0]
                        + "='" + TEST_USER + "'", null, null)).get(0)).getUnit()),
                ()-> assertTrue(server.simulateSelect(new DataPacket(INV, INV.getColumns()[0] + filterpart,
                        null, null)).isEmpty()),
                ()-> assertEquals(0, server.simulateSelect(new DataPacket(SELL, SELL.getColumns()[1] + filterpart,
                        null, null)).size()),
                ()-> assertEquals(0, server.simulateSelect(new DataPacket(BUY, BUY.getColumns()[1] + filterpart,
                        null, null)).size())
        );
        //the delete will succeed, but the user's unit will be set null and the inventory record will be deleted
    }
    @Test
    void applyConstraintsAsset() throws SQLException {
        //Test to make sure the Asset FKs in Inventories, Buyorder and SellOrder are all ON DELETE CASCADE
        server.simulateNonselect(INSERT,
                new DataPacket(INV, null,
                        new InventoryRecord(TEST_ORG_1, 1, 0), true));
        server.simulateNonselect(INSERT,
                new DataPacket(BUY, null,
                        new BuyOrder(TEST_USER_2, 1, 10, 5), false));
        server.simulateNonselect(INSERT,
                new DataPacket(SELL, null,
                        new SellOrder(TEST_USER, 1, 10, 5), false));
        String filterpart = "= 1";
        assertAll(()->assertEquals(1, server.simulateNonselect(DELETE, new DataPacket(ASSET,
                        ASSET.getColumns()[0] + filterpart, null, null))),
                ()-> assertEquals(0, server.simulateSelect(new DataPacket(ASSET, ASSET.getColumns()[0]
                        + filterpart, null, null)).size()),
                ()-> assertTrue(server.simulateSelect(new DataPacket(INV, INV.getColumns()[0] + filterpart,
                        null, null)).isEmpty()),
                ()-> assertTrue(server.simulateSelect(new DataPacket(SELL, SELL.getColumns()[2] + filterpart,
                        null, null)).isEmpty()),
                ()-> assertTrue(server.simulateSelect(new DataPacket(BUY, BUY.getColumns()[2] + filterpart,
                        null, null)).isEmpty())
        );
    }

    @Test
    void applyConstraintSellOrder() throws SQLException {
        //test that BoughtFrom is ON DELETE CASCADE
        server.simulateNonselect(INSERT,
                new DataPacket(SELL, null,
                        new SellOrder(TEST_ORG_1, 1, 10, 5), false));
        server.simulateNonselect(INSERT,
                new DataPacket(BUY, null,
                        new BuyOrder(1, TEST_ORG_2, 1, 10, 5, LocalDateTime.now(), LocalDateTime.now(), 1),
                        false));
        assertAll(()-> assertEquals(1, server.simulateSelect(new DataPacket(BUY, BUY.getColumns()[7] +
                        "= 1", null, null)).size()),
                ()->assertEquals(1, server.simulateNonselect(DELETE, new DataPacket(SELL,
                        SELL.getColumns()[0] + "= 1", null, null))),
                ()-> assertEquals(0, server.simulateSelect(new DataPacket(SELL, SELL.getColumns()[0] +
                        "= 1", null, null)).size()),
                ()-> assertEquals(0, server.simulateSelect(new DataPacket(BUY, BUY.getColumns()[7] +
                        "= 1", null, null)).size())
        );
    }
    @Test
    void successSelectBlankFilter() throws SQLException {
        ArrayList<DataObject> results = server.simulateSelect(
                new DataPacket(UNIT, BLANK_FILTER, null, null)); //will be sortd on compareto
        assertAll(
                ()->assertNotEquals(null, results),
                ()->assertEquals(2, results.size()),
                ()->assertTrue(results.get(0) instanceof OrgUnit),
                ()->assertEquals(TEST_ORG_1, ((OrgUnit) results.get(0)).getName()),
                ()->assertEquals(TEST_ORG_2, ((OrgUnit) results.get(1)).getName())
        );
    }
    @Test
    void failSelectBlankFilter() throws SQLException {
        ArrayList<DataObject> results = server.simulateSelect(
                new DataPacket(INV, BLANK_FILTER, null, null));
        assertAll(
                ()->assertNotEquals(null, results),
                ()->assertTrue(results.isEmpty())
        );
    }
    //TODO more select tests

    final String NAME_EQUALS = "name = '%s'";
    final String INV_KEY_EQUALS = "orgunit = '%s' AND asset = %d";

    @Test
    void tradeRecNoMatches() throws SQLException {
        server.simulateNonselect(INSERT,
                new DataPacket(SELL, null,
                        new SellOrder(TEST_ORG_1, 2, 10, 5), false));
        server.simulateNonselect(INSERT,
                new DataPacket(BUY, null,
                        new BuyOrder(TEST_ORG_2, 3, 10, 5), false));
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
        // and that the balances are at 0
    }
    @Test
    void tradeRecTrivialCase() throws SQLException {
        server.simulateNonselect(INSERT,
                new DataPacket(SELL, null,
                        new SellOrder(TEST_ORG_1, 2, 10, 5), false));
        server.simulateNonselect(INSERT,
                new DataPacket(BUY, null,
                        new BuyOrder(TEST_ORG_2, 3, 10, 5), false));
        server.simulateNonselect(INSERT,
                new DataPacket(SELL, null,
                        new SellOrder(TEST_ORG_1, 3, 10, 5), false));
        //there now exists a pair with equal qty and equal price
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
                ()->assertEquals(TEST_ORG_1, ((SellOrder)resolvedSells.get(0)).getUnit()),
                ()->assertEquals(TEST_ORG_2, ((BuyOrder)resolvedBuys.get(0)).getUnit()),
                ()->assertEquals(((SellOrder) resolvedSells.get(0)).getId(), ((BuyOrder) resolvedBuys.get(0)).getBoughtFrom()),
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
    @Test
    void tradeRecPriceDifference() throws SQLException {
        server.simulateNonselect(INSERT,
                new DataPacket(SELL, null,
                        new SellOrder(TEST_ORG_1, 2, 10, 5), false));
        server.simulateNonselect(INSERT,
                new DataPacket(BUY, null,
                        new BuyOrder(TEST_ORG_2, 2, 10, 4), false));
        server.simulateNonselect(INSERT,
                new DataPacket(SELL, null,
                        new SellOrder(TEST_ORG_2, 3, 10, 6), false));
        server.simulateNonselect(INSERT,
                new DataPacket(BUY, null,
                        new BuyOrder(TEST_ORG_1, 3, 10, 7), false));
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
                ()->assertEquals(TEST_ORG_2, ((SellOrder)resolvedSells.get(0)).getUnit()),
                ()->assertEquals(TEST_ORG_1, ((BuyOrder)resolvedBuys.get(0)).getUnit()),
                ()->assertEquals(((SellOrder) resolvedSells.get(0)).getId(), ((BuyOrder) resolvedBuys.get(0)).getBoughtFrom()),
                ()->assertEquals(((SellOrder) resolvedSells.get(0)).getDateResolved(),
                        ((BuyOrder) resolvedBuys.get(0)).getDateResolved()),
                ()->assertEquals(10, ((OrgUnit) server.simulateSelect(new DataPacket(UNIT,
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
    @Test
    void tradeRecQtyDifference() throws SQLException {
        server.simulateNonselect(INSERT,
                new DataPacket(SELL, null,
                        new SellOrder(TEST_ORG_1, 2, 11, 4), false));
        server.simulateNonselect(INSERT,
                new DataPacket(BUY, null,
                        new BuyOrder(TEST_ORG_2, 2, 10, 4), false));
        server.simulateNonselect(INSERT,
                new DataPacket(SELL, null,
                        new SellOrder(TEST_ORG_2, 3, 15, 5), false));
        server.simulateNonselect(INSERT,
                new DataPacket(BUY, null,
                        new BuyOrder(TEST_ORG_1, 3, 16, 5), false));
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
                ()->assertEquals(TEST_ORG_2, ((BuyOrder)resolvedBuys.get(0)).getUnit()),
                ()->assertEquals(1, ((SellOrder) sellsFor2.get(0)).getQty()),
                ()->assertEquals(((SellOrder) sellsFor2.get(0)).getId(), ((BuyOrder) resolvedBuys.get(0)).getBoughtFrom()),
                ()-> assertNull(((SellOrder) sellsFor2.get(0)).getDateResolved()),
                ()->assertEquals(40, ((OrgUnit) server.simulateSelect(new DataPacket(UNIT,
                        String.format(NAME_EQUALS, TEST_ORG_1), null, null)).get(0)).getCredits()),
                ()->assertEquals(10, ((InventoryRecord)server.simulateSelect(
                        new DataPacket(INV, String.format(INV_KEY_EQUALS, TEST_ORG_2, 2),
                                null, null)).get(0)).getQuantity())
        );
        //assert that the former buy order is the only resolved one, and former sell order has new qty 1
        //and that the transaction went through
    }
    @Test
    void tradeRecMultiMatch() throws SQLException {
        server.simulateNonselect(INSERT, new DataPacket(SELL, null,
                new SellOrder(TEST_ORG_1, 1, 10, 5), false));
        server.simulateNonselect(INSERT, new DataPacket(SELL, null,
                new SellOrder(TEST_ORG_1, 1, 15, 5), false));
        server.simulateNonselect(INSERT, new DataPacket(BUY, null,
                new BuyOrder(TEST_ORG_2, 1, 3, 5), false));
        server.simulateNonselect(INSERT, new DataPacket(BUY, null,
                new BuyOrder(TEST_ORG_2, 1, 8, 5), false));
        server.simulateNonselect(INSERT, new DataPacket(BUY, null,
                new BuyOrder(TEST_ORG_2, 1, 7, 5), false));
        server.simulateNonselect(INSERT, new DataPacket(BUY, null,
                new BuyOrder(TEST_ORG_2, 1, 8, 5), false));

        //now asset 1 has these sells from oldest to newest: 1) qty 10, 2) qty 15
        //and these buys from oldest to newest (price immaterial): 1) qty 3, 2) qty 8, 3) qty 7, 4) qty 8
        server.reconcileTrades();

        ArrayList<DataObject> resolvedSells = server.simulateSelect(
                new DataPacket(SELL, DATE_RESOLVED_IS_NOT_NULL, null, null));
        ArrayList<DataObject> resolvedBuys = server.simulateSelect(
                new DataPacket(BUY, DATE_RESOLVED_IS_NOT_NULL, null, null));
        ArrayList<DataObject> unresolvedBuys = server.simulateSelect(
                new DataPacket(BUY, DATE_RESOLVED_IS_NULL, null, null));
        ArrayList<DataObject> unresolvedSells = server.simulateSelect(
                new DataPacket(SELL, DATE_RESOLVED_IS_NULL, null, null));
        assertAll(
                //assert the right numbers of orders are returned
                ()->assertEquals(3, resolvedBuys.size()),
                ()->assertEquals(1, unresolvedBuys.size()),
                ()->assertEquals(1, resolvedSells.size()),
                ()->assertEquals(1, unresolvedSells.size()),
                ()->assertTrue(resolvedBuys.get(0) instanceof BuyOrder),
                ()->assertTrue(unresolvedBuys.get(0) instanceof BuyOrder),
                ()->assertTrue(resolvedSells.get(0) instanceof SellOrder),
                ()->assertTrue(unresolvedSells.get(0) instanceof SellOrder));
        TreeMap<Integer, Integer> boughtfroms = new TreeMap<>();
        for (DataObject b : resolvedBuys) {
            boughtfroms.put(((BuyOrder)b).getId(), ((BuyOrder)b).getBoughtFrom());
        }
        assertAll(
                //assert the RIGHT orders are returned and the resolution happened correctly
                ()->assertEquals(1, ((SellOrder)resolvedSells.get(0)).getId()),
                ()->assertEquals(2, ((SellOrder)unresolvedSells.get(0)).getId()),
                ()->assertEquals(7, ((SellOrder)unresolvedSells.get(0)).getQty()),
                ()->assertEquals(4, ((BuyOrder)unresolvedBuys.get(0)).getId()),
                ()->assertEquals(1, boughtfroms.get(1)),
                ()->assertEquals(2, boughtfroms.get(2)),
                ()->assertEquals(1, boughtfroms.get(3))
        );
        assertAll(
                //assert that user 2's unit now has 18 of asset 1 and user 1's unit now has 90 credits
                ()->assertEquals(18, ((InventoryRecord)server.simulateSelect(
                        new DataPacket(INV, String.format(INV_KEY_EQUALS, TEST_ORG_2, 1),
                                null, null)).get(0)).getQuantity()),
                ()->assertEquals(90, ((OrgUnit) server.simulateSelect(new DataPacket(UNIT,
                        String.format(NAME_EQUALS, TEST_ORG_1), null, null)).get(0)).getCredits())
        );
        //assert: sell 2 & buy 4 remain unresolved, buys 1&3 bought from sell 1, buy 2 bought from sell 2
    }



}
