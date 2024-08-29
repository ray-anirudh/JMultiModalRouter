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

public class RouteStop {
    private final LinkedHashMap<Integer, LinkedHashMap<Integer, Integer>> directionWiseStopMaps;
    // External keys refer to route directions, internal keys refer to stop IDs, and internal values to stop sequences

    RouteStop(LinkedHashMap<Integer, LinkedHashMap<Integer, Integer>> directionWiseStopMaps) {
        this.directionWiseStopMaps = directionWiseStopMaps;
    }

    RouteStop() {
        this(new LinkedHashMap<>());  // All values are initialized to zeroes
    }

    public LinkedHashMap<Integer, LinkedHashMap<Integer, Integer>> getDirectionWiseStopMaps() {
        return this.directionWiseStopMaps;
    }
}