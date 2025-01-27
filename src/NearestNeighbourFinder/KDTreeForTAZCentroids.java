package src.NearestNeighbourFinder;

import src.MultiModalRouter.TAZManager.TAZCentroid;

import java.util.Arrays;
import java.util.Comparator;

public class KDTreeForTAZCentroids {
    private KDTreeTAZCentroid kDTreeRootTAZCentroid;    // Represents the root node of the tree

    /**
     * BEHAVIOUR DEFINITIONS
     * For fast nearest-neighbour searches, methods below are executed to build and query TAZ centroid-based KD-Trees
     */

    private KDTreeTAZCentroid buildKDTreeForTAZCentroids(TAZCentroid[] tAZCentroids, int depth) {
        if ((tAZCentroids == null) || (tAZCentroids.length == 0)) {
            return null;
        }

        int axis = depth % 2;
        Arrays.sort(tAZCentroids, Comparator.comparingDouble(tAZCentroid -> (axis == 0) ? tAZCentroid.getLatitude() :
                tAZCentroid.getLongitude()));

        int medianIndex = tAZCentroids.length / 2;
        KDTreeTAZCentroid tAZCentroid = new KDTreeTAZCentroid(tAZCentroids[medianIndex]);

        tAZCentroid.setLeft(buildKDTreeForTAZCentroids(Arrays.copyOfRange(tAZCentroids, 0, medianIndex),
                depth + 1);
        tAZCentroid.setRight(buildKDTreeForTAZCentroids(Arrays.copyOfRange(tAZCentroids, medianIndex + 1,
                tAZCentroids.length), depth + 1));

        return tAZCentroid;
    }

    // Build TAZ centroid-based KD-Tree
    public void buildTAZCentroidBasedKDTree(TAZCentroid[] tAZCentroids) {
        this.kDTreeRootTAZCentroid = buildKDTreeForTAZCentroids(tAZCentroids, 0);
        System.out.println("KD-Tree created for TAZ centroids.");
    }

    private KDTreeTAZCentroid nearestNeighbourSearchForTAZCentroids(double sourceLongitude, double sourceLatitude,
                                                                    KDTreeTAZCentroid kDTreeTAZCentroid,
                                                                    KDTreeTAZCentroid bestKDTreeTAZCentroid,
                                                                    int depth) {
        if (kDTreeTAZCentroid == null) {
            return bestKDTreeTAZCentroid;
        }

        double distance = kDTreeTAZCentroid.getTAZCentroid().equiRectangularDistanceTo(sourceLongitude, sourceLatitude);
        double bestDistance = bestKDTreeTAZCentroid.getTAZCentroid().equiRectangularDistanceTo(sourceLongitude,
                sourceLatitude);

        if (distance < bestDistance) {
            bestKDTreeTAZCentroid = kDTreeTAZCentroid;
        }

        int axis = depth % 2;
        KDTreeTAZCentroid nextKDTreeTAZCentroid = ((axis == 0) ? (sourceLatitude < kDTreeTAZCentroid.getTAZCentroid().
                getLatitude()) : (sourceLongitude < kDTreeTAZCentroid.getTAZCentroid().getLongitude())) ?
                kDTreeTAZCentroid.getLeft() : kDTreeTAZCentroid.getRight();
        KDTreeTAZCentroid otherKDTreeTAZCentroid = (nextKDTreeTAZCentroid == kDTreeTAZCentroid.getLeft()) ?
                kDTreeTAZCentroid.getRight() : kDTreeTAZCentroid.getLeft();

        bestKDTreeTAZCentroid = nearestNeighbourSearchForTAZCentroids(sourceLongitude, sourceLatitude,
                nextKDTreeTAZCentroid, bestKDTreeTAZCentroid, depth + 1);

        double axisDistance = (axis == 0) ?
                Math.abs(kDTreeTAZCentroid.getTAZCentroid().getLatitude() - sourceLatitude) * 111_320 :
                Math.abs(kDTreeTAZCentroid.getTAZCentroid().getLongitude() - sourceLongitude) * 111_320 *
                        Math.cos(Math.toRadians(kDTreeTAZCentroid.getTAZCentroid().getLatitude()));

        if (axisDistance < bestDistance) {
            bestKDTreeTAZCentroid = nearestNeighbourSearchForTAZCentroids(sourceLongitude, sourceLatitude,
                    otherKDTreeTAZCentroid, bestKDTreeTAZCentroid, depth + 1);
        }

        return bestKDTreeTAZCentroid;
    }

    public TAZCentroid findNearestTAZCentroid(double sourceLongitude, double sourceLatitude) {
        if (this.kDTreeRootTAZCentroid == null) {
            throw new IllegalStateException("TAZ centroid-based KD-Tree is empty.")
        }

        KDTreeTAZCentroid bestKDTreeTAZCentroid = nearestNeighbourSearchForTAZCentroids(sourceLongitude, sourceLatitude,
                this.kDTreeRootTAZCentroid, this.kDTreeRootTAZCentroid, 0);
        return bestKDTreeTAZCentroid.getTAZCentroid();
    }
}
