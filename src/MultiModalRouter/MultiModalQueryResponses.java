package src.MultiModalRouter;
// Can average transfer costs at stops also be factored in for heuristic development

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
    private double travelTimeDestinationStopToDestination;

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

    // Set up setters
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

    public void setDepartureTimeOriginPoint(int departureTimeOriginPoint) {
        this.departureTimeOriginPoint = departureTimeOriginPoint;
    }

    public void setNearestOriginNodeId(long nearestOriginNodeId) {
        this.nearestOriginNodeId = nearestOriginNodeId;
    }

    public void setNearestDestinationNodeId(long nearestDestinationNodeId) {
        this.nearestDestinationNodeId = nearestDestinationNodeId;
    }

    public void setDestinationStopId(int destinationStopId) {
        this.destinationStopId = destinationStopId;
    }

    public void setDestinationStopName(String destinationStopName) {
        this.destinationStopName = destinationStopName;
    }

    public void setTravelTimeDestinationStopToDestination(double travelTimeDestinationStopToDestination) {
        this.travelTimeDestinationStopToDestination = travelTimeDestinationStopToDestination;
    }

    public void setCountOriginStopsConsideredExactSolution(int countOriginStopsConsideredExactSolution) {
        this.countOriginStopsConsideredExactSolution = countOriginStopsConsideredExactSolution;
    }

    public void setTimeElapsedQueryProcessingExactSolution(double timeElapsedQueryProcessingExactSolution) {
        this.timeElapsedQueryProcessingExactSolution = timeElapsedQueryProcessingExactSolution;
    }

    public void setOriginStopIdExactSolution(int originStopIdExactSolution) {
        this.originStopIdExactSolution = originStopIdExactSolution;
    }

    public void setOriginStopNameExactSolution(String originStopNameExactSolution) {
        this.originStopNameExactSolution = originStopNameExactSolution;
    }

    public void setOriginStopTypeExactSolution(int originStopTypeExactSolution) {
        this.originStopTypeExactSolution = originStopTypeExactSolution;
    }

    public void setOriginStopTripVolumeExactSolution(int originStopTripVolumeExactSolution) {
        this.originStopTripVolumeExactSolution = originStopTripVolumeExactSolution;
    }

    public void setOriginStopAverageTransferCostExactSolution(double originStopAverageTransferCostExactSolution) {
        this.originStopAverageTransferCostExactSolution = originStopAverageTransferCostExactSolution;
    }

    public void setTravelTimeOriginToOriginStopExactSolution(double travelTimeOriginToOriginStopExactSolution) {
        this.travelTimeOriginToOriginStopExactSolution = travelTimeOriginToOriginStopExactSolution;
    }

    public void setTravelTimeOriginStopToDestinationStopExactSolution(
            double travelTimeOriginStopToDestinationStopExactSolution) {
        this.travelTimeOriginStopToDestinationStopExactSolution = travelTimeOriginStopToDestinationStopExactSolution;
    }

    public void setTotalTravelTimeExactSolution(double totalTravelTimeExactSolution) {
        this.totalTravelTimeExactSolution = totalTravelTimeExactSolution;
    }

    public void setCountOriginStopsConsideredSHSolution(int countOriginStopsConsideredSHSolution) {
        this.countOriginStopsConsideredSHSolution = countOriginStopsConsideredSHSolution;
    }

    public void setTimeElapsedQueryProcessingSHSolution(double timeElapsedQueryProcessingSHSolution) {
        this.timeElapsedQueryProcessingSHSolution = timeElapsedQueryProcessingSHSolution;
    }

    public void setOriginStopIdSHSolution(int originStopIdSHSolution) {
        this.originStopIdSHSolution = originStopIdSHSolution;
    }

    public void setOriginStopNameSHSolution(String originStopNameSHSolution) {
        this.originStopNameSHSolution = originStopNameSHSolution;
    }

    public void setOriginStopTypeSHSolution(int originStopTypeSHSolution) {
        this.originStopTypeSHSolution = originStopTypeSHSolution;
    }

    public void setOriginStopTripVolumeSHSolution(int originStopTripVolumeSHSolution) {
        this.originStopTripVolumeSHSolution = originStopTripVolumeSHSolution;
    }

    public void setOriginStopAverageTransferCostSHSolution(double originStopAverageTransferCostSHSolution) {
        this.originStopAverageTransferCostSHSolution = originStopAverageTransferCostSHSolution;
    }

    public void setTravelTimeOriginToOriginStopSHSolution(double travelTimeOriginToOriginStopSHSolution) {
        this.travelTimeOriginToOriginStopSHSolution = travelTimeOriginToOriginStopSHSolution;
    }

    public void setTravelTimeOriginStopToDestinationStopSHSolution(
            double travelTimeOriginStopToDestinationStopSHSolution) {
        this.travelTimeOriginStopToDestinationStopSHSolution = travelTimeOriginStopToDestinationStopSHSolution;
    }

    public void setTotalTravelTimeSHSolution(double totalTravelTimeSHSolution) {
        this.totalTravelTimeSHSolution = totalTravelTimeSHSolution;
    }

    public void setRelativeTravelTimeDifferenceInSHAndExactSolutions(
            double relativeTravelTimeDifferenceInSHAndExactSolutions) {
        this.relativeTravelTimeDifferenceInSHAndExactSolutions = relativeTravelTimeDifferenceInSHAndExactSolutions;
    }

    public void setCountOriginStopsConsideredTVSolution(int countOriginStopsConsideredTVSolution) {
        this.countOriginStopsConsideredTVSolution = countOriginStopsConsideredTVSolution;
    }

    public void setTimeElapsedQueryProcessingTVSolution(double timeElapsedQueryProcessingTVSolution) {
        this.timeElapsedQueryProcessingTVSolution = timeElapsedQueryProcessingTVSolution;
    }

    public void setOriginStopIdTVSolution(int originStopIdTVSolution) {
        this.originStopIdTVSolution = originStopIdTVSolution;
    }

    public void setOriginStopNameTVSolution(String originStopNameTVSolution) {
        this.originStopNameTVSolution = originStopNameTVSolution;
    }

    public void setOriginStopTypeTVSolution(int originStopTypeTVSolution) {
        this.originStopTypeTVSolution = originStopTypeTVSolution;
    }

    public void setOriginStopTripVolumeTVSolution(int originStopTripVolumeTVSolution) {
        this.originStopTripVolumeTVSolution = originStopTripVolumeTVSolution;
    }

    public void setOriginStopAverageTransferCostTVSolution(double originStopAverageTransferCostTVSolution) {
        this.originStopAverageTransferCostTVSolution = originStopAverageTransferCostTVSolution;
    }

    public void setTravelTimeOriginToOriginStopTVSolution(double travelTimeOriginToOriginStopTVSolution) {
        this.travelTimeOriginToOriginStopTVSolution = travelTimeOriginToOriginStopTVSolution;
    }

    public void setTravelTimeOriginStopToDestinationStopTVSolution(
            double travelTimeOriginStopToDestinationStopTVSolution) {
        this.travelTimeOriginStopToDestinationStopTVSolution = travelTimeOriginStopToDestinationStopTVSolution;
    }

    public void setTotalTravelTimeTVSolution(double totalTravelTimeTVSolution) {
        this.totalTravelTimeTVSolution = totalTravelTimeTVSolution;
    }

    public void setRelativeTravelTimeDifferenceInTVAndExactSolutions(
            double relativeTravelTimeDifferenceInTVAndExactSolutions) {
        this.relativeTravelTimeDifferenceInTVAndExactSolutions = relativeTravelTimeDifferenceInTVAndExactSolutions;
    }

    // Set up getters
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

    public int getDepartureTimeOriginPoint() {
        return this.departureTimeOriginPoint;
    }

    public long getNearestOriginNodeId() {
        return this.nearestOriginNodeId;
    }

    public long getNearestDestinationNodeId() {
        return this.nearestDestinationNodeId;
    }

    public int getDestinationStopId() {
        return this.destinationStopId;
    }

    public String getDestinationStopName() {
        return this.destinationStopName;
    }

    public double getTravelTimeDestinationStopToDestination() {
        return this.travelTimeDestinationStopToDestination;
    }

    public int getCountOriginStopsConsideredExactSolution() {
        return this.countOriginStopsConsideredExactSolution;
    }

    public double getTimeElapsedQueryProcessingExactSolution() {
        return this.timeElapsedQueryProcessingExactSolution;
    }

    public int getOriginStopIdExactSolution() {
        return this.originStopIdExactSolution;
    }

    public String getOriginStopNameExactSolution() {
        return this.originStopNameExactSolution;
    }

    public int getOriginStopTypeExactSolution() {
        return this.originStopTypeExactSolution;
    }

    public int getOriginStopTripVolumeExactSolution() {
        return this.originStopTripVolumeExactSolution;
    }

    public double getOriginStopAverageTransferCostExactSolution() {
        return this.originStopAverageTransferCostExactSolution;
    }

    public double getTravelTimeOriginToOriginStopExactSolution() {
        return this.travelTimeOriginToOriginStopExactSolution;
    }

    public double getTravelTimeOriginStopToDestinationStopExactSolution() {
        return this.travelTimeOriginStopToDestinationStopExactSolution;
    }

    public double getTotalTravelTimeExactSolution() {
        return this.totalTravelTimeExactSolution;
    }

    public int getCountOriginStopsConsideredSHSolution() {
        return this.countOriginStopsConsideredSHSolution;
    }

    public double getTimeElapsedQueryProcessingSHSolution() {
        return this.timeElapsedQueryProcessingSHSolution;
    }

    public int getOriginStopIdSHSolution() {
        return this.originStopIdSHSolution;
    }

    public String getOriginStopNameSHSolution() {
        return this.originStopNameSHSolution;
    }

    public int getOriginStopTypeSHSolution() {
        return this.originStopTypeSHSolution;
    }

    public int getOriginStopTripVolumeSHSolution() {
        return this.originStopTripVolumeSHSolution;
    }

    public double getOriginStopAverageTransferCostSHSolution() {
        return this.originStopAverageTransferCostSHSolution;
    }

    public double getTravelTimeOriginToOriginStopSHSolution() {
        return this.travelTimeOriginToOriginStopSHSolution;
    }

    public double getTravelTimeOriginStopToDestinationStopSHSolution() {
        return this.travelTimeOriginStopToDestinationStopSHSolution;
    }

    public double getTotalTravelTimeSHSolution() {
        return this.totalTravelTimeSHSolution;
    }

    public double getRelativeTravelTimeDifferenceInSHAndExactSolutions() {
        return this.relativeTravelTimeDifferenceInSHAndExactSolutions;
    }

    public int getCountOriginStopsConsideredTVSolution() {
        return this.countOriginStopsConsideredTVSolution;
    }

    public double getTimeElapsedQueryProcessingTVSolution() {
        return this.timeElapsedQueryProcessingTVSolution;
    }

    public int getOriginStopIdTVSolution() {
        return this.originStopIdTVSolution;
    }

    public String getOriginStopNameTVSolution() {
        return this.originStopNameTVSolution;
    }

    public int getOriginStopTypeTVSolution() {
        return this.originStopTypeTVSolution;
    }

    public int getOriginStopTripVolumeTVSolution() {
        return this.originStopTripVolumeTVSolution;
    }

    public double getOriginStopAverageTransferCostTVSolution() {
        return this.originStopAverageTransferCostTVSolution;
    }

    public double getTravelTimeOriginToOriginStopTVSolution() {
        return this.travelTimeOriginToOriginStopTVSolution;
    }

    public double getTravelTimeOriginStopToDestinationStopTVSolution() {
        return this.travelTimeOriginStopToDestinationStopTVSolution;
    }

    public double getTotalTravelTimeTVSolution() {
        return this.totalTravelTimeTVSolution;
    }

    public double getRelativeTravelTimeDifferenceInTVAndExactSolutions() {
        return this.relativeTravelTimeDifferenceInTVAndExactSolutions;
    }
}