package src.PublicTransportRouter.GTFSDataParser;

import java.util.HashMap;

public class Transfer {
    private HashMap<Stop, Integer> transferMap;

    Transfer(HashMap<Stop, Integer> transferMap) {
        this.transferMap = transferMap;
    }

    Transfer() {
        this(new HashMap<>());    // All values are initialized to nulls and zeroes
    }

    void setTransferMap(HashMap<Stop, Integer> transferMap) {
        this.transferMap = transferMap;
    }

    HashMap<Stop, Integer> getTransferMap() {
        return this.transferMap;
    }
}