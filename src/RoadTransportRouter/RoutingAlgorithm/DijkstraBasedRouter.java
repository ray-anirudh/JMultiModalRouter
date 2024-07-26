package src.RoadTransportRouter.RoutingAlgorithm;

import src.RoadTransportRouter.OSMDataManager.*;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.TreeMap;

public class DijkstraBasedRouter {

    public double findShortestDrivingPath (long originNodeId,
                                           long destinationNodeId,
                                           LinkedHashMap<Long, Node> nodes,
                                           LinkedHashMap<Long, Link> links) {

        double travelTimeMin = 0;

        // Initialize variables and collections for iterations
        TreeMap<Double, Long> visitedNodes = new TreeMap<>();
        visitedNodes.put(0D, originNodeId);    // Arrival time for origin node is treated as zero
        TreeMap<Double, Long> nodesUnderEvaluation = new TreeMap<>();
        HashSet<Long> traversedLinksIds = new HashSet<>();

        // Execute the Dijkstra algorithm
        while(!(visitedNodes.lastEntry().getValue().equals(destinationNodeId))) {
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
            visitedNodes.put(nodesUnderEvaluation.firstEntry().getKey(), nodesUnderEvaluation.firstEntry().getValue());
            nodesUnderEvaluation.remove(nodesUnderEvaluation.firstKey());
        }

        // Return the travel time in minutes
        travelTimeMin = visitedNodes.lastKey();
        return travelTimeMin;
    }
}