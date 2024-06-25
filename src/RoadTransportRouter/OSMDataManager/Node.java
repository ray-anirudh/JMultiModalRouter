package src.RoadTransportRouter.OSMDataManager;

import java.util.ArrayList;

public class Node {    // Node IDs are present in the relevant hashmap
    private double nodeLongitude;
    private double nodeLatitude;
    private ArrayList<Long> linkIdList;

    void setNodeLongitude(double nodeLongitude) {
        this.nodeLongitude = nodeLongitude;
    }

    void setNodeLatitude(double nodeLatitude) {
        this.nodeLatitude = nodeLatitude;
    }

    double getNodeLongitude() {
        return this.nodeLongitude;
    }

    double getNodeLatitude() {
        return this.nodeLatitude;
    }

    public ArrayList<Long> getLinkIdList() {
        return this.linkIdList;
    }

    public double haversineDistanceTo(double otherPointLongitude, double otherPointLatitude) {
        final int EARTH_RADIUS_KM = 6371;
        double longitudeDifference = Math.toRadians(this.nodeLongitude - otherPointLongitude);
        double latitudeDifference = Math.toRadians(this.nodeLatitude - otherPointLatitude);

        double x = longitudeDifference * Math.cos(Math.toRadians((this.nodeLatitude + otherPointLatitude) / 2));
        double y = latitudeDifference;
        return Math.sqrt(x * x + y * y);
    }
}