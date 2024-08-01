package src.MultiModalRouter;

// todo literally build a filewriter and be happy with life

// TODO: HEURISTICS CAN BE BASED ON STOPS (STOPTYPE (ASCRIBED VIA ROUTETYPE - BIGGEST CURRENT CANDIDATE), PARENTSTOPPRESENCE), ROUTES (ROUTETYPES, #TRIPS, #STOPS)
// TODO: HEURISTIC DATA CAN BE ASCRIBED IN GTFSDATAMANAGER
// TODO: QUERY PARSING TO PEERFECT INPUT TYPES MUST HAPPEN IN THE CALLER, AND NOT HERE, OR IN THE QUERY CODE (SUCH AS LOCATING STOP AND THEN HOMING IN ON THE STOPIDS)
// TODO REVIEW ALL CODE TO GOD LEVEL PERFECTION BEFORE RUNNING THIS SHIT
// TODO DO TURURURURU DO LIKE A MASSSSSSSIVVVVE (DEEEEEEP, EVERY SINGLE LINE - TAKE 3-4 DAYS) REVIEW BEFORE YOU GET GOINGGGGG
// TODO BEAUTIFY EVERYTHING AFTER A SUCCESSFUL RUN
// TODO BUILD A NULL-SAFETY MADNESS, TYPE SAFETY MADNESS, AND DATA STRUCTURE SAFETY MADNESS
// TODO BLAZE EVERYTHING (SET EVERYTHING ALIGHT) AND THAT HAPPENS FASTEST FROM A 30000 FEET PERSPECTIVE
// TODO: WRITE THE FLASHIEST FANCIEST CODE POSSIBLE

// todo code final review YAYYYYYY
// todo heuristic accuracy mate???

