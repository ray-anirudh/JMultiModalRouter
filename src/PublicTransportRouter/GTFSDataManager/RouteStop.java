package src.PublicTransportRouter.GTFSDataManager;

import java.util.HashMap;

public class RouteStop {
    private HashMap<String, Integer> stopSequenceMap;
    /* The strings here refer to stop IDs, and the integers refer to stop sequences, route IDs are in the relevant
    hashmap
    */

    RouteStop(HashMap<String, Integer> stopSequenceMap) {
        this.stopSequenceMap = stopSequenceMap;
    }

    RouteStop() {
        this(new HashMap<>());  // All values are initialized to nulls and zeroes
    }

    void setStopSequenceMap(HashMap<String, Integer> stopSequenceMap) {
        this.stopSequenceMap = stopSequenceMap;
    }

    public HashMap<String, Integer> getStopSequenceMap() {
        return this.stopSequenceMap;
    }
}