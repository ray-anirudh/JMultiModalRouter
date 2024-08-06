package src.PublicTransportRouter.GTFSDataManager;
// GTFS: General Transit Feed Specification
// RAPTOR: Round-based Public Transit Router (Delling et. al., 2015)

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;

import src.NearestNeighbourFinder.KDTreeForNodes;
import src.RoadTransportRouter.OSMDataManager.Link;
import src.RoadTransportRouter.OSMDataManager.Node;
import src.RoadTransportRouter.OSMDataManager.OSMDataReaderWriter;
import src.RoadTransportRouter.RoutingAlgorithm.DijkstraBasedRouter;

public class GTFSDataReaderWriter {

    /**
     * ATTRIBUTE DEFINITIONS
     */

    private static final String[] AGENCY_ID_ARRAY = {
            "21",  // Stadtwerke München
            "40",  // Bayerische Regiobahn
            "54",  // DB Regio AG Bayern
            "62",  // DB Regio AG Südost
            "71",  // Bayerische Oberlandbahn
            "75",  // Go-Ahead Bayern GmbH
            "153", // DB RegioBus Bayern
            "173", // Regionalbus Ostbayern
            "215", // Münchner Verkehrs- und Tarifverbund
            "248", // Regionalverkehr Oberbayern
            "302", // DB RegioNetz Verkehrs GmbH Südostbayernbahn
            "390"  // Nahreisezug
    };

    // Set the constants
    private static final double STUDY_AREA_LATITUDE_MIN = 47.829752;
    private static final double STUDY_AREA_LATITUDE_MAX = 48.433757;
    private static final double STUDY_AREA_LONGITUDE_MIN = 10.962982;
    private static final double STUDY_AREA_LONGITUDE_MAX = 12.043762;
    private static final int MAXIMUM_TRANSFER_DISTANCE_M = 300;    // (Gritsch, 2024) and (Tischner, 2018)
    private static final double AVERAGE_WALKING_SPEED_M_PER_SEC = 1.4D;   // (Gritsch, 2024)
    private static final double AVERAGE_DRIVING_SPEED_M_PER_MIN = 483.33;
    // Refer to: https://www.tomtom.com/traffic-index/munich-traffic/; translates to approximately 29 km/h
    private static final int SECONDS_IN_MINUTE = 60;
    private static final int MINUTES_IN_HOUR = 60;
    private static final int MINUTES_IN_DAY = 1440;
    private static final String OSM_OPL_EXTRACT_FILE_PATH = "D:/Documents - Education + Work/Education - TUM/Year 2/" +
            "Fourth Semester/MasterThesis/Data/OSMDataMunich/Downloaded/planet_10.835,47.824_12.172,48.438.osm.opl/" +
            "BBBikeOSMExtract.opl";

    // Initialize the RAPTOR-relevant hashmaps
    // Key for "routes" hashmap refers to "route_id"
    private final LinkedHashMap<Integer, Route> routes = new LinkedHashMap<>();

    /* Key for "trips" hashmap also refers to "route_id" and value refers to a list of trip IDs, which is the linkage
    between "routes.txt" and "stop_times.txt" (Gritsch, 2024)
    */
    private final LinkedHashMap<Integer, Trip> trips = new LinkedHashMap<>();

    /* Key for "routeStops" hashmap also refers to "route_id" and value pertains to a direction-wise sequencing of stops
    on the route
    */
    private final LinkedHashMap<Integer, RouteStop> routeStops = new LinkedHashMap<>();

    // Key for "stopTimes" hashmap also refers to "route_id" and value refers to a hashmap of trip-wise stop time maps
    private final LinkedHashMap<Integer, StopTime> stopTimes = new LinkedHashMap<>();

    // Key for "stops" hashmap refers to "stop_id"
    private final LinkedHashMap<Integer, Stop> stops = new LinkedHashMap<>();

    // Key for "stopRoutes" hashmap also refers to "stop_id" and value refers to a list of all routes serving that stop
    private final LinkedHashMap<Integer, StopRoute> stopRoutes = new LinkedHashMap<>();

    /* Key for "transfers" hashmap also refers to "stop_id" and value pertains to a map of reachable stops and the time
    needed to reach them
    */
    private final LinkedHashMap<Integer, Transfer> transfers = new LinkedHashMap<>();

    /**
     * BEHAVIOUR DEFINITIONS
     * All readers and dataset manipulators below are for RAPTOR-relevant data, and sourced from GTFS files
     */

    // Build the "routes" hashmap
    public void readAndFilterGTFSRoutes(String gtfsRoutesFilePath) {
        try {
            // Reader for "routes.txt"
            BufferedReader gtfsRoutesReader = new BufferedReader(new FileReader(gtfsRoutesFilePath));
            String newline;

            // Set up header array and "agency_id" lookup set
            String[] routesHeaderArray = gtfsRoutesReader.readLine().split(",");
            int agencyIdIndex = findIndexInArray("agency_id", routesHeaderArray);
            int routeIdIndex = findIndexInArray("route_id", routesHeaderArray);
            int routeTypeIndex = findIndexInArray("route_type", routesHeaderArray);
            HashSet<String> agencyIdHashSet = new HashSet<>(Arrays.asList(AGENCY_ID_ARRAY));

            // Read body and process data
            while ((newline = gtfsRoutesReader.readLine()) != null) {
                String[] routeDataRecord = newline.split(",");
                String agencyId = routeDataRecord[agencyIdIndex];

                if (agencyIdHashSet.contains(agencyId)) {
                    int routeId = Integer.parseInt(routeDataRecord[routeIdIndex]);
                    int routeType = Integer.parseInt(routeDataRecord[routeTypeIndex]);

                    Route route = new Route();
                    route.setRouteType(routeType);
                    Trip trip = new Trip();
                    StopTime stopTime = new StopTime();
                    RouteStop routeStop = new RouteStop();

                    this.routes.put(routeId, route);
                    this.trips.put(routeId, trip);
                    this.stopTimes.put(routeId, stopTime);
                    this.routeStops.put(routeId, routeStop);
                }
            }
            System.out.println("Routes' data read from " + gtfsRoutesFilePath);

        } catch (FileNotFoundException fNFE) {
            System.out.println("File not found at specified path: " + gtfsRoutesFilePath);
        } catch (IOException iOE) {
            System.out.println("Input-output exception. Please check input file: " + gtfsRoutesFilePath);
        }
    }

