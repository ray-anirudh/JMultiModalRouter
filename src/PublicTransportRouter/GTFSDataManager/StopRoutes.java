package src.PublicTransportRouter.GTFSDataManager;

import java.util.ArrayList;

public class StopRoutes {
    private ArrayList<Integer> routeIdList;    // Integer elements are route IDs; stop IDs are in the relevant hashmap

    StopRoutes(ArrayList<Integer> routeIdList) {
        this.routeIdList = routeIdList;
    }

    StopRoutes() {
        this(new ArrayList<>());    // All values are initialized to zeroes
    }

    public ArrayList<Integer> getRouteIdList() {
        return this.routeIdList;
    }
}