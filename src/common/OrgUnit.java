package common;

import common.Exceptions.*;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

/**
 * Models a record of orgunit table
 */
public class OrgUnit extends DataObject implements Comparable<OrgUnit> {

    private final String orgName;
    private Integer orgCredits;
    static int minBalance = 0;

    /**
     * Constructor which includes the credits number
     * @param orgName the name
     * @param credits the credits value found
     */
    public OrgUnit(String orgName, int credits) {
        this.orgName = orgName;
        this.orgCredits = credits;
    }

    /***
     * Getter method for the org unit's name.
     * @return
     */
    public String getName() {
        return orgName;
    }
    /***
     * Getter method for the org unit's credits balance.
     * @return credit balance
     */
    public Integer getCredits() {
        return this.orgCredits;
    }

    /***
     * Private setter method for the org unit's credit balance.
     * @param amount to set
     */
    private void setCredits(Integer amount) {
            this.orgCredits = amount;
    }

    /***
     * Non-straightforward method to set an unit's balance.
     * @param amount to set
     * @throws InvalidAmount if the amount to set is less than 0
     */
    public void setBalance(int amount) throws InvalidAmount {
        if (amount >= minBalance) {
            this.setCredits(amount);
        } else {
            throw new InvalidAmount("Cannot set balance to less than "+minBalance, amount);
        }
    }

    /***
     * Method to adjust a unit's balance.
     * @param amount amount to add
     * @throws InvalidAmount if the amount to add will take the balance to less than 0
     */
    public void adjustBalance(int amount) throws InvalidAmount {
        setBalance(orgCredits + amount);
    }


    @Override
    public int compareTo(OrgUnit o) {
        return this.orgName.compareTo(o.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrgUnit)) return false;
        OrgUnit orgUnit = (OrgUnit) o;
        return orgName.equals(orgUnit.orgName) && orgCredits.equals(orgUnit.orgCredits);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orgName, orgCredits);
    }
}