    // Build the "trips" hashmap (for linkage purposes) and structure the "stopTimes" hashmap
    public void readAndFilterGTFSTrips(String gtfsTripsFilePath) {
        try {
            // Reader for "trips.txt"
            BufferedReader gtfsTripsReader = new BufferedReader(new FileReader(gtfsTripsFilePath));
            String newline;

            // Set up header array
            String[] tripsHeaderArray = gtfsTripsReader.readLine().split(",");
            int routeIdIndex = findIndexInArray("route_id", tripsHeaderArray);
            int tripIdIndex = findIndexInArray("trip_id", tripsHeaderArray);

            // Read body and process data
            while ((newline = gtfsTripsReader.readLine()) != null) {
                String[] tripDataRecord = newline.split(",");
                int routeId = Integer.parseInt(tripDataRecord[routeIdIndex]);

                if (this.routes.containsKey(routeId)) {
                    int tripId = Integer.parseInt(tripDataRecord[tripIdIndex]);
                    this.trips.get(routeId).getTripList().add(tripId);

                    LinkedHashMap<Integer, StopTimeTriplet> tripSpecificStopTimeMap = new LinkedHashMap<>();
                    this.stopTimes.get(routeId).getTripWiseStopTimeMaps().put(tripId, tripSpecificStopTimeMap);
                }
            }
            System.out.println("Trips' data read from " + gtfsTripsFilePath);

        } catch (FileNotFoundException fNFE) {
            System.out.println("File not found at specified path: " + gtfsTripsFilePath);
        } catch (IOException iOE) {
            System.out.println("Input-output exception. Please check input file: " + gtfsTripsFilePath);
        }

        // Pad each route with the number of trips
        for (int routeId : this.routes.keySet()) {
            this.routes.get(routeId).setNumberTrips(this.trips.get(routeId).getTripList().size());
        }
        System.out.println("Routes hashmap padded with data on number of trips");
    }

    // Build the unsorted "stopTimes" hashmap and structure the "stops", "stopRoutes", and "transfers" hashmaps
    public void readAndFilterGTFSStopTimes(String gtfsStopTimesFilePath) {
        try {
            // Reader for "stop_times.txt"
            BufferedReader gtfsStopTimesReader = new BufferedReader(new FileReader(gtfsStopTimesFilePath));
            String newline;

            // Set up header array and "trip_id" lookup set
            String[] stopTimesHeaderArray = gtfsStopTimesReader.readLine().split(",");
            int tripIdIndex = findIndexInArray("trip_id", stopTimesHeaderArray);
            int stopIdIndex = findIndexInArray("stop_id", stopTimesHeaderArray);
            int stopSequenceIndex = findIndexInArray("stop_sequence", stopTimesHeaderArray);
            int arrivalTimeIndex = findIndexInArray("arrival_time", stopTimesHeaderArray);
            int departureTimeIndex = findIndexInArray("departure_time", stopTimesHeaderArray);

            HashSet<Integer> tripIdHashSet = new HashSet<>();
            for (int routeId : this.trips.keySet()) {
                HashSet<Integer> routeSpecificTripIdSet = new HashSet<>(this.trips.get(routeId).getTripList());
                tripIdHashSet.addAll(routeSpecificTripIdSet);
            }

            // Read body and process data
            while ((newline = gtfsStopTimesReader.readLine()) != null) {
                String[] stopTimeDataRecord = newline.split(",");
                int tripId = Integer.parseInt(stopTimeDataRecord[tripIdIndex]);

                if (tripIdHashSet.contains(tripId)) {
                    int stopId = Integer.parseInt(stopTimeDataRecord[stopIdIndex]);

                    // gtfs.de indexing of stops begins at 0 in "stop_times.txt"; RAPTOR shall use 1
                    int stopSequence = Integer.parseInt(stopTimeDataRecord[stopSequenceIndex]) + 1;

                    // Midnight time wraparound errors exist in GTFS data
                    String arrivalTimeHourString = stopTimeDataRecord[arrivalTimeIndex].substring(0, 2);
                    String arrivalTimeMinuteString = stopTimeDataRecord[arrivalTimeIndex].substring(3, 5);
                    int arrivalTimeMinutes = (Integer.parseInt(arrivalTimeHourString) * MINUTES_IN_HOUR +
                            Integer.parseInt(arrivalTimeMinuteString)) % MINUTES_IN_DAY;

                    String departureTimeHourString = stopTimeDataRecord[departureTimeIndex].substring(0, 2);
                    String departureTimeMinuteString = stopTimeDataRecord[departureTimeIndex].substring(3, 5);
                    int departureTimeMinutes = (Integer.parseInt(departureTimeHourString) * MINUTES_IN_HOUR +
                            Integer.parseInt(departureTimeMinuteString)) % MINUTES_IN_DAY;

                    StopTimeTriplet stopTimeTriplet = new StopTimeTriplet(stopSequence, arrivalTimeMinutes,
                            departureTimeMinutes);

                    for (StopTime stopTime : this.stopTimes.values()) {
                        LinkedHashMap<Integer, LinkedHashMap<Integer, StopTimeTriplet>> tripWiseStopTimeMaps = stopTime.
                                getTripWiseStopTimeMaps();

                        if (tripWiseStopTimeMaps.containsKey(tripId)) {
                            tripWiseStopTimeMaps.get(tripId).put(stopId, stopTimeTriplet);
                        }
                    }

                    if (!this.stops.containsKey(stopId)) {
                        Stop stop = new Stop();
                        StopRoute stopRoute = new StopRoute();
                        Transfer transfer = new Transfer();
                        this.stops.put(stopId, stop);
                        this.stopRoutes.put(stopId, stopRoute);
                        this.transfers.put(stopId, transfer);
                    }
                }
            }
            System.out.println("Stop times' data read from " + gtfsStopTimesFilePath);

        } catch (FileNotFoundException fNFE) {
            System.out.println("File not found at the specified path: " + gtfsStopTimesFilePath);
        } catch (IOException iOE) {
            System.out.println("Input-output exception. Please check input file: " + gtfsStopTimesFilePath);
        }
    }

