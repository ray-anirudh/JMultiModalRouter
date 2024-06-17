package src.MultiModalRouter;

public class DrivingQuery {
    private long originNodeId;
    private long destinationNodeId;
    private int desiredDepartureTimeMinutes;

    DrivingQuery(long originNodeId, long destinationNodeId, int desiredDepartureTimeMinutes) {
        this.originNodeId = originNodeId;
        this.destinationNodeId = destinationNodeId;
        this.desiredDepartureTimeMinutes = desiredDepartureTimeMinutes;
    }

    public long getOriginNodeId() {
        return originNodeId;
    }

    public long getDestinationNodeId() {
        return destinationNodeId;
    }

    public int getDesiredDepartureTimeMinutes() {
        return desiredDepartureTimeMinutes;
    }
}
