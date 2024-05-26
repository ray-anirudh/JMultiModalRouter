package src.PublicTransportRouter.GTFSDataManager;

import java.util.HashMap;

public class RouteStop {
    private HashMap<Integer, Integer> stopSequenceMap;
    // The keys here refer to stop IDs, and the values refer to stop sequences; route IDs are in the relevant hashmap

    RouteStop(HashMap<Integer, Integer> stopSequenceMap) {
        this.stopSequenceMap = stopSequenceMap;
    }

    RouteStop() {
        this(new HashMap<>());  // All values are initialized to zeroes
    }

    public HashMap<Integer, Integer> getStopSequenceMap() {
        return this.stopSequenceMap;
    }
}