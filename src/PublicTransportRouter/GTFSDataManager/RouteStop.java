package src.PublicTransportRouter.GTFSDataManager;

import java.util.HashMap;

public class RouteStop {
    private HashMap<Integer, String> stopSequenceMap;
    /* The strings here refer to stop IDs, and the integers refer to stop sequences, route IDs are in the relevant
    hashmap
    */

    RouteStop(HashMap<Integer, String> stopSequenceMap) {
        this.stopSequenceMap = stopSequenceMap;
    }

    RouteStop() {
        this(new HashMap<>());  // All values are initialized to nulls and zeroes
    }

    void setStopSequenceMap(HashMap<Integer, String> stopSequenceMap) {
        this.stopSequenceMap = stopSequenceMap;
    }

    public HashMap<Integer, String> getStopSequenceMap() {
        return this.stopSequenceMap;
    }
}