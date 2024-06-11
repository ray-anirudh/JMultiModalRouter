package src.RoadTransportRouter.OSMDataParser;

public class Node {
    private double nodeLongitude;
    private double nodeLatitude;
    private int associatedLinksCount;

    Node(double nodeLongitude, double nodeLatitude) {
        this.nodeLongitude = nodeLongitude;
        this.nodeLatitude = nodeLatitude;
    }

    Node() {
        this(0D, 0D);
    }

    void setAssociatedLinksCount(int associatedLinksCount) {
        this.associatedLinksCount = associatedLinksCount;
    }

    double getNodeLongitude() {
        return this.nodeLongitude;
    }

    double getNodeLatitude() {
        return this.nodeLatitude;
    }
}
