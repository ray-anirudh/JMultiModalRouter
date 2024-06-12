package src.RoadTransportRouter.OSMDataManager;

public class Link {
    private int firstNodeId;
    private int secondNodeId;
    private String linkType;
    private double linkLengthM;

    Link(int firstNodeId, int secondNodeId, String linkType) {
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

    public int getFirstNodeId() {
        return this.firstNodeId;
    }

    public int getSecondNodeId() {
        return this.secondNodeId;
    }

    public String getLinkType() {
        return this.linkType;
    }

    public double getLinkLengthM() {
        return this.linkLengthM;
    }
}