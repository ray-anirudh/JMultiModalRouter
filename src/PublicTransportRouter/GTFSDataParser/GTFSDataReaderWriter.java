package src.PublicTransportRouter.GTFSDataParser;

// TODO: FIX THE STOP TIMES SORTING AND BUILD A CUTTER BASED ON SERVICE AREA LAT-LONGS OF MUNCHEN

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

    private HashMap<String, Route> routes = new HashMap<>();
    // Key for "routes" hashmap refers to "route_id"

    private HashMap<String, Trip> trips = new HashMap<>();
    /* Key for "trips" hashmap also refers to "route_id" and value refers to "trip_id", which is the linkage between
    "routes.txt" and "stop_times.txt"
    */

    private HashMap<String, RouteStop> routeStops = new HashMap<>();
    // Key for "routeStops" hashmap also refers to "route_id"

    private HashMap<String, StopTime> stopTimesUnsorted = new HashMap<>();
    // Key for "stopTimesUnsorted" hashmap also refers to "route_id"

    private HashMap<String, StopTime> stopTimesSorted = new HashMap<>();
    // Key for "stopTimesSorted" hashmap also refers to "route_id"

    private HashMap<String, Stop> stops = new HashMap<>();
    // Key for "stops" hashmap refers to "stop_id"

    private HashMap<String, StopRoutes> stopRoutes = new HashMap<>();
    // Key for "stopRoutes" hashmap also refers to "stop_id"
    private HashMap<String, Transfer> transfers = new HashMap<>();
    // Key for "transfers" hashmap also refers to "stop_id"

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
                    stopTimesUnsorted.put(routeId, stopTime);
                    routeStops.put(routeId, routeStop);
                }
            }

        } catch (FileNotFoundException fNFE) {
            System.out.println("File not found at specified path.");
        } catch (IOException iOE) {
            System.out.println("Input-output exception. Please check input file: " + gtfsRoutesFilePath);
        }
    }

    public void readAndFilterGTFSTrips(String gtfsTripsFilePath) {
        try {
            // Reader for "trips.txt"
            BufferedReader gtfsTripsReader = new BufferedReader(new FileReader(gtfsTripsFilePath));
            String newline;

            // Set up header array and "route_id" lookup set
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
                    stopTimesUnsorted.get(routeId).getTripWiseStopTimeLists().put(tripId, new ArrayList<>());
                }
            }

        } catch (FileNotFoundException fNFE) {
            System.out.println("File not found at specified path.");
        } catch (IOException iOE) {
            System.out.println("Input-output exception. Please check input file: " + gtfsTripsFilePath);
        }
    }

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
                    int stopSequence = Integer.parseInt(stopTimeDataRecord[stopSequenceIndex]);
                    String arrivalTimeHourString = stopTimeDataRecord[arrivalTimeIndex].substring(0, 2);
                    String arrivalTimeMinuteString = stopTimeDataRecord[arrivalTimeIndex].substring(3, 5);
                    int arrivalTimeMinutes = Integer.parseInt(arrivalTimeHourString) * 60 +
                            Integer.parseInt(arrivalTimeMinuteString);
                    String departureTimeHourString = stopTimeDataRecord[departureTimeIndex].substring(0, 2);
                    String departureTimeMinuteString = stopTimeDataRecord[departureTimeIndex].substring(3, 5);
                    int departureTimeMinutes = Integer.parseInt(departureTimeHourString) * 60 +
                            Integer.parseInt(departureTimeMinuteString);

                    StopTimeQuartet stopTimeQuartet = new StopTimeQuartet(stopId, stopSequence, arrivalTimeMinutes,
                            departureTimeMinutes);

                    for (String routeId : stopTimesUnsorted.keySet()) {
                        HashMap<String, ArrayList<StopTimeQuartet>> routeSpecificTripHashMap = stopTimesUnsorted.
                                get(routeId).getTripWiseStopTimeLists();

                        if (routeSpecificTripHashMap.containsKey(tripId)) {
                            routeSpecificTripHashMap.get(tripId).add(stopTimeQuartet);
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

    public void padGTFSRoutes() {
        // Padding the "routes" hashmap
        for (String routeId : stopTimesUnsorted.keySet()) {
            routes.get(routeId).setNumberTrips(stopTimesUnsorted.get(routeId).getTripWiseStopTimeLists().size());
            String firstTripId = String.valueOf(stopTimesUnsorted.get(routeId).getTripWiseStopTimeLists().keySet().
                    stream().findFirst());
            routes.get(routeId).setNumberStops(stopTimesUnsorted.get(routeId).getTripWiseStopTimeLists().
                    get(firstTripId).size());
        }
    }

    public void padGTFSRouteStops() {
        // Padding the "routeStops" hashmap
        for (String routeId : stopTimesUnsorted.keySet()) {
            StopTime tripWiseStopTimeLists = stopTimesUnsorted.get(routeId);
            String firstTripInRouteId = String.valueOf(tripWiseStopTimeLists.getTripWiseStopTimeLists().keySet().
                    stream().findFirst());
            ArrayList<StopTimeQuartet> stopTimeQuartetList = tripWiseStopTimeLists.getTripWiseStopTimeLists().
                    get(firstTripInRouteId);

            int stopCount = 1;
            for (StopTimeQuartet stopTimeQuartet : stopTimeQuartetList) {
                routeStops.get(routeId).getStopSequenceMap().put(stopCount, stopTimeQuartet.getStopId());
                stopCount++;

                if (!stops.containsKey(stopTimeQuartet.getStopId())) {
                    Stop stop = new Stop();
                    stops.put(stopTimeQuartet.getStopId(), stop);
                }
            }
        }
    }

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

    public void padStopRoutes() {
        // Padding for "stopRoutes" hashmap
        for (Stop stop : stops.values()) {
            StopRoutes stopSpecificRoutes = new StopRoutes();
            stopRoutes.put(stop.getStopId(), stopSpecificRoutes);
            for (String routeId : routeStops.keySet()) {
                if (routeStops.get(routeId).getStopSequenceMap().containsValue(stop.getStopId())) {
                    stopSpecificRoutes.getRouteList().add(routes.get(routeId));
                    stopRoutes.replace(stop.getStopId(), stopSpecificRoutes);
                }
            }
        }
    }

    public void readTransfers(String s) {
        try {
            // Reader for "transfers.txt"
            BufferedReader gtfsTransfersReader = new BufferedReader(new FileReader(s));
            String newline;

            // Set up header array
            String[] transfersHeaderArray = gtfsTransfersReader.readLine().split(",");
            int fromStopIdIndex = findIndexInArray("from_stop_id", transfersHeaderArray);
            int toStopIdIndex = findIndexInArray("to_stop_id", transfersHeaderArray);
            int transferDurationIndex = findIndexInArray("min_transfer_time", transfersHeaderArray);

            // Read body
            for (String fromStopId : stops.keySet()) {
                Transfer transfer = new Transfer();
                transfers.put(fromStopId, transfer);
                while ((newline = gtfsTransfersReader.readLine()) != null) {
                    String[] transferDataRecord = newline.split(",");
                    String transfersFileFromStopId = transferDataRecord[fromStopIdIndex].substring(1,
                            transferDataRecord[fromStopIdIndex].length() - 1);
                    if (transfersFileFromStopId.equalsIgnoreCase(fromStopId)) {
                        String toStopId = transferDataRecord[toStopIdIndex].substring(1, transferDataRecord
                                [toStopIdIndex].length() - 1);
                        int transferDuration = Integer.parseInt(transferDataRecord[transferDurationIndex].substring
                                (1, transferDataRecord[transferDurationIndex].length() - 1));

                        transfer.getTransferMap().put(stops.get(toStopId), transferDuration);
                    }
                }
            }

        } catch (FileNotFoundException fNFE) {
            System.out.println("File not found at the specified path.");
        } catch (IOException iOE) {
            System.out.println("Check the input file: " + s + "; found an IO exception.");
        }
    }

    private int findIndexInArray(String columnHeaderName, String[] headerArray) {
        int columnPosition = -1;
        for (int i = 0; i <= headerArray.length; i++) {
            if (headerArray[i].equalsIgnoreCase(columnHeaderName)) {
                columnPosition = i;
            }
        }
        return columnPosition;
    }
}