package src.PublicTransportRouter.GTFSDataManager;

import java.util.HashMap;

public class Transfer {
    private HashMap<String, Integer> transferMap;
    /* Strings refer to destination stops' IDs, and integers refer to minimum transfer times; origin stop IDs are keys
    of the pertinent hashmap
    */

    Transfer(HashMap<String, Integer> transferMap) {
        this.transferMap = transferMap;
    }

    Transfer() {
        this(new HashMap<>());    // All values are initialized to nulls and zeroes
    }

    public HashMap<String, Integer> getTransferMap() {
        return this.transferMap;
    }
}