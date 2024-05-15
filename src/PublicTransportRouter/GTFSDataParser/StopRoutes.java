package src.PublicTransportRouter.GTFSDataParser;

import java.util.ArrayList;

public class StopRoutes {
    private ArrayList<String> routeIdList;    // The strings here are route IDs; stop IDs are in the relevant hashmap

    StopRoutes(ArrayList<String> routeIdList) {
        this.routeIdList = routeIdList;
    }

    StopRoutes() {
        this(new ArrayList<>());    // All values are initialized to nulls and zeroes
    }

    void setRouteIdList(ArrayList<String> routeList) {
        this.routeIdList = routeList;
    }

    ArrayList<String> getRouteIdList() {
        return this.routeIdList;
    }
}