    // Sort the "stopTimes" hashmap
    public void sortStopTimes() {
        for (int routeId : this.stopTimes.keySet()) {
            // Get the StopTime instance that needs to be sorted internally
            StopTime stopTime = this.stopTimes.get(routeId);

            // Get the exact hashmap containing the integer-hashmap pairs of trip IDs and stop time triplet maps
            LinkedHashMap<Integer, LinkedHashMap<Integer, StopTimeTriplet>> tripWiseStopTimeMaps = stopTime.
                    getTripWiseStopTimeMaps();

            // Get the aforementioned integer-hashmap pairs into a list
            ArrayList<HashMap.Entry<Integer, LinkedHashMap<Integer, StopTimeTriplet>>> tripWiseStopTimeEntryList =
                    new ArrayList<>(tripWiseStopTimeMaps.entrySet());

            /* Sort the achieved arraylist of aforementioned integer-hashmap pairs using a comparator that gets every
            entry in the arraylist, then gets the value (HashMap<Integer, StopTimeTriplet>) pertaining to that entry,
            then gets the earliest StopTimeTriplet instance, and then compares using the departure time found there
            */
            tripWiseStopTimeEntryList.sort(Comparator.comparing(tripEntry -> tripEntry.getValue().values().iterator().
                    next().getDepartureTime()));

            // Get the sorted entries into a new hashmap
            LinkedHashMap<Integer, LinkedHashMap<Integer, StopTimeTriplet>> sortedTripWiseStopTimeMaps = new
                    LinkedHashMap<>();
            for (HashMap.Entry<Integer, LinkedHashMap<Integer, StopTimeTriplet>> tripSpecificStopTimeMap :
                    tripWiseStopTimeEntryList) {
                sortedTripWiseStopTimeMaps.put(tripSpecificStopTimeMap.getKey(), tripSpecificStopTimeMap.getValue());
            }

            // Create a StopTime instance that is sorted internally now
            StopTime sortedStopTime = new StopTime(sortedTripWiseStopTimeMaps);

            // Replace the old StopTime instance with the new sorted one
            this.stopTimes.replace(routeId, sortedStopTime);
        }
        System.out.println("Stop times' data sorted (route-specific trips ranked) using first stop's departure time");
    }

    // Complete the "routes" hashmap
    public void padGTFSRoutes() {
        for (HashMap.Entry<Integer, StopTime> stopTimeEntry : this.stopTimes.entrySet()) {
            int sizeOfTripWithMaxStops = -1;
            for (LinkedHashMap<Integer, StopTimeTriplet> tripSpecificStopTimeMap : stopTimeEntry.getValue().
                    getTripWiseStopTimeMaps().values()) {
                if (tripSpecificStopTimeMap.size() > sizeOfTripWithMaxStops) {
                    sizeOfTripWithMaxStops = tripSpecificStopTimeMap.size();
                }
            }
            this.routes.get(stopTimeEntry.getKey()).setNumberStops(sizeOfTripWithMaxStops);
        }
        System.out.println("Routes' hashmap padded with data on numbers of stops");
    }

    // Build the "routeStops" hashmap (carries most improvement potential for future research)
    public void padGTFSRouteStops() {
        for (HashMap.Entry<Integer, StopTime> stopTimeEntry : this.stopTimes.entrySet()) {
            LinkedHashMap<Integer, LinkedHashMap<Integer, StopTimeTriplet>> tripWiseStopTimeMaps = stopTimeEntry.
                    getValue().getTripWiseStopTimeMaps();
            int tripIdDirectionOne = -1;
            int tripIdDirectionTwo = -1;

            for (HashMap.Entry<Integer, LinkedHashMap<Integer, StopTimeTriplet>> tripConsideredForDirectionOne :
                    tripWiseStopTimeMaps.entrySet()) {
                if (tripConsideredForDirectionOne.getValue().size() == this.routes.get(stopTimeEntry.getKey()).
                        getNumberStops()) {
                    tripIdDirectionOne = tripConsideredForDirectionOne.getKey();
                    break;
                }
            }

            for (HashMap.Entry<Integer, LinkedHashMap<Integer, StopTimeTriplet>> tripConsideredForDirectionTwo :
                    tripWiseStopTimeMaps.entrySet()) {
                if ((tripConsideredForDirectionTwo.getValue().size() == this.routes.get(stopTimeEntry.getKey()).
                        getNumberStops()) && (!tripConsideredForDirectionTwo.getValue().keySet().toArray()[0].
                        equals(tripWiseStopTimeMaps.get(tripIdDirectionOne).keySet().toArray()[0]))) {
                    tripIdDirectionTwo = tripConsideredForDirectionTwo.getKey();
                    break;
                }
            }

            if (tripIdDirectionTwo == -1) {
                tripIdDirectionTwo = tripIdDirectionOne;    // Handling loop routes
            }

            LinkedHashMap<Integer, Integer> directionOneStopSequenceMap = new LinkedHashMap<>();
            for (HashMap.Entry<Integer, StopTimeTriplet> directionOneStopTimeEntry : tripWiseStopTimeMaps.
                    get(tripIdDirectionOne).entrySet()) {
                directionOneStopSequenceMap.put(directionOneStopTimeEntry.getKey(), directionOneStopTimeEntry.
                        getValue().getStopSequence());
            }

            LinkedHashMap<Integer, Integer> directionTwoStopSequenceMap = new LinkedHashMap<>();
            for (HashMap.Entry<Integer, StopTimeTriplet> directionTwoStopTimeEntry : tripWiseStopTimeMaps.
                    get(tripIdDirectionTwo).entrySet()) {
                directionTwoStopSequenceMap.put(directionTwoStopTimeEntry.getKey(), directionTwoStopTimeEntry.
                        getValue().getStopSequence());
            }

            this.routeStops.get(stopTimeEntry.getKey()).getDirectionWiseStopMaps().put(1, directionOneStopSequenceMap);
            this.routeStops.get(stopTimeEntry.getKey()).getDirectionWiseStopMaps().put(2, directionTwoStopSequenceMap);

            /* Debugging statements:
            System.out.println("Trip ID direction one: " + tripIdDirectionOne + "\n" +
                    "Trip size direction one: " + tripWiseStopTimeMaps.get(tripIdDirectionOne).size() + "\n" +
                    "Trip ID direction two: " + tripIdDirectionTwo + "\n" +
                    "Trip size direction two: " + tripWiseStopTimeMaps.get(tripIdDirectionTwo).size() + "\n" +
                    "Maximum size of pertinent route: " + this.routes.get(stopTimeEntry.getKey()).getNumberStops() +
                    "\n");
            */
        }
        System.out.println("Route stops' hashmap built");
    }

