package src.PublicTransportRouter.GTFSDataParser;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.Buffer;
import java.util.*;

public class GTFSDataReaderWriter {
    private static final String[] AGENCY_ID_ARRAY = {"21",  // Stadtwerke München
                                                     "62",  // DB Regio AG Südost
                                                     "390", // Nahreisezug
                                                     "54",  // DB Regio AG Bayern
                                                     "75",  // Go-Ahead Bayern GmbH
                                                     "302"  // DB RegioNetz Verkehrs GmbH Südostbayernbahn
                                                    };

    private HashMap<String, Route> routes = new HashMap<>();    // Key for "routes" hashmap refers to "route_id"
    private HashMap<String, Trip> trips = new HashMap<>();  // Key for "trips" hashmap refers to "route_id" and value
    // refers to "trip_id", which is the linkage between "routes.txt" and "stop_times.txt"

//    private HashMap<String, RouteStop> routeStops = new HashMap<>();
//    private HashMap<String, StopTime> stopTimesUnsorted = new HashMap<>();
//    // TODO: HANDLE THIS SORTING BELOW
//    private HashMap<String, StopTime> stopTimesSorted = new HashMap<>();
//    private HashMap<String, Stop> stops = new HashMap<>();
//    private HashMap<String, StopRoutes> stopRoutes = new HashMap<>();
//    private HashMap<String, Transfer> transfers = new HashMap<>();

    public void readAndFilterGTFSRoutes(String s) {
        try {
            // Reader for "routes.txt"
            BufferedReader gtfsRoutesReader = new BufferedReader(new FileReader(s));
            String newline;

            // Set up header array
            String[] routesHeaderArray = gtfsRoutesReader.readLine().split(",");
            int routeIdIndex = findIndexInArray("route_id", routesHeaderArray);

            // Read body and process data
            while((newline = gtfsRoutesReader.readLine()) != null) {
                String[] routeDataRecord = newline.split(",");
                String routeId = routeDataRecord[routeIdIndex];
                for (String agencyIdFilter : AGENCY_ID_ARRAY) {
                    if (routeId.equalsIgnoreCase(agencyIdFilter)) {
                        Route route = new Route();
                        routes.put(routeId, route);
                    }
                }
            }

        } catch (FileNotFoundException fNFE) {
            System.out.println("File not found at specified path.");
        } catch (IOException iOE) {
            System.out.println("Input-output exception. Please check input file: " + s);
        }
    }

    public void readAndFilterGTFSTrips(String s) {
        try {
            // Reader for "trips.txt"
            BufferedReader gtfsTripsReader = new BufferedReader(new FileReader(s));
            String newline;

            // Set up header array
            String[] tripsHeaderArray = gtfsTripsReader.readLine().split(",");
            int routeIdIndex = findIndexInArray("route_id", tripsHeaderArray);
            int tripIdIndex = findIndexInArray("trip_id", tripsHeaderArray);

            // Read body and process data
            while((newline = gtfsTripsReader.readLine()) != null) {
                String[] tripDataRecord = newline.split(",");
                String tripRouteId = tripDataRecord[routeIdIndex];
                for(String routeIdFilter : routes.keySet()) {
                    if(tripRouteId.equalsIgnoreCase(routeIdFilter)) {

                    }
                }
            }
        }
    }

    public void readGTFSStopTimes(String s) {
        try {
            // Reader for "stop_times.txt"
            BufferedReader gtfsStopTimesReader = new BufferedReader(new FileReader(s));
            String newline;

            // Set up header array
            String[] stopTimesHeaderArray = gtfsStopTimesReader.readLine().split(",");
            int tripIdIndex = findIndexInArray("trip_id", stopTimesHeaderArray);
            int stopIdIndex = findIndexInArray("stop_id", stopTimesHeaderArray);
            int stopSequenceIndex = findIndexInArray("stop_sequence", stopTimesHeaderArray);
            int arrivalTimeIndex = findIndexInArray("arrival_time", stopTimesHeaderArray);
            int departureTimeIndex = findIndexInArray("departure_time", stopTimesHeaderArray);
            int distanceTraveledIndex = findIndexInArray("shape_dist_traveled",
                    stopTimesHeaderArray);

            // Read body
            for(String routeId : routes.keySet()) {
                StopTime stopTime = new StopTime();
                stopTimesUnsorted.put(routeId, stopTime);

                while((newline = gtfsStopTimesReader.readLine()) != null) {
                    String[] stopTimeDataRecord = newline.split(",");

                    String distanceTraveledString = stopTimeDataRecord[distanceTraveledIndex].substring(1,
                            stopTimeDataRecord[distanceTraveledIndex].length() - 1);
                    double distanceTraveled = Double.parseDouble(distanceTraveledString);

                    String tripId = stopTimeDataRecord[tripIdIndex].substring(1, stopTimeDataRecord[tripIdIndex].
                            length() - 1);

                    String stopId = stopTimeDataRecord[stopIdIndex].substring(1,
                            stopTimeDataRecord[stopIdIndex].length() - 1);

                    String stopSequenceString = stopTimeDataRecord[stopSequenceIndex].substring(1,
                            stopTimeDataRecord[stopSequenceIndex].length() - 1);
                    int stopSequence = Integer.parseInt(stopSequenceString);

                    String arrivalTimeHourString = stopTimeDataRecord[arrivalTimeIndex].substring(1, 3);
                    String arrivalTimeMinuteString = stopTimeDataRecord[arrivalTimeIndex].substring(4, 6);
                    int arrivalTimeInMinutes = Integer.parseInt(arrivalTimeHourString) * 60 +
                            Integer.parseInt(arrivalTimeMinuteString);

                    String departureTimeHourString = stopTimeDataRecord[departureTimeIndex].substring(1, 3);
                    String departureTimeMinuteString = stopTimeDataRecord[departureTimeIndex].substring(4, 6);
                    int departureTimeInMinutes = Integer.parseInt(departureTimeHourString) * 60 +
                            Integer.parseInt(departureTimeMinuteString);

                    StopTimeQuartet stopTimeQuartet = new StopTimeQuartet(stopId, stopSequence, arrivalTimeInMinutes,
                            departureTimeInMinutes);

                    if(tripId.contains(routeId)) {
                        if (distanceTraveled == 0.00d) {
                            ArrayList<StopTimeQuartet> tripSpecificStopTimeList = new ArrayList<>();
                            tripSpecificStopTimeList.add(stopTimeQuartet);
                            stopTime.getTripWiseStopTimeList().put(tripId, tripSpecificStopTimeList);
                        } else {
                            stopTime.getTripWiseStopTimeList().get(tripId).add(stopTimeQuartet);
                        }
                    }
                }
            }

        } catch (FileNotFoundException fNFE) {
            System.out.println("File not found at the specified path.");
        } catch (IOException iOE) {
            System.out.println("Check the input file: " + s + "; found an IO exception.");
        }

        // Fill out the routes hashmap
        for(String routeId : stopTimesUnsorted.keySet()) {
            routes.get(routeId).setNumberTrips(stopTimesUnsorted.get(routeId).getTripWiseStopTimeList().size());
            String tripId = String.valueOf(stopTimesUnsorted.get(routeId).getTripWiseStopTimeList().keySet().stream().
                    findFirst());
            routes.get(routeId).setNumberStops(stopTimesUnsorted.get(routeId).getTripWiseStopTimeList().get(tripId).
                    size());
        }
    }

