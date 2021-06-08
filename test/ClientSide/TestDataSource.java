package ClientSide;

import common.*;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

//INSTRUCTIONS TO RUN: run ServerGui then run this
public class TestDataSource {
    public static final LocalDateTime START_OF_YEAR = LocalDateTime.of(2021, 1, 5, 0, 0);
    static TradingAppDataSource n;
    public static final String TEST_USER = "testUser";
    public static final String TEST_USER_2 = "testUserTwo";
    public static final String TEST_ORG_1 = "Devs";
    public static final String TEST_ORG_2 = "Marketing";
    public static final String TEST_ASSET = "test";
    public static final String TEST_ASSET_2 = "test2";
    @BeforeAll @Test
    static void setupAndSuccessInserts() {
        n = new MockDataSource();

//        long ping = n.ping();
//        System.out.println(ping);
//        assertTrue(ping > 0);
        n.debugDeleteEverything();
        n.recreate();
        assertAll(
                ()->assertEquals(1, n.insertUnit(new OrgUnit(TEST_ORG_1))),
                ()->assertFalse(n.allOrgUnits().isEmpty()),
                ()->assertEquals(1, n.insertUser(new User(TEST_USER,
                        "password", false, TEST_ORG_1))),
                ()->assertEquals(1, n.insertAsset(new Asset(TEST_ASSET))),
                ()->assertEquals(1, n.insertSellOrder(new SellOrder(TEST_USER, 1, 10, 5))),
                ()->assertEquals(1, n.insertBuyOrder(new BuyOrder(TEST_USER, 1, 10, 5)))
        );
    }
    @AfterAll
    static void done() {
        n.debugDeleteEverything();
    }

