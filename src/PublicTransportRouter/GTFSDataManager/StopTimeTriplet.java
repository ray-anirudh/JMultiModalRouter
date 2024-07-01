package src.PublicTransportRouter.GTFSDataManager;

public class StopTimeTriplet {
    private int stopSequence;
    private int arrivalTime;
    private int departureTime;

    StopTimeTriplet(int stopSequence, int arrivalTime, int departureTime) {
        this.stopSequence = stopSequence;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
    }

    public int getStopSequence() {
        return this.stopSequence;
    }
    public int getArrivalTime() {
        return this.arrivalTime;
    }
    public int getDepartureTime() {
        return this.departureTime;
    }
}