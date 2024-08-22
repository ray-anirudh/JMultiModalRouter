package src.MultiModalRouter;
// TUM: Technical University of Munich
// RAPTOR: Round-based Public Transit Router (Delling et. al., 2015)
// OSM: OpenStreetMap
// OPL: Object-Per-Line (format)
// todo check travel times against what google maps says and against GTFS stop IDs

import java.awt.*;
import java.util.*;

import src.NearestNeighbourFinder.KDTreeForNodes;
import src.NearestNeighbourFinder.KDTreeForStops;
import src.PublicTransportRouter.GTFSDataManager.*;
import src.PublicTransportRouter.RoutingAlgorithm.RAPTOR;
import src.RoadTransportRouter.OSMDataManager.Link;
import src.RoadTransportRouter.OSMDataManager.Node;
import src.RoadTransportRouter.OSMDataManager.OSMDataReaderWriter;
import src.RoadTransportRouter.RoutingAlgorithm.DijkstraBasedRouter;

public class Caller {

    /**
     * ATTRIBUTE DEFINITIONS
     */

    private static final long NUMBER_MULTI_MODAL_QUERIES = 100;
    private static final long NANOSECONDS_PER_MIN = 60_000_000_000L;
    private static final double MINIMUM_DRIVING_DISTANCE_M = 2_000;
    // Refer to: https://www.emerald.com/insight/content/doi/10.1108/SASBE-07-2017-0031/full/html
    private static final double MAXIMUM_DRIVING_DISTANCE_M = 9_000;
    private static final double AVERAGE_WALKING_SPEED_M_PER_MIN = 85.2;     // Translates to 1.4 m/s
    private static final double AVERAGE_DRIVING_SPEED_M_PER_MIN = 483.33;
    // (Source: https://www.tomtom.com/traffic-index/munich-traffic/); translates to approximately 29 km/h
    private static final double AVERAGE_ODM_WAIT_TIME_MIN = 6;
    // (Source: https://link.springer.com/article/10.1007/s13177-023-00345-5/tables/6)
    private static final int STOP_TYPE_TO_IGNORE = ;    // Aimed at the "stop hierarchy" (SH) heuristic
    private static final int CUTOFF_TRIP_VOLUME_SERVED_BY_STOP = ;  // Aimed at the "trip volume" (TV) heuristic

    /**
     * BEHAVIOUR DEFINITIONS
     */

