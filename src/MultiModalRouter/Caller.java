package src.MultiModalRouter;
// TODO: HEURISTICS CAN BE BASED ON STOPS (STOPTYPE (ASCRIBED VIA ROUTETYPE - BIGGEST CURRENT CANDIDATE), PARENTSTOPPRESENCE), ROUTES (ROUTETYPES, #TRIPS, #STOPS)
// TODO: HEURISTIC DATA CAN BE ASCRIBED IN GTFSDATAMANAGER
// TODO: QUERY PARSING TO PEERFECT INPUT TYPES MUST HAPPEN IN THE CALLER, AND NOT HERE, OR IN THE QUERY CODE (SUCH AS LOCATING STOP AND THEN HOMING IN ON THE STOPIDS)
// TODO REVIEW ALL CODE TO GOD LEVEL PERFECTION BEFORE RUNNING THIS SHIT
// TODO DO TURURURURU DO LIKE A MASSSSSSSIVVVVE (DEEEEEEP, EVERY SINGLE LINE - TAKE 3-4 DAYS) REVIEW BEFORE YOU GET GOINGGGGG
// TODO BEAUTIFY EVERYTHING AFTER A SUCCESSFUL RUN

import src.NearestNeighbourFinder.KDTreeForNodes;
import src.PublicTransportRouter.GTFSDataManager.*;
import src.RoadTransportRouter.OSMDataManager.Link;
import src.RoadTransportRouter.OSMDataManager.Node;
import src.RoadTransportRouter.OSMDataManager.OSMDataReaderWriter;

import java.util.LinkedHashMap;

public class Caller {


//// Display results
//        for (HashMap.Entry<TransitQuery, Double> queryTravelTimeEntry : shortestTravelTimes.entrySet()) {
//        System.out.println("Origin stop: " + queryTravelTimeEntry.getKey().getOriginStopId() + "\n" +
//        "Destination stop: " + queryTravelTimeEntry.getKey().getDestinationStopId() + "\n" +
//        "Departure time: " + queryTravelTimeEntry.getKey().getDepartureTime() + "\n" +
//        "Travel time: " + queryTravelTimeEntry.getValue() + "\n");
//        }


    /* todo example usage
    public class Main {
    public static void main(String[] args) {
        Node[] nodes = {
            new Node(52.5200, 13.4050), // Berlin
            new Node(48.8566, 2.3522),  // Paris
            new Node(51.5074, -0.1278), // London
            new Node(50.1109, 8.6821)   // Frankfurt
        };

        KDTreeForNodes tree = new KDTreeForNodes();
        tree.build(nodes);

        // Find nodes within 500 km but outside 100 km of Brussels (50.8503, 4.3517)
        List<Node> nearbyNodes = tree.findNodesWithinDoughnut(4.3517, 50.8503, 100, 500);

        for (Node node : nearbyNodes) {
            System.out.println("Node within doughnut range: (" + node.getNodeLatitude() + ", " + node.getNodeLongitude() + ")");
        }
    }
}
     */


