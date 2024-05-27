package src.PublicTransportRouter.GTFSDataManager;
// GTFS: General Transit Feed Specification
// RAPTOR: Round-based Public Transit Router (Delling et. al., 2015)

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;

import java.io.*;
import java.util.*;

public class GTFSDataReaderWriter {

    /**
     * ATTRIBUTE DEFINITIONS
     */

    private static final String[] AGENCY_ID_ARRAY = {
            "21",  // Stadtwerke München
            "62",  // DB Regio AG Südost
            "390", // Nahreisezug
            "54",  // DB Regio AG Bayern
            "75",  // Go-Ahead Bayern GmbH
            "302"  // DB RegioNetz Verkehrs GmbH Südostbayernbahn
    };

    // Initialize GMaps API context with the API key of "Anirudh Ray"
    private final GeoApiContext googleGeoApiContext = new GeoApiContext.Builder().
            apiKey("Your Google API Key").build();

    // Set the constants
    private final double STUDY_AREA_LATITUDE_MIN = 47.829752;
    private final double STUDY_AREA_LATITUDE_MAX = 48.433757;
    private final double STUDY_AREA_LONGITUDE_MIN = 10.962982;
    private final double STUDY_AREA_LONGITUDE_MAX = 12.043762;
    private final int EARTH_RADIUS_M = 6371000;
    private final int MAXIMUM_TRANSFER_DISTANCE_M = 300;    // (Gritsch, 2024) and (Tischner, 2018)
    private final double AVERAGE_WALKING_SPEED_MPS = 1.4;   // (Gritsch, 2024)
    private final double SECONDS_IN_MINUTES = 60;

    // Initialize the RAPTOR-relevant hashmaps
    private HashMap<Integer, Route> routes = new HashMap<>();
    // Key for "routes" hashmap refers to "route_id"

    private HashMap<Integer, Trip> trips = new HashMap<>();
    /* Key for "trips" hashmap also refers to "route_id" and value refers to a list of trip IDs, which is the linkage
    between "routes.txt" and "stop_times.txt" (Gritsch, 2024)
    */

    private HashMap<Integer, RouteStop> routeStops = new HashMap<>();
    // Key for "routeStops" hashmap also refers to "route_id" and value pertains to a sequencing of stops on the route

    private HashMap<Integer, StopTime> stopTimes = new HashMap<>();
    // Key for "stopTimes" hashmap also refers to "route_id" and value refers to a hashmap of trip-wise stop time lists

    private HashMap<Integer, Stop> stops = new HashMap<>();
    // Key for "stops" hashmap refers to "stop_id"

    private HashMap<Integer, StopRoute> stopRoutes = new HashMap<>();
    // Key for "stopRoutes" hashmap also refers to "stop_id" and value refers to list of all routes serving that stop

    private HashMap<Integer, Transfer> transfers = new HashMap<>();
    /* Key for "transfers" hashmap also refers to "stop_id" and value pertains to a map of reachable stops and the time
    needed to reach them
    */

    /**
     * BEHAVIOUR DEFINITIONS
     */

