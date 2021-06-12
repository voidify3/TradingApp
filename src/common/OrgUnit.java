package common;

import common.Exceptions.*;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

public class OrgUnit extends DataObject implements Comparable<OrgUnit> {

    private final String orgName;
    private Integer orgCredits;
    static int minBalance = 0;

    /***
     * Constructor for creating new org units with default credits of 0.
     * @param orgName the name of the org unit
     */
    public OrgUnit(String orgName) throws IllegalString {
        this(orgName, minBalance);
    }

    /**
     * Constructor which includes the credits number
     * @param orgName the name
     * @param credits the credits value found
     */
    public OrgUnit(String orgName, int credits) throws IllegalString {
        if (orgName.length() > 30) throw new IllegalString("'%s' exceeds the maximum organisational unit" +
                "name length of 30. Please try again.", orgName);
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
     * Setter method for the org unit's credit balance.
     * @param amount to set
     */
    public void setCredits(Integer amount) {
            this.orgCredits = amount;
    }
    //No need to update the database, this changes an OBJECT which is SENT

    /***
     * Method to set an unit's balance.
     * @param amount to set
     * @throws InvalidAmount if the amount to set is less than 0
     */
    public void setBalance(int amount) throws InvalidAmount {
        if (amount >= minBalance) {
            this.setCredits(amount);
        } else {
            throw new InvalidAmount("Cannot set balance to less than 0", amount);
        }
    }

    /***
     * Method to adjust a unit's balance.
     * @param amount amount to add
     * @throws InvalidAmount if the amount to add will take the balance to less than 0
     */
    public void adjustBalance(int amount) throws InvalidAmount {
        setCredits(orgCredits + amount);
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


