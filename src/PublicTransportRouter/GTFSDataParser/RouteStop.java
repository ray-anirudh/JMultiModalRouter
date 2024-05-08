package src.PublicTransportRouter.GTFSDataParser;

import java.util.HashMap;

public class RouteStop {
    private HashMap<Integer, String> stopSequenceMap;

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