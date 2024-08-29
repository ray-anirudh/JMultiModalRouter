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

public class Transfer {
    private final LinkedHashMap<Integer, Double> transferMap;
    /* Keys refer to destination stops' IDs, and values refer to minimum transfer times; origin stop IDs are keys of
    the pertinent hashmap
    */

    Transfer() {
        this.transferMap = new LinkedHashMap<>();    // All values are initialized to zeroes
    }

    public LinkedHashMap<Integer, Double> getTransferMap() {
        return this.transferMap;
    }
}