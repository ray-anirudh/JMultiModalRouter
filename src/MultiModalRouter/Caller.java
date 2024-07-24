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

    private static final double AVERAGE_WALKING_SPEED_M_PER_MIN = 85.2;
    private static final double MAXIMUM_WALKING_DISTANCE_M = 1_500;
    // Refer to: https://www.emerald.com/insight/content/doi/10.1108/SASBE-07-2017-0031/full/html
    private static final double MAXIMUM_DRIVING_DISTANCE_M = 6_000;
    private static final int GTFS_BUS_ROUTE_TYPE_ID = 3;
    private static final int MINIMUM_TRIPS_SERVED_BY_HIGH_FREQUENCY_STOPS = 135;
    private static final int MINUTES_PER_HOUR = 60;
    private static final int MINUTES_PER_DAY = 1440;

    public static void main(String[] args) {
        // GTFS data reader-writer instantiation to read, write, and store data
        long gtfsStartTime = System.nanoTime();
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
        Stop[] stopsForNNSearches = stops.values().toArray(new Stop[0]);
        LinkedHashMap<Integer, StopRoute> stopRoutes = gtfsDataReaderWriterForRaptor.getStopRoutes();
        LinkedHashMap<Integer, Transfer> transfers = gtfsDataReaderWriterForRaptor.getTransfers();
        long gtfsEndTime = System.nanoTime();

        // OSM data reader-writer instantiation to read, write, and store data
        long osmStartTime = System.nanoTime();
        OSMDataReaderWriter osmDataReaderWriterForDijkstra = new OSMDataReaderWriter();
        String osmOplExtractFilePath = "";
        String dijkstraFolderPath = "";
        getDijkstraMaps(osmOplExtractFilePath, dijkstraFolderPath, osmDataReaderWriterForDijkstra);

        // Get all data for Dijkstra algorithm's execution
        LinkedHashMap<Long, Link> links = osmDataReaderWriterForDijkstra.getLinks();
        LinkedHashMap<Long, Node> nodes = osmDataReaderWriterForDijkstra.getNodes();
        Node[] nodesForNNSearches = nodes.values().toArray(new Node[0]);
        long osmEndTime = System.nanoTime();

        // Build KD-Tree for snapping to RAPTOR-relevant transit stops
        long kDStartTime = System.nanoTime();
        KDTreeForStops kDTreeForStops = new KDTreeForStops();
        kDTreeForStops.buildStopBasedKDTree(stopsForNNSearches);

        // Build KD-Tree for snapping to Dijkstra-relevant network nodes
        KDTreeForNodes kDTreeForNodes = new KDTreeForNodes();
        kDTreeForNodes.buildNodeBasedKDTree(nodesForNNSearches);
        long kDEndTime = System.nanoTime();

        // Load all multi-modal queries
        MultiModalQueryReader multiModalQueryReader = new MultiModalQueryReader();
        String multiModalQueriesFilePath = "";
        String multiModalQueriesResponsesFilePath = "";
        LinkedHashMap<Integer, MultiModalQuery> multiModalQueries = multiModalQueryReader.
                readMultiModalQueries(multiModalQueriesFilePath);
        LinkedHashMap<Integer, MultiModalQueryResponse> multiModalQueriesResponses = new LinkedHashMap<>();

        // Create hashmaps to collect responses from routing queries
        LinkedHashMap<Integer, MultiModalQueryResponse> multiModalQueriesResponsesExact = new LinkedHashMap<>();
        LinkedHashMap<Integer, MultiModalQueryResponse> multiModalQueriesResponsesNonBus = new LinkedHashMap<>();
        LinkedHashMap<Integer, MultiModalQueryResponse> multiModalQueriesResponsesHighFrequency = new LinkedHashMap<>();

        int routingQueryCount = 0;
        long cumulativeQueryProcessingDuration = 0;
        // Iterate through all multi-modal queries
        for(MultiModalQuery multiModalQuery : multiModalQueries.values()) {
            long queryStartTime = System.nanoTime();

            // Instantiate an empty multi-modal query response and add it to all hashmaps
            routingQueryCount++;
            MultiModalQueryResponse multiModalQueryResponse = new MultiModalQueryResponse();
            multiModalQueriesResponsesExact.put(routingQueryCount, multiModalQueryResponse);
            multiModalQueriesResponsesNonBus.put(routingQueryCount, multiModalQueryResponse);
            multiModalQueriesResponsesHighFrequency.put(routingQueryCount, multiModalQueryResponse);

            // Extract query parameters
            double originLongitude = multiModalQuery.getOriginLongitude();
            double originLatitude = multiModalQuery.getOriginLatitude();
            int originDepartureTime = multiModalQuery.getDepartureTime();
            double destinationLongitude = multiModalQuery.getDestinationLongitude();
            double destinationLatitude = multiModalQuery.getDestinationLatitude();

            // Find nearest nodes for the origin and destination points, as well as their parameters
            Node originNode = kDTreeForNodes.findNearestNode(originLongitude, originLatitude);
            Node destinationNode = kDTreeForNodes.findNearestNode(destinationLongitude, destinationLatitude);
            long originNodeId = originNode.getNodeId();
            long destinationNodeId = destinationNode.getNodeId();
            double originNodeLongitude = originNode.getNodeLongitude();
            double originNodeLatitude = originNode.getNodeLatitude();
            double destinationNodeLongitude = destinationNode.getNodeLongitude();
            double destinationNodeLatitude = destinationNode.getNodeLatitude();

            // Determine snapping cost - minutes required to get from one point to another (aerial distance) on foot
            double costOriginToOriginNode = originNode.equiRectangularDistanceTo(originLongitude, originNodeLatitude) /
                    AVERAGE_WALKING_SPEED_M_PER_MIN;
            double costDestinationNodeToDestination = destinationNode.equiRectangularDistanceTo(destinationLongitude,
                    destinationLatitude) / AVERAGE_WALKING_SPEED_M_PER_MIN;

            /**
             * Set up nearest neighbor stop lists
             */

            // Stop lists containing all types of transit stops in a node's vicinity
            ArrayList<Stop> originNodeStops = kDTreeForStops.findStopsWithinDoughnut(originNodeLongitude,
                    originNodeLatitude, MAXIMUM_WALKING_DISTANCE_M, MAXIMUM_DRIVING_DISTANCE_M);
            ArrayList<Stop> destinationNodeStops = kDTreeForStops.findStopsWithinDoughnut(destinationNodeLongitude,
                    destinationNodeLatitude, 0, MAXIMUM_WALKING_DISTANCE_M);

            // Stop lists containing all types of transit stops in a node's vicinity, except bus stops
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

            // Stop lists containing transit stops exceeding a pre-defined number of served trips
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

            // Set up arraylists for all types of nearest-stop lists
            ArrayList<ArrayList<Stop>> originStopLists = new ArrayList<>();
            originStopLists.add(originNodeStops);
            originStopLists.add(originNodeNonBusStops);
            originStopLists.add(originNodeHighFrequencyStops);
            ArrayList<ArrayList<Stop>> destinationStopLists = new ArrayList<>();
            destinationStopLists.add(destinationNodeStops);
            destinationStopLists.add(destinationNodeNonBusStops);
            destinationStopLists.add(destinationNodeHighFrequencyStops);

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

                // Find nearest network nodes of the found stops
                ArrayList<Node> nearestNodesOfOriginStops = new ArrayList<>();
                ArrayList<Double> nNSnappingCostsOfOriginStops = new ArrayList<>();
                ArrayList<Node> nearestNodesOfDestinationStops = new ArrayList<>();
                ArrayList<Double> nNSnappingCostsOfDestinationStops = new ArrayList<>();

                for (Stop stopNearOriginNode : originStops) {
                    Node nearestNodeOfOriginStop = kDTreeForNodes.findNearestNode(stopNearOriginNode.getStopLongitude(),
                            stopNearOriginNode.getStopLatitude());
                    double costOriginStopToNearestNode = nearestNodeOfOriginStop.equiRectangularDistanceTo(
                            stopNearOriginNode.getStopLongitude(), stopNearOriginNode.getStopLatitude()) /
                            AVERAGE_WALKING_SPEED_M_PER_MIN;
                    nearestNodesOfOriginStops.add(nearestNodeOfOriginStop);
                    nNSnappingCostsOfOriginStops.add(costOriginStopToNearestNode);
                }

                for (Stop stopNearDestinationNode : destinationStops) {
                    Node nearestNodeOfDestinationStop = kDTreeForNodes.findNearestNode(stopNearDestinationNode.
                            getStopLongitude(), stopNearDestinationNode.getStopLatitude());
                    double costDestinationStopToNearestNode = nearestNodeOfDestinationStop.equiRectangularDistanceTo
                            (stopNearDestinationNode.getStopLongitude(), stopNearDestinationNode.getStopLatitude()) /
                            AVERAGE_WALKING_SPEED_M_PER_MIN;
                    nearestNodesOfDestinationStops.add(nearestNodeOfDestinationStop);
                    nNSnappingCostsOfDestinationStops.add(costDestinationStopToNearestNode);
                }

                // Dijkstra-runs for origin and destination stops (attained travel times have minutes as units)
                ArrayList<Double> arrivalTimesToOriginStopsNodes = new ArrayList<>();
                for (int i = 0; i < nearestNodesOfOriginStops.size(); i++) {
                    DijkstraBasedRouter dijkstraBasedRouter = new DijkstraBasedRouter();
                    double dijkstraTravelTime = dijkstraBasedRouter.findShortestDrivingPath(originNodeId,
                            nearestNodesOfOriginStops.get(i).getNodeId(), nodes, links);
                    arrivalTimesToOriginStopsNodes.add(originDepartureTime + dijkstraTravelTime +
                            nNSnappingCostsOfOriginStops.get(i));
                }

                ArrayList<Double> travelTimesFromDestinationStopsNodes = new ArrayList<>();
                for (int i = 0; i < nearestNodesOfDestinationStops.size(); i++) {
                    DijkstraBasedRouter dijkstraBasedRouter = new DijkstraBasedRouter();
                    double dijkstraTravelTime = dijkstraBasedRouter.findShortestDrivingPath(
                            nearestNodesOfDestinationStops.get(i).getNodeId(), destinationNodeId, nodes, links);
                    travelTimesFromDestinationStopsNodes.add(dijkstraTravelTime);
                }

                // Execute RAPTOR runs
                double leastTotalTravelTime = Double.MAX_VALUE;
                double totalTransitDuration = -1;
                double originToOriginStopDuration = -1;
                double destinationStopToDestinationDuration = -1;
                int selectedOriginStopId = -1;
                int selectedDestinationStopId = -1;
                String selectedOriginStopName = "";
                String selectedDestinationStopName = "";

                for (int originStopCounter = 0; originStopCounter < originNodeStops.size(); originStopCounter++) {
                    for (int destinationStopCounter = 0; destinationStopCounter < destinationNodeStops.size();
                         destinationStopCounter++) {

                        RAPTOR rAPTOR = new RAPTOR();
                        double transitTime = rAPTOR.findShortestTransitPath(originNodeStops.get(originStopCounter).
                                        getStopId(), destinationNodeStops.get(destinationStopCounter).getStopId(),
                                arrivalTimesToOriginStopsNodes.get(originStopCounter),
                                routeStops, stopTimes, stops, stopRoutes, transfers).getTravelTimeMinutes();

                        selectedOriginStopId = originNodeStops.get(originStopCounter).getStopId();
                        selectedOriginStopName = originNodeStops.get(originStopCounter).getStopName();
                        selectedDestinationStopId = destinationNodeStops.get(destinationStopCounter).getStopId();
                        selectedDestinationStopName = destinationNodeStops.get(destinationStopCounter).getStopName();

                        if (transitTime != -1) {
                            totalTransitDuration = transitTime;
                            originToOriginStopDuration = costOriginToOriginNode + arrivalTimesToOriginStopsNodes.
                                    get(originStopCounter) + nNSnappingCostsOfOriginStops.get(originStopCounter);
                            destinationStopToDestinationDuration = nNSnappingCostsOfDestinationStops.
                                    get(destinationStopCounter) + travelTimesFromDestinationStopsNodes.
                                    get(destinationStopCounter) + costDestinationNodeToDestination;

                            double totalTravelTime = originToOriginStopDuration + totalTransitDuration +
                                    destinationStopToDestinationDuration;
                            if (totalTravelTime < leastTotalTravelTime) {
                                leastTotalTravelTime = totalTravelTime;
                            }

                        } else {
                            System.out.println("Multi-modal routing query #" + routingQueryCount+ "\n" +
                                    "Origin coordinates: (" + originLatitude + ", " + originLongitude + ")\n" +
                                    "Destination coordinates: (" + destinationLatitude + ", " + destinationLongitude +
                                    "\n" +
                                    "Departure time: " + (originDepartureTime / 60) + ":" + (originDepartureTime % 60) +
                                    "\n" +
                                    "No transit route found between stops " + selectedOriginStopName + " and " +
                                    selectedDestinationStopName + ".");
                        }
                    }
                }

                long queryEndTime = System.nanoTime();
                long queryProcessingDuration = queryEndTime - queryStartTime;
                cumulativeQueryProcessingDuration += queryProcessingDuration;

                int accuracyMarker = 0;
                // 0 indicates incorrect response from routing process and stop list combination
                // if (routingQueryCount)

                // Print result
                System.out.println("Solution found for multi-modal routing query #" + routingQueryCount);
                MultiModalQueryResponse multiModalQueryResponse = new MultiModalQueryResponse(listTypeIndex,
                        originLongitude, originLatitude, destinationLongitude, destinationLatitude,
                        (((originDepartureTime % MINUTES_PER_DAY) / MINUTES_PER_HOUR) + ":" +
                                ((originDepartureTime % MINUTES_PER_DAY) % MINUTES_PER_HOUR)), originStops.size(),
                        destinationStops.size(), selectedOriginStopName, selectedOriginStopId,
                        selectedDestinationStopName, selectedDestinationStopId, originToOriginStopDuration,
                        totalTransitDuration, destinationStopToDestinationDuration, queryProcessingDuration, );
                multiModalQueriesResponses.put(routingQueryCount, multiModalQueryResponse);
            }
        }

        writeMultiModalQueriesResponses(multiModalQueriesResponsesFilePath, multiModalQueriesResponses);
        System.out.println("Times elapsed (in nanoseconds) for:" + "\n" +
                "1. Preprocessing GTFS data: " + (gtfsEndTime - gtfsStartTime) + "\n" +
                "2. Preprocessing OSM-OPL data: " + (osmEndTime - osmStartTime) + "\n" +
                "3. Building KD Trees for Stops and Nodes: " + (kDEndTime - kDStartTime) + "\n" +
                "4. Processing " + routingQueryCount + " queries: " + cumulativeQueryProcessingDuration);
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
        gtfsDataReaderWriterForRaptor.makeTransfersTransitive();
        gtfsDataReaderWriterForRaptor.filterHashMapsOnLatLong();

        // Write out data used for RAPTOR
        gtfsDataReaderWriterForRaptor.writeRaptorRoutes(raptorRoutesFilePath);
        gtfsDataReaderWriterForRaptor.writeRaptorRouteStops(raptorRouteStopsFilePath);
        gtfsDataReaderWriterForRaptor.writeTrips(tripsFilePath);
        gtfsDataReaderWriterForRaptor.writeRaptorStopTimes(raptorStopTimesFilePath);
        gtfsDataReaderWriterForRaptor.writeRaptorStops(raptorStopsFilePath);
        gtfsDataReaderWriterForRaptor.writeRaptorStopRoutes(raptorStopRoutesFilePath);
        gtfsDataReaderWriterForRaptor.writeRaptorTransfers(raptorTransfersFilePath);
    }

    // Get Dijkstra-relevant datasets ready
    public static void getDijkstraMaps(String osmOplExtractFilePath,
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

    // Write out responses to multi-modal queries in a .txt file
    static void writeMultiModalQueriesResponses(String multiModalQueriesResponsesFilePath,
                                         LinkedHashMap<Integer, MultiModalQueryResponse>
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
            for(HashMap.Entry<Integer, MultiModalQueryResponse> multiModalQueryResponseEntry :
                    multiModalQueriesResponses.entrySet()) {
                MultiModalQueryResponse multiModalQueryResponse = multiModalQueryResponseEntry.getValue();
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
