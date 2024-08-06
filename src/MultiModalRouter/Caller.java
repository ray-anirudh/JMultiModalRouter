package src.MultiModalRouter;
// GTFS: General Transit Feed Specification
// RAPTOR: Round-based Public Transit Router (Delling et. al., 2015)
// OSM: OpenStreetMap
// OPL: Object-Per-Line (format)

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
import java.util.*;

public class Caller {
    private static final int NUMBER_MULTI_MODAL_QUERIES = 100_000;
    private static final double AVERAGE_WALKING_SPEED_M_PER_MIN = 85.2;     // Translates to 1.4 m/s
    private static final double MAXIMUM_WALKING_DISTANCE_M = 1_500;
    // Refer to: https://www.emerald.com/insight/content/doi/10.1108/SASBE-07-2017-0031/full/html
    private static final double MINIMUM_DRIVING_DISTANCE_M = 2_000;
    // Refer to: https://www.emerald.com/insight/content/doi/10.1108/SASBE-07-2017-0031/full/html
    private static final double MAXIMUM_DRIVING_DISTANCE_M = 10_000;
    private static final int GTFS_BUS_ROUTE_TYPE_ID = 3;
    // Parameter for stop-hierarchy heuristic
    private static final int MINIMUM_TRIPS_SERVED_BY_HIGH_FREQUENCY_ORIGIN_STOPS = 135;     // todo manage mate
    private static final int MINIMUM_TRIPS_SERVED_BY_HIGH_FREQUENCY_DESTINATION_STOPS = 135;    // todo manage mate
    // Parameters for stop-frequency heuristic
    private static final int MINUTES_PER_HOUR = 60;
    private static final int MINUTES_PER_DAY = 1440;
    private static final long NANOSECONDS_PER_MINUTE = 60_000_000_000L;

