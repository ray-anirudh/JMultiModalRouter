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

    void setStopSequence(int stopSequence) {
        this.stopSequence = stopSequence;
    }

    void setArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    void setDepartureTime(int departureTime) {
        this.departureTime = departureTime;
    }

    int getStopSequence() {
        return stopSequence;
    }

    int getArrivalTime() {
        return arrivalTime;
    }

    int getDepartureTime() {
        return departureTime;
    }
}