package src.PublicTransportRouter.RoutingAlgorithm;

import org.jetbrains.annotations.NotNull;
import src.MultiModalRouter.TransitQuery;
import src.PublicTransportRouter.GTFSDataManager.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class RAPTOR {
    private static final int MINUTES_IN_DAY = 1440;

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
        LinkedHashMap<Integer, Double> summaryEarliestArrivalTimeMap = initializeSummaryEarliestArrivalTimeMap(stops);
        summaryEarliestArrivalTimeMap.replace(originStopId, (double) desiredDepartureTime);

        // Create an empty trip-leg wise earliest arrival time map
        LinkedHashMap<Integer, LinkedHashMap<Integer, Double>> tripLegWiseEarliestArrivalTimeMap =
                new LinkedHashMap<>();

        // Add the origin stop to the list of marked stops, only for the first round of route scans
        ArrayList<Integer> markedStops = new ArrayList<>();
        markedStops.add(originStopId);
        LinkedHashMap<Integer, Integer> routesServingMarkedStops = new LinkedHashMap<>();

        // Core RAPTOR loops
        while (!markedStops.isEmpty()) {
            // Initialize trip-leg specific earliest arrival time map inside the trip-leg wise earliest arrival time map
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

            tripLegNumber += 1;
        }

        // Return earliest arrival time output
        double earliestArrivalTimeAtDestination = summaryEarliestArrivalTimeMap.get(destinationStopId);
        if (earliestArrivalTimeAtDestination != Double.MAX_VALUE) {
            if (earliestArrivalTimeAtDestination < MINUTES_IN_DAY) {
                transitQueryResponse = new TransitQueryResponse((int) (Math.round(summaryEarliestArrivalTimeMap.
                        get(destinationStopId))), (int) (Math.round(summaryEarliestArrivalTimeMap.
                        get(destinationStopId))) - desiredDepartureTime);
            } else {
                transitQueryResponse = new TransitQueryResponse((int) (Math.round
                        (summaryEarliestArrivalTimeMap.get(destinationStopId))) % MINUTES_IN_DAY, (int) (Math.round
                        (summaryEarliestArrivalTimeMap.get(destinationStopId))) - desiredDepartureTime);
            }
        }

        return transitQueryResponse;
    }

    // Initialize trip-leg specific earliest arrival time map by setting timestamps at all stops
    private void initializeTripLegSpecificArrivalTimeMap(int tripLegNumber,
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

    // Initialize a summary map for the earliest arrival time at each stop
    @NotNull
    private LinkedHashMap<Integer, Double> initializeSummaryEarliestArrivalTimeMap(
            @NotNull LinkedHashMap<Integer, Stop> stops) {
        LinkedHashMap<Integer, Double> summaryEarliestArrivalTimeMap = new LinkedHashMap<>();
        // Keys refer to stop IDs, and values refer to the stop-wise earliest arrival times, irrespective of trip leg

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

        The last hashmap is modified in this method, which then contains routes to be traversed in RAPTOR, appended with
        directional identifiers 111 or 222 (based on the direction they serve)
        */

        for (int markedStopId : markedStops) {
            for (int markedStopBasedRouteId : stopRoutes.get(markedStopId).getRouteList()) {

                LinkedHashMap<Integer, LinkedHashMap<Integer, Integer>> routeStopConsidered = routeStops.
                        get(markedStopBasedRouteId).getDirectionWiseStopSequenceMap();

                String directionalRouteId = String.valueOf(markedStopBasedRouteId);
                if (routeStopConsidered.get(1).containsKey(markedStopId)) {
                    directionalRouteId = directionalRouteId + "111";     // Pay attention during stopTime traversals
                } else if (routeStopConsidered.get(2).containsKey(markedStopId)) {
                    directionalRouteId = directionalRouteId + "222";     // Pay attention during stopTime traversals
                }

                int directionId = Integer.parseInt(directionalRouteId.substring(directionalRouteId.
                        length() - 1));
                int directionalRouteIdInt = Integer.parseInt(directionalRouteId);

                LinkedHashMap<Integer, Integer> directionalStopSequence = routeStopConsidered.get(directionId);
                if (routesServingMarkedStops.containsKey(directionalRouteIdInt)) {
                    int preexistingStopId = routesServingMarkedStops.get(directionalRouteIdInt);

                    // Traversal must begin at the earliest-sequenced stop on a route (characterised by direction)
                    if (directionalStopSequence.get(markedStopId) < directionalStopSequence.get(preexistingStopId)) {
                        routesServingMarkedStops.replace(directionalRouteIdInt, markedStopId);
                    }
                } else {
                    routesServingMarkedStops.put(directionalRouteIdInt, markedStopId);
                }
            }
        }
        markedStops.clear();
    }

    // Traverse each route from the list of accumulated routes
    private void traverseEachRoute(int destinationStopId,
                                   int tripLegNumber,
                                   ArrayList<Integer> markedStops,
                                   @NotNull LinkedHashMap<Integer, Integer> routesServingMarkedStops,
                                   LinkedHashMap<Integer, StopTime> stopTimes,
                                   LinkedHashMap<Integer, Double> summaryEarliestArrivalTimeMap,
                                   LinkedHashMap<Integer, LinkedHashMap<Integer, Double>>
                                           tripLegWiseEarliestArrivalTimeMap) {

        for (HashMap.Entry<Integer, Integer> routeServingMarkedStop : routesServingMarkedStops.entrySet()) {

            // Get parameters of the route-stop pair
            String routeIdStr = String.valueOf(routeServingMarkedStop.getKey());
            int routeId = Integer.parseInt(routeIdStr.substring(0, routeIdStr.length() - 3));
            int stopId = routeServingMarkedStop.getValue();

            // To handle arrival times after a temporal wraparound
            int dayCounter = (int) (summaryEarliestArrivalTimeMap.get(stopId) / MINUTES_IN_DAY);


            // Determine the pertinent trip
            LinkedHashMap<Integer, LinkedHashMap<Integer, StopTimeTriplet>> tripWiseStopTimeMaps = stopTimes.
                    get(routeId).getTripWiseStopTimeMaps();
            int tripIdForTraversal = findEarliestTripIdForTraversal(stopId, tripWiseStopTimeMaps,
                    summaryEarliestArrivalTimeMap);

            LinkedHashMap<Integer, StopTimeTriplet> tripBeingTraversed = tripWiseStopTimeMaps.get(tripIdForTraversal);
            if (tripBeingTraversed == null) {
                continue;
            }
            Iterator<HashMap.Entry<Integer, StopTimeTriplet>> tripIterator = tripBeingTraversed.entrySet().iterator();

            // Traverse trip
            while (tripIterator.hasNext()) {
                HashMap.Entry<Integer, StopTimeTriplet> stopTimeTripletEntry = tripIterator.next();
                if (stopTimeTripletEntry.getKey().equals(stopId)) {

                    while (tripIterator.hasNext()) {
                        int previousArrivalTime = stopTimeTripletEntry.getValue().getArrivalTime();

                        stopTimeTripletEntry = tripIterator.next();
                        int currentStopId = stopTimeTripletEntry.getKey();
                        int currentArrivalTime = stopTimeTripletEntry.getValue().getArrivalTime();

                        if (currentArrivalTime < previousArrivalTime) {
                            dayCounter += 1;
                        }
                        int minutesForWraparound = dayCounter * MINUTES_IN_DAY;
                        currentArrivalTime += minutesForWraparound;

                        if (currentArrivalTime < Math.min(summaryEarliestArrivalTimeMap.get(currentStopId),
                                summaryEarliestArrivalTimeMap.get(destinationStopId))) {
                            // Update trip-wise (and thereby, trip-specific) earliest arrival time map
                            tripLegWiseEarliestArrivalTimeMap.get(tripLegNumber).replace(currentStopId,
                                    (double) currentArrivalTime);
                            // Update summary earliest arrival time map
                            summaryEarliestArrivalTimeMap.replace(currentStopId, (double) currentArrivalTime);
                            // Update list of marked stops
                            markedStops.add(currentStopId);
                        }

                        /* Check to see if an earlier trip can be found at the concerned stop (check does not apply to
                        first trip leg, as it is certain that no earlier trips could be caught at any of the iterated
                        over stops)
                        */
                        if (tripLegNumber > 1) {
                            if (summaryEarliestArrivalTimeMap.get(currentStopId) < currentArrivalTime) {

                                int revisedTripIdForTraversal = findEarliestTripIdForTraversal(currentStopId,
                                        tripWiseStopTimeMaps, summaryEarliestArrivalTimeMap);
                                tripIterator = tripWiseStopTimeMaps.get(revisedTripIdForTraversal).entrySet().
                                        iterator();
                                stopId = currentStopId;
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    // Determine the earliest possible trip that can be taken from a stop along a route
    private int findEarliestTripIdForTraversal(int stopId,
                                               @NotNull LinkedHashMap<Integer, LinkedHashMap<Integer, StopTimeTriplet>>
                                                       tripWiseStopTimeMaps,
                                               LinkedHashMap<Integer, Double> summaryEarliestArrivalTimeMap) {
        int tripIdForTraversal = -1;
        for (HashMap.Entry<Integer, LinkedHashMap<Integer, StopTimeTriplet>> tripSpecificStopTimeMap :
                tripWiseStopTimeMaps.entrySet()) {

            StopTimeTriplet stopTimeTriplet = tripSpecificStopTimeMap.getValue().get(stopId);
            if (stopTimeTriplet != null) {
                if ((stopTimeTriplet.getDepartureTime() % MINUTES_IN_DAY) >= (summaryEarliestArrivalTimeMap.get(stopId)
                        % MINUTES_IN_DAY)) {
                    tripIdForTraversal = tripSpecificStopTimeMap.getKey();
                    break;
                }
            }
        }

        // If no pertinent trip is found, then midnight time wraparound is the issue, which is addressed here
        if (tripIdForTraversal == -1) {
            for (HashMap.Entry<Integer, LinkedHashMap<Integer, StopTimeTriplet>> tripSpecificStopTimeMap :
                    tripWiseStopTimeMaps.entrySet()) {

                StopTimeTriplet stopTimeTriplet = tripSpecificStopTimeMap.getValue().get(stopId);
                if (stopTimeTriplet != null) {
                    if ((stopTimeTriplet.getDepartureTime() % MINUTES_IN_DAY + MINUTES_IN_DAY) >=
                            (summaryEarliestArrivalTimeMap.get(stopId) % MINUTES_IN_DAY)) {
                        tripIdForTraversal = tripSpecificStopTimeMap.getKey();
                        break;
                    }
                }
            }
        }
        return tripIdForTraversal;
    }

    // Look at footpaths to update arrival times
    private void lookAtFootpaths(int tripLegNumber,
                                 @NotNull ArrayList<Integer> markedStops,
                                 LinkedHashMap<Integer, Transfer> transfers,
                                 LinkedHashMap<Integer, Double> summaryEarliestArrivalTimeMap,
                                 LinkedHashMap<Integer, LinkedHashMap<Integer, Double>>
                                         tripLegWiseEarliestArrivalTimeMap) {

        for (int markedStopId : markedStops) {
            for (HashMap.Entry<Integer, Double> transferEntry : transfers.get(markedStopId).getTransferMap().
                    entrySet()) {

                double earliestArrivalTime = Math.min(summaryEarliestArrivalTimeMap.get(transferEntry.getKey()),
                        summaryEarliestArrivalTimeMap.get(markedStopId) + transferEntry.getValue());
                tripLegWiseEarliestArrivalTimeMap.get(tripLegNumber).replace(transferEntry.getKey(),
                        earliestArrivalTime);
                summaryEarliestArrivalTimeMap.replace(transferEntry.getKey(), earliestArrivalTime);

                markedStops.add(transferEntry.getKey());
            }
        }
    }
}