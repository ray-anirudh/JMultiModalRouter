package src.MultiModalRouter;
// TODO: HEURISTICS CAN BE BASED ON STOPS (STOPTYPE (ASCRIBED VIA ROUTETYPE - BIGGEST CURRENT CANDIDATE), PARENTSTOPPRESENCE), ROUTES (ROUTETYPES, #TRIPS, #STOPS)
// TODO: HEURISTIC DATA CAN BE ASCRIBED IN GTFSDATAMANAGER
// TODO: QUERY PARSING TO PEERFECT INPUT TYPES MUST HAPPEN IN THE CALLER, AND NOT HERE, OR IN THE QUERY CODE (SUCH AS LOCATING STOP AND THEN HOMING IN ON THE STOPIDS)
// TODO REVIEW ALL CODE TO GOD LEVEL PERFECTION BEFORE RUNNING THIS SHIT
// TODO DO TURURURURU DO LIKE A MASSSSSSSIVVVVE (DEEEEEEP, EVERY SINGLE LINE - TAKE 3-4 DAYS) REVIEW BEFORE YOU GET GOINGGGGG
// TODO BEAUTIFY EVERYTHING AFTER A SUCCESSFUL RUN
// TODO BUILD A NULL-SAFETY MADNESS, TYPE SAFETY MADNESS, AND DATA STRUCTURE SAFETY MADNESS
// TODO BLAZE EVERYTHING (SET EVERYTHING ALIGHT) AND THAT HAPPENS FASTEST FROM A 30000 FEET PERSPECTIVE
// TODO: WRITE THE FLASHIEST FANCIEST CODE POSSIBLE

