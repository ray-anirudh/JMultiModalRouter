package src.PublicTransportRouter.GTFSDataParser;

public class Route {
    private int numberTrips;
    private int numberStops;

    Route(int numberTrips, int numberStops) {
        this.numberTrips = numberTrips;
        this.numberStops = numberStops;
    }

    Route() {
        this(0, 0);     // Akin to default constructor
    }

    void setNumberTrips (int numberTrips) {
        this.numberTrips = numberTrips;
    }

    void setNumberStops (int numberStops) {
        this.numberStops = numberStops;
    }

    int getNumberTrips() {
        return this.numberTrips;
    }

    int getNumberStops() {
        return this.numberStops;
    }
}