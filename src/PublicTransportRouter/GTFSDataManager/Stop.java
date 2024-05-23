package src.PublicTransportRouter.GTFSDataManager;

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