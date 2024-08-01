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