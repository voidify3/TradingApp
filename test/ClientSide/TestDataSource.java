package ClientSide;

import common.*;
import org.junit.jupiter.api.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;


public class TestDataSource {
    public static final LocalDateTime START_OF_YEAR = LocalDateTime.of(2021, 1, 5, 0, 0);
    static TradingAppDataSource n;
    public static final String TEST_USER = "testUser";
    public static final String TEST_USER_2 = "testUserTwo";
    public static final String TEST_ORG_1 = "Devs";
    public static final String TEST_ORG_2 = "Marketing";
    public static final String TEST_ASSET = "test";
    public static final String TEST_ASSET_2 = "test2";
    @BeforeEach
    void setupAndSuccessInserts() {
        //to test network data source,
        // use a compound configuration: this, and ServerGui with CLI args {"RESET"}
        n = new MockDataSource();
        //n = new NetworkDataSource();
        assertAll(
                ()->assertEquals(1, n.insertUnit(new OrgUnit(TEST_ORG_1, 0))),
                ()->assertFalse(n.allOrgUnits().isEmpty()),
                ()->assertEquals(1, n.insertUser(new User(TEST_USER,
                        "password", false, TEST_ORG_1))),
                ()->assertEquals(1, n.insertAsset(new Asset(0, TEST_ASSET))),
                ()->assertEquals(1, n.insertSellOrder(new SellOrder(TEST_ORG_1, 1, 10, 5))),
                ()->assertEquals(1, n.insertBuyOrder(new BuyOrder(TEST_ORG_1, 1, 10, 5)))
        );
    }
    @AfterEach
    void done() {
        n.debugDeleteEverything(); n.recreate();
    }

