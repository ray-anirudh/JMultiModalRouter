package src.PublicTransportRouter.GTFSDataManager;

import java.util.HashMap;

public class Transfer {
    private HashMap<String, Double> transferMap;
    /* Strings refer to destination stops' IDs, and doubles refer to minimum transfer times; origin stop IDs are keys
    of the pertinent hashmap
    */

    Transfer(HashMap<String, Double> transferMap) {
        this.transferMap = transferMap;
    }

    Transfer() {
        this(new HashMap<>());    // All values are initialized to nulls and zeroes
    }

    public HashMap<String, Double> getTransferMap() {
        return this.transferMap;
    }
}