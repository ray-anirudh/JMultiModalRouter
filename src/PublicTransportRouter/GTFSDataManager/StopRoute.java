package src.PublicTransportRouter.GTFSDataManager;

import java.util.ArrayList;

public class StopRoute {
    private ArrayList<Integer> routeIdList;    // Integer elements are route IDs; stop IDs are in the relevant hashmap

    StopRoute(ArrayList<Integer> routeIdList) {
        this.routeIdList = routeIdList;
    }

    StopRoute() {
        this(new ArrayList<>());    // All values are initialized to zeroes
    }

    public ArrayList<Integer> getRouteIdList() {
        return this.routeIdList;
    }
}