    // Build the "stops" hashmap
    public void readAndFilterGTFSStops(String gtfsStopsFilePath) {
        try {
            // Reader for "stops.txt"
            BufferedReader gtfsStopsReader = new BufferedReader(new FileReader(gtfsStopsFilePath));
            String newline;

            // Set up header array
            String[] stopsHeaderArray = gtfsStopsReader.readLine().split(",");
            int stopIdIndex = findIndexInArray("stop_id", stopsHeaderArray);
            int stopNameIndex = findIndexInArray("stop_name", stopsHeaderArray);
            int stopLatitudeIndex = findIndexInArray("stop_lat", stopsHeaderArray);
            int stopLongitudeIndex = findIndexInArray("stop_lon", stopsHeaderArray);

            // Read body and process data; do not avoid parent stations, as they might be visited by transit vehicles
            while ((newline = gtfsStopsReader.readLine()) != null) {
                int commasInStopRecord = countCommasInString(newline);
                final int COMMAS_NORMAL_STOP_RECORD = 5;
                int indexAdder = commasInStopRecord - COMMAS_NORMAL_STOP_RECORD;

                String[] stopDataRecord = newline.split(",");
                int stopId = Integer.parseInt(stopDataRecord[stopIdIndex + indexAdder]);

                if (this.stops.containsKey(stopId)) {
                    int stopType = -1;
                    int stopTripCount = 0;
                    double stopLatitude = Double.parseDouble(stopDataRecord[stopLatitudeIndex + indexAdder]);
                    double stopLongitude = Double.parseDouble(stopDataRecord[stopLongitudeIndex + indexAdder]);
                    String stopName = "";

                    if (indexAdder == 0) {
                        stopName = stopDataRecord[stopNameIndex + indexAdder];
                    } else {
                        for (int i = 0; i <= indexAdder; i++) {
                            stopName += stopDataRecord[i].replace("\"", "") + " ";
                        }
                    }

                    Stop stop = new Stop(stopId, stopName, stopType, stopTripCount, stopLongitude, stopLatitude);
                    this.stops.replace(stopId, stop);

                    /* Debugging statements:
                    System.out.println("Stop name: " + stopName + "\n" +
                            "Stop Latitude: " + stopLatitude + "\n" +
                            "Stop Longitude: " + stopLongitude + "\n");
                    */
                }
            }
            System.out.println("Stops' data read from " + gtfsStopsFilePath);

        } catch (FileNotFoundException nFNE) {
            System.out.println("File not found at the specified path: " + gtfsStopsFilePath);
        } catch (IOException iOE) {
            System.out.println("Input-output exception. Please check input file: " + gtfsStopsFilePath);
        }
    }

    // Build the "stopRoutes" hashmap and ascribe stop types and stop trip counts to stops
    public void padStopRoutes() {
        ArrayList<Integer> stopIds = new ArrayList<>(this.stopRoutes.keySet());
        for (int stopId : stopIds) {
            StopRoute stopSpecificRouteList = new StopRoute();

            for (HashMap.Entry<Integer, RouteStop> routeStopEntry : this.routeStops.entrySet()) {
                if ((routeStopEntry.getValue().getDirectionWiseStopMaps().get(1).containsKey(stopId)) ||
                        routeStopEntry.getValue().getDirectionWiseStopMaps().get(2).containsKey(stopId)) {

                    stopSpecificRouteList.getRouteList().add(routeStopEntry.getKey());
                    Route route = this.routes.get(routeStopEntry.getKey());
                    int routeType = route.getRouteType();
                    int routeTripCount = route.getNumberTrips();

                    Stop stop = this.stops.get(stopId);
                    stop.setStopType(routeType);
                    stop.setStopTripCount(stop.getStopTripCount() + routeTripCount);
                }
            }
            this.stopRoutes.replace(stopId, stopSpecificRouteList);

            if (this.stopRoutes.get(stopId).getRouteList().isEmpty()) {
                this.stops.remove(stopId);
                this.stopRoutes.remove(stopId);
                this.transfers.remove(stopId);
            }
        }
        System.out.println("Stop-wise routes' hashmap padded with route IDs, and stop types and trip counts ascribed");
    }

    // Build the "transfers" hashmap, ignoring pairs of distant stops
    public void buildTransfersHashMap() {
        ArrayList<Integer> fromStopIds = new ArrayList<>(this.transfers.keySet());
        for (int fromStopId : fromStopIds) {
            Transfer stopSpecificTransferMap = new Transfer();

            ArrayList<Integer> toStopIds = new ArrayList<>(this.transfers.keySet());
            for (int toStopId : toStopIds) {
                double toStopLongitude = this.stops.get(toStopId).getStopLongitude();
                double toStopLatitude = this.stops.get(toStopId).getStopLatitude();
                double interStopAerialDistanceM = this.stops.get(fromStopId).equiRectangularDistanceTo(
                        toStopLongitude, toStopLatitude);

                if (interStopAerialDistanceM <= MAXIMUM_TRANSFER_DISTANCE_M) {
                    /* Stops with identical latitude-longitude pairs are removed from each map, which is realistic to
                    avoid transfers at the very same stop; transfers are recorded in minutes
                    */
                    if (fromStopId != toStopId) {
                        double interStopAerialWalkingTimeMin = interStopAerialDistanceM /
                                (AVERAGE_WALKING_SPEED_M_PER_SEC * SECONDS_IN_MINUTE);
                        stopSpecificTransferMap.getTransferMap().put(toStopId, interStopAerialWalkingTimeMin);

                        /* Debugging statements:
                        System.out.println("From stop: " + this.stops.get(fromStopId).getStopName() + " " +
                                fromStopId + "\n" +
                                "To stop: " + this.stops.get(toStopId).getStopName() + " " + toStopId + "\n" +
                                "Inter-stop aerial distance: " + interStopAerialDistanceM + "\n" +
                                "Inter-stop aerial walking time: " + interStopAerialWalkingTimeMin + "\n");
                        */
                    }
                }
            }
            this.transfers.replace(fromStopId, stopSpecificTransferMap);
        }
        System.out.println("Transfers hashmap built (boundary conditions based on aerial distances)");
    }

