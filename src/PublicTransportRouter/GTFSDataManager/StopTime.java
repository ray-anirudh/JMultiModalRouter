package src.PublicTransportRouter.GTFSDataManager;

import java.util.HashMap;

public class StopTime {
    private HashMap<Integer, HashMap<Integer, StopTimeTriplet>> tripWiseStopTimeLists;
    /* Integer keys refer to trip IDs within a route; overarching route IDs are in the pertinent hashmap
    Internal hashmap has actual stop IDs mapped to arrival and departure times, as well as stop sequences
    */

    StopTime(HashMap<Integer, HashMap<Integer, StopTimeTriplet>> tripWiseStopTimeLists) {
        this.tripWiseStopTimeLists = tripWiseStopTimeLists;
    }

    StopTime() {
        this(new HashMap<>());    // All values are initialized to zeroes
    }

    public HashMap<Integer, HashMap<Integer, StopTimeTriplet>> getTripWiseStopTimeLists() {
        return this.tripWiseStopTimeLists;
    }
}