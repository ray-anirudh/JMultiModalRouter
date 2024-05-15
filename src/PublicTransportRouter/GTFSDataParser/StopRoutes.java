package src.PublicTransportRouter.GTFSDataParser;

import java.util.ArrayList;

public class StopRoutes {
    private ArrayList<String> routeIdList;    // The strings here refer to route IDs

    StopRoutes(ArrayList<String> routeIdList) {
        this.routeIdList = routeIdList;
    }

    StopRoutes() {
        this(new ArrayList<>());
    }

    void setRouteIdList(ArrayList<String> routeList) {
        this.routeIdList = routeList;
    }

    ArrayList<String> getRouteIdList() {
        return this.routeIdList;
    }
}