    // Filter out unrealistic "transfers" based on transfer distances
    public void filterTransfersHashMap() {
        DijkstraBasedRouter dijkstraBasedRouter = new DijkstraBasedRouter();
        OSMDataReaderWriter osmDataReaderWriterForDijkstra = new OSMDataReaderWriter();
        getDijkstraMaps(osmDataReaderWriterForDijkstra);
        LinkedHashMap<Long, Node> nodes = osmDataReaderWriterForDijkstra.getNodes();
        LinkedHashMap<Long, Link> links = osmDataReaderWriterForDijkstra.getLinks();
        Node[] nodesForNNSearches = nodes.values().toArray(new Node[0]);

        KDTreeForNodes kDTreeForNodes = new KDTreeForNodes();
        kDTreeForNodes.buildNodeBasedKDTree(nodesForNNSearches);

        ArrayList<Integer> fromStopIds = new ArrayList<>(this.transfers.keySet());
        for (int fromStopId : fromStopIds) {
            LinkedHashMap<Integer, Double> stopSpecificTransferMap = this.transfers.get(fromStopId).getTransferMap();
            double fromStopLongitude = this.stops.get(fromStopId).getStopLongitude();
            double fromStopLatitude = this.stops.get(fromStopId).getStopLatitude();
            Node nearestNodeFromStop = kDTreeForNodes.findNearestNode(fromStopLongitude, fromStopLatitude);

            ArrayList<Integer> toStopIds = new ArrayList<>(stopSpecificTransferMap.keySet());
            for (int toStopId : toStopIds) {
                double toStopLongitude = this.stops.get(toStopId).getStopLongitude();
                double toStopLatitude = this.stops.get(toStopId).getStopLatitude();
                Node nearestNodeToStop = kDTreeForNodes.findNearestNode(toStopLongitude, toStopLatitude);

                double interStopWalkingDistanceM = nearestNodeFromStop.equiRectangularDistanceTo(fromStopLongitude,
                        fromStopLatitude) + nearestNodeToStop.equiRectangularDistanceTo(toStopLongitude,
                        toStopLatitude) + dijkstraBasedRouter.findShortestDrivingPath(nearestNodeFromStop.getNodeId(),
                        nearestNodeToStop.getNodeId(), nodes, links) * AVERAGE_DRIVING_SPEED_M_PER_MIN;

                if (interStopWalkingDistanceM <= MAXIMUM_TRANSFER_DISTANCE_M) {
                    double interStopWalkingTimeMin = interStopWalkingDistanceM / (AVERAGE_WALKING_SPEED_M_PER_SEC *
                            SECONDS_IN_MINUTE);
                    stopSpecificTransferMap.replace(toStopId, interStopWalkingTimeMin);

                    /* Debugging statements:
                    System.out.println("From stop: " + this.stops.get(fromStopId).getStopName() + " " + fromStopId +
                    "\n" +
                            "To stop: " + this.stops.get(toStopId).getStopName() + " " + toStopId + "\n" +
                            "Inter-stop walking distance: " + interStopWalkingDistanceM + "\n" +
                            "Inter-stop walking time: " + interStopWalkingTimeMin + "\n");
                    */
                } else {
                    stopSpecificTransferMap.remove(toStopId);
                }
            }
        }
        System.out.println("Unrealistic transfers based on walking distances filtered out");
    }

    // Filter out unrealistic "transfers" based on transfer distances
    public void filterTransfersHashMap2() {
        DijkstraBasedRouter dijkstraBasedRouter = new DijkstraBasedRouter();
        OSMDataReaderWriter osmDataReaderWriterForDijkstra = new OSMDataReaderWriter();
        getDijkstraMaps(osmDataReaderWriterForDijkstra);
        LinkedHashMap<Long, Node> nodes = osmDataReaderWriterForDijkstra.getNodes();
        LinkedHashMap<Long, Link> links = osmDataReaderWriterForDijkstra.getLinks();
        Node[] nodesForNNSearches = nodes.values().toArray(new Node[0]);

        KDTreeForNodes kDTreeForNodes = new KDTreeForNodes();
        kDTreeForNodes.buildNodeBasedKDTree(nodesForNNSearches);

        ArrayList<Integer> fromStopIds = new ArrayList<>(this.transfers.keySet());
        for (int fromStopId : fromStopIds) {
            LinkedHashMap<Integer, Double> stopSpecificTransferMap = this.transfers.get(fromStopId).getTransferMap();
            double fromStopLongitude = this.stops.get(fromStopId).getStopLongitude();
            double fromStopLatitude = this.stops.get(fromStopId).getStopLatitude();
            Node nearestNodeFromStop = kDTreeForNodes.findNearestNode(fromStopLongitude, fromStopLatitude);

            ArrayList<Integer> toStopIds = new ArrayList<>(stopSpecificTransferMap.keySet());
            for (int toStopId : toStopIds) {
                double toStopLongitude = this.stops.get(toStopId).getStopLongitude();
                double toStopLatitude = this.stops.get(toStopId).getStopLatitude();
                Node nearestNodeToStop = kDTreeForNodes.findNearestNode(toStopLongitude, toStopLatitude);

                double interStopWalkingDistanceM = nearestNodeFromStop.equiRectangularDistanceTo(fromStopLongitude,
                        fromStopLatitude) + nearestNodeToStop.equiRectangularDistanceTo(toStopLongitude,
                        toStopLatitude) + dijkstraBasedRouter.findShortestDrivingPath(nearestNodeFromStop.getNodeId(),
                        nearestNodeToStop.getNodeId(), nodes, links) * AVERAGE_DRIVING_SPEED_M_PER_MIN;

                if (interStopWalkingDistanceM <= MAXIMUM_TRANSFER_DISTANCE_M) {
                    double interStopWalkingTimeMin = interStopWalkingDistanceM / (AVERAGE_WALKING_SPEED_M_PER_SEC *
                            SECONDS_IN_MINUTE);
                    stopSpecificTransferMap.replace(toStopId, interStopWalkingTimeMin);

                    /* Debugging statements:
                    System.out.println("From stop: " + this.stops.get(fromStopId).getStopName() + " " + fromStopId +
                    "\n" +
                            "To stop: " + this.stops.get(toStopId).getStopName() + " " + toStopId + "\n" +
                            "Inter-stop walking distance: " + interStopWalkingDistanceM + "\n" +
                            "Inter-stop walking time: " + interStopWalkingTimeMin + "\n");
                    */
                } else {
                    stopSpecificTransferMap.remove(toStopId);
                }
            }
        }
        System.out.println("Unrealistic transfers based on walking distances filtered out");
    }

