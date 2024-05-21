package src.PublicTransportRouter.RAPTOR;

// TODO: CUT OUT ALL IRRELEVANT CODE FROM MANAGER, MAKE THE CODE WATERTIGHT AND ENCAPSULATED
// TODO: REVISE ALL LOOP CODE TO SPEED IT UP, REVISE MANAGER's DATA STRUCTURES IF NEEDED
// TODO: REVISIT THE STOPPING CRITERION FOR RAPTOR, AND ALSO THE WHILE LOOP USED TO BUILD IT
// TODO: HANDLE TRIP DAYS BY MANAGING 400 AM DEPARTURES AND NIGHT OPERATIONS AND SO ON (OR ADDING A TRIP DAY FOR TRIPS)
// TODO: PRINT RESULT USING STOP NAMES AND ARRIVAL DURATIONS (IN MIN)
// TODO: Marked stops' arraylist development (only for addition, removal is taken care of already) comes later

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

import src.PublicTransportRouter.GTFSDataManager.*;

public class RoutingAlgorithm {

    public static void main(String[] args) {
        // GTFS data reader-writer instantiation to read, write, and store data
        GTFSDataReaderWriter gtfsDataReaderWriterForRaptor = new GTFSDataReaderWriter();
        String gtfsFolderPath = "";
        String raptorFolderPath = "";

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
        gtfsDataReaderWriterForRaptor.filterHashMapsOnLatLong();
        gtfsDataReaderWriterForRaptor.makeTransfersTransitive();

        // Write out data used for RAPTOR
        gtfsDataReaderWriterForRaptor.writeRaptorRoutes(raptorRoutesFilePath);
        gtfsDataReaderWriterForRaptor.writeRaptorRouteStops(raptorRouteStopsFilePath);
        gtfsDataReaderWriterForRaptor.writeTrips(tripsFilePath);
        gtfsDataReaderWriterForRaptor.writeRaptorStopTimes(raptorStopTimesFilePath);
        gtfsDataReaderWriterForRaptor.writeRaptorStops(raptorStopsFilePath);
        gtfsDataReaderWriterForRaptor.writeRaptorStopRoutes(raptorStopRoutesFilePath);
        gtfsDataReaderWriterForRaptor.writeRaptorTransfers(raptorTransfersFilePath);

        // Get all data for RAPTOR execution
        HashMap<String, Route> routes = gtfsDataReaderWriterForRaptor.getRoutes();
        HashMap<String, RouteStop> routeStops = gtfsDataReaderWriterForRaptor.getRouteStops();
        HashMap<String, Trip> trips = gtfsDataReaderWriterForRaptor.getTrips();
        HashMap<String, StopTime> stopTimes = gtfsDataReaderWriterForRaptor.getStopTimes();
        HashMap<String, Stop> stops = gtfsDataReaderWriterForRaptor.getStops();
        HashMap<String, StopRoutes> stopRoutes = gtfsDataReaderWriterForRaptor.getStopRoutes();
        HashMap<String, Transfer> transfers = gtfsDataReaderWriterForRaptor.getTransfers();

        // Source all queries for the RAPTOR algorithm
        QueryReader queryReader = new QueryReader();
        String queriesFilePath = "";
        queryReader.readQueryList(queriesFilePath);

        ArrayList<Query> queries = queryReader.getQueries();
        ArrayList<Integer> travelTimes = new ArrayList<>();

        // Loop through queries
        for (Query query : queries) {
            String originStopId = query.getOriginStopId();
            String destinationStopId = query.getDestinationStopId();
            int departureTime = query.getDepartureTime();

            // Initialize RAPTOR for each query
            int tripLegNumber = 1;
            HashMap<Integer, HashMap<String, Integer>> initialEarliestArrivalTimeMap = new HashMap<>();
            HashMap<Integer, HashMap<String, Integer>> tripLegSpecificEarliestArrivalTimeMap =
                    initializeTripLegSpecificArrivalTimeMap(tripLegNumber, initialEarliestArrivalTimeMap, stops);
            HashMap<String, Integer> summaryEarliestArrivalTimeMap = initializeSummaryEarliestArrivalTimeMap(stops);

            // Set origin stop's "earliest arrival time" to "departure time specified for departing from the same stop"
            tripLegSpecificEarliestArrivalTimeMap.get(1).replace(originStopId, departureTime);

            // Add the origin stop to the list of marked stops, for the first round of route scans
            ArrayList<String> markedStopsIdsList = new ArrayList<>();
            markedStopsIdsList.add(originStopId);
            HashMap<String, String> accumulatedRoutesFromStopsMap = new HashMap<>();

            // Core algorithm loops
            while (!markedStopsIdsList.isEmpty()) {
                /* Accumulate routes (route IDs) serving marked stops (stop IDs) from previous round, and recreate the
                earliest arrival time map
                */
                accumulateRoutesFromStops(markedStopsIdsList, stopRoutes, routeStops, accumulatedRoutesFromStopsMap);
                initializeTripLegSpecificArrivalTimeMap(tripLegNumber, tripLegSpecificEarliestArrivalTimeMap, stops);

                // Traverse each route
                for (HashMap.Entry<String, String> routeStopPair : accumulatedRoutesFromStopsMap.entrySet()) {
                    for (HashMap.Entry<String, ArrayList<StopTimeQuartet>> tripForTraversal : stopTimes.
                            get(routeStopPair.getKey()).getTripWiseStopTimeLists().entrySet()) {
                        for (StopTimeQuartet stopTimeQuartet : tripForTraversal.getValue()) {
                            if (stopTimeQuartet.getArrivalTime() < Math.min(summaryEarliestArrivalTimeMap.
                                    get(stopTimeQuartet.getStopId()), summaryEarliestArrivalTimeMap.
                                    get(destinationStopId))) {
                                tripLegSpecificEarliestArrivalTimeMap.get(tripLegNumber).replace(stopTimeQuartet.
                                        getStopId(), stopTimeQuartet.getArrivalTime());
                                summaryEarliestArrivalTimeMap.replace(stopTimeQuartet.getStopId(), stopTimeQuartet.
                                        getArrivalTime());
                                markedStopsIdsList.add(stopTimeQuartet.getStopId());


                            }
                        }
                    }
                    StopTime routeSpecificStopTimeList = stopTimes.get(routeId);
                    for (HashMap.Entry<String, ArrayList<StopTimeQuartet>> tripSpecificStopTimeList :
                            routeSpecificStopTimeList.getTripWiseStopTimeLists().entrySet()) {
                        for (StopTimeQuartet stopTimeQuartet : tripSpecificStopTimeList.getValue()) {
                            if (stopTimeQuartet.)
                        }
                    }

                    // Look at footpaths

                    // todo Clear Q
                    accumulatedRoutesFromStopsMap.clear();
                    tripLegNumber++;
                }
            }
        }

        // Display results
        // todo: ready the travel times array
        for (int index = 1; index <= queries.size(); index++) {
            System.out.println("Origin stop: " + queries.get(index).getOriginStopId() + "\n" +
                    "Destination stop: " + queries.get(index).getDestinationStopId() + "\n" +
                    "Departure time: " + queries.get(index).getDepartureTime() + "\n" +
                    "Travel time: " + travelTimes.get(index) + "\n");
        }
    }

