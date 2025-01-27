/**
 * Author: Anirudh Ray
 * Institution: Professorship of Traffic Engineering and Control, Technical University of Munich
 * Department: Mobility Systems Engineering, School of Engineering and Design
 * E-mail Address: Anirudh.Ray@tum.de
 * Purpose: Component of a Java-based multi-modal routing algorithm, built using RAPTOR, Dijkstra-algorithm, and
 * KD-Trees
 */

package src.MultiModalRouter.QueryManager;
// Can average transfer costs at stops also be factored in for heuristic development

public class MultiModalQueryResponsesPub {
    // Trip-related attributes to be used as independent variables
    private double originPointLongitude;
    private double originPointLatitude;
    private double destinationPointLongitude;
    private double destinationPointLatitude;
    private int departureTimeOriginPoint;
    private int originTazId;
    private int destinationTazId;
    private double travelTimeOriginTazToDestinationTazPeak;
    private double travelTimeOriginTazToDestinationTazOffPeak;
    private double travelTimeOriginTazToDestinationTazNight;
    private long nearestOriginNodeId;
    private double nearestOriginNodeLongitude;
    private double nearestOriginNodeLatitude;
    private long nearestDestinationNodeId;
    private double nearestDestinationNodeLongitude;
    private double nearestDestinationNodeLatitude;
    private int destinationStopId;
    private String destinationStopName;
    private int destinationStopType;
    private int destinationStopDailyServiceCount;
    private double destinationStopAverageTransferCost;
    private double destinationStopLongitude;
    private double destinationStopLatitude;
    private long destinationStopNearestNodeId;
    private double destinationStopNearestNodeLongitude;
    private double destinationStopNearestNodeLatitude;
    private int countOriginStopsConsideredSolution;

    // Journey-related attributes to be used as co-variates
    private double travelTimeOriginToOriginStop;
    private double travelTimeOriginStopToDestinationStop;
    private double travelTimeDestinationStopToDestination;
    private int numberTransfersInTransit;
    private long timeElapsedInJourneyComputationNanoSeconds;

    // Stop-related attributes to be used as regressors
    private int originStopId;
    private String originStopName;
    private int originStopType;
    private int originStopDailyServiceCount;
    private double originStopAverageTransferCost;
    private double originStopLongitude;
    private double originStopLatitude;
    private long originStopNearestNodeId;
    private double originStopNearestNodeLongitude;
    private double originStopNearestNodeLatitude;

    // Journey-related attributes to be used as target variables
    private double totalJourneyTime;
    private double relativeDifferenceToBestJourneyTime;