    /**
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

                    ArrayList<StopTimeQuartet> tripSpecificStopTimeList = new ArrayList<>();
                    this.stopTimes.get(routeId).getTripWiseStopTimeLists().put(tripId, tripSpecificStopTimeList);
                }
            }
            System.out.println("Trips' data read from " + gtfsTripsFilePath);

        } catch (FileNotFoundException fNFE) {
            System.out.println("File not found at specified path: " + gtfsTripsFilePath);
        } catch (IOException iOE) {
            System.out.println("Input-output exception. Please check input file: " + gtfsTripsFilePath);
        }
    }

    // Build the unsorted "stopTimes" hashmap
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
            for (Integer routeId : this.trips.keySet()) {
                HashSet<Integer> routeSpecificTripIdHashSet = new HashSet<>(this.trips.get(routeId).getTripList());
                tripIdHashSet.addAll(routeSpecificTripIdHashSet);
            }

            // Read body and process data
            while ((newline = gtfsStopTimesReader.readLine()) != null) {
                String[] stopTimeDataRecord = newline.split(",");
                int tripId = Integer.parseInt(stopTimeDataRecord[tripIdIndex]);
                if (tripIdHashSet.contains(tripId)) {
                    int stopId = Integer.parseInt(stopTimeDataRecord[stopIdIndex]);

                    // gtfs.de indexing of stops begins at 0 in "stop_times.txt"; we will use 1 for RAPTOR
                    int stopSequence = Integer.parseInt(stopTimeDataRecord[stopSequenceIndex]) + 1;

                    String arrivalTimeHourString = stopTimeDataRecord[arrivalTimeIndex].substring(0, 2);
                    String arrivalTimeMinuteString = stopTimeDataRecord[arrivalTimeIndex].substring(3, 5);
                    int arrivalTimeMinutes = Integer.parseInt(arrivalTimeHourString) * 60 +
                            Integer.parseInt(arrivalTimeMinuteString);

                    String departureTimeHourString = stopTimeDataRecord[departureTimeIndex].substring(0, 2);
                    String departureTimeMinuteString = stopTimeDataRecord[departureTimeIndex].substring(3, 5);
                    int departureTimeMinutes = Integer.parseInt(departureTimeHourString) * 60 +
                            Integer.parseInt(departureTimeMinuteString);

                    StopTimeQuartet stopTimeQuartet = new StopTimeQuartet(stopSequence, stopId, arrivalTimeMinutes,
                            departureTimeMinutes);

                    for (int routeId : this.stopTimes.keySet()) {
                        HashMap<Integer, ArrayList<StopTimeQuartet>> tripWiseStopTimeLists = this.stopTimes.
                                get(routeId).getTripWiseStopTimeLists();

                        if (tripWiseStopTimeLists.containsKey(tripId)) {
                            tripWiseStopTimeLists.get(tripId).add(stopTimeQuartet);
                        }
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

            // Get the exact hashmap containing the integer-object pairs of trip IDs and stop time quartet lists
            HashMap<Integer, ArrayList<StopTimeQuartet>> tripWiseStopTimeLists = stopTime.getTripWiseStopTimeLists();

            // Get the aforementioned integer-object pairs into a list
            ArrayList<HashMap.Entry<Integer, ArrayList<StopTimeQuartet>>> tripWiseStopTimeEntryList =
                    new ArrayList<>(tripWiseStopTimeLists.entrySet());

            /* Sort the achieved arraylist of aforementioned integer-object pairs using a comparator that gets every
            entry in the arraylist, then gets the value (ArrayList<StopTimeQuartet>) pertaining to that entry, then gets
            the earliest StopTimeQuartet instance, and then compares using the departure time found there
            */
            tripWiseStopTimeEntryList.sort(Comparator.comparing(entry -> entry.getValue().get(0).getDepartureTime()));

            // Get the sorted entries into a new hashmap
            HashMap<Integer, ArrayList<StopTimeQuartet>> sortedTripWiseStopTimeLists = new HashMap<>();
            for (HashMap.Entry<Integer, ArrayList<StopTimeQuartet>> tripSpecificStopTimeList :
                    tripWiseStopTimeEntryList) {
                sortedTripWiseStopTimeLists.put(tripSpecificStopTimeList.getKey(),
                        tripSpecificStopTimeList.getValue());
            }

            // Create a StopTime instance that is sorted internally now
            StopTime sortedStopTime = new StopTime(sortedTripWiseStopTimeLists);

