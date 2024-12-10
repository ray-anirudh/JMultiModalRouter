/**
 * Author: Anirudh Ray
 * Institution: Professorship of Traffic Engineering and Control, Technical University of Munich
 * Department: Mobility Systems Engineering, School of Engineering and Design
 * E-mail Address: Anirudh.Ray@tum.de
 * Purpose: Component of a Java-based multi-modal routing algorithm, built using RAPTOR, Dijkstra-algorithm, and
 * KD-Trees
 */

package src.PublicTransportRouter.RoutingAlgorithm;
// GTFS: General Transit Feed Specification
// RAPTOR: Round-based Public Transit Router (Delling et. al., 2015)

import java.util.*;

import src.PublicTransportRouter.GTFSDataManager.*;

public class RAPTOR {
    private static final int MINUTES_IN_DAY = 1_440;

    // Determine the shortest travel time for a single transit-based query
    public TransitQueryResponse findShortestTransitPath(int originStopId,
                                                        int destinationStopId,
                                                        double departureTimeOriginStop,
                                                        LinkedHashMap<Integer, RouteStop> routeStops,
                                                        LinkedHashMap<Integer, StopTime> stopTimes,
                                                        LinkedHashMap<Integer, Stop> stops,
                                                        LinkedHashMap<Integer, StopRoute> stopRoutes,
                                                        LinkedHashMap<Integer, Transfer> transfers) {

        // Initialize earliest arrival time output
        TransitQueryResponse transitQueryResponse = new TransitQueryResponse(-1,
                -1, -1);

        // Initialize RAPTOR
        LinkedHashMap<Integer, Double> summaryEarliestArrivalTimeMap = initializeSummaryEarliestArrivalTimeMap(stops);
        summaryEarliestArrivalTimeMap.put(originStopId, departureTimeOriginStop);

        // Initialize a trip leg-wise earliest arrival time map
        LinkedHashMap<Integer, LinkedHashMap<Integer, Double>> tripLegWiseEarliestArrivalTimeMap =
                new LinkedHashMap<>();
        int tripLegNumber = 0;
        tripLegWiseEarliestArrivalTimeMap.put(tripLegNumber, summaryEarliestArrivalTimeMap);  // For the zeroth trip leg

        // Add the origin stop to the list of marked stops, only for the first round of route scans
        ArrayList<Integer> markedStops = new ArrayList<>();
        markedStops.add(originStopId);
        LinkedHashMap<Integer, Integer> routesServingMarkedStops = new LinkedHashMap<>();


        // Core RAPTOR loops
        while (!markedStops.isEmpty()) {
            tripLegNumber += 1;
            /* Initialize a trip leg-specific earliest arrival time map inside the trip leg-wise earliest arrival time
            map; external integer keys refer to trip leg numbers, internal integer keys to stop IDs, and internal
            decimal values to the known earliest arrival times at the corresponding stops
            */
            tripLegWiseEarliestArrivalTimeMap.put(tripLegNumber, tripLegWiseEarliestArrivalTimeMap.
                    get(tripLegNumber - 1));

            routesServingMarkedStops.clear();
            // Accumulate routes serving the marked stops (empties the list of marked stops)
            accumulateRoutesFromStops(markedStops, stopRoutes, routeStops, routesServingMarkedStops);

            // Traverse each route
            traverseEachRoute(destinationStopId, tripLegNumber, markedStops, routesServingMarkedStops,
                    stopTimes, summaryEarliestArrivalTimeMap, tripLegWiseEarliestArrivalTimeMap);

            // Look at footpaths
            lookAtFootpaths(tripLegNumber, markedStops, transfers, summaryEarliestArrivalTimeMap,
                    tripLegWiseEarliestArrivalTimeMap);

            // Handle fall-through(s) as with stop ID "570936"
            handleFirstLegExits(tripLegNumber, originStopId, markedStops, transfers, summaryEarliestArrivalTimeMap,
                    tripLegWiseEarliestArrivalTimeMap);

            /* Debugging statements:
            System.out.println("Trip leg number: " + tripLegNumber);
            for (HashMap.Entry<Integer, Double> stopArrivalTimeEntry : tripLegWiseEarliestArrivalTimeMap.
                    get(tripLegNumber).entrySet()) {
                if (stopArrivalTimeEntry.getValue() != Double.MAX_VALUE) {
                    System.out.println("Stop name: " + stops.get(stopArrivalTimeEntry.getKey()).getStopName() +
                            ", Arrival time: " + stopArrivalTimeEntry.getValue());
                }
            }
            */
        }

        // Return query response
        double destinationStopEarliestArrivalTimeMinutes = summaryEarliestArrivalTimeMap.get(destinationStopId);
        if ((destinationStopEarliestArrivalTimeMinutes != Double.MAX_VALUE) &&
                (destinationStopEarliestArrivalTimeMinutes != Double.POSITIVE_INFINITY)) {
            double transitTravelTimeMinutes = destinationStopEarliestArrivalTimeMinutes - departureTimeOriginStop;
            transitQueryResponse = new TransitQueryResponse(destinationStopEarliestArrivalTimeMinutes,
                    transitTravelTimeMinutes, tripLegNumber - 1);
        }

        /* Debugging statements:
            System.out.println("Origin stop name: " + stops.get(originStopId).getStopName());
            System.out.println("Origin stop latitude: " + stops.get(originStopId).getStopLatitude());
            System.out.println("Origin stop longitude: " + stops.get(originStopId).getStopLongitude());
            System.out.println("Destination stop name: " + stops.get(destinationStopId).getStopName());
            System.out.println("Destination stop latitude: " + stops.get(destinationStopId).getStopLatitude());
            System.out.println("Destination stop longitude: " + stops.get(destinationStopId).getStopLongitude());
            System.out.println("Departure time from origin stop: " + departureTimeOriginStop);
            System.out.println("SEAT from origin stop: " + summaryEarliestArrivalTimeMap.get(originStopId));
            System.out.println("SEAT from destination stop: " + summaryEarliestArrivalTimeMap.get(destinationStopId));
            System.out.println(tripLegNumber);
            System.exit(7);
        */
        return transitQueryResponse;
    }

