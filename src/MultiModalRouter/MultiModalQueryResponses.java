package src.MultiModalRouter;

public class MultiModalQueryResponses {
    // Attributes common to all cases
    private double originPointLongitude;
    private double originPointLatitude;
    private double destinationPointLongitude;
    private double destinationPointLatitude;
    private int departureTimeOriginPointInt;
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

    // Set up setters :)
    public void setOriginPointLongitude(double originPointLongitude) {
        this.originPointLongitude = originPointLongitude;
    }

    public void setOriginPointLatitude(double originPointLatitude) {
        this.originPointLatitude = originPointLatitude;
    }

    public void setDestinationPointLongitude(double destinationPointLongitude) {
        this.destinationPointLongitude = destinationPointLongitude;
    }

    public void setDestinationPointLatitude(double destinationPointLatitude) {
        this.destinationPointLatitude = destinationPointLatitude;
    }

    public void setDepartureTimeOriginPointInt(int departureTimeOriginPointInt) {
        this.departureTimeOriginPointInt = departureTimeOriginPointInt;
    }

    public void setNearestOriginNodeId(long nearestOriginNodeId) {
        this.nearestOriginNodeId = nearestOriginNodeId;
    }

    public void setNearestDestinationNodeId(long nearestDestinationNodeId) {
        this.nearestDestinationNodeId = nearestDestinationNodeId;
    }

    public void setCountOriginStopsConsideredExactSolution(int countOriginStopsConsideredExactSolution) {
        this.countOriginStopsConsideredExactSolution = countOriginStopsConsideredExactSolution;
    }

    public void setCountDestinationStopsConsideredExactSolution(int countDestinationStopsConsideredExactSolution) {
        this.countDestinationStopsConsideredExactSolution = countDestinationStopsConsideredExactSolution;
    }

    public void setTimeElapsedQueryProcessingExactSolution(long timeElapsedQueryProcessingExactSolution) {
        this.timeElapsedQueryProcessingExactSolution = timeElapsedQueryProcessingExactSolution;
    }

    public void setOriginStopIdExactSolution(int originStopIdExactSolution) {
        this.originStopIdExactSolution = originStopIdExactSolution;
    }

    public void setOriginStopNameExactSolution(String originStopNameExactSolution) {
        this.originStopNameExactSolution = originStopNameExactSolution;
    }

    public void setDestinationStopIdExactSolution(int destinationStopIdExactSolution) {
        this.destinationStopIdExactSolution = destinationStopIdExactSolution;
    }

    public void setDestinationStopNameExactSolution(String destinationStopNameExactSolution) {
        this.destinationStopNameExactSolution = destinationStopNameExactSolution;
    }

    public void setTravelTimeOriginToOriginStopExactSolution(double travelTimeOriginToOriginStopExactSolution) {
        this.travelTimeOriginToOriginStopExactSolution = travelTimeOriginToOriginStopExactSolution;
    }

    public void setTravelTimeOriginStopToDestinationStopExactSolution(double travelTimeOriginStopToDestinationStopExactSolution) {
        this.travelTimeOriginStopToDestinationStopExactSolution = travelTimeOriginStopToDestinationStopExactSolution;
    }

    public void setTravelTimeDestinationStopToDestinationExactSolution(double travelTimeDestinationStopToDestinationExactSolution) {
        this.travelTimeDestinationStopToDestinationExactSolution = travelTimeDestinationStopToDestinationExactSolution;
    }

    public void setTotalTravelTimeExactSolution(double totalTravelTimeExactSolution) {
        this.totalTravelTimeExactSolution = totalTravelTimeExactSolution;
    }

    public void setEarliestArrivalTimeExactSolution(String earliestArrivalTimeExactSolution) {
        this.earliestArrivalTimeExactSolution = earliestArrivalTimeExactSolution;
    }

    public void setCountOriginStopsConsideredSHSolution(int countOriginStopsConsideredSHSolution) {
        this.countOriginStopsConsideredSHSolution = countOriginStopsConsideredSHSolution;
    }

    public void setCountDestinationStopsConsideredSHSolution(int countDestinationStopsConsideredSHSolution) {
        this.countDestinationStopsConsideredSHSolution = countDestinationStopsConsideredSHSolution;
    }

    public void setTimeElapsedQueryProcessingSHSolution(long timeElapsedQueryProcessingSHSolution) {
        this.timeElapsedQueryProcessingSHSolution = timeElapsedQueryProcessingSHSolution;
    }

    public void setOriginStopIdSHSolution(int originStopIdSHSolution) {
        this.originStopIdSHSolution = originStopIdSHSolution;
    }

    public void setOriginStopNameSHSolution(String originStopNameSHSolution) {
        this.originStopNameSHSolution = originStopNameSHSolution;
    }