    public static void main(String[] args) {
        /**
         * OSM data reader-writer instantiation to read, write, and store data
         */
        long osmStartTime = System.nanoTime();
        OSMDataReaderWriter osmDataReaderWriterForDijkstra = new OSMDataReaderWriter();
        String osmOplExtractFilePath = "D:/Documents - Education + Work/Education - TUM/Year 2/Fourth Semester" +
                "/MasterThesis/Data/OSMDataMunich/Downloaded/planet_10.835,47.824_12.172,48.438.osm.opl/" +
                "BBBikeOSMExtract.opl";
        String dijkstraFolderPath = "D:/Documents - Education + Work/Education - TUM/Year 2/Fourth Semester/" +
                "MasterThesis/Results/DijkstraMaps";
        getDijkstraMaps(osmOplExtractFilePath, dijkstraFolderPath, osmDataReaderWriterForDijkstra);

        // Get all data for Dijkstra algorithm's execution
        LinkedHashMap<Long, Link> links = osmDataReaderWriterForDijkstra.getLinks();
        LinkedHashMap<Long, Node> nodes = osmDataReaderWriterForDijkstra.getNodes();
        Node[] nodesForNNSearches = nodes.values().toArray(new Node[0]);
        long osmEndTime = System.nanoTime();
        double osmDataProcessingDuration = (double) (osmEndTime - osmStartTime);

        // Set up the KD-Tree for nearest node searches
        long kDNodeStartTime = System.nanoTime();
        KDTreeForNodes kDTreeForNodes = new KDTreeForNodes();
        kDTreeForNodes.buildNodeBasedKDTree(nodesForNNSearches);
        long kDNodeEndTime = System.nanoTime();

        System.out.println("\n" +
                "Characteristics of parsed OSM data:" + "\n" +
                "Number of nodes: " + nodes.size() + "\n" +
                "Number of links: " + links.size() + "\n" +
                "OSM-OPL data processed in " + String.format("%.3f", osmDataProcessingDuration / NANOSECONDS_PER_MIN) +
                " minutes.");

        /**
         * GTFS data reader-writer instantiation to read, write, and store data
         */
        long gtfsStartTime = System.nanoTime();
        GTFSDataReaderWriter gtfsDataReaderWriterForRAPTOR = new GTFSDataReaderWriter();
        String gtfsFolderPath = "D:/Documents - Education + Work/Education - TUM/Year 2/Fourth Semester/MasterThesis/" +
                "Data/GTFSDataMunich/Downloaded/AGGTFSData";
        String rAPTORFolderPath = "D:/Documents - Education + Work/Education - TUM/Year 2/Fourth Semester/" +
                "MasterThesis/Results/RAPTORMaps";
        getRAPTORMaps(gtfsFolderPath, rAPTORFolderPath, gtfsDataReaderWriterForRAPTOR);

        // Get all data for RAPTOR execution
        LinkedHashMap<Integer, Route> routes = gtfsDataReaderWriterForRAPTOR.getRoutes();
        LinkedHashMap<Integer, Trip> trips = gtfsDataReaderWriterForRAPTOR.getTrips();
        LinkedHashMap<Integer, RouteStop> routeStops = gtfsDataReaderWriterForRAPTOR.getRouteStops();
        LinkedHashMap<Integer, StopTime> stopTimes = gtfsDataReaderWriterForRAPTOR.getStopTimes();
        LinkedHashMap<Integer, Stop> stops = gtfsDataReaderWriterForRAPTOR.getStops();
        Stop[] stopsForNNSearches = stops.values().toArray(new Stop[0]);
        LinkedHashMap<Integer, StopRoute> stopRoutes = gtfsDataReaderWriterForRAPTOR.getStopRoutes();
        LinkedHashMap<Integer, Transfer> transfers = gtfsDataReaderWriterForRAPTOR.getTransfers();
        long gtfsEndTime = System.nanoTime();
        double gtfsDataProcessingDuration = (double) (gtfsEndTime - gtfsStartTime);

        // Set up the KD-Tree for nearest stop searches
        long kDStopStartTime = System.nanoTime();
        KDTreeForStops kDTreeForStops = new KDTreeForStops();
        kDTreeForStops.buildStopBasedKDTree(stopsForNNSearches);
        long kDStopEndTime = System.nanoTime();

        System.out.println("\n" +
                "Characteristics of parsed GTFS data:" + "\n" +
                "Number of routes: " + routes.size() + "\n" +
                "Number of trips: " + trips.size() + "\n" +
                "Number of routeStop objects: " + routeStops.size() + "\n" +
                "Number of stopTime objects: " + stopTimes.size() + "\n" +
                "Number of stops: " + stops.size() + "\n" +
                "Number of stopRoute objects: " + stopRoutes.size() + "\n" +
                "Number of transfers: " + transfers.size() + "\n" +
                "GTFS data processed in " + String.format("%.3f", gtfsDataProcessingDuration / NANOSECONDS_PER_MIN)
                + " minutes.");

        double kDTreesBuildDuration = (double) ((kDStopEndTime - kDStopStartTime) + (kDNodeEndTime - kDNodeStartTime));
        System.out.println("\n" +
                "KD-Trees for searching nearest nodes and stops built in " + String.format("%.3f",
                kDTreesBuildDuration / NANOSECONDS_PER_MIN) + " minutes.");

        /**
         * Instantiate routers and generate/ read multi-modal queries
         */
        // Load all multi-modal queries, and instantiate the responses map
        long queryGenStartTime = System.nanoTime();
        // Consideration of trips simulated by TUM's Travel Behaviour professorship for Munich and its environs
        String multiModalQueriesFilePath = "D:/Documents - Education + Work/Education - TUM/Year 2/Fourth Semester/" +
                "MasterThesis/Data/MITOTripDataMunich/multiModalQueries.csv";
        MultiModalQueryReader multiModalQueryReader = new MultiModalQueryReader();
        multiModalQueryReader.readMultiModalQueries(multiModalQueriesFilePath);
        LinkedHashMap<Long, MultiModalQuery> multiModalQueries = multiModalQueryReader.getMultiModalQueries();

//        // Alternate pathway (bi-variate normal distribution-based) for generating random multi-modal queries
//        String multiModalQueriesFilePath = "D:/Documents - Education + Work/Education - TUM/Year 2/Fourth Semester/" +
//                "MasterThesis/Results/MultiModalQueriesMap/multiModalQueries.txt";
//        MultiModalQueryGenerator multiModalQueryGenerator = new MultiModalQueryGenerator();
//        LinkedHashMap<Long, MultiModalQuery> multiModalQueries = multiModalQueryGenerator.
//                generateQueries(NUMBER_MULTI_MODAL_QUERIES);   // Method argument contains number of queries to generate
//        multiModalQueryGenerator.writeMultiModalQueries(multiModalQueriesFilePath);
        long queryGenEndTime = System.nanoTime();

        double queryGenerationDuration = (double) (queryGenEndTime - queryGenStartTime);
        LinkedHashMap<Long, MultiModalQueryResponses> multiModalQueriesResponses = new LinkedHashMap<>();
        System.out.println("\n" +
                multiModalQueries.size() + " multi-modal queries for JavaMultiModalRouter read in " + String.format
                ("%.3f", queryGenerationDuration / NANOSECONDS_PER_MIN) + " minutes.");

        // Set up router instances
        DijkstraBasedRouter dijkstraBasedRouter = new DijkstraBasedRouter();
        RAPTOR rAPTOR = new RAPTOR();

        /**
         * Execute few queries on the JMultiModalRouter architecture
         */
        long queriesSolvingStartTime = System.nanoTime();
        for (HashMap.Entry<Long, MultiModalQuery> multiModalQueryEntry : multiModalQueries.entrySet()) {
            // Get the multi-modal query and response instances
            MultiModalQuery multiModalQuery = multiModalQueryEntry.getValue();
            MultiModalQueryResponses multiModalQueryResponses = new MultiModalQueryResponses();

            // Parse locational and temporal information from the query
            double originPointLongitude = multiModalQuery.getOriginLongitude();
            double originPointLatitude = multiModalQuery.getOriginLatitude();
            double destinationPointLongitude = multiModalQuery.getDestinationLongitude();
            double destinationPointLatitude = multiModalQuery.getDestinationLatitude();
            int originPointDepartureTime = multiModalQuery.getDepartureTime();
            multiModalQueryResponses.setOriginPointLongitude(originPointLongitude);
            multiModalQueryResponses.setOriginPointLatitude(originPointLatitude);
            multiModalQueryResponses.setDestinationPointLongitude(destinationPointLongitude);
            multiModalQueryResponses.setDestinationPointLatitude(destinationPointLatitude);
            multiModalQueryResponses.setDepartureTimeOriginPoint(originPointDepartureTime);

            // Determine nodes nearest to the origin and destination points
            Node originNode = kDTreeForNodes.findNearestNode(originPointLongitude, originPointLatitude);
            Node destinationNode = kDTreeForNodes.findNearestNode(destinationPointLongitude,
                    destinationPointLatitude);
            long originNodeId = originNode.getNodeId();
            long destinationNodeId = destinationNode.getNodeId();
            multiModalQueryResponses.setNearestOriginNodeId(originNodeId);
            multiModalQueryResponses.setNearestDestinationNodeId(destinationNodeId);

            // Determine stop (and allied parameters) nearest to destination node
            Stop destinationStop = kDTreeForStops.findNearestStop(destinationNode.getNodeLongitude(),
                    destinationNode.getNodeLatitude());
            Node destinationStopNode = kDTreeForNodes.findNearestNode(destinationStop.getStopLongitude(),
                    destinationStop.getStopLatitude());
            int destinationStopId = destinationStop.getStopId();
            String destinationStopName = destinationStop.getStopName();
            multiModalQueryResponses.setDestinationStopId(destinationStopId);
            multiModalQueryResponses.setDestinationStopName(destinationStopName);

            // Determine travel time from destination stop to destination
            double travelTimeDestinationStopToDestinationPoint = ((destinationStop.equiRectangularDistanceTo(
                    destinationStopNode.getNodeLongitude(), destinationStopNode.getNodeLatitude()) + destinationNode.
                    equiRectangularDistanceTo(destinationPointLongitude, destinationPointLatitude)) +
                    (dijkstraBasedRouter.findShortestDrivingPathCostMin(originNodeId, destinationNodeId, nodes, links) *
                    AVERAGE_DRIVING_SPEED_M_PER_MIN)) / AVERAGE_WALKING_SPEED_M_PER_MIN;

            /**
             * Building three sets of origin stops to test different heuristics
             */
            // todo setup timers for all solution types
            // For origin node, get all the stops in a doughnut catchment
            ArrayList<Stop> stopsNearOriginNode = kDTreeForStops.findStopsWithinDoughnut(originNode.getNodeLongitude(),
                    originNode.getNodeLatitude(), MINIMUM_DRIVING_DISTANCE_M, MAXIMUM_DRIVING_DISTANCE_M);

            // Filtering in unique stops near the origin node
            HashSet<String> uniqueOriginStops = new HashSet<>();
            Iterator<Stop> originStopIterator = stopsNearOriginNode.iterator();
            while (originStopIterator.hasNext()) {
                Stop originStop = originStopIterator.next();
                String stopKey = originStop.getStopName() + "-" + originStop.getStopType();
                if (uniqueOriginStops.contains(stopKey)) {
                    originStopIterator.remove();
                } else {
                    uniqueOriginStops.add(stopKey);
                }
            }

            ArrayList<Stop> stopsNearOriginNodeSHHeuristic = new ArrayList<>();
            ArrayList<Stop> stopsNearOriginNodeTVHeuristic = new ArrayList<>();

            if ((stopsNearOriginNode == null) || (stopsNearOriginNode.isEmpty())) {
                System.out.println("Skipping");
                continue;
            } else {
                // For origin node, get all the stops that satisfy the "stop hierarchy" heuristic criterion
                for (Stop stopNearOriginNode : stopsNearOriginNode) {
                    if (stopNearOriginNode.getStopType() != STOP_TYPE_TO_IGNORE) {
                        stopsNearOriginNodeSHHeuristic.add(stopNearOriginNode);
                    }
                }

                // For origin node, get all the stops that satisfy the "trip volume" heuristic criterion
                for (Stop stopNearOriginNode : stopsNearOriginNode) {
                    if (stopNearOriginNode.getStopTripCount() > CUTOFF_TRIP_VOLUME_SERVED_BY_STOP) {
                        stopsNearOriginNodeTVHeuristic.add(stopNearOriginNode);
                    }
                }
            }

            if ((stopsNearOriginNodeSHHeuristic != null) && (!stopsNearOriginNodeSHHeuristic.isEmpty()) &&
                    (stopsNearOriginNodeTVHeuristic != null) && (!stopsNearOriginNodeTVHeuristic.isEmpty())) {
                String solutionTypeFlag = "Exact";
                // todo if we get double max value then there is no point in writing such travel times to file
                solutionTypeFlag = "StopHierarchy";

                solutionTypeFlag = "TripVolume";
                multiModalQueriesResponses.put(multiModalQueryEntry.getKey(), multiModalQueryResponses);
            } else {
                System.out.println("Skipping due to poor heuristic sets.");
            }













            double leastTotalTravelTime = runRAPTORAndDijkstra(solutionTypeFlag, originPointLongitude, originPointLatitude,
                    destinationPointLongitude, destinationPointLatitude, originPointDepartureTime, rAPTOR,
                    dijkstraBasedRouter, nodes, links, nearestOriginNode, nearestDestinationNode, kDTreeForNodes,
                    stopsNearOriginNode, stopNearestToDestinationNode, routeStops, stopTimes, stops, stopRoutes,
                    transfers);
            Toolkit.getDefaultToolkit().beep();
            System.out.println("Origin point coordinates: " + originPointLatitude + ", " + originPointLongitude);
            System.out.println("Destination point coordinates: " + destinationPointLatitude + ", " + destinationPointLongitude);
            System.out.println("Departure time: " + originPointDepartureTime);
            System.out.println("Travel time in minutes: " + leastTotalTravelTime + "\n\n\n");
            System.out.println("Least total travel time is: " + leastTotalTravelTime);
        }
        long queriesSolvingEndTime = System.nanoTime();
        double queriesSolvingDuration = (double) (queriesSolvingEndTime - queriesSolvingStartTime);
        System.out.println("\n" + multiModalQueries.size() + " multi-modal queries solved in " + String.format("%.3f",
                queriesSolvingDuration / NANOSECONDS_PER_MIN) + " minutes.");
        System.exit(1);
    }