            // Replace the old StopTime instance with the new sorted one
            this.stopTimes.replace(routeId, sortedStopTime);
        }
        System.out.println("Stop times' data sorted (route-specific trips ranked) by first stop's departure time");
    }

    // Build the "routes" hashmap (route types have been addressed already)
    public void padGTFSRoutes() {
        for (int routeId : this.routes.keySet()) {
            this.routes.get(routeId).setNumberTrips(this.stopTimes.get(routeId).getTripWiseStopTimeLists().size());
            int firstTripInRouteId = new ArrayList<>(this.stopTimes.get(routeId).getTripWiseStopTimeLists().
                    keySet()).get(0);
            this.routes.get(routeId).setNumberStops(this.stopTimes.get(routeId).getTripWiseStopTimeLists().
                    get(firstTripInRouteId).size());
        }
        System.out.println("Routes' hashmap padded with data on numbers of trips and stops");
    }

    // Build the "routeStops" hashmap and structure the "stops" hashmap
    public void padGTFSRouteStops() {
        for (int routeId : this.stopTimes.keySet()) {
            StopTime tripWiseStopTimeLists = this.stopTimes.get(routeId);
            int firstTripInRouteId = new ArrayList<>(this.stopTimes.get(routeId).getTripWiseStopTimeLists().keySet()).
                    get(0);
            ArrayList<StopTimeQuartet> stopTimeQuartetList = tripWiseStopTimeLists.getTripWiseStopTimeLists().
                    get(firstTripInRouteId);

            for (StopTimeQuartet stopTimeQuartet : stopTimeQuartetList) {
                this.routeStops.get(routeId).getStopSequenceMap().put(stopTimeQuartet.getStopId(),
                        stopTimeQuartet.getStopSequence());

                if (!this.stops.containsKey(stopTimeQuartet.getStopId())) {
                    Stop stop = new Stop();
                    StopRoute stopRoute = new StopRoute();
                    Transfer transfer = new Transfer();
                    this.stops.put(stopTimeQuartet.getStopId(), stop);
                    this.stopRoutes.put(stopTimeQuartet.getStopId(), stopRoute);
                    this.transfers.put(stopTimeQuartet.getStopId(), transfer);
                }
            }
        }
        System.out.println("Route stops' hashmap padded with stop IDs and sequences, and stops' hashmap structured");
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
            int stopTypeIndex = findIndexInArray("location_type", stopsHeaderArray);
            int stopLatitudeIndex = findIndexInArray("stop_lat", stopsHeaderArray);
            int stopLongitudeIndex = findIndexInArray("stop_lon", stopsHeaderArray);

            // Read body and process data
            while ((newline = gtfsStopsReader.readLine()) != null) {
                String[] stopDataRecord = newline.split(",");
                int stopId = Integer.parseInt(stopDataRecord[stopIdIndex]);

                if (this.stops.containsKey(stopId)) {
                    String stopName = stopDataRecord[stopNameIndex];
                    int stopType = ((stopDataRecord[stopTypeIndex].isEmpty()) ? 0 : Integer.
                            parseInt(stopDataRecord[stopTypeIndex]));
                    double stopLatitude = Double.parseDouble(stopDataRecord[stopLatitudeIndex]);
                    double stopLongitude = Double.parseDouble(stopDataRecord[stopLongitudeIndex]);

                    Stop stop = new Stop(stopName, stopType, stopLatitude, stopLongitude);
                    this.stops.replace(stopId, stop);
                }
            }
            System.out.println("Stops' data read from " + gtfsStopsFilePath);

        } catch (FileNotFoundException nFNE) {
            System.out.println("File not found at the specified path: " + gtfsStopsFilePath);
        } catch (IOException iOE) {
            System.out.println("Input-output exception. Please check input file: " + gtfsStopsFilePath);
        }
    }

    // Build the "stopRoutes" hashmap
    public void padStopRoutes() {
        for (int stopId : this.stopRoutes.keySet()) {
            StopRoute stopSpecificRouteIds = new StopRoute();

            for (int routeId : this.routeStops.keySet()) {
                if (this.routeStops.get(routeId).getStopSequenceMap().containsKey(stopId)) {
                    stopSpecificRouteIds.getRouteIdList().add(routeId);
                }
            }
            this.stopRoutes.replace(stopId, stopSpecificRouteIds);
        }
        System.out.println("Stop-wise routes' hashmap padded with route IDs");
    }

    // Build the "transfers" hashmap, ignoring pairs of distant stops
    public void buildTransfersHashMap() {
        for (int fromStopId : this.transfers.keySet()) {
            double fromStopLatitudeRadians = Math.toRadians(this.stops.get(fromStopId).getStopLatitude());
            double fromStopLongitudeRadians = Math.toRadians(this.stops.get(fromStopId).getStopLongitude());
            Transfer stopSpecificTransferMap = new Transfer();

            for (int toStopId : this.transfers.keySet()) {
                double toStopLatitudeRadians = Math.toRadians(this.stops.get(toStopId).getStopLatitude());
                double toStopLongitudeRadians = Math.toRadians(this.stops.get(toStopId).getStopLongitude());

                double latitudeDifference = toStopLatitudeRadians - fromStopLatitudeRadians;
                double longitudeDifference = toStopLongitudeRadians - fromStopLongitudeRadians;

                double x = longitudeDifference * Math.cos((fromStopLatitudeRadians + toStopLatitudeRadians) / 2);
                double y = latitudeDifference;
                double interStopAerialDistanceM = Math.sqrt(x * x + y * y) * EARTH_RADIUS_M;

                if (interStopAerialDistanceM <= MAXIMUM_TRANSFER_DISTANCE_M) {
                    /* Stops with identical latitude-longitude pairs are removed from each map, which is realistic to
                    avoid transfers at the very same stop
                    */
                    if (fromStopId != toStopId) {
                        stopSpecificTransferMap.getTransferMap().put(toStopId, interStopAerialDistanceM /
                                (AVERAGE_WALKING_SPEED_MPS * SECONDS_IN_MINUTES));
                    }
                }
            }
            this.transfers.put(fromStopId, stopSpecificTransferMap);
        }
        System.out.println("Transfers hashmap built (time limitation based on equi-rectangular distances)");
    }

    // Filter out unrealistic "transfers" based on GMaps API calls
    public void filterTransfersHashMap() {
        for (int fromStopId : this.transfers.keySet()) {
            HashMap<Integer, Double> stopSpecificTransferMap = this.transfers.get(fromStopId).getTransferMap();
            double fromStopLatitude = this.stops.get(fromStopId).getStopLatitude();
            double fromStopLongitude = this.stops.get(fromStopId).getStopLongitude();

            for (int toStopId : stopSpecificTransferMap.keySet()) {
                double toStopLatitude = this.stops.get(toStopId).getStopLatitude();
                double toStopLongitude = this.stops.get(toStopId).getStopLongitude();
                double interStopWalkingDistanceM = calculateWalkingDistance(fromStopLatitude, fromStopLongitude,
                        toStopLatitude, toStopLongitude);

                if (interStopWalkingDistanceM <= MAXIMUM_TRANSFER_DISTANCE_M) {
                    stopSpecificTransferMap.replace(toStopId, interStopWalkingDistanceM / (AVERAGE_WALKING_SPEED_MPS *
                            SECONDS_IN_MINUTES));
                } else {
                    stopSpecificTransferMap.remove(toStopId);
                }
            }
        }
        System.out.println("Unrealistic transfers based on walking distances filtered out");
    }

    // Filter out all "stops" from outside the study area, and all allied data from the pertinent hashmaps
    public void filterHashMapsOnLatLong() {
        // For hashmaps "stops", "stopRoutes", and "transfers":
        for (HashMap.Entry<Integer, Stop> stopEntry : this.stops.entrySet()) {
            if ((stopEntry.getValue().getStopLatitude() > STUDY_AREA_LATITUDE_MAX) ||
                    (stopEntry.getValue().getStopLatitude() < STUDY_AREA_LATITUDE_MIN) ||
                    (stopEntry.getValue().getStopLongitude() > STUDY_AREA_LONGITUDE_MAX) ||
                    (stopEntry.getValue().getStopLongitude() < STUDY_AREA_LONGITUDE_MIN)) {
                this.stops.remove(stopEntry.getKey());
                this.stopRoutes.remove(stopEntry.getKey());
                this.transfers.remove(stopEntry.getKey());
            }
        }

        // For hashmaps "routes", "trips", "routeStops", and "stopTimes"
        for (int routeId : this.stopTimes.keySet()) {
            for (int tripId : this.stopTimes.get(routeId).getTripWiseStopTimeLists().keySet()) {
                this.stopTimes.get(routeId).getTripWiseStopTimeLists().get(tripId).removeIf(stopTimeQuartet ->
                        !this.stops.containsKey(stopTimeQuartet.getStopId()));

                if (this.stopTimes.get(routeId).getTripWiseStopTimeLists().get(tripId).isEmpty()) {
                    this.stopTimes.get(routeId).getTripWiseStopTimeLists().remove(tripId);
                    this.trips.get(routeId).getTripList().remove(tripId);
                }
            }

            if (this.stopTimes.get(routeId).getTripWiseStopTimeLists().isEmpty()) {
                this.stopTimes.remove(routeId);
                this.trips.remove(routeId);
                this.routes.remove(routeId);
            }
        }
        System.out.println("Data external to study area deleted");
    }

    // Make "transfers" hashmap transitive (consider a chain like fromStop-intermediateStop-toStop)
    public void makeTransfersTransitive() {
        for (int fromStopId : this.transfers.keySet()) {
            for (int intermediateStopId : this.transfers.get(fromStopId).getTransferMap().keySet()) {
                for (int toStopId : this.transfers.get(intermediateStopId).getTransferMap().keySet()) {
                    if (!this.transfers.get(fromStopId).getTransferMap().containsKey(toStopId)) {
                        double interStopWalkingDistanceM = calculateWalkingDistance(this.stops.get(fromStopId).
                                getStopLatitude(), this.stops.get(fromStopId).getStopLongitude(), this.stops.
                                get(toStopId).getStopLatitude(), this.stops.get(toStopId).getStopLongitude());
                        if (interStopWalkingDistanceM <= MAXIMUM_TRANSFER_DISTANCE_M) {
                            this.transfers.get(fromStopId).getTransferMap().put(toStopId, interStopWalkingDistanceM /
                                    (AVERAGE_WALKING_SPEED_MPS * SECONDS_IN_MINUTES));
                        } else {
                            this.transfers.get(fromStopId).getTransferMap().put(toStopId, Double.MAX_VALUE);
                        }
                    }
                }
            }
        }
        System.out.println("Transitivity of transfers established");
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
                int numberOfTrips = routeEntry.getValue().getNumberTrips();
                int numberOfStops = routeEntry.getValue().getNumberStops();
                int routeType = routeEntry.getValue().getRouteType();

                raptorRoutesWriter.write(routeId + "," + numberOfTrips + "," + numberOfStops + "," + routeType +
                        "\n");
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
            raptorRouteStopsWriter.write("route_id,stop_id,stop_sequence\n");

            // Write body based on "routeStops" hashmap
            for (HashMap.Entry<Integer, RouteStop> routeStopsMap : this.routeStops.entrySet()) {
                int routeId = routeStopsMap.getKey();
                for (HashMap.Entry<Integer, Integer> routeStopEntry : routeStopsMap.getValue().getStopSequenceMap().
                        entrySet()) {
                    int stopId = routeStopEntry.getKey();
                    int stopSequenceInRoute = routeStopEntry.getValue();

                    raptorRouteStopsWriter.write(routeId + "," + stopId + "," + stopSequenceInRoute + "\n");
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
            for (HashMap.Entry<Integer, Trip> tripListEntry : this.trips.entrySet()) {
                int routeId = tripListEntry.getKey();
                for (int tripId : tripListEntry.getValue().getTripList()) {
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
            for (HashMap.Entry<Integer, StopTime> tripWiseStopTimeLists : this.stopTimes.entrySet()) {
                int routeId = tripWiseStopTimeLists.getKey();

                for (HashMap.Entry<Integer, ArrayList<StopTimeQuartet>> tripSpecificStopTimeList : tripWiseStopTimeLists
                        .getValue().getTripWiseStopTimeLists().entrySet()) {
                    int tripId = tripSpecificStopTimeList.getKey();

                    for (StopTimeQuartet stopTimeQuartet : tripSpecificStopTimeList.getValue()) {
                        int stopSequence = stopTimeQuartet.getStopSequence();
                        int stopId = stopTimeQuartet.getStopId();
                        String arrivalTime = stopTimeQuartet.getArrivalTime() / 60 + ":" +
                                stopTimeQuartet.getArrivalTime() % 60;
                        String departureTime = stopTimeQuartet.getDepartureTime() / 60 + ":" +
                                stopTimeQuartet.getDepartureTime() % 60;

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
            raptorStopsWriter.write("stop_id,stop_name,location_type,stop_lat,stop_lon\n");

            // Write body based on "stops" hashmap
            for (HashMap.Entry<Integer, Stop> stop : this.stops.entrySet()) {
                int stopId = stop.getKey();
                String stopName = stop.getValue().getStopName();
                int locationType = stop.getValue().getStopType();
                double stopLatitude = stop.getValue().getStopLatitude();
                double stopLongitude = stop.getValue().getStopLongitude();

                raptorStopsWriter.write(stopId + "," + stopName + "," + locationType + "," + stopLatitude + "," +
                        stopLongitude + "\n");
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
            raptorStopRoutesWriter.write("stop_id,route_id\n");

            // Write body based on "stopRoutes" hashmap
            for (HashMap.Entry<Integer, StopRoute> stopSpecificRoutesList : this.stopRoutes.entrySet()) {
                int stopId = stopSpecificRoutesList.getKey();

                for (int routeIdOperatingAtStop : stopSpecificRoutesList.getValue().getRouteIdList()) {
                    raptorStopRoutesWriter.write(stopId + "," + routeIdOperatingAtStop + "\n");
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
            for (HashMap.Entry<Integer, Transfer> transferMap : this.transfers.entrySet()) {
                int fromStopId = transferMap.getKey();

                for (HashMap.Entry<Integer, Double> transfer : transferMap.getValue().getTransferMap().entrySet()) {
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

    // Walking distance calculator based on GMaps API
    private double calculateWalkingDistance(double fromStopLatitude, double fromStopLongitude, double toStopLatitude,
                                            double toStopLongitude) {
        try {
            // Query GMaps Directions API for walking route
            DirectionsResult result = DirectionsApi.newRequest(this.googleGeoApiContext)
                    .origin(new com.google.maps.model.LatLng(fromStopLatitude, fromStopLongitude))
                    .destination(new com.google.maps.model.LatLng(toStopLatitude, toStopLongitude))
                    .mode(TravelMode.WALKING)
                    .await();

            // Extract and return walking distance from the result
            return result.routes[0].legs[0].distance.inMeters;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    // Index finder by column name strings
    private int findIndexInArray(String columnHeaderName, String[] headerArray) {
        int columnPosition = -1;
        for (int i = 0; i <= headerArray.length; i++) {
            if (headerArray[i].equalsIgnoreCase(columnHeaderName)) {
                columnPosition = i;
            }
        }
        return columnPosition;
    }

    // Getters of transit timetable data for RAPTOR queries
    public HashMap<Integer, Route> getRoutes() {
        return this.routes;
    }

    public HashMap<Integer, Trip> getTrips() {
        return this.trips;
    }

    public HashMap<Integer, RouteStop> getRouteStops() {
        return this.routeStops;
    }

    public HashMap<Integer, StopTime> getStopTimes() {
        return this.stopTimes;
    }

    public HashMap<Integer, Stop> getStops() {
        return this.stops;
    }

    public HashMap<Integer, StopRoute> getStopRoutes() {
        return this.stopRoutes;
    }

    public HashMap<Integer, Transfer> getTransfers() {
        return this.transfers;
    }
}