package src.NearestNeighbourFinder;

import src.RoadTransportRouter.OSMDataManager.Node;

public class KDTreeNode {
    Node node;
    KDTreeNode left, right;

    KDTreeNode(Node node) {
        this.node = node;
        this.left = this.right = null;
    }
}
