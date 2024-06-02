package src.PublicTransportRouter.RoutingAlgorithm;
// TODO: FINAL REVIEW OF CODE BEFORE MOVING ON SHOULD COVER SPEED, EXCEPTIONS, ERROS, LOGIC FLAWS, AND ANNOTATIONS (AND ALL THE TIME 23:59 trips)
// TODO: VELOCIRAPTORRRRR DESPITE THE SEQUENCING BS (VELOCIRAPTOR IS RAZOR SHARP AND SUPERFAST FOR BIPOINT QUERIES)

// TODO: HOW TO FIX A DEPARTURE AT 23:59 USING THIS VERY CLASS (NOT JUST DEPARTURES, BUT ALSO ITERATED OVER ARRIVAL TIMES)
// TODO: CHECK STOPPING CONDITION AND BEYOND FIRST-LEG ITERATIONS FO SHO AND ALSO OVERALL LOOPING STRUCTURE

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Iterator;

import org.jetbrains.annotations.NotNull;
import src.MultiModalRouter.TransitQuery;
import src.PublicTransportRouter.GTFSDataManager.*;

public class RAPTOR {

    // Determine the earliest arrival time for a single transit-based query
    public TransitQueryResponse findShortestTransitPath(@NotNull TransitQuery transitQuery,
                                                        LinkedHashMap<Integer, RouteStop> routeStops,
                                                        LinkedHashMap<Integer, StopTime> stopTimes,
                                                        @NotNull LinkedHashMap<Integer, Stop> stops,
                                                        LinkedHashMap<Integer, StopRoute> stopRoutes,
                                                        LinkedHashMap<Integer, Transfer> transfers) {
        // Parse query information
        int originStopId = transitQuery.getOriginStopId();
        int destinationStopId = transitQuery.getDestinationStopId();
        int desiredDepartureTime = transitQuery.getDesiredDepartureTimeMinutes();

        // Initialize earliest arrival time output
        TransitQueryResponse transitQueryResponse = new TransitQueryResponse(-1, -1);

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
                    stopTimes, summaryEarliestArrivalTimeMap, tripLegWiseEarliestArrivalTimeMap);

            // Look at footpaths
            lookAtFootpaths(tripLegNumber, markedStops, transfers, summaryEarliestArrivalTimeMap,
                    tripLegWiseEarliestArrivalTimeMap);

