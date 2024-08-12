package src.MultiModalRouter;

import java.util.*;

import org.jetbrains.annotations.NotNull;

import src.NearestNeighbourFinder.KDTreeForNodes;
import src.NearestNeighbourFinder.KDTreeForStops;
import src.PublicTransportRouter.GTFSDataManager.*;
import src.PublicTransportRouter.RoutingAlgorithm.RAPTOR;
import src.RoadTransportRouter.OSMDataManager.Link;
import src.RoadTransportRouter.OSMDataManager.Node;
import src.RoadTransportRouter.OSMDataManager.OSMDataReaderWriter;
import src.RoadTransportRouter.RoutingAlgorithm.DijkstraBasedRouter;

public class Caller {
    private static final long NUMBER_MULTI_MODAL_QUERIES = 100;
    private static final long NANOSECONDS_PER_MINUTE = 60_000_000_000L;
    private static final double MINIMUM_DRIVING_DISTANCE_M = 2_000;
    // Refer to: https://www.emerald.com/insight/content/doi/10.1108/SASBE-07-2017-0031/full/html
    private static final double MAXIMUM_DRIVING_DISTANCE_M = 10_000;
    private static final double AVERAGE_WALKING_SPEED_M_PER_MIN = 85.2;     // Translates to 1.4 m/s
    private static final double AVERAGE_DRIVING_SPEED_M_PER_MIN = 483.33;
    // (Source: https://www.tomtom.com/traffic-index/munich-traffic/); translates to approximately 29 km/h

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
                "OSM-OPL data processed in " + String.format("%.3f",
                osmDataProcessingDuration / NANOSECONDS_PER_MINUTE) + " minutes.");

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
                "Number of routeStop objects: "  + routeStops.size() + "\n" +
                "Number of stopTime objects: "  + stopTimes.size() + "\n" +
                "Number of stops: "  + stops.size() + "\n" +
                "Number of stopRoute objects: "  + stopRoutes.size() + "\n" +
                "Number of transfers: "  + transfers.size() + "\n" +
                "GTFS data processed in " + String.format("%.3f", gtfsDataProcessingDuration / NANOSECONDS_PER_MINUTE)
                + " minutes.");

        double kDTreesBuildDuration = (double) ((kDStopEndTime - kDStopStartTime) + (kDNodeEndTime - kDNodeStartTime));
        System.out.println("\n" +
                "KD-Trees for searching nearest nodes and stops built in " + String.format("%.3f",
                kDTreesBuildDuration / NANOSECONDS_PER_MINUTE) + " minutes.");

        /**
         * Instantiate routers and generate multi-modal queries
         */
        // Set up router instances
        DijkstraBasedRouter dijkstraBasedRouter = new DijkstraBasedRouter();
        RAPTOR rAPTOR = new RAPTOR();

        // Load and write all multi-modal queries using the generator
        long queryGenStartTime = System.nanoTime();
        String multiModalQueriesFilePath = "D:/Documents - Education + Work/Education - TUM/Year 2/Fourth Semester/" +
                "MasterThesis/Results/MultiModalQueriesMap/multiModalQueries.txt";
        MultiModalQueryGenerator multiModalQueryGenerator = new MultiModalQueryGenerator();
        LinkedHashMap<Long, MultiModalQuery> multiModalQueries = multiModalQueryGenerator.
                generateQueries(NUMBER_MULTI_MODAL_QUERIES);   // Method argument contains number of queries to generate
        multiModalQueryGenerator.writeMultiModalQueries(multiModalQueriesFilePath);
        long queryGenEndTime = System.nanoTime();

        double queryGenerationDuration = (double) (queryGenEndTime - queryGenStartTime);
        System.out.println("\n" +
                "Multi-modal queries for JavaMultiModalRouter created in " + String.format("%.3f",
                queryGenerationDuration / NANOSECONDS_PER_MINUTE) + " minutes.");

        /**
         * Execute few queries on the JMultiModalRouter architecture
         */
        for (HashMap.Entry<Long, MultiModalQuery> multiModalQueryEntry : multiModalQueries.entrySet()) {
            long queriesSolvingStartTime = System.nanoTime();
            // Parse query data
            MultiModalQuery multiModalQuery = multiModalQueryEntry.getValue();
            double originPointLongitude = multiModalQuery.getOriginLongitude();
            double originPointLatitude = multiModalQuery.getOriginLatitude();
            double destinationPointLongitude = multiModalQuery.getDestinationLongitude();
            double destinationPointLatitude = multiModalQuery.getDestinationLatitude();
            int originPointDepartureTime = multiModalQuery.getDepartureTime();

            // For origin node, get the nearest stops in a doughnut-search space and elucidate travel to each stop
            Node nodeNearOrigin = kDTreeForNodes.findNearestNode(originPointLongitude, originPointLatitude);
            ArrayList<Stop> stopsNearOriginNode = kDTreeForStops.findStopsWithinDoughnut(nodeNearOrigin.
                            getNodeLongitude(), nodeNearOrigin.getNodeLatitude(), MINIMUM_DRIVING_DISTANCE_M,
                    MAXIMUM_DRIVING_DISTANCE_M);

            if ((stopsNearOriginNode == null) || (stopsNearOriginNode.size() == 0)) {
                System.out.println("Skipping");
                continue;
            }

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

            ArrayList<Node> nodesNearOriginStops = new ArrayList<>();
            ArrayList<Double> travelTimesOriginToOriginStops = new ArrayList<>();
            for (Stop stopNearOriginNode : stopsNearOriginNode) {
                Node nodeNearOriginStop = kDTreeForNodes.findNearestNode(stopNearOriginNode.getStopLongitude(),
                        stopNearOriginNode.getStopLatitude());
                nodesNearOriginStops.add(nodeNearOriginStop);
                travelTimesOriginToOriginStops.add(((nodeNearOrigin.equiRectangularDistanceTo(originPointLongitude,
                        originPointLatitude) + nodeNearOriginStop.equiRectangularDistanceTo(stopNearOriginNode.
                        getStopLongitude(), stopNearOriginNode.getStopLatitude())) / AVERAGE_WALKING_SPEED_M_PER_MIN) +
                        (dijkstraBasedRouter.findShortestDrivingPathCostMin(nodeNearOrigin.getNodeId(),
                                nodeNearOriginStop.getNodeId(), nodes, links)));
            }

            // For destination point, get the nearest transit stop and elucidate travel between destination and stop
            Node nodeNearDestination = kDTreeForNodes.findNearestNode(destinationPointLongitude,
                    destinationPointLatitude);
            Stop stopNearestToDestinationNode = kDTreeForStops.findNearestStop(nodeNearDestination.getNodeLongitude(),
                    nodeNearDestination.getNodeLatitude());
            Node nodeNearDestinationStop = kDTreeForNodes.findNearestNode(stopNearestToDestinationNode.getStopLongitude(),
                    stopNearestToDestinationNode.getStopLatitude());
            double travelTimeDestinationToDestinationStop = ((nodeNearDestination.equiRectangularDistanceTo(
                    destinationPointLongitude, destinationPointLatitude) + nodeNearDestinationStop.
                    equiRectangularDistanceTo(stopNearestToDestinationNode.getStopLongitude(), stopNearestToDestinationNode.
                            getStopLatitude())) + (dijkstraBasedRouter.findShortestDrivingPathCostMin(
                    nodeNearDestinationStop.getNodeId(), nodeNearDestination.getNodeId(), nodes, links) *
                    AVERAGE_DRIVING_SPEED_M_PER_MIN)) / AVERAGE_WALKING_SPEED_M_PER_MIN;
            System.out.println("Stop near destination node: " + stopNearestToDestinationNode.getStopName());
            System.out.println("Travel time destination stop to destination: " + travelTimeDestinationToDestinationStop);

            long queriesSolvingEndTime = System.nanoTime();
            double queriesSolvingDuration = (double) (queriesSolvingEndTime - queriesSolvingStartTime);
            System.out.println("Queries solved in: " + (queriesSolvingDuration / NANOSECONDS_PER_MINUTE) + " minutes.");
        }
        // Find nodes close to origin and destination stops
        // ArrayList<Node> nodesNear
        System.exit(1);

    }


    // private ArrayList<Integer>





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
        osmDataReaderWriterForDijkstra.readAndFilterOsmNodes(osmOplExtractFilePath);
        osmDataReaderWriterForDijkstra.associateLinksWithNode();
        osmDataReaderWriterForDijkstra.calculateLinkTravelTimesMin();
        // osmDataReaderWriterForDijkstra.contractNodesAndBuildShortcuts();    // This step is optional

        // Write out data used for the Dijkstra algorithm
        osmDataReaderWriterForDijkstra.writeDijkstraLinks(dijkstraLinksFilePath);
        osmDataReaderWriterForDijkstra.writeDijkstraNodes(dijkstraNodesFilePath);
    }
}