    // Run RAPTOR and Dijkstra algorithms and update the multi-modal query response object todo add multimodalqueryresponses here
    private static void runRAPTORAndDijkstra(ArrayList<Stop> stopsNearOriginNode, KDTreeForNodes kDTreeForNodes,
                                             String solutionTypeFlag, double originPointLongitude,
                                             double originPointLatitude, Node originNode, int originPointDepartureTime,
                                             double travelTimeDestinationStopToDestinationPoint, int destinationStopId,
                                             RAPTOR rAPTOR, DijkstraBasedRouter dijkstraBasedRouter,
                                             LinkedHashMap<Long, Node> nodes, LinkedHashMap<Long, Link> links,
                                             LinkedHashMap<Integer, RouteStop> routeStops,
                                             LinkedHashMap<Integer, StopTime> stopTimes,
                                             LinkedHashMap<Integer, Stop> stops,
                                             LinkedHashMap<Integer, StopRoute> stopRoutes,
                                             LinkedHashMap<Integer, Transfer> transfers,
                                             MultiModalQueryResponses multiModalQueryResponses) {
        long singleMultiModalQueryEvaluationStartTime = System.nanoTime();
        double leastTotalTravelTime = Double.MAX_VALUE;

        // Derive travel times from origin point to stops in its vicinity
        ArrayList<Double> travelTimesOriginPointToOriginStops = new ArrayList<>();
        for (Stop stopNearOriginNode : stopsNearOriginNode) {
            Node nodeNearOriginStop = kDTreeForNodes.findNearestNode(stopNearOriginNode.getStopLongitude(),
                    stopNearOriginNode.getStopLatitude());

            travelTimesOriginPointToOriginStops.add(AVERAGE_ODM_WAIT_TIME_MIN + // Waiting for ODM service is expected
                    // Walking to and from ODM pick-up and drop-off is also expected
                    ((originNode.equiRectangularDistanceTo(originPointLongitude, originPointLatitude) +
                    nodeNearOriginStop.equiRectangularDistanceTo(stopNearOriginNode.getStopLongitude(),
                            stopNearOriginNode.getStopLatitude())) / AVERAGE_WALKING_SPEED_M_PER_MIN) +
                    // Driving between network nodes via an ODM vehicle is also expected
                    (dijkstraBasedRouter.findShortestDrivingPathCostMin(originNode.getNodeId(),
                            nodeNearOriginStop.getNodeId(), nodes, links)));
        }

        // Calculate travel times between origin-destination stops, and report the minimized total travel time
        for (int i = 0; i < stopsNearOriginNode.size(); i++) {
            double travelTimeOriginStopToDestinationStop = rAPTOR.findShortestTransitPath(stopsNearOriginNode.get(i).
                            getStopId(), destinationStopId, (originPointDepartureTime +
                    travelTimesOriginPointToOriginStops.get(i)), routeStops, stopTimes, stops, stopRoutes, transfers).
                    getTravelTimeMinutes();
            // todo handle better
            double totalTravelTime = (travelTimeOriginStopToDestinationStop == -1) ? Double.MAX_VALUE :
                    travelTimesOriginPointToOriginStops.get(i) + travelTimeOriginStopToDestinationStop +
                            travelTimeDestinationStopToDestinationPoint;

            if (totalTravelTime < leastTotalTravelTime) {
                leastTotalTravelTime = totalTravelTime;
            }
            // todo if we get double max value then there is no point in writing such travel times to file

//            System.out.println("Least total trav time: " + leastTotalTravelTime);
        }

        if (solutionType.equalsIgnoreCase("Exact")) {   // todo build

        } else if (solutionType.equalsIgnoreCase("SH")) {

        } else if (solutionType.equalsIgnoreCase("TV")) {

        }

        return leastTotalTravelTime;
    }

