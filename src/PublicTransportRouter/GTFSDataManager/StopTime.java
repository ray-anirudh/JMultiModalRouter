package src.PublicTransportRouter.GTFSDataManager;

import java.util.LinkedHashMap;

public class StopTime {
    private final LinkedHashMap<Integer, LinkedHashMap<Integer, StopTimeTriplet>> tripWiseStopTimeMaps;
    /* Integer keys refer to trip IDs within a route; overarching route IDs are in the pertinent hashmap
    Internal hashmap has actual stop IDs mapped to arrival and departure times, as well as stop sequences
    */

    StopTime(LinkedHashMap<Integer, LinkedHashMap<Integer, StopTimeTriplet>> tripWiseStopTimeMaps) {
        this.tripWiseStopTimeMaps = tripWiseStopTimeMaps;
    }

    StopTime() {
        this(new LinkedHashMap<>());    // All values are initialized to zeroes
    }

    public LinkedHashMap<Integer, LinkedHashMap<Integer, StopTimeTriplet>> getTripWiseStopTimeMaps() {
        return this.tripWiseStopTimeMaps;
    }
}