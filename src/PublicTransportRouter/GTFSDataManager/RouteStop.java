package src.PublicTransportRouter.GTFSDataManager;

import java.util.HashMap;

public class RouteStop {
    private HashMap<Integer, HashMap<Integer, Integer>> directionWiseStopSequenceMap;
    /* External keys refer to route directions, internal keys refer to stop IDs, and internal values refer to stop
    sequences
    */
    RouteStop(HashMap<Integer, HashMap<Integer, Integer>> directionWiseStopSequenceMap) {
        this.directionWiseStopSequenceMap = directionWiseStopSequenceMap;
    }

    RouteStop() {
        this(new HashMap<>());  // All values are initialized to zeroes
    }

    public HashMap<Integer, HashMap<Integer, Integer>> getDirectionWiseStopSequenceMap() {
        return this.directionWiseStopSequenceMap;
    }
}