    // Get RAPTOR-relevant datasets ready
    private static void getRAPTORMaps(String gtfsFolderPath,
                                      String rAPTORFolderPath,
                                      GTFSDataReaderWriter gtfsDataReaderWriterForRAPTOR) {
        // Ready filepath arguments to read
        String gtfsRoutesFilePath = gtfsFolderPath + "/routes.txt";
        String gtfsTripsFilePath = gtfsFolderPath + "/trips.txt";
        String gtfsStopTimesFilePath = gtfsFolderPath + "/stop_times.txt";
        String gtfsStopsFilePath = gtfsFolderPath + "/stops.txt";

        // Ready filepath arguments to write
        String rAPTORRoutesFilePath = rAPTORFolderPath + "/routes.txt";
        String rAPTORRouteStopsFilePath = rAPTORFolderPath + "/routeStops.txt";
        String tripsFilePath = rAPTORFolderPath + "/trips.txt";
        String rAPTORStopTimesFilePath = rAPTORFolderPath + "/stop_times.txt";
        String rAPTORStopsFilePath = rAPTORFolderPath + "/stops.txt";
        String rAPTORStopRoutesFilePath = rAPTORFolderPath + "/stopRoutes.txt";
        String rAPTORTransfersFilePath = rAPTORFolderPath + "/transfers.txt";

        // Read and manage data for main RAPTOR loop
        gtfsDataReaderWriterForRAPTOR.readAndFilterGTFSRoutes(gtfsRoutesFilePath);
        gtfsDataReaderWriterForRAPTOR.readAndFilterGTFSTrips(gtfsTripsFilePath);
        gtfsDataReaderWriterForRAPTOR.readAndFilterGTFSStopTimes(gtfsStopTimesFilePath);
        gtfsDataReaderWriterForRAPTOR.sortStopTimes();
        gtfsDataReaderWriterForRAPTOR.padGTFSRoutes();
        gtfsDataReaderWriterForRAPTOR.padGTFSRouteStops();

        // Read and manage data for ancillary RAPTOR loop
        gtfsDataReaderWriterForRAPTOR.readAndFilterGTFSStops(gtfsStopsFilePath);
        gtfsDataReaderWriterForRAPTOR.padStopRoutes();
        gtfsDataReaderWriterForRAPTOR.buildTransfersHashMap();
        // todo fix back gtfsDataReaderWriterForRAPTOR.filterTransfersHashMap();

        // Limit dataset to study area and ensure transitivity of transfers
        // todo fix back gtfsDataReaderWriterForRAPTOR.makeTransfersTransitive();
        gtfsDataReaderWriterForRAPTOR.filterHashMapsOnLatLong();

        // Write out data used for RAPTOR
        gtfsDataReaderWriterForRAPTOR.writeRaptorRoutes(rAPTORRoutesFilePath);
        gtfsDataReaderWriterForRAPTOR.writeRaptorRouteStops(rAPTORRouteStopsFilePath);
        gtfsDataReaderWriterForRAPTOR.writeTrips(tripsFilePath);
        gtfsDataReaderWriterForRAPTOR.writeRaptorStopTimes(rAPTORStopTimesFilePath);
        gtfsDataReaderWriterForRAPTOR.writeRaptorStops(rAPTORStopsFilePath);
        gtfsDataReaderWriterForRAPTOR.writeRaptorStopRoutes(rAPTORStopRoutesFilePath);
        gtfsDataReaderWriterForRAPTOR.writeRaptorTransfers(rAPTORTransfersFilePath);
    }

