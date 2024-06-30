package src.RoadTransportRouter.OSMDataManager;

import java.util.ArrayList;

public class Node {    // Node IDs are present in the relevant hashmap
    private long nodeId;
    private double nodeLongitude;
    private double nodeLatitude;
    private ArrayList<Long> linkIdList;

    public double equiRectangularDistanceTo(double otherPointLongitude, double otherPointLatitude) {
        final int EARTH_RADIUS_M = 6371000;
        double longitudeDifference = Math.toRadians(this.nodeLongitude - otherPointLongitude);
        double latitudeDifference = Math.toRadians(this.nodeLatitude - otherPointLatitude);

        double x = longitudeDifference * Math.cos(Math.toRadians((this.nodeLatitude + otherPointLatitude) / 2));
        double y = latitudeDifference;
        return Math.sqrt(x * x + y * y) * EARTH_RADIUS_M;
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