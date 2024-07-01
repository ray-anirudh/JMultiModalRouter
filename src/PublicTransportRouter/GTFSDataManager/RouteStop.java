package src.PublicTransportRouter.GTFSDataManager;

import java.util.LinkedHashMap;

public class RouteStop {
    private final LinkedHashMap<Integer, LinkedHashMap<Integer, Integer>> directionWiseStopMaps;
    // External keys refer to route directions, internal keys refer to stop IDs, and internal values to stop sequences

    RouteStop(LinkedHashMap<Integer, LinkedHashMap<Integer, Integer>> directionWiseStopMaps) {
        this.directionWiseStopMaps = directionWiseStopMaps;
    }

    RouteStop() {
        this(new LinkedHashMap<>());  // All values are initialized to zeroes
    }

    public LinkedHashMap<Integer, LinkedHashMap<Integer, Integer>> getDirectionWiseStopMaps() {
        return this.directionWiseStopMaps;
    }
}