    // Make "transfers" hashmap transitive (consider a chain like fromStop-intermediateStop-toStop)
    public void makeTransfersTransitive() {
        DijkstraBasedRouter dijkstraBasedRouter = new DijkstraBasedRouter();
        OSMDataReaderWriter osmDataReaderWriterForDijkstra = new OSMDataReaderWriter();
        getDijkstraMaps(osmDataReaderWriterForDijkstra);
        LinkedHashMap<Long, Node> nodes = osmDataReaderWriterForDijkstra.getNodes();
        LinkedHashMap<Long, Link> links = osmDataReaderWriterForDijkstra.getLinks();
        Node[] nodesForNNSearches = nodes.values().toArray(new Node[0]);

        KDTreeForNodes kDTreeForNodes = new KDTreeForNodes();
        kDTreeForNodes.buildNodeBasedKDTree(nodesForNNSearches);

        ArrayList<Integer> fromStopIds = new ArrayList<>(this.transfers.keySet());
        for (int fromStopId : fromStopIds) {
            double fromStopLongitude = this.stops.get(fromStopId).getStopLongitude();
            double fromStopLatitude = this.stops.get(fromStopId).getStopLatitude();
            Node nearestNodeFromStop = kDTreeForNodes.findNearestNode(fromStopLongitude, fromStopLatitude);

            ArrayList<Integer> intermediateStopIds = new ArrayList<>(this.transfers.get(fromStopId).getTransferMap().
                    keySet());
            for (int intermediateStopId : intermediateStopIds) {

                ArrayList<Integer> toStopIds = new ArrayList<>(this.transfers.get(intermediateStopId).getTransferMap().
                        keySet());
                for (int toStopId : toStopIds) {
                    double toStopLongitude = this.stops.get(toStopId).getStopLongitude();
                    double toStopLatitude = this.stops.get(toStopId).getStopLatitude();
                    Node nearestNodeToStop = kDTreeForNodes.findNearestNode(toStopLongitude, toStopLatitude);

                    if (!this.transfers.get(fromStopId).getTransferMap().containsKey(toStopId)) {
                        double interStopWalkingDistanceM = nearestNodeFromStop.equiRectangularDistanceTo(fromStopLongitude,
                                fromStopLatitude) + nearestNodeToStop.equiRectangularDistanceTo(toStopLongitude,
                                toStopLatitude) + dijkstraBasedRouter.findShortestDrivingPath(nearestNodeFromStop.getNodeId(),
                                nearestNodeToStop.getNodeId(), nodes, links) * AVERAGE_DRIVING_SPEED_M_PER_MIN;

                        if (interStopWalkingDistanceM <= MAXIMUM_TRANSFER_DISTANCE_M) {
                            this.transfers.get(fromStopId).getTransferMap().put(toStopId, interStopWalkingDistanceM /
                                    (AVERAGE_WALKING_SPEED_M_PER_SEC * SECONDS_IN_MINUTE));
                        } else {
                            // Penalize unrealistic transfers
                            final double ARBITRARY_HIGH_TRANSFER_COST = 1_000_000D;
                            this.transfers.get(fromStopId).getTransferMap().put(toStopId, ARBITRARY_HIGH_TRANSFER_COST);
                        }
                    }
                }
            }
        }
        System.out.println("Transitivity of transfers established");
    }

    // Filter out all "stops" from outside the study area, and all allied data from the pertinent hashmaps
    public void filterHashMapsOnLatLong() {
        // For hashmaps "stops", "stopRoutes", and "transfers":
        for (Iterator<HashMap.Entry<Integer, Stop>> stopIterator = this.stops.entrySet().iterator(); stopIterator.
                hasNext(); ) {
            HashMap.Entry<Integer, Stop> stopEntry = stopIterator.next();
            if ((stopEntry.getValue().getStopLatitude() > STUDY_AREA_LATITUDE_MAX) ||
                    (stopEntry.getValue().getStopLatitude() < STUDY_AREA_LATITUDE_MIN) ||
                    (stopEntry.getValue().getStopLongitude() > STUDY_AREA_LONGITUDE_MAX) ||
                    (stopEntry.getValue().getStopLongitude() < STUDY_AREA_LONGITUDE_MIN)) {
                stopIterator.remove();
                this.stopRoutes.remove(stopEntry.getKey());
                this.transfers.remove(stopEntry.getKey());
            }
        }

        // For hashmaps "routes", "trips", "routeStops", and "stopTimes"
        for (Iterator<Integer> routeIterator = this.stopTimes.keySet().iterator(); routeIterator.hasNext(); ) {
            int routeId = routeIterator.next();
            LinkedHashMap<Integer, LinkedHashMap<Integer, StopTimeTriplet>> tripWiseStopTimeMaps = this.stopTimes.
                    get(routeId).getTripWiseStopTimeMaps();

            for (Iterator<Integer> tripIterator = this.trips.get(routeId).getTripList().iterator(); tripIterator.
                    hasNext(); ) {
                int tripId = tripIterator.next();

                for (Iterator<Integer> stopIterator = tripWiseStopTimeMaps.get(tripId).
                        keySet().iterator(); stopIterator.hasNext(); ) {
                    int stopId = stopIterator.next();

                    if (!this.stops.containsKey(stopId)) {
                        stopIterator.remove();
                        this.routeStops.get(routeId).getDirectionWiseStopMaps().get(1).remove(stopId);
                        this.routeStops.get(routeId).getDirectionWiseStopMaps().get(2).remove(stopId);
                    }
                }
                if (tripWiseStopTimeMaps.get(tripId).isEmpty()) {
                    tripIterator.remove();
                    this.stopTimes.get(routeId).getTripWiseStopTimeMaps().remove(tripId);
                }
            }
            if (tripWiseStopTimeMaps.isEmpty()) {
                routeIterator.remove();
                this.trips.remove(routeId);
                this.routes.remove(routeId);
                this.routeStops.remove(routeId);
                // Debugging advice: Try printing sizes of different maps for comparison and consistency checks
            }
        }
        System.out.println("Data external to study area deleted");
    }

