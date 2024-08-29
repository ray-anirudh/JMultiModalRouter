/**
 * Author: Anirudh Ray
 * Institution: Professorship of Traffic Engineering and Control, Technical University of Munich
 * Department: Mobility Systems Engineering, School of Engineering and Design
 * E-mail Address: Anirudh.Ray@tum.de
 * Purpose: Component of a Java-based multi-modal routing algorithm, built using RAPTOR, Dijkstra-algorithm, and
 * KD-Trees
 */

package src.PublicTransportRouter.GTFSDataManager;

import java.util.ArrayList;

public class StopRoute {
    private final ArrayList<Integer> routeList;
    // Integer elements are route IDs serving the stop; stop IDs are in the relevant hashmap

    StopRoute() {
        this.routeList = new ArrayList<>();    // All values are initialized to zeroes
    }

    public ArrayList<Integer> getRouteList() {
        return this.routeList;
    }
}