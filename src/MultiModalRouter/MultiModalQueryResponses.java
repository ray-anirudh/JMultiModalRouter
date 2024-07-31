package src.MultiModalRouter;

public class MultiModalQueryResponses {
    // Attributes common to all cases
    private double originPointLongitude;
    private double originPointLatitude;
    private double destinationPointLongitude;
    private double destinationPointLatitude;
    private int departureTimeOriginPointInt;
    private String departureTimeOriginPointString;
    private long nearestOriginNodeId;
    private long nearestDestinationNodeId;

    // Attributes of exact response
    private int countOriginStopsConsideredExactSolution;
    private int countDestinationStopsConsideredExactSolution;
    private long timeElapsedQueryProcessingExactSolution;
    private int originStopIdExactSolution;
    private String originStopNameExactSolution;
    private int destinationStopIdExactSolution;
    private String destinationStopNameExactSolution;
    private double travelTimeOriginToOriginStopExactSolution;
    private double travelTimeOriginStopToDestinationStopExactSolution;
    private double travelTimeDestinationStopToDestinationExactSolution;
    private double totalTravelTimeExactSolution;
    private String earliestArrivalTimeExactSolution;

    // Attributes of response based on stop-hierarchy heuristic
    private int countOriginStopsConsideredSHSolution;
    private int countDestinationStopsConsideredSHSolution;
    private long timeElapsedQueryProcessingSHSolution;
    private int originStopIdSHSolution;
    private String originStopNameSHSolution;
    private int destinationStopIdSHSolution;
    private String destinationStopNameSHSolution;
    private double travelTimeOriginToOriginStopSHSolution;
    private double travelTimeOriginStopToDestinationStopSHSolution;
    private double travelTimeDestinationStopToDestinationSHSolution;
    private double totalTravelTimeSHSolution;
    private String earliestArrivalTimeSHSolution;
    private boolean accuracyMarkerSHSolution;

    // Attributes of response based on stop-frequency heuristic
    private int countOriginStopsConsideredSFSolution;
    private int countDestinationStopsConsideredSFSolution;
    private long timeElapsedQueryProcessingSFSolution;
    private int originStopIdSFSolution;
    private String originStopNameSFSolution;
    private int destinationStopIdSFSolution;
    private String destinationStopNameSFSolution;
    private double travelTimeOriginToOriginStopSFSolution;
    private double travelTimeOriginStopToDestinationStopSFSolution;
    private double travelTimeDestinationStopToDestinationSFSolution;
    private double totalTravelTimeSFSolution;
    private String earliestArrivalTimeSFSolution;
    private boolean accuracyMarkerSFSolution;

