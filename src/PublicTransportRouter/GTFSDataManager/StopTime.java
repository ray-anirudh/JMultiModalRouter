package src.PublicTransportRouter.GTFSDataManager;

import java.util.ArrayList;
import java.util.HashMap;

public class StopTime {
    private HashMap<String, ArrayList<StopTimeQuartet>> tripWiseStopTimeLists;
    /* String keys refer to a trip IDs within a route; routes are in the pertinent hashmap
    ArrayList has actual stop times
    Conversion to sets or linked lists for faster iteration may be considered later
    */

    StopTime(HashMap<String, ArrayList<StopTimeQuartet>> tripWiseStopTimeLists) {
        this.tripWiseStopTimeLists = tripWiseStopTimeLists;
    }

    StopTime() {
        this(new HashMap<>());    // All values are initialized to nulls and zeroes
    }

    void setTripWiseStopTimeList(HashMap<String, ArrayList<StopTimeQuartet>> tripWiseStopTimeLists) {
        this.tripWiseStopTimeLists = tripWiseStopTimeLists;
    }

    HashMap<String, ArrayList<StopTimeQuartet>> getTripWiseStopTimeLists() {
        return this.tripWiseStopTimeLists;
    }
}