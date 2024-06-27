package src.PublicTransportRouter.GTFSDataManager;

public class Stop {
    private String stopName;    // Stop IDs are contained in the pertinent hashmap
    private int stopType;
    // Ascribed via route type information (Refer to: https://gtfs.org/de/schedule/reference/#routestxt)
    private int stopTripCount;     // Number of trips served per day via the stop
    private double stopLatitude;
    private double stopLongitude;

    Stop(String stopName, int stopType, int stopTripCount, double stopLatitude, double stopLongitude) {
        this.stopName = stopName;
        this.stopType = stopType;
        this.stopTripCount = stopTripCount;
        this.stopLatitude = stopLatitude;
        this.stopLongitude = stopLongitude;
    }

    Stop() {
        this(null, 0, 0, 0D, 0D);
    }

    void setStopType(int stopType) {
        this.stopType = stopType;
    }

    void setStopTripCount(int stopTripCount) {
        this.stopTripCount = stopTripCount;
    }

    public String getStopName() {
        return this.stopName;
    }

    int getStopType() {
        return this.stopType;
    }

    int getStopTripCount() {
        return this.stopTripCount;
    }

    double getStopLatitude() {
        return this.stopLatitude;
    }

    double getStopLongitude() {
        return this.stopLongitude;
    }
}