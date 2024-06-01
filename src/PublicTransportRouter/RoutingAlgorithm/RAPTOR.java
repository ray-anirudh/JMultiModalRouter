package src.PublicTransportRouter.RoutingAlgorithm;
// todo: Remember Waldperlach and Putzbrunn null pointer exception possibilities

// TODO: VELOCIRAPTORRRRR DESPITE THE SEQUENCING BS (VELOCIRAPTOR IS RAZOR SHARP AND SUPERFAST FOR BIPOINT QUERIES)
// TODO: AVOID USAGE OF ROUTESTOPS and STOPROUTES, USE STOPTIMES FOR ERROR AVOIDANCE (MISSING STOPS AND EXTRA STOPS ISSUE)
// TODO: HOW TO FIX A DEPARTURE AT 23:59 USING THIS VERY CLASS (NOT JUST DEPARTURES, BUT ALSO ITERATED OVER ARRIVAL TIMES)

// TODO: CHECK STOPPING CONDITION AND BEYOND FIRST-LEG ITERATIONS FO SHO AND ALSO OVERALL LOOPING STRUCTURE
// TODO: DONT TRAVERSE ROUTES, TRAVERSE STOPTIME AND TRIPS TO AVOID THE BIDIRECTIONAL PROBLEM AND ROUTEID SHARING ISSUES AND SHORT TRIP IDS
// TODO: FOR FASTER LOOKUPS, BUILD TRAVERSAL, BUT WITH SAFETY NETS FOR ROUTESTOPS AND STOPROUTES

// TODO: QUERY PARSING TO PEERFECT INPUT TYPES MUST HAPPEN IN THE CALLER, AND NOT HERE, OR IN THE QUERY CODE (SUCH AS LOCATING STOP AND THEN HOMING IN ON THE STOPIDS)

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Iterator;

import src.PublicTransportRouter.GTFSDataManager.*;

public class RAPTOR {

    // Determine the fastest public transport path for a single query
    public String findShortestTransitPath(int originStopId, int destinationStopId, int desiredDepartureTime,
                                          LinkedHashMap<Integer, RouteStop> routeStops,
                                          LinkedHashMap<Integer, StopTime> stopTimes,
                                          LinkedHashMap<Integer, Stop> stops,
                                          LinkedHashMap<Integer, StopRoute> stopRoutes,
                                          LinkedHashMap<Integer, Transfer> transfers) {
        String output = "A valid public transport path does not exist between " + stops.get(originStopId).getStopName()
                + " and " + stops.get(destinationStopId).getStopName() + " at the specified departure time.";

        // Initialize RAPTOR
        int tripLegNumber = 1;
        LinkedHashMap<Integer, LinkedHashMap<Integer, Double>> tripLegWiseEarliestArrivalTimeMap =
                new LinkedHashMap<>();
        LinkedHashMap<Integer, Double> summaryEarliestArrivalTimeMap = initializeSummaryEarliestArrivalTimeMap(stops);
        summaryEarliestArrivalTimeMap.replace(originStopId, (double) desiredDepartureTime);

        // Add the origin stop to the list of marked stops, only for the first round of route scans
        ArrayList<Integer> markedStops = new ArrayList<>();
        markedStops.add(originStopId);
        LinkedHashMap<Integer, Integer> routesServingMarkedStops = new LinkedHashMap<>();

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

        if(summaryEarliestArrivalTimeMap.get(destinationStopId) != Double.MAX_VALUE) {
            output = "Origin stop: " + stops.get(originStopId).getStopName() + "\n" +
                    "Destination stop: " + stops.get(destinationStopId).getStopName() + "\n" +
                    "Departure time from origin stop: " + desiredDepartureHour + ":" + desiredDepartureMinute + "\n" +
                    "Earliest possible arrival time at destination: " + earliestArrivalHourAtDestination + ":" +
                    earliestArrivalMinuteAtDestination + "\n";
        }

        return output;
    }

