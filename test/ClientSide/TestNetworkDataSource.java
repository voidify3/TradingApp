package ClientSide;

import common.*;
import org.junit.jupiter.api.*;

import static common.DatabaseTables.ASSET;
import static common.ProtocolKeywords.INSERT;
import static org.junit.jupiter.api.Assertions.*;

public class TestNetworkDataSource {
    NetworkDataSource n;
    public static final String TEST_USER = "testUser";
    public static final String TEST_USER_2 = "testUserTwo";
    public static final String TEST_ORG_1 = "Devs";
    public static final String TEST_ORG_2 = "Marketing";
    public static final String TEST_ASSET = "test";
    public static final String TEST_ASSET_2 = "test2";
    @BeforeEach
    public void setupAndSuccessInserts() {
        n = new NetworkDataSource();

        long ping = n.ping();
        System.out.println(ping);
        assertTrue(ping > 0);
        n.debugDeleteEverything();
        n.recreate();
        assertAll(
                ()->assertEquals(1, n.insertUnit(new OrgUnit(TEST_ORG_1)))
//                ()->assertEquals(1, n.insertUser(new User(TEST_USER,
//                        "password", false, TEST_ORG_1))),
//                ()->assertEquals(1, n.insertAsset(new Asset(TEST_ASSET))),
//                ()->assertEquals(1, n.insertSellOrder(new SellOrder(TEST_USER, 1, 10, 5))),
//                ()->assertEquals(1, n.insertBuyOrder(new BuyOrder(TEST_USER, 1, 10, 5)))
        );
    }
    @AfterEach
    public void done() {
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
    }
    @Test
    void failUpdates() {
    }
    @Test
    void constrainedUpdates() {

    }
    @Test
    void successDelByKeys() {

    }
    @Test
    void failDelByKeys() {

    }
    @Test
    void constrainedDelByKeys() {

    }
    @Test
    void successDelInv() {

    }
    @Test
    void failDelInv() {

    }
    @Test
    void selectEmptyTables() {

    }
    @Test
    void selectNonEmptyTables() {

    }
    @Test
    void nonexistentSellOrderQueries() {

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

    }
    @Test
    void failSelectOneInv() {

    }
    @Test
    void successSelectByValue() {

    }
    @Test
    void failSelectByValue() {

    }
    @Test
    void successSelectByKey() {

    }
    @Test
    void failSelectByKey() {

    }
}