    public MultiModalQueryResponsesPub(double originPointLongitude, double originPointLatitude,
                                       double destinationPointLongitude, double destinationPointLatitude,
                                       int departureTimeOriginPoint, int originTazId, int destinationTazId,
                                       double travelTimeOriginTazToDestinationTazPeak,
                                       double travelTimeOriginTazToDestinationTazOffPeak,
                                       double travelTimeOriginTazToDestinationTazNight,
                                       long nearestOriginNodeId, double nearestOriginNodeLongitude,
                                       double nearestOriginNodeLatitude, long nearestDestinationNodeId,
                                       double nearestDestinationNodeLongitude, double nearestDestinationNodeLatitude,
                                       int destinationStopId, String destinationStopName, int destinationStopType,
                                       int destinationStopDailyServiceCount, double destinationStopAverageTransferCost,
                                       double destinationStopLongitude, double destinationStopLatitude,
                                       long destinationStopNearestNodeId, double destinationStopNearestNodeLongitude,
                                       double destinationStopNearestNodeLatitude,
                                       int countOriginStopsConsideredSolution, double travelTimeOriginToOriginStop,
                                       double travelTimeOriginStopToDestinationStop,
                                       double travelTimeDestinationStopToDestination, int numberTransfersInTransit,
                                       long timeElapsedInJourneyComputationNanoSeconds, int originStopId,
                                       String originStopName, int originStopType, int originStopDailyServiceCount,
                                       double originStopAverageTransferCost, double originStopLongitude,
                                       double originStopLatitude, long originStopNearestNodeId,
                                       double originStopNearestNodeLongitude, double originStopNearestNodeLatitude,
                                       double totalJourneyTime, double relativeDifferenceToBestJourneyTime) {
        this.originPointLongitude = originPointLongitude;
        this.originPointLatitude = originPointLatitude;
        this.destinationPointLongitude = destinationPointLongitude;
        this.destinationPointLatitude = destinationPointLatitude;
        this.departureTimeOriginPoint = departureTimeOriginPoint;
        this.originTazId = originTazId;
        this.destinationTazId = destinationTazId;
        this.travelTimeOriginTazToDestinationTazPeak = travelTimeOriginTazToDestinationTazPeak;
        this.travelTimeOriginTazToDestinationTazOffPeak = travelTimeOriginTazToDestinationTazOffPeak;
        this.travelTimeOriginTazToDestinationTazNight = travelTimeOriginTazToDestinationTazNight;
        this.nearestOriginNodeId = nearestOriginNodeId;
        this.nearestOriginNodeLongitude = nearestOriginNodeLongitude;
        this.nearestOriginNodeLatitude = nearestOriginNodeLatitude;
        this.nearestDestinationNodeId = nearestDestinationNodeId;
        this.nearestDestinationNodeLongitude = nearestDestinationNodeLongitude;
        this.nearestDestinationNodeLatitude = nearestDestinationNodeLatitude;
        this.destinationStopId = destinationStopId;
        this.destinationStopName = destinationStopName;
        this.destinationStopType = destinationStopType;
        this.destinationStopDailyServiceCount = destinationStopDailyServiceCount;
        this.destinationStopAverageTransferCost = destinationStopAverageTransferCost;
        this.destinationStopLongitude = destinationStopLongitude;
        this.destinationStopLatitude = destinationStopLatitude;
        this.destinationStopNearestNodeId = destinationStopNearestNodeId;
        this.destinationStopNearestNodeLongitude = destinationStopNearestNodeLongitude;
        this.destinationStopNearestNodeLatitude = destinationStopNearestNodeLatitude;
        this.countOriginStopsConsideredSolution = countOriginStopsConsideredSolution;
        this.travelTimeOriginToOriginStop = travelTimeOriginToOriginStop;
        this.travelTimeOriginStopToDestinationStop = travelTimeOriginStopToDestinationStop;
        this.travelTimeDestinationStopToDestination = travelTimeDestinationStopToDestination;
        this.numberTransfersInTransit = numberTransfersInTransit;
        this.timeElapsedInJourneyComputationNanoSeconds = timeElapsedInJourneyComputationNanoSeconds;
        this.originStopId = originStopId;
        this.originStopName = originStopName;
        this.originStopType = originStopType;
        this.originStopDailyServiceCount = originStopDailyServiceCount;
        this.originStopAverageTransferCost = originStopAverageTransferCost;
        this.originStopLongitude = originStopLongitude;
        this.originStopLatitude = originStopLatitude;
        this.originStopNearestNodeId = originStopNearestNodeId;
        this.originStopNearestNodeLongitude = originStopNearestNodeLongitude;
        this.originStopNearestNodeLatitude = originStopNearestNodeLatitude;
        this.totalJourneyTime = totalJourneyTime;
        this.relativeDifferenceToBestJourneyTime = relativeDifferenceToBestJourneyTime;
    }

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

    public int getOriginTazId() {
        return this.originTazId;
    }

    public void setOriginTazId(int originTazId) {
        this.originTazId = originTazId;
    }

    public int getDestinationTazId() {
        return this.destinationTazId;
    }

    public void setDestinationTazId(int destinationTazId) {
        this.destinationTazId = destinationTazId;
    }

    public double getTravelTimeOriginTazToDestinationTazPeak() {
        return this.travelTimeOriginTazToDestinationTazPeak;
    }

    public void setTravelTimeOriginTazToDestinationTazPeak(double travelTimeOriginTazToDestinationTazPeak) {
        this.travelTimeOriginTazToDestinationTazPeak = travelTimeOriginTazToDestinationTazPeak;
    }

    public double getTravelTimeOriginTazToDestinationTazOffPeak() {
        return this.travelTimeOriginTazToDestinationTazOffPeak;
    }

    public void setTravelTimeOriginTazToDestinationTazOffPeak(double travelTimeOriginTazToDestinationTazOffPeak) {
        this.travelTimeOriginTazToDestinationTazOffPeak = travelTimeOriginTazToDestinationTazOffPeak;
    }

    public double getTravelTimeOriginTazToDestinationTazNight() {
        return this.travelTimeOriginTazToDestinationTazNight;
    }

