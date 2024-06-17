package src.RoadTransportRouter.RoutingAlgorithm;

import src.MultiModalRouter.DrivingQuery;
import src.RoadTransportRouter.OSMDataManager.*;

import java.util.LinkedHashMap;

public class DijkstraBasedRouter {

    public DrivingQueryResponse findShortestDrivingPath (DrivingQuery drivingQuery,
                                                         LinkedHashMap<Long, Node> nodes,
                                                         LinkedHashMap<Long, Link> links) {
        DrivingQueryResponse drivingQueryResponse = new DrivingQueryResponse();

        // Parse query information
        long originNodeId = drivingQuery.getOriginNodeId();
        long destinationNodeId = drivingQuery.getDestinationNodeId();
        long desiredDepartureTimeMinutes = drivingQuery.getDesiredDepartureTimeMinutes();

        return drivingQueryResponse;
    }
}