    @Test
    void failInserts() {
        assertAll(
                ()->assertEquals(0, n.insertUnit(new OrgUnit(TEST_ORG_1, 0))),
                ()->assertEquals(0, n.insertUser(new User(TEST_USER,
                        "password", false, TEST_ORG_1)))
        );
    }
    @Test
    void constrainedInserts() {
        assertAll(
                ()->assertEquals(-1, n.insertUser(new User(TEST_USER_2,
                        "password", false, "aaa"))),
                ()->assertEquals(-1, n.insertSellOrder(new SellOrder(TEST_ORG_1, 2, 10, 5))),
                ()->assertEquals(-1, n.insertBuyOrder(new BuyOrder(TEST_ORG_1, 2, 10, 5))),
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
                ()->assertEquals(1, n.updateSellOrder(new SellOrder(1, TEST_ORG_1, 1, 10, 5,
                        START_OF_YEAR, null))),
                ()->assertEquals(1, n.updateBuyOrder(new BuyOrder(1, TEST_ORG_1, 1, 10, 5,
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
                ()->assertEquals(0, n.updateSellOrder(new SellOrder(2, TEST_ORG_1, 1, 10, 5,
                        START_OF_YEAR, null))),
                ()->assertEquals(0, n.updateBuyOrder(new BuyOrder(2, TEST_ORG_1, 1, 10, 5,
                        START_OF_YEAR, null, null)))
        );
    }
    @Test
    void constrainedUpdates() {
        assertAll(
                ()->assertEquals(-1, n.updateUser(new User(TEST_USER,
                        "password", false, TEST_ORG_2))),
                ()->assertEquals(-1, n.updateSellOrder(new SellOrder(1, TEST_ORG_2, 1, 10, 5,
                        START_OF_YEAR, null))),
                ()->assertEquals(-1, n.updateSellOrder(new SellOrder(1, TEST_ORG_1, 2, 10, 5,
                        START_OF_YEAR, null))),
                ()->assertEquals(-1, n.updateBuyOrder(new BuyOrder(1, TEST_ORG_2, 1, 10, 5,
                        START_OF_YEAR, null, null))),
                ()->assertEquals(-1, n.updateBuyOrder(new BuyOrder(1, TEST_ORG_1, 2, 10, 5,
                        START_OF_YEAR, null, null))),
                ()->assertEquals(-1, n.updateBuyOrder(new BuyOrder(1, TEST_ORG_1, 1, 10, 5,
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
    void successDelInv() {
        assertEquals(1, n.insertOrUpdateInventory(new InventoryRecord(TEST_ORG_1, 1, 5)));
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
                ()->assertTrue(n.allInventories().isEmpty()),
                ()->assertTrue(n.allBuyOrders().isEmpty()),
                ()->assertTrue(n.allSellOrders().isEmpty())
        );
    }
    @Test
    void selectNonEmptyTables() {
        n.insertOrUpdateInventory(new InventoryRecord(TEST_ORG_1, 1, 5));
        assertAll(
                ()->assertFalse(n.allOrgUnits().isEmpty()),
                ()->assertFalse(n.allUsers().isEmpty()),
                ()->assertFalse(n.allAssets().isEmpty()),
                ()->assertFalse(n.allInventories().isEmpty())
        );
    }
    @Test
    void nonexistentSellOrderQueries() {
        assertAll(
                ()->assertTrue(n.allSellOrders(true).isEmpty()),
                ()->assertTrue(n.sellOrdersByAsset(1, true).isEmpty()),
                ()->assertTrue(n.sellOrdersByAsset(5,null).isEmpty()),
                ()->assertTrue(n.sellOrdersByUnit(TEST_ORG_1, true).isEmpty()),
                ()->assertTrue(n.sellOrdersByUnit(TEST_ORG_2, null).isEmpty()),
                ()->assertTrue(n.sellOrdersResolvedBetween(Timestamp.valueOf(START_OF_YEAR),
                        Timestamp.valueOf(LocalDateTime.now())).isEmpty()),
                ()->assertTrue(n.sellOrdersPlacedBetween(Timestamp.valueOf(START_OF_YEAR),
                Timestamp.valueOf(LocalDateTime.now()), true).isEmpty())
        );
    }
    @Test
    void successSellOrderQueries() {
        assertAll(
                ()-> assertFalse(n.allSellOrders(false).isEmpty()),
                ()-> assertFalse(n.sellOrdersByAsset(1, null).isEmpty()),
                ()-> assertFalse(n.sellOrdersByUnit(TEST_ORG_1, null).isEmpty()),
                ()-> assertFalse(n.sellOrdersPlacedBetween(Timestamp.valueOf(START_OF_YEAR),
                        Timestamp.valueOf(LocalDateTime.now()), null).isEmpty())
        );
        assertAll(
                ()-> assertFalse(n.allSellOrders(null).isEmpty()),
                ()-> assertFalse(n.sellOrdersByAsset(1, false).isEmpty()),
                ()-> assertFalse(n.sellOrdersByUnit(TEST_ORG_1, false).isEmpty()),
                ()-> assertFalse(n.sellOrdersPlacedBetween(Timestamp.valueOf(START_OF_YEAR),
                        Timestamp.valueOf(LocalDateTime.now()), false).isEmpty())
        );
        n.updateSellOrder(new SellOrder(1, TEST_ORG_1, 1, 10, 5,
                START_OF_YEAR,
                LocalDateTime.now()));
        assertAll(
                ()-> assertFalse(n.allSellOrders(true).isEmpty()),
                ()-> assertFalse(n.sellOrdersByAsset(1, true).isEmpty()),
                ()-> assertFalse(n.sellOrdersByUnit(TEST_ORG_1, true).isEmpty()),
                ()-> assertFalse(n.sellOrdersPlacedBetween(Timestamp.valueOf(START_OF_YEAR),
                        Timestamp.valueOf(LocalDateTime.now()), true).isEmpty()),
                ()-> assertFalse(n.sellOrdersResolvedBetween(Timestamp.valueOf(START_OF_YEAR),
                        Timestamp.valueOf(LocalDateTime.now())).isEmpty())

        );

    }
    @Test
    void nonexistentBuyOrderQueries() {
        assertAll(
                ()->assertTrue(n.allBuyOrders(true).isEmpty()),
                ()->assertTrue(n.buyOrdersByAsset(1, true).isEmpty()),
                ()->assertTrue(n.buyOrdersByAsset(5, null).isEmpty()),
                ()->assertTrue(n.buyOrdersByUnit(TEST_ORG_1, true).isEmpty()),
                ()->assertTrue(n.buyOrdersByUnit(TEST_ORG_2, null).isEmpty()),
                ()->assertTrue(n.buyOrdersResolvedBetween(Timestamp.valueOf(START_OF_YEAR),
                        Timestamp.valueOf(LocalDateTime.now())).isEmpty()),
                ()->assertTrue(n.buyOrdersByAssetResolvedBetween(1, Timestamp.valueOf(START_OF_YEAR),
                        Timestamp.valueOf(LocalDateTime.now())).isEmpty()),
                ()->assertTrue(n.buyOrdersPlacedBetween(Timestamp.valueOf(START_OF_YEAR),
                        Timestamp.valueOf(LocalDateTime.now()), true).isEmpty()),
                ()->assertTrue(n.buyOrdersByBoughtFrom(1).isEmpty())
        );
    }
    @Test
    void successBuyOrderQueries() {
        assertAll(
                ()-> assertFalse(n.allBuyOrders(false).isEmpty()),
                ()-> assertFalse(n.buyOrdersByAsset(1, false).isEmpty()),
                ()-> assertFalse(n.buyOrdersByUnit(TEST_ORG_1, false).isEmpty()),
                ()-> assertFalse(n.buyOrdersPlacedBetween(Timestamp.valueOf(START_OF_YEAR),
                        Timestamp.valueOf(LocalDateTime.now()), false).isEmpty())
        );
        assertAll(
                ()-> assertFalse(n.allBuyOrders(null).isEmpty()),
                ()-> assertFalse(n.buyOrdersByAsset(1, null).isEmpty()),
                ()-> assertFalse(n.buyOrdersByUnit(TEST_ORG_1, null).isEmpty()),
                ()-> assertFalse(n.buyOrdersPlacedBetween(Timestamp.valueOf(START_OF_YEAR),
                        Timestamp.valueOf(LocalDateTime.now()), null).isEmpty())
        );
        n.updateBuyOrder(new BuyOrder(1, TEST_ORG_1, 1, 10, 5,
                START_OF_YEAR,
                LocalDateTime.now(), 1));
        assertAll(
                ()-> assertFalse(n.allBuyOrders(true).isEmpty()),
                ()-> assertFalse(n.buyOrdersByAsset(1, true).isEmpty()),
                ()-> assertFalse(n.buyOrdersByUnit(TEST_ORG_1, true).isEmpty()),
                ()-> assertFalse(n.buyOrdersPlacedBetween(Timestamp.valueOf(START_OF_YEAR),
                        Timestamp.valueOf(LocalDateTime.now()), true).isEmpty()),
                ()-> assertFalse(n.buyOrdersResolvedBetween(Timestamp.valueOf(START_OF_YEAR),
                        Timestamp.valueOf(LocalDateTime.now())).isEmpty()),
                ()-> assertFalse(n.buyOrdersByAssetResolvedBetween(1, Timestamp.valueOf(START_OF_YEAR),
                        Timestamp.valueOf(LocalDateTime.now())).isEmpty()),
                ()-> assertFalse(n.buyOrdersByBoughtFrom(1).isEmpty())
        );
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
        n.insertOrUpdateInventory(new InventoryRecord(TEST_ORG_1, 1, 5));

        assertAll(
                ()->assertEquals(1, n.updateBuyOrder(new BuyOrder(1, TEST_ORG_1, 1, 10, 5,
                        START_OF_YEAR,
                        LocalDateTime.of(2021, 6, 5, 0, 0), 1))),
                ()->assertFalse(n.usersByUnit(TEST_ORG_1).isEmpty()),
                ()->assertFalse(n.inventoriesByUnit(TEST_ORG_1).isEmpty()),
                ()->assertFalse(n.inventoriesByAsset(1).isEmpty()),
                ()->assertFalse(n.buyOrdersByBoughtFrom(1).isEmpty())
        );
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
                ()->assertNull(n.userByKey(TEST_USER_2)),
                ()->assertNull(n.assetByKey(2)),
                ()->assertNull(n.sellOrderByKey(2)),
                ()->assertNull(n.buyOrderByKey(2))
        );
    }
}
