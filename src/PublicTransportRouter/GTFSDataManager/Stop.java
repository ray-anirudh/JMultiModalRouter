package src.PublicTransportRouter.GTFSDataManager;

public class Stop {
    private String stopName;    // Stop IDs are contained in the pertinent hashmap
    private int stopType;
    private double stopLatitude;
    private double stopLongitude;

    Stop(String stopName, int stopType, double stopLatitude, double stopLongitude) {
        this.stopName = stopName;
        this.stopType = stopType;
        this.stopLatitude = stopLatitude;
        this.stopLongitude = stopLongitude;
    }

    Stop() {
        this(null, 0, 0d, 0d);
    }

    String getStopName() {
        return this.stopName;
    }

    int getStopType() {
        return this.stopType;
    }

    double getStopLatitude() {
        return stopLatitude;
    }

    double getStopLongitude() {
        return stopLongitude;
    }
}