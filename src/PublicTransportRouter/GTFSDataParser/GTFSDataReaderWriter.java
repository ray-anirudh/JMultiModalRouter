package src.PublicTransportRouter.GTFSDataParser;

// TODO: BUILD A WRITER

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;

import java.io.*;
import java.util.*;

public class GTFSDataReaderWriter {
    private static final String[] AGENCY_ID_ARRAY = {
            "21",  // Stadtwerke München
            "62",  // DB Regio AG Südost
            "390", // Nahreisezug
            "54",  // DB Regio AG Bayern
            "75",  // Go-Ahead Bayern GmbH
            "302"  // DB RegioNetz Verkehrs GmbH Südostbayernbahn
    };

    // Initialize GMaps API context with the API key of "Anirudh Ray"
    private GeoApiContext googleGeoApiContext = new GeoApiContext.Builder().
            apiKey("Your Google API Key").build();

    // Set the constants
    private final int EARTH_RADIUS_M = 6371000;
    private final int MAXIMUM_TRANSFER_DISTANCE_M = 250;    // (Gritsch, 2024) and (Tischner, 2018)
    private final double STUDY_AREA_LATITUDE_MIN = 47.829752;
    private final double STUDY_AREA_LATITUDE_MAX = 48.433757;
    private final double STUDY_AREA_LONGITUDE_MIN = 10.962982;
    private final double STUDY_AREA_LONGITUDE_MAX = 12.043762;

    private HashMap<String, Route> routes = new HashMap<>();
    // Key for "routes" hashmap refers to "route_id"

    private HashMap<String, Trip> trips = new HashMap<>();
    /* Key for "trips" hashmap also refers to "route_id" and value refers to "trip_id", which is the linkage between
    "routes.txt" and "stop_times.txt" (Gritsch, 2024)
    */

    private HashMap<String, RouteStop> routeStops = new HashMap<>();
    // Key for "routeStops" hashmap also refers to "route_id"

    private HashMap<String, StopTime> stopTimes = new HashMap<>();
    // Key for "stopTimes" hashmap also refers to "route_id"

    private HashMap<String, Stop> stops = new HashMap<>();
    // Key for "stops" hashmap refers to "stop_id"

    private HashMap<String, StopRoutes> stopRoutes = new HashMap<>();
    // Key for "stopRoutes" hashmap also refers to "stop_id"

    private HashMap<String, Transfer> transfers = new HashMap<>();
    // Key for "transfers" hashmap also refers to "stop_id"

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
                    String routeId = routeDataRecord[routeIdIndex];
                    int routeType = Integer.parseInt(routeDataRecord[routeTypeIndex]);

                    Route route = new Route();
                    route.setRouteType(routeType);
                    Trip trip = new Trip();
                    StopTime stopTime = new StopTime();
                    RouteStop routeStop = new RouteStop();

