package ClientSide;
import common.*;
import common.Exceptions.*;
import org.junit.jupiter.api.*;

import java.time.LocalDate;

import static ClientSide.TradingAppData.*;
import static org.junit.jupiter.api.Assertions.*;

public class TestTradingAppData {
    TradingAppData i = new TradingAppData(new MockDataSource());
    @BeforeEach
    void setupData() {
        i.mockObjectsWithPrices(4*365);
    }
    @AfterEach
    void reset() {
        i.deleteEverything();
    }

    @Test
    void testSuccessLogin() {
        assertDoesNotThrow(()->{
            User u = i.login("sophia", "bo$$");
            assertEquals(adminDev, u);
            //usernames are case insensitive
            u = i.login("SoPhIa", "bo$$");
            assertEquals(adminDev, u);
        });
    }
    @Test
    void testFailLogin() {
        assertAll(
                //passwords aren't case insensitive
                ()->assertThrows(DoesNotExist.class, ()->{
                    i.login("sophia", "Bo$$");
                }),
                //trivially incorrect password is incorrect
                ()->assertThrows(DoesNotExist.class, ()->{
                    i.login("sophia", "a");
                }),
                //different exception when password has whitespace
                ()->assertThrows(IllegalString.class, ()->{
                    i.login("sophia", "a a a");
                }),
                //exception when username doesn't exist in database at all
                ()->assertThrows(DoesNotExist.class, ()->{
                    i.login("FakeUsername", "a");
                })
        );
    }


    @Test
    public void testGetAveragePrice() {
        assertAll(
                // Multiple days
                () -> assertEquals(15, i.getAveragePrice(LocalDate.of(2021,1,1),
                        LocalDate.of(2021, 2, 1), assetDev1.getId())),
                // Single day
                () -> assertEquals(15, i.getAveragePrice(LocalDate.of(2021,1,1),
                        LocalDate.of(2021, 1, 1), assetDev1.getId())),
                // Future end date correction
                () -> assertEquals(15, i.getAveragePrice(LocalDate.of(2021,1,1),
                        LocalDate.now().plusDays(1), assetDev1.getId())),
                // Early start date correction
                () -> assertEquals(15, i.getAveragePrice(LocalDate.now().minusDays(4 * 365 + 1),
                        LocalDate.now(), assetDev1.getId())),
                // Future start date exception
                () -> assertThrows(InvalidDate.class, () ->
                        i.getAveragePrice(LocalDate.now().plusDays(1), LocalDate.now(), assetDev1.getId())),
                // Early end date exception
                () -> assertThrows(InvalidDate.class, () ->
                        i.getAveragePrice(LocalDate.now(), LocalDate.now().minusDays(4 * 365 + 1), assetDev1.getId())),
                // Start date after end date
                () -> assertThrows(InvalidDate.class, () ->
                        i.getAveragePrice(LocalDate.now(), LocalDate.now().minusDays(1), assetDev1.getId()))
        );
    }

    @Test
    public void testGetHistoricalPrices() throws InvalidDate, DoesNotExist {
        //TODO: TEST THAT VALUES ARE CORRECT
//        i.getHistoricalPrices(assetDev1.getId(), TradingAppData.Intervals.DAYS);
//        i.getHistoricalPrices(assetDev1.getId(), TradingAppData.Intervals.WEEKS);
//        i.getHistoricalPrices(assetDev1.getId(), TradingAppData.Intervals.MONTHS);
//        i.getHistoricalPrices(assetDev1.getId(), TradingAppData.Intervals.YEARS);
    }

    //TODO:
    // SELECT
    // -normal cases: non-empty arraylists from straightforward methods
    // -normal cases: empty arraylists from straightforward methods
    // -normal cases: non-empty from non-straightforward methods (ensure correct behaviour)
    // -exception cases: DoesNotExist due to non-key filter value
    // -normal cases: single dataobjects
    // -exception cases: DoesNotExist due to key filter value