    public static void main(String[] args) {
        // GTFS data reader-writer instantiation to read, write, and store data
        GTFSDataReaderWriter gtfsDataReaderWriterForRaptor = new GTFSDataReaderWriter();
        String gtfsFolderPath = "";
        String raptorFolderPath = "";
        getRaptorMaps(gtfsFolderPath, raptorFolderPath, gtfsDataReaderWriterForRaptor);

        // Get all data for RAPTOR execution
        LinkedHashMap<Integer, Route> routes = gtfsDataReaderWriterForRaptor.getRoutes();
        LinkedHashMap<Integer, RouteStop> routeStops = gtfsDataReaderWriterForRaptor.getRouteStops();
        LinkedHashMap<Integer, Trip> trips = gtfsDataReaderWriterForRaptor.getTrips();
        LinkedHashMap<Integer, StopTime> stopTimes = gtfsDataReaderWriterForRaptor.getStopTimes();
        LinkedHashMap<Integer, Stop> stops = gtfsDataReaderWriterForRaptor.getStops();
        LinkedHashMap<Integer, StopRoute> stopRoutes = gtfsDataReaderWriterForRaptor.getStopRoutes();
        LinkedHashMap<Integer, Transfer> transfers = gtfsDataReaderWriterForRaptor.getTransfers();

        // OSM data reader-writer instantiation to read, write, and store data
        OSMDataReaderWriter osmDataReaderWriterForDijkstra = new OSMDataReaderWriter();
        String osmOplExtractFilePath = "";
        String dijkstraFolderPath = "";
        getDijkstraMaps(osmOplExtractFilePath, dijkstraFolderPath, osmDataReaderWriterForDijkstra);

        // Get all data for Dijkstra algorithm's execution
        LinkedHashMap<Long, Link> links = osmDataReaderWriterForDijkstra.getLinks();
        LinkedHashMap<Long, Node> nodes = osmDataReaderWriterForDijkstra.getNodes();

        // Build KD-Tree for snapping to Dijkstra-relevant network nodes
        Node[] nodesForKDTree = nodes.values().toArray(new Node[0]);
        KDTreeForNodes snappingKDTreeForNodes = new KDTreeForNodes();
        snappingKDTreeForNodes.buildNodeBasedKDTree(nodesForKDTree);

        // Load all multi-modal queries
        MultiModalQueryReader multiModalQueryReader = new MultiModalQueryReader();
        String multiModalQueriesFilePath = "";
        LinkedHashMap<Integer, MultiModalQuery> multiModalQueries = multiModalQueryReader.
                readMultiModalQueries(multiModalQueriesFilePath);

        // Iterate through all multi-modal queries
        for(MultiModalQuery multiModalQuery : multiModalQueries.values()) {
            double originLongitude = multiModalQuery.getOriginLongitude();
            double originLatitude = multiModalQuery.getOriginLatitude();
            int departureTime = multiModalQuery.getDepartureTime();
            double destinationLongitude = multiModalQuery.getDestinationLongitude();
            double destinationLatitude = multiModalQuery.getDestinationLatitude();

            Node nodeNearestToOrigin = snappingKDTreeForNodes.findNearestNode(originLongitude, originLatitude);
            Node nodeNearestToDestination = snappingKDTreeForNodes.findNearestNode(destinationLongitude, destinationLatitude);
            long originNodeId = nodeNearestToOrigin.getNodeId();
            long destinationNodeId = nodeNearestToDestination.getNodeId();

            // todo calculate snapping costs mate
            // Determine snapping cost, which is the cost of getting from one point to another (aerial distance) on foot
            /*public double calculateSnappingCost(double longitudeOne, double latitudeOne, double longitudeTwo,
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
            */

            // todo inner and outer radius arguments will be maximum walking, maximum driving, 0, and max walking distances

        }

    }

    // Get RAPTOR-relevant datasets ready
    public static void getRaptorMaps(String gtfsFolderPath,
                                     String raptorFolderPath,
                                     GTFSDataReaderWriter gtfsDataReaderWriterForRaptor) {
        // Ready filepath arguments to read
        String gtfsRoutesFilePath = gtfsFolderPath + "/routes.txt";
        String gtfsTripsFilePath = gtfsFolderPath + "/trips.txt";
        String gtfsStopTimesFilePath = gtfsFolderPath + "/stop_times.txt";
        String gtfsStopsFilePath = gtfsFolderPath + "/stops.txt";

        // Ready filepath arguments to write
        String raptorRoutesFilePath = raptorFolderPath + "/routes.txt";
        String raptorRouteStopsFilePath = raptorFolderPath + "/routeStops.txt";
        String tripsFilePath = raptorFolderPath + "/trips.txt";
        String raptorStopTimesFilePath = raptorFolderPath + "/stop_times.txt";
        String raptorStopsFilePath = raptorFolderPath + "/stops.txt";
        String raptorStopRoutesFilePath = raptorFolderPath + "/stopRoutes.txt";
        String raptorTransfersFilePath = raptorFolderPath + "/transfers.txt";

        // Read and manage data for main RAPTOR loop
        gtfsDataReaderWriterForRaptor.readAndFilterGTFSRoutes(gtfsRoutesFilePath);
        gtfsDataReaderWriterForRaptor.readAndFilterGTFSTrips(gtfsTripsFilePath);
        gtfsDataReaderWriterForRaptor.readAndFilterGTFSStopTimes(gtfsStopTimesFilePath);
        gtfsDataReaderWriterForRaptor.sortStopTimes();
        gtfsDataReaderWriterForRaptor.padGTFSRoutes();
        gtfsDataReaderWriterForRaptor.padGTFSRouteStops();

        // Read and manage data for ancillary RAPTOR loop
        gtfsDataReaderWriterForRaptor.readAndFilterGTFSStops(gtfsStopsFilePath);
        gtfsDataReaderWriterForRaptor.padStopRoutes();
        gtfsDataReaderWriterForRaptor.buildTransfersHashMap();
        gtfsDataReaderWriterForRaptor.filterTransfersHashMap();

        // Limit dataset to study area and ensure transitivity of transfers
        gtfsDataReaderWriterForRaptor.filterHashMapsOnLatLong();
        gtfsDataReaderWriterForRaptor.makeTransfersTransitive();

        // Write out data used for RAPTOR
        gtfsDataReaderWriterForRaptor.writeRaptorRoutes(raptorRoutesFilePath);
        gtfsDataReaderWriterForRaptor.writeRaptorRouteStops(raptorRouteStopsFilePath);
        gtfsDataReaderWriterForRaptor.writeTrips(tripsFilePath);
        gtfsDataReaderWriterForRaptor.writeRaptorStopTimes(raptorStopTimesFilePath);
        gtfsDataReaderWriterForRaptor.writeRaptorStops(raptorStopsFilePath);
        gtfsDataReaderWriterForRaptor.writeRaptorStopRoutes(raptorStopRoutesFilePath);
        gtfsDataReaderWriterForRaptor.writeRaptorTransfers(raptorTransfersFilePath);
    }

