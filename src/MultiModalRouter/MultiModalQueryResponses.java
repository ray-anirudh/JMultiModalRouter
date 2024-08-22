package src.MultiModalRouter;

public class MultiModalQueryResponses {
    // Attributes common to all cases
    private double originPointLongitude;
    private double originPointLatitude;
    private double destinationPointLongitude;
    private double destinationPointLatitude;
    private int departureTimeOriginPoint;
    private long nearestOriginNodeId;
    private long nearestDestinationNodeId;
    private int destinationStopId;
    private String destinationStopName;
    private double travelTimeDestinationStopToDestinationMin;

    // Attributes of "exact" solution
    private int countOriginStopsConsideredExactSolution;
    private double timeElapsedQueryProcessingExactSolution;
    private int originStopIdExactSolution;
    private String originStopNameExactSolution;
    private int originStopTypeExactSolution;
    private int originStopTripVolumeExactSolution;
    private double originStopAverageTransferCostExactSolution;
    private double travelTimeOriginToOriginStopExactSolution;
    private double travelTimeOriginStopToDestinationStopExactSolution;
    private double totalTravelTimeExactSolution;

    // Attributes of solution based on "stop hierarchy" heuristic
    private int countOriginStopsConsideredSHSolution;
    private double timeElapsedQueryProcessingSHSolution;
    private int originStopIdSHSolution;
    private String originStopNameSHSolution;
    private int originStopTypeSHSolution;
    private int originStopTripVolumeSHSolution;
    private double originStopAverageTransferCostSHSolution;
    private double travelTimeOriginToOriginStopSHSolution;
    private double travelTimeOriginStopToDestinationStopSHSolution;
    private double totalTravelTimeSHSolution;
    private double relativeTravelTimeDifferenceInSHAndExactSolutions;

    // Attributes of solution based on "trip volume" heuristic
    private int countOriginStopsConsideredTVSolution;
    private double timeElapsedQueryProcessingTVSolution;
    private int originStopIdTVSolution;
    private String originStopNameTVSolution;
    private int originStopTypeTVSolution;
    private int originStopTripVolumeTVSolution;
    private double originStopAverageTransferCostTVSolution;
    private double travelTimeOriginToOriginStopTVSolution;
    private double travelTimeOriginStopToDestinationStopTVSolution;
    private double totalTravelTimeTVSolution;
    private double relativeTravelTimeDifferenceInTVAndExactSolutions;

    // Set up setters and getters

    public double getOriginPointLongitude() {
        return this.originPointLongitude;
    }

    public void setOriginPointLongitude(double originPointLongitude) {
        this.originPointLongitude = originPointLongitude;
    }

    public double getOriginPointLatitude() {
        return this.originPointLatitude;
    }

    public void setOriginPointLatitude(double originPointLatitude) {
        this.originPointLatitude = originPointLatitude;
    }

    public double getDestinationPointLongitude() {
        return this.destinationPointLongitude;
    }

    public void setDestinationPointLongitude(double destinationPointLongitude) {
        this.destinationPointLongitude = destinationPointLongitude;
    }

    public double getDestinationPointLatitude() {
        return this.destinationPointLatitude;
    }

    public void setDestinationPointLatitude(double destinationPointLatitude) {
        this.destinationPointLatitude = destinationPointLatitude;
    }

    public int getDepartureTimeOriginPoint() {
        return this.departureTimeOriginPoint;
    }

    public void setDepartureTimeOriginPoint(int departureTimeOriginPoint) {
        this.departureTimeOriginPoint = departureTimeOriginPoint;
    }

    public long getNearestOriginNodeId() {
        return this.nearestOriginNodeId;
    }

    public void setNearestOriginNodeId(long nearestOriginNodeId) {
        this.nearestOriginNodeId = nearestOriginNodeId;
    }

    public long getNearestDestinationNodeId() {
        return this.nearestDestinationNodeId;
    }

    public void setNearestDestinationNodeId(long nearestDestinationNodeId) {
        this.nearestDestinationNodeId = nearestDestinationNodeId;
    }

    public int getDestinationStopId() {
        return this.destinationStopId;
    }

    public void setDestinationStopId(int destinationStopId) {
        this.destinationStopId = destinationStopId;
    }

    public String getDestinationStopName() {
        return this.destinationStopName;
    }

    public void setDestinationStopName(String destinationStopName) {
        this.destinationStopName = destinationStopName;
    }

    public double getTravelTimeDestinationStopToDestinationMin() {
        return this.travelTimeDestinationStopToDestinationMin;
    }

    public void setTravelTimeDestinationStopToDestinationMin(double travelTimeDestinationStopToDestinationMin) {
        this.travelTimeDestinationStopToDestinationMin = travelTimeDestinationStopToDestinationMin;
    }

    public int getCountOriginStopsConsideredExactSolution() {
        return this.countOriginStopsConsideredExactSolution;
    }

    public void setCountOriginStopsConsideredExactSolution(int countOriginStopsConsideredExactSolution) {
        this.countOriginStopsConsideredExactSolution = countOriginStopsConsideredExactSolution;
    }

