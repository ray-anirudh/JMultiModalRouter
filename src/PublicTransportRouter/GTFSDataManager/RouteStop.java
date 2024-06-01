package src.PublicTransportRouter.GTFSDataManager;

import java.util.LinkedHashMap;

public class RouteStop {
    private final LinkedHashMap<Integer, LinkedHashMap<Integer, Integer>> directionWiseStopSequenceMap;
    // External keys refer to route directions, internal keys refer to stop IDs, and internal values to stop sequences

    RouteStop(LinkedHashMap<Integer, LinkedHashMap<Integer, Integer>> directionWiseStopSequenceMap) {
        this.directionWiseStopSequenceMap = directionWiseStopSequenceMap;
    }

    RouteStop() {
        this(new LinkedHashMap<>());  // All values are initialized to zeroes
    }

    public LinkedHashMap<Integer, LinkedHashMap<Integer, Integer>> getDirectionWiseStopSequenceMap() {
        return this.directionWiseStopSequenceMap;
    }
}