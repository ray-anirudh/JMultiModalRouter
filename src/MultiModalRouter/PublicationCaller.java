/**
 * Author: Anirudh Ray
 * Institution: Professorship of Traffic Engineering and Control, Technical University of Munich
 * Department: Mobility Systems Engineering, School of Engineering and Design
 * E-mail Address: Anirudh.Ray@tum.de
 * Purpose: Component of a Java-based multi-modal routing algorithm, built using RAPTOR, Dijkstra's algorithm, and
 * KD-Trees; Caller class built explicitly for publication purposes
 */

package src.MultiModalRouter;
// TUM: Technical University of Munich
// RAPTOR: Round-based Public Transit Router (Delling et. al., 2015)
// OSM: OpenStreetMap
// OPL: Object-Per-Line (file format)

import src.MultiModalRouter.QueryManager.MultiModalQuery;
import src.MultiModalRouter.QueryManager.MultiModalQueryReader;
import src.MultiModalRouter.QueryManager.MultiModalQueryResponsesPub;
import src.MultiModalRouter.TAZManager.TAZCentroid;
import src.MultiModalRouter.TAZManager.TAZCentroidsReader;
import src.NearestNeighbourFinder.KDTreeForNodes;
import src.NearestNeighbourFinder.KDTreeForStops;

import src.NearestNeighbourFinder.KDTreeForTAZCentroids;
import src.PublicTransportRouter.GTFSDataManager.*;
import src.PublicTransportRouter.RoutingAlgorithm.RAPTOR;

import src.PublicTransportRouter.RoutingAlgorithm.TransitQueryResponse;
import src.RoadTransportRouter.OSMDataManager.Link;
import src.RoadTransportRouter.OSMDataManager.Node;
import src.RoadTransportRouter.OSMDataManager.OSMDataReaderWriter;
import src.RoadTransportRouter.RoutingAlgorithm.DijkstraBasedRouter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.*;

public class PublicationCaller {

    /**
     * ATTRIBUTE DEFINITIONS
     */

    private static final CallerParametersReader callerParametersReader = new CallerParametersReader();
    private static final String callerParametersFilePath = "JMMRParameters/CallerParameters.txt";
    private static final long NANOSECONDS_PER_MIN = 60_000_000_000L;
    private static final long NANOSECONDS_PER_SECOND = 1_000_000_000L;

    /**
     * BEHAVIOUR DEFINITIONS
     */