    public void setDestinationStopIdSHSolution(int destinationStopIdSHSolution) {
        this.destinationStopIdSHSolution = destinationStopIdSHSolution;
    }

    public void setDestinationStopNameSHSolution(String destinationStopNameSHSolution) {
        this.destinationStopNameSHSolution = destinationStopNameSHSolution;
    }

    public void setTravelTimeOriginToOriginStopSHSolution(double travelTimeOriginToOriginStopSHSolution) {
        this.travelTimeOriginToOriginStopSHSolution = travelTimeOriginToOriginStopSHSolution;
    }

    public void setTravelTimeOriginStopToDestinationStopSHSolution(double travelTimeOriginStopToDestinationStopSHSolution) {
        this.travelTimeOriginStopToDestinationStopSHSolution = travelTimeOriginStopToDestinationStopSHSolution;
    }

    public void setTravelTimeDestinationStopToDestinationSHSolution(double travelTimeDestinationStopToDestinationSHSolution) {
        this.travelTimeDestinationStopToDestinationSHSolution = travelTimeDestinationStopToDestinationSHSolution;
    }

    public void setTotalTravelTimeSHSolution(double totalTravelTimeSHSolution) {
        this.totalTravelTimeSHSolution = totalTravelTimeSHSolution;
    }

    public void setEarliestArrivalTimeSHSolution(String earliestArrivalTimeSHSolution) {
        this.earliestArrivalTimeSHSolution = earliestArrivalTimeSHSolution;
    }

    public void setAccuracyMarkerSHSolution(boolean accuracyMarkerSHSolution) {
        this.accuracyMarkerSHSolution = accuracyMarkerSHSolution;
    }

    public void setCountOriginStopsConsideredSFSolution(int countOriginStopsConsideredSFSolution) {
        this.countOriginStopsConsideredSFSolution = countOriginStopsConsideredSFSolution;
    }

    public void setCountDestinationStopsConsideredSFSolution(int countDestinationStopsConsideredSFSolution) {
        this.countDestinationStopsConsideredSFSolution = countDestinationStopsConsideredSFSolution;
    }

    public void setTimeElapsedQueryProcessingSFSolution(long timeElapsedQueryProcessingSFSolution) {
        this.timeElapsedQueryProcessingSFSolution = timeElapsedQueryProcessingSFSolution;
    }

    public void setOriginStopIdSFSolution(int originStopIdSFSolution) {
        this.originStopIdSFSolution = originStopIdSFSolution;
    }

    public void setOriginStopNameSFSolution(String originStopNameSFSolution) {
        this.originStopNameSFSolution = originStopNameSFSolution;
    }

    public void setDestinationStopIdSFSolution(int destinationStopIdSFSolution) {
        this.destinationStopIdSFSolution = destinationStopIdSFSolution;
    }

    public void setDestinationStopNameSFSolution(String destinationStopNameSFSolution) {
        this.destinationStopNameSFSolution = destinationStopNameSFSolution;
    }

    public void setTravelTimeOriginToOriginStopSFSolution(double travelTimeOriginToOriginStopSFSolution) {
        this.travelTimeOriginToOriginStopSFSolution = travelTimeOriginToOriginStopSFSolution;
    }

    public void setTravelTimeOriginStopToDestinationStopSFSolution
            (double travelTimeOriginStopToDestinationStopSFSolution) {
        this.travelTimeOriginStopToDestinationStopSFSolution = travelTimeOriginStopToDestinationStopSFSolution;
    }

    public void setTravelTimeDestinationStopToDestinationSFSolution
            (double travelTimeDestinationStopToDestinationSFSolution) {
        this.travelTimeDestinationStopToDestinationSFSolution = travelTimeDestinationStopToDestinationSFSolution;
    }

    public void setTotalTravelTimeSFSolution(double totalTravelTimeSFSolution) {
        this.totalTravelTimeSFSolution = totalTravelTimeSFSolution;
    }

    public void setEarliestArrivalTimeSFSolution(String earliestArrivalTimeSFSolution) {
        this.earliestArrivalTimeSFSolution = earliestArrivalTimeSFSolution;
    }

    public void setAccuracyMarkerSFSolution(boolean accuracyMarkerSFSolution) {
        this.accuracyMarkerSFSolution = accuracyMarkerSFSolution;
    }

    // Set up getters :)
    public double getOriginPointLongitude() {
        return this.originPointLongitude;
    }

    public double getOriginPointLatitude() {
        return this.originPointLatitude;
    }

    public double getDestinationPointLongitude() {
        return this.destinationPointLongitude;
    }

    public double getDestinationPointLatitude() {
        return this.destinationPointLatitude;
    }

    public int getDepartureTimeOriginPointInt() {
        return this.departureTimeOriginPointInt;
    }

