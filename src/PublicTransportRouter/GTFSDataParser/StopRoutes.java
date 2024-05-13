package src.PublicTransportRouter.GTFSDataParser;

import java.util.ArrayList;

public class StopRoutes {
    private ArrayList<Route> routeList;

    StopRoutes(ArrayList<Route> routeList) {
        this.routeList = routeList;
    }

    StopRoutes() {
        this(new ArrayList<>());
    }

    void setRouteList(ArrayList<Route> routeList) {
        this.routeList = routeList;
    }

    ArrayList<Route> getRouteList() {
        return this.routeList;
    }
}