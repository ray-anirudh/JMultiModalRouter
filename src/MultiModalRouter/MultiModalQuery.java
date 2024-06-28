package src.MultiModalRouter;

public class MultiModalQuery {
    private double originLongitude;
    private double originLatitude;
    private int departureTime;  // Agent departs origin at this very time; no delays are considered
    private double destinationLongitude;
    private double destinationLatitude;

    MultiModalQuery(double originLongitude, double originLatitude, int departureTime, double destinationLongitude,
                    double destinationLatitude) {
        this.originLongitude = originLongitude;
        this.originLatitude = originLatitude;
        this.departureTime = departureTime;
        this.destinationLongitude = destinationLongitude;
        this.destinationLatitude = destinationLatitude;
    }

    double getOriginLongitude() {
        return this.originLongitude;
    }

    double getOriginLatitude() {
        return this.originLatitude;
    }

    int getDepartureTime() {
        return this.departureTime;
    }

    double getDestinationLongitude() {
        return this.destinationLongitude;
    }

    double getDestinationLatitude() {
        return this.destinationLatitude;
    }
}