    public long getNearestOriginNodeId() {
        return this.nearestOriginNodeId;
    }

    public long getNearestDestinationNodeId() {
        return this.nearestDestinationNodeId;
    }

    public int getCountOriginStopsConsideredExactSolution() {
        return this.countOriginStopsConsideredExactSolution;
    }

    public int getCountDestinationStopsConsideredExactSolution() {
        return this.countDestinationStopsConsideredExactSolution;
    }

    public long getTimeElapsedQueryProcessingExactSolution() {
        return this.timeElapsedQueryProcessingExactSolution;
    }

    public int getOriginStopIdExactSolution() {
        return this.originStopIdExactSolution;
    }

    public String getOriginStopNameExactSolution() {
        return this.originStopNameExactSolution;
    }

    public int getDestinationStopIdExactSolution() {
        return this.destinationStopIdExactSolution;
    }

    public String getDestinationStopNameExactSolution() {
        return this.destinationStopNameExactSolution;
    }

    public double getTravelTimeOriginToOriginStopExactSolution() {
        return this.travelTimeOriginToOriginStopExactSolution;
    }

    public double getTravelTimeOriginStopToDestinationStopExactSolution() {
        return this.travelTimeOriginStopToDestinationStopExactSolution;
    }

    public double getTravelTimeDestinationStopToDestinationExactSolution() {
        return this.travelTimeDestinationStopToDestinationExactSolution;
    }

    public double getTotalTravelTimeExactSolution() {
        return this.totalTravelTimeExactSolution;
    }

    public String getEarliestArrivalTimeExactSolution() {
        return this.earliestArrivalTimeExactSolution;
    }

    public int getCountOriginStopsConsideredSHSolution() {
        return this.countOriginStopsConsideredSHSolution;
    }

    public int getCountDestinationStopsConsideredSHSolution() {
        return this.countDestinationStopsConsideredSHSolution;
    }

    public long getTimeElapsedQueryProcessingSHSolution() {
        return this.timeElapsedQueryProcessingSHSolution;
    }

    public int getOriginStopIdSHSolution() {
        return this.originStopIdSHSolution;
    }

    public String getOriginStopNameSHSolution() {
        return this.originStopNameSHSolution;
    }

    public int getDestinationStopIdSHSolution() {
        return this.destinationStopIdSHSolution;
    }

    public String getDestinationStopNameSHSolution() {
        return this.destinationStopNameSHSolution;
    }

    public double getTravelTimeOriginToOriginStopSHSolution() {
        return this.travelTimeOriginToOriginStopSHSolution;
    }

    public double getTravelTimeOriginStopToDestinationStopSHSolution() {
        return this.travelTimeOriginStopToDestinationStopSHSolution;
    }

    public double getTravelTimeDestinationStopToDestinationSHSolution() {
        return this.travelTimeDestinationStopToDestinationSHSolution;
    }

    public double getTotalTravelTimeSHSolution() {
        return this.totalTravelTimeSHSolution;
    }

    public String getEarliestArrivalTimeSHSolution() {
        return this.earliestArrivalTimeSHSolution;
    }

    public boolean isAccuracyMarkerSHSolution() {
        return this.accuracyMarkerSHSolution;
    }

    public int getCountOriginStopsConsideredSFSolution() {
        return this.countOriginStopsConsideredSFSolution;
    }

    public int getCountDestinationStopsConsideredSFSolution() {
        return this.countDestinationStopsConsideredSFSolution;
    }

    public long getTimeElapsedQueryProcessingSFSolution() {
        return this.timeElapsedQueryProcessingSFSolution;
    }

    public int getOriginStopIdSFSolution() {
        return this.originStopIdSFSolution;
    }

    public String getOriginStopNameSFSolution() {
        return this.originStopNameSFSolution;
    }

    public int getDestinationStopIdSFSolution() {
        return this.destinationStopIdSFSolution;
    }

    public String getDestinationStopNameSFSolution() {
        return this.destinationStopNameSFSolution;
    }

    public double getTravelTimeOriginToOriginStopSFSolution() {
        return this.travelTimeOriginToOriginStopSFSolution;
    }

    public double getTravelTimeOriginStopToDestinationStopSFSolution() {
        return this.travelTimeOriginStopToDestinationStopSFSolution;
    }

    public double getTravelTimeDestinationStopToDestinationSFSolution() {
        return this.travelTimeDestinationStopToDestinationSFSolution;
    }

    public double getTotalTravelTimeSFSolution() {
        return this.totalTravelTimeSFSolution;
    }

    public String getEarliestArrivalTimeSFSolution() {
        return this.earliestArrivalTimeSFSolution;
    }

    public boolean isAccuracyMarkerSFSolution() {
        return this.accuracyMarkerSFSolution;
    }
}