    public static void main(String[] args) {
        /** OSM data reader-writer instantiation to read, write, and store data
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
                "OSM-OPL data processed in " + String.format("%.2f",
                osmDataProcessingDuration / NANOSECONDS_PER_MINUTE) + " minutes.");

        /** GTFS data reader-writer instantiation to read, write, and store data
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
                "Number of routeStop objects: "  + routeStops.size() + "\n" +
                "Number of stopTime objects: "  + stopTimes.size() + "\n" +
                "Number of stops: "  + stops.size() + "\n" +
                "Number of stopRoute objects: "  + stopRoutes.size() + "\n" +
                "Number of transfers: "  + transfers.size() + "\n" +
                "GTFS data processed in " + String.format("%.2f", gtfsDataProcessingDuration / NANOSECONDS_PER_MINUTE)
                + " minutes.");

        double kDTreesBuildDuration = (double) ((kDStopEndTime - kDStopStartTime) + (kDNodeEndTime - kDNodeStartTime));
        System.out.println("\n" +
                "KD-Trees for searching nearest nodes and stops built in " + String.format("%.2f",
                kDTreesBuildDuration / NANOSECONDS_PER_MINUTE) + " minutes.");

        // Load and write all multi-modal queries using the generator
        long queryGenStartTime = System.nanoTime();
        String multiModalQueriesFilePath = "D:/Documents - Education + Work/Education - TUM/Year 2/Fourth Semester/" +
                "MasterThesis/Results/MultiModalQueriesMap/multiModalQueries.txt";
        MultiModalQueryGenerator multiModalQueryGenerator = new MultiModalQueryGenerator();
        LinkedHashMap<Integer, MultiModalQuery> multiModalQueries = multiModalQueryGenerator.
                generateQueries(NUMBER_MULTI_MODAL_QUERIES);   // Method argument contains number of queries to generate
        multiModalQueryGenerator.writeMultiModalQueries(multiModalQueriesFilePath);
        long queryGenEndTime = System.nanoTime();
        long queryGenerationDuration = queryGenEndTime - queryGenStartTime;

        // Initialize counters, maps, accuracy counters, and timekeepers
        long queriesProcessingStartTime = System.nanoTime();
        LinkedHashMap<Integer, MultiModalQueryResponses> multiModalQueriesResponses = new LinkedHashMap<>();
        int nonNullExactResponseCounter = 0;
        int sHHeuristicCorrectOutputCounter = 0;
        int sFHeuristicCorrectOutputCounter = 0;

        // Iterate through all multi-modal queries
        for (HashMap.Entry<Integer, MultiModalQuery> multiModalQueryEntry : multiModalQueries.entrySet()) {
            // Parse query parameters and gauge details of nearest nodes
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

            // Set up nearest neighbor stop lists, and execute routing algorithms within such stop lists
            // Stop lists containing all types of transit stops in a node's vicinity
            long exactQueryProcessingStartTime = System.nanoTime();
            ArrayList<Stop> originNodeStops = kDTreeForStops.findStopsWithinDoughnut(originNodeLongitude,
                    originNodeLatitude, MINIMUM_DRIVING_DISTANCE_M, MAXIMUM_DRIVING_DISTANCE_M);    // todo change argument
            ArrayList<Stop> destinationNodeStops = kDTreeForStops.findStopsWithinDoughnut(destinationNodeLongitude,
                    destinationNodeLatitude, 0, MAXIMUM_WALKING_DISTANCE_M);    // todo change argument

            // Filtering unique stops near the origin node
            HashSet<String> uniqueOriginStops = new HashSet<>();
            Iterator<Stop> originStopIterator = originNodeStops.iterator();
            while (originStopIterator.hasNext()) {
                Stop originStop = originStopIterator.next();
                String stopKey = originStop.getStopName() + "-" + originStop.getStopType();
                if (uniqueOriginStops.contains(stopKey)) {
                    originStopIterator.remove();
                } else {
                    uniqueOriginStops.add(stopKey);
                }
            }

            // Filtering unique stops near the destination node
            Set<String> uniqueDestinationStops = new HashSet<>();
            Iterator<Stop> destinationStopIterator = destinationNodeStops.iterator();
            while (destinationStopIterator.hasNext()) {
                Stop destinationStop = destinationStopIterator.next();
                String stopKey = destinationStop.getStopName() + "-" + destinationStop.getStopType();
                if (uniqueDestinationStops.contains(stopKey)) {
                    destinationStopIterator.remove();
                } else {
                    uniqueDestinationStops.add(stopKey);
                }
            }

            if ((originNodeStops.isEmpty()) || (destinationNodeStops.isEmpty())) {
                continue;
            } else {
                // Create arraylists of travel times from stops to origin and destination
                ArrayList<Double> travelTimesOriginToOriginStops = findTravelTimesFromOriginToOriginStops(originNodeId,
                        costOriginToOriginNode, kDTreeForNodes, originNodeStops, nodes, links);
                System.out.println("Travel times origin to origin vicinity stops: " +
                        travelTimesOriginToOriginStops);  // todo debug
                ArrayList<Double> travelTimesDestinationStopsToDestination =
                        findTravelTimesFromDestinationStopsToDestination(destinationNodeId,
                                costDestinationNodeToDestination, kDTreeForNodes, destinationNodeStops, nodes, links);
                System.out.println("Travel times destination vicinity stops to destination: " +
                        travelTimesDestinationStopsToDestination);  // todo debug

                // Execute RAPTOR runs (arrival times at each stop are defined within the method call)
                determineLeastTotalTravelTimeExactRAPTOR(originPointDepartureTime, originNodeStops,
                        destinationNodeStops, travelTimesOriginToOriginStops, travelTimesDestinationStopsToDestination,
                        multiModalQueryResponses, routeStops, stopTimes, stops, stopRoutes, transfers);
            }

            long exactQueryProcessingEndTime = System.nanoTime();
            multiModalQueryResponses.setTimeElapsedQueryProcessingExactSolution(exactQueryProcessingEndTime -
                    exactQueryProcessingStartTime);

            // Stop lists containing all types of transit stops except bus stops (stop hierarchy (SH) heuristic)
            long sHQueryProcessingStartTime = System.nanoTime();
            if ((!originNodeStops.isEmpty()) && (!destinationNodeStops.isEmpty())) {
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

                if ((!originNodeNonBusStops.isEmpty()) && (!destinationNodeNonBusStops.isEmpty())) {
                    // Create arraylists of travel times from stops to origin and destination
                    ArrayList<Double> travelTimesOriginToOriginStopsSH = findTravelTimesFromOriginToOriginStops
                            (originNodeId, costOriginToOriginNode, kDTreeForNodes, originNodeNonBusStops, nodes, links);
                    ArrayList<Double> travelTimesDestinationStopsSHToDestination =
                            findTravelTimesFromDestinationStopsToDestination(destinationNodeId,
                                    costDestinationNodeToDestination, kDTreeForNodes, destinationNodeNonBusStops, nodes,
                                    links);

                    // Execute RAPTOR runs (arrival times at each stop are defined within the method call)
                    determineLeastTotalTravelTimeSHRAPTOR(originPointDepartureTime, originNodeNonBusStops,
                            destinationNodeNonBusStops, travelTimesOriginToOriginStopsSH,
                            travelTimesDestinationStopsSHToDestination, multiModalQueryResponses, routeStops, stopTimes,
                            stops, stopRoutes, transfers);
                }
            }
            long sHQueryProcessingEndTime = System.nanoTime();
            multiModalQueryResponses.setTimeElapsedQueryProcessingSHSolution(sHQueryProcessingEndTime -
                    sHQueryProcessingStartTime);

            // Stop lists containing transit stops serving a certain volume of trips (stop frequency (SF) heuristic)
            long sFQueryProcessingStartTime = System.nanoTime();
            System.out.println("Looking at SH cases");
            if ((!originNodeStops.isEmpty()) && (!destinationNodeStops.isEmpty())) {
                ArrayList<Stop> originNodeHighFrequencyStops = new ArrayList<>();
                for (Stop stop : originNodeStops) {
                    if (stop.getStopTripCount() >= MINIMUM_TRIPS_SERVED_BY_HIGH_FREQUENCY_ORIGIN_STOPS) {
                        originNodeHighFrequencyStops.add(stop);
                    }
                }

                ArrayList<Stop> destinationNodeHighFrequencyStops = new ArrayList<>();
                for (Stop stop : destinationNodeStops) {
                    if (stop.getStopTripCount() >= MINIMUM_TRIPS_SERVED_BY_HIGH_FREQUENCY_DESTINATION_STOPS) {
                        destinationNodeHighFrequencyStops.add(stop);
                    }
                }

                if ((!originNodeHighFrequencyStops.isEmpty()) && (!destinationNodeHighFrequencyStops.isEmpty())) {
                    // Create arraylists of travel times from stops to origin and destination
                    ArrayList<Double> travelTimesOriginToOriginStopsSF = findTravelTimesFromOriginToOriginStops
                            (originNodeId, costOriginToOriginNode, kDTreeForNodes, originNodeHighFrequencyStops, nodes,
                                    links);
                    ArrayList<Double> travelTimesDestinationStopsSFToDestination =
                            findTravelTimesFromDestinationStopsToDestination(destinationNodeId,
                                    costDestinationNodeToDestination, kDTreeForNodes, destinationNodeHighFrequencyStops,
                                    nodes, links);

                    // Execute RAPTOR runs (arrival times at each stop are defined within the method call)
                    determineLeastTotalTravelTimeSFRAPTOR(originPointDepartureTime, originNodeHighFrequencyStops,
                            destinationNodeHighFrequencyStops, travelTimesOriginToOriginStopsSF,
                            travelTimesDestinationStopsSFToDestination, multiModalQueryResponses, routeStops, stopTimes,
                            stops, stopRoutes, transfers);

                }
            }
            long sFQueryProcessingEndTime = System.nanoTime();
            multiModalQueryResponses.setTimeElapsedQueryProcessingSFSolution(sFQueryProcessingEndTime -
                    sFQueryProcessingStartTime);

            /* Debugging statements:
            System.out.println(
                    multiModalQueryResponses.getOriginPointLongitude() + "\n" +
                            multiModalQueryResponses.getOriginPointLatitude() + "\n" +
                            multiModalQueryResponses.getDestinationPointLongitude() + "\n" +
                            multiModalQueryResponses.getDestinationPointLatitude() + "\n" +
                            multiModalQueryResponses.getDepartureTimeOriginPointInt() + "\n" +
                            multiModalQueryResponses.getNearestOriginNodeId() + "\n" +
                            multiModalQueryResponses.getNearestDestinationNodeId() + "\n" +

                            multiModalQueryResponses.getCountOriginStopsConsideredExactSolution() + "\n" +
                            multiModalQueryResponses.getCountDestinationStopsConsideredExactSolution() + "\n" +
                            multiModalQueryResponses.getTimeElapsedQueryProcessingExactSolution() + "\n" +
                            multiModalQueryResponses.getOriginStopIdExactSolution() + "\n" +
                            multiModalQueryResponses.getOriginStopNameExactSolution() + "\n" +
                            multiModalQueryResponses.getDestinationStopIdExactSolution() + "\n" +
                            multiModalQueryResponses.getDestinationStopNameExactSolution() + "\n" +
                            multiModalQueryResponses.getTravelTimeOriginToOriginStopExactSolution() + "\n" +
                            multiModalQueryResponses.getTravelTimeOriginStopToDestinationStopExactSolution() + "\n" +
                            multiModalQueryResponses.getTravelTimeDestinationStopToDestinationExactSolution() + "\n" +
                            multiModalQueryResponses.getTotalTravelTimeExactSolution() + "\n" +
                            multiModalQueryResponses.getEarliestArrivalTimeExactSolution() + "\n" +

                            multiModalQueryResponses.getCountOriginStopsConsideredSHSolution() + "\n" +
                            multiModalQueryResponses.getCountDestinationStopsConsideredSHSolution() + "\n" +
                            multiModalQueryResponses.getTimeElapsedQueryProcessingSHSolution() + "\n" +
                            multiModalQueryResponses.getOriginStopIdSHSolution() + "\n" +
                            multiModalQueryResponses.getOriginStopNameSHSolution() + "\n" +
                            multiModalQueryResponses.getDestinationStopIdSHSolution() + "\n" +
                            multiModalQueryResponses.getDestinationStopNameSHSolution() + "\n" +
                            multiModalQueryResponses.getTravelTimeOriginToOriginStopSHSolution() + "\n" +
                            multiModalQueryResponses.getTravelTimeOriginStopToDestinationStopSHSolution() + "\n" +
                            multiModalQueryResponses.getTravelTimeDestinationStopToDestinationSHSolution() + "\n" +
                            multiModalQueryResponses.getTotalTravelTimeSHSolution() + "\n" +
                            multiModalQueryResponses.getEarliestArrivalTimeSHSolution() + "\n" +
                            multiModalQueryResponses.isAccuracyMarkerSHSolution() + "\n" +

                            multiModalQueryResponses.getCountOriginStopsConsideredSFSolution() + "\n" +
                            multiModalQueryResponses.getCountDestinationStopsConsideredSFSolution() + "\n" +
                            multiModalQueryResponses.getTimeElapsedQueryProcessingSFSolution() + "\n" +
                            multiModalQueryResponses.getOriginStopIdSFSolution() + "\n" +
                            multiModalQueryResponses.getOriginStopNameSFSolution() + "\n" +
                            multiModalQueryResponses.getDestinationStopIdSFSolution() + "\n" +
                            multiModalQueryResponses.getDestinationStopNameSFSolution() + "\n" +
                            multiModalQueryResponses.getTravelTimeOriginToOriginStopSFSolution() + "\n" +
                            multiModalQueryResponses.getTravelTimeOriginStopToDestinationStopSFSolution() + "\n" +
                            multiModalQueryResponses.getTravelTimeDestinationStopToDestinationSFSolution() + "\n" +
                            multiModalQueryResponses.getTotalTravelTimeSFSolution() + "\n" +
                            multiModalQueryResponses.getEarliestArrivalTimeSFSolution() + "\n" +
                            multiModalQueryResponses.isAccuracyMarkerSFSolution()
            );
            */

            // todo nest below-two blocks in larger blocks that check whether we get outputs from exact solution
            //  checks or not (sometimes we may get no route at all in the first place), and then count such cases
            //  for reducing from accuracy percentage denominator, and also handle cases wherein SF and SH
            //  ideal paths may not exist

            // todo real logic flaw debugging begins here, since you ran through the path finders super fast, but to
            //  get there you need non null responses, and also a basic distance checker between OD pairs
            if ((multiModalQueryResponses.getOriginStopNameExactSolution() != null) && (multiModalQueryResponses.   // todo debug
                    getDestinationStopNameExactSolution() != null)) {
                nonNullExactResponseCounter++;
                if ((multiModalQueryResponses.getOriginStopNameExactSolution().equalsIgnoreCase(multiModalQueryResponses.
                        getOriginStopNameSHSolution())) && (multiModalQueryResponses.getDestinationStopNameExactSolution().
                        equalsIgnoreCase(multiModalQueryResponses.getDestinationStopNameSHSolution())) &&
                        ((int) (multiModalQueryResponses.getTotalTravelTimeExactSolution()) ==
                                (int) (multiModalQueryResponses.getTotalTravelTimeSHSolution()))) {
                    multiModalQueryResponses.setAccuracyMarkerSHSolution(true);
                    sHHeuristicCorrectOutputCounter++;
                }

                if ((multiModalQueryResponses.getOriginStopNameExactSolution().equalsIgnoreCase(multiModalQueryResponses.
                        getOriginStopNameSFSolution())) && (multiModalQueryResponses.getDestinationStopNameExactSolution().
                        equalsIgnoreCase(multiModalQueryResponses.getDestinationStopNameSFSolution())) &&
                        ((int) (multiModalQueryResponses.getTotalTravelTimeExactSolution()) ==
                                (int) (multiModalQueryResponses.getTotalTravelTimeSFSolution()))) {
                    multiModalQueryResponses.setAccuracyMarkerSFSolution(true);
                    sFHeuristicCorrectOutputCounter++;
                }

                multiModalQueriesResponses.put(multiModalQueryEntry.getKey(), multiModalQueryResponses);
            }
        }
        long queriesProcessingEndTime = System.nanoTime();
        long queriesProcessingDuration = queriesProcessingEndTime - queriesProcessingStartTime;

