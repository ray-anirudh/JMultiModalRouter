package src.PublicTransportRouter.RAPTOR;

// TODO: CUT OUT ALL IRRELEVANT CODE FROM MANAGER, MAKE THE CODE WATERTIGHT AND ENCAPSULATED
// TODO: REVISE ALL LOOP CODE TO SPEED IT UP, REVISE MANAGER's DATA STRUCTURES IF NEEDED

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

        // Specify attributes' for routing query
        String originStopId = "";
        String destinationStopId = "";
        int departureTime = 0;

        // Initialize RAPTOR
        int tripLegNumber = 1;
        HashMap<Integer, HashMap<String, Integer>> firstTripLegEarliestArrivalTimeMap = new HashMap<>();
        HashMap<Integer, HashMap<String, Integer>> tripLegWiseEarliestArrivalTimeMap =
                initializeRoundSpecificArrivalTimeMap(tripLegNumber, firstTripLegEarliestArrivalTimeMap, stops);
        HashMap<String, Integer> stopWiseEarliestArrivalTimeMap = initializeEarliestArrivalTimeMap(stops);

        // Set origin stop's earliest arrival time to the "departure time specified for departing from the same stop"
        tripLegWiseEarliestArrivalTimeMap.get(1).replace(originStopId, departureTime);

        // Add the origin stop to the list of marked stops, for the first round of route scans
        ArrayList<String> markedStopsIdsList = new ArrayList<>();
        markedStopsIdsList.add(originStopId);

        // Core algorithm loops
        while(!markedStopsIdsList.isEmpty()) {
            // Setting up an earliest arrival time map, explicitly for all stops and the current trip leg
            initializeRoundSpecificArrivalTimeMap(tripLegNumber, tripLegWiseEarliestArrivalTimeMap, stops);

            // Accumulate routes serving marked stops from previous round
            HashMap<String, String> accumulatedRoutesStopsMap = accumulateRoutesFromStops(markedStopsIdsList,
                    stopRoutes, routeStops);

            // TODO: Marked stops' arraylist development (only for addition, removal is taken care of already) comes later
            // Traverse each route
            for (String routeId : accumulatedRoutesStopsMap.keySet()) {
                StopTime routeSpecificStopTimeList = stopTimes.get(routeId);
                for(HashMap.Entry<String, ArrayList<StopTimeQuartet>> tripSpecificStopTimeList :
                        routeSpecificStopTimeList.getTripWiseStopTimeLists().entrySet()) {
                    for(StopTimeQuartet stopTimeQuartet : tripSpecificStopTimeList.getValue()) {
                        if (stopTimeQuartet.g)
                    }
            }

            // Look at footpaths

            tripLegNumber++;
            }
        }
    }

    /* Initialize algorithm by setting arrival timestamps at all stops for all trip leg numbers to infinity, except the
    one for the origin stop; also initialize a map, and an arraylist for earliest known arrival times
    */
    // todo: this code block might need a huge revamp
    private static HashMap<Integer, HashMap<String, Integer>>
    initializeRoundSpecificArrivalTimeMap(int tripLegNumber, HashMap<Integer, HashMap<String, Integer>>
            tripLegWiseEarliestArrivalTimeAtEachStop, HashMap<String, Stop> stops) {

        /* In the above hashmap, external integer keys refer to trip leg numbers, internal string keys to stop IDs, and
        internal integer values to the earliest known arrival times at the pertinent stops; the stop-arrival time pair
        is repeatedly built for every trip leg number iteration, in the external code
        */

        HashMap<String, Integer> tripLegSpecificEarliestArrivalTimeAtEachStop = new HashMap<>();
        // Strings refer to stop IDs, and integers refer to the pertinent earliest arrival time
        for (String stopId : stops.keySet()) {
            tripLegSpecificEarliestArrivalTimeAtEachStop.put(stopId, Integer.MAX_VALUE);
        }

        tripLegWiseEarliestArrivalTimeAtEachStop.put(tripLegNumber, tripLegSpecificEarliestArrivalTimeAtEachStop);
        return tripLegWiseEarliestArrivalTimeAtEachStop;
    }

    private static HashMap<String, Integer> initializeEarliestArrivalTimeMap(HashMap<String, Stop> stops) {
        HashMap<String, Integer> stopWiseEarliestArrivalTimeMap = new HashMap<>();
        /* String keys refer to stop IDs, and integer values refer to the earliest arrival time at each stop,
        irrespective of the trip leg
        */

        for (String stopId : stops.keySet()) {
            stopWiseEarliestArrivalTimeMap.put(stopId, Integer.MAX_VALUE);
        }

        return stopWiseEarliestArrivalTimeMap;
    }

    // Accumulate routes (and hop-on stops) based on stop IDs and "stopRoutes" hashmap
    private static HashMap<String, String> accumulateRoutesFromStops(ArrayList<String> markedStopsIdsList,
                                                                     HashMap<String, StopRoutes> stopRoutesMap,
                                                                     HashMap<String, RouteStop> routeStopsMap) {
        /* In the method arguments, the arraylist's strings are stop IDs to be iterated over, and the hashmap's keys are
        all stop IDs in the study area, which are mapped to lists of routes for every stop; what is returned is a
        hashmap of routes (string keys), and the stops that led to those routes being selected (string values)

        The second hashmap is for finding the earliest possible stop to hop-on when changing from one route to another
        */

        HashMap<String, String> accumulatedRoutesStopsMap = new HashMap<>();

        for (String currentMarkedStopId : markedStopsIdsList) {
            for (String markedStopSpecificRouteId : stopRoutesMap.get(currentMarkedStopId).getRouteIdList()) {
                if (accumulatedRoutesStopsMap.containsKey(markedStopSpecificRouteId)) {
                    String existingMarkedStopId = accumulatedRoutesStopsMap.get(markedStopSpecificRouteId);
                    int currentMarkedStopSequence = routeStopsMap.get(markedStopSpecificRouteId).getStopSequenceMap().
                            get(currentMarkedStopId);
                    int existingMarkedStopSequence = routeStopsMap.get(markedStopSpecificRouteId).getStopSequenceMap().
                            get(existingMarkedStopId);

                    if (currentMarkedStopSequence < existingMarkedStopSequence) {
                        accumulatedRoutesStopsMap.replace(markedStopSpecificRouteId, currentMarkedStopId);
                    }
                } else {
                    accumulatedRoutesStopsMap.put(markedStopSpecificRouteId, currentMarkedStopId);
                }
            }

            markedStopsIdsList.remove(currentMarkedStopId);
        }

        return accumulatedRoutesStopsMap;
    }
}