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

    public void setOriginStopId(String originStopId) {
        this.originStopId = originStopId;
    }

    public void setDestinationStopId(String destinationStopId) {
        this.destinationStopId = destinationStopId;
    }

    public void setDepartureTime(int departureTime) {
        this.departureTime = departureTime;
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