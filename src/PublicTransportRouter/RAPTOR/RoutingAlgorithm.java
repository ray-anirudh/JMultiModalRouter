package src.PublicTransportRouter.RAPTOR;

import java.util.ArrayList;
import java.util.HashMap;

import src.PublicTransportRouter.GTFSDataManager.*;

public class RoutingAlgorithm {

    // Determine an exact shortest public transport path for a single query only
    public String findExactShortestTransitPath(int originStopId,
                                               int destinationStopId,
                                               int desiredDepartureTime,
                                               HashMap<Integer, Stop> stops,
                                               HashMap<Integer, StopRoute> stopRoutes,
                                               HashMap<Integer, RouteStop> routeStops,
                                               HashMap<Integer, StopTime> stopTimes,
                                               HashMap<Integer, Transfer> transfers) {
        // Initialize RAPTOR
        int tripLegNumber = 1;
        HashMap<Integer, HashMap<Integer, Double>> initialEarliestArrivalTimeMap = new HashMap<>();
        HashMap<Integer, HashMap<Integer, Double>> tripLegWiseEarliestArrivalTimeMap =
                initializeTripLegSpecificArrivalTimeMap(tripLegNumber, initialEarliestArrivalTimeMap, stops);
        HashMap<Integer, Double> summaryEarliestArrivalTimeMap = initializeSummaryEarliestArrivalTimeMap(stops);

        // Set origin stop's "earliest arrival time" to "desired departure time"
        tripLegWiseEarliestArrivalTimeMap.get(1).replace(originStopId, (double) desiredDepartureTime);
        summaryEarliestArrivalTimeMap.replace(originStopId, (double) desiredDepartureTime);

        // Add the origin stop to the list of marked stops, for the first round of route scans
        ArrayList<Integer> markedStopsIdsList = new ArrayList<>();
        markedStopsIdsList.add(originStopId);
        HashMap<Integer, Integer> accumulatedRoutesFromStopsMap = new HashMap<>();

        // Core algorithm loops
        while (!markedStopsIdsList.isEmpty()) {
        /* Accumulate routes (route IDs) serving marked stops (stop IDs) from previous round, and add another earliest
        arrival time map
        */
            accumulatedRoutesFromStopsMap.clear();
            accumulateRoutesFromStops(markedStopsIdsList, stopRoutes, routeStops, accumulatedRoutesFromStopsMap);
            initializeTripLegSpecificArrivalTimeMap(tripLegNumber, tripLegWiseEarliestArrivalTimeMap, stops);

            // Traverse each route
            traverseEachRoute(destinationStopId, tripLegNumber, markedStopsIdsList, accumulatedRoutesFromStopsMap,
                    stopTimes, routeStops, summaryEarliestArrivalTimeMap, tripLegWiseEarliestArrivalTimeMap);

            // Look at footpaths
            lookAtFootpaths(tripLegNumber, markedStopsIdsList, transfers, summaryEarliestArrivalTimeMap,
                    tripLegWiseEarliestArrivalTimeMap);

            tripLegNumber++;
        }

        int desiredDepartureHour = desiredDepartureTime / 60;
        int desiredDepartureMinute = desiredDepartureTime % 60;

        double earliestArrivalTimeAtDestination = summaryEarliestArrivalTimeMap.get(destinationStopId);
        double earliestArrivalHourAtDestination = earliestArrivalTimeAtDestination / 60;
        double earliestArrivalMinuteAtDestination = earliestArrivalTimeAtDestination % 60;

        return ("Origin stop: " + stops.get(originStopId).getStopName() + "\n" +
                "Destination stop: " + stops.get(destinationStopId).getStopName() + "\n" +
                "Departure time from origin stop: " + desiredDepartureHour + ":" + desiredDepartureMinute + "\n" +
                "Earliest possible arrival time at destination: " + earliestArrivalHourAtDestination + ":" +
                earliestArrivalMinuteAtDestination + "\n");
    }