import org.jetbrains.annotations.NotNull;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Caller {

    private static final double AVERAGE_WALKING_SPEED_M_PER_MIN = 85.2; // Translates to 1.4 m/s
    private static final double MAXIMUM_WALKING_DISTANCE_M = 800;
    // Refer to: https://www.emerald.com/insight/content/doi/10.1108/SASBE-07-2017-0031/full/html
    private static final double MAXIMUM_DRIVING_DISTANCE_M = 3_000;
    private static final int GTFS_BUS_ROUTE_TYPE_ID = 3;
    // Parameter for stop-hierarchy heuristic
    private static final int MINIMUM_TRIPS_SERVED_BY_HIGH_FREQUENCY_STOPS = 135;
    // Parameter for stop-frequency heuristic
    private static final int MINUTES_PER_HOUR = 60;
    private static final int MINUTES_PER_DAY = 1440;

    public static void main(String[] args) {
        // GTFS data reader-writer instantiation to read, write, and store data
        long gtfsStartTime = System.nanoTime();
        GTFSDataReaderWriter gtfsDataReaderWriterForRAPTOR = new GTFSDataReaderWriter();
        String gtfsFolderPath = "D:/Documents - Education + Work/Education - TUM/Year 2/Fourth Semester/MasterThesis/" +
                "Data/GTFSDataMunich/Downloaded/AGGTFSData";
        String rAPTORFolderPath = "D:/Documents - Education + Work/Education - TUM/Year 2/Fourth Semester/" +
                "MasterThesis/Data/GTFSDataMunich";
        getRAPTORMaps(gtfsFolderPath, rAPTORFolderPath, gtfsDataReaderWriterForRAPTOR);

        // Get all data for RAPTOR execution
        LinkedHashMap<Integer, Route> routes = gtfsDataReaderWriterForRAPTOR.getRoutes();
        LinkedHashMap<Integer, RouteStop> routeStops = gtfsDataReaderWriterForRAPTOR.getRouteStops();
        LinkedHashMap<Integer, Trip> trips = gtfsDataReaderWriterForRAPTOR.getTrips();
        LinkedHashMap<Integer, StopTime> stopTimes = gtfsDataReaderWriterForRAPTOR.getStopTimes();
        LinkedHashMap<Integer, Stop> stops = gtfsDataReaderWriterForRAPTOR.getStops();
        Stop[] stopsForNNSearches = stops.values().toArray(new Stop[0]);
        LinkedHashMap<Integer, StopRoute> stopRoutes = gtfsDataReaderWriterForRAPTOR.getStopRoutes();
        LinkedHashMap<Integer, Transfer> transfers = gtfsDataReaderWriterForRAPTOR.getTransfers();
        long gtfsEndTime = System.nanoTime();
        long gtfsDataProcessingDuration = gtfsEndTime - gtfsStartTime;

        // OSM data reader-writer instantiation to read, write, and store data
        long osmStartTime = System.nanoTime();
        OSMDataReaderWriter osmDataReaderWriterForDijkstra = new OSMDataReaderWriter();
        String osmOplExtractFilePath = "D:/Documents - Education + Work/Education - TUM/Year 2/Fourth Semester" +
                "/MasterThesis/Data/OSMDataMunich/Downloaded/planet_10.835,47.824_12.172,48.438.osm.opl/" +
                "BBBikeOSMExtract.opl";
        String dijkstraFolderPath = "D:/Documents - Education + Work/Education - TUM/Year 2/Fourth Semester/" +
                "MasterThesis/Data/OSMDataMunich/DijkstraContractionHierarchiesInput";
        getDijkstraMaps(osmOplExtractFilePath, dijkstraFolderPath, osmDataReaderWriterForDijkstra);

        // Get all data for Dijkstra algorithm's execution
        LinkedHashMap<Long, Link> links = osmDataReaderWriterForDijkstra.getLinks();
        LinkedHashMap<Long, Node> nodes = osmDataReaderWriterForDijkstra.getNodes();
        Node[] nodesForNNSearches = nodes.values().toArray(new Node[0]);
        long osmEndTime = System.nanoTime();
        long osmDataProcessingDuration = osmEndTime - osmStartTime;

        // Build KD-Trees for snapping to RAPTOR-relevant transit stops and Dijkstra-relevant network nodes
        long kDStartTime = System.nanoTime();
        KDTreeForStops kDTreeForStops = new KDTreeForStops();
        kDTreeForStops.buildStopBasedKDTree(stopsForNNSearches);
        KDTreeForNodes kDTreeForNodes = new KDTreeForNodes();
        kDTreeForNodes.buildNodeBasedKDTree(nodesForNNSearches);
        long kDEndTime = System.nanoTime();
        long kDTreesBuildDuration = kDEndTime - kDStartTime;

        // Load all multi-modal queries using the generator
        long queryGenStartTime = System.nanoTime();
        MultiModalQueryGenerator multiModalQueryGenerator = new MultiModalQueryGenerator();
        LinkedHashMap<Integer, MultiModalQuery> multiModalQueries = multiModalQueryGenerator.
                generateQueries(10_000);    // Method argument contains number of queries to generate
        long queryGenEndTime = System.nanoTime();
        long queryGenerationDuration = queryGenEndTime - queryGenStartTime;

        // Initialize counters, maps, and timekeepers
        long queriesProcessingStartTime = System.nanoTime();
        LinkedHashMap<Integer, MultiModalQueryResponses> multiModalQueriesResponses = new LinkedHashMap<>();

        // Iterate through all multi-modal queries
        for (HashMap.Entry<Integer, MultiModalQuery> multiModalQueryEntry : multiModalQueries.entrySet()) {
            /**
             * Parse query parameters and gauge details of nearest nodes
             */
            // Instantiate an empty multi-modal query response and add it to all hashmaps
            MultiModalQueryResponses multiModalQueryResponses = new MultiModalQueryResponses();

            // Extract query parameters
            double originPointLongitude = multiModalQueryEntry.getValue().getOriginLongitude();
            double originPointLatitude = multiModalQueryEntry.getValue().getOriginLatitude();
            double destinationPointLongitude = multiModalQueryEntry.getValue().getDestinationLongitude();
            double destinationPointLatitude = multiModalQueryEntry.getValue().getDestinationLatitude();
            int originPointDepartureTime = multiModalQueryEntry.getValue().getDepartureTime();

            multiModalQueryResponses.setOriginPointLongitude(originPointLongitude);
            multiModalQueryResponses.setOriginPointLatitude(originPointLatitude);
            multiModalQueryResponses.setDestinationPointLongitude(destinationPointLongitude);
            multiModalQueryResponses.setDestinationPointLatitude(destinationPointLatitude);
            multiModalQueryResponses.setDepartureTimeOriginPointInt(originPointDepartureTime);

            // Find nearest nodes for the origin and destination points, as well as their parameters
            Node originNode = kDTreeForNodes.findNearestNode(originPointLongitude, originPointLatitude);
            Node destinationNode = kDTreeForNodes.findNearestNode(destinationPointLongitude, destinationPointLatitude);
            long originNodeId = originNode.getNodeId();
            long destinationNodeId = destinationNode.getNodeId();
            double originNodeLongitude = originNode.getNodeLongitude();
            double originNodeLatitude = originNode.getNodeLatitude();
            double destinationNodeLongitude = destinationNode.getNodeLongitude();
            double destinationNodeLatitude = destinationNode.getNodeLatitude();

            multiModalQueryResponses.setNearestOriginNodeId(originNodeId);
            multiModalQueryResponses.setNearestDestinationNodeId(destinationNodeId);

            // Determine snapping cost - minutes required to get from one point to another (aerial distance) on foot
            double costOriginToOriginNode = originNode.equiRectangularDistanceTo(originPointLongitude,
                    originNodeLatitude) / AVERAGE_WALKING_SPEED_M_PER_MIN;
            double costDestinationNodeToDestination = destinationNode.
                    equiRectangularDistanceTo(destinationPointLongitude, destinationPointLatitude) /
                    AVERAGE_WALKING_SPEED_M_PER_MIN;

            /**
             * Set up nearest neighbor stop lists, and execute routing algorithms within such stop lists
             */
            // Stop lists containing all types of transit stops in a node's vicinity
            ArrayList<Stop> originNodeStops = kDTreeForStops.findStopsWithinDoughnut(originNodeLongitude,
                    originNodeLatitude, MAXIMUM_WALKING_DISTANCE_M, MAXIMUM_DRIVING_DISTANCE_M);
            ArrayList<Stop> destinationNodeStops = kDTreeForStops.findStopsWithinDoughnut(destinationNodeLongitude,
                    destinationNodeLatitude, 0, MAXIMUM_WALKING_DISTANCE_M);

            if ((originNodeStops != null) && (originNodeStops.size() != 0) && (destinationNodeStops != null) &&
                    (destinationNodeStops.size() != 0)) {

                ArrayList<Double> travelTimesOriginToOriginStops = findTravelTimesFromOriginToOriginStops(originNodeId,
                        costOriginToOriginNode, kDTreeForNodes, originNodeStops, nodes, links);
                ArrayList<Double> travelTimesDestinationStopsToDestination =
                        findTravelTimesFromDestinationStopsToDestination(destinationNodeId,
                                costDestinationNodeToDestination, kDTreeForNodes, destinationNodeStops, nodes, links);

                // Execute RAPTOR runs (arrival times at each stop are defined within the method call)
                determineLeastTotalTravelTimeSFRAPTOR(originPointDepartureTime, originNodeStops, destinationNodeStops,
                        travelTimesOriginToOriginStops, travelTimesDestinationStopsToDestination,
                        multiModalQueryResponses, routeStops, stopTimes, stops, stopRoutes, transfers);
            }

            // Stop lists containing all types of transit stops except bus stops (stop hierarchy heuristic)
            if ((originNodeStops.size() != 0) && (destinationNodeStops.size() != 0)) {
                ArrayList<Stop> originNodeNonBusStops = new ArrayList<>();
                for (Stop stop : originNodeStops) {
                    if (stop.getStopType() != GTFS_BUS_ROUTE_TYPE_ID) {
                        originNodeNonBusStops.add(stop);
                    }
                }

                ArrayList<Stop> destinationNodeNonBusStops = new ArrayList<>();
                for (Stop stop : destinationNodeStops) {
                    if (stop.getStopType() != GTFS_BUS_ROUTE_TYPE_ID) {
                        destinationNodeNonBusStops.add(stop);
                    }
                }

                if ((originNodeNonBusStops.size() != 0) && (destinationNodeNonBusStops.size() != 0)) {
                    originStopLists.add(originNodeNonBusStops);
                    destinationStopLists.add(destinationNodeNonBusStops);
                }
            }

            // Stop lists containing transit stops exceeding a certain number of served trips (stop frequency heuristic)
            if ((originNodeStops.size() != 0) && (destinationNodeStops.size() != 0)) {
                ArrayList<Stop> originNodeHighFrequencyStops = new ArrayList<>();
                for (Stop stop : originNodeStops) {
                    if (stop.getStopTripCount() >= MINIMUM_TRIPS_SERVED_BY_HIGH_FREQUENCY_STOPS) {
                        originNodeHighFrequencyStops.add(stop);
                    }
                }

                ArrayList<Stop> destinationNodeHighFrequencyStops = new ArrayList<>();
                for (Stop stop : destinationNodeStops) {
                    if (stop.getStopTripCount() >= MINIMUM_TRIPS_SERVED_BY_HIGH_FREQUENCY_STOPS) {
                        destinationNodeHighFrequencyStops.add(stop);
                    }
                }

                if ((originNodeHighFrequencyStops.size() != 0) && (destinationNodeHighFrequencyStops.size() != 0)) {
                    originStopLists.add(originNodeHighFrequencyStops);
                    destinationStopLists.add(destinationNodeHighFrequencyStops);
                }
            }

        }


        // Iterate over all types of nearest-stop lists
        for (int listTypeIndex = 0; listTypeIndex < originStopLists.size(); listTypeIndex++) {
            ArrayList<Stop> originStops = originStopLists.get(listTypeIndex);
            ArrayList<Stop> destinationStops = destinationStopLists.get(listTypeIndex);

            if ((originStops == null) || (destinationStops == null)) {
                System.out.println("Multi-modal routing query #" + routingQueryCount + "\n" +
                        "Origin coordinates: (" + originLatitude + ", " + originLongitude + ")\n" +
                        "Destination coordinates: (" + destinationLatitude + ", " + destinationLongitude + "\n" +
                        "Departure time: " + ((originDepartureTime % MINUTES_PER_DAY) / MINUTES_PER_HOUR) + ":" +
                        ((originDepartureTime % MINUTES_PER_DAY) % MINUTES_PER_HOUR) + "\n" +
                        "Transit stops could not be found near origin and/ or destination nodes.");
                continue;
            }

            long queryEndTime = System.nanoTime();
            long queryProcessingDuration = queryEndTime - queryStartTime;
            cumulativeQueryProcessingDuration += queryProcessingDuration;

            int accuracyMarker = 0;
            // 0 indicates incorrect response from routing process and stop list combination
            // if (routingQueryCount)

            // Print result
            System.out.println("Solution found for multi-modal routing query #" + routingQueryCount);
            MultiModalQueryResponses multiModalQueryResponse = new MultiModalQueryResponses(listTypeIndex,
                    originLongitude, originLatitude, destinationLongitude, destinationLatitude,
                    (((originDepartureTime % MINUTES_PER_DAY) / MINUTES_PER_HOUR) + ":" +
                            ((originDepartureTime % MINUTES_PER_DAY) % MINUTES_PER_HOUR)), originStops.size(),
                    destinationStops.size(), selectedOriginStopName, selectedOriginStopId,
                    selectedDestinationStopName, selectedDestinationStopId, originToOriginStopDuration,
                    totalTransitDuration, destinationStopToDestinationDuration, queryProcessingDuration, );
            multiModalQueriesResponses.put(routingQueryCount, multiModalQueryResponse);
        }

        long queriesProcessingEndTime = System.nanoTime();
        long queriesProcessingDuration = queriesProcessingEndTime - queriesProcessingStartTime;

        writeMultiModalResponses(multiModalQueriesResponsesFilePath, multiModalQueriesResponses);
        System.out.println("Times elapsed (in nanoseconds) for:" + "\n" +
                "1. Preprocessing GTFS data: " + (gtfsEndTime - gtfsStartTime) + "\n" +
                "2. Preprocessing OSM-OPL data: " + (osmEndTime - osmStartTime) + "\n" +
                "3. Building KD Trees for Stops and Nodes: " + (kDEndTime - kDStartTime) + "\n" +
                "4. Processing " + routingQueryCount + " queries: " + cumulativeQueryProcessingDuration);
    }

    // Initialize RAPTOR-relevant datasets
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

    // Get Dijkstra-relevant datasets ready
    private static void getDijkstraMaps(String osmOplExtractFilePath,
                                String dijkstraFolderPath,
                                @NotNull OSMDataReaderWriter osmDataReaderWriterForDijkstra) {
        // Ready filepath arguments to write
        String dijkstraLinksFilePath = dijkstraFolderPath + "/dijkstraLinks.txt";
        String dijkstraNodesFilePath = dijkstraFolderPath + "/dijkstraNodes.txt";

        // Read and manage data for Dijkstra operations
        osmDataReaderWriterForDijkstra.readAndFilterOsmLinks(osmOplExtractFilePath);
        osmDataReaderWriterForDijkstra.removeCircularLinks();
        osmDataReaderWriterForDijkstra.readAndFilterOsmNodes(osmOplExtractFilePath);
        osmDataReaderWriterForDijkstra.associateLinksWithNode();
        osmDataReaderWriterForDijkstra.calculateLinkTravelTimesMin();
        // osmDataReaderWriterForDijkstra.contractNodesAndBuildShortcuts();    // This step is optional

        // Write out data used for the Dijkstra algorithm
        osmDataReaderWriterForDijkstra.writeDijkstraLinks(dijkstraLinksFilePath);
        osmDataReaderWriterForDijkstra.writeDijkstraNodes(dijkstraNodesFilePath);
    }

    // Find travel times incorporating Dijkstra runs and snapping costs at both ends of such runs
    private static ArrayList<Double> findTravelTimesFromOriginToOriginStops(long originNodeId,
                                                                    double costOriginToOriginNode,
                                                                    KDTreeForNodes kDTreeForNodes,
                                                                    ArrayList<Stop> stopsNearOrigin,
                                                                    LinkedHashMap<Long, Node> nodes,
                                                                    LinkedHashMap<Long, Link> links) {
        ArrayList<Double> travelTimesOriginToOriginStops = new ArrayList<>();

        for (Stop stopNearOriginNode : stopsNearOrigin) {
            Node nearestNodeOfOriginStop = kDTreeForNodes.findNearestNode(stopNearOriginNode.getStopLongitude(),
                    stopNearOriginNode.getStopLatitude());
            double costOriginStopToNearestNode = nearestNodeOfOriginStop.equiRectangularDistanceTo(
                    stopNearOriginNode.getStopLongitude(), stopNearOriginNode.getStopLatitude()) /
                    AVERAGE_WALKING_SPEED_M_PER_MIN;

            DijkstraBasedRouter dijkstraBasedRouter = new DijkstraBasedRouter();
            double dijkstraTravelTime = dijkstraBasedRouter.findShortestDrivingPath(originNodeId,
                    nearestNodeOfOriginStop.getNodeId(), nodes, links);

            travelTimesOriginToOriginStops.add(costOriginToOriginNode + dijkstraTravelTime +
                    costOriginStopToNearestNode);
        }

        return travelTimesOriginToOriginStops;
    }

    private static ArrayList<Double> findTravelTimesFromDestinationStopsToDestination
            (long destinationNodeId,
             double costDestinationNodeToDestination,
             KDTreeForNodes kDTreeForNodes,
             ArrayList<Stop> stopsNearDestination,
             LinkedHashMap<Long, Node> nodes,
             LinkedHashMap<Long, Link> links) {
        ArrayList<Double> travelTimesDestinationStopsToDestination = new ArrayList<>();

        for (Stop stopNearDestinationNode : stopsNearDestination) {
            Node nearestNodeOfDestinationStop = kDTreeForNodes.findNearestNode(stopNearDestinationNode.
                    getStopLongitude(), stopNearDestinationNode.getStopLatitude());
            double costDestinationStopToNearestNode = nearestNodeOfDestinationStop.equiRectangularDistanceTo(
                    stopNearDestinationNode.getStopLongitude(), stopNearDestinationNode.getStopLatitude()) /
                    AVERAGE_WALKING_SPEED_M_PER_MIN;

            DijkstraBasedRouter dijkstraBasedRouter = new DijkstraBasedRouter();
            double dijkstraTravelTime = dijkstraBasedRouter.findShortestDrivingPath(destinationNodeId,
                    nearestNodeOfDestinationStop.getNodeId(), nodes, links);

            travelTimesDestinationStopsToDestination.add(costDestinationNodeToDestination + dijkstraTravelTime +
                    costDestinationStopToNearestNode);
        }

        return travelTimesDestinationStopsToDestination;
    }

    //
    private static void determineLeastTotalTravelTimeExactRAPTOR(int originPointDepartureTime,
                                                         ArrayList<Stop> originStopList,
                                                         ArrayList<Stop> destinationStopList,
                                                         ArrayList<Double> travelTimesOriginToOriginStops,
                                                         ArrayList<Double> travelTimesDestinationStopsToDestination,
                                                         MultiModalQueryResponses multiModalQueryResponses,
                                                         LinkedHashMap<Integer, RouteStop> routeStops,
                                                         LinkedHashMap<Integer, StopTime> stopTimes,
                                                         LinkedHashMap<Integer, Stop> stops,
                                                         LinkedHashMap<Integer, StopRoute> stopRoutes,
                                                         LinkedHashMap<Integer, Transfer> transfers) {

        double leastTotalTravelTime = Double.MAX_VALUE;
        int originStopIndexLeastTotalTravelTimeExact = -1;
        int destinationStopIndexLeastTotalTravelTimeExact = -1;

        for (int originStopCounter = 0; originStopCounter < originStopList.size(); originStopCounter++) {
            for (int destinationStopCounter = 0; destinationStopCounter < destinationStopList.size();
                 destinationStopCounter++) {
                int originStopId = originStopList.get(originStopCounter).getStopId();
                int destinationStopId = destinationStopList.get(destinationStopCounter).getStopId();

                double totalTravelTimeForStopPair = runRAPTOR(originPointDepartureTime, originStopId, destinationStopId,
                        travelTimesOriginToOriginStops.get(originStopCounter),
                        travelTimesDestinationStopsToDestination.get(destinationStopCounter),
                        routeStops, stopTimes, stops, stopRoutes, transfers);

                if (totalTravelTimeForStopPair != -1) {
                    if (totalTravelTimeForStopPair < leastTotalTravelTime) {
                        leastTotalTravelTime = totalTravelTimeForStopPair;
                        originStopIndexLeastTotalTravelTimeExact = originStopCounter;
                        destinationStopIndexLeastTotalTravelTimeExact = destinationStopCounter;
                    }
                }
            }
        }

        if (leastTotalTravelTime != Double.MAX_VALUE) {
            multiModalQueryResponses.setCountOriginStopsConsideredExactSolution(originStopList.size());
            multiModalQueryResponses.setCountDestinationStopsConsideredExactSolution(destinationStopList.size());
            multiModalQueryResponses.setOriginStopIdExactSolution(originStopList.
                    get(originStopIndexLeastTotalTravelTimeExact).getStopId());
            multiModalQueryResponses.setOriginStopNameExactSolution(originStopList.
                    get(originStopIndexLeastTotalTravelTimeExact).getStopName());
            multiModalQueryResponses.setDestinationStopIdExactSolution(destinationStopList.
                    get(destinationStopIndexLeastTotalTravelTimeExact).getStopId());
            multiModalQueryResponses.setDestinationStopNameExactSolution(destinationStopList.
                    get(destinationStopIndexLeastTotalTravelTimeExact).getStopName());
            multiModalQueryResponses.setTravelTimeOriginToOriginStopExactSolution(travelTimesOriginToOriginStops.
                    get(originStopIndexLeastTotalTravelTimeExact));
            multiModalQueryResponses.setTravelTimeDestinationStopToDestinationExactSolution(
                    travelTimesDestinationStopsToDestination.get(destinationStopIndexLeastTotalTravelTimeExact));
            multiModalQueryResponses.setTravelTimeOriginStopToDestinationStopExactSolution(leastTotalTravelTime -
                    travelTimesOriginToOriginStops.get(originStopIndexLeastTotalTravelTimeExact) -
                    travelTimesDestinationStopsToDestination.get(destinationStopIndexLeastTotalTravelTimeExact));
            multiModalQueryResponses.setTotalTravelTimeExactSolution(leastTotalTravelTime);
            multiModalQueryResponses.setEarliestArrivalTimeExactSolution(
                    (int) (((originPointDepartureTime + leastTotalTravelTime) % MINUTES_PER_DAY) / MINUTES_PER_HOUR) +
                            ":" + (int) (((originPointDepartureTime + leastTotalTravelTime) % MINUTES_PER_DAY) %
                            MINUTES_PER_HOUR));
        }
    }

    private static void determineLeastTotalTravelTimeSHRAPTOR(int originPointDepartureTime,
                                                      ArrayList<Stop> originStopList,
                                                      ArrayList<Stop> destinationStopList,
                                                      ArrayList<Double> travelTimesOriginToOriginStops,
                                                      ArrayList<Double> travelTimesDestinationStopsToDestination,
                                                      MultiModalQueryResponses multiModalQueryResponses,
                                                      LinkedHashMap<Integer, RouteStop> routeStops,
                                                      LinkedHashMap<Integer, StopTime> stopTimes,
                                                      LinkedHashMap<Integer, Stop> stops,
                                                      LinkedHashMap<Integer, StopRoute> stopRoutes,
                                                      LinkedHashMap<Integer, Transfer> transfers) {

        double leastTotalTravelTime = Double.MAX_VALUE;
        int originStopIndexLeastTotalTravelTimeSH = -1;
        int destinationStopIndexLeastTotalTravelTimeSH = -1;

        for (int originStopCounter = 0; originStopCounter < originStopList.size(); originStopCounter++) {
            for (int destinationStopCounter = 0; destinationStopCounter < destinationStopList.size();
                 destinationStopCounter++) {
                int originStopId = originStopList.get(originStopCounter).getStopId();
                int destinationStopId = destinationStopList.get(destinationStopCounter).getStopId();

                double totalTravelTimeForStopPair = runRAPTOR(originPointDepartureTime, originStopId, destinationStopId,
                        travelTimesOriginToOriginStops.get(originStopCounter),
                        travelTimesDestinationStopsToDestination.get(destinationStopCounter),
                        routeStops, stopTimes, stops, stopRoutes, transfers);

                if (totalTravelTimeForStopPair != -1) {
                    if (totalTravelTimeForStopPair < leastTotalTravelTime) {
                        leastTotalTravelTime = totalTravelTimeForStopPair;
                        originStopIndexLeastTotalTravelTimeSH = originStopCounter;
                        destinationStopIndexLeastTotalTravelTimeSH = destinationStopCounter;
                    }
                }
            }
        }

        if (leastTotalTravelTime != Double.MAX_VALUE) {
            multiModalQueryResponses.setCountOriginStopsConsideredSHSolution(originStopList.size());
            multiModalQueryResponses.setCountDestinationStopsConsideredSHSolution(destinationStopList.size());
            multiModalQueryResponses.setOriginStopIdSHSolution(originStopList.
                    get(originStopIndexLeastTotalTravelTimeSH).getStopId());
            multiModalQueryResponses.setOriginStopNameSHSolution(originStopList.
                    get(originStopIndexLeastTotalTravelTimeSH).getStopName());
            multiModalQueryResponses.setDestinationStopIdSHSolution(destinationStopList.
                    get(destinationStopIndexLeastTotalTravelTimeSH).getStopId());
            multiModalQueryResponses.setDestinationStopNameSHSolution(destinationStopList.
                    get(destinationStopIndexLeastTotalTravelTimeSH).getStopName());
            multiModalQueryResponses.setTravelTimeOriginToOriginStopSHSolution(travelTimesOriginToOriginStops.
                    get(originStopIndexLeastTotalTravelTimeSH));
            multiModalQueryResponses.setTravelTimeDestinationStopToDestinationSHSolution(
                    travelTimesDestinationStopsToDestination.get(destinationStopIndexLeastTotalTravelTimeSH));
            multiModalQueryResponses.setTravelTimeOriginStopToDestinationStopSHSolution(leastTotalTravelTime -
                    travelTimesOriginToOriginStops.get(originStopIndexLeastTotalTravelTimeSH) -
                    travelTimesDestinationStopsToDestination.get(destinationStopIndexLeastTotalTravelTimeSH));
            multiModalQueryResponses.setTotalTravelTimeSHSolution(leastTotalTravelTime);
            multiModalQueryResponses.setEarliestArrivalTimeSHSolution(
                    (int) (((originPointDepartureTime + leastTotalTravelTime) % MINUTES_PER_DAY) / MINUTES_PER_HOUR) +
                            ":" + (int) (((originPointDepartureTime + leastTotalTravelTime) % MINUTES_PER_DAY) %
                            MINUTES_PER_HOUR));
        }
    }

    private static void determineLeastTotalTravelTimeSFRAPTOR(int originPointDepartureTime,
                                                      ArrayList<Stop> originStopList,
                                                      ArrayList<Stop> destinationStopList,
                                                      ArrayList<Double> travelTimesOriginToOriginStops,
                                                      ArrayList<Double> travelTimesDestinationStopsToDestination,
                                                      MultiModalQueryResponses multiModalQueryResponses,
                                                      LinkedHashMap<Integer, RouteStop> routeStops,
                                                      LinkedHashMap<Integer, StopTime> stopTimes,
                                                      LinkedHashMap<Integer, Stop> stops,
                                                      LinkedHashMap<Integer, StopRoute> stopRoutes,
                                                      LinkedHashMap<Integer, Transfer> transfers) {

        double leastTotalTravelTime = Double.MAX_VALUE;
        int originStopIndexLeastTotalTravelTimeSF = -1;
        int destinationStopIndexLeastTotalTravelTimeSF = -1;

        for (int originStopCounter = 0; originStopCounter < originStopList.size(); originStopCounter++) {
            for (int destinationStopCounter = 0; destinationStopCounter < destinationStopList.size();
                 destinationStopCounter++) {
                int originStopId = originStopList.get(originStopCounter).getStopId();
                int destinationStopId = destinationStopList.get(destinationStopCounter).getStopId();

                double totalTravelTimeForStopPair = runRAPTOR(originPointDepartureTime, originStopId, destinationStopId,
                        travelTimesOriginToOriginStops.get(originStopCounter),
                        travelTimesDestinationStopsToDestination.get(destinationStopCounter),
                        routeStops, stopTimes, stops, stopRoutes, transfers);

                if (totalTravelTimeForStopPair != -1) {
                    if (totalTravelTimeForStopPair < leastTotalTravelTime) {
                        leastTotalTravelTime = totalTravelTimeForStopPair;
                        originStopIndexLeastTotalTravelTimeSF = originStopCounter;
                        destinationStopIndexLeastTotalTravelTimeSF = destinationStopCounter;
                    }
                }
            }
        }

        if (leastTotalTravelTime != Double.MAX_VALUE) {
            multiModalQueryResponses.setCountOriginStopsConsideredSFSolution(originStopList.size());
            multiModalQueryResponses.setCountDestinationStopsConsideredSFSolution(destinationStopList.size());
            multiModalQueryResponses.setOriginStopIdSFSolution(originStopList.
                    get(originStopIndexLeastTotalTravelTimeSF).getStopId());
            multiModalQueryResponses.setOriginStopNameSFSolution(originStopList.
                    get(originStopIndexLeastTotalTravelTimeSF).getStopName());
            multiModalQueryResponses.setDestinationStopIdSFSolution(destinationStopList.
                    get(destinationStopIndexLeastTotalTravelTimeSF).getStopId());
            multiModalQueryResponses.setDestinationStopNameSFSolution(destinationStopList.
                    get(destinationStopIndexLeastTotalTravelTimeSF).getStopName());
            multiModalQueryResponses.setTravelTimeOriginToOriginStopSFSolution(travelTimesOriginToOriginStops.
                    get(originStopIndexLeastTotalTravelTimeSF));
            multiModalQueryResponses.setTravelTimeDestinationStopToDestinationSFSolution(
                    travelTimesDestinationStopsToDestination.get(destinationStopIndexLeastTotalTravelTimeSF));
            multiModalQueryResponses.setTravelTimeOriginStopToDestinationStopSFSolution(leastTotalTravelTime -
                    travelTimesOriginToOriginStops.get(originStopIndexLeastTotalTravelTimeSF) -
                    travelTimesDestinationStopsToDestination.get(destinationStopIndexLeastTotalTravelTimeSF));
            multiModalQueryResponses.setTotalTravelTimeSFSolution(leastTotalTravelTime);
            multiModalQueryResponses.setEarliestArrivalTimeSFSolution(
                    (int) (((originPointDepartureTime + leastTotalTravelTime) % MINUTES_PER_DAY) / MINUTES_PER_HOUR) +
                            ":" + (int) (((originPointDepartureTime + leastTotalTravelTime) % MINUTES_PER_DAY) %
                            MINUTES_PER_HOUR));
        }
    }

    // Run RAPTOR given an origin stop-destination stop pair
    private static double runRAPTOR(int originPointDepartureTime, int originStopId, int destinationStopId,
                                    double travelTimeOriginToOriginStop, double travelTimeDestinationStopToDestination,
                                    LinkedHashMap<Integer, RouteStop> routeStops,
                                    LinkedHashMap<Integer, StopTime> stopTimes, LinkedHashMap<Integer, Stop> stops,
                                    LinkedHashMap<Integer, StopRoute> stopRoutes,
                                    LinkedHashMap<Integer, Transfer> transfers) {
        double totalTravelTime = -1;

        RAPTOR rAPTOR = new RAPTOR();
        double totalTransitDuration = rAPTOR.findShortestTransitPath(originStopId, destinationStopId,
                (originPointDepartureTime + travelTimeOriginToOriginStop),
                routeStops, stopTimes, stops, stopRoutes, transfers).getTravelTimeMinutes();

        if (totalTransitDuration != -1) {
            totalTravelTime = travelTimeOriginToOriginStop + totalTransitDuration +
                    travelTimeDestinationStopToDestination;
        }

        return totalTravelTime;
    }

    // Write out responses to multi-modal queries in a .txt file
    private static void writeMultiModalResponses(String multiModalQueriesResponsesFilePath,
                                         LinkedHashMap<Integer, MultiModalQueryResponses>
                                                 multiModalQueriesResponses) {
        try {
            // Writer for "multiModalQueriesResponses.txt"
            BufferedWriter multiModalQueriesResponsesWriter = new BufferedWriter(new FileWriter
                    (multiModalQueriesResponsesFilePath));

            // Set up header array
            multiModalQueriesResponsesWriter.write("RoutingQueryId,StopListType,OriginLongitude," +
                    "OriginLatitude,DestinationLongitude,DestinationLatitude,DepartureTime,NumberOriginStops," +
                    "NumberDestinationStops,OriginStopName,OriginStopId,DestinationStopName,DestinationStopId," +
                    "TravelTimeOriginToOriginStop,TravelTimeOriginStopToDestinationStop," +
                    "TravelTimeDestinationStopToDestination,TimeElapsedQueryProcessing,TotalTravelTime,AccuracyMarker");

            // Write body based on "multiModalQueriesResponses" hashmap
            for (HashMap.Entry<Integer, MultiModalQueryResponses> multiModalQueryResponseEntry :
                    multiModalQueriesResponses.entrySet()) {
                MultiModalQueryResponses multiModalQueryResponse = multiModalQueryResponseEntry.getValue();
                int routingQueryId = multiModalQueryResponseEntry.getKey();
                int listTypeIndex = multiModalQueryResponse.getListTypeIndex();
                double originLongitude = multiModalQueryResponse.getOriginLongitude();
                double originLatitude = multiModalQueryResponse.getOriginLatitude();
                double destinationLongitude = multiModalQueryResponse.getDestinationLongitude();
                double destinationLatitude = multiModalQueryResponse.getDestinationLatitude();
                String departureTime = multiModalQueryResponse.getDepartureTime();
                int numberOriginStops = multiModalQueryResponse.getNumberOriginStops();
                int numberDestinationStops = multiModalQueryResponse.getNumberDestinationStops();
                String originStopName = multiModalQueryResponse.getOriginStopName();
                int originStopId = multiModalQueryResponse.getOriginStopId();
                String destinationStopName = multiModalQueryResponse.getDestinationStopName();
                int destinationStopId = multiModalQueryResponse.getDestinationStopId();
                double travelTimeOriginToOriginStop = multiModalQueryResponse.getTravelTimeOriginToOriginStop();
                double travelTimeOriginStopToDestinationStop = multiModalQueryResponse.
                        getTravelTimeOriginStopToDestinationStop();
                double travelTimeDestinationStopToDestination = multiModalQueryResponse.
                        getTravelTimeDestinationStopToDestination();
                double timeElapsedQueryProcessing = multiModalQueryResponse.getTimeElapsedQueryProcessing();
                double totalTravelTime = travelTimeOriginToOriginStop + travelTimeOriginStopToDestinationStop +
                        travelTimeDestinationStopToDestination;
                int accuracyMarker = multiModalQueryResponse.getAccuracyMarker();

                multiModalQueriesResponsesWriter.write(routingQueryId + "," + ((listTypeIndex == 0) ?
                        "List of all transit stops" : ((listTypeIndex == 1) ? "List of non-bus transit stops" :
                        "List of high-frequency transit stops")) + "," + originLongitude + "," + originLatitude + "," +
                        destinationLongitude + "," + destinationLatitude + "," + departureTime + "," +
                        numberOriginStops + "," + numberDestinationStops + "," + originStopName + "," + originStopId +
                        "," + destinationStopName + "," + destinationStopId + "," + travelTimeOriginToOriginStop + "," +
                        travelTimeOriginStopToDestinationStop + "," + travelTimeDestinationStopToDestination + "," +
                        timeElapsedQueryProcessing + "," + totalTravelTime + "," + accuracyMarker + "\n");
            }

        } catch (IOException iOE) {
            System.out.println("Input-output exception. Please check the \"multiModalQueriesResponses\" hashmap.");
        }
    }
}
