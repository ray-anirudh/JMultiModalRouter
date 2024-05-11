package src.PublicTransportRouter.GTFSDataParser;

public class Stop {
    private String stopId;
    private double stopLatitude;
    private double stopLongitude;

    Stop(String stopId, double stopLatitude, double stopLongitude) {
        this.stopId = stopId;
        this.stopLatitude = stopLatitude;
        this.stopLongitude = stopLongitude;
    }

    void setStopId(String stopId) {
        this.stopId = stopId;
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

    double getStopLatitude() {
        return stopLatitude;
    }

    double getStopLongitude() {
        return stopLongitude;
    }
}