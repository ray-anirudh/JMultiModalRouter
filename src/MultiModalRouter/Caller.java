/**
 * Author: Anirudh Ray
 * Institution: Professorship of Traffic Engineering and Control, Technical University of Munich
 * Department: Mobility Systems Engineering, School of Engineering and Design
 * E-mail Address: Anirudh.Ray@tum.de
 * Purpose: Component of a Java-based multi-modal routing algorithm, built using RAPTOR, Dijkstra-algorithm, and
 * KD-Trees
 */

package src.MultiModalRouter;
// TUM: Technical University of Munich
// RAPTOR: Round-based Public Transit Router (Delling et. al., 2015)
// OSM: OpenStreetMap
// OPL: Object-Per-Line (format)

import src.NearestNeighbourFinder.KDTreeForNodes;
import src.NearestNeighbourFinder.KDTreeForStops;

import src.PublicTransportRouter.GTFSDataManager.*;
import src.PublicTransportRouter.RoutingAlgorithm.RAPTOR;

import src.RoadTransportRouter.OSMDataManager.Link;
import src.RoadTransportRouter.OSMDataManager.Node;
import src.RoadTransportRouter.OSMDataManager.OSMDataReaderWriter;
import src.RoadTransportRouter.RoutingAlgorithm.DijkstraBasedRouter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Caller {

    /**
     * ATTRIBUTE DEFINITIONS
     */

    private static final long BEGIN_QUERY_ID = 209_000L;
    private static final long NUMBER_MULTI_MODAL_QUERIES = 90L;
    private static final long NANOSECONDS_PER_MIN = 60_000_000_000L;
    private static final long NANOSECONDS_PER_SECOND = 1_000_000_000L;
    private static final double MINIMUM_DRIVING_DISTANCE_M = 2_000;
    // Refer to: https://www.emerald.com/insight/content/doi/10.1108/SASBE-07-2017-0031/full/html
    private static final double MAXIMUM_DRIVING_DISTANCE_M = 9_000;
    private static final double AVERAGE_WALKING_SPEED_M_PER_MIN = 85.20;     // Translates to 1.4 m/s
    private static final double AVERAGE_DRIVING_SPEED_M_PER_MIN = 483.33;
    // (Source: https://www.tomtom.com/traffic-index/munich-traffic/); translates to approximately 29 km/h
    private static final double AVERAGE_ODM_WAIT_TIME_MIN = 6;
    // (Source: https://link.springer.com/article/10.1007/s13177-023-00345-5/tables/6)
    private static final int STOP_TYPE_TO_IGNORE = 3;    // Aimed at the "stop hierarchy" (SH) heuristic
    private static final int CUTOFF_TRIP_VOLUME_SERVED_BY_STOP = 450;  // Aimed at the "trip volume" (TV) heuristic

    /**
     * BEHAVIOUR DEFINITIONS
     */

    public static void main(String[] args) {
        /**
         * OSM data reader-writer instantiation to read, write, and store data
         */
        long osmStartTime = System.nanoTime();
        OSMDataReaderWriter osmDataReaderWriterForDijkstra = new OSMDataReaderWriter();
        String osmOplExtractFilePath = "D:/Documents - Education + Work/Education - TUM/Year 2/Fourth Semester/" +
                "MasterThesis/Data/OSMDataMunich/Downloaded/planet_10.835_47.824_12.172_48.438.osm.opl/" +
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
        String gtfsFolderPath = "D:/Documents - Education + Work/Education - TUM/Year 2/Fourth Semester/" +
                "MasterThesis/Data/GTFSDataMunich/Downloaded/AGGTFSData";
        String rAPTORFolderPath = "D:/Documents - Education + Work/Education - TUM/Year 2/Fourth Semester/" +
                "MasterThesis/Results/RAPTORMaps";
        String parametersFileFilePath = "D:/Documents - Education + Work/Education - TUM/Year 2/Fourth Semester/" +
                "MasterThesis/JMultiModalRouter/JMMRParameters/GTFSParameters.txt";
        getRAPTORMaps(gtfsFolderPath, parametersFileFilePath, rAPTORFolderPath, gtfsDataReaderWriterForRAPTOR);

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
        LinkedHashMap<Long, MultiModalQuery> allMultiModalQueries = multiModalQueryReader.getMultiModalQueries();
        LinkedHashMap<Long, MultiModalQuery> multiModalQueries = new LinkedHashMap<>();

        // Limit the number of multi-modal queries to be processed, slicing through the master-list of queries
        for (long multiModalQueryCount = BEGIN_QUERY_ID; multiModalQueryCount <= BEGIN_QUERY_ID +
                NUMBER_MULTI_MODAL_QUERIES; multiModalQueryCount++) {
            multiModalQueries.put(multiModalQueryCount, allMultiModalQueries.get(multiModalQueryCount));
        }

//        // Alternate pathway (bi-variate normal distribution-based) for generating random multi-modal queries
//        String multiModalQueriesFilePath = "D:/Documents - Education + Work/Education - TUM/Year 2/Fourth Semester/" +
//                "MasterThesis/Results/MultiModalQueriesMap/multiModalQueries.txt";
//        MultiModalQueryGenerator multiModalQueryGenerator = new MultiModalQueryGenerator();
//        LinkedHashMap<Long, MultiModalQuery> multiModalQueries = multiModalQueryGenerator.
//                generateQueries(NUMBER_MULTI_MODAL_QUERIES);
//                // Method argument contains number of queries to generate
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

        // Set up an ExecutorService instance for parallel processing, and a list to hold Future objects
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<Map.Entry<Long, MultiModalQueryResponses>>> futures = new ArrayList<>();

        /**
         * Execute few queries on the JMultiModalRouter architecture
         */
        long queriesSolvingStartTime = System.nanoTime();
        for (HashMap.Entry<Long, MultiModalQuery> multiModalQueryEntry : multiModalQueries.entrySet()) {
            Future<Map.Entry<Long, MultiModalQueryResponses>> future = executor.submit(() -> {

                // Get the multi-modal query and response instances
                Long queryId = multiModalQueryEntry.getKey();
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
                double travelTimeDestinationStopToDestinationPoint = (destinationStop.equiRectangularDistanceTo(
                        destinationStopNode.getNodeLongitude(), destinationStopNode.getNodeLatitude()) +
                        destinationNode.equiRectangularDistanceTo(destinationPointLongitude, destinationPointLatitude) +
                        (dijkstraBasedRouter.findShortestDrivingPathCostMin(destinationStopNode.getNodeId(),
                                destinationNodeId, nodes, links) * AVERAGE_DRIVING_SPEED_M_PER_MIN)) /
                        AVERAGE_WALKING_SPEED_M_PER_MIN;
                multiModalQueryResponses.setTravelTimeDestinationStopToDestination(
                        travelTimeDestinationStopToDestinationPoint);

                /**
                 * Building three sets of origin stops to test different heuristics
                 */
                // For origin node, get all the stops in a doughnut catchment; initialize heuristic-based stop lists
                ArrayList<Stop> stopsNearOriginNode = kDTreeForStops.findStopsWithinDoughnut(originNode.
                                getNodeLongitude(), originNode.getNodeLatitude(), MINIMUM_DRIVING_DISTANCE_M,
                        MAXIMUM_DRIVING_DISTANCE_M);
                ArrayList<Stop> stopsNearOriginNodeSHHeuristic = new ArrayList<>();
                ArrayList<Stop> stopsNearOriginNodeTVHeuristic = new ArrayList<>();

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

                if (!stopsNearOriginNode.isEmpty()) {
                    // For origin node, get all the stops that satisfy the "stop hierarchy" heuristic criterion
                    for (Stop stopNearOriginNode : stopsNearOriginNode) {
                        if (stopNearOriginNode.getStopType() != STOP_TYPE_TO_IGNORE) {
                            stopsNearOriginNodeSHHeuristic.add(stopNearOriginNode);
                        }
                    }

                    // For origin node, get all the stops that satisfy the "trip volume" heuristic criterion
                    for (Stop stopNearOriginNode : stopsNearOriginNode) {
                        if (stopNearOriginNode.getStopTripCount() >= CUTOFF_TRIP_VOLUME_SERVED_BY_STOP) {
                            stopsNearOriginNodeTVHeuristic.add(stopNearOriginNode);
                        }
                    }
                }

                if ((!stopsNearOriginNodeSHHeuristic.isEmpty()) && (!stopsNearOriginNodeTVHeuristic.isEmpty())) {
                    String solutionTypeFlag = "Exact";
                    runRAPTORAndDijkstra(stopsNearOriginNode, kDTreeForNodes, solutionTypeFlag, originPointLongitude,
                            originPointLatitude, originNode, originPointDepartureTime,
                            travelTimeDestinationStopToDestinationPoint, destinationStopId, rAPTOR, dijkstraBasedRouter,
                            nodes, links, routeStops, stopTimes, stops, stopRoutes, transfers,
                            multiModalQueryResponses);

                    solutionTypeFlag = "StopHierarchy";
                    runRAPTORAndDijkstra(stopsNearOriginNodeSHHeuristic, kDTreeForNodes, solutionTypeFlag,
                            originPointLongitude, originPointLatitude, originNode, originPointDepartureTime,
                            travelTimeDestinationStopToDestinationPoint, destinationStopId, rAPTOR, dijkstraBasedRouter,
                            nodes, links, routeStops, stopTimes, stops, stopRoutes, transfers,
                            multiModalQueryResponses);

                    solutionTypeFlag = "TripVolume";
                    runRAPTORAndDijkstra(stopsNearOriginNodeTVHeuristic, kDTreeForNodes, solutionTypeFlag,
                            originPointLongitude, originPointLatitude, originNode, originPointDepartureTime,
                            travelTimeDestinationStopToDestinationPoint, destinationStopId, rAPTOR, dijkstraBasedRouter,
                            nodes, links, routeStops, stopTimes, stops, stopRoutes, transfers,
                            multiModalQueryResponses);

                    if ((multiModalQueryResponses.getTotalTravelTimeExactSolution() != Double.MAX_VALUE) &&
                            (multiModalQueryResponses.getTotalTravelTimeSHSolution() != Double.MAX_VALUE) &&
                            (multiModalQueryResponses.getTotalTravelTimeTVSolution() != Double.MAX_VALUE)) {
                        return new AbstractMap.SimpleEntry<>(queryId, multiModalQueryResponses);
                        // multiModalQueriesResponses.put(multiModalQueryEntry.getKey(), multiModalQueryResponses);
                    }
                }
                return null;
            });
            futures.add(future);
        }

        // Process the results and add to the response map before shutting down the executor
        for (Future<Map.Entry<Long, MultiModalQueryResponses>> future : futures) {
            try {
                Map.Entry<Long, MultiModalQueryResponses> result = future.get();
                if (result != null) {
                    multiModalQueriesResponses.put(result.getKey(), result.getValue());
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();

        long queriesSolvingEndTime = System.nanoTime();
        double queriesSolvingDuration = (double) (queriesSolvingEndTime - queriesSolvingStartTime);
        System.out.println("\n" + multiModalQueries.size() + " multi-modal queries solved in " + String.format("%.3f",
                queriesSolvingDuration / NANOSECONDS_PER_MIN) + " minutes.");

        // Write out responses to the multi-modal queries
        String multiModalQueriesResponsesFilePath = "D:/Documents - Education + Work/Education - TUM/Year 2/" +
                "Fourth Semester/MasterThesis/Results/LearningData/multiModalQueriesResponses.csv";
        writeMultiModalQueriesResponses(multiModalQueriesResponsesFilePath, multiModalQueriesResponses);
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

    // Get RAPTOR-relevant datasets ready
    private static void getRAPTORMaps(String gtfsFolderPath,
                                      String parametersFileFilePath,
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
        gtfsDataReaderWriterForRAPTOR.readAndFilterGTFSRoutes(gtfsRoutesFilePath, parametersFileFilePath);
        gtfsDataReaderWriterForRAPTOR.readAndFilterGTFSTrips(gtfsTripsFilePath);
        gtfsDataReaderWriterForRAPTOR.readAndFilterGTFSStopTimes(gtfsStopTimesFilePath);
        gtfsDataReaderWriterForRAPTOR.sortStopTimes();
        gtfsDataReaderWriterForRAPTOR.padGTFSRoutes();
        gtfsDataReaderWriterForRAPTOR.padGTFSRouteStops();

        // Read and manage data for ancillary RAPTOR loop
        gtfsDataReaderWriterForRAPTOR.readAndFilterGTFSStops(gtfsStopsFilePath);
        gtfsDataReaderWriterForRAPTOR.padStopRoutes();
        gtfsDataReaderWriterForRAPTOR.buildTransfersHashMap();
        gtfsDataReaderWriterForRAPTOR.filterTransfersHashMap();

        // Limit dataset to study area and ensure transitivity of transfers
        gtfsDataReaderWriterForRAPTOR.makeTransfersTransitive();
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

    // Run RAPTOR and Dijkstra algorithms and update the multi-modal query response object
    private static void runRAPTORAndDijkstra(ArrayList<Stop> stopsNearOriginNode,
                                             KDTreeForNodes kDTreeForNodes, String solutionTypeFlag,
                                             double originPointLongitude, double originPointLatitude, Node originNode,
                                             int originPointDepartureTime,
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
        int solutionStopIndex = -1;

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
                            travelTimesOriginPointToOriginStops.get(i)), routeStops, stopTimes, stops, stopRoutes,
                            transfers).
                    getTravelTimeMinutes();

            double totalTravelTime = (travelTimeOriginStopToDestinationStop == -1) ? Double.MAX_VALUE :
                    travelTimesOriginPointToOriginStops.get(i) + travelTimeOriginStopToDestinationStop +
                            travelTimeDestinationStopToDestinationPoint;

            if (totalTravelTime < leastTotalTravelTime) {
                leastTotalTravelTime = totalTravelTime;
                solutionStopIndex = i;
            }
        }
        long singleMultiModalQueryEvaluationEndTime = System.nanoTime();
        double singleMultiModalQueryEvaluationDuration = (double) ((singleMultiModalQueryEvaluationEndTime -
                singleMultiModalQueryEvaluationStartTime) / NANOSECONDS_PER_SECOND);

        // Ascribe output(s) to multi-modal query response instance
        if (leastTotalTravelTime != Double.MAX_VALUE) {
            Stop solutionStop = stopsNearOriginNode.get(solutionStopIndex);

            if (solutionTypeFlag.equalsIgnoreCase("Exact")) {
                multiModalQueryResponses.setCountOriginStopsConsideredExactSolution(stopsNearOriginNode.size());
                multiModalQueryResponses.setTimeElapsedQueryProcessingExactSolution(
                        singleMultiModalQueryEvaluationDuration);
                multiModalQueryResponses.setOriginStopIdExactSolution(solutionStop.getStopId());
                multiModalQueryResponses.setOriginStopNameExactSolution(solutionStop.getStopName());
                multiModalQueryResponses.setOriginStopTypeExactSolution(solutionStop.getStopType());
                multiModalQueryResponses.setOriginStopTripVolumeExactSolution(solutionStop.getStopTripCount());
                multiModalQueryResponses.setOriginStopAverageTransferCostExactSolution(solutionStop.
                        getAverageTransferCost());
                multiModalQueryResponses.setTravelTimeOriginToOriginStopExactSolution(
                        travelTimesOriginPointToOriginStops.get(solutionStopIndex));
                multiModalQueryResponses.setTravelTimeOriginStopToDestinationStopExactSolution(leastTotalTravelTime -
                        travelTimesOriginPointToOriginStops.get(solutionStopIndex) -
                        travelTimeDestinationStopToDestinationPoint);
                multiModalQueryResponses.setTotalTravelTimeExactSolution(leastTotalTravelTime);

            } else if (solutionTypeFlag.equalsIgnoreCase("StopHierarchy")) {
                multiModalQueryResponses.setCountOriginStopsConsideredSHSolution(stopsNearOriginNode.size());
                multiModalQueryResponses.setTimeElapsedQueryProcessingSHSolution(
                        singleMultiModalQueryEvaluationDuration);
                multiModalQueryResponses.setOriginStopIdSHSolution(solutionStop.getStopId());
                multiModalQueryResponses.setOriginStopNameSHSolution(solutionStop.getStopName());
                multiModalQueryResponses.setOriginStopTypeSHSolution(solutionStop.getStopType());
                multiModalQueryResponses.setOriginStopTripVolumeSHSolution(solutionStop.getStopTripCount());
                multiModalQueryResponses.setOriginStopAverageTransferCostSHSolution(solutionStop.
                        getAverageTransferCost());
                multiModalQueryResponses.setTravelTimeOriginToOriginStopSHSolution(travelTimesOriginPointToOriginStops.
                        get(solutionStopIndex));
                multiModalQueryResponses.setTravelTimeOriginStopToDestinationStopSHSolution(leastTotalTravelTime -
                        travelTimesOriginPointToOriginStops.get(solutionStopIndex) -
                        travelTimeDestinationStopToDestinationPoint);
                multiModalQueryResponses.setTotalTravelTimeSHSolution(leastTotalTravelTime);
                multiModalQueryResponses.setRelativeTravelTimeDifferenceInSHAndExactSolutions((multiModalQueryResponses.
                        getTotalTravelTimeExactSolution() - leastTotalTravelTime) /
                        multiModalQueryResponses.getTotalTravelTimeExactSolution());

            } else if (solutionTypeFlag.equalsIgnoreCase("TripVolume")) {
                multiModalQueryResponses.setCountOriginStopsConsideredTVSolution(stopsNearOriginNode.size());
                multiModalQueryResponses.setTimeElapsedQueryProcessingTVSolution(
                        singleMultiModalQueryEvaluationDuration);
                multiModalQueryResponses.setOriginStopIdTVSolution(solutionStop.getStopId());
                multiModalQueryResponses.setOriginStopNameTVSolution(solutionStop.getStopName());
                multiModalQueryResponses.setOriginStopTypeTVSolution(solutionStop.getStopType());
                multiModalQueryResponses.setOriginStopTripVolumeTVSolution(solutionStop.getStopTripCount());
                multiModalQueryResponses.setOriginStopAverageTransferCostTVSolution(solutionStop.
                        getAverageTransferCost());
                multiModalQueryResponses.setTravelTimeOriginToOriginStopTVSolution(travelTimesOriginPointToOriginStops.
                        get(solutionStopIndex));
                multiModalQueryResponses.setTravelTimeOriginStopToDestinationStopTVSolution(leastTotalTravelTime -
                        travelTimesOriginPointToOriginStops.get(solutionStopIndex) -
                        travelTimeDestinationStopToDestinationPoint);
                multiModalQueryResponses.setTotalTravelTimeTVSolution(leastTotalTravelTime);
                multiModalQueryResponses.setRelativeTravelTimeDifferenceInTVAndExactSolutions((multiModalQueryResponses.
                        getTotalTravelTimeExactSolution() - leastTotalTravelTime) /
                        multiModalQueryResponses.getTotalTravelTimeExactSolution());
            }
        } else {
            if (solutionTypeFlag.equalsIgnoreCase("Exact")) {
                multiModalQueryResponses.setTotalTravelTimeExactSolution(leastTotalTravelTime);
            } else if (solutionTypeFlag.equalsIgnoreCase("StopHierarchy")) {
                multiModalQueryResponses.setTotalTravelTimeSHSolution(leastTotalTravelTime);
            } else if (solutionTypeFlag.equalsIgnoreCase("TripVolume")) {
                multiModalQueryResponses.setTotalTravelTimeTVSolution(leastTotalTravelTime);
            }
        }
    }

    // Write a "multiModalQueriesResponses.txt" file
    private static void writeMultiModalQueriesResponses(String multiModalQueriesResponsesFilePath,
                                                        LinkedHashMap<Long, MultiModalQueryResponses>
                                                                multiModalQueriesResponses) {
        try {
            // Writer for "multiModalQueriesResponses.csv"
            BufferedWriter multiModalQueriesResponsesWriter = new BufferedWriter(new FileWriter(
                    multiModalQueriesResponsesFilePath));

            // Set up header array
            multiModalQueriesResponsesWriter.write("query_id,origin_point_longitude,origin_point_latitude," +
                    "destination_point_longitude,destination_point_latitude,departure_time_origin_point," +
                    "nearest_origin_node_id,nearest_destination_node_id,destination_stop_id,destination_stop_name," +
                    "travel_time_destination_stop_to_destination,count_origin_stops_considered_exact_solution," +
                    "time_elapsed_query_processing_exact_solution,origin_stop_id_exact_solution," +
                    "origin_stop_name_exact_solution,origin_stop_type_exact_solution," +
                    "origin_stop_trip_volume_exact_solution,origin_stop_average_transfer_cost_exact_solution," +
                    "travel_time_origin_to_origin_stop_exact_solution," +
                    "travel_time_origin_stop_to_destination_stop_exact_solution,total_travel_time_exact_solution," +
                    "count_origin_stops_considered_sh_solution,time_elapsed_query_processing_sh_solution," +
                    "origin_stop_id_sh_solution,origin_stop_name_sh_solution,origin_stop_type_sh_solution," +
                    "origin_stop_trip_volume_sh_solution,origin_stop_average_transfer_cost_sh_solution," +
                    "travel_time_origin_to_origin_stop_sh_solution," +
                    "travel_time_origin_stop_to_destination_stop_sh_solution,total_travel_time_sh_solution," +
                    "relative_travel_time_difference_in_sh_and_exact_solutions," +
                    "count_origin_stops_considered_tv_solution,time_elapsed_query_processing_tv_solution," +
                    "origin_stop_id_tv_solution,origin_stop_name_tv_solution,origin_stop_type_tv_solution," +
                    "origin_stop_trip_volume_tv_solution,origin_stop_average_transfer_cost_tv_solution," +
                    "travel_time_origin_to_origin_stop_tv_solution," +
                    "travel_time_origin_stop_to_destination_stop_tv_solution,total_travel_time_tv_solution," +
                    "relative_travel_time_difference_in_tv_and_exact_solutions\n");

            // Write body based on "multiModalQueriesResponses" hashmap
            for (HashMap.Entry<Long, MultiModalQueryResponses> multiModalQueryResponsesEntry :
                    multiModalQueriesResponses.entrySet()) {
                multiModalQueriesResponsesWriter.write(multiModalQueryResponsesEntry.getKey() + "," +
                        multiModalQueryResponsesEntry.getValue().getOriginPointLongitude() + "," +
                        multiModalQueryResponsesEntry.getValue().getOriginPointLatitude() + "," +
                        multiModalQueryResponsesEntry.getValue().getDestinationPointLongitude() + "," +
                        multiModalQueryResponsesEntry.getValue().getDestinationPointLatitude() + "," +
                        multiModalQueryResponsesEntry.getValue().getDepartureTimeOriginPoint() + "," +
                        multiModalQueryResponsesEntry.getValue().getNearestOriginNodeId() + "," +
                        multiModalQueryResponsesEntry.getValue().getNearestDestinationNodeId() + "," +
                        multiModalQueryResponsesEntry.getValue().getDestinationStopId() + "," +
                        multiModalQueryResponsesEntry.getValue().getDestinationStopName() + "," +
                        multiModalQueryResponsesEntry.getValue().getTravelTimeDestinationStopToDestination() + "," +
                        multiModalQueryResponsesEntry.getValue().getCountOriginStopsConsideredExactSolution() + "," +
                        String.format("%.5f", multiModalQueryResponsesEntry.getValue().
                                getTimeElapsedQueryProcessingExactSolution()) + "," +
                        multiModalQueryResponsesEntry.getValue().getOriginStopIdExactSolution() + "," +
                        multiModalQueryResponsesEntry.getValue().getOriginStopNameExactSolution() + "," +
                        multiModalQueryResponsesEntry.getValue().getOriginStopTypeExactSolution() + "," +
                        multiModalQueryResponsesEntry.getValue().getOriginStopTripVolumeExactSolution() + "," +
                        multiModalQueryResponsesEntry.getValue().getOriginStopAverageTransferCostExactSolution() + "," +
                        multiModalQueryResponsesEntry.getValue().getTravelTimeOriginToOriginStopExactSolution() + "," +
                        multiModalQueryResponsesEntry.getValue().
                                getTravelTimeOriginStopToDestinationStopExactSolution() + "," +
                        multiModalQueryResponsesEntry.getValue().getTotalTravelTimeExactSolution() + "," +
                        multiModalQueryResponsesEntry.getValue().getCountOriginStopsConsideredSHSolution() + "," +
                        String.format("%.5f", multiModalQueryResponsesEntry.getValue().
                                getTimeElapsedQueryProcessingSHSolution()) + "," +
                        multiModalQueryResponsesEntry.getValue().getOriginStopIdSHSolution() + "," +
                        multiModalQueryResponsesEntry.getValue().getOriginStopNameSHSolution() + "," +
                        multiModalQueryResponsesEntry.getValue().getOriginStopTypeSHSolution() + "," +
                        multiModalQueryResponsesEntry.getValue().getOriginStopTripVolumeSHSolution() + "," +
                        multiModalQueryResponsesEntry.getValue().getOriginStopAverageTransferCostSHSolution() + "," +
                        multiModalQueryResponsesEntry.getValue().getTravelTimeOriginToOriginStopSHSolution() + "," +
                        multiModalQueryResponsesEntry.getValue().
                                getTravelTimeOriginStopToDestinationStopSHSolution() + "," +
                        multiModalQueryResponsesEntry.getValue().getTotalTravelTimeSHSolution() + "," +
                        String.format("%.18f", multiModalQueryResponsesEntry.getValue().
                                getRelativeTravelTimeDifferenceInSHAndExactSolutions()) + "," +
                        multiModalQueryResponsesEntry.getValue().getCountOriginStopsConsideredTVSolution() + "," +
                        String.format("%.5f", multiModalQueryResponsesEntry.getValue().
                                getTimeElapsedQueryProcessingTVSolution()) + "," +
                        multiModalQueryResponsesEntry.getValue().getOriginStopIdTVSolution() + "," +
                        multiModalQueryResponsesEntry.getValue().getOriginStopNameTVSolution() + "," +
                        multiModalQueryResponsesEntry.getValue().getOriginStopTypeTVSolution() + "," +
                        multiModalQueryResponsesEntry.getValue().getOriginStopTripVolumeTVSolution() + "," +
                        multiModalQueryResponsesEntry.getValue().getOriginStopAverageTransferCostTVSolution() + "," +
                        multiModalQueryResponsesEntry.getValue().getTravelTimeOriginToOriginStopTVSolution() + "," +
                        multiModalQueryResponsesEntry.getValue().
                                getTravelTimeOriginStopToDestinationStopTVSolution() + "," +
                        multiModalQueryResponsesEntry.getValue().getTotalTravelTimeTVSolution() + "," +
                        String.format("%.18f", multiModalQueryResponsesEntry.getValue().
                                getRelativeTravelTimeDifferenceInTVAndExactSolutions())
                        + "\n");
            }
            System.out.println("Multi-modal queries' responses written to: " + multiModalQueriesResponsesFilePath);

        } catch (IOException iOE) {
            System.out.println("Input-output exception. Please check the \"multi-modal queries' responses\" hashmap.");
        }
    }
}