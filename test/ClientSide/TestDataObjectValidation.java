package ClientSide;
import common.*;
import common.Exceptions.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for data object validation in DataObjectFactory
 */
public class TestDataObjectValidation {
    String lengthFiftyNine = "abcdefjhijklmnopqrstuvwxyzabcdefjhijklmnopqrstuvwxyz1234567";
    String lengthTwentyNine = "abcdefjhijklmnopqrstuvwxyz123";
    @Test
    void testValidAsset() {
        assertDoesNotThrow(()->{
            //test that an asset can have a description of length 1
            Asset testAsset=DataObjectFactory.newAssetValidated("a");
            assertEquals("a", testAsset.getDescription());
            //test that an asset can have a description of length 59
            testAsset = DataObjectFactory.newAssetValidated(
                    lengthFiftyNine);
            assertEquals(lengthFiftyNine, testAsset.getDescription());
        });
    }
    @Test
    void testInvalidAsset() {
        assertAll(
                ()->assertThrows(IllegalString.class, ()->{
                    Asset testAsset=DataObjectFactory.newAssetValidated("");
                }),
                ()->assertThrows(IllegalString.class, ()->{
                    Asset testAsset=DataObjectFactory.newAssetValidated(lengthFiftyNine + "8");
                })
        );
    }
    //TODO:
    // -valid unit
    // -invalid unit
    // -valid orders
    // -invalid orders
}
