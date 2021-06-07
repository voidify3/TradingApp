package ClientSide;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class TestNetworkDataSource {
    NetworkDataSource n;
    @BeforeEach @Test
    public void setup() {
        n = new NetworkDataSource();
    }
    //TODO: these tests:
    // * Success and fail tests for insertOrUpdateInventory
    // * Success and fail tests for insertion-- assert return value for all insert-calling publics in same test
    // * Ditto for update-calling methods
    // * Ditto for deleteByKey-calling methods
    // * Success and fail tests for deleteInventoryRecord
    // * All select-entire-table methods in one test with empty tables
    // * Select-entire-table success test
    // * Test calling all multi-param selection SellOrder methods with values that will succeed
    // * Ditto but fail
    // * Above two tests but BuyOrder
    // * Success and fail tests for inventoryRecordByKeys
    // * Success and fail tests blanketing all selectByValue-calling methods
    // * Success and fail tests blanketing all selectByKey-calling methods
    @AfterEach
    public void done() {
        n.debugDeleteEverything();
    }
}
