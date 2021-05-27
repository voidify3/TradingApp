package ClientSideTests;

import static org.junit.jupiter.api.Assertions.*;

import ClientSide.TradingAppData;
import common.Exceptions.*;

//import org.junit.Test;
import common.*;
import common.Order;
import org.junit.jupiter.api.*;

/**
 * @author Alistair Ridge
 *
 * The following tests are used to test the functionality of the Orders class.
 *
 * The key functionality that will be tested is placing a buy and sell order.
 * As part of this this class will test that an exception is thrown in the following scenarios:
 *      - Buy order exceeds organisations total credits minus the cost of unconsolidated buy orders.
 *      - Sell order exceeds the amount of an asset the organisation holds.
 *      - Sell order for an asset the organisation does not hold.
 *      - Asset being traded does not exist in the system.
 *      -
 */
public class TestOrders {
    User JohnDoe;
    Asset ABC;
    Asset XYZ;

    Order testOrder;
    BuyOrder testBuyOrder;
    SellOrder testSellOrder;

    TradingAppData i = new TradingAppData();

    public TestOrders() throws IllegalString, AlreadyExists, DoesNotExist {
        i.mockObjects();
        JohnDoe = new User("JohnDoe", "Password1234", false);
        i.addUser(JohnDoe);
        i.updateUserUnit("JohnDoe", "Developers");
        ABC = TradingAppData.assetDev1;
        XYZ = TradingAppData.assetDev2;
    }
    
    @BeforeEach
    public void setupOrders() throws InvalidAmount, OrderException, DoesNotExist {
        i.changeUnitBalance("Developers", 1000);
        i.setInventory(new InventoryRecord("Developers", ABC.getId(), 500));
        testOrder = new Order(JohnDoe, ABC, 5, 10);
        testBuyOrder = new BuyOrder(JohnDoe, ABC, 20, 13);
        testSellOrder = new SellOrder(JohnDoe, ABC, 6, 47);
        i.placeBuyOrder(testBuyOrder);
        i.placeSellOrder(testSellOrder);
    }

    @Test
    public void TestOrdersSetup() {
        assertAll(
                // Test that the parent class has been setup correctly
                () -> assertEquals("johndoe", testOrder.user),
                () -> assertEquals(ABC.getId(), testOrder.asset),
                () -> assertEquals(5, testOrder.qty),
                () -> assertEquals(10, testOrder.price),
                // Test that the Buy Order class has been setup correctly
                () -> assertEquals("johndoe", testBuyOrder.user),
                () -> assertEquals(ABC.getId(), testBuyOrder.asset),
                () -> assertEquals(20, testBuyOrder.qty),
                () -> assertEquals(13, testBuyOrder.price),
                // Test that the Sell Order class has been setup correctly
                () -> assertEquals("johndoe", testSellOrder.user),
                () -> assertEquals(ABC.getId(), testSellOrder.asset),
                () -> assertEquals(6, testSellOrder.qty),
                () -> assertEquals(47, testSellOrder.price));
    }

    /**
     * Test that when too many assets are attempted to be sold an exception is thrown.
     */
    @Test
    public void TestSellTooMany() {
        testSellOrder = new SellOrder(JohnDoe, ABC, 500, 10);
        assertThrows(OrderException.class, () -> {i.placeSellOrder(testSellOrder);});
    }

    /**
     * Test that when the total cost of a buy order exceeds the organisations credits an exception is thrown
     */
    @Test
    public void TestBuyTooExpensive() {
        testBuyOrder = new BuyOrder(JohnDoe, ABC, 5, 1000);
        assertThrows(OrderException.class, () -> {i.placeBuyOrder(testBuyOrder);});
    }

/*
    /**
     * Test that when the amount of assets being sold exceeds the quantity held by an organisation an exception is thrown.
     * /
    @Test
    public void TestBuyTooMany() {
        testBuyOrder = new BuyOrder(JohnDoe, ABC, 500, 10);
        assertThrows(OrderException.class, () -> {i.placeBuyOrder(testBuyOrder);});
    }
*/

    /*@Test
    public void TestAssetDoesNotExist() {
        testOrder = new Order(JohnDoe, XYZ, 5, 10);
        assertThrows();
    }*/


    /**
     * Test that upon deletion of trade it no longer exists in the database.
     */
    @Test
    public void TestDeleteBuy() throws DoesNotExist {
        i.cancelBuyOrder(testBuyOrder.id);
        assertThrows(DoesNotExist.class, ()-> i.getBuyByKey(testBuyOrder.id));
    }
    @Test
    public void TestDeleteSell() throws DoesNotExist, ConstraintException {
        i.cancelSellOrder(testSellOrder.id);
        assertThrows(DoesNotExist.class, ()-> i.getSellByKey(testSellOrder.id));
    }
}


