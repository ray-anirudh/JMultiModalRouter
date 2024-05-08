package src.PublicTransportRouter.GTFSDataParser;

public class Stop {
    private String stopId;
    private int latitude;
    private int longitude;

    Stop(String stopId, int latitude, int longitude) {
        this.stopId = stopId;
    }

    void setStopId(String stopId) {
        this.stopId = stopId;
    }

    String getStopId() {
        return this.stopId;
    }
}