package src.NearestNeighbourFinder;

import src.PublicTransportRouter.GTFSDataManager.Stop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class KDTreeForStops {
    private KDTreeStop kDTreeRootStop;    // Represents the root (highest level) stop of the tree

    /**
     * BEHAVIOUR DEFINITIONS
     * For fast nearest-neighbour searches, the below methods are executed to build and query stop-based KD-Trees
     */

    private KDTreeStop buildKDTreeForStops(Stop[] stops, int depth) {
        if ((stops == null) || (stops.length == 0)) {
            return null;
        }

        int axis = depth % 2;
        Arrays.sort(stops, Comparator.comparingDouble(stop -> (axis == 0) ? stop.getStopLatitude() : stop.
                getStopLongitude()));

        int medianIndex = stops.length / 2;     // Indexing for roots of new subtrees
        KDTreeStop stop = new KDTreeStop(stops[medianIndex]);   // Setting up stop-based roots of new subtrees

        stop.setLeft(buildKDTreeForStops(Arrays.copyOfRange(stops, 0, medianIndex), depth + 1));
        stop.setRight(buildKDTreeForStops(Arrays.copyOfRange(stops, medianIndex + 1, stops.length),
                depth + 1));

        return stop;
    }

    // Build a KD-Tree to find all stops in a certain vicinity, or the nearest possible stop
    public void buildStopBasedKDTree(Stop[] stops) {
        this.kDTreeRootStop = buildKDTreeForStops(stops, 0);
        System.out.println("KD-Tree created for stops");
    }

    private void doughnutSearchForStops(double sourceLongitude, double sourceLatitude, ArrayList<Stop> nearbyStops,
                                        KDTreeStop kDTreeStop, double innerRadius, double outerRadius, int depth) {
        if (kDTreeStop == null) {
            return;
        }

        double distance = kDTreeStop.getStop().equiRectangularDistanceTo(sourceLongitude, sourceLatitude);

        if ((distance > innerRadius) && (distance <= outerRadius)) {
            nearbyStops.add(kDTreeStop.getStop());
        }

        int axis = depth % 2;
        double nodeValue = (axis == 0) ? kDTreeStop.getStop().getStopLatitude() : kDTreeStop.getStop().
                getStopLongitude();
        double sourceValue = (axis == 0) ? sourceLatitude : sourceLongitude;

        double latConversion = 111320; // Meters per degree of latitude
        double longConversion = latConversion * Math.cos(Math.toRadians(sourceLatitude));

        double axisDistance = (axis == 0)
                ? Math.abs(kDTreeStop.getStop().getStopLatitude() - sourceLatitude) * latConversion
                : Math.abs(kDTreeStop.getStop().getStopLongitude() - sourceLongitude) * longConversion;

        if (axisDistance <= outerRadius) {
            if (sourceValue - (outerRadius / (axis == 0 ? latConversion : longConversion)) <= nodeValue) {
                doughnutSearchForStops(sourceLongitude, sourceLatitude, nearbyStops, kDTreeStop.getLeft(), innerRadius,
                        outerRadius, depth + 1);
            }

            if (sourceValue + (outerRadius / (axis == 0 ? latConversion : longConversion)) >= nodeValue) {
                doughnutSearchForStops(sourceLongitude, sourceLatitude, nearbyStops, kDTreeStop.getRight(), innerRadius,
                        outerRadius, depth + 1);
            }
        }
    }

    // Find a set of stops (from amongst a larger set) within a specified distance range from a source point
    public ArrayList<Stop> findStopsWithinDoughnut(double sourceLongitude, double sourceLatitude, double innerRadius,
                                                   double outerRadius) {
        ArrayList<Stop> nearbyStops = new ArrayList<>();

        doughnutSearchForStops(sourceLongitude, sourceLatitude, nearbyStops, this.kDTreeRootStop, innerRadius,
                outerRadius, 0);
        return nearbyStops;
    }

    private KDTreeStop nearestNeighbourSearchForStops(double sourceLongitude, double sourceLatitude,
                                                      KDTreeStop kDTreeStop, KDTreeStop bestKDTreeStop, int depth) {
        if (kDTreeStop == null) {
            return bestKDTreeStop;
        }

        double distance = kDTreeStop.getStop().equiRectangularDistanceTo(sourceLongitude, sourceLatitude);
        double bestDistance = bestKDTreeStop.getStop().equiRectangularDistanceTo(sourceLongitude, sourceLatitude);

        if (distance < bestDistance) {
            bestKDTreeStop = kDTreeStop;
        }

        int axis = depth % 2;
        KDTreeStop nextKDTreeStop = ((axis == 0) ? (sourceLatitude < kDTreeStop.getStop().getStopLatitude()) :
                (sourceLongitude < kDTreeStop.getStop().getStopLongitude())) ? kDTreeStop.getLeft() :
                kDTreeStop.getRight();
        KDTreeStop otherKDTreeStop = (nextKDTreeStop == kDTreeStop.getLeft()) ? kDTreeStop.getRight() :
                kDTreeStop.getLeft();

        bestKDTreeStop = nearestNeighbourSearchForStops(sourceLongitude, sourceLatitude, nextKDTreeStop, bestKDTreeStop,
                depth + 1);

        double axisDistance = (axis == 0) ?
                Math.abs(kDTreeStop.getStop().getStopLatitude() - sourceLatitude) * 111_320 :
                Math.abs(kDTreeStop.getStop().getStopLongitude() - sourceLongitude) * 111_320 *
                        Math.cos(Math.toRadians(kDTreeStop.getStop().getStopLatitude()));

        // Search goes in the direction of the other stop if the next stop is deemed to be a suboptimal option
        if (axisDistance < bestDistance) {
            bestKDTreeStop = nearestNeighbourSearchForStops(sourceLongitude, sourceLatitude, otherKDTreeStop,
                    bestKDTreeStop, depth + 1);
        }
        return bestKDTreeStop;
    }

    // Find the nearest stop to a source point from amongst a set of stops
    public Stop findNearestStop(double sourceLongitude, double sourceLatitude) {
        if (this.kDTreeRootStop == null) {
            throw new IllegalStateException("Stop-based KD-Tree is empty.");
        }

        KDTreeStop bestKDTreeStop = nearestNeighbourSearchForStops(sourceLongitude, sourceLatitude, this.kDTreeRootStop,
                this.kDTreeRootStop, 0);
        return bestKDTreeStop.getStop();
    }
}