    /* Initialize algorithm by setting arrival timestamps at all stops for all trip leg numbers to infinity, except the
    one for the origin stop
    */
    private static HashMap<Integer, HashMap<Integer, Double>> initializeTripLegSpecificArrivalTimeMap
    (int tripLegNumber, HashMap<Integer, HashMap<Integer, Double>> initialEarliestArrivalTimeMap,
     HashMap<Integer, Stop> stops) {
        /* In the above hashmap, external integer keys refer to trip leg numbers, internal integer keys to stop IDs, and
        internal decimal values to the earliest known arrival times at the corresponding stops; the stop-arrival time
        pair is repeatedly built for every trip leg number iteration, in the external code
        */

        if (tripLegNumber == 1) {
            HashMap<Integer, Double> tripLegSpecificEarliestArrivalTimeMap = new HashMap<>();
            // Keys refer to stop IDs, and values refer to the corresponding earliest arrival time
            for (int stopId : stops.keySet()) {
                tripLegSpecificEarliestArrivalTimeMap.put(stopId, Double.MAX_VALUE);
            }
            initialEarliestArrivalTimeMap.put(tripLegNumber, tripLegSpecificEarliestArrivalTimeMap);
        } else {
            // Replicate earliest arrival times reported in previous iterations
            initialEarliestArrivalTimeMap.put(tripLegNumber, initialEarliestArrivalTimeMap.get(tripLegNumber - 1));
        }
        return initialEarliestArrivalTimeMap;
    }

    // Initialize a summary map for the earliest known arrival time at each stop
    private static HashMap<Integer, Double> initializeSummaryEarliestArrivalTimeMap(
            HashMap<Integer, Stop> stops) {
        HashMap<Integer, Double> summaryEarliestArrivalTimeMap = new HashMap<>();
        // Keys refer to stop IDs, and values refer to the earliest arrival time at each stop, irrespective of trip leg

        for (int stopId : stops.keySet()) {
            summaryEarliestArrivalTimeMap.put(stopId, Double.MAX_VALUE);
        }
        return summaryEarliestArrivalTimeMap;
    }

