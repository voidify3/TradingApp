package common;

import common.Exceptions.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/***
 * @author Scott Peachey
 */
public class Asset extends DataObject {
    // INSTANCE VARIABLES-----------------------------------------------------------------------------------------------
    private final int id;
    private final String description;

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

    public Asset(String description) {
        this.id=0;
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
        Integer id = this.id;
        String idString = id.toString();
        return idString;
    }
}