    /**
     * All writers below are for datasets pertinent to RAPTOR, aligned with GTFS terminologies and standards
     */

    // Write a "routes.txt" file
    public void writeRaptorRoutes(String raptorRoutesFilePath) {
        try {
            // Writer for "routes.txt"
            BufferedWriter raptorRoutesWriter = new BufferedWriter(new FileWriter(raptorRoutesFilePath));

            // Set up header array
            raptorRoutesWriter.write("route_id,number_trips,number_stops,route_type\n");

            // Write body based on "routes" hashmap
            for (HashMap.Entry<Integer, Route> routeEntry : this.routes.entrySet()) {
                int routeId = routeEntry.getKey();
                int numberTrips = routeEntry.getValue().getNumberTrips();
                int numberStops = routeEntry.getValue().getNumberStops();
                int routeType = routeEntry.getValue().getRouteType();

                raptorRoutesWriter.write(routeId + "," + numberTrips + "," + numberStops + "," + routeType + "\n");
            }
            System.out.println("Routes' data written to " + raptorRoutesFilePath);

        } catch (IOException iOE) {
            System.out.println("Input-output exception. Please check the \"routes\" hashmap.");
        }
    }

    // Write a "routeStops.txt" file
    public void writeRaptorRouteStops(String raptorRouteStopsFilePath) {
        try {
            // Writer for "routeStops.txt"
            BufferedWriter raptorRouteStopsWriter = new BufferedWriter(new FileWriter(raptorRouteStopsFilePath));

            // Set up header array
            raptorRouteStopsWriter.write("route_id,direction_id,stop_id,stop_sequence\n");

            // Write body based on "routeStops" hashmap
            for (HashMap.Entry<Integer, RouteStop> routeStopEntry : this.routeStops.entrySet()) {
                int routeId = routeStopEntry.getKey();

                for (HashMap.Entry<Integer, LinkedHashMap<Integer, Integer>> directionSpecificStopSequenceMap :
                        routeStopEntry.getValue().getDirectionWiseStopMaps().entrySet()) {
                    int directionId = directionSpecificStopSequenceMap.getKey();

                    for (HashMap.Entry<Integer, Integer> stopSequenceEntry : directionSpecificStopSequenceMap.
                            getValue().entrySet()) {
                        int stopId = stopSequenceEntry.getKey();
                        int stopSequenceInRouteDirection = stopSequenceEntry.getValue();
                        raptorRouteStopsWriter.write(routeId + "," + directionId + "," + stopId + "," +
                                stopSequenceInRouteDirection + "\n");
                    }
                }
            }
            System.out.println("Route-wise stops' data written to " + raptorRouteStopsFilePath);

        } catch (IOException iOE) {
            System.out.println("Input-output exception. Please check the \"routeStops\" hashmap.");
        }
    }

    // Write a "trips.txt" file
    public void writeTrips(String tripsFilePath) {
        try {
            // Writer for "trips.txt"
            BufferedWriter tripsWriter = new BufferedWriter(new FileWriter(tripsFilePath));

            // Set up header array
            tripsWriter.write("route_id,trip_id\n");

            // Write body based on "trips" hashmap
            for (HashMap.Entry<Integer, Trip> tripEntry : this.trips.entrySet()) {
                int routeId = tripEntry.getKey();
                for (int tripId : tripEntry.getValue().getTripList()) {
                    tripsWriter.write(routeId + "," + tripId + "\n");
                }
            }
            System.out.println("Route-wise trips' data written to " + tripsFilePath);

        } catch (IOException iOE) {
            System.out.println("Input-output exception. Please check the \"trips\" hashmap.");
        }
    }

    // Write a "stopTimes.txt" file
    public void writeRaptorStopTimes(String raptorStopTimesFilePath) {
        try {
            // Writer for "stop_times.txt"
            BufferedWriter raptorStopTimesWriter = new BufferedWriter(new FileWriter(raptorStopTimesFilePath));

            // Set up header array
            raptorStopTimesWriter.write("route_id,trip_id,stop_sequence,stop_id,arrival_time,departure_time\n");

            // Write body based on "stopTimes" hashmap
            for (HashMap.Entry<Integer, StopTime> stopTimeEntry : this.stopTimes.entrySet()) {
                int routeId = stopTimeEntry.getKey();

                for (HashMap.Entry<Integer, LinkedHashMap<Integer, StopTimeTriplet>> tripSpecificStopTimeList :
                        stopTimeEntry.getValue().getTripWiseStopTimeMaps().entrySet()) {
                    int tripId = tripSpecificStopTimeList.getKey();

                    for (HashMap.Entry<Integer, StopTimeTriplet> stopTimeTriplet : tripSpecificStopTimeList.getValue().
                            entrySet()) {
                        int stopId = stopTimeTriplet.getKey();
                        int stopSequence = stopTimeTriplet.getValue().getStopSequence();
                        String arrivalTime = (stopTimeTriplet.getValue().getArrivalTime() % MINUTES_IN_DAY) /
                                MINUTES_IN_HOUR + ":" + (stopTimeTriplet.getValue().getArrivalTime() % MINUTES_IN_DAY) %
                                MINUTES_IN_HOUR;
                        String departureTime = (stopTimeTriplet.getValue().getDepartureTime() % MINUTES_IN_DAY) /
                                MINUTES_IN_HOUR + ":" + (stopTimeTriplet.getValue().getDepartureTime() %
                                MINUTES_IN_DAY) % MINUTES_IN_HOUR;

                        raptorStopTimesWriter.write(routeId + "," + tripId + "," + stopSequence + "," + stopId +
                                "," + arrivalTime + "," + departureTime + "\n");
                    }
                }
            }
            System.out.println("Route-wise stop times' data written to " + raptorStopTimesFilePath);

        } catch (IOException iOE) {
            System.out.println("Input-output exception. Please check the \"stopTimes\" hashmap.");
        }
    }

