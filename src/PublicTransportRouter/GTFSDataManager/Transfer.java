package src.PublicTransportRouter.GTFSDataManager;

import java.util.HashMap;

public class Transfer {
    private final HashMap<Integer, Double> transferMap;
    /* Keys refer to destination stops' IDs, and values refer to minimum transfer times; origin stop IDs are keys of
    the pertinent hashmap
    */

    Transfer(HashMap<Integer, Double> transferMap) {
        this.transferMap = transferMap;
    }

    Transfer() {
        this(new HashMap<>());    // All values are initialized to zeroes
    }

    public HashMap<Integer, Double> getTransferMap() {
        return this.transferMap;
    }
}