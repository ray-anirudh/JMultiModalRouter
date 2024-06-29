package src.NearestNeighbourFinder;

import src.PublicTransportRouter.GTFSDataManager.Stop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class KDTreeForStops {

    /**
     * ATTRIBUTE DEFINITIONS
     */

    private KDTreeStop kDTreeRootStop;    // Represents the root (highest level) stop of the tree

    /**
     * BEHAVIOUR DEFINITIONS
     */

    /**
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
        KDTreeStop stop = new KDTreeStop(stops[medianIndex]);
        // Setting up roots of new subtrees; stop is ascribed to a KDTreeStop

        stop.setLeft(buildKDTreeForStops(Arrays.copyOfRange(stops, 0, medianIndex), depth + 1));
        stop.setRight(buildKDTreeForStops(Arrays.copyOfRange(stops, medianIndex + 1, stops.length),
                depth + 1));

        return stop;
    }

    public void buildStopBasedKDTree(Stop[] stops) {
        this.kDTreeRootStop = buildKDTreeForStops(stops, 0);
    }

    public void doughnutSearchForStops(double sourceLongitude, double sourceLatitude, ArrayList<Stop> nearbyStops,
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

        if (sourceValue - outerRadius <= nodeValue) {
            doughnutSearchForStops(sourceLongitude, sourceLatitude, nearbyStops, kDTreeStop.getLeft(), innerRadius,
                    outerRadius, depth + 1);
        }

        if (sourceValue + outerRadius >= nodeValue) {
            doughnutSearchForStops(sourceLongitude, sourceLatitude, nearbyStops, kDTreeStop.getRight(), innerRadius,
                    outerRadius, depth + 1);
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
}