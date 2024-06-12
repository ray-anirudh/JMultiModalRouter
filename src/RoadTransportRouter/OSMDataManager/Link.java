package src.RoadTransportRouter.OSMDataManager;

public class Link {
    private int firstNodeId;
    private int secondNodeId;
    private String linkType;
    private double linkLengthM;
    private static final int EARTH_RADIUS_M = 6371000;

    Link(int firstNodeId, int secondNodeId) {
        this.firstNodeId = firstNodeId;
        this.secondNodeId = secondNodeId;

//        double firstNodeLongitudeRadians = Math.toRadians(firstNodeId.getNodeLongitude());
//        double firstNodeLatitudeRadians = Math.toRadians(firstNodeId.getNodeLatitude());
//
//        double secondNodeLongitudeRadians = Math.toRadians(secondNodeId.getNodeLongitude());
//        double secondNodeLatitudeRadians = Math.toRadians(secondNodeId.getNodeLatitude());
//
//        double latitudeDifference = secondNodeLatitudeRadians - firstNodeLatitudeRadians;
//        double longitudeDifference = secondNodeLongitudeRadians - firstNodeLongitudeRadians;
//
//        double x = longitudeDifference * Math.cos((firstNodeLatitudeRadians + secondNodeLatitudeRadians) / 2);
//        double y = latitudeDifference;
//
//        this.linkLengthM = Math.sqrt(x * x + y * y) * EARTH_RADIUS_M;
    }

    Link() {
        this(0, 0);
    }

    public void setLinkType(String linkType) {
        this.linkType = linkType;
    }

    public int getFirstNode() {
        return this.firstNodeId;
    }

    public int getSecondNode() {
        return this.secondNodeId;
    }

    public String getLinkType() {
        return this.linkType;
    }

    public double getLinkLengthM() {
        return this.linkLengthM;
    }
}