    public void setTravelTimeOriginTazToDestinationTazNight(double travelTimeOriginTazToDestinationTazNight) {
        this.travelTimeOriginTazToDestinationTazNight = travelTimeOriginTazToDestinationTazNight;
    }

    public long getNearestOriginNodeId() {
        return this.nearestOriginNodeId;
    }

    public void setNearestOriginNodeId(long nearestOriginNodeId) {
        this.nearestOriginNodeId = nearestOriginNodeId;
    }

    public double getNearestOriginNodeLongitude() {
        return this.nearestOriginNodeLongitude;
    }

    public void setNearestOriginNodeLongitude(double nearestOriginNodeLongitude) {
        this.nearestOriginNodeLongitude = nearestOriginNodeLongitude;
    }

    public double getNearestOriginNodeLatitude() {
        return this.nearestOriginNodeLatitude;
    }

    public void setNearestOriginNodeLatitude(double nearestOriginNodeLatitude) {
        this.nearestOriginNodeLatitude = nearestOriginNodeLatitude;
    }

    public long getNearestDestinationNodeId() {
        return this.nearestDestinationNodeId;
    }

    public void setNearestDestinationNodeId(long nearestDestinationNodeId) {
        this.nearestDestinationNodeId = nearestDestinationNodeId;
    }

    public double getNearestDestinationNodeLongitude() {
        return this.nearestDestinationNodeLongitude;
    }

    public void setNearestDestinationNodeLongitude(double nearestDestinationNodeLongitude) {
        this.nearestDestinationNodeLongitude = nearestDestinationNodeLongitude;
    }

    public double getNearestDestinationNodeLatitude() {
        return this.nearestDestinationNodeLatitude;
    }