    public static void main(String[] args) {
        // Set the caller's parameters
        setCallerParameters(callerParametersReader, callerParametersFilePath);
        long beginQueryId = callerParametersReader.getBeginQueryId();
        long numberMultiModalQueries = callerParametersReader.getNumberMultiModalQueries();
        double minimumDestinationLatitude = callerParametersReader.getMinimumDestinationLatitude();
        double maximumDestinationLatitude = callerParametersReader.getMaximumDestinationLatitude();
        double minimumDestinationLongitude = callerParametersReader.getMinimumDestinationLongitude();
        double maximumDestinationLongitude = callerParametersReader.getMaximumDestinationLongitude();
        double minimumDrivingDistance = callerParametersReader.getMinimumDrivingDistance();
        double maximumDrivingDistance = callerParametersReader.getMaximumDrivingDistance();
        double avgWalkingSpeedMPerMin = callerParametersReader.getAvgWalkingSpeedMPMin();
        double avgDrivingSpeedMPerMin = callerParametersReader.getAvgDrivingSpeedMPMin();
        double avgODMWaitTimeMin = callerParametersReader.getAvgODMWaitTimeMin();
        int stopTypeToIgnore = callerParametersReader.getStopTypeToIgnore();
        int cutoffDailyServiceCountOfStop = callerParametersReader.getCutoffDailyServiceCountOfStop();
        String osmOplExtractFilePath = callerParametersReader.getOsmOplExtractFilePath();
        String dijkstraFolderPath = callerParametersReader.getDijkstraFolderPath();
        String gtfsFolderPath = callerParametersReader.getGtfsFolderPath();
        String raptorFolderPath = callerParametersReader.getRaptorFolderPath();
        String gtfsParametersFilePath = callerParametersReader.getGtfsParametersFilePath();
        String multiModalQueriesFilePath = callerParametersReader.getMultiModalQueriesFilePath();
        String tAZCentroidsFilePath = callerParametersReader.getTAZCentroidsFilePath();
        double departureTimeForTAZToTAZTravel = callerParametersReader.getDepartureTimeForTAZToTAZTravel();
        /* Debugging statements:
        System.out.println(beginQueryId + ", " + numberMultiModalQueries + ", " + minimumDrivingDistance + ", " +
                maximumDrivingDistance + ", " + avgWalkingSpeedMPerMin + ", " + avgDrivingSpeedMPerMin + ", " +
                avgODMWaitTimeMin + ", " + stopTypeToIgnore + ", " + cutoffDailyServiceCountOfStop + ", " +
                osmOplExtractFilePath + ", " + dijkstraFolderPath + ", " + gtfsFolderPath + ", " +
                raptorFolderPath + ", " + gtfsParametersFilePath + ", " + multiModalQueriesFilePath);
        */

        /**
         * OSM data reader-writer instantiation to read, write, and store data
         */
        long osmStartTime = System.nanoTime();
        OSMDataReaderWriter osmDataReaderWriterForDijkstra = new OSMDataReaderWriter();
        getDijkstraMaps(osmOplExtractFilePath, dijkstraFolderPath, osmDataReaderWriterForDijkstra);

        // Get all data for Dijkstra algorithm's execution
        LinkedHashMap<Long, Link> links = osmDataReaderWriterForDijkstra.getLinks();
        LinkedHashMap<Long, Node> nodes = osmDataReaderWriterForDijkstra.getNodes();
        long osmEndTime = System.nanoTime();
        double osmDataProcessingDuration = (double) (osmEndTime - osmStartTime);

        // Set up the KD-Tree for nearest node searches
        long kDNodeStartTime = System.nanoTime();
        Node[] nodesForNNSearches = nodes.values().toArray(new Node[0]);
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
        getRAPTORMaps(gtfsFolderPath, gtfsParametersFilePath, raptorFolderPath, gtfsDataReaderWriterForRAPTOR);

        // Get all data for RAPTOR execution
        LinkedHashMap<Integer, Route> routes = gtfsDataReaderWriterForRAPTOR.getRoutes();
        LinkedHashMap<Integer, Trip> trips = gtfsDataReaderWriterForRAPTOR.getTrips();
        LinkedHashMap<Integer, RouteStop> routeStops = gtfsDataReaderWriterForRAPTOR.getRouteStops();
        LinkedHashMap<Integer, StopTime> stopTimes = gtfsDataReaderWriterForRAPTOR.getStopTimes();
        LinkedHashMap<Integer, Stop> stops = gtfsDataReaderWriterForRAPTOR.getStops();
        LinkedHashMap<Integer, StopRoute> stopRoutes = gtfsDataReaderWriterForRAPTOR.getStopRoutes();
        LinkedHashMap<Integer, Transfer> transfers = gtfsDataReaderWriterForRAPTOR.getTransfers();
        long gtfsEndTime = System.nanoTime();
        double gtfsDataProcessingDuration = (double) (gtfsEndTime - gtfsStartTime);

        // Set up the KD-Tree for nearest stop searches
        long kDStopStartTime = System.nanoTime();
        Stop[] stopsForNNSearches = stops.values().toArray(new Stop[0]);
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
         * (Possible to convert the procedure to a flag-based method)
         */
        // Load all multi-modal queries, and instantiate the responses map
        long queryGenStartTime = System.nanoTime();
        // Consideration of trips simulated by TUM's Travel Behaviour Professorship for Munich and its environs
        MultiModalQueryReader multiModalQueryReader = new MultiModalQueryReader();
        multiModalQueryReader.readMultiModalQueries(multiModalQueriesFilePath);
        LinkedHashMap<Long, MultiModalQuery> allMultiModalQueries = multiModalQueryReader.getMultiModalQueries();
        LinkedHashMap<Long, MultiModalQuery> multiModalQueries = new LinkedHashMap<>();

        // Limit the number of multi-modal queries to be processed, slicing through the master-list of queries
        for (long multiModalQueryCount = beginQueryId; multiModalQueryCount <= beginQueryId + numberMultiModalQueries;
             multiModalQueryCount++) {
            multiModalQueries.put(multiModalQueryCount, allMultiModalQueries.get(multiModalQueryCount));
        }

//        // Alternate pathway (bi-variate normal distribution-based) for generating random multi-modal queries
//        String multiModalQueriesFilePath = "D:/Documents - Education + Work/Education - TUM/Year 2/Fourth Semester/" +
//                "MasterThesis/Results/MultiModalQueriesMap/multiModalQueries.txt";
//        MultiModalQueryGenerator multiModalQueryGenerator = new MultiModalQueryGenerator();
//        LinkedHashMap<Long, MultiModalQuery> multiModalQueries = multiModalQueryGenerator.
//                generateQueries(numberMultiModalQueries);
//                // Method argument contains number of queries to generate
//        multiModalQueryGenerator.writeMultiModalQueries(multiModalQueriesFilePath);
        long queryGenEndTime = System.nanoTime();
        double queryGenerationDuration = (double) (queryGenEndTime - queryGenStartTime);

        // Hashmap to store learning data for all possible journeys resulting for each query
        LinkedHashMap<Long, ArrayList<MultiModalQueryResponsesPub>> multiModalQueriesResponsesPub = new
                LinkedHashMap<>();
        System.out.println("\n" +
                multiModalQueries.size() + " multi-modal queries for JavaMultiModalRouter read in " + String.format
                ("%.3f", queryGenerationDuration / NANOSECONDS_PER_MIN) + " minutes.");

        // Set up routers' instances
        DijkstraBasedRouter dijkstraBasedRouter = new DijkstraBasedRouter();
        RAPTOR rAPTOR = new RAPTOR();

        // Set up an ExecutorService instance for parallel processing of inter-TAZ travel times
        int totalAvailableProcessorsForTAZ = Runtime.getRuntime().availableProcessors();
        double processingCapacityUtilizationFactorForTAZ = 0.7;
        int totalLeveragedProcessorsForTAZ = (int) (totalAvailableProcessorsForTAZ *
                processingCapacityUtilizationFactorForTAZ);

        // Set up an ExecutorService instance for parallel processing of queries, and a list to hold Future objects
        int totalAvailableProcessors = Runtime.getRuntime().availableProcessors();
        double processingCapacityUtilizationFactor = 0.9;
        int totalLeveragedProcessors = (int) (totalAvailableProcessors * processingCapacityUtilizationFactor);

        ExecutorService executor = Executors.newFixedThreadPool(totalLeveragedProcessors);
        ArrayList<Future<ArrayList<MultiModalQueryResponsesPub>>> futures = new ArrayList<>();

        /**
         * Find TAZ-to-TAZ travel times
         */
        TAZCentroidsReader tAZCentroidsReader = new TAZCentroidsReader();
        tAZCentroidsReader.readTAZCentroids(tAZCentroidsFilePath);
        LinkedHashMap<Integer, TAZCentroid> tAZCentroidsMaster = tAZCentroidsReader.getTAZCentroids();
        LinkedHashMap<Integer, TAZCentroid> originTAZCentroids = new LinkedHashMap<>();
        LinkedHashMap<Integer, TAZCentroid> destinationTAZCentroids = new LinkedHashMap<>();

        for (Integer tAZCentroidId : tAZCentroidsMaster.keySet()) {
            TAZCentroid tAZCentroid = tAZCentroidsMaster.get(tAZCentroidId);
            if ((tAZCentroid.getLatitude() < maximumDestinationLatitude) &&
                    (tAZCentroid.getLatitude() > minimumDestinationLatitude) &&
                    (tAZCentroid.getLongitude() < maximumDestinationLongitude) &&
                    (tAZCentroid.getLongitude() > minimumDestinationLongitude)) {
                destinationTAZCentroids.put(tAZCentroidId, tAZCentroid);
            } else {
                originTAZCentroids.put(tAZCentroidId, tAZCentroid);
            }
        }

        System.out.println("Origin TAZ count: " + originTAZCentroids.size());
        System.out.println("Destination TAZ count: " + destinationTAZCentroids.size());

        TAZCentroid[] originTAZCentroidsArray = originTAZCentroids.values().toArray(new TAZCentroid[0]);
        KDTreeForTAZCentroids kDTreeForOriginTAZCentroids = new KDTreeForTAZCentroids();
        kDTreeForOriginTAZCentroids.buildTAZCentroidBasedKDTree(originTAZCentroidsArray);

        TAZCentroid[] destinationTAZCentroidsArray = destinationTAZCentroids.values().toArray(new TAZCentroid[0]);
        KDTreeForTAZCentroids kDTreeForDestinationTAZCentroids = new KDTreeForTAZCentroids();
        kDTreeForDestinationTAZCentroids.buildTAZCentroidBasedKDTree(destinationTAZCentroidsArray);

        ExecutorService executorForTAZ = Executors.newFixedThreadPool(totalLeveragedProcessorsForTAZ);
        LinkedHashMap<Integer, LinkedHashMap<Integer, Double>> tAZTravelTimeMatrix = new LinkedHashMap<>();
        // Integer keys above refer to origin and destination TAZ IDs, and double values refer to travel times (minutes)

        for (TAZCentroid originTAZCentroid : originTAZCentroids.values()) {
            Node nodeNearOriginTAZCentroid = kDTreeForNodes.findNearestNode(originTAZCentroid.getLongitude(),
                    originTAZCentroid.getLatitude());
            ArrayList<Stop> stopsNearOriginTAZCentroid = kDTreeForStops.findStopsWithinDoughnut(originTAZCentroid.
                    getLongitude(), originTAZCentroid.getLatitude(), minimumDrivingDistance, maximumDrivingDistance);

            // Clean the master list of stops to limit redundant routing
            HashSet<String> uniqueOriginTAZStops = new HashSet<>();
            Iterator<Stop> originTAZStopIterator = stopsNearOriginTAZCentroid.iterator();
            while(originTAZStopIterator.hasNext()) {
                Stop currentTAZStop = originTAZStopIterator.next();
                String tAZStopKey = currentTAZStop.getStopName() + "-" + currentTAZStop.getStopType();
                if (uniqueOriginTAZStops.contains(tAZStopKey)) {
                    originTAZStopIterator.remove();
                } else {
                    uniqueOriginTAZStops.add(tAZStopKey);
                }
            }

            ArrayList<Double> travelTimesOriginTAZToOriginStops = new ArrayList<>();
            for (Stop stopNearOriginTAZCentroid : stopsNearOriginTAZCentroid) {
                Node nodeNearOriginTAZStop = kDTreeForNodes.findNearestNode(stopNearOriginTAZCentroid.
                        getStopLongitude(), stopNearOriginTAZCentroid.getStopLatitude());
                double timeToNetworkNodeFromOriginTAZCentroid = nodeNearOriginTAZCentroid.equiRectangularDistanceTo(
                        originTAZCentroid.getLongitude(), originTAZCentroid.getLatitude());
                double timeToOriginTAZStopFromNetworkNode = nodeNearOriginTAZStop.equiRectangularDistanceTo(
                        stopNearOriginTAZCentroid.getStopLongitude(), stopNearOriginTAZCentroid.getStopLatitude());
                double timeFromOriginTAZCentroidNodeToOriginTAZStopNode = dijkstraBasedRouter.
                        findShortestDrivingPathCostMin(nodeNearOriginTAZCentroid.getNodeId(), nodeNearOriginTAZStop.
                                getNodeId(), nodes, links);
                travelTimesOriginTAZToOriginStops.add(timeToNetworkNodeFromOriginTAZCentroid +
                        timeFromOriginTAZCentroidNodeToOriginTAZStopNode + timeToOriginTAZStopFromNetworkNode);
            }

            // Parallel-process pairwise inter-TAZ routing queries
            LinkedHashMap<Integer, Double> travelTimeMatrixRow = new LinkedHashMap<>();
            ArrayList<Callable<Void>> tasksForTAZ = new ArrayList<>();

            for (TAZCentroid destinationTAZCentroid : destinationTAZCentroids.values()) {
                tasksForTAZ.add(() -> {
                    // Calculate travel times for this particular destination TAZ
                    Stop stopNearDestinationTAZCentroid = kDTreeForStops.findNearestStop(destinationTAZCentroid.
                                    getLongitude(), destinationTAZCentroid.getLatitude());
                    Node nodeNearDestinationTAZCentroid = kDTreeForNodes.findNearestNode(destinationTAZCentroid.
                                    getLongitude(), destinationTAZCentroid.getLatitude());
                    Node nodeNearDestinationTAZStop = kDTreeForNodes.findNearestNode(stopNearDestinationTAZCentroid.
                                    getStopLongitude(), stopNearDestinationTAZCentroid.getStopLatitude());
                    double timeFromDestinationStopToNetworkNode = stopNearDestinationTAZCentroid.
                            equiRectangularDistanceTo(nodeNearDestinationTAZStop.getNodeLongitude(),
                                    nodeNearDestinationTAZStop.getNodeLatitude());
                    double timeFromNetworkNodeToDestination = nodeNearDestinationTAZCentroid.equiRectangularDistanceTo(
                            destinationTAZCentroid.getLongitude(), destinationTAZCentroid.getLatitude());
                    double timeFromDestinationTAZStopNodeToDestinationTAZCentroid = dijkstraBasedRouter.
                            findShortestDrivingPathCostMin(nodeNearDestinationTAZStop.getNodeId(),
                                    nodeNearDestinationTAZCentroid.getNodeId(), nodes, links) * avgDrivingSpeedMPerMin /
                                    avgWalkingSpeedMPerMin;
                    double travelTimeDestinationStopToDestinationTAZ = timeFromDestinationStopToNetworkNode +
                            timeFromNetworkNodeToDestination + timeFromDestinationTAZStopNodeToDestinationTAZCentroid;

                    double totalMinimumTravelTime = 480; // 8 hours is set as the time to get from anywhere to anywhere
                    for (int i = 0; i < stopsNearOriginTAZCentroid.size(); i++) {
                        double transitBasedCrossTAZTravelTime = rAPTOR.findShortestTransitPath(
                                stopsNearOriginTAZCentroid.get(i).getStopId(), stopNearDestinationTAZCentroid.
                                                getStopId(), departureTimeForTAZToTAZTravel, routeStops, stopTimes,
                                        stops, stopRoutes, transfers).getTravelTimeMinutes();
                        // With RAPTOR, always check for -1's, as double values are used to test for changing the output
                        double totalCrossTAZTravelTime = travelTimesOriginTAZToOriginStops.get(i) +
                                transitBasedCrossTAZTravelTime + travelTimeDestinationStopToDestinationTAZ;

                        if ((transitBasedCrossTAZTravelTime != -1) && (totalCrossTAZTravelTime <
                                totalMinimumTravelTime)) {
                            totalMinimumTravelTime = totalCrossTAZTravelTime;
                        }
                    }

                    /* Debugging statements:
                    System.out.println(destinationTAZCentroid.getId() + ", " + totalMinimumTravelTime);
                    System.out.println(System.nanoTime());
                    */
                    travelTimeMatrixRow.put(destinationTAZCentroid.getId(), totalMinimumTravelTime);
                    return null;
                });
            }

            // Submit tasks for all destination TAZs
            try {
                List<Future<Void>> futuresForTAZ = executorForTAZ.invokeAll(tasksForTAZ);
                for (Future<Void> futureForTAZ : futuresForTAZ) {
                    futureForTAZ.get(); // Wait for task completion
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Add the computed row to the final travel time matrix
            tAZTravelTimeMatrix.put(originTAZCentroid.getId(), travelTimeMatrixRow);
        }
        executorForTAZ.shutdown();

        /**
         * Execute queries on the JMultiModalRouter architecture
         */
        long queriesSolvingStartTime = System.nanoTime();
        for (HashMap.Entry<Long, MultiModalQuery> multiModalQueryEntry : multiModalQueries.entrySet()) {
            Long queryEntryId = multiModalQueryEntry.getKey();
            Random solutionTypeRandomizer = new Random(7);
            int solutionTypeSelector = solutionTypeRandomizer.nextInt(9);

            Future<ArrayList<MultiModalQueryResponsesPub>> future = executor.submit(() -> {

                // Get the multi-modal query and response instances
                MultiModalQuery multiModalQuery = multiModalQueryEntry.getValue();
                ArrayList<MultiModalQueryResponsesPub> querySpecificMultiModalQueryResponsesPub = new ArrayList<>();

                // Parse locational and temporal data from the query
                double originPointLongitude = multiModalQuery.getOriginLongitude();
                double originPointLatitude = multiModalQuery.getOriginLatitude();
                double destinationPointLongitude = multiModalQuery.getDestinationLongitude();
                double destinationPointLatitude = multiModalQuery.getDestinationLatitude();
                int originPointDepartureTime = multiModalQuery.getDepartureTime();

                // Determine nodes nearest to the origin and destination points
                Node originNode = kDTreeForNodes.findNearestNode(originPointLongitude, originPointLatitude);
                Node destinationNode = kDTreeForNodes.findNearestNode(destinationPointLongitude,
                        destinationPointLatitude);
                long destinationNodeId = destinationNode.getNodeId();

                // Determine stop (and allied node) nearest to destination point
                Stop destinationStop = kDTreeForStops.findNearestStop(destinationPointLongitude,
                        destinationPointLatitude);
                int destinationStopId = destinationStop.getStopId();

                Node destinationStopNearestNode = kDTreeForNodes.findNearestNode(destinationStop.getStopLongitude(),
                        destinationStop.getStopLatitude());
                long destinationStopNearestNodeId = destinationStopNearestNode.getNodeId();
                double destinationStopNearestNodeLongitude = destinationStopNearestNode.getNodeLongitude();
                double destinationStopNearestNodeLatitude = destinationStopNearestNode.getNodeLatitude();

                // Determine travel time from destination stop to destination
                double travelTimeDestinationStopToDestination = (destinationStop.equiRectangularDistanceTo(
                        destinationStopNearestNodeLongitude, destinationStopNearestNodeLatitude) +
                        destinationNode.equiRectangularDistanceTo(destinationPointLongitude, destinationPointLatitude) +
                        (dijkstraBasedRouter.findShortestDrivingPathCostMin(destinationStopNearestNodeId,
                                destinationNodeId, nodes, links) * avgDrivingSpeedMPerMin)) / avgWalkingSpeedMPerMin;

                /**
                 * Building the set of origin stops (must leverage the solution type randomizer for learning data
                 * creation)
                 */
                ArrayList<Stop> stopsNearOrigin = kDTreeForStops.findStopsWithinDoughnut(originPointLongitude,
                        originPointLatitude, minimumDrivingDistance, maximumDrivingDistance);

                // Filtering in unique stops near the origin node
                HashSet<String> uniqueOriginStops = new HashSet<>();
                Iterator<Stop> originStopIterator = stopsNearOrigin.iterator();
                while (originStopIterator.hasNext()) {
                    Stop originStop = originStopIterator.next();
                    String stopKey = originStop.getStopName() + "-" + originStop.getStopType();
                    if (uniqueOriginStops.contains(stopKey)) {
                        originStopIterator.remove();
                    } else {
                        uniqueOriginStops.add(stopKey);
                    }
                }

                if (solutionTypeSelector % 3 != 0) {
                    stopsNearOrigin.removeIf(originStop -> originStop.getStopTripCount() <
                            cutoffDailyServiceCountOfStop);
                }

                // Exactly... route!
                runRAPTORAndDijkstra(stopsNearOrigin, kDTreeForNodes, originPointLongitude,
                        originPointLatitude, originNode, destinationPointLongitude, destinationPointLatitude,
                        destinationNode, destinationStopNearestNode, originPointDepartureTime, avgWalkingSpeedMPerMin,
                        avgODMWaitTimeMin, travelTimeDestinationStopToDestination, destinationStopId, rAPTOR,
                        dijkstraBasedRouter, nodes, links, routeStops, stopTimes, stops, stopRoutes, transfers,
                        querySpecificMultiModalQueryResponsesPub, tAZTravelTimeMatrix, kDTreeForOriginTAZCentroids,
                        kDTreeForDestinationTAZCentroids);

                return querySpecificMultiModalQueryResponsesPub;
            });
            futures.add(future);
        }

        // Process the results and add to the response map before shutting down the executor
        for (int i = 0; i < futures.size(); i++) {
            try {
                ArrayList<MultiModalQueryResponsesPub> responsePubList = futures.get(i).get();
                if (responsePubList != null) {
                    multiModalQueriesResponsesPub.put((long) i + beginQueryId, responsePubList);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();

        System.out.println("Response list size: " + multiModalQueriesResponsesPub.size());

        long queriesSolvingEndTime = System.nanoTime();
        double queriesSolvingDuration = (double) (queriesSolvingEndTime - queriesSolvingStartTime);
        System.out.println("\n" + multiModalQueriesResponsesPub.size() + " multi-modal queries solved in " +
                String.format("%.3f", queriesSolvingDuration / NANOSECONDS_PER_MIN) + " minutes.");

        // Write out responses to the multi-modal queries in batches (based on query ID)
        for (long i : multiModalQueriesResponsesPub.keySet()) {
            if (i % 10_000 == 1) {
                String multiModalQueriesResponsesPubFilePath = "D:/Documents - Education + Work/Education -" +
                        "TUM/Year 2/Fourth Semester/MasterThesis/Results/LearningData/" +
                        "multiModalQueriesResponsesPub" + i + ".csv";
                int queryVolumeToWrite = 10;
                writeMultiModalQueriesResponses(i, queryVolumeToWrite, multiModalQueriesResponsesPubFilePath,
                        multiModalQueriesResponsesPub);
            }
        }
    }

    // Set the parameters required for running the caller class
    public static void setCallerParameters(CallerParametersReader callerParametersReader,
                                           String callerParametersFilePath) {
        callerParametersReader.readCallerParameters(callerParametersFilePath);
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
                                      String gtfsParametersFilePath,
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
        gtfsDataReaderWriterForRAPTOR.readAndFilterGTFSRoutes(gtfsRoutesFilePath, gtfsParametersFilePath);
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

    // Run RAPTOR and Dijkstra algorithms and update the multi-modal query response object (pertaining to the pub)
    private static void runRAPTORAndDijkstra(ArrayList<Stop> stopsNearOrigin, KDTreeForNodes kDTreeForNodes,
                                             double originPointLongitude, double originPointLatitude, Node originNode,
                                             double destinationPointLongitude, double destinationPointLatitude,
                                             Node destinationNode, Node destinationStopNearestNode,
                                             int originPointDepartureTime, double avgWalkingSpeedMPerMin,
                                             double avgODMWaitTimeMin,
                                             double travelTimeDestinationStopToDestinationPoint, int destinationStopId,
                                             RAPTOR rAPTOR, DijkstraBasedRouter dijkstraBasedRouter,
                                             LinkedHashMap<Long, Node> nodes, LinkedHashMap<Long, Link> links,
                                             LinkedHashMap<Integer, RouteStop> routeStops,
                                             LinkedHashMap<Integer, StopTime> stopTimes,
                                             LinkedHashMap<Integer, Stop> stops,
                                             LinkedHashMap<Integer, StopRoute> stopRoutes,
                                             LinkedHashMap<Integer, Transfer> transfers,
                                             ArrayList<MultiModalQueryResponsesPub>
                                                     querySpecificMultiModalQueryResponsesPub,
                                             LinkedHashMap<Integer, LinkedHashMap<Integer, Double>>
                                                     tAZTravelTimeMatrix,
                                             KDTreeForTAZCentroids kDTreeForOriginTAZCentroids,
                                             KDTreeForTAZCentroids kDTreeForDestinationTAZCentroids) {
        long journeyFindingStartTime = System.nanoTime();
        double leastTotalTravelTime = Double.MAX_VALUE;
        int solutionStopIndex;

        // Derive travel times from origin point to stops in its vicinity
        ArrayList<Double> travelTimesOriginPointToOriginStops = new ArrayList<>();
        ArrayList<Node> originStopsNearestNodeList = new ArrayList<>();

        for (Stop stopNearOrigin : stopsNearOrigin) {
            Node nodeNearOriginStop = kDTreeForNodes.findNearestNode(stopNearOrigin.getStopLongitude(),
                    stopNearOrigin.getStopLatitude());
            originStopsNearestNodeList.add(nodeNearOriginStop);

            travelTimesOriginPointToOriginStops.add(avgODMWaitTimeMin + // Waiting for ODM service is expected
                    // Walking to and from ODM pick-up and drop-off is also expected
                    ((originNode.equiRectangularDistanceTo(originPointLongitude, originPointLatitude) +
                            nodeNearOriginStop.equiRectangularDistanceTo(stopNearOrigin.getStopLongitude(),
                                    stopNearOrigin.getStopLatitude())) / avgWalkingSpeedMPerMin) +
                    // Driving between network nodes via an ODM vehicle is also expected
                    (dijkstraBasedRouter.findShortestDrivingPathCostMin(originNode.getNodeId(),
                            nodeNearOriginStop.getNodeId(), nodes, links)));
        }

        ArrayList<TransitQueryResponse> transitQueryResponses = new ArrayList<>();
        // Calculate travel times between origin-destination stops, and report a list of transit query responses
        for (int i = 0; i < stopsNearOrigin.size(); i++) {
            TransitQueryResponse transitQueryResponse = rAPTOR.findShortestTransitPath(stopsNearOrigin.get(i).
                                    getStopId(), destinationStopId, (originPointDepartureTime +
                                    travelTimesOriginPointToOriginStops.get(i)), routeStops, stopTimes, stops,
                            stopRoutes, transfers);
            // If any attribute of response is -1, a transit path was not found via RAPTOR (unlikely for graphs)
            transitQueryResponses.add(transitQueryResponse);

            double totalTravelTime = (transitQueryResponse.getTravelTimeMinutes() == -1) ? Double.MAX_VALUE :
                    travelTimesOriginPointToOriginStops.get(i) + transitQueryResponse.getTravelTimeMinutes() +
                            travelTimeDestinationStopToDestinationPoint;

            if (totalTravelTime < leastTotalTravelTime) {
                leastTotalTravelTime = totalTravelTime;
                solutionStopIndex = i;
            }
        }

        long journeyFindingEndTime = System.nanoTime();
        long journeyComputationTimeNs = journeyFindingEndTime - journeyFindingStartTime;

        if (leastTotalTravelTime == Double.MAX_VALUE) {
            return;
        }

        for (int i = 0; i < transitQueryResponses.size(); i++) {
            TransitQueryResponse transitQueryResponse = transitQueryResponses.get(i);
            if (transitQueryResponse.getTravelTimeMinutes() != -1) {
                Stop originStop = stopsNearOrigin.get(i);
                Stop destinationStop = stops.get(destinationStopId);
                Node nodeNearOriginStop = originStopsNearestNodeList.get(i);
                double totalJourneyTime = travelTimesOriginPointToOriginStops.get(i) + transitQueryResponse.
                        getTravelTimeMinutes() + travelTimeDestinationStopToDestinationPoint;
                double relativeDiffMinTravelTime = (totalJourneyTime - leastTotalTravelTime) / leastTotalTravelTime;

                TAZCentroid tAZNearOrigin = kDTreeForOriginTAZCentroids.findNearestTAZCentroid(
                        originStop.getStopLongitude(), originStop.getStopLatitude());
                TAZCentroid tAZNearDestination = kDTreeForDestinationTAZCentroids.findNearestTAZCentroid(
                        destinationStop.getStopLongitude(), destinationStop.getStopLatitude());

                MultiModalQueryResponsesPub multiModalQueryResponsesPub = new MultiModalQueryResponsesPub(
                        originPointLongitude, originPointLatitude, destinationPointLongitude, destinationPointLatitude,
                        originPointDepartureTime, tAZNearOrigin.getId(), tAZNearDestination.getId(),
                        tAZTravelTimeMatrix.get(tAZNearOrigin.getId()).get(tAZNearDestination.getId()),
                        1, 1, originNode.getNodeId(),
                        originNode.getNodeLongitude(), originNode.getNodeLatitude(), destinationNode.getNodeId(),
                        destinationNode.getNodeLongitude(), destinationNode.getNodeLatitude(), destinationStopId,
                        destinationStop.getStopName(), destinationStop.getStopType(),
                        destinationStop.getStopTripCount(), destinationStop.getAverageTransferCost(),
                        destinationStop.getStopLongitude(), destinationStop.getStopLatitude(),
                        destinationStopNearestNode.getNodeId(), destinationStopNearestNode.getNodeLongitude(),
                        destinationStopNearestNode.getNodeLatitude(), transitQueryResponses.size(),
                        travelTimesOriginPointToOriginStops.get(i), transitQueryResponses.get(i).getTravelTimeMinutes(),
                        travelTimeDestinationStopToDestinationPoint, transitQueryResponse.getNumberOfTransfers(),
                        journeyComputationTimeNs, originStop.getStopId(), originStop.getStopName(),
                        originStop.getStopType(), originStop.getStopTripCount(), originStop.getAverageTransferCost(),
                        originStop.getStopLongitude(), originStop.getStopLatitude(), nodeNearOriginStop.getNodeId(),
                        nodeNearOriginStop.getNodeLongitude(), nodeNearOriginStop.getNodeLatitude(), totalJourneyTime,
                        relativeDiffMinTravelTime);

                querySpecificMultiModalQueryResponsesPub.add(multiModalQueryResponsesPub);
            }
        }
    }

    // Write a "multiModalQueriesResponses.txt" file
    private static void writeMultiModalQueriesResponses(Long queryEntryId, int queryVolume,
                                                        String multiModalQueriesResponsesPubFilePath,
                                                        LinkedHashMap<Long, ArrayList<MultiModalQueryResponsesPub>>
                                                                multiModalQueriesResponsesPub) {
        try {
            // Writer for "multiModalQueriesResponses.csv"
            BufferedWriter multiModalQueriesResponsesPubWriter = new BufferedWriter(new FileWriter(
                    multiModalQueriesResponsesPubFilePath));

            // Set up header array
            multiModalQueriesResponsesPubWriter.write("query_id,origin_point_longitude,origin_point_latitude," +
                    "destination_point_longitude,destination_point_latitude,departure_time_origin_point," +
                    "origin_taz_id,destination_taz_id,travel_time_origin_taz_to_destination_taz_peak," +
                    "travel_time_origin_taz_to_destination_taz_off_peak," +
                    "travel_time_origin_taz_to_destination_taz_night," +
                    "nearest_origin_node_id,nearest_origin_node_longitude,nearest_origin_node_latitude," +
                    "nearest_destination_node_id,nearest_destination_node_longitude," +
                    "nearest_destination_node_latitude," +
                    "destination_stop_id,destination_stop_name,destination_stop_type," +
                    "destination_stop_daily_service_count," +
                    "destination_stop_average_transfer_cost,destination_stop_longitude,destination_stop_latitude," +
                    "destination_stop_nearest_node_id,destination_stop_nearest_node_longitude," +
                    "destination_stop_nearest_node_latitude,count_origin_stops_considered_solution," +
                    "travel_time_origin_to_origin_stop,travel_time_origin_stop_to_destination_stop," +
                    "travel_time_destination_stop_to_destination,number_transfers_in_transit," +
                    "time_elapsed_in_journey_computation_nanoseconds,origin_stop_id,origin_stop_name," +
                    "origin_stop_type,origin_stop_daily_service_count,origin_stop_average_transfer_cost," +
                    "origin_stop_longitude,origin_stop_latitude,origin_stop_nearest_node_id," +
                    "origin_stop_nearest_node_longitude,origin_stop_nearest_node_latitude,total_journey_time," +
                    "relative_difference_to_best_journey_time\n");

            // Write body based on "multiModalQueriesResponses" hashmap
            for(long queryEntryIdentifier = queryEntryId; queryEntryIdentifier < queryEntryId + queryVolume;
                queryEntryIdentifier++) {
                ArrayList<MultiModalQueryResponsesPub> multiModalQueriesResponses = multiModalQueriesResponsesPub.
                        get(queryEntryIdentifier);
                if (multiModalQueriesResponses == null) continue;

                for (MultiModalQueryResponsesPub stopPairSpecificResponse : multiModalQueriesResponses) {
                    multiModalQueriesResponsesPubWriter.write(queryEntryIdentifier + "," +
                            stopPairSpecificResponse.getOriginPointLongitude() + "," +
                            stopPairSpecificResponse.getOriginPointLatitude() + "," +
                            stopPairSpecificResponse.getDestinationPointLongitude() + "," +
                            stopPairSpecificResponse.getDestinationPointLatitude() + "," +
                            stopPairSpecificResponse.getDepartureTimeOriginPoint() + "," +
                            stopPairSpecificResponse.getOriginTazId() + "," +
                            stopPairSpecificResponse.getDestinationTazId() + "," +
                            stopPairSpecificResponse.getTravelTimeOriginTazToDestinationTazPeak() + "," +
                            stopPairSpecificResponse.getTravelTimeOriginTazToDestinationTazOffPeak() + "," +
                            stopPairSpecificResponse.getTravelTimeOriginTazToDestinationTazNight() + "," +
                            stopPairSpecificResponse.getNearestOriginNodeId() + "," +
                            stopPairSpecificResponse.getNearestOriginNodeLongitude() + "," +
                            stopPairSpecificResponse.getNearestOriginNodeLatitude() + "," +
                            stopPairSpecificResponse.getNearestDestinationNodeId() + "," +
                            stopPairSpecificResponse.getNearestDestinationNodeLongitude() + "," +
                            stopPairSpecificResponse.getNearestDestinationNodeLatitude() + "," +
                            stopPairSpecificResponse.getDestinationStopId() + "," +
                            stopPairSpecificResponse.getDestinationStopName() + "," +
                            stopPairSpecificResponse.getDestinationStopType() + "," +
                            stopPairSpecificResponse.getDestinationStopDailyServiceCount() + "," +
                            stopPairSpecificResponse.getDestinationStopAverageTransferCost() + "," +
                            stopPairSpecificResponse.getDestinationStopLongitude() + "," +
                            stopPairSpecificResponse.getDestinationStopLatitude() + "," +
                            stopPairSpecificResponse.getDestinationStopNearestNodeId() + "," +
                            stopPairSpecificResponse.getDestinationStopNearestNodeLongitude() + "," +
                            stopPairSpecificResponse.getDestinationStopNearestNodeLatitude() + "," +
                            stopPairSpecificResponse.getCountOriginStopsConsideredSolution() + "," +
                            stopPairSpecificResponse.getTravelTimeOriginToOriginStop() + "," +
                            stopPairSpecificResponse.getTravelTimeOriginStopToDestinationStop() + "," +
                            stopPairSpecificResponse.getTravelTimeDestinationStopToDestination() + "," +
                            stopPairSpecificResponse.getNumberTransfersInTransit() + "," +
                            stopPairSpecificResponse.getTimeElapsedInJourneyComputationNanoSeconds() + "," +
                            stopPairSpecificResponse.getOriginStopId() + "," +
                            stopPairSpecificResponse.getOriginStopName() + "," +
                            stopPairSpecificResponse.getOriginStopType() + "," +
                            stopPairSpecificResponse.getOriginStopDailyServiceCount() + "," +
                            stopPairSpecificResponse.getOriginStopAverageTransferCost() + "," +
                            stopPairSpecificResponse.getOriginStopLongitude() + "," +
                            stopPairSpecificResponse.getOriginStopLatitude() + "," +
                            stopPairSpecificResponse.getOriginStopNearestNodeId() + "," +
                            stopPairSpecificResponse.getOriginStopNearestNodeLongitude() + "," +
                            stopPairSpecificResponse.getOriginStopNearestNodeLatitude() + "," +
                            stopPairSpecificResponse.getTotalJourneyTime() + "," +
                            stopPairSpecificResponse.getRelativeDifferenceToBestJourneyTime() + "\n");
                }
            }

            multiModalQueriesResponsesPubWriter.flush();
            multiModalQueriesResponsesPubWriter.close();
            System.out.println("Multi-modal queries' responses written to: " + multiModalQueriesResponsesPubFilePath);

        } catch (IOException iOE) {
            System.out.println("Input-output exception. Please check the \"multi-modal queries' responses\" hashmap.");
        }
    }
}