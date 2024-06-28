package src.NearestNeighbourFinder;

import src.RoadTransportRouter.OSMDataManager.Node;

import java.util.Arrays;
import java.util.Comparator;

public class KDTree {

    /**
     * ATTRIBUTE DEFINITIONS
     */

    private KDTreeNode kDTreeRootNode;    // Represents the root (highest level) node of the tree

    /**
     * BEHAVIOUR DEFINITIONS
     */

    /**
     * For fast nearest-neighbour searches, the below methods are executed to build and query KD-Trees
     */

    KDTreeNode buildKDTree(Node[] nodes, int depth) {
        if ((nodes == null) || (nodes.length == 0)) {
            return null;
        }

        int axis = depth % 2;
        Arrays.sort(nodes, Comparator.comparingDouble(node -> (axis == 0) ? node.getNodeLatitude() : node.
                getNodeLongitude()));

        int medianIndex = nodes.length / 2;     // Indexing for roots of new subtrees
        KDTreeNode node = new KDTreeNode(nodes[medianIndex]);   // Setting up roots of new subtrees

        node.setLeft(buildKDTree(Arrays.copyOfRange(nodes, 0, medianIndex), depth + 1));
        node.setRight(buildKDTree(Arrays.copyOfRange(nodes, medianIndex + 1, nodes.length), depth + 1));

        return node;
    }

    public void build(Node[] nodes) {
        this.kDTreeRootNode = buildKDTree(nodes, 0);
    }

    public KDTreeNode nearestNeighbourSearch(double sourceLongitude, double sourceLatitude,
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

        bestKDTreeNode = nearestNeighbourSearch(sourceLongitude, sourceLatitude, nextKDTreeNode, bestKDTreeNode,
                depth + 1);
        double axisDistance = (axis == 0) ? Math.abs(kDTreeNode.getNode().getNodeLatitude() - sourceLatitude) :
                Math.abs(kDTreeNode.getNode().getNodeLongitude() - sourceLongitude);

        if (axisDistance < bestDistance) {
            bestKDTreeNode = nearestNeighbourSearch(sourceLongitude, sourceLatitude, otherKDTreeNode, bestKDTreeNode,
                    depth + 1);
        }
        return bestKDTreeNode;
    }

    // Find the nearest node to a source point from amongst a set of nodes
    public Node findNearest(double sourceLongitude, double sourceLatitude) {
        if (kDTreeRootNode == null) {
            throw new IllegalStateException("KD-Tree is empty.");
        }

        KDTreeNode bestKDTreeNode = nearestNeighbourSearch(sourceLongitude, sourceLatitude, kDTreeRootNode,
                kDTreeRootNode, 0);
        return bestKDTreeNode.getNode();
    }

    // Determine snapping cost, which is the cost of getting from one point to another (aerial distance) on foot
    public double calculateSnappingCost(double longitudeOne, double latitudeOne, double longitudeTwo,
                                        double latitudeTwo) {
        final int EARTH_RADIUS_M = 6371000;
        final double AVERAGE_WALKING_SPEED_MS = 1.42;

        // Determine and return the equi-rectangular distance
        double longitudeDifference = Math.toRadians(longitudeTwo - longitudeOne);
        double latitudeDifference = Math.toRadians(latitudeTwo - latitudeOne);
        double x = longitudeDifference * Math.cos(Math.toRadians((latitudeTwo + latitudeOne) / 2));
        double y = latitudeDifference;
        return ((Math.sqrt(x * x + y * y) * EARTH_RADIUS_M) / AVERAGE_WALKING_SPEED_MS);
    }
}