        System.out.println("Non null exact response count: " + nonNullExactResponseCounter + "\n" +
                "SH Count: " + sHHeuristicCorrectOutputCounter + "\n" +
                "SF Count: " + sFHeuristicCorrectOutputCounter);

        // Print preprocessing and querying costs and heuristic-performance outputs
        System.out.println("Times elapsed (in nanoseconds) for:" + "\n" +
                "1. Processing GTFS data: " + gtfsDataProcessingDuration + "\n" +
                "2. Processing OSM-OPL data: " + osmDataProcessingDuration + "\n" +
                "3. Building KD Trees for Stops and Nodes: " + kDTreesBuildDuration + "\n" +
                "4. Generating " + multiModalQueries.size() + " queries: " + queryGenerationDuration + "\n" +
                "5. Processing " + multiModalQueries.size() + " queries: " + queriesProcessingDuration + "\n" +
                "\n" +
                "Performance evaluation (in %) of each heuristic:" + "\n" +
                "1. Accuracy of stop hierarchy heuristic: " +
                (sHHeuristicCorrectOutputCounter / nonNullExactResponseCounter) + "%" + "\n" +
                "2. Accuracy of stop frequency heuristic: " +
                (sFHeuristicCorrectOutputCounter / nonNullExactResponseCounter) + "%" + "\n");

