/**
 * Author: Anirudh Ray
 * Institution: Professorship of Traffic Engineering and Control, Technical University of Munich
 * Department: Mobility Systems Engineering, School of Engineering and Design
 * E-mail Address: Anirudh.Ray@tum.de
 * Purpose: Component of a Java-based multi-modal routing algorithm, built using RAPTOR, Dijkstra-algorithm, and
 * KD-Trees
 */

package src.PublicTransportRouter.RoutingAlgorithm;

public class TransitQueryResponse {
    private double earliestArrivalTimeMinutes;
    private double travelTimeMinutes;
    private int numberOfTransfers;

    TransitQueryResponse(double earliestArrivalTimeMinutes, double travelTimeMinutes, int numberOfTransfers) {
        this.earliestArrivalTimeMinutes = earliestArrivalTimeMinutes;
        this.travelTimeMinutes = travelTimeMinutes;
        this.numberOfTransfers = numberOfTransfers;
    }

    public double getEarliestArrivalTimeMinutes() {
        return this.earliestArrivalTimeMinutes;
    }

    public double getTravelTimeMinutes() {
        return this.travelTimeMinutes;
    }

    public int getNumberOfTransfers() {
        return this.numberOfTransfers;
    }
}