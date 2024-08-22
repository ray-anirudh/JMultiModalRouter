package src.PublicTransportRouter.GTFSDataManager;

// Class features are key for heuristic-related operations
public class Stop {
    private int stopId;
    private String stopName;    // Stop IDs are also contained in the pertinent hashmap, as well
    private double stopLongitude;
    private double stopLatitude;
    private int stopType;
    // Ascribed via route type information (Refer to: https://gtfs.org/de/schedule/reference/#routestxt)
    private int stopTripCount;     // Number of trips served per day via the stop
    private double averageTransferCost;

    Stop(int stopId, String stopName, double stopLongitude, double stopLatitude, int stopType, int stopTripCount,
         double averageTransferCost) {
        this.stopId = stopId;
        this.stopName = stopName;
        this.stopLongitude = stopLongitude;
        this.stopLatitude = stopLatitude;
        this.stopType = stopType;
        this.stopTripCount = stopTripCount;
        this.averageTransferCost = averageTransferCost;
    }

    Stop() {
        this(0, "", 0D, 0D, 0, 0, 0D);
    }

    public double equiRectangularDistanceTo(double otherPointLongitude, double otherPointLatitude) {
        final int EARTH_RADIUS_M = 6_371_000;
        double longitudeDifference = Math.toRadians(this.stopLongitude - otherPointLongitude);
        double latitudeDifference = Math.toRadians(this.stopLatitude - otherPointLatitude);

        double x = longitudeDifference * Math.cos(Math.toRadians((this.stopLatitude + otherPointLatitude) / 2));
        return Math.sqrt(x * x + latitudeDifference * latitudeDifference) * EARTH_RADIUS_M;
    }

    void setStopType(int stopType) {
        this.stopType = stopType;
    }

    void setStopTripCount(int stopTripCount) {
        this.stopTripCount = stopTripCount;
    }

    void setAverageTransferCost(double averageTransferCost) {
        this.averageTransferCost = averageTransferCost;
    }

    public int getStopId() {
        return this.stopId;
    }

    public String getStopName() {
        return this.stopName;
    }

    public double getStopLongitude() {
        return this.stopLongitude;
    }

    public double getStopLatitude() {
        return this.stopLatitude;
    }

    public int getStopType() {
        return this.stopType;
    }

    public int getStopTripCount() {
        return this.stopTripCount;
    }

    public double getAverageTransferCost() {
        return this.averageTransferCost;
    }
}