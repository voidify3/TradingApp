package common;

import common.Exceptions.IllegalString;
import common.Exceptions.InvalidAmount;

import java.time.LocalDateTime;

public class DataObjectFactory {
    public static Asset newAssetNaive(int id, String description) {
        return new Asset(id, description);
    }
    public static Asset newAssetValidated(int id, String description) throws IllegalString {
        if (description.length() > 60) {
            throw new IllegalString("Asset description exceeds the maximum length of 60. Please try again");
        } else if (description.trim().equals("")) {
            throw new IllegalString("Asset description should not be empty or all whitespace");
        }
        return newAssetNaive(id, description);
    }
    public static Asset newAssetValidated(String description) throws IllegalString {
        return newAssetValidated(0, description);
    }
    public static OrgUnit newOrgUnitNaive(String orgName, int orgCredits) {
        return new OrgUnit(orgName,orgCredits);
    }
    public static OrgUnit newOrgUnitValidated(String orgName, int orgCredits) throws IllegalString, InvalidAmount {
        OrgUnit o = newOrgUnitValidated(orgName);
        o.setBalance(orgCredits);
        return o;
    }
    public static OrgUnit newOrgUnitValidated(String orgName) throws IllegalString {
        if (orgName.trim().equals("")) throw new IllegalString("Unit name may not be empty or all whitespace");
        if (orgName.length() > 30) throw new IllegalString("'%s' exceeds the maximum organisational unit" +
                "name length of 30. Please try again.", orgName);
        return newOrgUnitNaive(orgName, OrgUnit.minBalance);
    }
    private static void validateOrderParameters(int qty, int price) throws InvalidAmount {
        if (qty <= 0) throw new InvalidAmount("Quantity %d is invalid; must be greater than 0", qty);
        if (price <= 0) throw new InvalidAmount("Price %d is invalid; must be greater than 0", price);
    }
    public static BuyOrder newBuyNaive(int id, String user, int asset, int qty, int price, LocalDateTime placed,
                                       LocalDateTime resolved, Integer boughtFrom) {
        return new BuyOrder(id,user,asset,qty,price,placed,resolved,boughtFrom);
    }
    public static SellOrder newSellNaive(int id, String user, int asset, int qty, int price, LocalDateTime placed,
                                       LocalDateTime resolved) {
        return new SellOrder(id,user,asset,qty,price,placed,resolved);
    }
    public static BuyOrder newBuyValidated(String unit, int asset, int qty, int price) throws InvalidAmount {
        validateOrderParameters(qty, price);
        return new BuyOrder(unit, asset, qty, price);
    }
    public static SellOrder newSellValidated(String unit, int asset, int qty, int price) throws InvalidAmount {
        validateOrderParameters(qty, price);
        return new SellOrder(unit, asset, qty, price);
    }
    public static Order newOrderValidated(boolean isBuy, String unit, int asset, int qty, int price) throws InvalidAmount {
        if (isBuy) return newBuyValidated(unit, asset, qty, price);
        else return newSellValidated(unit, asset, qty, price);
    }
}
