package src.PublicTransportRouter.GTFSDataParser;

public class Route {    // Route IDs are captured in the pertinent hashmap
    private int numberTrips;
    private int numberStops;
    private int routeType;

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