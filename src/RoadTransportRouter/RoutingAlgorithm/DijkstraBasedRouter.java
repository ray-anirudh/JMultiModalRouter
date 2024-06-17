package src.RoadTransportRouter.RoutingAlgorithm;

import src.RoadTransportRouter.OSMDataManager.*;

import java.util.LinkedHashMap;
import java.util.TreeMap;

public class DijkstraBasedRouter {

    public double findShortestDrivingPath (long originNodeId,
                                           long destinationNodeId,
                                           LinkedHashMap<Long, Node> nodes,
                                           LinkedHashMap<Long, Link> links) {

        // Initialize variables and collections for iterations
        TreeMap<Double, Long> visitedNodes = new TreeMap<>();
        visitedNodes.put(0D, originNodeId);    // Departure time is treated as 0

        // Execute Dijkstra algorithm
        while (!(visitedNodes.firstEntry().getValue().equals(destinationNodeId))) {
            long currentNodeId = visitedNodes.firstEntry().getValue();
            double baselineTravelTime = visitedNodes.firstEntry().getKey();

            for (long linkId : nodes.get(currentNodeId).getLinkIdList()) {
                Link linkUnderConsideration = links.get(linkId);
                double totalTravelTimeToOtherNode = baselineTravelTime + linkUnderConsideration.getLinkTravelTimeMin();

                long otherNodeId = -1;
                if (linkUnderConsideration.getFirstNodeId() == currentNodeId) {
                    otherNodeId = linkUnderConsideration.getSecondNodeId();
                } else if (linkUnderConsideration.getSecondNodeId() == currentNodeId) {
                    otherNodeId = linkUnderConsideration.getFirstNodeId();
                }
                visitedNodes.put(totalTravelTimeToOtherNode, otherNodeId);
            }
        }

        // Return the travel time in minutes
        return visitedNodes.firstKey();
    }
}