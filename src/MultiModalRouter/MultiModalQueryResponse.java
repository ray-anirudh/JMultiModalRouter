package src.MultiModalRouter;

public class MultiModalQueryResponse {
    private int listTypeIndex;
    private double originLongitude;
    private double originLatitude;
    private double destinationLongitude;
    private double destinationLatitude;
    private String departureTime;
    private int numberOfOriginStops;
    private int numberOfDestinationStops;
    private String originStopName;
    private int originStopId;
    private String destinationStopName;
    private int destinationStopId;
    private double travelTimeOriginToOriginStop;
    private double travelTimeOriginStopToDestinationStop;
    private double travelTimeDestinationStopToDestination;
    private long timeElapsedInQueryProcessing;

    public MultiModalQueryResponse(int listTypeIndex, double originLongitude, double originLatitude,
                                   double destinationLongitude, double destinationLatitude, String departureTime,
                                   int numberOfOriginStops, int numberOfDestinationStops, String originStopName,
                                   int originStopId, String destinationStopName, int destinationStopId,
                                   double travelTimeOriginToOriginStop, double travelTimeOriginStopToDestinationStop,
                                   double travelTimeDestinationStopToDestination, long timeElapsedInQueryProcessing) {
        this.listTypeIndex = listTypeIndex;
        this.originLongitude = originLongitude;
        this.originLatitude = originLatitude;
        this.destinationLongitude = destinationLongitude;
        this.destinationLatitude = destinationLatitude;
        this.departureTime = departureTime;
        this.numberOfOriginStops = numberOfOriginStops;
        this.numberOfDestinationStops = numberOfDestinationStops;
        this.originStopName = originStopName;
        this.originStopId = originStopId;
        this.destinationStopName = destinationStopName;
        this.destinationStopId = destinationStopId;
        this.travelTimeOriginToOriginStop = travelTimeOriginToOriginStop;
        this.travelTimeOriginStopToDestinationStop = travelTimeOriginStopToDestinationStop;
        this.travelTimeDestinationStopToDestination = travelTimeDestinationStopToDestination;
        this.timeElapsedInQueryProcessing = timeElapsedInQueryProcessing;
    }
}
