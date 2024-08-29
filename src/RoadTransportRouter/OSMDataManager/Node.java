/**
 * Author: Anirudh Ray
 * Institution: Professorship of Traffic Engineering and Control, Technical University of Munich
 * Department: Mobility Systems Engineering, School of Engineering and Design
 * E-mail Address: Anirudh.Ray@tum.de
 * Purpose: Component of a Java-based multi-modal routing algorithm, built using RAPTOR, Dijkstra-algorithm, and
 * KD-Trees
 */

package src.RoadTransportRouter.OSMDataManager;

import java.util.ArrayList;

public class Node {    // Node IDs are present in the relevant hashmap, as well
    private long nodeId;
    private double nodeLongitude;
    private double nodeLatitude;
    private final ArrayList<Long> linkIdList;

    public Node(ArrayList<Long> linkIdList) {
        this.linkIdList = linkIdList;
    }

    public double equiRectangularDistanceTo(double otherPointLongitude, double otherPointLatitude) {
        final int EARTH_RADIUS_M = 6_371_000;
        double longitudeDifference = Math.toRadians(this.nodeLongitude - otherPointLongitude);
        double latitudeDifference = Math.toRadians(this.nodeLatitude - otherPointLatitude);

        double x = longitudeDifference * Math.cos(Math.toRadians((this.nodeLatitude + otherPointLatitude) / 2));
        return EARTH_RADIUS_M * (Math.sqrt(x * x + latitudeDifference * latitudeDifference));
    }

    void setNodeId(long nodeId) {
        this.nodeId = nodeId;
    }

    void setNodeLongitude(double nodeLongitude) {
        this.nodeLongitude = nodeLongitude;
    }

    void setNodeLatitude(double nodeLatitude) {
        this.nodeLatitude = nodeLatitude;
    }

    public long getNodeId() {
        return this.nodeId;
    }

    public double getNodeLongitude() {
        return this.nodeLongitude;
    }

    public double getNodeLatitude() {
        return this.nodeLatitude;
    }

    public ArrayList<Long> getLinkIdList() {
        return this.linkIdList;
    }
}