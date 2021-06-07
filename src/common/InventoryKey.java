package common;


import java.io.Serializable;

/**
 * Class to hold the composite key of an InventoryObject
 */
public class InventoryKey implements Serializable {
    public String unit;
    public int asset;

    public InventoryKey(String unit, int asset) {
        this.unit = unit;
        this.asset = asset;
    }

    public String getUnit() {
        return this.unit;
    }

    public int getAsset() {
        return this.asset;
    }
}
