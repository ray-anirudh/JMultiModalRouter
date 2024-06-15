package src.RoadTransportRouter.OSMDataManager;

public class Link {    // Link IDs are present in the relevant hashmap
    private long firstNodeId;
    private long secondNodeId;
    private String linkType;
    private double linkLengthM;

    Link(long firstNodeId, long secondNodeId, String linkType) {
        this.firstNodeId = firstNodeId;
        this.secondNodeId = secondNodeId;
        this.linkType = linkType;
    }

    Link() {
        this(0, 0, null);
    }

    public void setLinkLengthM(double linkLengthM) {
        this.linkLengthM = linkLengthM;
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

    public double getLinkLengthM() {
        return this.linkLengthM;
    }
}