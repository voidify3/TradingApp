package ClientSide;
import common.*;
import common.Exceptions.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for data object validation in DataObjectFactory
 */
public class TestDataObjectValidation {
    String lengthSixty = "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz12345678";
    String lengthThirty = "abcdefghijklmnopqrstuvwxyz1234";
    @Test
    void testValidAsset() {
        assertDoesNotThrow(()->{
            //test that an asset can have a description of length 1
            Asset testAsset=DataObjectFactory.newAssetValidated("a");
            assertEquals("a", testAsset.getDescription());
            assertEquals(0, testAsset.getId());
            //test that an asset can have a description of length 60
            testAsset = DataObjectFactory.newAssetValidated(
                    lengthSixty);
            assertEquals(lengthSixty, testAsset.getDescription());
            assertEquals(0, testAsset.getId());
            //this also tests that it defaults to id 0
        });
    }
    @Test
    void testInvalidAsset() {
        assertAll(
                //an asset can't have an empty description
                ()->assertThrows(IllegalString.class, ()->{
                    Asset testAsset=DataObjectFactory.newAssetValidated("");
                }),
                //or one consisting entirely of whitespace
                ()->assertThrows(IllegalString.class, ()->{
                    Asset testAsset=DataObjectFactory.newAssetValidated(" ");
                }),
                //or one of 61 or more characters
                ()->assertThrows(IllegalString.class, ()->{
                    Asset testAsset=DataObjectFactory.newAssetValidated(lengthSixty + "9");
                })
        );
    }
    @Test
    void testValidUnit() {
        assertDoesNotThrow(()->{
            //test that a unit can have a name of length 1
            OrgUnit testUnit =DataObjectFactory.newOrgUnitValidated("a");
            assertEquals("a", testUnit.getName());
            assertEquals(0, testUnit.getCredits());
            //test that a unit can have a name of length 30
            testUnit = DataObjectFactory.newOrgUnitValidated(
                    lengthThirty);
            assertEquals(lengthThirty, testUnit.getName());
            assertEquals(0, testUnit.getCredits());
            //this also tests that it defaults to 0 credits
        });
    }
    @Test
    void testInvalidUnit() {
        assertAll(
                ()->assertThrows(IllegalString.class, ()->{
                    OrgUnit testUnit=DataObjectFactory.newOrgUnitValidated("");
                }),
                ()->assertThrows(IllegalString.class, ()->{
                    OrgUnit testUnit=DataObjectFactory.newOrgUnitValidated(" ");
                }),
                ()->assertThrows(InvalidAmount.class, ()->{
                    OrgUnit testUnit=DataObjectFactory.newOrgUnitValidated("a", -1);
                }),
                ()->assertThrows(IllegalString.class, ()->{
                    OrgUnit testUnit=DataObjectFactory.newOrgUnitValidated(lengthThirty + "9");
                })
        );
    }
    @Test
    void testValidOrders() {
        assertDoesNotThrow(()->{
            common.Order o = DataObjectFactory.newOrderValidated(true, "a", 1,
                    1, 1);
            assertTrue(o instanceof BuyOrder);
            o = DataObjectFactory.newOrderValidated(false, "a", 1,
                    1, 1);
            assertTrue(o instanceof SellOrder);
        });
    }
    @Test
    void testInvalidOrders() {
        assertAll(
                ()->assertThrows(InvalidAmount.class, ()->{
                    BuyOrder o = (BuyOrder) DataObjectFactory.newOrderValidated(true, "a", 1,
                            0, 1);
                }),
                ()->assertThrows(InvalidAmount.class, ()->{
                    BuyOrder o = (BuyOrder) DataObjectFactory.newOrderValidated(true, "a", 1,
                            1, 0);
                }),
                ()->assertThrows(InvalidAmount.class, ()->{
                    SellOrder o = (SellOrder) DataObjectFactory.newOrderValidated(false, "a", 1,
                            0, 1);
                }),
                ()->assertThrows(InvalidAmount.class, ()->{
                    SellOrder o = (SellOrder) DataObjectFactory.newOrderValidated(false, "a", 1,
                            1,0);
                })
        );
    }
}