    MultiModalQueryResponses(double originPointLongitude, double originPointLatitude,
                             double destinationPointLongitude, double destinationPointLatitude,
                             int departureTimeOriginPointInt, String departureTimeOriginPointString,
                             long nearestOriginNodeId, long nearestDestinationNodeId,
                             int countOriginStopsConsideredExactSolution,
                             int countDestinationStopsConsideredExactSolution,
                             long timeElapsedQueryProcessingExactSolution, int originStopIdExactSolution,
                             String originStopNameExactSolution, int destinationStopIdExactSolution,
                             String destinationStopNameExactSolution, double travelTimeOriginToOriginStopExactSolution,
                             double travelTimeOriginStopToDestinationStopExactSolution,
                             double travelTimeDestinationStopToDestinationExactSolution,
                             double totalTravelTimeExactSolution, String earliestArrivalTimeExactSolution,
                             int countOriginStopsConsideredSHSolution, int countDestinationStopsConsideredSHSolution,
                             long timeElapsedQueryProcessingSHSolution, int originStopIdSHSolution,
                             String originStopNameSHSolution, int destinationStopIdSHSolution,
                             String destinationStopNameSHSolution, double travelTimeOriginToOriginStopSHSolution,
                             double travelTimeOriginStopToDestinationStopSHSolution,
                             double travelTimeDestinationStopToDestinationSHSolution,
                             double totalTravelTimeSHSolution, String earliestArrivalTimeSHSolution,
                             boolean accuracyMarkerSHSolution, int countOriginStopsConsideredSFSolution,
                             int countDestinationStopsConsideredSFSolution, long timeElapsedQueryProcessingSFSolution,
                             int originStopIdSFSolution, String originStopNameSFSolution,
                             int destinationStopIdSFSolution, String destinationStopNameSFSolution,
                             double travelTimeOriginToOriginStopSFSolution,
                             double travelTimeOriginStopToDestinationStopSFSolution,
                             double travelTimeDestinationStopToDestinationSFSolution,
                             double totalTravelTimeSFSolution, String earliestArrivalTimeSFSolution,
                             boolean accuracyMarkerSFSolution) {
        this.originPointLongitude = originPointLongitude;
        this.originPointLatitude = originPointLatitude;
        this.destinationPointLongitude = destinationPointLongitude;
        this.destinationPointLatitude = destinationPointLatitude;
        this.departureTimeOriginPointInt = departureTimeOriginPointInt;
        this.departureTimeOriginPointString = departureTimeOriginPointString;
        this.nearestOriginNodeId = nearestOriginNodeId;
        this.nearestDestinationNodeId = nearestDestinationNodeId;
        this.countOriginStopsConsideredExactSolution = countOriginStopsConsideredExactSolution;
        this.countDestinationStopsConsideredExactSolution = countDestinationStopsConsideredExactSolution;
        this.timeElapsedQueryProcessingExactSolution = timeElapsedQueryProcessingExactSolution;
        this.originStopIdExactSolution = originStopIdExactSolution;
        this.originStopNameExactSolution = originStopNameExactSolution;
        this.destinationStopIdExactSolution = destinationStopIdExactSolution;
        this.destinationStopNameExactSolution = destinationStopNameExactSolution;
        this.travelTimeOriginToOriginStopExactSolution = travelTimeOriginToOriginStopExactSolution;
        this.travelTimeOriginStopToDestinationStopExactSolution = travelTimeOriginStopToDestinationStopExactSolution;
        this.travelTimeDestinationStopToDestinationExactSolution = travelTimeDestinationStopToDestinationExactSolution;
        this.totalTravelTimeExactSolution = totalTravelTimeExactSolution;
        this.earliestArrivalTimeExactSolution = earliestArrivalTimeExactSolution;
        this.countOriginStopsConsideredSHSolution = countOriginStopsConsideredSHSolution;
        this.countDestinationStopsConsideredSHSolution = countDestinationStopsConsideredSHSolution;
        this.timeElapsedQueryProcessingSHSolution = timeElapsedQueryProcessingSHSolution;
        this.originStopIdSHSolution = originStopIdSHSolution;
        this.originStopNameSHSolution = originStopNameSHSolution;
        this.destinationStopIdSHSolution = destinationStopIdSHSolution;
        this.destinationStopNameSHSolution = destinationStopNameSHSolution;
        this.travelTimeOriginToOriginStopSHSolution = travelTimeOriginToOriginStopSHSolution;
        this.travelTimeOriginStopToDestinationStopSHSolution = travelTimeOriginStopToDestinationStopSHSolution;
        this.travelTimeDestinationStopToDestinationSHSolution = travelTimeDestinationStopToDestinationSHSolution;
        this.totalTravelTimeSHSolution = totalTravelTimeSHSolution;
        this.earliestArrivalTimeSHSolution = earliestArrivalTimeSHSolution;
        this.accuracyMarkerSHSolution = accuracyMarkerSHSolution;
        this.countOriginStopsConsideredSFSolution = countOriginStopsConsideredSFSolution;
        this.countDestinationStopsConsideredSFSolution = countDestinationStopsConsideredSFSolution;
        this.timeElapsedQueryProcessingSFSolution = timeElapsedQueryProcessingSFSolution;
        this.originStopIdSFSolution = originStopIdSFSolution;
        this.originStopNameSFSolution = originStopNameSFSolution;
        this.destinationStopIdSFSolution = destinationStopIdSFSolution;
        this.destinationStopNameSFSolution = destinationStopNameSFSolution;
        this.travelTimeOriginToOriginStopSFSolution = travelTimeOriginToOriginStopSFSolution;
        this.travelTimeOriginStopToDestinationStopSFSolution = travelTimeOriginStopToDestinationStopSFSolution;
        this.travelTimeDestinationStopToDestinationSFSolution = travelTimeDestinationStopToDestinationSFSolution;
        this.totalTravelTimeSFSolution = totalTravelTimeSFSolution;
        this.earliestArrivalTimeSFSolution = earliestArrivalTimeSFSolution;
        this.accuracyMarkerSFSolution = accuracyMarkerSFSolution;
    }

    public MultiModalQueryResponses() {

    }

    public double getOriginPointLongitude() {
        return originPointLongitude;
    }

    public double getOriginPointLatitude() {
        return originPointLatitude;
    }

    public double getDestinationPointLongitude() {
        return destinationPointLongitude;
    }

    public double getDestinationPointLatitude() {
        return destinationPointLatitude;
    }

    public int getDepartureTimeOriginPointInt() {
        return departureTimeOriginPointInt;
    }

