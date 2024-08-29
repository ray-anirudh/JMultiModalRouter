/**
 * Author: Anirudh Ray
 * Institution: Professorship of Traffic Engineering and Control, Technical University of Munich
 * Department: Mobility Systems Engineering, School of Engineering and Design
 * E-mail Address: Anirudh.Ray@tum.de
 * Purpose: Component of a Java-based multi-modal routing algorithm, built using RAPTOR, Dijkstra-algorithm, and
 * KD-Trees
 */

package src.NearestNeighbourFinder;

import src.PublicTransportRouter.GTFSDataManager.Stop;

public class KDTreeStop {   // Use for stop-based KD-Trees
    private Stop stop;  // KD-Tree stops are modelled on top of transit stops
    private KDTreeStop left;
    private KDTreeStop right;

    KDTreeStop(Stop stop) {
        this.stop = stop;
        this.left = this.right = null;
    }

    public void setLeft(KDTreeStop left) {
        this.left = left;
    }

    public void setRight(KDTreeStop right) {
        this.right = right;
    }

    public Stop getStop() {
        return this.stop;
    }

    public KDTreeStop getLeft() {
        return this.left;
    }

    public KDTreeStop getRight() {
        return this.right;
    }
}