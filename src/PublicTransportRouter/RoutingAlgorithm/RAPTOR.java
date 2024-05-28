package src.PublicTransportRouter.RoutingAlgorithm;

// TODO: VELOCIRAPTORRRRR DESPITE THE SEQUENCING BS
// TODO: AVOID USAGE OF ROUTESTOPS and STOPROUTES AT ALL COSTS, USE STOPTIMES FOR ERROR AVOIDANCE (MISSING STOPS AND EXTRA STOPS ISSUE)
// TODO: HOW TO FIX A DEPARTURE AT 23:59 USING THIS VERY CLASS
// TODO: CHECK STOPPING CONDITION AND BEYOND FIRST-LEG ITERATIONS FO SHO AND ALSO OVERALL LOOPING STRUCTURE
// TODO: DONT TRAVERSE ROUTES, TRAVERSE STOPTIME AND TRIPS TO AVOID THE UNIDIRECTIONAL PROBLEM

import java.util.ArrayList;
import java.util.HashMap;

import src.PublicTransportRouter.GTFSDataManager.*;

public class RAPTOR {

    // Determine the shortest public transport path for a single query
    public String findShortestTransitPath(int originStopId, int destinationStopId, int desiredDepartureTime,
                                          HashMap<Integer, RouteStop> routeStops,
                                          HashMap<Integer, StopTime> stopTimes,
                                          HashMap<Integer, Stop> stops,
                                          HashMap<Integer, StopRoute> stopRoutes,
                                          HashMap<Integer, Transfer> transfers) {
        // Initialize RAPTOR
        int tripLegNumber = 1;
        HashMap<Integer, HashMap<Integer, Double>> tripLegWiseEarliestArrivalTimeMap = new HashMap<>();
        HashMap<Integer, Double> summaryEarliestArrivalTimeMap = initializeSummaryEarliestArrivalTimeMap(stops);
        summaryEarliestArrivalTimeMap.replace(originStopId, (double) desiredDepartureTime);

        // Add the origin stop to the list of marked stops, only for the first round of route scans
        ArrayList<Integer> markedStops = new ArrayList<>();
        markedStops.add(originStopId);
        HashMap<Integer, Integer> routesServingMarkedStops = new HashMap<>();

        // Core RAPTOR loops
        while (!markedStops.isEmpty()) {
            // Initialize trip-leg specific earliest arrival time map
            initializeTripLegSpecificArrivalTimeMap(tripLegNumber, originStopId, desiredDepartureTime,
                    tripLegWiseEarliestArrivalTimeMap, stops);

            routesServingMarkedStops.clear();
            // Accumulate routes serving the marked stops (empties the list of marked stops)
            accumulateRoutesFromStops(markedStops, stopRoutes, routeStops, routesServingMarkedStops);

            // Traverse each route
            traverseEachRoute(destinationStopId, tripLegNumber, markedStops, routesServingMarkedStops,
                    stopTimes, routeStops, summaryEarliestArrivalTimeMap, tripLegWiseEarliestArrivalTimeMap);

            // Look at footpaths
            lookAtFootpaths(tripLegNumber, markedStops, transfers, summaryEarliestArrivalTimeMap,
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

    /* Initialize algorithm by setting arrival timestamps at all stops (except the origin stop) for all trip leg numbers
    to infinity
    */
    private void initializeTripLegSpecificArrivalTimeMap (int tripLegNumber,
                                                          int originStopId,
                                                          int desiredDepartureTime,
                                                          HashMap<Integer, HashMap<Integer, Double>>
                                                                  previousEarliestArrivalTimeMap,
                                                          HashMap<Integer, Stop> stops) {
        /* In the method argument's hashmap, external integer keys refer to trip leg numbers, internal integer keys to
        stop IDs, and internal decimal values to the earliest known arrival times at the corresponding stops; the
        stop-arrival time pair is repeatedly built for every trip leg number iteration, in the external code
        */
        if (tripLegNumber == 1) {
            HashMap<Integer, Double> firstEarliestArrivalTimeMap = new HashMap<>();
            // Keys refer to stop IDs, and values refer to corresponding earliest arrival times
            for (int stopId : stops.keySet()) {
                firstEarliestArrivalTimeMap.put(stopId, Double.MAX_VALUE);
            }
            firstEarliestArrivalTimeMap.replace(originStopId, (double) desiredDepartureTime);
            previousEarliestArrivalTimeMap.put(tripLegNumber, firstEarliestArrivalTimeMap);
        } else {
            // Replicate earliest arrival times reported in previous iterations
            previousEarliestArrivalTimeMap.put(tripLegNumber, previousEarliestArrivalTimeMap.get(tripLegNumber - 1));
        }
    }

    // Initialize a summary map for the earliest known arrival time at each stop
    private HashMap<Integer, Double> initializeSummaryEarliestArrivalTimeMap(HashMap<Integer, Stop> stops) {
        HashMap<Integer, Double> summaryEarliestArrivalTimeMap = new HashMap<>();
        // Keys refer to stop IDs, and values refer to the earliest arrival time at each stop, irrespective of trip leg

        for (int stopId : stops.keySet()) {
            summaryEarliestArrivalTimeMap.put(stopId, Double.MAX_VALUE);
        }
        return summaryEarliestArrivalTimeMap;
    }

    // Accumulate routes and hop-on stops based on marked stops' IDs and "stopRoutes" hashmap
    private void accumulateRoutesFromStops(ArrayList<Integer> markedStops,
                                           HashMap<Integer, StopRoute> stopRoutes,
                                           HashMap<Integer, RouteStop> routeStops,
                                           HashMap<Integer, Integer> routesServingMarkedStops) {
        /* In the method arguments, the arraylist's integers are stop IDs to be iterated over, and the first hashmap's
        keys are all stop IDs in the study area, which are mapped to lists of routes (one for every stop); the second
        hashmap is for finding the earliest possible stop to hop-on when changing from one route to another

        The last hashmap is modified in this method, which then contains routes to be traversed in RAPTOR
        */

        for (int currentMarkedStopId : markedStops) {
            for (int markedStopSpecificRouteId : stopRoutes.get(currentMarkedStopId).getRouteIdList()) {

                if (routesServingMarkedStops.containsKey(markedStopSpecificRouteId)) {
                    int existingMarkedStopId = routesServingMarkedStops.get(markedStopSpecificRouteId);

                    // If earlier-sequenced stops are found on the same route, traversal must begin there
                    if (routeStops.get(markedStopSpecificRouteId).getStopSequenceMap().get(currentMarkedStopId) <
                    routeStops.get(markedStopSpecificRouteId).getStopSequenceMap().get(existingMarkedStopId)) {
                        routesServingMarkedStops.replace(markedStopSpecificRouteId, currentMarkedStopId);
                    }
                } else {
                    routesServingMarkedStops.put(markedStopSpecificRouteId, currentMarkedStopId);
                }
            }
            markedStops.remove(currentMarkedStopId);
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
            StopTime stopTimeForTripFinding = stopTimes.get(routeStopPair.getKey());

            int indexToStartTraversal = routeStops.get(routeStopPair.getKey()).getStopSequenceMap().get(routeStopPair.
                    getValue()) - 1;

            int tripIdForTraversal = -1;
            for (HashMap.Entry<Integer, ArrayList<StopTimeTriplet>> tripConsideredForTraversal : stopTimes.
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
                StopTimeTriplet stopTimeTriplet = stopTimes.get(routeStopPair.getKey()).getTripWiseStopTimeLists().
                        get(tripIdForTraversal).get(traversalIndex);

                if (stopTimeTriplet.getArrivalTime() < Math.min(summaryEarliestArrivalTimeMap.get(routeStopPair.
                        getValue()), summaryEarliestArrivalTimeMap.get(destinationStopId))) {
                    tripLegWiseEarliestArrivalTimeMap.get(tripLegNumber).replace(stopTimeTriplet.getStopId(),
                            (double) stopTimeTriplet.getArrivalTime());
                    summaryEarliestArrivalTimeMap.replace(stopTimeTriplet.getStopId(),
                            (double) stopTimeTriplet.getArrivalTime());
                    markedStopsIdsList.add(stopTimeTriplet.getStopId());
                }

                /* Check to see if an earlier trip can be found at the concerned stop (check does not apply to first
                trip leg, as it is certain that no earlier trips could be caught at any of the iterated over stops)
                */
                if (tripLegNumber > 1) {
                    if (tripLegWiseEarliestArrivalTimeMap.get(tripLegNumber - 1).get(stopTimeTriplet.getStopId()) <
                            stopTimeTriplet.getArrivalTime()) {

                        for (HashMap.Entry<Integer, ArrayList<StopTimeTriplet>> newTripConsideredForTraversal :
                                stopTimes.get(routeStopPair.getKey()).getTripWiseStopTimeLists().entrySet()) {
                            if (newTripConsideredForTraversal.getValue().get(traversalIndex).getDepartureTime() >
                                    summaryEarliestArrivalTimeMap.get(stopTimeTriplet.getStopId())) {
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