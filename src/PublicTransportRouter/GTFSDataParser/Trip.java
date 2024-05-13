package src.PublicTransportRouter.GTFSDataParser;

import java.util.ArrayList;

public class Trip {
    private ArrayList<String> tripList; // Route-specific trip-list

    Trip(ArrayList<String> tripList) {
        this.tripList = tripList;
    }

    Trip() {
        this(new ArrayList<>());
    }

    void setTripList(ArrayList<String> tripList) {
        this.tripList = tripList;
    }

    ArrayList<String> getTripList() {
        return this.tripList;
    }
}