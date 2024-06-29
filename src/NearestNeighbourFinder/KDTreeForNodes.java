package src.NearestNeighbourFinder;

import src.RoadTransportRouter.OSMDataManager.Node;

import java.util.Arrays;
import java.util.Comparator;

public class KDTreeForNodes {

    /**
     * ATTRIBUTE DEFINITIONS
     */

    private KDTreeNode kDTreeRootNode;    // Represents the root (highest level) node of the tree

    /**
     * BEHAVIOUR DEFINITIONS
     */

    /**
     * For fast nearest-neighbour searches, the below methods are executed to build and query node-based KD-Trees
     */

    private KDTreeNode buildKDTreeForNodes(Node[] nodes, int depth) {
        if ((nodes == null) || (nodes.length == 0)) {
            return null;
        }

        int axis = depth % 2;
        Arrays.sort(nodes, Comparator.comparingDouble(node -> (axis == 0) ? node.getNodeLatitude() : node.
                getNodeLongitude()));

        int medianIndex = nodes.length / 2;     // Indexing for roots of new subtrees
        KDTreeNode node = new KDTreeNode(nodes[medianIndex]);
        // Setting up roots of new subtrees; nodes is ascribed to a KDTreeNode

        node.setLeft(buildKDTreeForNodes(Arrays.copyOfRange(nodes, 0, medianIndex), depth + 1));
        node.setRight(buildKDTreeForNodes(Arrays.copyOfRange(nodes, medianIndex + 1, nodes.length),
                depth + 1));

        return node;
    }

    public void buildNodeBasedKDTree(Node[] nodes) {
        this.kDTreeRootNode = buildKDTreeForNodes(nodes, 0);
    }

    private KDTreeNode nearestNeighbourSearchForNodes(double sourceLongitude, double sourceLatitude,
                                                      KDTreeNode kDTreeNode, KDTreeNode bestKDTreeNode, int depth) {
        if (kDTreeNode == null) {
            return bestKDTreeNode;
        }

        double distance = kDTreeNode.getNode().equiRectangularDistanceTo(sourceLongitude, sourceLatitude);
        double bestDistance = bestKDTreeNode.getNode().equiRectangularDistanceTo(sourceLongitude, sourceLatitude);

        if (distance < bestDistance) {
            bestKDTreeNode = kDTreeNode;
        }

        int axis = depth % 2;
        KDTreeNode nextKDTreeNode = (((axis == 0) ? sourceLatitude < kDTreeNode.getNode().getNodeLatitude() :
                sourceLongitude < kDTreeNode.getNode().getNodeLongitude())) ? kDTreeNode.getLeft() :
                kDTreeNode.getRight();
        KDTreeNode otherKDTreeNode = (nextKDTreeNode == kDTreeNode.getLeft()) ? kDTreeNode.getRight() : kDTreeNode.
                getLeft();

        bestKDTreeNode = nearestNeighbourSearchForNodes(sourceLongitude, sourceLatitude, nextKDTreeNode, bestKDTreeNode,
                depth + 1);
        double axisDistance = (axis == 0) ? Math.abs(kDTreeNode.getNode().getNodeLatitude() - sourceLatitude) :
                Math.abs(kDTreeNode.getNode().getNodeLongitude() - sourceLongitude);

        if (axisDistance < bestDistance) {
            bestKDTreeNode = nearestNeighbourSearchForNodes(sourceLongitude, sourceLatitude, otherKDTreeNode, bestKDTreeNode,
                    depth + 1);
        }
        return bestKDTreeNode;
    }

    // Find the nearest node to a source point from amongst a set of nodes
    public Node findNearestNode(double sourceLongitude, double sourceLatitude) {
        if (kDTreeRootNode == null) {
            throw new IllegalStateException("KD-Tree is empty.");
        }

        KDTreeNode bestKDTreeNode = nearestNeighbourSearchForNodes(sourceLongitude, sourceLatitude, this.kDTreeRootNode,
                this.kDTreeRootNode, 0);
        return bestKDTreeNode.getNode();
    }
}