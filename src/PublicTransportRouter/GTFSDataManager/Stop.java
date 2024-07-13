package src.PublicTransportRouter.GTFSDataManager;

// Class features are key for heuristic-related operations
public class Stop {
    private int stopId;
    private String stopName;    // Stop IDs are also contained in the pertinent hashmap
    private int stopType;
    // Ascribed via route type information (Refer to: https://gtfs.org/de/schedule/reference/#routestxt)
    private int stopTripCount;     // Number of trips served per day via the stop
    private double stopLongitude;
    private double stopLatitude;

    Stop(int stopId, String stopName, int stopType, int stopTripCount, double stopLongitude, double stopLatitude) {
        this.stopId = stopId;
        this.stopName = stopName;
        this.stopType = stopType;
        this.stopTripCount = stopTripCount;
        this.stopLongitude = stopLongitude;
        this.stopLatitude = stopLatitude;
    }

    Stop() {
        this(0, null, 0, 0, 0D, 0D);
    }

    public double equiRectangularDistanceTo(double otherLongitude, double otherLatitude) {
        final int EARTH_RADIUS_M = 6_371_000;
        double longitudeDifference = Math.toRadians(this.stopLongitude - otherLongitude);
        double latitudeDifference = Math.toRadians(this.stopLatitude - otherLatitude);

        double x = longitudeDifference * Math.cos(Math.toRadians((this.stopLatitude + otherLatitude) / 2));
        double y = latitudeDifference;
        return Math.sqrt(x * x + y * y) * EARTH_RADIUS_M;
    }

    void setStopType(int stopType) {
        this.stopType = stopType;
    }
    void setStopTripCount(int stopTripCount) {
        this.stopTripCount = stopTripCount;
    }
    public int getStopId() {
        return this.stopId;
    }
    public String getStopName() {
        return this.stopName;
    }
    public int getStopType() {
        return this.stopType;
    }
    public int getStopTripCount() {
        return this.stopTripCount;
    }
    public double getStopLongitude() {
        return this.stopLongitude;
    }
    public double getStopLatitude() {
        return this.stopLatitude;
    }
}