package src.PublicTransportRouter.GTFSDataParser;

public class Stop {
    private String stopId;  // Possibly redundant attribute, owing to twinning with pertinent hashmap's keys
    private String stopName;
    private int stopType;
    private double stopLatitude;
    private double stopLongitude;

    Stop(String stopId, String stopName, int stopType, double stopLatitude, double stopLongitude) {
        this.stopId = stopId;
        this.stopName = stopName;
        this.stopType = stopType;
        this.stopLatitude = stopLatitude;
        this.stopLongitude = stopLongitude;
    }

    Stop() {
        this(null, null, 0, 0d, 0d);
    }

    void setStopId(String stopId) {
        this.stopId = stopId;
    }

    void setStopName(String stopName) {
        this.stopName = stopName;
    }

    void setStopType(int stopType) {
        this.stopType = stopType;
    }

    void setStopLatitude(double stopLatitude) {
        this.stopLatitude = stopLatitude;
    }

    void setStopLongitude(double stopLongitude) {
        this.stopLongitude = stopLongitude;
    }

    String getStopId() {
        return this.stopId;
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