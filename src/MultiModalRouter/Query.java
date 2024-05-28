package src.MultiModalRouter;



// TODO: REVIEW USEFULNESS
public class Query {
    private int originStopId;
    private int destinationStopId;
    private int desiredDepartureTime;

    Query(int originStopId, int destinationStopId, int desiredDepartureTime) {
        this.originStopId = originStopId;
        this.destinationStopId = destinationStopId;
        this.desiredDepartureTime = desiredDepartureTime;
    }

    public int getOriginStopId() {
        return originStopId;
    }

    public int getDestinationStopId() {
        return destinationStopId;
    }

    public int getDesiredDepartureTime() {
        return desiredDepartureTime;
    }
}