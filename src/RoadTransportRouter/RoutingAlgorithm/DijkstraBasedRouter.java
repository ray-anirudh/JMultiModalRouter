package src.RoadTransportRouter.RoutingAlgorithm;

import src.RoadTransportRouter.OSMDataManager.*;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.TreeMap;

public class DijkstraBasedRouter {
    private final double DRIVING_VS_AERIAL_DISTANCE_FACTOR = 1.45;
    /* Refer to: https://link.springer.com/chapter/10.1007/978-3-030-12381-9_12 or
    https://ctl.mit.edu/pub/workingpaper/quantifying-impact-urban-road-networks-efficiency-local-trips for more details
    */

    private static final double AVERAGE_DRIVING_SPEED_M_PER_MIN = 483.33;
    // Refer to: https://www.tomtom.com/traffic-index/munich-traffic/; translates to approximately 29 km/h

    public double findShortestDrivingPathCostMin(long originNodeId,
                                                 long destinationNodeId,
                                                 LinkedHashMap<Long, Node> nodes,
                                                 LinkedHashMap<Long, Link> links) {
        double travelTimeMin;

        // Initialize variables and collections for iterations
        TreeMap<Double, Long> visitedNodes = new TreeMap<>();
        visitedNodes.put(0D, originNodeId);    // Arrival time for origin node is treated as zero
        TreeMap<Double, Long> nodesUnderEvaluation = new TreeMap<>();
        HashSet<Long> traversedLinksIds = new HashSet<>();

        // Execute the Dijkstra algorithm
        while (!(visitedNodes.lastEntry().getValue().equals(destinationNodeId))) {
            long currentNodeId = visitedNodes.lastEntry().getValue();
            double currentNodeTravelTimeMin = visitedNodes.lastEntry().getKey();

            for (long linkIdUnderConsideration : nodes.get(currentNodeId).getLinkIdList()) {
                if (!traversedLinksIds.contains(linkIdUnderConsideration)) {
                    Link linkUnderConsideration = links.get(linkIdUnderConsideration);
                    double travelTimeToOtherNodeMin = currentNodeTravelTimeMin + linkUnderConsideration.
                            getLinkTravelTimeMin();
                    long otherNodeId = (linkUnderConsideration.getFirstNodeId() == currentNodeId) ?
                            linkUnderConsideration.getSecondNodeId() : linkUnderConsideration.getFirstNodeId();

                    // Update the collections of nodes yet to be relaxed and of links whose travel times are known
                    nodesUnderEvaluation.put(travelTimeToOtherNodeMin, otherNodeId);
                    traversedLinksIds.add(linkIdUnderConsideration);
                }
            }

            // Add the cheapest node to the collection of visited nodes, thereby removing it from under observation
            if (!nodesUnderEvaluation.isEmpty()) {
                visitedNodes.put(nodesUnderEvaluation.firstKey(), nodesUnderEvaluation.firstEntry().getValue());
                nodesUnderEvaluation.remove(nodesUnderEvaluation.firstKey());
            } else {
                travelTimeMin = (nodes.get(originNodeId).equiRectangularDistanceTo(nodes.get(destinationNodeId).
                        getNodeLongitude(), nodes.get(destinationNodeId).getNodeLatitude()) *
                        DRIVING_VS_AERIAL_DISTANCE_FACTOR) / AVERAGE_DRIVING_SPEED_M_PER_MIN;
                return travelTimeMin;
            }

        }

        // Return the travel time in minutes
        travelTimeMin = visitedNodes.lastEntry().getKey();
        return travelTimeMin;
    }
}