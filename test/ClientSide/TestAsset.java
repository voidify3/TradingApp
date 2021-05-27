package ClientSide;

import common.Exceptions.*;
import common.Asset;
import common.OrgUnit;
import common.User;
import org.junit.jupiter.api.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
/***
 * @author Scott
 */
public class TestAsset {

    Asset mockAsset;
    Asset mockAssetEmpty; // kept empty before tests so we can test creation exceptions
    User mockGenericUser;
    User mockAdminUser;
    OrgUnit mockUnit;
    TradingAppData i = new TradingAppData();

    // BEFORE & AFTER EACH TEST-----------------------------------------------------------------------------------------
    /***
     * Create fresh dummy asset with history before each test, as well as users and a unit.
     */
    @BeforeEach
    @DisplayName("Before each... create new mock asset, users & a unit")
    public void mockAsset() throws InvalidDate, InvalidPrice, IllegalString, AlreadyExists {
        i.mockObjectsWithPrices();
        mockAsset = TradingAppData.assetDev1; //999, "Test asset for development!"
//        mockAsset = new Asset(123, "This is an asset.");
//        LocalDate today = LocalDate.now();
//        for (int i = 0; i < 4 * 365; i++) {
//            mockAsset.addHistoricalPrice(10, today.minusDays(i));
//            mockAsset.addHistoricalPrice(15, today.minusDays(i));
//            mockAsset.addHistoricalPrice(20, today.minusDays(i));
//        }
//        mockGenericUser = new User("dude", "dud3", false);
//        mockAdminUser = new User("scott", "password", true);
//        mockUnit = new OrgUnit("developers");
    }

    /***
     * Leave no trace of the mock users.
     */
    @AfterEach
    @DisplayName("After each... delete mock asset, users & the unit")
    public void clearMockUsers() throws DoesNotExist {
        i.deleteEverything();
    }

    /***
     * Checks if assetID is not null when returning.
     */
    @Test
    public void testAssetIDNotNull() {
        assertNotNull(mockAsset.getId());
    }

    /***
     * Checks if assetDesc is not null when returning.
     */
    @Test
    public void testAssetDescNotNull() {
        assertNotNull(mockAsset.getDescription());
    }

    // TEST CONSTRUCTOR, GETTERS, & SETTERS-----------------------------------------------------------------------------
    /***
     * Tests if asset constructor compiles, which indirectly tests the getters as well.
     */
    @Test
    @DisplayName("Asset constructor assigns the data correctly")
    public void testAssetConstructor() {
        assertAll(
                // the asset ID is assigned correctly
                () -> assertEquals(999, mockAsset.getId()),
                // the asset description is assigned correctly
                () -> assertEquals("Test asset for development!", mockAsset.getDescription()));
    }

    // TEST METHODS-----------------------------------------------------------------------------------------------------
//    @Test
//    public void testAddHistoricalPricesPass() throws InvalidPrice, InvalidDate {
//        mockAsset.addHistoricalPrice(999, LocalDate.now());
//        assertAll(
//                // Test if works
//                () -> assertEquals(999, mockAsset.priceHistory.get(LocalDate.now())
//                        .get((mockAsset.priceHistory.get(LocalDate.now()).size()) - 1)),
//                // Future date exception
//                () -> assertThrows(InvalidDate.class, () ->
//                        mockAsset.addHistoricalPrice(10, LocalDate.now().plusDays(1))),
//                // Price less than zero exception
//                () -> assertThrows(InvalidPrice.class, () ->
//                        mockAsset.addHistoricalPrice(-1, LocalDate.now()))
//        );
//    }

    @Test
    public void testGetAveragePrice() {
        assertAll(
                // Multiple days
                () -> assertEquals(15, i.getAveragePrice(LocalDate.of(2021,1,1),
                        LocalDate.of(2021, 2, 1), mockAsset)),
                // Single day
                () -> assertEquals(15, i.getAveragePrice(LocalDate.of(2021,1,1),
                        LocalDate.of(2021, 1, 1), mockAsset)),
                // Future end date correction
                () -> assertEquals(15, i.getAveragePrice(LocalDate.of(2021,1,1),
                        LocalDate.now().plusDays(1), mockAsset)),
                // Early start date correction
                () -> assertEquals(15, i.getAveragePrice(LocalDate.now().minusDays(4 * 365 + 1),
                        LocalDate.now(), mockAsset)),
                // Future start date exception
                () -> assertThrows(InvalidDate.class, () ->
                        i.getAveragePrice(LocalDate.now().plusDays(1), LocalDate.now(), mockAsset)),
                // Early end date exception
                () -> assertThrows(InvalidDate.class, () ->
                        i.getAveragePrice(LocalDate.now(), LocalDate.now().minusDays(4 * 365 + 1), mockAsset)),
                // Start date after end date
                () -> assertThrows(InvalidDate.class, () ->
                        i.getAveragePrice(LocalDate.now(), LocalDate.now().minusDays(1), mockAsset))
        );
    }

    @Test
    public void testGetHistoricalPrices() throws InvalidDate, DoesNotExist {
        i.getHistoricalPrices(mockAsset, TradingAppData.Intervals.DAYS);
        i.getHistoricalPrices(mockAsset, TradingAppData.Intervals.WEEKS);
        i.getHistoricalPrices(mockAsset, TradingAppData.Intervals.MONTHS);
        i.getHistoricalPrices(mockAsset, TradingAppData.Intervals.YEARS);
    }


}

