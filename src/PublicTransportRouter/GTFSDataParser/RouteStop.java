package src.PublicTransportRouter.GTFSDataParser;

import java.util.HashMap;

public class RouteStop {
    private HashMap<Integer, String> stopSequenceMap;   // The strings here refer to stop IDs, and the integers refer to
                                                        // stop sequences

    RouteStop(HashMap<Integer, String> stopSequenceMap) {
        this.stopSequenceMap = stopSequenceMap;
    }

    RouteStop() {
        this(new HashMap<>());
    }

    void setStopSequenceMap(HashMap<Integer, String> stopSequenceMap) {
        this.stopSequenceMap = stopSequenceMap;
    }

    HashMap<Integer, String> getStopSequenceMap() {
        return this.stopSequenceMap;
    }
}