    @Test
    void insertNormalCases() {
        assertDoesNotThrow(()->{
            //this will be asset 3 when inserted since 2 assets are in the mock objects
            i.addAsset(DataObjectFactory.newAssetNaive(0,"aaa"));
            i.addUnit(DataObjectFactory.newOrgUnitNaive("aaa", 0));
            i.addUser(new User("aaa", "aaa", false, null));
            i.setInventory(new InventoryRecord(unitDev.getName(), 3, 5));
            int oldCredits = i.getUnitByKey(unitDev.getName()).getCredits();
            BuyOrder testBuyOrder = new BuyOrder(unitDev.getName(), assetDev1.getId(), 5, 5);
            i.placeBuyOrder(testBuyOrder);
            assertEquals(oldCredits-25, i.getUnitByKey(unitDev.getName()).getCredits());
            int oldQuantity = i.getInv(unitDev.getName(),assetDev1.getId()).getQuantity();
            SellOrder testSellOrder = new SellOrder(unitDev.getName(), assetDev1.getId(), 5, 10);
            i.placeSellOrder(testSellOrder);
            assertEquals(oldQuantity-5, i.getInv(unitDev.getName(),assetDev1.getId()).getQuantity());
        });
    }
    @Test
    void insertExceptionCases() {
        assertAll(
                ()-> assertThrows(AlreadyExists.class, ()->i.addUser(adminDev)),
                ()-> assertThrows(AlreadyExists.class, ()->i.addUnit(unitDev)),
                ()-> assertThrows(DoesNotExist.class, ()->i.addUser(new User("ah", "a",
                        false, "beeeeeeeeeeeee"))),
                ()-> assertThrows(DoesNotExist.class, ()->i.setInventory(new InventoryRecord(unitDev.getName(),
                        200, 5))),
                ()-> assertThrows(DoesNotExist.class, ()->i.setInventory(new InventoryRecord("aaa",
                        1, 5)))

        );
    }

    /**
     * Test that when too many assets are attempted to be sold an exception is thrown.
     */
    @Test
    public void testSellTooMany() {
        SellOrder testSellOrder = new SellOrder(unitDev.getName(), assetDev1.getId(), 500, 10);
        assertThrows(OrderException.class, () -> i.placeSellOrder(testSellOrder));

    }
    /**
     * Test that when the total cost of a buy order exceeds the organisations credits an exception is thrown
     */
    @Test
    public void testBuyTooExpensive() {
        BuyOrder testBuyOrder = new BuyOrder(unitDev.getName(), assetDev1.getId(), 5, 1000);
        assertThrows(OrderException.class, () -> i.placeBuyOrder(testBuyOrder));
    }