    /* Initialize algorithm by setting arrival timestamps at all stops for all trip leg numbers to infinity, except the
    one for the origin stop; also initialize a map, and an arraylist for earliest known arrival times
    */
    private static HashMap<Integer, HashMap<String, Integer>> initializeTripLegSpecificArrivalTimeMap
    (int tripLegNumber, HashMap<Integer, HashMap<String, Integer>> initialEarliestArrivalTimeMap,
     HashMap<String, Stop> stops) {
        /* In the above hashmap, external integer keys refer to trip leg numbers, internal string keys to stop IDs, and
        internal integer values to the earliest known arrival times at the pertinent stops; the stop-arrival time pair
        is repeatedly built for every trip leg number iteration, in the external code
        */

        if (tripLegNumber == 1) {
            HashMap<String, Integer> tripLegSpecificEarliestArrivalTimeMap = new HashMap<>();
            // Strings refer to stop IDs, and integers refer to the pertinent earliest arrival time
            for (String stopId : stops.keySet()) {
                tripLegSpecificEarliestArrivalTimeMap.put(stopId, Integer.MAX_VALUE);
            }
            initialEarliestArrivalTimeMap.put(tripLegNumber, tripLegSpecificEarliestArrivalTimeMap);

        } else {
            // Replicate earliest arrival times reported in previous iterations
            initialEarliestArrivalTimeMap.put(tripLegNumber, initialEarliestArrivalTimeMap.get(tripLegNumber - 1));
        }

        return initialEarliestArrivalTimeMap;
    }

    private static HashMap<String, Integer> initializeSummaryEarliestArrivalTimeMap(HashMap<String,
            Stop> stops) {
        HashMap<String, Integer> summaryEarliestArrivalTimeMap = new HashMap<>();
        /* String keys refer to stop IDs, and integer values refer to the earliest arrival time at each stop,
        irrespective of the trip leg
        */

        for (String stopId : stops.keySet()) {
            summaryEarliestArrivalTimeMap.put(stopId, Integer.MAX_VALUE);
        }

        return summaryEarliestArrivalTimeMap;
    }

    // Accumulate routes (and hop-on stops) based on stop IDs and "stopRoutes" hashmap
    private static HashMap<String, String> accumulateRoutesFromStops(ArrayList<String> markedStopsIdsList,
                                                                     HashMap<String, StopRoutes> stopRoutesMap,
                                                                     HashMap<String, RouteStop> routeStopsMap,
                                                                     HashMap<String, String>
                                                                             accumulatedRoutesFromStopsMap) {
        /* In the method arguments, the arraylist's strings are stop IDs to be iterated over, and the hashmap's keys are
        all stop IDs in the study area, which are mapped to lists of routes for every stop; what is returned is a
        hashmap of routes (string keys), and the stops that led to those routes being selected (string values)

        The second hashmap is for finding the earliest possible stop to hop-on when changing from one route to another
        */

        for (String currentMarkedStopId : markedStopsIdsList) {
            for (String markedStopSpecificRouteId : stopRoutesMap.get(currentMarkedStopId).getRouteIdList()) {

                if (accumulatedRoutesFromStopsMap.containsKey(markedStopSpecificRouteId)) {
                    String existingMarkedStopId = accumulatedRoutesFromStopsMap.get(markedStopSpecificRouteId);
                    int currentMarkedStopSequence = routeStopsMap.get(markedStopSpecificRouteId).getStopSequenceMap().
                            get(currentMarkedStopId);
                    int existingMarkedStopSequence = routeStopsMap.get(markedStopSpecificRouteId).getStopSequenceMap().
                            get(existingMarkedStopId);

                    if (currentMarkedStopSequence < existingMarkedStopSequence) {
                        accumulatedRoutesFromStopsMap.replace(markedStopSpecificRouteId, currentMarkedStopId);
                    }

                } else {
                    accumulatedRoutesFromStopsMap.put(markedStopSpecificRouteId, currentMarkedStopId);
                }
            }

            markedStopsIdsList.remove(currentMarkedStopId);
        }

        return accumulatedRoutesFromStopsMap;
    }
}