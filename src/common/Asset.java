package common;

import common.Exceptions.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/***
 *
 * @author Scott Peachey
 */
public class Asset extends DataObject {
    // INSTANCE VARIABLES-----------------------------------------------------------------------------------------------
    private final int id;
    private String description;

    // CONSTRUCTORS------------------------------------------------------------------------------------------------------
    /***
     * Asset Constructor
     * @param assetID the asset's unique ID
     * @param description the asset's description
     */
    public Asset(int assetID, String description) {
        // Store the asset ID locally
        this.id = assetID;

        // Store the asset description locally
        this.description = description;
    }
    // GETTERS & SETTERS------------------------------------------------------------------------------------------------
    /***
     * Method used to get the assets description
     * @return assetDesc
     */
    public String getDescription() { return description; }
    /***
     * Method used to get the assets ID
     * @return assetID
     */
    public int getId() { return id; }

    /***
     * Method used to get the assets ID as a string
     * @return String
     */
    public String getIdString() {
        return Integer.toString(this.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Asset)) return false;
        Asset asset = (Asset) o;
        return id == asset.id && description.equals(asset.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, description);
    }
}


