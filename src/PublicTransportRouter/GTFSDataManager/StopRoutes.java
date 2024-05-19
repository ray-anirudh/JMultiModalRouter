package src.PublicTransportRouter.GTFSDataManager;

import java.util.ArrayList;

public class StopRoutes {
    private ArrayList<String> routeIdList;    // Strings are route IDs; stop IDs are in the relevant hashmap

    StopRoutes(ArrayList<String> routeIdList) {
        this.routeIdList = routeIdList;
    }

    StopRoutes() {
        this(new ArrayList<>());    // All values are initialized to nulls and zeroes
    }

    void setRouteIdList(ArrayList<String> routeList) {
        this.routeIdList = routeList;
    }

    public ArrayList<String> getRouteIdList() {
        return this.routeIdList;
    }
}