    @Test
    void failInserts() {
        assertAll(
                ()->assertEquals(0, n.insertUnit(new OrgUnit(TEST_ORG_1))),
                ()->assertEquals(0, n.insertUser(new User(TEST_USER,
                        "password", false, TEST_ORG_1))),
                ()->assertEquals(0, n.insertAsset(new Asset(TEST_ASSET))),
                ()->assertEquals(0, n.insertSellOrder(new SellOrder(TEST_USER, 1, 10, 5))),
                ()->assertEquals(0, n.insertBuyOrder(new BuyOrder(TEST_USER, 1, 10, 5)))
        );
    }
    @Test
    void constrainedInserts() {
        assertAll(
                ()->assertEquals(-1, n.insertUser(new User(TEST_USER,
                        "password", false, "aaa"))),
                ()->assertEquals(-1, n.insertSellOrder(new SellOrder(TEST_USER, 2, 10, 5))),
                ()->assertEquals(-1, n.insertBuyOrder(new BuyOrder(TEST_USER, 2, 10, 5))),
                ()->assertEquals(-1, n.insertSellOrder(new SellOrder("aaa", 1, 10, 5))),
                ()->assertEquals(-1, n.insertBuyOrder(new BuyOrder("aaa", 1, 10, 5)))
        );
    }
    @Test
    void successIOUD() {
        assertEquals(1,
                        n.insertOrUpdateInventory(new InventoryRecord(TEST_ORG_1, 1, 5)));
    }
    @Test
    void twoRowsIOUD() {
        assertAll(
                ()->assertEquals(1, n.insertOrUpdateInventory(
                        new InventoryRecord(TEST_ORG_1, 1, 5))),
                ()->assertEquals(2, n.insertOrUpdateInventory(
                        new InventoryRecord(TEST_ORG_1, 1, 6)))
        );
    }
    @Test
    void constrainedIOUD() {
        assertAll(
                ()->assertEquals(-1, n.insertOrUpdateInventory(
                        new InventoryRecord(TEST_ORG_1, 2, 5))),
                ()->assertEquals(-1, n.insertOrUpdateInventory(
                        new InventoryRecord("aaa", 1, 5)))
        );
    }
    @Test
    void successUpdates() {
        assertAll(
                ()->assertEquals(1, n.updateUnit(new OrgUnit(TEST_ORG_1, 20))),

                ()->assertEquals(1, n.updateUser(new User(TEST_USER,
                        "password", true, TEST_ORG_1))),
                ()->assertEquals(1, n.updateAsset(new Asset(1, TEST_ASSET_2))),
                ()->assertEquals(1, n.updateSellOrder(new SellOrder(1, TEST_USER, 1, 10, 5,
                        START_OF_YEAR, null))),
                ()->assertEquals(1, n.updateBuyOrder(new BuyOrder(1, TEST_USER, 1, 10, 5,
                        START_OF_YEAR, null, null)))
        );
    }
    @Test
    void failUpdates() {
        assertAll(
                ()->assertEquals(0, n.updateUnit(new OrgUnit(TEST_ORG_2, 20))),
                ()->assertEquals(0, n.updateUser(new User(TEST_USER_2,
                        "password", true, TEST_ORG_1))),
                ()->assertEquals(0, n.updateAsset(new Asset(2, TEST_ASSET))),
                ()->assertEquals(0, n.updateSellOrder(new SellOrder(2, TEST_USER, 1, 10, 5,
                        START_OF_YEAR, null))),
                ()->assertEquals(0, n.updateBuyOrder(new BuyOrder(2, TEST_USER, 1, 10, 5,
                        START_OF_YEAR, null, null)))
        );
    }
    @Test
    void constrainedUpdates() {
        assertAll(
                ()->assertEquals(-1, n.updateUser(new User(TEST_USER,
                        "password", false, TEST_ORG_2))),
                ()->assertEquals(-1, n.updateSellOrder(new SellOrder(1, TEST_USER_2, 1, 10, 5,
                        START_OF_YEAR, null))),
                ()->assertEquals(-1, n.updateSellOrder(new SellOrder(1, TEST_USER, 2, 10, 5,
                        START_OF_YEAR, null))),
                ()->assertEquals(-1, n.updateBuyOrder(new BuyOrder(1, TEST_USER_2, 1, 10, 5,
                        START_OF_YEAR, null, null))),
                ()->assertEquals(-1, n.updateBuyOrder(new BuyOrder(1, TEST_USER, 2, 10, 5,
                        START_OF_YEAR, null, null))),
                ()->assertEquals(-1, n.updateBuyOrder(new BuyOrder(1, TEST_USER, 1, 10, 5,
                        START_OF_YEAR, LocalDateTime.now(), 2)))
        );
    }
    @Test
    void successDelByKeys() {
        assertAll(
                ()->assertEquals(1, n.deleteSellOrder(1)),
                ()->assertEquals(1, n.deleteBuyOrder(1)),
                ()->assertEquals(1, n.deleteAsset(1)),
                ()->assertEquals(1, n.deleteUser(TEST_USER)),
                ()->assertEquals(1, n.deleteUnit(TEST_ORG_1))
        );
        n.debugDeleteEverything();
        n.recreate();
        assertAll(
                ()->assertEquals(1, n.insertUnit(new OrgUnit(TEST_ORG_1))),
                ()->assertEquals(1, n.insertUser(new User(TEST_USER,
                        "password", false, TEST_ORG_1))),
                ()->assertEquals(1, n.insertAsset(new Asset(TEST_ASSET))),
                ()->assertEquals(1, n.insertSellOrder(new SellOrder(TEST_USER, 1, 10, 5))),
                ()->assertEquals(1, n.insertBuyOrder(new BuyOrder(TEST_USER, 1, 10, 5)))
        );
    }
    @Test
    void failDelByKeys() {
        assertAll(
                ()->assertEquals(0, n.deleteUnit(TEST_ORG_2)),
                ()->assertEquals(0, n.deleteUser(TEST_USER_2)),
                ()->assertEquals(0, n.deleteAsset(2)),
                ()->assertEquals(0, n.deleteSellOrder(2)),
                ()->assertEquals(0, n.deleteBuyOrder(2))
        );
    }
    @Test
    void constrainedDelByKeys() {
        n.updateBuyOrder(new BuyOrder(1, TEST_USER, 1, 10, 5,
                START_OF_YEAR,
                LocalDateTime.now(), 1));
        assertAll(
                ()->assertEquals(-1, n.deleteUnit(TEST_ORG_1)),
                ()->assertEquals(-1, n.deleteUser(TEST_USER)),
                ()->assertEquals(-1, n.deleteAsset(1)),
                ()->assertEquals(-1, n.deleteSellOrder(1))
        );
        n.updateBuyOrder(new BuyOrder(1, TEST_USER, 1, 10, 5,
                START_OF_YEAR, null, null));
    }
    @Test
    void successDelInv() {
        n.insertOrUpdateInventory(new InventoryRecord(TEST_ORG_1, 1, 5));
        assertEquals(1, n.deleteInventoryRecord(TEST_ORG_1, 1));
    }
    @Test
    void failDelInv() {
        assertEquals(0, n.deleteInventoryRecord(TEST_ORG_2, 1));
    }
    @Test
    void selectEmptyTables() {
        assertAll(
                ()->assertEquals(1, n.deleteSellOrder(1)),
                ()->assertEquals(1, n.deleteBuyOrder(1)),
                ()->assertEquals(1, n.deleteAsset(1)),
                ()->assertEquals(1, n.deleteUser(TEST_USER)),
                ()->assertEquals(1, n.deleteUnit(TEST_ORG_1))
        );
        assertAll(
                ()->assertTrue(n.allOrgUnits().isEmpty()),
                ()->assertTrue(n.allUsers().isEmpty()),
                ()->assertTrue(n.allAssets().isEmpty()),
                ()->assertTrue(n.inventoryList().isEmpty())
//                ()->assertTrue(n.bu().isEmpty()),
//                ()->assertTrue(n.s().isEmpty())
        );
        n.debugDeleteEverything();
        n.recreate();
        assertAll(
                ()->assertEquals(1, n.insertUnit(new OrgUnit(TEST_ORG_1))),
                ()->assertEquals(1, n.insertUser(new User(TEST_USER,
                        "password", false, TEST_ORG_1))),
                ()->assertEquals(1, n.insertAsset(new Asset(TEST_ASSET))),
                ()->assertEquals(1, n.insertSellOrder(new SellOrder(TEST_USER, 1, 10, 5))),
                ()->assertEquals(1, n.insertBuyOrder(new BuyOrder(TEST_USER, 1, 10, 5)))
        );
    }
    @Test
    void selectNonEmptyTables() {
        assertAll(
                ()->assertFalse(n.allOrgUnits().isEmpty()),
                ()->assertFalse(n.allUsers().isEmpty()),
                ()->assertFalse(n.allAssets().isEmpty()),
                ()->assertFalse(n.inventoryList().isEmpty())
        );
    }
    @Test
    void nonexistentSellOrderQueries() {
        assertAll(
                ()->assertTrue(n.sellOrdersByAsset(1, true).isEmpty()),
                ()->assertTrue(n.sellOrdersByUser(TEST_USER, true).isEmpty())
                //placed between
                //resolved between
        );
    }
    @Test
    void successSellOrderQueries() {

    }
    @Test
    void nonexistentBuyOrderQueries() {

    }
    @Test
    void successBuyOrderQueries() {

    }
    @Test
    void successSelectOneInv() {
        n.insertOrUpdateInventory(new InventoryRecord(TEST_ORG_1, 1, 5));
        assertEquals(5, n.inventoryRecordByKeys(TEST_ORG_1, 1).getQuantity());
    }
    @Test
    void failSelectOneInv() {
        assertNull(n.inventoryRecordByKeys(TEST_ORG_1, 2));
    }
    @Test
    void successSelectByValue() {
        n.updateBuyOrder(new BuyOrder(1, TEST_USER, 1, 10, 5,
                START_OF_YEAR,
                LocalDateTime.of(2021, 6, 5, 0, 0), 1));
        assertAll(
                ()->assertFalse(n.usersByUnit(TEST_ORG_1).isEmpty()),
                ()->assertFalse(n.inventoriesByUnit(TEST_ORG_1).isEmpty()),
                ()->assertFalse(n.inventoriesByAsset(1).isEmpty()),
                ()->assertFalse(n.buyOrdersByBoughtFrom(1).isEmpty())
        );
        n.updateBuyOrder(new BuyOrder(1, TEST_USER, 1, 10, 5,
                START_OF_YEAR, null, null));
    }
    @Test
    void failSelectByValue() {
        assertAll(
                ()->assertTrue(n.usersByUnit(TEST_ORG_2).isEmpty()),
                ()->assertTrue(n.inventoriesByUnit(TEST_ORG_2).isEmpty()),
                ()->assertTrue(n.inventoriesByAsset(1).isEmpty()),
                ()->assertTrue(n.buyOrdersByBoughtFrom(2).isEmpty())
        );
    }
    @Test
    void successSelectByKey() {
        assertAll(
                ()->assertNotNull(n.unitByKey(TEST_ORG_1)),
                ()->assertNotNull(n.userByKey(TEST_USER)),
                ()->assertNotNull(n.assetByKey(1)),
                ()->assertNotNull(n.sellOrderByKey(1)),
                ()->assertNotNull(n.buyOrderByKey(1))
        );
    }
    @Test
    void failSelectByKey() {
        assertAll(
                ()->assertNull(n.unitByKey(TEST_ORG_2)),
                ()->assertNull(n.userByKey(TEST_USER)),
                ()->assertNull(n.assetByKey(2)),
                ()->assertNull(n.sellOrderByKey(2)),
                ()->assertNull(n.buyOrderByKey(2))
        );
    }
}
