package src.PublicTransportRouter.RAPTOR;

import src.PublicTransportRouter.GTFSDataManager.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RoutingAlgorithm {

    public static void main(String[] args) {
        // Read, write, and store data
        GTFSDataReaderWriter gtfsDataReaderWriterForRaptor = new GTFSDataReaderWriter();
        String gtfsFolderPath = "";

        String gtfsRoutesFilePath = gtfsFolderPath + "/routes.txt";
        gtfsDataReaderWriterForRaptor.readAndFilterGTFSRoutes(gtfsRoutesFilePath);

        String gtfsTripsFilePath = gtfsFolderPath + "/trips.txt";
        gtfsDataReaderWriterForRaptor.readAndFilterGTFSTrips(gtfsTripsFilePath);

        String gtfsStopTimesFilePath = gtfsFolderPath + "/stop_times.txt";
        gtfsDataReaderWriterForRaptor.readAndFilterGTFSStopTimes(gtfsStopTimesFilePath);

        gtfsDataReaderWriterForRaptor.sortStopTimes();
        gtfsDataReaderWriterForRaptor.padGTFSRoutes();
        gtfsDataReaderWriterForRaptor.padGTFSRouteStops();

        String gtfsStopsFilePath = gtfsFolderPath + "/stops.txt";
        gtfsDataReaderWriterForRaptor.readAndFilterGTFSStops(gtfsStopsFilePath);

        gtfsDataReaderWriterForRaptor.padStopRoutes();
        gtfsDataReaderWriterForRaptor.buildTransfersHashMap();
        gtfsDataReaderWriterForRaptor.filterTransfersHashMap();
        gtfsDataReaderWriterForRaptor.filterHashMapsOnLatLong();
        gtfsDataReaderWriterForRaptor.makeTransfersTransitive();

        String raptorFolderPath = "";
        String raptorRoutesFilePath = raptorFolderPath + "/routes.txt";
        gtfsDataReaderWriterForRaptor.writeRaptorRoutes(raptorRoutesFilePath);

        String raptorRouteStopsFilePath = raptorFolderPath + "/routeStops.txt";
        gtfsDataReaderWriterForRaptor.writeRaptorRouteStops(raptorRouteStopsFilePath);

        String tripsFilePath = raptorFolderPath + "/trips.txt";
        gtfsDataReaderWriterForRaptor.writeTrips(tripsFilePath);

        String raptorStopTimesFilePath = raptorFolderPath + "/stop_times.txt";
        gtfsDataReaderWriterForRaptor.writeRaptorStopTimes(raptorStopTimesFilePath);

        String raptorStopsFilePath = raptorFolderPath + "/stops.txt";
        gtfsDataReaderWriterForRaptor.writeRaptorStops(raptorStopsFilePath);

        String raptorStopRoutesFilePath = raptorFolderPath + "/stopRoutes.txt";
        gtfsDataReaderWriterForRaptor.writeRaptorStopRoutes(raptorStopRoutesFilePath);

        String raptorTransfersFilePath = raptorFolderPath + "/transfers.txt";
        gtfsDataReaderWriterForRaptor.writeRaptorTransfers(raptorTransfersFilePath);

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
        int maxTripLegs = 0;

        // Initialize RAPTOR
        HashMap<Integer, HashMap<String, Integer>> tripLegWiseEarliestArrivalTimeMap = initializeAlgorithm(stops,
                maxTripLegs, originStopId, departureTime);
        HashMap<String, Integer> stopWiseEarliestArrivalTimeMap = initializeEarliestArrivalTimeMap(stops);

        for (int currentTripLeg = 1; currentTripLeg <= maxTripLegs; currentTripLeg++) {
            // TODO: Marked stops' arraylist development comes later
            // Accumulating routes serving marked stops from previous round
        }

        // Traversing each route

        // Looking at footpaths

        // Stopping criterion
    }

    /* Initialize algorithm by setting arrival timestamps at all stops for all trip leg numbers to infinity, except the
    one for the origin stop; also initialize a map, and an arraylist for earliest known arrival times
    */
    private static HashMap<Integer, HashMap<String, Integer>> initializeAlgorithm(HashMap<String, Stop> stops,
                                                                                  int maxTripLegs, String originStopId,
                                                                                  int departureTime) {

        HashMap<Integer, HashMap<String, Integer>> tripLegWiseEarliestArrivalTimeAtEachStop = new HashMap<>();
        /* In the above hashmap, external integer keys refer to trip leg numbers, internal string keys to stop IDs, and
        internal integer values to the earliest known arrival times at the pertinent stops; the stop-arrival time pair
        is repeatedly built for every trip leg number iteration
        */

        for (int tripLegNumber = 1; tripLegNumber <= maxTripLegs; tripLegNumber += 1) {
            HashMap<String, Integer> tripLegSpecificEarliestArrivalTimeAtEachStop = new HashMap<>();
            // Strings refer to stop IDs, and integers refer to the pertinent earliest arrival time

            for(String stopId : stops.keySet()) {
                tripLegSpecificEarliestArrivalTimeAtEachStop.put(stopId, Integer.MAX_VALUE);
            }

            tripLegWiseEarliestArrivalTimeAtEachStop.put(tripLegNumber, tripLegSpecificEarliestArrivalTimeAtEachStop);
        }

        tripLegWiseEarliestArrivalTimeAtEachStop.get(1).replace(originStopId, departureTime);
        return tripLegWiseEarliestArrivalTimeAtEachStop;
    }

    private static HashMap<String, Integer> initializeEarliestArrivalTimeMap(HashMap<String, Stop> stops) {
        HashMap<String, Integer> stopWiseEarliestArrivalTimeMap = new HashMap<>();
        /* String keys refer to stop IDs, and integer values refer to the earliest arrival time at each stop,
        irrespective of the trip leg
        */

        for(String stopId : stops.keySet()) {
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

        for(String currentMarkedStopId : markedStopsIdsList) {
            for (String markedStopSpecificRouteId : stopRoutesMap.get(currentMarkedStopId).getRouteIdList()) {
                if (accumulatedRoutesStopsMap.containsKey(markedStopSpecificRouteId)) {
                    String existingStopId = accumulatedRoutesStopsMap.get(markedStopSpecificRouteId);
                    for (Map.Entry<Integer, String> sequenceExistingStopIdEntry : routeStopsMap.
                            get(markedStopSpecificRouteId).getStopSequenceMap().entrySet()) {
                        if (sequenceExistingStopIdEntry.getValue() == existingStopId) {
                            int existingStopSequence = sequenceExistingStopIdEntry.getKey();
                            for (Map.Entry<Integer, String> sequenceCurrentStopIdEntry : routeStopsMap.
                                    get(markedStopSpecificRouteId).getStopSequenceMap().entrySet()) {
                                if (sequenceCurrentStopIdEntry.getValue() == currentMarkedStopId) {
                                    int currentStopSequence = sequenceCurrentStopIdEntry.getKey();

                                    if (currentStopSequence < existingStopSequence) {
                                        accumulatedRoutesStopsMap.replace(markedStopSpecificRouteId,
                                                currentMarkedStopId);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    accumulatedRoutesStopsMap.put(markedStopSpecificRouteId, currentMarkedStopId);
                }
            }
        }

        return accumulatedRoutesStopsMap;
    }

}