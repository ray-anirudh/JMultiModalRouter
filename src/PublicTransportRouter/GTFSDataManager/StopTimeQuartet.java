package src.PublicTransportRouter.GTFSDataManager;

public class StopTimeQuartet {
    private int stopSequence;
    private String stopId;
    private int arrivalTime;
    private int departureTime;

    StopTimeQuartet(int stopSequence, String stopId, int arrivalTime, int departureTime) {
        this.stopSequence = stopSequence;
        this.stopId = stopId;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
    }

    int getStopSequence() {
        return this.stopSequence;
    }

    public String getStopId() {
        return this.stopId;
    }

    public int getArrivalTime() {
        return this.arrivalTime;
    }

    public int getDepartureTime() {
        return this.departureTime;
    }
}