    /* Initialize algorithm by setting arrival timestamps at all stops (except the origin stop) for all trip leg numbers
    to infinity
    */
    private void initializeTripLegSpecificArrivalTimeMap (int tripLegNumber,
                                                          int originStopId,
                                                          int desiredDepartureTime,
                                                          LinkedHashMap<Integer, LinkedHashMap<Integer, Double>>
                                                                  previousEarliestArrivalTimeMap,
                                                          LinkedHashMap<Integer, Stop> stops) {
        /* In the method arguments' first hashmap, external integer keys refer to trip leg numbers, internal integer
        keys to stop IDs, and internal decimal values to the known earliest arrival times at the corresponding stops;
        the stop-arrival time pair is repeatedly built for every trip leg number iteration, in the external code
        */
        if (tripLegNumber == 1) {
            LinkedHashMap<Integer, Double> firstEarliestArrivalTimeMap = new LinkedHashMap<>();
            // Keys refer to stop IDs, and values refer to corresponding earliest arrival times
            for (int stopId : stops.keySet()) {
                firstEarliestArrivalTimeMap.put(stopId, Double.MAX_VALUE);
            }
            firstEarliestArrivalTimeMap.replace(originStopId, (double) desiredDepartureTime);
            previousEarliestArrivalTimeMap.put(tripLegNumber, firstEarliestArrivalTimeMap);
        } else {
            // Replicate earliest arrival times reported in previous trip-legs
            previousEarliestArrivalTimeMap.put(tripLegNumber, previousEarliestArrivalTimeMap.get(tripLegNumber - 1));
        }
    }

    // Initialize a summary map for the known earliest arrival time at each stop
    private LinkedHashMap<Integer, Double> initializeSummaryEarliestArrivalTimeMap(LinkedHashMap<Integer, Stop> stops) {
        LinkedHashMap<Integer, Double> summaryEarliestArrivalTimeMap = new LinkedHashMap<>();
        // Keys refer to stop IDs, and values refer to the earliest arrival time at each stop, irrespective of trip leg

        for (int stopId : stops.keySet()) {
            summaryEarliestArrivalTimeMap.put(stopId, Double.MAX_VALUE);
        }
        return summaryEarliestArrivalTimeMap;
    }

    // Accumulate routes and hop-on stops based on marked stops' IDs and "stopRoutes" hashmap
    private void accumulateRoutesFromStops(ArrayList<Integer> markedStops,
                                           LinkedHashMap<Integer, StopRoute> stopRoutes,
                                           LinkedHashMap<Integer, RouteStop> routeStops,
                                           LinkedHashMap<Integer, Integer> routesServingMarkedStops) {
        /* In the method arguments, the arraylist's integers are the stop IDs to be iterated over, and the first
        hashmap's keys are all stop IDs in the study area, which are mapped to lists of routes (one for every stop); the
        second hashmap is for finding the earliest possible stop to hop-on when changing from one route to another

        The last hashmap is modified in this method, which then contains routes to be traversed in RAPTOR
        */

        for (int markedStopId : markedStops) {
            for (int markedStopBasedRouteId : stopRoutes.get(markedStopId).getRouteList()) {

                if (routesServingMarkedStops.containsKey(markedStopBasedRouteId)) {
                    int preexistingStopId = routesServingMarkedStops.get(markedStopBasedRouteId);

                    LinkedHashMap<Integer, Integer> routeBasedStopSequenceDirectionOne = routeStops.
                            get(markedStopBasedRouteId).getDirectionWiseStopSequenceMap().get(1);
                    LinkedHashMap<Integer, Integer> routeBasedStopSequenceDirectionTwo = routeStops.
                            get(markedStopBasedRouteId).getDirectionWiseStopSequenceMap().get(2);

                    // Traversal must begin at the earliest-sequenced stop on a route (characteristic of direction)
                    if ((routeBasedStopSequenceDirectionOne.containsKey(preexistingStopId)) &&
                            (routeBasedStopSequenceDirectionOne.containsKey(markedStopId))) {
                        if (routeBasedStopSequenceDirectionOne.get(markedStopId) < routeBasedStopSequenceDirectionOne.
                                get(preexistingStopId)) {
                            routesServingMarkedStops.replace(markedStopBasedRouteId, markedStopId);
                        }
                    } else if (routeBasedStopSequenceDirectionTwo.containsKey(preexistingStopId) &&
                            routeBasedStopSequenceDirectionTwo.containsKey(markedStopId)) {
                        if (routeBasedStopSequenceDirectionTwo.get(markedStopId) < routeBasedStopSequenceDirectionTwo.
                                get(preexistingStopId)) {
                            routesServingMarkedStops.replace(markedStopBasedRouteId, markedStopId);
                        }
                    }
                } else {
                    routesServingMarkedStops.put(markedStopBasedRouteId, markedStopId);
                }
            }
        }
        markedStops.clear();
    }

