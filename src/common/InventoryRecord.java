package common;

public class InventoryRecord extends DataObject {
    private final InventoryKey key;
    private int quantity;

    public InventoryRecord(String orgUnitName, int assetID, int quantity) {
        this.key = new InventoryKey(orgUnitName,assetID);
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getUnitName() {
        return key.getUnit();
    }

    public int getAssetID() {
        return key.getAsset();
    }
}
