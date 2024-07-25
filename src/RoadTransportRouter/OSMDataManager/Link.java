package src.RoadTransportRouter.OSMDataManager;

public class Link {    // Link IDs are present in the relevant hashmap
    private long firstNodeId;
    private long secondNodeId;  // "first" and "second" in node nomenclature do not signify any order
    private String linkType;
    private double linkTravelTimeMin;

    Link(long firstNodeId, long secondNodeId, String linkType) {
        this.firstNodeId = firstNodeId;
        this.secondNodeId = secondNodeId;
        this.linkType = linkType;
    }

    void setLinkTravelTimeMin(double linkTravelTimeMin) {
        this.linkTravelTimeMin = linkTravelTimeMin;
    }

    public long getFirstNodeId() {
        return this.firstNodeId;
    }

    public long getSecondNodeId() {
        return this.secondNodeId;
    }

    public String getLinkType() {
        return this.linkType;
    }

    public double getLinkTravelTimeMin() {
        return this.linkTravelTimeMin;
    }
}