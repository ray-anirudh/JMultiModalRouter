package src.PublicTransportRouter.GTFSDataManager;

public class Stop {
    private int stopId;
    private String stopName;    // Stop IDs are contained in the pertinent hashmap
    private int stopType;
    // Ascribed via route type information (Refer to: https://gtfs.org/de/schedule/reference/#routestxt)
    private int stopTripCount;     // Number of trips served per day via the stop
    private double stopLatitude;
    private double stopLongitude;

    Stop(String stopName, int stopType, int stopTripCount, double stopLatitude, double stopLongitude) {
        this.stopName = stopName;
        this.stopType = stopType;
        this.stopTripCount = stopTripCount;
        this.stopLatitude = stopLatitude;
        this.stopLongitude = stopLongitude;
    }

    Stop() {
        this(null, 0, 0, 0D, 0D);
    }

    public double equiRectangularDistanceTo(double otherLongitude, double otherLatitude) {
        final int EARTH_RADIUS_M = 6371000;
        double longitudeDifference = Math.toRadians(this.stopLongitude - otherLongitude);
        double latitudeDifference = Math.toRadians(this.stopLatitude - otherLatitude);

        double x = longitudeDifference * Math.cos(Math.toRadians((this.stopLatitude + otherLatitude) / 2));
        double y = latitudeDifference;
        return Math.sqrt(x * x + y * y) * EARTH_RADIUS_M;
    }

    void setStopId(int stopId) {
        this.stopId = stopId;
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

    int getStopType() {
        return this.stopType;
    }

    int getStopTripCount() {
        return this.stopTripCount;
    }

    public double getStopLatitude() {
        return this.stopLatitude;
    }

    public double getStopLongitude() {
        return this.stopLongitude;
    }
}