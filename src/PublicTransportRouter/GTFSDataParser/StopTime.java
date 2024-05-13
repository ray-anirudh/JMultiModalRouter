package src.PublicTransportRouter.GTFSDataParser;

import java.util.ArrayList;
import java.util.HashMap;

public class StopTime {
    private HashMap<String, ArrayList<StopTimeQuartet>> tripWiseStopTimeLists;    // String refers to a trip ID

    StopTime(HashMap<String, ArrayList<StopTimeQuartet>> tripWiseStopTimeLists) {
        this.tripWiseStopTimeLists = tripWiseStopTimeLists;
    }

    StopTime() {
        this(new HashMap<>());    // All values are initialized to nulls and zeroes
    }

    void setTripWiseStopTimeList(HashMap<String, ArrayList<StopTimeQuartet>> tripWiseStopTimeLists) {
        this.tripWiseStopTimeLists = tripWiseStopTimeLists;
    }

    HashMap<String, ArrayList<StopTimeQuartet>> getTripWiseStopTimeLists() {
        return this.tripWiseStopTimeLists;
    }
}