        // Write out file documenting router responses to multi-modal queries
        String multiModalQueriesResponsesFilePath = "D:/Documents - Education + Work/Education - TUM/Year 2/" +
                "Fourth Semester/MasterThesis/Results/Responses/multiModalQueriesResponses.txt";
        writeMultiModalResponses(multiModalQueriesResponsesFilePath, multiModalQueriesResponses);
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
        // osmDataReaderWriterForDijkstra.removeCircularLinks();    // This step is optional
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
            double dijkstraTravelTime = dijkstraBasedRouter.findShortestDrivingPath(originNodeId,   // todo this might be super inefficient, instantiate one dijkstra and throw it everywhere
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
                    AVERAGE_WALKING_SPEED_M_PER_MIN;    // todo leading to laughable values, sort this comedy

            DijkstraBasedRouter dijkstraBasedRouter = new DijkstraBasedRouter();
            double dijkstraTravelTime = dijkstraBasedRouter.findShortestDrivingPath(destinationNodeId,
                    nearestNodeOfDestinationStop.getNodeId(), nodes, links) * AVERAGE_WALKING_SPEED_M_PER_MIN;

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

        System.out.println("Exact raptor running");
        double leastTotalTravelTime = Double.MAX_VALUE;
        int originStopIndexLeastTotalTravelTimeExact = -1;
        int destinationStopIndexLeastTotalTravelTimeExact = -1;

        for (int originStopCounter = 0; originStopCounter < originStopList.size(); originStopCounter++) {
            for (int destinationStopCounter = 0; destinationStopCounter < destinationStopList.size();
                 destinationStopCounter++) {
                int originStopId = originStopList.get(originStopCounter).getStopId();
                int destinationStopId = destinationStopList.get(destinationStopCounter).getStopId();

                System.out.println("Touched a raptor");
                double totalTravelTimeForStopPair = runRAPTOR(originPointDepartureTime, originStopId, destinationStopId,
                        travelTimesOriginToOriginStops.get(originStopCounter),
                        travelTimesDestinationStopsToDestination.get(destinationStopCounter),
                        routeStops, stopTimes, stops, stopRoutes, transfers);
                System.out.println("Outta raptor");

                if ((totalTravelTimeForStopPair != -1) && (totalTravelTimeForStopPair >= 0)) {
                    if (totalTravelTimeForStopPair < leastTotalTravelTime) {
                        leastTotalTravelTime = totalTravelTimeForStopPair;
                        originStopIndexLeastTotalTravelTimeExact = originStopCounter;
                        destinationStopIndexLeastTotalTravelTimeExact = destinationStopCounter;
                    }
                    System.out.println("Travel time for stop pair: " + totalTravelTimeForStopPair);
                    System.out.println("Least total travel time: " + leastTotalTravelTime);
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
                (originPointDepartureTime + travelTimeOriginToOriginStop),  //
                routeStops, stopTimes, stops, stopRoutes, transfers).getTravelTimeMinutes();

        if (totalTransitDuration != -1) {
            totalTravelTime = travelTimeOriginToOriginStop + totalTransitDuration +
                    travelTimeDestinationStopToDestination;
        }

        return totalTravelTime;
    }

    // Write out responses to multi-modal queries
    private static void writeMultiModalResponses(String multiModalQueriesResponsesFilePath,
                                         LinkedHashMap<Integer, MultiModalQueryResponses>
                                                 multiModalQueriesResponses) {
        try {
            // Writer for "multiModalQueriesResponses.txt"
            BufferedWriter multiModalQueriesResponsesWriter = new BufferedWriter(new FileWriter
                    (multiModalQueriesResponsesFilePath));

            // Set up header array
            multiModalQueriesResponsesWriter.write("queryId,originPointLongitude,originPointLatitude," +
                    "destinationPointLongitude,destinationPointLatitude,departureTimeOriginPointInt," +
                    "nearestOriginNodeId,nearestDestinationNodeId,countOriginStopsConsideredExactSolution," +
                    "countDestinationStopsConsideredExactSolution,timeElapsedQueryProcessingExactSolution," +
                    "originStopIdExactSolution,originStopNameExactSolution,destinationStopIdExactSolution," +
                    "destinationStopNameExactSolution,travelTimeOriginToOriginStopExactSolution," +
                    "travelTimeOriginStopToDestinationStopExactSolution," +
                    "travelTimeDestinationStopToDestinationExactSolution,totalTravelTimeExactSolution," +
                    "earliestArrivalTimeExactSolution,countOriginStopsConsideredSHSolution," +
                    "countDestinationStopsConsideredSHSolution,timeElapsedQueryProcessingSHSolution," +
                    "originStopIdSHSolution,originStopNameSHSolution,destinationStopIdSHSolution," +
                    "destinationStopNameSHSolution,travelTimeOriginToOriginStopSHSolution," +
                    "travelTimeOriginStopToDestinationStopSHSolution," +
                    "travelTimeDestinationStopToDestinationSHSolution,totalTravelTimeSHSolution," +
                    "earliestArrivalTimeSHSolution,accuracyMarkerSHSolution,countOriginStopsConsideredSFSolution," +
                    "countDestinationStopsConsideredSFSolution,timeElapsedQueryProcessingSFSolution," +
                    "originStopIdSFSolution,originStopNameSFSolution,destinationStopIdSFSolution," +
                    "destinationStopNameSFSolution,travelTimeOriginToOriginStopSFSolution," +
                    "travelTimeOriginStopToDestinationStopSFSolution," +
                    "travelTimeDestinationStopToDestinationSFSolution,totalTravelTimeSFSolution," +
                    "earliestArrivalTimeSFSolution,accuracyMarkerSFSolution\n");

            // Write body based on "multiModalQueriesResponses" hashmap
            for (HashMap.Entry<Integer, MultiModalQueryResponses> multiModalQueryResponsesEntry :
                    multiModalQueriesResponses.entrySet()) {
                multiModalQueriesResponsesWriter.write(
                        multiModalQueryResponsesEntry.getKey() + "," +
                                multiModalQueryResponsesEntry.getValue().getOriginPointLongitude() + "," +
                                multiModalQueryResponsesEntry.getValue().getOriginPointLatitude() + "," +
                                multiModalQueryResponsesEntry.getValue().getDestinationPointLongitude() + "," +
                                multiModalQueryResponsesEntry.getValue().getDestinationPointLatitude() + "," +
                                multiModalQueryResponsesEntry.getValue().getDepartureTimeOriginPointInt() + "," +
                                multiModalQueryResponsesEntry.getValue().getNearestOriginNodeId() + "," +
                                multiModalQueryResponsesEntry.getValue().getNearestDestinationNodeId() + "," +
                                multiModalQueryResponsesEntry.getValue().
                                        getCountOriginStopsConsideredExactSolution() + "," +
                                multiModalQueryResponsesEntry.getValue().
                                        getCountDestinationStopsConsideredExactSolution() + "," +
                                multiModalQueryResponsesEntry.getValue().
                                        getTimeElapsedQueryProcessingExactSolution() + "," +
                                multiModalQueryResponsesEntry.getValue().getOriginStopIdExactSolution() + "," +
                                multiModalQueryResponsesEntry.getValue().getOriginStopNameExactSolution() + "," +
                                multiModalQueryResponsesEntry.getValue().getDestinationStopIdExactSolution() + "," +
                                multiModalQueryResponsesEntry.getValue().getDestinationStopNameExactSolution() + "," +
                                multiModalQueryResponsesEntry.getValue().
                                        getTravelTimeOriginToOriginStopExactSolution() + "," +
                                multiModalQueryResponsesEntry.getValue().
                                        getTravelTimeOriginStopToDestinationStopExactSolution() + "," +
                                multiModalQueryResponsesEntry.getValue().
                                        getTravelTimeDestinationStopToDestinationExactSolution() + "," +
                                multiModalQueryResponsesEntry.getValue().getTotalTravelTimeExactSolution() + "," +
                                multiModalQueryResponsesEntry.getValue().getEarliestArrivalTimeExactSolution() + "," +
                                multiModalQueryResponsesEntry.getValue().
                                        getCountOriginStopsConsideredSHSolution() + "," +
                                multiModalQueryResponsesEntry.getValue().
                                        getCountDestinationStopsConsideredSHSolution() + "," +
                                multiModalQueryResponsesEntry.getValue().
                                        getTimeElapsedQueryProcessingSHSolution() + "," +
                                multiModalQueryResponsesEntry.getValue().getOriginStopIdSHSolution() + "," +
                                multiModalQueryResponsesEntry.getValue().getOriginStopNameSHSolution() + "," +
                                multiModalQueryResponsesEntry.getValue().getDestinationStopIdSHSolution() + "," +
                                multiModalQueryResponsesEntry.getValue().getDestinationStopNameSHSolution() + "," +
                                multiModalQueryResponsesEntry.getValue().
                                        getTravelTimeOriginToOriginStopSHSolution() + "," +
                                multiModalQueryResponsesEntry.getValue().
                                        getTravelTimeOriginStopToDestinationStopSHSolution() + "," +
                                multiModalQueryResponsesEntry.getValue().
                                        getTravelTimeDestinationStopToDestinationSHSolution() + "," +
                                multiModalQueryResponsesEntry.getValue().getTotalTravelTimeSHSolution() + "," +
                                multiModalQueryResponsesEntry.getValue().getEarliestArrivalTimeSHSolution() + "," +
                                multiModalQueryResponsesEntry.getValue().isAccuracyMarkerSHSolution() + "," +
                                multiModalQueryResponsesEntry.getValue().
                                        getCountOriginStopsConsideredSFSolution() + "," +
                                multiModalQueryResponsesEntry.getValue().
                                        getCountDestinationStopsConsideredSFSolution() + "," +
                                multiModalQueryResponsesEntry.getValue().
                                        getTimeElapsedQueryProcessingSFSolution() + "," +
                                multiModalQueryResponsesEntry.getValue().getOriginStopIdSFSolution() + "," +
                                multiModalQueryResponsesEntry.getValue().getOriginStopNameSFSolution() + "," +
                                multiModalQueryResponsesEntry.getValue().getDestinationStopIdSFSolution() + "," +
                                multiModalQueryResponsesEntry.getValue().getDestinationStopNameSFSolution() + "," +
                                multiModalQueryResponsesEntry.getValue().
                                        getTravelTimeOriginToOriginStopSFSolution() + "," +
                                multiModalQueryResponsesEntry.getValue().
                                        getTravelTimeOriginStopToDestinationStopSFSolution() + "," +
                                multiModalQueryResponsesEntry.getValue().
                                        getTravelTimeDestinationStopToDestinationSFSolution() + "," +
                                multiModalQueryResponsesEntry.getValue().getTotalTravelTimeSFSolution() + "," +
                                multiModalQueryResponsesEntry.getValue().getEarliestArrivalTimeSFSolution() + "," +
                                multiModalQueryResponsesEntry.getValue().isAccuracyMarkerSFSolution() + "\n"
                );
            }

        } catch (IOException iOE) {
            System.out.println("Input-output exception. Please check the \"multiModalQueriesResponses\" hashmap.");
        }
    }
}
