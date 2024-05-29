package src.PublicTransportRouter.GTFSDataManager;

public class Route {    // Route IDs are present in the pertinent hashmap
    private int numberTrips;
    private int numberStops;
    // Maximum number of stops on the route, from amongst all trips operating on the route
    private int routeType;
    // Refer to: https://gtfs.org/de/schedule/reference/#routestxt

    Route(int numberTrips, int numberStops, int routeType) {
        this.numberTrips = numberTrips;
        this.numberStops = numberStops;
        this.routeType = routeType;
    }

    Route() {
        this(0, 0, 0);
    }

    void setNumberTrips(int numberTrips) {
        this.numberTrips = numberTrips;
    }

    void setNumberStops(int numberStops) {
        this.numberStops = numberStops;
    }

    void setRouteType(int routeType) {
        this.routeType = routeType;
    }

    int getNumberTrips() {
        return this.numberTrips;
    }

    int getNumberStops() {
        return this.numberStops;
    }

    int getRouteType() {
        return this.routeType;
    }
}