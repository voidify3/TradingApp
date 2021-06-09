package common;

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
}