    // Traverse each route from the list of accumulated routes
    public static void traverseEachRoute(int destinationStopId,
                                         int tripLegNumber,
                                         ArrayList<Integer> markedStopsIdsList,
                                         LinkedHashMap<Integer, Integer> accumulatedRoutesFromStopsMap,
                                         LinkedHashMap<Integer, StopTime> stopTimes,
                                         LinkedHashMap<Integer, RouteStop> routeStops,
                                         LinkedHashMap<Integer, Double> summaryEarliestArrivalTimeMap,
                                         LinkedHashMap<Integer, LinkedHashMap<Integer, Double>>
                                                 tripLegWiseEarliestArrivalTimeMap) {

        for (HashMap.Entry<Integer, Integer> routeStopPair : accumulatedRoutesFromStopsMap.entrySet()) {
            LinkedHashMap<Integer, LinkedHashMap<Integer, StopTimeTriplet>> tripWiseStopTimeMaps = stopTimes.
                    get(routeStopPair.getKey()).getTripWiseStopTimeMaps();
            LinkedHashMap<Integer, LinkedHashMap<Integer, Integer>> routeStop = routeStops.get(routeStopPair.getKey()).
                    getDirectionWiseStopSequenceMap();

            int tripIdForTraversal = -1;
            for (HashMap.Entry<Integer, LinkedHashMap<Integer, StopTimeTriplet>> tripSpecificStopTimeMap :
                    tripWiseStopTimeMaps.entrySet()) {

                StopTimeTriplet stopTimeTriplet = tripSpecificStopTimeMap.getValue().get(routeStopPair.getValue());
                if ((stopTimeTriplet != null) && (stopTimeTriplet.getDepartureTime() >= summaryEarliestArrivalTimeMap.
                        get(routeStopPair.getValue()))) {
                    tripIdForTraversal = tripSpecificStopTimeMap.getKey();
                    break;
                }
            }

            LinkedHashMap<Integer, StopTimeTriplet> tripBeingTraversed = tripWiseStopTimeMaps.get(tripIdForTraversal);
            Iterator<HashMap.Entry<Integer, StopTimeTriplet>> tripIterator = tripBeingTraversed.entrySet().iterator();

            while (tripIterator.hasNext()) {
                HashMap.Entry<Integer, StopTimeTriplet> stopTimeTripletEntry = tripIterator.next();
                if(stopTimeTripletEntry.getKey().equals(routeStopPair.getValue())) {
                    break;
                }
            }

            while (tripIterator.hasNext()) {
                HashMap.Entry<Integer, StopTimeTriplet> stopTimeTripletEntry = tripIterator.next();
                if (stopTimeTripletEntry.getValue().getArrivalTime() < Math.min(summaryEarliestArrivalTimeMap.
                        get(stopTimeTripletEntry.getKey()), summaryEarliestArrivalTimeMap.get(destinationStopId))) {
                    tripLegWiseEarliestArrivalTimeMap.get(tripLegNumber).replace(stopTimeTripletEntry.getKey(),
                            (double) stopTimeTripletEntry.getValue().getArrivalTime());
                    summaryEarliestArrivalTimeMap.replace(stopTimeTriplet.getStopId(),
                            (double) stopTimeTriplet.getArrivalTime());
                    markedStopsIdsList.add(stopTimeTriplet.getStopId());
            }



            for (int traversalIndex = indexToStartTraversal; traversalIndex <= tripBeingTraversed.size() - 1;
                 traversalIndex++) {
                StopTimeTriplet stopTimeTriplet = stopTimes.get(routeStopPair.getKey()).getTripWiseStopTimeMaps().
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
                                stopTimes.get(routeStopPair.getKey()).getTripWiseStopTimeMaps().entrySet()) {
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
        LinkedHashMap<Integer, Transfer> transfers,
        LinkedHashMap<Integer, Double> summaryEarliestArrivalTimeMap,
        LinkedHashMap<Integer, LinkedHashMap<Integer, Double>> tripLegWiseEarliestArrivalTimeMap) {

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