    // Get Dijkstra- and Contraction Hierarchies-relevant datasets ready
    public static void getDijkstraMaps(String osmOplExtractFilePath,
                                       String dijkstraFolderPath,
                                       OSMDataReaderWriter osmDataReaderWriterForDijkstra) {
        // Ready filepath arguments to write
        String dijkstraLinksFilePath = dijkstraFolderPath + "/dijkstraLinks.txt";
        String dijkstraNodesFilePath = dijkstraFolderPath + "/dijkstraNodes.txt";

        // Read and manage data for Dijkstra operations
        osmDataReaderWriterForDijkstra.readAndFilterOsmLinks(osmOplExtractFilePath);
        osmDataReaderWriterForDijkstra.removeCircularLinks();
        osmDataReaderWriterForDijkstra.readAndFilterOsmNodes(osmOplExtractFilePath);
        osmDataReaderWriterForDijkstra.associateLinksWithNode();
        osmDataReaderWriterForDijkstra.calculateLinkTravelTimesMin();
        osmDataReaderWriterForDijkstra.contractNodesAndBuildShortcuts();

        // Write out data used for the Dijkstra algorithm
        osmDataReaderWriterForDijkstra.writeDijkstraLinks(dijkstraLinksFilePath);
        osmDataReaderWriterForDijkstra.writeDijkstraNodes(dijkstraNodesFilePath);
    }

/*
    int desiredDepartureHour = desiredDepartureTime / 60;
    int desiredDepartureMinute = desiredDepartureTime % 60;

    double earliestArrivalTimeAtDestination = summaryEarliestArrivalTimeMap.get(destinationStopId);
    double earliestArrivalHourAtDestination = earliestArrivalTimeAtDestination / 60;
    double earliestArrivalMinuteAtDestination = earliestArrivalTimeAtDestination % 60;
    double travelTime = summaryEarliestArrivalTimeMap.get(destinationStopId) - desiredDepartureTime;
    double travelTimeHour = travelTime / 60;
    double travelTimeMinute = travelTime % 60;

    output = "Origin stop: " + stops.get(originStopId).getStopName() + "\n" +
            "Destination stop: " + stops.get(destinationStopId).getStopName() + "\n" +
            "Departure time from origin stop: " + desiredDepartureHour + ":" + desiredDepartureMinute + "\n" +
            "Earliest possible arrival time at destination: " + (int) earliestArrivalHourAtDestination + ":" +
            (int) earliestArrivalMinuteAtDestination + "\n" +
            "Travel time: " + travelTimeHour + ":" + travelTimeMinute + "\n";

            */

    //           shortestTravelTimes.put(query, summaryEarliestArrivalTimeMap.get(destinationStopId));

}
