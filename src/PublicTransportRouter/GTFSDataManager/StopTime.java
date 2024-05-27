package src.PublicTransportRouter.GTFSDataManager;

import java.util.ArrayList;
import java.util.HashMap;

public class StopTime {
    private HashMap<Integer, ArrayList<StopTimeQuartet>> tripWiseStopTimeLists;
    /* Integer keys refer to trip IDs within a route; overarching route IDs are in the pertinent hashmap
    ArrayList has actual stop times
    */

    StopTime(HashMap<Integer, ArrayList<StopTimeQuartet>> tripWiseStopTimeLists) {
        this.tripWiseStopTimeLists = tripWiseStopTimeLists;
    }

    StopTime() {
        this(new HashMap<>());    // All values are initialized to zeroes
    }

    public HashMap<Integer, ArrayList<StopTimeQuartet>> getTripWiseStopTimeLists() {
        return this.tripWiseStopTimeLists;
    }
}