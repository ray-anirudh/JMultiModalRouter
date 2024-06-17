package src.MultiModalRouter;

public class DrivingQuery {
    private long originNodeId;
    private long destinationNodeId;

    DrivingQuery(long originNodeId, long destinationNodeId, int desiredDepartureTimeMinutes) {
        this.originNodeId = originNodeId;
        this.destinationNodeId = destinationNodeId;
    }

    public long getOriginNodeId() {
        return originNodeId;
    }

    public long getDestinationNodeId() {
        return destinationNodeId;
    }
}
