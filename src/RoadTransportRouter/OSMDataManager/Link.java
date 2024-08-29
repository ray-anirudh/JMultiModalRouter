/**
 * Author: Anirudh Ray
 * Institution: Professorship of Traffic Engineering and Control, Technical University of Munich
 * Department: Mobility Systems Engineering, School of Engineering and Design
 * E-mail Address: Anirudh.Ray@tum.de
 * Purpose: Component of a Java-based multi-modal routing algorithm, built using RAPTOR, Dijkstra-algorithm, and
 * KD-Trees
 */

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