package src.PublicTransportRouter.GTFSDataParser;

import java.util.HashMap;
public class Transfer {
    private HashMap<Stop, Integer> departureStopTransferDurationMap;

    Transfer(HashMap<Stop, Integer> departureStopTransferDurationMap) {
        this.departureStopTransferDurationMap = departureStopTransferDurationMap;
    }

    void setDepartureStopTransferDurationMap(HashMap<Stop, Integer> departureStopTransferDurationMap) {
        this.departureStopTransferDurationMap = departureStopTransferDurationMap;
    }

    HashMap<Stop, Integer> getDepartureStopTransferDurationMap() {
        return this.departureStopTransferDurationMap;
    }
}
