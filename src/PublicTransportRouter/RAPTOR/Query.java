package src.PublicTransportRouter.RAPTOR;

public class Query {
    private String originStopId;
    private String destinationStopId;
    private int departureTime;

    Query(String originStopId, String destinationStopId, int departureTime) {
        this.originStopId = originStopId;
        this.destinationStopId = destinationStopId;
        this.departureTime = departureTime;
    }

    Query() {
        this(null, null, 0);
    }

    public String getOriginStopId() {
        return originStopId;
    }

    public String getDestinationStopId() {
        return destinationStopId;
    }

    public int getDepartureTime() {
        return departureTime;
    }
}