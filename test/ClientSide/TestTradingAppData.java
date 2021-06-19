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
    void setupData() throws IllegalString, InvalidAmount, DoesNotExist, OrderException {
        i.mockObjectsWithPrices(4*365);
    }
    @AfterEach
    void reset() {
        i.deleteEverything();
    }

    @Test
    void testSuccessLogin() {

    }
    @Test
    void testFailLogin() {

    }

    //TODO:
    // SELECT
    // -normal cases: non-empty arraylists from straightforward methods
    // -normal cases: empty arraylists from straightforward methods
    // -normal cases: non-empty from non-straightforward methods (ensure correct behaviour)
    // -exception cases: DoesNotExist due to non-key filter value
    // -normal cases: single dataobjects
    // -exception cases: DoesNotExist due to key filter value
    // INSERT
    // -normal cases
    // -normal order placing cases; check that secondary operations took place
    // -exception cases: DoesNotExist
    // -exception cases: AlreadyExists
    // UPDATE
    // -normal cases
    // -exception cases
    // DELETE
    // -normal cases
    // -DoesNotExist cases


    /**
     * Test that when too many assets are attempted to be sold an exception is thrown.
     */
    @Test
    public void testSellTooMany() {
        SellOrder testSellOrder = new SellOrder(unitDev.getName(), assetDev1.getId(), 500, 10);
        assertThrows(OrderException.class, () -> {i.placeSellOrder(testSellOrder);});

    }

    /**
     * Test that when the total cost of a buy order exceeds the organisations credits an exception is thrown
     */
    @Test
    public void testBuyTooExpensive() {
        BuyOrder testBuyOrder = new BuyOrder(unitDev.getName(), assetDev1.getId(), 5, 1000);
        assertThrows(OrderException.class, () -> {i.placeBuyOrder(testBuyOrder);});
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
        i.getHistoricalPrices(assetDev1.getId(), TradingAppData.Intervals.DAYS);
        i.getHistoricalPrices(assetDev1.getId(), TradingAppData.Intervals.WEEKS);
        i.getHistoricalPrices(assetDev1.getId(), TradingAppData.Intervals.MONTHS);
        i.getHistoricalPrices(assetDev1.getId(), TradingAppData.Intervals.YEARS);
    }
}
