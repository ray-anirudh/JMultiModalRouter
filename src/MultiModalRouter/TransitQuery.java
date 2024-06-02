package src.MultiModalRouter;

public class TransitQuery {
    private int originStopId;
    private int destinationStopId;
    private int desiredDepartureTimeMinutes;

    TransitQuery(int originStopId, int destinationStopId, int desiredDepartureTimeMinutes) {
        this.originStopId = originStopId;
        this.destinationStopId = destinationStopId;
        this.desiredDepartureTimeMinutes = desiredDepartureTimeMinutes;
    }

    public int getOriginStopId() {
        return originStopId;
    }

    public int getDestinationStopId() {
        return destinationStopId;
    }

    public int getDesiredDepartureTimeMinutes() {
        return desiredDepartureTimeMinutes;
    }
}