    public double getTimeElapsedQueryProcessingExactSolution() {
        return this.timeElapsedQueryProcessingExactSolution;
    }

    public void setTimeElapsedQueryProcessingExactSolution(double timeElapsedQueryProcessingExactSolution) {
        this.timeElapsedQueryProcessingExactSolution = timeElapsedQueryProcessingExactSolution;
    }

    public int getOriginStopIdExactSolution() {
        return this.originStopIdExactSolution;
    }

    public void setOriginStopIdExactSolution(int originStopIdExactSolution) {
        this.originStopIdExactSolution = originStopIdExactSolution;
    }

    public String getOriginStopNameExactSolution() {
        return this.originStopNameExactSolution;
    }

    public void setOriginStopNameExactSolution(String originStopNameExactSolution) {
        this.originStopNameExactSolution = originStopNameExactSolution;
    }

    public int getOriginStopTypeExactSolution() {
        return this.originStopTypeExactSolution;
    }

    public void setOriginStopTypeExactSolution(int originStopTypeExactSolution) {
        this.originStopTypeExactSolution = originStopTypeExactSolution;
    }

    public int getOriginStopTripVolumeExactSolution() {
        return this.originStopTripVolumeExactSolution;
    }

    public void setOriginStopTripVolumeExactSolution(int originStopTripVolumeExactSolution) {
        this.originStopTripVolumeExactSolution = originStopTripVolumeExactSolution;
    }

    public double getOriginStopAverageTransferCostExactSolution() {
        return this.originStopAverageTransferCostExactSolution;
    }

    public void setOriginStopAverageTransferCostExactSolution(double originStopAverageTransferCostExactSolution) {
        this.originStopAverageTransferCostExactSolution = originStopAverageTransferCostExactSolution;
    }

    public double getTravelTimeOriginToOriginStopExactSolution() {
        return this.travelTimeOriginToOriginStopExactSolution;
    }

    public void setTravelTimeOriginToOriginStopExactSolution(double travelTimeOriginToOriginStopExactSolution) {
        this.travelTimeOriginToOriginStopExactSolution = travelTimeOriginToOriginStopExactSolution;
    }

    public double getTravelTimeOriginStopToDestinationStopExactSolution() {
        return this.travelTimeOriginStopToDestinationStopExactSolution;
    }

    public void setTravelTimeOriginStopToDestinationStopExactSolution(double travelTimeOriginStopToDestinationStopExactSolution) {
        this.travelTimeOriginStopToDestinationStopExactSolution = travelTimeOriginStopToDestinationStopExactSolution;
    }

    public double getTotalTravelTimeExactSolution() {
        return this.totalTravelTimeExactSolution;
    }

    public void setTotalTravelTimeExactSolution(double totalTravelTimeExactSolution) {
        this.totalTravelTimeExactSolution = totalTravelTimeExactSolution;
    }

    public int getCountOriginStopsConsideredSHSolution() {
        return this.countOriginStopsConsideredSHSolution;
    }

    public void setCountOriginStopsConsideredSHSolution(int countOriginStopsConsideredSHSolution) {
        this.countOriginStopsConsideredSHSolution = countOriginStopsConsideredSHSolution;
    }

    public double getTimeElapsedQueryProcessingSHSolution() {
        return this.timeElapsedQueryProcessingSHSolution;
    }

    public void setTimeElapsedQueryProcessingSHSolution(double timeElapsedQueryProcessingSHSolution) {
        this.timeElapsedQueryProcessingSHSolution = timeElapsedQueryProcessingSHSolution;
    }

    public int getOriginStopIdSHSolution() {
        return this.originStopIdSHSolution;
    }

    public void setOriginStopIdSHSolution(int originStopIdSHSolution) {
        this.originStopIdSHSolution = originStopIdSHSolution;
    }

    public String getOriginStopNameSHSolution() {
        return this.originStopNameSHSolution;
    }

    public void setOriginStopNameSHSolution(String originStopNameSHSolution) {
        this.originStopNameSHSolution = originStopNameSHSolution;
    }

    public int getOriginStopTypeSHSolution() {
        return this.originStopTypeSHSolution;
    }

    public void setOriginStopTypeSHSolution(int originStopTypeSHSolution) {
        this.originStopTypeSHSolution = originStopTypeSHSolution;
    }

    public int getOriginStopTripVolumeSHSolution() {
        return this.originStopTripVolumeSHSolution;
    }

    public void setOriginStopTripVolumeSHSolution(int originStopTripVolumeSHSolution) {
        this.originStopTripVolumeSHSolution = originStopTripVolumeSHSolution;
    }

    public double getOriginStopAverageTransferCostSHSolution() {
        return this.originStopAverageTransferCostSHSolution;
    }

    public void setOriginStopAverageTransferCostSHSolution(double originStopAverageTransferCostSHSolution) {
        this.originStopAverageTransferCostSHSolution = originStopAverageTransferCostSHSolution;
    }

    public double getTravelTimeOriginToOriginStopSHSolution() {
        return this.travelTimeOriginToOriginStopSHSolution;
    }