    // Accumulate routes (and hop-on stops) based on stop IDs and "stopRoutes" hashmap
    private static void accumulateRoutesFromStops(ArrayList<Integer> markedStopsIdsList,
                                                  HashMap<Integer, StopRoute> stopRoutes,
                                                  HashMap<Integer, RouteStop> routeStops,
                                                  HashMap<Integer, Integer> accumulatedRoutesFromStopsMap) {
        /* In the method arguments, the arraylist's integers are stop IDs to be iterated over, and the first hashmap's
        keys are all stop IDs in the study area, which are mapped to lists of routes for every stop; the second hashmap
        is for finding the earliest possible stop to hop-on when changing from one route to another
        */

        for (int currentMarkedStopId : markedStopsIdsList) {
            for (int markedStopSpecificRouteId : stopRoutes.get(currentMarkedStopId).getRouteIdList()) {
                if (accumulatedRoutesFromStopsMap.containsKey(markedStopSpecificRouteId)) {
                    int existingMarkedStopId = accumulatedRoutesFromStopsMap.get(markedStopSpecificRouteId);
                    int currentMarkedStopSequence = routeStops.get(markedStopSpecificRouteId).getStopSequenceMap().
                            get(currentMarkedStopId);
                    int existingMarkedStopSequence = routeStops.get(markedStopSpecificRouteId).getStopSequenceMap().
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
    }

    // Traverse each route from the list of accumulated routes
    public static void traverseEachRoute(int destinationStopId,
                                         int tripLegNumber,
                                         ArrayList<Integer> markedStopsIdsList,
                                         HashMap<Integer, Integer> accumulatedRoutesFromStopsMap,
                                         HashMap<Integer, StopTime> stopTimes,
                                         HashMap<Integer, RouteStop> routeStops,
                                         HashMap<Integer, Double> summaryEarliestArrivalTimeMap,
                                         HashMap<Integer, HashMap<Integer, Double>> tripLegWiseEarliestArrivalTimeMap) {

        for (HashMap.Entry<Integer, Integer> routeStopPair : accumulatedRoutesFromStopsMap.entrySet()) {
            int indexToStartTraversal = routeStops.get(routeStopPair.getKey()).getStopSequenceMap().get(routeStopPair.
                    getValue()) - 1;

            int tripIdForTraversal = -1;
            for (HashMap.Entry<Integer, ArrayList<StopTimeQuartet>> tripConsideredForTraversal : stopTimes.
                    get(routeStopPair.getKey()).getTripWiseStopTimeLists().entrySet()) {
                if (tripConsideredForTraversal.getValue().get(indexToStartTraversal).getDepartureTime() >
                        summaryEarliestArrivalTimeMap.get(tripConsideredForTraversal.getValue().
                                get(indexToStartTraversal).getStopId())) {
                    tripIdForTraversal = tripConsideredForTraversal.getKey();
                    break;
                }
            }

            for (int traversalIndex = indexToStartTraversal; traversalIndex <= stopTimes.get(routeStopPair.getKey()).
                    getTripWiseStopTimeLists().get(tripIdForTraversal).size() - 1; traversalIndex++) {
                StopTimeQuartet stopTimeQuartet = stopTimes.get(routeStopPair.getKey()).getTripWiseStopTimeLists().
                        get(tripIdForTraversal).get(traversalIndex);

                if (stopTimeQuartet.getArrivalTime() < Math.min(summaryEarliestArrivalTimeMap.get(routeStopPair.
                        getValue()), summaryEarliestArrivalTimeMap.get(destinationStopId))) {
                    tripLegWiseEarliestArrivalTimeMap.get(tripLegNumber).replace(stopTimeQuartet.getStopId(),
                            (double) stopTimeQuartet.getArrivalTime());
                    summaryEarliestArrivalTimeMap.replace(stopTimeQuartet.getStopId(),
                            (double) stopTimeQuartet.getArrivalTime());
                    markedStopsIdsList.add(stopTimeQuartet.getStopId());
                }

                /* Check to see if an earlier trip can be found at the concerned stop (check does not apply to first
                trip leg, as it is certain that no earlier trips could be caught at any of the iterated over stops)
                */
                if (tripLegNumber > 1) {
                    if (tripLegWiseEarliestArrivalTimeMap.get(tripLegNumber - 1).get(stopTimeQuartet.getStopId()) <
                            stopTimeQuartet.getArrivalTime()) {

                        for (HashMap.Entry<Integer, ArrayList<StopTimeQuartet>> newTripConsideredForTraversal :
                                stopTimes.get(routeStopPair.getKey()).getTripWiseStopTimeLists().entrySet()) {
                            if (newTripConsideredForTraversal.getValue().get(traversalIndex).getDepartureTime() >
                                    summaryEarliestArrivalTimeMap.get(stopTimeQuartet.getStopId())) {
                                tripIdForTraversal = newTripConsideredForTraversal.getKey();
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    // Look at footpaths to update arrival times
    public static void lookAtFootpaths(int tripLegNumber,
                                       ArrayList<Integer> markedStopsIdsList,
                                       HashMap<Integer, Transfer> transfers,
                                       HashMap<Integer, Double> summaryEarliestArrivalTimeMap,
                                       HashMap<Integer, HashMap<Integer, Double>> tripLegWiseEarliestArrivalTimeMap) {

        for (int markedStopId : markedStopsIdsList) {
            for (HashMap.Entry<Integer, Double> transferEntry : transfers.get(markedStopId).getTransferMap().
                    entrySet()) {
                double earliestArrivalTimeViaTransfer = Math.min(tripLegWiseEarliestArrivalTimeMap.get(tripLegNumber).
                                get(transferEntry.getKey()), tripLegWiseEarliestArrivalTimeMap.get(tripLegNumber).
                                get(markedStopId) + transferEntry.getValue());

                tripLegWiseEarliestArrivalTimeMap.get(tripLegNumber).replace(transferEntry.getKey(),
                        earliestArrivalTimeViaTransfer);
                summaryEarliestArrivalTimeMap.replace(transferEntry.getKey(), earliestArrivalTimeViaTransfer);

                markedStopsIdsList.add(transferEntry.getKey());
            }
        }
    }
}