import src.NearestNeighbourFinder.KDTreeForNodes;
import src.NearestNeighbourFinder.KDTreeForStops;
import src.PublicTransportRouter.GTFSDataManager.*;
import src.PublicTransportRouter.RoutingAlgorithm.RAPTOR;
import src.RoadTransportRouter.OSMDataManager.Link;
import src.RoadTransportRouter.OSMDataManager.Node;
import src.RoadTransportRouter.OSMDataManager.OSMDataReaderWriter;
import src.RoadTransportRouter.RoutingAlgorithm.DijkstraBasedRouter;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Caller {

    private static final double AVERAGE_WALKING_SPEED_M_PER_MIN = 85.2;
    private static final double MAXIMUM_WALKING_DISTANCE_M = 800;
    private static final double MAXIMUM_DRIVING_DISTANCE_M = 6_000;

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
        Stop[] stopsForNNSearches = stops.values().toArray(new Stop[0]);
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
        Node[] nodesForNNSearches = nodes.values().toArray(new Node[0]);

        // Build KD-Tree for snapping to RAPTOR-relevant transit stops
        KDTreeForStops kDTreeForStops = new KDTreeForStops();
        kDTreeForStops.buildStopBasedKDTree(stopsForNNSearches);

        // Build KD-Tree for snapping to Dijkstra-relevant network nodes
        KDTreeForNodes kDTreeForNodes = new KDTreeForNodes();
        kDTreeForNodes.buildNodeBasedKDTree(nodesForNNSearches);

        // Load all multi-modal queries
        MultiModalQueryReader multiModalQueryReader = new MultiModalQueryReader();
        String multiModalQueriesFilePath = "";
        LinkedHashMap<Integer, MultiModalQuery> multiModalQueries = multiModalQueryReader.
                readMultiModalQueries(multiModalQueriesFilePath);

        int routingQueryCount = 0;
        // Iterate through all multi-modal queries
        for(MultiModalQuery multiModalQuery : multiModalQueries.values()) {
            routingQueryCount++;
            double originLongitude = multiModalQuery.getOriginLongitude();
            double originLatitude = multiModalQuery.getOriginLatitude();
            int departureTime = multiModalQuery.getDepartureTime();
            double destinationLongitude = multiModalQuery.getDestinationLongitude();
            double destinationLatitude = multiModalQuery.getDestinationLatitude();

            Node nodeNearestToOrigin = kDTreeForNodes.findNearestNode(originLongitude, originLatitude);
            Node nodeNearestToDestination = kDTreeForNodes.findNearestNode(destinationLongitude,
                    destinationLatitude);
            long originNodeId = nodeNearestToOrigin.getNodeId();
            long destinationNodeId = nodeNearestToDestination.getNodeId();

            double originNodeLongitude = nodes.get(originNodeId).getNodeLongitude();
            double originNodeLatitude = nodes.get(originNodeId).getNodeLatitude();
            double destinationNodeLongitude = nodes.get(destinationNodeId).getNodeLongitude();
            double destinationNodeLatitude = nodes.get(destinationNodeId).getNodeLatitude();

            // Determine snapping cost, which is the cost of getting from one point to another (aerial distance) on foot
            double costOriginToOriginNode = nodeNearestToOrigin.equiRectangularDistanceTo(originNodeLongitude,
                    originNodeLatitude) / AVERAGE_WALKING_SPEED_M_PER_MIN;
            double costDestinationToDestinationNode = nodeNearestToDestination.
                    equiRectangularDistanceTo(destinationNodeLongitude, destinationNodeLatitude)
                    / AVERAGE_WALKING_SPEED_M_PER_MIN;

            // Set up nearest neighbor stop lists
            ArrayList<Stop> stopsNearOriginNode = kDTreeForStops.findStopsWithinDoughnut(originNodeLongitude,
                    originNodeLatitude, MAXIMUM_WALKING_DISTANCE_M, MAXIMUM_DRIVING_DISTANCE_M);
            ArrayList<Stop> stopsNearDestinationNode = kDTreeForStops.findStopsWithinDoughnut(
                    destinationLongitude, destinationLatitude, 0, MAXIMUM_WALKING_DISTANCE_M);

            // Find nearest network nodes of the found stops
            ArrayList<Node> nearestNodesOfStopsNearOriginNode = new ArrayList<>();
            ArrayList<Double> nNSnappingCostsOfStopsNearOriginNode = new ArrayList<>();
            // todo null safety of stop lists for areas in remote places
            for (Stop stopNearOriginNode : stopsNearOriginNode) {
                Node nearestNodeOfStopNearOriginNode = kDTreeForNodes.findNearestNode(stopNearOriginNode.
                        getStopLongitude(), stopNearOriginNode.getStopLatitude());
                double costOriginBasedStopToNearestNode = nearestNodeOfStopNearOriginNode.equiRectangularDistanceTo(
                        stopNearOriginNode.getStopLongitude(), stopNearOriginNode.getStopLatitude()) /
                        AVERAGE_WALKING_SPEED_M_PER_MIN;
                nearestNodesOfStopsNearOriginNode.add(nearestNodeOfStopNearOriginNode);
                nNSnappingCostsOfStopsNearOriginNode.add(costOriginBasedStopToNearestNode);
            }

            ArrayList<Node> nearestNodesOfStopsNearDestinationNode = new ArrayList<>();
            ArrayList<Double> nNSnappingCostsOfStopsNearDestinationNode = new ArrayList<>();
            for (Stop stopNearDestinationNode : stopsNearDestinationNode) {
                Node nearestNodeOfStopNearDestinationNode = kDTreeForNodes.findNearestNode(
                        stopNearDestinationNode.getStopLongitude(), stopNearDestinationNode.getStopLatitude());
                double costDestinationBasedStopToNearestNode = nearestNodeOfStopNearDestinationNode.
                        equiRectangularDistanceTo(stopNearDestinationNode.getStopLongitude(), stopNearDestinationNode.
                                getStopLatitude()) / AVERAGE_WALKING_SPEED_M_PER_MIN;
                nearestNodesOfStopsNearDestinationNode.add(nearestNodeOfStopNearDestinationNode);
                nNSnappingCostsOfStopsNearDestinationNode.add(costDestinationBasedStopToNearestNode);
            }

            // Dijkstra-runs for origin and destination stops
            ArrayList<Double> dijkstraArrivalTimesToOriginStops = new ArrayList<>();
            for (int i = 0; i < nearestNodesOfStopsNearOriginNode.size(); i++) {
                DijkstraBasedRouter dijkstraBasedRouter = new DijkstraBasedRouter();
                double dijkstraTravelTime = dijkstraBasedRouter.findShortestDrivingPath(originNodeId,
                        nearestNodesOfStopsNearOriginNode.get(i).getNodeId(), nodes, links);
                dijkstraArrivalTimesToOriginStops.add(departureTime + nNSnappingCostsOfStopsNearOriginNode.get(i) +
                        dijkstraTravelTime);
            }

            ArrayList<Double> dijkstraTravelTimesFromDestinationStop = new ArrayList<>();
            for (int i = 0; i < nearestNodesOfStopsNearDestinationNode.size(); i++) {
                DijkstraBasedRouter dijkstraBasedRouter = new DijkstraBasedRouter();
                double dijkstraTravelTime = dijkstraBasedRouter.findShortestDrivingPath(destinationNodeId,
                        nearestNodesOfStopsNearDestinationNode.get(i).getNodeId(), nodes, links);
                dijkstraTravelTimesFromDestinationStop.add(dijkstraTravelTime);
            }

            // Execute RAPTOR runs
            // todo handle transresponses with -1, -1 parameters
            double leastTotalTravelTime = Double.MAX_VALUE;
            double timeSpentInTransit = -1;
            double timeSpentFromOriginToOriginStop = -1;
            double timeSpentFromDestinationStopToDestination = -1;
            int selectedOriginStopId = -1;
            int selectedDestinationStopId = -1;

            for (int originStopCounter = 0; originStopCounter < stopsNearOriginNode.size(); originStopCounter++) {
                for (int destinationStopCounter = 0; destinationStopCounter < stopsNearDestinationNode.size();
                destinationStopCounter++) {
                    RAPTOR rAPTOR = new RAPTOR();
                    double transitTime = rAPTOR.findShortestTransitPath(
                            stopsNearOriginNode.get(originStopCounter).getStopId(),
                            stopsNearDestinationNode.get(destinationStopCounter).getStopId(),
                            departureTime + costOriginToOriginNode +
                                    dijkstraArrivalTimesToOriginStops.get(originStopCounter),
                            routeStops, stopTimes, stops, stopRoutes, transfers).getTravelTimeMinutes();
                    double totalTravelTime = costOriginToOriginNode + dijkstraArrivalTimesToOriginStops.
                            get(originStopCounter) + nNSnappingCostsOfStopsNearOriginNode.get(originStopCounter) +
                            transitTime + nNSnappingCostsOfStopsNearDestinationNode.get(destinationStopCounter) +
                            costDestinationToDestinationNode;
                    if (totalTravelTime < leastTotalTravelTime) {
                        leastTotalTravelTime = totalTravelTime;
                        timeSpentInTransit = transitTime;
                        timeSpentFromOriginToOriginStop = costOriginToOriginNode + dijkstraArrivalTimesToOriginStops.
                                get(originStopCounter) + nNSnappingCostsOfStopsNearOriginNode.get(originStopCounter);
                        timeSpentFromDestinationStopToDestination = nNSnappingCostsOfStopsNearDestinationNode.
                                get(destinationStopCounter) + costDestinationToDestinationNode;
                        selectedOriginStopId = stopsNearOriginNode.get(originStopCounter).getStopId();
                        selectedDestinationStopId = stopsNearDestinationNode.get(destinationStopCounter).getStopId();
                    }
                }
            }
            String selectedOriginStopName = stops.get(selectedOriginStopId).getStopName();
            String selectedDestinationStopName = stops.get(selectedDestinationStopId).getStopName();

            // Print result
            System.out.println("Multi-modal routing query #" + routingQueryCount+ "\n" +
                    "Origin coordinates: (" + originLatitude + ", " + originLongitude + ")\n" +
                    "Destination coordinates: (" + destinationLatitude + ", " + destinationLongitude + "\n" +
                    "Departure time: " + (departureTime / 60) + ":" + (departureTime % 60) + "\n" +
                    "Number of transit stops considered around origin: " + stopsNearOriginNode.size() + "\n" +
                    "Number of transit stops considered around destination: " + stopsNearDestinationNode.size() + "\n" +
                    "Origin stop in solution: " + selectedOriginStopName + "(" + selectedOriginStopId + ")\n" +
                    "Destination stop in solution: " + selectedDestinationStopName + "(" + selectedDestinationStopId +
                    ")\n" +
                    "Travel time from origin to " + selectedOriginStopName + ": " + timeSpentFromOriginToOriginStop +
                    "minutes\n" +
                    "Travel time from " + selectedOriginStopName + " to " + selectedDestinationStopName + ": " +
                    timeSpentInTransit + "minutes\n" +
                    "Travel time from " + selectedDestinationStopName + " to destination: " +
                    timeSpentFromDestinationStopToDestination + "\n" +
                    "Total travel time: " + leastTotalTravelTime + "\n" );
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
        osmDataReaderWriterForDijkstra.contractNodesAndBuildShortcuts();    // This step is optional

        // Write out data used for the Dijkstra algorithm
        osmDataReaderWriterForDijkstra.writeDijkstraLinks(dijkstraLinksFilePath);
        osmDataReaderWriterForDijkstra.writeDijkstraNodes(dijkstraNodesFilePath);
    }
}