    // Get Dijkstra-relevant datasets ready
    private static void getDijkstraMaps(String osmOplExtractFilePath,
                                        String dijkstraFolderPath,
                                        OSMDataReaderWriter osmDataReaderWriterForDijkstra) {
        // Ready filepath arguments to write
        String dijkstraLinksFilePath = dijkstraFolderPath + "/dijkstraLinks.txt";
        String dijkstraNodesFilePath = dijkstraFolderPath + "/dijkstraNodes.txt";

        // Read and manage data for Dijkstra operations
        osmDataReaderWriterForDijkstra.readAndFilterOsmLinks(osmOplExtractFilePath);
        osmDataReaderWriterForDijkstra.readAndFilterOsmNodes(osmOplExtractFilePath);
        osmDataReaderWriterForDijkstra.associateLinksWithNode();
        osmDataReaderWriterForDijkstra.calculateLinkTravelTimesMin();
        // osmDataReaderWriterForDijkstra.contractNodesAndBuildShortcuts();    // Assess step post-thesis

        // Write out data used for the Dijkstra algorithm
        osmDataReaderWriterForDijkstra.writeDijkstraLinks(dijkstraLinksFilePath);
        osmDataReaderWriterForDijkstra.writeDijkstraNodes(dijkstraNodesFilePath);
    }
}