    @Test
    void updateNormalCases() {

        assertDoesNotThrow(()->{
            i.setInventory(new InventoryRecord(unitDev.getName(), assetDev1.getId(), 400));
            userDev2.changePassword("hello");
            i.updateUser(userDev2);
            userDev3.setAdminAccess(true);
            i.updateUser(userDev3);
            userDev4.setUnit(unitDev2.getName());
            i.updateUser(userDev4);
            unitDev.setBalance(0);
            i.updateUnit(unitDev);
            i.updateAsset(new Asset(1, "iii"));
        });
    }
    @Test
    void updateExceptionCases() {
        userDev4.setUnit("aaaaaaaaaaaaaaaaaa");
        SellOrder badSell1 = new SellOrder(1, "aaaaaaaa", testSellOrder.getAsset(),
                testSellOrder.getQty(), testSellOrder.getPrice(), testSellOrder.getDatePlaced(), null);
        SellOrder badSell2 = new SellOrder(1, testSellOrder.getUnit(), 500,
                testSellOrder.getQty(), testSellOrder.getPrice(), testSellOrder.getDatePlaced(), null);
        BuyOrder badBuy1 = new BuyOrder(1, "aaaaaaaa", testBuyOrder.getAsset(),
                testBuyOrder.getQty(), testBuyOrder.getPrice(), testBuyOrder.getDatePlaced(), null, null);
        BuyOrder badBuy2 = new BuyOrder(1, testBuyOrder.getUnit(), 500,
                testBuyOrder.getQty(), testBuyOrder.getPrice(), testBuyOrder.getDatePlaced(), null, null);
        BuyOrder badBuy3 = new BuyOrder(1, testBuyOrder.getUnit(), testBuyOrder.getAsset(),
                testBuyOrder.getQty(), testBuyOrder.getPrice(), testBuyOrder.getDatePlaced(), null, -1);
        assertAll(
                ()->assertThrows(DoesNotExist.class, ()->i.updateUser(new User("eeby", "ass", false, null))),
                ()->assertThrows(ConstraintException.class, ()->i.updateUser(userDev4)),
                ()->assertThrows(DoesNotExist.class, ()->i.updateBuyOrder(new BuyOrder(unitDev.getName(), 1, 1, 1))),
                ()->assertThrows(DoesNotExist.class, ()->i.updateSellOrder(new SellOrder(unitDev.getName(), 1, 1,1))),
                ()->assertThrows(DoesNotExist.class, ()->i.updateAsset(new Asset(500, "eeee"))),
                ()->assertThrows(ConstraintException.class,()->i.updateSellOrder(badSell1)),
                ()->assertThrows(ConstraintException.class,()->i.updateSellOrder(badSell2)),
                ()->assertThrows(ConstraintException.class,()->i.updateBuyOrder(badBuy1)),
                ()->assertThrows(ConstraintException.class,()->i.updateBuyOrder(badBuy2)),
                ()->assertThrows(ConstraintException.class,()->i.updateBuyOrder(badBuy3))
        );
    }

    @Test
    void deleteNormalCases() {
        assertDoesNotThrow(()->{
            i.deleteInventoryRecord(unitDev.getName(), assetDev1.getId());
            i.deleteUser(userDev.getUsername());
            int change;
            //test that cancelling an outstanding buy order returns the credits
            int oldCredits = i.getUnitByKey(unitDev.getName()).getCredits();
            change = testBuyOrder.getPrice()*testBuyOrder.getQty();
            i.cancelBuyOrder(1);
            int newCredits = i.getUnitByKey(unitDev.getName()).getCredits();
            assertEquals(oldCredits+change, newCredits);
            //test that credits aren't changed by cancelling a non-outstanding buy (i.e. the first of the historical price buys)
            i.cancelBuyOrder(2);
            assertEquals(newCredits, i.getUnitByKey(unitDev.getName()).getCredits());

            //test that cancelling an outstanding sell order returns the assets
            int oldQty = i.getInv(unitDev.getName(),assetDev1.getId()).getQuantity();
            change = testSellOrder.getQty();
            i.cancelSellOrder(1);
            int newQty = i.getInv(unitDev.getName(),assetDev1.getId()).getQuantity();
            assertEquals(oldQty+change, newQty);
            //test that assets aren't changed by cancelling a non-outstanding sell (the first of the historical price sells)
            i.cancelSellOrder(2);
            assertEquals(newQty, i.getInv(unitDev.getName(),assetDev1.getId()).getQuantity());
            i.deleteUnit(unitDev.getName());
            i.deleteAsset(assetDev1.getId());
        });
    }
    @Test
    void deleteExceptionCases() {
        assertAll(
                ()->assertThrows(DoesNotExist.class, ()->i.deleteInventoryRecord(unitDev2.getName(), assetDev2.getId())),
                ()->assertThrows(DoesNotExist.class, ()->i.deleteInventoryRecord(unitDev.getName(), 50)),
                ()->assertThrows(DoesNotExist.class, ()->i.deleteInventoryRecord("aaa", assetDev1.getId())),
                ()->assertThrows(DoesNotExist.class, ()->i.deleteUser("a")),
                ()->assertThrows(DoesNotExist.class, ()->i.cancelBuyOrder(10000)),
                ()->assertThrows(DoesNotExist.class, ()->i.cancelSellOrder(10000)),
                ()->assertThrows(DoesNotExist.class, ()->i.deleteUnit("a")),
                ()->assertThrows(DoesNotExist.class, ()->i.deleteAsset(10000))
        );
    }

}
