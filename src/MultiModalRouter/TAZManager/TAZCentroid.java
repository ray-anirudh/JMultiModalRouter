package src.MultiModalRouter.TAZManager;

public class TAZCentroid {
    private int id;
    private double xCoordinate;
    private double yCoordinate;
    private double longitude;
    private double latitude;

    TAZCentroid(int id, double xCoordinate, double yCoordinate, double longitude, double latitude) {
        this.id = id;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public double equiRectangularDistanceTo(double sourceLongitude, double sourceLatitude) {
        final int EARTH_RADIUS_M = 6_371_000;
        double longitudeDifference = Math.toRadians(this.longitude - sourceLongitude);
        double latitudeDifference = Math.toRadians(this.latitude - sourceLatitude);

        double x = longitudeDifference * Math.cos(Math.toRadians((this.latitude + sourceLatitude) / 2));
        return EARTH_RADIUS_M * (Math.sqrt(x * x + latitudeDifference * latitudeDifference));
    }

    public int getId() {
        return this.id;
    }
    public double getXCoordinate() {
        return this.xCoordinate;
    }
    public double getYCoordinate() {
        return this.yCoordinate;
    }
    public double getLongitude() {
        return this.longitude;
    }
    public double getLatitude() {
        return this.latitude;
    }
}
