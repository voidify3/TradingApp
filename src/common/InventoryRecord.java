package common;

import java.util.Objects;

/**
 * Models a record of Inventories table
 */
public class InventoryRecord extends DataObject {
    private final String unit;
    private final int asset;
    private int quantity;

    public InventoryRecord(String orgUnitName, int assetID, int quantity) {
        this.unit = orgUnitName;
        this.asset = assetID;
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void adjustQuantity(int adjustment) {
        this.quantity+=adjustment;
        //no check for negative because negative is used in higher level adjustment
    }

    public String getUnitName() {
        return unit;
    }

    public int getAssetID() {
        return asset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InventoryRecord)) return false;
        InventoryRecord that = (InventoryRecord) o;
        return asset == that.asset && quantity == that.quantity && unit.equals(that.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unit, asset, quantity);
    }
}
