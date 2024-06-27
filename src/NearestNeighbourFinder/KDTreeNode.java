package src.NearestNeighbourFinder;

import src.RoadTransportRouter.OSMDataManager.Node;

public class KDTreeNode {
    private Node node;
    private KDTreeNode left;
    private KDTreeNode right;

    KDTreeNode(Node node) {
        this.node = node;
        this.left = this.right = null;
    }

    public void setLeft(KDTreeNode left) {
        this.left = left;
    }

    public void setRight(KDTreeNode right) {
        this.right = right;
    }

    public Node getNode() {
        return this.node;
    }

    public KDTreeNode getLeft() {
        return this.left;
    }

    public KDTreeNode getRight() {
        return this.right;
    }
}
