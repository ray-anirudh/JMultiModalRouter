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

public class Trip {
    private final ArrayList<Integer> tripList;    // Route-specific trip ID list; route IDs are in the relevant hashmap

    Trip() {
        this.tripList = new ArrayList<>();    // All values are initialized to zeroes
    }

    ArrayList<Integer> getTripList() {
        return this.tripList;
    }
}