    public void padGTFSRouteStops(String s) {
        for (String routeId : stopTimesUnsorted.keySet()) {
            RouteStop routeStop = new RouteStop();
            StopTime tripWiseStopTimeList = stopTimesUnsorted.get(routeId);
            String firstTripInRouteId = String.valueOf(tripWiseStopTimeList.getTripWiseStopTimeList().keySet().
                    stream().findFirst());
            ArrayList<StopTimeQuartet> stopTimeQuartetList = tripWiseStopTimeList.getTripWiseStopTimeList().
                    get(firstTripInRouteId);

            for(StopTimeQuartet stopTimeQuartet : stopTimeQuartetList) {
                int stopCount = 1;
                routeStop.getStopSequenceMap().put(stopCount, stopTimeQuartet.getStopId());
            }
        }
    }

    public void readGTFSStops(String s) {
        try {
            // Reader for "stops.txt"
            BufferedReader gtfsStopsReader = new BufferedReader(new FileReader(s));
            String newline;

            // Set up header array
            String[] stopsHeaderArray = gtfsStopsReader.readLine().split(",");
            int stopIdIndex = findIndexInArray("stop_id", stopsHeaderArray);
            int stopNameIndex = findIndexInArray("stop_name", stopsHeaderArray);
            int stopLatitudeIndex = findIndexInArray("stop_lat", stopsHeaderArray);
            int stopLongitudeIndex = findIndexInArray("stop_lon", stopsHeaderArray);

            // Read body
            while((newline = gtfsStopsReader.readLine()) != null) {
                String[] stopDataRecord = newline.split(",");

                String stopId = stopDataRecord[stopIdIndex].substring(1, stopDataRecord[stopIdIndex].length() - 1);
                double stopLatitude = Double.parseDouble(stopDataRecord[stopLatitudeIndex].substring(1,
                        stopDataRecord[stopLatitudeIndex].length() - 1));
                double stopLongitude = Double.parseDouble(stopDataRecord[stopLongitudeIndex].substring(1,
                        stopDataRecord[stopLongitudeIndex].length() - 1));

                Stop stop = new Stop(stopId, stopLatitude, stopLongitude);
                stops.put(stopId, stop);
            }

        } catch (FileNotFoundException nFNE) {
            System.out.println("File not found at the specified path.");
        } catch (IOException iOE) {
            System.out.println("Check the input file: " + s + "; found an IO exception.");
        }
    }

    public void readTransfers(String s) {
        try{
            // Reader for "transfers.txt"
            BufferedReader gtfsTransfersReader = new BufferedReader(new FileReader(s));
            String newline;

            // Set up header array
            String[] transfersHeaderArray = gtfsTransfersReader.readLine().split(",");
            int fromStopIdIndex = findIndexInArray("from_stop_id", transfersHeaderArray);
            int toStopIdIndex = findIndexInArray("to_stop_id", transfersHeaderArray);
            int transferDurationIndex = findIndexInArray("min_transfer_time", transfersHeaderArray);

            // Read body
            for(String fromStopId : stops.keySet()) {
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

    public void readStopRoutes(String s) {
        try {
            // Reader for "stop_times.txt"
            BufferedReader gtfsStopRoutesReader = new BufferedReader(new FileReader(s));
            String newline;

            // Set up header array
            String[] stopRoutesHeaderArray = gtfsStopRoutesReader.readLine().split(",");
            int routeIdIndex = findIndexInArray("")

            // Read body

        }
    }

    private int findIndexInArray(String columnHeaderName, String[] headerArray) {
        int columnPosition = -1;
        for(int i = 0; i <= headerArray.length; i++) {
            if(headerArray[i].equalsIgnoreCase(columnHeaderName)) {
                columnPosition = i;
            }
        }
        return columnPosition;
    }
}