/**
 * Author: Anirudh Ray
 * Institution: Professorship of Traffic Engineering and Control, Technical University of Munich
 * Department: Mobility Systems Engineering, School of Engineering and Design
 * E-mail Address: Anirudh.Ray@tum.de
 * Purpose: Component of a Java-based multi-modal routing algorithm, built using RAPTOR, Dijkstra-algorithm, and
 * KD-Trees
 */

package src.PublicTransportRouter.GTFSDataManager;

public class StopTimeTriplet {
    private int stopSequence;
    private double arrivalTime;
    private double departureTime;

    StopTimeTriplet(int stopSequence, double arrivalTime, double departureTime) {
        this.stopSequence = stopSequence;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
    }

    public int getStopSequence() {
        return this.stopSequence;
    }
    public double getArrivalTime() {
        return this.arrivalTime;
    }
    public double getDepartureTime() {
        return this.departureTime;
    }
}