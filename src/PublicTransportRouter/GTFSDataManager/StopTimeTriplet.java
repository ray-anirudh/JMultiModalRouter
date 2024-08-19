package src.PublicTransportRouter.GTFSDataManager;

public class StopTimeTriplet {
    private int stopSequence;
    private double arrivalTime;
    private double departureTime;

    StopTimeTriplet(int stopSequence, double arrivalTime, double departureTime) {
        this.stopSequence = stopSequence;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
    }

    public int getStopSequence() {
        return this.stopSequence;
    }
    public double getArrivalTime() {
        return this.arrivalTime;
    }
    public double getDepartureTime() {
        return this.departureTime;
    }
}