    public void setTravelTimeOriginToOriginStopSHSolution(double travelTimeOriginToOriginStopSHSolution) {
        this.travelTimeOriginToOriginStopSHSolution = travelTimeOriginToOriginStopSHSolution;
    }

    public double getTravelTimeOriginStopToDestinationStopSHSolution() {
        return this.travelTimeOriginStopToDestinationStopSHSolution;
    }

    public void setTravelTimeOriginStopToDestinationStopSHSolution(double travelTimeOriginStopToDestinationStopSHSolution) {
        this.travelTimeOriginStopToDestinationStopSHSolution = travelTimeOriginStopToDestinationStopSHSolution;
    }

    public double getTotalTravelTimeSHSolution() {
        return this.totalTravelTimeSHSolution;
    }

    public void setTotalTravelTimeSHSolution(double totalTravelTimeSHSolution) {
        this.totalTravelTimeSHSolution = totalTravelTimeSHSolution;
    }

    public double getRelativeTravelTimeDifferenceInSHAndExactSolutions() {
        return this.relativeTravelTimeDifferenceInSHAndExactSolutions;
    }

    public void setRelativeTravelTimeDifferenceInSHAndExactSolutions(double relativeTravelTimeDifferenceInSHAndExactSolutions) {
        this.relativeTravelTimeDifferenceInSHAndExactSolutions = relativeTravelTimeDifferenceInSHAndExactSolutions;
    }

    public int getCountOriginStopsConsideredTVSolution() {
        return this.countOriginStopsConsideredTVSolution;
    }

    public void setCountOriginStopsConsideredTVSolution(int countOriginStopsConsideredTVSolution) {
        this.countOriginStopsConsideredTVSolution = countOriginStopsConsideredTVSolution;
    }

    public double getTimeElapsedQueryProcessingTVSolution() {
        return this.timeElapsedQueryProcessingTVSolution;
    }

    public void setTimeElapsedQueryProcessingTVSolution(double timeElapsedQueryProcessingTVSolution) {
        this.timeElapsedQueryProcessingTVSolution = timeElapsedQueryProcessingTVSolution;
    }

    public int getOriginStopIdTVSolution() {
        return this.originStopIdTVSolution;
    }

    public void setOriginStopIdTVSolution(int originStopIdTVSolution) {
        this.originStopIdTVSolution = originStopIdTVSolution;
    }

    public String getOriginStopNameTVSolution() {
        return this.originStopNameTVSolution;
    }

    public void setOriginStopNameTVSolution(String originStopNameTVSolution) {
        this.originStopNameTVSolution = originStopNameTVSolution;
    }

    public int getOriginStopTypeTVSolution() {
        return this.originStopTypeTVSolution;
    }

    public void setOriginStopTypeTVSolution(int originStopTypeTVSolution) {
        this.originStopTypeTVSolution = originStopTypeTVSolution;
    }

    public int getOriginStopTripVolumeTVSolution() {
        return this.originStopTripVolumeTVSolution;
    }

    public void setOriginStopTripVolumeTVSolution(int originStopTripVolumeTVSolution) {
        this.originStopTripVolumeTVSolution = originStopTripVolumeTVSolution;
    }

    public double getOriginStopAverageTransferCostTVSolution() {
        return this.originStopAverageTransferCostTVSolution;
    }

    public void setOriginStopAverageTransferCostTVSolution(double originStopAverageTransferCostTVSolution) {
        this.originStopAverageTransferCostTVSolution = originStopAverageTransferCostTVSolution;
    }

    public double getTravelTimeOriginToOriginStopTVSolution() {
        return this.travelTimeOriginToOriginStopTVSolution;
    }

    public void setTravelTimeOriginToOriginStopTVSolution(double travelTimeOriginToOriginStopTVSolution) {
        this.travelTimeOriginToOriginStopTVSolution = travelTimeOriginToOriginStopTVSolution;
    }

    public double getTravelTimeOriginStopToDestinationStopTVSolution() {
        return this.travelTimeOriginStopToDestinationStopTVSolution;
    }

    public void setTravelTimeOriginStopToDestinationStopTVSolution(double travelTimeOriginStopToDestinationStopTVSolution) {
        this.travelTimeOriginStopToDestinationStopTVSolution = travelTimeOriginStopToDestinationStopTVSolution;
    }

    public double getTotalTravelTimeTVSolution() {
        return this.totalTravelTimeTVSolution;
    }

    public void setTotalTravelTimeTVSolution(double totalTravelTimeTVSolution) {
        this.totalTravelTimeTVSolution = totalTravelTimeTVSolution;
    }

    public double getRelativeTravelTimeDifferenceInTVAndExactSolutions() {
        return this.relativeTravelTimeDifferenceInTVAndExactSolutions;
    }

    public void setRelativeTravelTimeDifferenceInTVAndExactSolutions(double relativeTravelTimeDifferenceInTVAndExactSolutions) {
        this.relativeTravelTimeDifferenceInTVAndExactSolutions = relativeTravelTimeDifferenceInTVAndExactSolutions;
    }
}