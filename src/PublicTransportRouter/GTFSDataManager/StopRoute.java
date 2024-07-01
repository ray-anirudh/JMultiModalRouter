package src.PublicTransportRouter.GTFSDataManager;

import java.util.ArrayList;

public class StopRoute {
    private final ArrayList<Integer> routeList;
    // Integer elements are route IDs serving the stop; stop IDs are in the relevant hashmap

    StopRoute() {
        this.routeList = new ArrayList<>();    // All values are initialized to zeroes
    }

    public ArrayList<Integer> getRouteList() {
        return this.routeList;
    }
}