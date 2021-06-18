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
        i.mockObjectsWithPrices();
    }
    @AfterEach
    void reset() {
        i.deleteEverything();
    }
    /**
     * Test that when too many assets are attempted to be sold an exception is thrown.
     */
    @Test
    public void TestSellTooMany() {
        SellOrder testSellOrder = new SellOrder(unitDev.getName(), assetDev1.getId(), 500, 10);
        assertThrows(OrderException.class, () -> {i.placeSellOrder(testSellOrder);});

    }

    /**
     * Test that when the total cost of a buy order exceeds the organisations credits an exception is thrown
     */
    @Test
    public void TestBuyTooExpensive() {
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
