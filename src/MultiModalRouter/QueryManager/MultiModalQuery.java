/**
 * Author: Anirudh Ray
 * Institution: Professorship of Traffic Engineering and Control, Technical University of Munich
 * Department: Mobility Systems Engineering, School of Engineering and Design
 * E-mail Address: Anirudh.Ray@tum.de
 * Purpose: Component of a Java-based multi-modal routing algorithm, built using RAPTOR, Dijkstra-algorithm, and
 * KD-Trees
 */

package src.MultiModalRouter.QueryManager;

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

    public double getOriginLongitude() {
        return this.originLongitude;
    }

    public double getOriginLatitude() {
        return this.originLatitude;
    }

    public int getDepartureTime() {
        return this.departureTime;
    }

    public double getDestinationLongitude() {
        return this.destinationLongitude;
    }

    public double getDestinationLatitude() {
        return this.destinationLatitude;
    }
}