    // Initialize a summary map for the earliest arrival time at each stop
    private LinkedHashMap<Integer, Double> initializeSummaryEarliestArrivalTimeMap(LinkedHashMap<Integer, Stop> stops) {
        LinkedHashMap<Integer, Double> summaryEarliestArrivalTimeMap = new LinkedHashMap<>();
        // Keys refer to stop IDs, and values refer to the stop-wise earliest arrival times, irrespective of trip leg

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
        hashmap's keys are all stop IDs in the study area, mapped to lists of routes (one for every stop); the
        second hashmap is for finding the earliest possible stop to hop-on when changing from one route to another

        The last hashmap is modified in this method, which then contains routes to be traversed in RAPTOR, appended with
        directional identifiers 111 or 222 (based on the direction they serve)
        */

        for (int markedStopId : markedStops) {
            for (int markedStopBasedRouteId : stopRoutes.get(markedStopId).getRouteList()) {

                LinkedHashMap<Integer, LinkedHashMap<Integer, Integer>> routeStopConsidered = routeStops.
                        get(markedStopBasedRouteId).getDirectionWiseStopMaps();

                String directionalRouteId = String.valueOf(markedStopBasedRouteId);
                if (routeStopConsidered.get(1).containsKey(markedStopId)) {
                    directionalRouteId = directionalRouteId + "111";     // Pay attention during stopTime traversals
                } else if (routeStopConsidered.get(2).containsKey(markedStopId)) {
                    directionalRouteId = directionalRouteId + "222";     // Pay attention during stopTime traversals
                } else {
                    continue;
                }

                int directionId = Integer.parseInt(directionalRouteId.substring(directionalRouteId.
                        length() - 1));
                int directionalRouteIdInt = Integer.parseInt(directionalRouteId);

                LinkedHashMap<Integer, Integer> directionalStopSequence = routeStopConsidered.get(directionId);
                if (routesServingMarkedStops.containsKey(directionalRouteIdInt)) {
                    int preexistingStopId = routesServingMarkedStops.get(directionalRouteIdInt);

                    // Traversal must begin at the earliest-sequenced stop on a route (characterised by direction)
                    if (directionalStopSequence.get(markedStopId) < directionalStopSequence.get(preexistingStopId)) {
                        routesServingMarkedStops.put(directionalRouteIdInt, markedStopId);
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
                                   LinkedHashMap<Integer, Integer> routesServingMarkedStops,
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
            int[] dayCounter = {(int) (summaryEarliestArrivalTimeMap.get(stopId) / MINUTES_IN_DAY)};
            // System.out.println("Day counter initial value: " + Arrays.toString(dayCounter)); // Debugging statement

            // Determine the pertinent trip
            LinkedHashMap<Integer, LinkedHashMap<Integer, StopTimeTriplet>> tripWiseStopTimeMaps = stopTimes.
                    get(routeId).getTripWiseStopTimeMaps();
            int tripIdForRedundancyCheck = -1;
            int tripIdForTraversal = findEarliestTripIdForTraversal(stopId, dayCounter, tripIdForRedundancyCheck,
                    tripWiseStopTimeMaps, summaryEarliestArrivalTimeMap);

            LinkedHashMap<Integer, StopTimeTriplet> tripBeingTraversed = tripWiseStopTimeMaps.get(tripIdForTraversal);
            if ((tripBeingTraversed == null) || (tripBeingTraversed.isEmpty())) {
                continue;
            }
            ListIterator<HashMap.Entry<Integer, StopTimeTriplet>> tripIterator = new ArrayList<>(tripBeingTraversed.
                    entrySet()).listIterator();

            // Traverse trip
            while (tripIterator.hasNext()) {
                HashMap.Entry<Integer, StopTimeTriplet> stopTimeTripletEntry = tripIterator.next();
                /* Debugging statements:
                System.out.println("Stop-time triplet - stop ID: " + stopTimeTripletEntry.getKey() +
                ", Stop-time triplet - arrival time at stop: " + stopTimeTripletEntry.getValue().getArrivalTime());
                */
                if (stopTimeTripletEntry.getKey() == stopId) {

                    double previousArrivalTime = -1;
                    boolean visitableNextStop;
                    do {
                        int currentStopId = stopTimeTripletEntry.getKey();
                        double currentArrivalTime = stopTimeTripletEntry.getValue().getArrivalTime() + (dayCounter[0] *
                                MINUTES_IN_DAY);    // Last expression is to address temporal wraparound
                        /* Debugging statements:
                        System.out.println("Day counter: " + Arrays.toString(dayCounter));
                        System.out.println(
                                "Route ID: " + routeId + ", Trip ID: " + tripIdForTraversal +
                                ", Current stop ID: " + currentStopId + ", Current arrival time: " + currentArrivalTime
                                + ", Previous arrival time: " + previousArrivalTime);
                        */

                        if (currentArrivalTime < previousArrivalTime) {
                            dayCounter[0]++;
                            currentArrivalTime += MINUTES_IN_DAY;    // To address temporal wraparound
                            /* Debugging statements:
                            System.out.println("Current arrival time after day change: " + currentArrivalTime);
                            */
                        }

                        if (currentArrivalTime < Math.min(summaryEarliestArrivalTimeMap.get(currentStopId),
                                summaryEarliestArrivalTimeMap.get(destinationStopId))) {
                            // Update trip-wise (and thereby, trip-specific) earliest arrival time map
                            tripLegWiseEarliestArrivalTimeMap.get(tripLegNumber).put(currentStopId,
                                    currentArrivalTime);
                            // Update summary earliest arrival time map
                            summaryEarliestArrivalTimeMap.put(currentStopId, currentArrivalTime);
                            // Update list of marked stops
                            markedStops.add(currentStopId);
                        }

                        visitableNextStop = tripIterator.hasNext();
                        if (visitableNextStop) {
                            stopTimeTripletEntry = tripIterator.next();
                            previousArrivalTime = currentArrivalTime;
                        }

                        // Check to see if an earlier trip can be found at the concerned stop
                        if (summaryEarliestArrivalTimeMap.get(currentStopId) < currentArrivalTime) {
                            int revisedTripIdForTraversal = findEarliestTripIdForTraversal(currentStopId, dayCounter,
                                    tripIdForTraversal, tripWiseStopTimeMaps, summaryEarliestArrivalTimeMap);
                            LinkedHashMap<Integer, StopTimeTriplet> revisedTripForTraversal = tripWiseStopTimeMaps.
                                    get(revisedTripIdForTraversal);
                            if ((revisedTripIdForTraversal != tripIdForTraversal) && (revisedTripForTraversal != null)
                                    && (revisedTripForTraversal.isEmpty())) {
                                tripIdForTraversal = revisedTripIdForTraversal;
                                tripIterator = new ArrayList<>(revisedTripForTraversal.entrySet()).listIterator();
                                stopId = currentStopId;
                                break;
                            }
                        }
                    } while (visitableNextStop);
                }
            }
        }
        // System.exit(0);  // Debugging statement to assess performance over a single route-stop pair
    }

    // Determine the earliest possible trip that can be taken from a stop along a route
    private int findEarliestTripIdForTraversal(int stopId, int[] dayCounter, int tripIdForRedundancyCheck,
                                               LinkedHashMap<Integer, LinkedHashMap<Integer, StopTimeTriplet>>
                                                       tripWiseStopTimeMaps,
                                               LinkedHashMap<Integer, Double> summaryEarliestArrivalTimeMap) {
        int tripIdForTraversal = -1;
        for (HashMap.Entry<Integer, LinkedHashMap<Integer, StopTimeTriplet>> tripSpecificStopTimeMap :
                tripWiseStopTimeMaps.entrySet()) {
            StopTimeTriplet stopTimeTriplet = tripSpecificStopTimeMap.getValue().get(stopId);
            if (stopTimeTriplet != null) {
                // Deep analysis was needed to switch out departure times with arrival times
                if ((stopTimeTriplet.getArrivalTime() % MINUTES_IN_DAY) >= (summaryEarliestArrivalTimeMap.get(stopId)
                        % MINUTES_IN_DAY)) {
                    tripIdForTraversal = tripSpecificStopTimeMap.getKey();
                    // System.out.println("Trip to hop-on to has been found");   // Debugging statement
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
                    // Deep analysis was needed to switch out departure times with arrival times
                    if (((stopTimeTriplet.getArrivalTime() % MINUTES_IN_DAY) + MINUTES_IN_DAY) >=
                            (summaryEarliestArrivalTimeMap.get(stopId) % MINUTES_IN_DAY)) {
                        tripIdForTraversal = tripSpecificStopTimeMap.getKey();
                        if (tripIdForTraversal != tripIdForRedundancyCheck) {
                            dayCounter[0]++;
                            // System.out.println("Day counter updated with wraparound");   // Debugging statement
                        }
                        break;
                    }
                }
            }
        }
        return tripIdForTraversal;
    }

    // Look at footpaths to update arrival times
    private static void lookAtFootpaths(int tripLegNumber,
                                 ArrayList<Integer> markedStops,
                                 LinkedHashMap<Integer, Transfer> transfers,
                                 LinkedHashMap<Integer, Double> summaryEarliestArrivalTimeMap,
                                 LinkedHashMap<Integer, LinkedHashMap<Integer, Double>>
                                         tripLegWiseEarliestArrivalTimeMap) {

        ArrayList<Integer> newMarkedStops = new ArrayList<>();
        for (int markedStopId : markedStops) {
            for (HashMap.Entry<Integer, Double> transferEntry : transfers.get(markedStopId).getTransferMap().
                    entrySet()) {
                if ((summaryEarliestArrivalTimeMap.get(transferEntry.getKey()) != null) &&
                        (summaryEarliestArrivalTimeMap.get(markedStopId) != null)) {

                    double earliestArrivalTime = Math.min(summaryEarliestArrivalTimeMap.get(transferEntry.getKey()),
                            summaryEarliestArrivalTimeMap.get(markedStopId) + transferEntry.getValue());
                    tripLegWiseEarliestArrivalTimeMap.get(tripLegNumber).put(transferEntry.getKey(),
                            earliestArrivalTime);
                    summaryEarliestArrivalTimeMap.put(transferEntry.getKey(), earliestArrivalTime);

                    newMarkedStops.add(transferEntry.getKey());
                }

                /* Debugging statements:
                System.out.println("Marked stop ID: " + markedStopId + "\n" +
                        "To stop ID: " + transferEntry.getKey() + "\n" +
                        "Marked stop SEAT check: " + summaryEarliestArrivalTimeMap.containsKey(markedStopId) + "\n" +
                        "To stop SEAT check: " + summaryEarliestArrivalTimeMap.containsKey(transferEntry.getKey()) +
                        "\n" +
                        "Earliest arrival time in summary map: " + summaryEarliestArrivalTimeMap.
                        get(transferEntry.getKey()) + "\n" +
                        "Earliest arrival time via transfer: " + summaryEarliestArrivalTimeMap.get(transferEntry.
                        getKey()));
                */
            }
        }
        markedStops.addAll(newMarkedStops);
    }

    // Handle potential exits from the first leg of traversals (not the zeroth leg, as it concerns only the origin stop)
    private static void handleFirstLegExits(int tripLegNumber,
                                            int originStopId,
                                            ArrayList<Integer> markedStops,
                                            LinkedHashMap<Integer, Transfer> transfers,
                                            LinkedHashMap<Integer, Double> summaryEarliestArrivalTimeMap,
                                            LinkedHashMap<Integer, LinkedHashMap<Integer, Double>>
                                                    tripLegWiseEarliestArrivalTimeMap) {
        int nonUpdatedStopsCount = 0;
        if ((tripLegNumber == 1) && (markedStops.isEmpty())) {
            for (double earliestArrivalTimeValue : summaryEarliestArrivalTimeMap.values()) {
                if (earliestArrivalTimeValue == Double.MAX_VALUE) {
                    nonUpdatedStopsCount++;
                }
            }
        }

        if (nonUpdatedStopsCount == (summaryEarliestArrivalTimeMap.size() - 1)) {
            markedStops.add(originStopId);
            lookAtFootpaths(tripLegNumber, markedStops, transfers, summaryEarliestArrivalTimeMap,
                    tripLegWiseEarliestArrivalTimeMap);
            markedStops.remove(0);
        }
    }
}