                    routes.put(routeId, route);
                    trips.put(routeId, trip);
                    stopTimes.put(routeId, stopTime);
                    routeStops.put(routeId, routeStop);
                }
            }

        } catch (FileNotFoundException fNFE) {
            System.out.println("File not found at specified path.");
        } catch (IOException iOE) {
            System.out.println("Input-output exception. Please check input file: " + gtfsRoutesFilePath);
        }
    }

    // Build the "trips" hashmap for ancillary purposes
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
                String routeId = tripDataRecord[routeIdIndex];

                if (routes.containsKey(routeId)) {
                    String tripId = tripDataRecord[tripIdIndex];

                    trips.get(routeId).getTripList().add(tripId);
                    ArrayList<StopTimeQuartet> tripSpecificStopTimeList = new ArrayList<>();
                    stopTimes.get(routeId).getTripWiseStopTimeLists().put(tripId, tripSpecificStopTimeList);
                }
            }

        } catch (FileNotFoundException fNFE) {
            System.out.println("File not found at specified path.");
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

            HashSet<String> tripIdHashSet = new HashSet<>();
            for (String routeId : trips.keySet()) {
                HashSet<String> routeSpecificTripIdHashSet = new HashSet<>(trips.get(routeId).getTripList());
                tripIdHashSet.addAll(routeSpecificTripIdHashSet);
            }

            // Read body and process data
            while ((newline = gtfsStopTimesReader.readLine()) != null) {
                String[] stopTimeDataRecord = newline.split(",");
                String tripId = stopTimeDataRecord[tripIdIndex];
                if (tripIdHashSet.contains(tripId)) {
                    String stopId = stopTimeDataRecord[stopIdIndex];

                    // gtfs.de indexing of stops begins at 0 in "stop_times.txt"
                    int stopSequence = Integer.parseInt(stopTimeDataRecord[stopSequenceIndex] + 1);

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

                    for (String routeId : stopTimes.keySet()) {
                        HashMap<String, ArrayList<StopTimeQuartet>> routeSpecificTripWiseStopTimeLists = stopTimes.
                                get(routeId).getTripWiseStopTimeLists();

                        if (routeSpecificTripWiseStopTimeLists.containsKey(tripId)) {
                            routeSpecificTripWiseStopTimeLists.get(tripId).add(stopTimeQuartet);
                        }
                    }
                }
            }

        } catch (FileNotFoundException fNFE) {
            System.out.println("File not found at the specified path.");
        } catch (IOException iOE) {
            System.out.println("Check the input file: " + gtfsStopTimesFilePath + "; found an IO exception.");
        }
    }

    // Sort the "stopTimes" hashmap
    public void sortStopTimes() {
        for (String routeId : stopTimes.keySet()) {
            // Get the StopTime instance that needs to be sorted internally
            StopTime tripWiseStopTimeLists = stopTimes.get(routeId);

            // Get the exact hashmap containing the string-object pairs of trip IDs and stop time quartet lists
            HashMap<String, ArrayList<StopTimeQuartet>> routeSpecificStopTimeLists = tripWiseStopTimeLists.
                    getTripWiseStopTimeLists();

            // Get the aforementioned string-object pairs into a list
            List<Map.Entry<String, ArrayList<StopTimeQuartet>>> routeSpecificStopTimeEntryList = new ArrayList<>
                    (routeSpecificStopTimeLists.entrySet());

            /* Sort the achieved arraylist of aforementioned string-object pairs using a comparator that gets every
            entry in the arraylist, then gets the value (ArrayList<StopTimeQuartet>) pertaining to that entry, then gets
            the earliest StopTimeQuartet instance, and then compares using the departure time found there
            */
            Collections.sort(routeSpecificStopTimeEntryList, Comparator.comparing(entry -> entry.getValue().get(0).
                    getDepartureTime()));

            // Get the sorted entries into a new hashmap
            HashMap<String, ArrayList<StopTimeQuartet>> sortedRouteSpecificStopTimeEntryLists = new HashMap<>();
            for (Map.Entry<String, ArrayList<StopTimeQuartet>> routeSpecificStopTimeEntry :
                    routeSpecificStopTimeEntryList) {
                sortedRouteSpecificStopTimeEntryLists.put(routeSpecificStopTimeEntry.getKey(),
                        routeSpecificStopTimeEntry.getValue());
            }

            // Create a StopTime instance that is sorted internally now
            StopTime sortedStopTime = new StopTime(sortedRouteSpecificStopTimeEntryLists);

            // Replace the old StopTime instance with the new sorted one
            stopTimes.replace(routeId, sortedStopTime);
        }
    }

    // Build the "routes" hashmap (route types have been addressed already)
    public void padGTFSRoutes() {
        for (String routeId : stopTimes.keySet()) {
            routes.get(routeId).setNumberTrips(stopTimes.get(routeId).getTripWiseStopTimeLists().size());
            String firstTripId = String.valueOf(stopTimes.get(routeId).getTripWiseStopTimeLists().keySet().
                    stream().findFirst());
            routes.get(routeId).setNumberStops(stopTimes.get(routeId).getTripWiseStopTimeLists().
                    get(firstTripId).size());
        }
    }

    // Build the "routeStops" hashmap and structure the "stops" hashmap
    public void padGTFSRouteStops() {
        for (String routeId : stopTimes.keySet()) {
            StopTime tripWiseStopTimeLists = stopTimes.get(routeId);
            String firstTripInRouteId = String.valueOf(tripWiseStopTimeLists.getTripWiseStopTimeLists().keySet().
                    stream().findFirst());
            ArrayList<StopTimeQuartet> stopTimeQuartetList = tripWiseStopTimeLists.getTripWiseStopTimeLists().
                    get(firstTripInRouteId);

            for (StopTimeQuartet stopTimeQuartet : stopTimeQuartetList) {
                routeStops.get(routeId).getStopSequenceMap().put(stopTimeQuartet.getStopSequence(),
                        stopTimeQuartet.getStopId());

                if (!stops.containsKey(stopTimeQuartet.getStopId())) {
                    Stop stop = new Stop();
                    stops.put(stopTimeQuartet.getStopId(), stop);
                }
            }
        }
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
                String stopId = stopDataRecord[stopIdIndex];

                if (stops.containsKey(stopId)) {
                    String stopName = stopDataRecord[stopNameIndex];
                    int stopType = (stopDataRecord[stopTypeIndex].isEmpty()) ? 0 : Integer.parseInt(stopDataRecord
                            [stopTypeIndex]);
                    double stopLatitude = Double.parseDouble(stopDataRecord[stopLatitudeIndex]);
                    double stopLongitude = Double.parseDouble(stopDataRecord[stopLongitudeIndex]);

                    Stop stop = new Stop(stopId, stopName, stopType, stopLatitude, stopLongitude);
                    stops.replace(stopId, stop);
                }
            }

        } catch (FileNotFoundException nFNE) {
            System.out.println("File not found at the specified path.");
        } catch (IOException iOE) {
            System.out.println("Check the input file: " + gtfsStopsFilePath + "; found an IO exception.");
        }
    }

    // Build the "stopRoutes" hashmap
    public void padStopRoutes() {
        for (String stopId : stops.keySet()) {
            StopRoutes stopSpecificRouteIds = new StopRoutes();
            stopRoutes.put(stopId, stopSpecificRouteIds);

            for (String routeId : routeStops.keySet()) {
                if (routeStops.get(routeId).getStopSequenceMap().containsValue(stopId)) {
                    stopSpecificRouteIds.getRouteIdList().add(routeId);
                }
            }

            stopRoutes.replace(stopId, stopSpecificRouteIds);
        }
    }

    // Build the "transfers" hashmap, ignoring pairs of distant stops
    public void buildTransfersHashMap() {
        for (String fromStopId : stops.keySet()) {
            double fromStopLatitudeRadians = Math.toRadians(stops.get(fromStopId).getStopLatitude());
            double fromStopLongitudeRadians = Math.toRadians(stops.get(fromStopId).getStopLongitude());
            Transfer stopSpecificTransferMap = new Transfer();

            for (String toStopId : stops.keySet()) {
                double toStopLatitudeRadians = Math.toRadians(stops.get(toStopId).getStopLatitude());
                double toStopLongitudeRadians = Math.toRadians(stops.get(toStopId).getStopLongitude());

                double latitudeDifference = toStopLatitudeRadians - fromStopLatitudeRadians;
                double longitudeDifference = toStopLongitudeRadians - fromStopLongitudeRadians;

                double x = longitudeDifference * Math.cos((fromStopLatitudeRadians + toStopLatitudeRadians) / 2);
                double y = latitudeDifference;
                double interStopDistance = Math.sqrt(x * x + y * y) * EARTH_RADIUS_M;

                if (interStopDistance <= MAXIMUM_TRANSFER_DISTANCE_M) {
                    stopSpecificTransferMap.getTransferMap().put(toStopId, (int) (Math.round(interStopDistance)));
                }
            }

            // Transfer cost between stops with identical latitude-longitude pairs is zero, which is realistic
            transfers.put(fromStopId, stopSpecificTransferMap);
        }
    }

    // Filter out unrealistic "transfers" based on GMaps API calls
    public void filterTransfersHashMap() {
        for (String fromStopId : transfers.keySet()) {
            HashMap<String, Integer> stopSpecificTransferMap = transfers.get(fromStopId).getTransferMap();
            double fromStopLatitude = stops.get(fromStopId).getStopLatitude();
            double fromStopLongitude = stops.get(fromStopId).getStopLongitude();

            for (String toStopId : stopSpecificTransferMap.keySet()) {
                double toStopLatitude = stops.get(toStopId).getStopLatitude();
                double toStopLongitude = stops.get(toStopId).getStopLongitude();
                int walkingDistanceBetweenStops = (int) (Math.round(calculateWalkingDistance(fromStopLatitude,
                        fromStopLongitude, toStopLatitude, toStopLongitude)));

                if (walkingDistanceBetweenStops <= MAXIMUM_TRANSFER_DISTANCE_M) {
                    transfers.get(fromStopId).getTransferMap().replace(toStopId, walkingDistanceBetweenStops);
                } else {
                    transfers.get(fromStopId).getTransferMap().remove(toStopId);
                }
            }
        }
    }

    // Filter out all "stops" from outside the study area, and all allied data from the pertinent hashmaps
    public void filterHashMapsOnLatLong() {
        // For hashmaps "stops", "stopRoutes", and "transfers":
        for (Stop stop : stops.values()) {
            if ((stop.getStopLatitude() > STUDY_AREA_LATITUDE_MAX) || (stop.getStopLatitude() < STUDY_AREA_LATITUDE_MIN)
                    || (stop.getStopLongitude() > STUDY_AREA_LONGITUDE_MAX) || (stop.getStopLongitude() <
                    STUDY_AREA_LONGITUDE_MIN)) {
                stops.remove(stop.getStopId());
                stopRoutes.remove(stop.getStopId());
                transfers.remove(stop.getStopId());
            }
        }

        // For hashmaps "routes", "routeStops", and "stopTimes"
        for (String routeId : stopTimes.keySet()) {
            for (String tripId : stopTimes.get(routeId).getTripWiseStopTimeLists().keySet()) {
                for (StopTimeQuartet stopTimeQuartet : stopTimes.get(routeId).getTripWiseStopTimeLists().get(tripId)) {
                    if (!stops.containsKey(stopTimeQuartet.getStopId())) {
                        stopTimes.get(routeId).getTripWiseStopTimeLists().get(tripId).remove(stopTimeQuartet);
                    }
                }

                if (stopTimes.get(routeId).getTripWiseStopTimeLists().get(tripId).isEmpty()) {
                    stopTimes.get(routeId).getTripWiseStopTimeLists().remove(tripId);
                    trips.get(routeId).getTripList().remove(tripId);
                }
            }

            if (stopTimes.get(routeId).getTripWiseStopTimeLists().isEmpty()) {
                stopTimes.remove(routeId);
                trips.remove(routeId);
                routes.remove(routeId);
            }
        }
    }

    // Make "transfers" hashmap transitive (consider a chain like fromStop-intermediateStop-toStop)
    public void makeTransfersTransitive() {
        for (String fromStopId : transfers.keySet()) {
            for (String intermediateStopId : transfers.get(fromStopId).getTransferMap().keySet()) {
                for (String toStopId : transfers.get(intermediateStopId).getTransferMap().keySet()) {
                    if (!transfers.get(fromStopId).getTransferMap().containsKey(toStopId)) {
                        double walkingDistanceBetweenStops = calculateWalkingDistance(stops.get(fromStopId).
                                getStopLatitude(), stops.get(fromStopId).getStopLongitude(), stops.get(toStopId).
                                getStopLatitude(), stops.get(toStopId).getStopLongitude());
                        transfers.get(fromStopId).getTransferMap().put(toStopId, (int) (Math.round(
                                walkingDistanceBetweenStops)));
                    }
                }
            }
        }
    }

    // Walking distance calculator based on GMaps API
    private double calculateWalkingDistance(double fromStopLatitude, double fromStopLongitude, double toStopLatitude,
                                            double toStopLongitude) {
        try {
            // Query GMaps Directions API for walking route
            DirectionsResult result = DirectionsApi.newRequest(googleGeoApiContext)
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
    public HashMap<String, Route> getRoutes() { return this.routes; }
    public HashMap<String, RouteStop> getRouteStops() { return this.routeStops; }
    public HashMap<String, StopTime> getStopTimes() { return this.stopTimes; }
    public HashMap<String, Stop> getStops() { return this.stops; }
    public HashMap<String, StopRoutes> getStopRoutes() { return this.stopRoutes; }
    public HashMap<String, Transfer> getTransfers() { return this.transfers; }
}