package src.PublicTransportRouter.GTFSDataManager;

import java.util.HashMap;

public class StopTime {
    private final HashMap<Integer, HashMap<Integer, StopTimeTriplet>> tripWiseStopTimeMaps;
    /* Integer keys refer to trip IDs within a route; overarching route IDs are in the pertinent hashmap
    Internal hashmap has actual stop IDs mapped to arrival and departure times, as well as stop sequences
    */

    StopTime(HashMap<Integer, HashMap<Integer, StopTimeTriplet>> tripWiseStopTimeMaps) {
        this.tripWiseStopTimeMaps = tripWiseStopTimeMaps;
    }

    StopTime() {
        this(new HashMap<>());    // All values are initialized to zeroes
    }

    public HashMap<Integer, HashMap<Integer, StopTimeTriplet>> getTripWiseStopTimeMaps() {
        return this.tripWiseStopTimeMaps;
    }
}