            tripLegNumber++;
        }

        // Return earliest arrival time output
        double earliestArrivalTimeAtDestination = summaryEarliestArrivalTimeMap.get(destinationStopId);
        if (earliestArrivalTimeAtDestination != Double.MAX_VALUE) {
            if (earliestArrivalTimeAtDestination < 1440) {
                transitQueryResponse = new TransitQueryResponse((int) (Math.round(summaryEarliestArrivalTimeMap.
                        get(destinationStopId))), (int) (Math.round(summaryEarliestArrivalTimeMap.
                        get(destinationStopId))) - desiredDepartureTime);
            } else {
                transitQueryResponse = new TransitQueryResponse((int) (Math.round
                        (summaryEarliestArrivalTimeMap.get(destinationStopId))) % 1440, (int) (Math.round
                        (summaryEarliestArrivalTimeMap.get(destinationStopId))) - desiredDepartureTime);
            }
        }

        return transitQueryResponse;
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
    @NotNull
    private LinkedHashMap<Integer, Double> initializeSummaryEarliestArrivalTimeMap(
            @NotNull LinkedHashMap<Integer, Stop> stops) {
        LinkedHashMap<Integer, Double> summaryEarliestArrivalTimeMap = new LinkedHashMap<>();
        // Keys refer to stop IDs, and values refer to the earliest arrival time at each stop, irrespective of trip leg

        for (int stopId : stops.keySet()) {
            summaryEarliestArrivalTimeMap.put(stopId, Double.MAX_VALUE);
        }
        return summaryEarliestArrivalTimeMap;
    }

    // Accumulate routes and hop-on stops based on marked stops' IDs and "stopRoutes" hashmap
    private void accumulateRoutesFromStops(@NotNull ArrayList<Integer> markedStops,
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
                                         ArrayList<Integer> markedStops,
                                         @NotNull LinkedHashMap<Integer, Integer> accumulatedRoutesFromStopsMap,
                                         LinkedHashMap<Integer, StopTime> stopTimes,
                                         LinkedHashMap<Integer, Double> summaryEarliestArrivalTimeMap,
                                         LinkedHashMap<Integer, LinkedHashMap<Integer, Double>>
                                                 tripLegWiseEarliestArrivalTimeMap) {

        for (HashMap.Entry<Integer, Integer> routeStopPair : accumulatedRoutesFromStopsMap.entrySet()) {
            LinkedHashMap<Integer, LinkedHashMap<Integer, StopTimeTriplet>> tripWiseStopTimeMaps = stopTimes.
                    get(routeStopPair.getKey()).getTripWiseStopTimeMaps();

            int tripIdForTraversal = -1;
            for (HashMap.Entry<Integer, LinkedHashMap<Integer, StopTimeTriplet>> tripSpecificStopTimeMap :
                    tripWiseStopTimeMaps.entrySet()) {

                StopTimeTriplet stopTimeTriplet = tripSpecificStopTimeMap.getValue().get(routeStopPair.getValue());
                if ((stopTimeTriplet != null) && (stopTimeTriplet.getDepartureTime() >= (summaryEarliestArrivalTimeMap.
                        get(routeStopPair.getValue()) % 1440))) {
                    tripIdForTraversal = tripSpecificStopTimeMap.getKey();
                    break;
                }
            }

            LinkedHashMap<Integer, StopTimeTriplet> tripBeingTraversed = tripWiseStopTimeMaps.get(tripIdForTraversal);
            Iterator<HashMap.Entry<Integer, StopTimeTriplet>> tripIterator = tripBeingTraversed.entrySet().iterator();

            double previousArrivalTime = -1;
            while (tripIterator.hasNext()) {
                HashMap.Entry<Integer, StopTimeTriplet> stopTimeTripletEntry = tripIterator.next();
                if (stopTimeTripletEntry.getKey().equals(routeStopPair.getValue())) {
                    previousArrivalTime = stopTimeTripletEntry.getValue().getArrivalTime();
                    break;
                }
            }

            while (tripIterator.hasNext()) {
                HashMap.Entry<Integer, StopTimeTriplet> stopTimeTripletEntry = tripIterator.next();
                double currentArrivalTime = stopTimeTripletEntry.getValue().getArrivalTime();

                if (previousArrivalTime < currentArrivalTime) {
                    if (currentArrivalTime < Math.min(summaryEarliestArrivalTimeMap.get(stopTimeTripletEntry.getKey()),
                            summaryEarliestArrivalTimeMap.get(destinationStopId))) {
                        // Update earliest arrival time map for all trip legs
                        tripLegWiseEarliestArrivalTimeMap.get(tripLegNumber).replace(stopTimeTripletEntry.getKey(),
                                currentArrivalTime);

                        // Update earliest arrival time summary map
                        summaryEarliestArrivalTimeMap.replace(stopTimeTripletEntry.getKey(), currentArrivalTime);

                        // Update list of marked stops
                        markedStops.add(stopTimeTripletEntry.getKey());
                        previousArrivalTime = currentArrivalTime;
                    } else {
                        double minutesToBeAddedForCurrentStop = 0;
                        if (summaryEarliestArrivalTimeMap.get(stopTimeTripletEntry.getKey()) > 2880) {
                            minutesToBeAddedForCurrentStop = 2880;
                        } else if (summaryEarliestArrivalTimeMap.get(stopTimeTripletEntry.getKey()) > 1440) {
                            minutesToBeAddedForCurrentStop = 1440;
                        }

                        double minutesToBeAddedForDestinationStop = 0;
                        if (summaryEarliestArrivalTimeMap.get(destinationStopId) > 2880) {
                            minutesToBeAddedForDestinationStop = 2880;
                        } else if (summaryEarliestArrivalTimeMap.get(destinationStopId) > 1440) {
                            minutesToBeAddedForDestinationStop = 1440;
                        }

                        if (currentArrivalTime + minutesToBeAddedForCurrentStop < Math.min(summaryEarliestArrivalTimeMap.get(stopTimeTripletEntry.
                                        getKey()) + minutesToBeAddedForCurrentStop,
                                summaryEarliestArrivalTimeMap.get(destinationStopId) + minutesToBeAddedForDestinationStop)) {
                            // Update earliest arrival time map for all trip legs
                            tripLegWiseEarliestArrivalTimeMap.get(tripLegNumber).replace(stopTimeTripletEntry.getKey(),
                                    currentArrivalTime + minutesToBeAddedForCurrentStop);

                            // Update earliest arrival time summary map
                            summaryEarliestArrivalTimeMap.replace(stopTimeTripletEntry.getKey(), currentArrivalTime + minutesToBeAddedForCurrentStop);

                            // Update list of marked stops
                            markedStops.add(stopTimeTripletEntry.getKey());
                            previousArrivalTime = currentArrivalTime + minutesToBeAddedForCurrentStop;
                        }

                    }

                /* Check to see if an earlier trip can be found at the concerned stop (check does not apply to first
                trip leg, as it is certain that no earlier trips could be caught at any of the iterated over stops)
                */
                    if (tripLegNumber > 1) {
                        if (summaryEarliestArrivalTimeMap.get(stopTimeTripletEntry.getKey()) < currentArrivalTime) {

                            for (HashMap.Entry<Integer, LinkedHashMap<Integer, StopTimeTriplet>> revisedTripForTraversal :
                                    tripWiseStopTimeMaps.entrySet()) {
                                if (revisedTripForTraversal.getValue().get(stopTimeTripletEntry.getKey()).getArrivalTime() <
                                        currentArrivalTime) {
                                    tripIterator = revisedTripForTraversal.getValue().entrySet().iterator();
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Look at footpaths to update arrival times
    public static void lookAtFootpaths(int tripLegNumber,
                                       @NotNull ArrayList<Integer> markedStops,
                                       LinkedHashMap<Integer, Transfer> transfers,
                                       LinkedHashMap<Integer, Double> summaryEarliestArrivalTimeMap,
                                       LinkedHashMap<Integer, LinkedHashMap<Integer, Double>>
                                               tripLegWiseEarliestArrivalTimeMap) {

        for (int markedStopId : markedStops) {
            for (HashMap.Entry<Integer, Double> transferEntry : transfers.get(markedStopId).getTransferMap().
                    entrySet()) {

                double earliestArrivalTime = Math.min(tripLegWiseEarliestArrivalTimeMap.get(tripLegNumber).
                                get(transferEntry.getKey()), tripLegWiseEarliestArrivalTimeMap.get(tripLegNumber).
                                get(markedStopId) + transferEntry.getValue());

                tripLegWiseEarliestArrivalTimeMap.get(tripLegNumber).replace(transferEntry.getKey(),
                        earliestArrivalTime);
                summaryEarliestArrivalTimeMap.replace(transferEntry.getKey(), earliestArrivalTime);

                markedStops.add(transferEntry.getKey());
            }
        }
    }
}