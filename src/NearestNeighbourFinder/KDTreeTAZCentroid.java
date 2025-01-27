package src.NearestNeighbourFinder;

import src.MultiModalRouter.TAZManager.TAZCentroid;

public class KDTreeTAZCentroid {
    private TAZCentroid tAZCentroid;
    private KDTreeTAZCentroid left;
    private KDTreeTAZCentroid right;

    KDTreeTAZCentroid(TAZCentroid tAZCentroid) {
        this.tAZCentroid = tAZCentroid;
        this.left = this.right = null;
    }

    public void setLeft(KDTreeTAZCentroid left) {
        this.left = left;
    }

    public void setRight(KDTreeTAZCentroid right) {
        this.right = right;
    }

    public TAZCentroid getTAZCentroid() {
        return this.tAZCentroid;
    }

    public KDTreeTAZCentroid getLeft() {
        return this.left;
    }

    public KDTreeTAZCentroid getRight() {
        return this.right;
    }
}
