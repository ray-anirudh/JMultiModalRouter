package src.PublicTransportRouter.GTFSDataParser;

class StopTimeQuartet {
    private String stopId;
    private int stopSequence;
    private int arrivalTime;
    private int departureTime;

    StopTimeQuartet(String stopId, int stopSequence, int arrivalTime, int departureTime) {
        this.stopId = stopId;
        this.stopSequence = stopSequence;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
    }

    void setStopId(String stopId) {
        this.stopId = stopId;
    }

    void setStopSequence(int stopSequence) {
        this.stopSequence = stopSequence;
    }

    void setArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    void setDepartureTime(int departureTime) {
        this.departureTime = departureTime;
    }

    String getStopId() {
        return this.stopId;
    }

    int getStopSequence() {
        return this.stopSequence;
    }

    int getArrivalTime() {
        return this.arrivalTime;
    }

    int getDepartureTime() {
        return this.departureTime;
    }
}