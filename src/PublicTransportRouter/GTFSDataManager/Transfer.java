package src.PublicTransportRouter.GTFSDataManager;

import java.util.LinkedHashMap;

public class Transfer {
    private final LinkedHashMap<Integer, Double> transferMap;
    /* Keys refer to destination stops' IDs, and values refer to minimum transfer times; origin stop IDs are keys of
    the pertinent hashmap
    */

    Transfer(LinkedHashMap<Integer, Double> transferMap) {
        this.transferMap = transferMap;
    }

    Transfer() {
        this(new LinkedHashMap<>());    // All values are initialized to zeroes
    }

    public LinkedHashMap<Integer, Double> getTransferMap() {
        return this.transferMap;
    }
}