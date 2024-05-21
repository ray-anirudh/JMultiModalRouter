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

    void setStopSequence(int stopSequence) {
        this.stopSequence = stopSequence;
    }

    void setStopId(String stopId) {
        this.stopId = stopId;
    }

    void setArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    void setDepartureTime(int departureTime) {
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