package src.RoadTransportRouter.OSMDataManager;

import java.util.ArrayList;

public class Node {    // Node IDs are present in the relevant hashmap
    private double nodeLongitude;
    private double nodeLatitude;
    private ArrayList<Long> linkIdList;

    public void setNodeLongitude(double nodeLongitude) {
        this.nodeLongitude = nodeLongitude;
    }

    public void setNodeLatitude(double nodeLatitude) {
        this.nodeLatitude = nodeLatitude;
    }

    double getNodeLongitude() {
        return this.nodeLongitude;
    }

    double getNodeLatitude() {
        return this.nodeLatitude;
    }

    ArrayList<Long> getLinkIdList() {
        return this.linkIdList;
    }
}