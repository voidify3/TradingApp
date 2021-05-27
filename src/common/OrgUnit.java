package common;

import common.Exceptions.*;
import java.util.HashMap;
import java.util.Set;

public class OrgUnit extends DataObject {

    private String orgName;
    private Integer orgCredits;
    static int minBalance = 0;

    /***
     * Constructor for creating new org units.
     * @param orgName the name of the org unit
     */
    public OrgUnit(String orgName) {
        this.orgName = orgName;
        this.orgCredits = 0;
    }

    /**
     * Constructor for org units retrieved from the database.
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
     * Setter method for the org unit name.
     * (for future changes / typo during during creation).
     * @param newName new org unit name as a string
     */
    public void setName(String newName) {
        this.orgName = newName;
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


}


