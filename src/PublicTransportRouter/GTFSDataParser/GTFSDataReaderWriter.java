package src.PublicTransportRouter.GTFSDataParser;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GTFSDataReaderWriter {

    private HashMap<String, Route> routes = new HashMap<>();
    private HashMap<String, RouteStop> routeStops = new HashMap<>();
    private HashMap<String, StopTime> stopTimesUnsorted = new HashMap<>();
    // TODO: HANDLE THIS SORTING BELOW
    private HashMap<String, StopTime> stopTimesSorted = new HashMap<>();
    private HashMap<String, Stop> stops = new HashMap<>();
    private HashMap<String, StopRoutes> stopRoutes = new HashMap<>();
    private HashMap<String, Transfer> transfers = new HashMap<>();

    public void readGTFSRoutes(String s) {
        try {
            // Reader for "routes.txt"
            BufferedReader gtfsRoutesReader = new BufferedReader(new FileReader(s));
            String newline;

            // Set up header array
            String[] routesHeaderArray = gtfsRoutesReader.readLine().split(",");
            int routeIdIndex = findIndexInArray("route_id", routesHeaderArray);

            // Read body
            while((newline = gtfsRoutesReader.readLine()) != null) {
                String[] routeDataRecord = newline.split(",");
                Route route = new Route();
                String routeId = routeDataRecord[routeIdIndex].substring(1, routeDataRecord[routeIdIndex].length() - 1);

                routes.put(routeId, route);
            }
        } catch (FileNotFoundException fNFE) {
            System.out.println("File not found at the specified path.");
        } catch (IOException iOE) {
            System.out.println("Input-output exception. Please check the input file: " + s);
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

    public void readGTFSStops(String s) {
        try {
            // Reader for "stop_times.txt"
            BufferedReader gtfsStopsReader = new BufferedReader(new FileReader(s));
            String newline;

            // Set up header array
            String[] stopsHeaderArray = gtfsStopsReader.readLine().split(",");

            // Read body
            for (String routeId : stopTimesUnsorted.keySet()) {
                RouteStop routeStop = new RouteStop();
                Optional<ArrayList<StopTimeQuartet>> stopTimeQuartetList = stopTimesUnsorted.get(routeId).
                        getTripWiseStopTimeList().values().stream().findFirst();

                if(stopTimeQuartetList.isPresent()) {
                    String stopId = stopTimeQuartetList
                    ArrayList<StopTimeQuartet> validStopTimeQuartetList = stopTimeQuartetList.get();
                }
                for(StopTimeQuartet stopTimeQuartet : stopTimeQuartetList) {

                }
                String firstTripIdForRoute = String.valueOf(stopTimesUnsorted.get(routeId).getTripWiseStopTimeList().
                        keySet().stream().findFirst());

                for(StopTimeQuartet stopTimeQuartet : stopTimesUnsorted.get(routeId).getTripWiseStopTimeList().values().stream().findFirst());
                stopTimesUnsorted.get(routeId).getTripWiseStopTimeList().
                        Optional<ArrayList<StopTimeQuartet>> stopTimeQuartets = stopTimesUnsorted.get(routeId).
                        getTripWiseStopTimeList().values().stream().findFirst();
                firstTripInRoute
                routeStops.put(stopTime.getKey(), routeStop);
                HashMap<String, ArrayList<StopTimeQuartet>> firstTripStopTimeList = stopTime.getValue().
                        getTripWiseStopTimeList();

                for(int routeSizeCounter = 1; routeSizeCounter <= firstTripStopTimeList.size(); routeSizeCounter += 1) {
                    routeStop.getStopSequenceMap().put(routeSizeCounter, );

                }
                routeStops.put(stopTime.getKey(), routeStop);
            }

        } catch (FileNotFoundException fNFE) {
            System.out.println("File not found at the specified path.");
        } catch (IOException iOE) {
            System.out.println("Check the input file: " + s + "; found an IO exception.");
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