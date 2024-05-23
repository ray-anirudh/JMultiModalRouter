package src.PublicTransportRouter.GTFSDataManager;

import java.util.ArrayList;
import java.util.HashMap;

public class StopTime {
    private HashMap<String, ArrayList<StopTimeQuartet>> tripWiseStopTimeLists;
    /* String keys refer to a trip IDs within a route; routes are in the pertinent hashmap
    ArrayList has actual stop times
    */

    StopTime(HashMap<String, ArrayList<StopTimeQuartet>> tripWiseStopTimeLists) {
        this.tripWiseStopTimeLists = tripWiseStopTimeLists;
    }

    StopTime() {
        this(new HashMap<>());    // All values are initialized to nulls and zeroes
    }

    public HashMap<String, ArrayList<StopTimeQuartet>> getTripWiseStopTimeLists() {
        return this.tripWiseStopTimeLists;
    }
}