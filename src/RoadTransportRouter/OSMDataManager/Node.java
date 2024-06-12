package src.RoadTransportRouter.OSMDataManager;

public class Node {
    private double nodeLongitude;
    private double nodeLatitude;
    private int numberLinks;

    Node(double nodeLongitude, double nodeLatitude) {
        this.nodeLongitude = nodeLongitude;
        this.nodeLatitude = nodeLatitude;
    }

    Node() {
        this(0D, 0D);
    }

    void setNumberLinks(int numberLinks) {
        this.numberLinks = numberLinks;
    }

    double getNodeLongitude() {
        return this.nodeLongitude;
    }

    double getNodeLatitude() {
        return this.nodeLatitude;
    }
}
