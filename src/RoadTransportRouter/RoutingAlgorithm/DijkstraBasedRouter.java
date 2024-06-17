package src.RoadTransportRouter.RoutingAlgorithm;

import src.MultiModalRouter.DrivingQuery;
import src.RoadTransportRouter.OSMDataManager.*;

import java.util.LinkedHashMap;
import java.util.TreeMap;

public class DijkstraBasedRouter {

    public DrivingQueryResponse findShortestDrivingPath (DrivingQuery drivingQuery,
                                                         LinkedHashMap<Long, Node> nodes,
                                                         LinkedHashMap<Long, Link> links) {
        DrivingQueryResponse drivingQueryResponse = new DrivingQueryResponse();

        // Parse query information
        long originNodeId = drivingQuery.getOriginNodeId();
        long destinationNodeId = drivingQuery.getDestinationNodeId();

        // Initialize variables and collections for iterations
        TreeMap<Double, Long> visitedNodes = new TreeMap<>();
        TreeMap<Double, Link> activeLinks = new TreeMap<>();
        long currentNodeId = originNodeId;
        double currentTravelTime = 0;

        while (!visitedNodes.containsValue(destinationNodeId)) {
            for (long linkId : nodes.get(currentNodeId).getLinkIdList()) {
                Link linkUnderConsideration = links.get(linkId);
                double travelTimeToAdd = linkUnderConsideration.getLinkTravelTimeMin();
                double travelTimeToOtherNode = currentTravelTime + travelTimeToAdd;
                long otherNodeId = -1;

                if (linkUnderConsideration.getFirstNodeId() == currentNodeId) {
                    otherNodeId = linkUnderConsideration.getSecondNodeId();
                } else if (linkUnderConsideration.getSecondNodeId() == currentNodeId) {
                    otherNodeId = linkUnderConsideration.getFirstNodeId();
                }

                visitedNodes.put(travelTimeToOtherNode, otherNodeId);
            }
            currentNodeId = visitedNodes.firstEntry().getValue();
        }

        return drivingQueryResponse;
    }
}