    public String getDepartureTimeOriginPointString() {
        return departureTimeOriginPointString;
    }

    public long getNearestOriginNodeId() {
        return nearestOriginNodeId;
    }

    public long getNearestDestinationNodeId() {
        return nearestDestinationNodeId;
    }

    public int getCountOriginStopsConsideredExactSolution() {
        return countOriginStopsConsideredExactSolution;
    }

    public int getCountDestinationStopsConsideredExactSolution() {
        return countDestinationStopsConsideredExactSolution;
    }

    public long getTimeElapsedQueryProcessingExactSolution() {
        return timeElapsedQueryProcessingExactSolution;
    }

    public int getOriginStopIdExactSolution() {
        return originStopIdExactSolution;
    }

    public String getOriginStopNameExactSolution() {
        return originStopNameExactSolution;
    }

    public int getDestinationStopIdExactSolution() {
        return destinationStopIdExactSolution;
    }

    public String getDestinationStopNameExactSolution() {
        return destinationStopNameExactSolution;
    }

    public double getTravelTimeOriginToOriginStopExactSolution() {
        return travelTimeOriginToOriginStopExactSolution;
    }

    public double getTravelTimeOriginStopToDestinationStopExactSolution() {
        return travelTimeOriginStopToDestinationStopExactSolution;
    }

    public double getTravelTimeDestinationStopToDestinationExactSolution() {
        return travelTimeDestinationStopToDestinationExactSolution;
    }

    public double getTotalTravelTimeExactSolution() {
        return totalTravelTimeExactSolution;
    }

    public String getEarliestArrivalTimeExactSolution() {
        return earliestArrivalTimeExactSolution;
    }

    public int getCountOriginStopsConsideredSHSolution() {
        return countOriginStopsConsideredSHSolution;
    }

    public int getCountDestinationStopsConsideredSHSolution() {
        return countDestinationStopsConsideredSHSolution;
    }

    public long getTimeElapsedQueryProcessingSHSolution() {
        return timeElapsedQueryProcessingSHSolution;
    }

    public int getOriginStopIdSHSolution() {
        return originStopIdSHSolution;
    }

    public String getOriginStopNameSHSolution() {
        return originStopNameSHSolution;
    }

    public int getDestinationStopIdSHSolution() {
        return destinationStopIdSHSolution;
    }

    public String getDestinationStopNameSHSolution() {
        return destinationStopNameSHSolution;
    }

    public double getTravelTimeOriginToOriginStopSHSolution() {
        return travelTimeOriginToOriginStopSHSolution;
    }

    public double getTravelTimeOriginStopToDestinationStopSHSolution() {
        return travelTimeOriginStopToDestinationStopSHSolution;
    }

    public double getTravelTimeDestinationStopToDestinationSHSolution() {
        return travelTimeDestinationStopToDestinationSHSolution;
    }

    public double getTotalTravelTimeSHSolution() {
        return totalTravelTimeSHSolution;
    }

    public String getEarliestArrivalTimeSHSolution() {
        return earliestArrivalTimeSHSolution;
    }

    public boolean isAccuracyMarkerSHSolution() {
        return accuracyMarkerSHSolution;
    }

    public int getCountOriginStopsConsideredSFSolution() {
        return countOriginStopsConsideredSFSolution;
    }

    public int getCountDestinationStopsConsideredSFSolution() {
        return countDestinationStopsConsideredSFSolution;
    }

    public long getTimeElapsedQueryProcessingSFSolution() {
        return timeElapsedQueryProcessingSFSolution;
    }

    public int getOriginStopIdSFSolution() {
        return originStopIdSFSolution;
    }

    public String getOriginStopNameSFSolution() {
        return originStopNameSFSolution;
    }

    public int getDestinationStopIdSFSolution() {
        return destinationStopIdSFSolution;
    }

    public String getDestinationStopNameSFSolution() {
        return destinationStopNameSFSolution;
    }

    public double getTravelTimeOriginToOriginStopSFSolution() {
        return travelTimeOriginToOriginStopSFSolution;
    }

    public double getTravelTimeOriginStopToDestinationStopSFSolution() {
        return travelTimeOriginStopToDestinationStopSFSolution;
    }

    public double getTravelTimeDestinationStopToDestinationSFSolution() {
        return travelTimeDestinationStopToDestinationSFSolution;
    }

    public double getTotalTravelTimeSFSolution() {
        return totalTravelTimeSFSolution;
    }

    public String getEarliestArrivalTimeSFSolution() {
        return earliestArrivalTimeSFSolution;
    }

    public boolean isAccuracyMarkerSFSolution() {
        return accuracyMarkerSFSolution;
    }
}