    public void setNearestDestinationNodeLatitude(double nearestDestinationNodeLatitude) {
        this.nearestDestinationNodeLatitude = nearestDestinationNodeLatitude;
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

    public int getDestinationStopType() {
        return this.destinationStopType;
    }

    public void setDestinationStopType(int destinationStopType) {
        this.destinationStopType = destinationStopType;
    }

    public int getDestinationStopDailyServiceCount() {
        return this.destinationStopDailyServiceCount;
    }

    public void setDestinationStopDailyServiceCount(int destinationStopDailyServiceCount) {
        this.destinationStopDailyServiceCount = destinationStopDailyServiceCount;
    }

    public double getDestinationStopAverageTransferCost() {
        return this.destinationStopAverageTransferCost;
    }

    public void setDestinationStopAverageTransferCost(double destinationStopAverageTransferCost) {
        this.destinationStopAverageTransferCost = destinationStopAverageTransferCost;
    }

    public double getDestinationStopLongitude() {
        return this.destinationStopLongitude;
    }

    public void setDestinationStopLongitude(double destinationStopLongitude) {
        this.destinationStopLongitude = destinationStopLongitude;
    }

    public double getDestinationStopLatitude() {
        return this.destinationStopLatitude;
    }

    public void setDestinationStopLatitude(double destinationStopLatitude) {
        this.destinationStopLatitude = destinationStopLatitude;
    }

    public long getDestinationStopNearestNodeId() {
        return this.destinationStopNearestNodeId;
    }

    public void setDestinationStopNearestNodeId(long destinationStopNearestNodeId) {
        this.destinationStopNearestNodeId = destinationStopNearestNodeId;
    }

    public double getDestinationStopNearestNodeLongitude() {
        return this.destinationStopNearestNodeLongitude;
    }

    public void setDestinationStopNearestNodeLongitude(double destinationStopNearestNodeLongitude) {
        this.destinationStopNearestNodeLongitude = destinationStopNearestNodeLongitude;
    }

    public double getDestinationStopNearestNodeLatitude() {
        return this.destinationStopNearestNodeLatitude;
    }

    public void setDestinationStopNearestNodeLatitude(double destinationStopNearestNodeLatitude) {
        this.destinationStopNearestNodeLatitude = destinationStopNearestNodeLatitude;
    }

    public int getCountOriginStopsConsideredSolution() {
        return this.countOriginStopsConsideredSolution;
    }

    public void setCountOriginStopsConsideredSolution(int countOriginStopsConsideredSolution) {
        this.countOriginStopsConsideredSolution = countOriginStopsConsideredSolution;
    }

    public double getTravelTimeOriginToOriginStop() {
        return this.travelTimeOriginToOriginStop;
    }

    public void setTravelTimeOriginToOriginStop(double travelTimeOriginToOriginStop) {
        this.travelTimeOriginToOriginStop = travelTimeOriginToOriginStop;
    }

    public double getTravelTimeOriginStopToDestinationStop() {
        return this.travelTimeOriginStopToDestinationStop;
    }

    public void setTravelTimeOriginStopToDestinationStop(double travelTimeOriginStopToDestinationStop) {
        this.travelTimeOriginStopToDestinationStop = travelTimeOriginStopToDestinationStop;
    }

    public double getTravelTimeDestinationStopToDestination() {
        return this.travelTimeDestinationStopToDestination;
    }

    public void setTravelTimeDestinationStopToDestination(double travelTimeDestinationStopToDestination) {
        this.travelTimeDestinationStopToDestination = travelTimeDestinationStopToDestination;
    }

    public int getNumberTransfersInTransit() {
        return this.numberTransfersInTransit;
    }

    public void setNumberTransfersInTransit(int numberTransfersInTransit) {
        this.numberTransfersInTransit = numberTransfersInTransit;
    }

    public long getTimeElapsedInJourneyComputationNanoSeconds() {
        return this.timeElapsedInJourneyComputationNanoSeconds;
    }

    public void setTimeElapsedInJourneyComputationNanoSeconds(long timeElapsedInJourneyComputationNanoSeconds) {
        this.timeElapsedInJourneyComputationNanoSeconds = timeElapsedInJourneyComputationNanoSeconds;
    }

    public int getOriginStopId() {
        return this.originStopId;
    }

    public void setOriginStopId(int originStopId) {
        this.originStopId = originStopId;
    }

    public String getOriginStopName() {
        return this.originStopName;
    }

    public void setOriginStopName(String originStopName) {
        this.originStopName = originStopName;
    }

    public int getOriginStopType() {
        return this.originStopType;
    }

    public void setOriginStopType(int originStopType) {
        this.originStopType = originStopType;
    }

    public int getOriginStopDailyServiceCount() {
        return this.originStopDailyServiceCount;
    }

    public void setOriginStopDailyServiceCount(int originStopDailyServiceCount) {
        this.originStopDailyServiceCount = originStopDailyServiceCount;
    }

    public double getOriginStopAverageTransferCost() {
        return this.originStopAverageTransferCost;
    }

    public void setOriginStopAverageTransferCost(double originStopAverageTransferCost) {
        this.originStopAverageTransferCost = originStopAverageTransferCost;
    }

    public double getOriginStopLongitude() {
        return this.originStopLongitude;
    }

    public void setOriginStopLongitude(double originStopLongitude) {
        this.originStopLongitude = originStopLongitude;
    }

    public double getOriginStopLatitude() {
        return this.originStopLatitude;
    }

    public void setOriginStopLatitude(double originStopLatitude) {
        this.originStopLatitude = originStopLatitude;
    }

    public long getOriginStopNearestNodeId() {
        return this.originStopNearestNodeId;
    }

    public void setOriginStopNearestNodeId(long originStopNearestNodeId) {
        this.originStopNearestNodeId = originStopNearestNodeId;
    }

    public double getOriginStopNearestNodeLongitude() {
        return this.originStopNearestNodeLongitude;
    }

    public void setOriginStopNearestNodeLongitude(double originStopNearestNodeLongitude) {
        this.originStopNearestNodeLongitude = originStopNearestNodeLongitude;
    }

    public double getOriginStopNearestNodeLatitude() {
        return this.originStopNearestNodeLatitude;
    }

    public void setOriginStopNearestNodeLatitude(double originStopNearestNodeLatitude) {
        this.originStopNearestNodeLatitude = originStopNearestNodeLatitude;
    }

    public double getTotalJourneyTime() {
        return this.totalJourneyTime;
    }

    public void setTotalJourneyTime(double totalJourneyTime) {
        this.totalJourneyTime = totalJourneyTime;
    }

    public double getRelativeDifferenceToBestJourneyTime() {
        return this.relativeDifferenceToBestJourneyTime;
    }

    public void setRelativeDifferenceToBestJourneyTime(double relativeDifferenceToBestJourneyTime) {
        this.relativeDifferenceToBestJourneyTime = relativeDifferenceToBestJourneyTime;
    }
}