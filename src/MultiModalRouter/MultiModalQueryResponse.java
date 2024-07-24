package src.MultiModalRouter;

public class MultiModalQueryResponse {
    private int listTypeIndex;
    private double originLongitude;
    private double originLatitude;
    private double destinationLongitude;
    private double destinationLatitude;
    private String departureTime;
    private int numberOriginStops;
    private int numberDestinationStops;
    private String originStopName;
    private int originStopId;
    private String destinationStopName;
    private int destinationStopId;
    private double travelTimeOriginToOriginStop;
    private double travelTimeOriginStopToDestinationStop;
    private double travelTimeDestinationStopToDestination;
    private long timeElapsedQueryProcessing;
    private int accuracyMarker;

    public MultiModalQueryResponse(int listTypeIndex, double originLongitude, double originLatitude,
                                   double destinationLongitude, double destinationLatitude, String departureTime,
                                   int numberOfOriginStops, int numberOfDestinationStops, String originStopName,
                                   int originStopId, String destinationStopName, int destinationStopId,
                                   double travelTimeOriginToOriginStop, double travelTimeOriginStopToDestinationStop,
                                   double travelTimeDestinationStopToDestination, long timeElapsedQueryProcessing,
                                   int accuracyMarker) {
        this.listTypeIndex = listTypeIndex;
        this.originLongitude = originLongitude;
        this.originLatitude = originLatitude;
        this.destinationLongitude = destinationLongitude;
        this.destinationLatitude = destinationLatitude;
        this.departureTime = departureTime;
        this.numberOriginStops = numberOfOriginStops;
        this.numberDestinationStops = numberOfDestinationStops;
        this.originStopName = originStopName;
        this.originStopId = originStopId;
        this.destinationStopName = destinationStopName;
        this.destinationStopId = destinationStopId;
        this.travelTimeOriginToOriginStop = travelTimeOriginToOriginStop;
        this.travelTimeOriginStopToDestinationStop = travelTimeOriginStopToDestinationStop;
        this.travelTimeDestinationStopToDestination = travelTimeDestinationStopToDestination;
        this.timeElapsedQueryProcessing = timeElapsedQueryProcessing;
        this.accuracyMarker = accuracyMarker;
    }

    public MultiModalQueryResponse() {

    }

    public int getListTypeIndex() {
        return this.listTypeIndex;
    }

    public double getOriginLongitude() {
        return this.originLongitude;
    }

    public double getOriginLatitude() {
        return this.originLatitude;
    }

    public double getDestinationLongitude() {
        return this.destinationLongitude;
    }

    public double getDestinationLatitude() {
        return this.destinationLatitude;
    }

    public String getDepartureTime() {
        return this.departureTime;
    }

    public int getNumberOriginStops() {
        return this.numberOriginStops;
    }

    public int getNumberDestinationStops() {
        return this.numberDestinationStops;
    }

    public String getOriginStopName() {
        return this.originStopName;
    }

    public int getOriginStopId() {
        return this.originStopId;
    }

    public String getDestinationStopName() {
        return this.destinationStopName;
    }

    public int getDestinationStopId() {
        return this.destinationStopId;
    }

    public double getTravelTimeOriginToOriginStop() {
        return this.travelTimeOriginToOriginStop;
    }

    public double getTravelTimeOriginStopToDestinationStop() {
        return this.travelTimeOriginStopToDestinationStop;
    }

    public double getTravelTimeDestinationStopToDestination() {
        return this.travelTimeDestinationStopToDestination;
    }

    public long getTimeElapsedQueryProcessing() {
        return this.timeElapsedQueryProcessing;
    }

    public int getAccuracyMarker() {
        return this.accuracyMarker;
    }
}