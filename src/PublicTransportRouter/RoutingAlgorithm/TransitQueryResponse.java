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

    TransitQueryResponse(double earliestArrivalTimeMinutes, double travelTimeMinutes) {
        this.earliestArrivalTimeMinutes = earliestArrivalTimeMinutes;
        this.travelTimeMinutes = travelTimeMinutes;
    }

    public double getEarliestArrivalTimeMinutes() {
        return this.earliestArrivalTimeMinutes;
    }

    public double getTravelTimeMinutes() {
        return this.travelTimeMinutes;
    }
}