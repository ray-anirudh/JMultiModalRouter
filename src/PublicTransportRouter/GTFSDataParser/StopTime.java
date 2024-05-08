package src.PublicTransportRouter.GTFSDataParser;

import java.util.ArrayList;
import java.util.HashMap;

public class StopTime {
    private HashMap<String, ArrayList<StopTimeQuartet>> tripWiseStopTimeList;    // String refers to a trip ID

    StopTime(HashMap<String, ArrayList<StopTimeQuartet>> tripWiseStopTimeList) {
        this.tripWiseStopTimeList = tripWiseStopTimeList;
    }

    StopTime() {
        this(new HashMap<>());    // All values are initialized to nulls and zeroes
    }

    void setTripWiseStopTimeList(HashMap<String, ArrayList<StopTimeQuartet>> tripWiseStopTimeList) {
        this.tripWiseStopTimeList = tripWiseStopTimeList;
    }

    HashMap<String, ArrayList<StopTimeQuartet>> getTripWiseStopTimeList() {
        return this.tripWiseStopTimeList;
    }
}