    // Write a "stops.txt" file
    public void writeRaptorStops(String raptorStopsFilePath) {
        try {
            // Writer for "stops.txt"
            BufferedWriter raptorStopsWriter = new BufferedWriter(new FileWriter(raptorStopsFilePath));

            // Set up header array
            raptorStopsWriter.write("stop_id,stop_name,location_type,stop_trip_count,stop_lon,stop_lat\n");

            // Write body based on "stops" hashmap
            for (HashMap.Entry<Integer, Stop> stopEntry : this.stops.entrySet()) {
                int stopId = stopEntry.getKey();
                String stopName = stopEntry.getValue().getStopName();
                int locationType = stopEntry.getValue().getStopType();
                int stopTripCount = stopEntry.getValue().getStopTripCount();
                double stopLongitude = stopEntry.getValue().getStopLongitude();
                double stopLatitude = stopEntry.getValue().getStopLatitude();

                raptorStopsWriter.write(stopId + "," + stopName + "," + locationType + "," + stopTripCount + "," +
                        stopLongitude + "," + stopLatitude + "\n");
            }
            System.out.println("Stops' data written to " + raptorStopsFilePath);

        } catch (IOException iOE) {
            System.out.println("Input-output exception. Please check the \"stops\" hashmap.");
        }
    }

    // Write a "stopRoutes.txt" file
    public void writeRaptorStopRoutes(String raptorStopRoutesFilePath) {
        try {
            // Writer for "stopRoutes.txt"
            BufferedWriter raptorStopRoutesWriter = new BufferedWriter(new FileWriter(raptorStopRoutesFilePath));

            // Set up header array
            raptorStopRoutesWriter.write("stop_id,stop_name,route_id\n");

            // Write body based on "stopRoutes" hashmap
            for (HashMap.Entry<Integer, StopRoute> stopRouteEntry : this.stopRoutes.entrySet()) {
                int stopId = stopRouteEntry.getKey();
                String stopName = this.stops.get(stopId).getStopName();

                for (int routeAtStop : stopRouteEntry.getValue().getRouteList()) {
                    raptorStopRoutesWriter.write(stopId + "," + stopName + "," + routeAtStop + "\n");
                }
            }
            System.out.println("Stop-wise routes' data written to " + raptorStopRoutesFilePath);

        } catch (IOException iOE) {
            System.out.println("Input-output exception. Please check the \"stopRoutes\" hashmap.");
        }
    }

    // Write a "transfers.txt" file
    public void writeRaptorTransfers(String raptorTransfersFilePath) {
        try {
            // Writer for "transfers.txt"
            BufferedWriter raptorTransfersWriter = new BufferedWriter(new FileWriter(raptorTransfersFilePath));

            // Set up header array
            raptorTransfersWriter.write("from_stop_id,to_stop_id,min_transfer_time\n");

            // Write body based on "transfers" hashmap
            for (HashMap.Entry<Integer, Transfer> transferEntry : this.transfers.entrySet()) {
                int fromStopId = transferEntry.getKey();

                for (HashMap.Entry<Integer, Double> transfer : transferEntry.getValue().getTransferMap().entrySet()) {
                    int toStopId = transfer.getKey();
                    double minTransferTime = transfer.getValue();

                    raptorTransfersWriter.write(fromStopId + "," + toStopId + "," + minTransferTime + "\n");
                }
            }
            System.out.println("Transfers' data written to " + raptorTransfersFilePath);

        } catch (IOException iOE) {
            System.out.println("Input-output exception. Please check the \"transfers\" hashmap.");
        }
    }

    /**
     * All supporting methods are below
     */

    // Index finder by column name strings
    private int findIndexInArray(String columnHeaderName, @NotNull String[] headerArray) {
        int columnPosition = -1;
        for (int i = 0; i < headerArray.length; i++) {
            if (headerArray[i].equalsIgnoreCase(columnHeaderName)) {
                columnPosition = i;
            }
        }
        return columnPosition;
    }

    // Comma counter in a string array
    private int countCommasInString(String string) {
        int commaCount = 0;
        for(int i = 0; i < string.length(); i++) {
            if (string.substring(i, i+1).equalsIgnoreCase(",")) {
                commaCount++;
            }
        }
        return commaCount;
    }

    // Get Dijkstra-relevant datasets ready
    private static void getDijkstraMaps(@NotNull OSMDataReaderWriter osmDataReaderWriterForDijkstra) {
        osmDataReaderWriterForDijkstra.readAndFilterOsmLinks(GTFSDataReaderWriter.OSM_OPL_EXTRACT_FILE_PATH);
        osmDataReaderWriterForDijkstra.readAndFilterOsmNodes(GTFSDataReaderWriter.OSM_OPL_EXTRACT_FILE_PATH);
        osmDataReaderWriterForDijkstra.associateLinksWithNode();
        osmDataReaderWriterForDijkstra.calculateLinkTravelTimesMin();
    }

    // Getters of transit timetable data for RAPTOR queries
    public LinkedHashMap<Integer, Route> getRoutes() { return this.routes; }
    public LinkedHashMap<Integer, Trip> getTrips() { return this.trips; }
    public LinkedHashMap<Integer, RouteStop> getRouteStops() { return this.routeStops; }
    public LinkedHashMap<Integer, StopTime> getStopTimes() { return this.stopTimes; }
    public LinkedHashMap<Integer, Stop> getStops() { return this.stops; }
    public LinkedHashMap<Integer, StopRoute> getStopRoutes() { return this.stopRoutes; }
    public LinkedHashMap<Integer, Transfer> getTransfers() { return this.transfers; }
}