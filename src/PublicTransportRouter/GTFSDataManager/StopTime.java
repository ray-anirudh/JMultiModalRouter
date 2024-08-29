/**
 * Author: Anirudh Ray
 * Institution: Professorship of Traffic Engineering and Control, Technical University of Munich
 * Department: Mobility Systems Engineering, School of Engineering and Design
 * E-mail Address: Anirudh.Ray@tum.de
 * Purpose: Component of a Java-based multi-modal routing algorithm, built using RAPTOR, Dijkstra-algorithm, and
 * KD-Trees
 */

package src.PublicTransportRouter.GTFSDataManager;

import java.util.LinkedHashMap;

public class StopTime {
    private final LinkedHashMap<Integer, LinkedHashMap<Integer, StopTimeTriplet>> tripWiseStopTimeMaps;
    /* Integer keys refer to trip IDs within a route; overarching route IDs are in the pertinent hashmap
    Internal hashmap has actual stop IDs mapped to arrival times, departure times and stop sequences
    */

    StopTime(LinkedHashMap<Integer, LinkedHashMap<Integer, StopTimeTriplet>> tripWiseStopTimeMaps) {
        this.tripWiseStopTimeMaps = tripWiseStopTimeMaps;
    }

    StopTime() {
        this(new LinkedHashMap<>());    // All values are initialized to zeroes
    }

    public LinkedHashMap<Integer, LinkedHashMap<Integer, StopTimeTriplet>> getTripWiseStopTimeMaps() {
        return this.tripWiseStopTimeMaps;
    }
}