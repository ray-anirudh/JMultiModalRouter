package src.MultiModalRouter;

public class DrivingQuery {
    private int originNodeId;
    private int destinationNodeId;
    private int desiredDepartureTimeMinutes;

    DrivingQuery(int originNodeId, int destinationNodeId, int desiredDepartureTimeMinutes) {
        this.originNodeId = originNodeId;
        this.destinationNodeId = destinationNodeId;
        this.desiredDepartureTimeMinutes = desiredDepartureTimeMinutes;
    }

    public int getOriginNodeId() {
        return originNodeId;
    }

    public int getDestinationNodeId() {
        return destinationNodeId;
    }

    public int getDesiredDepartureTimeMinutes() {
        return desiredDepartureTimeMinutes;
    }
}
