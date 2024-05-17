package src.PublicTransportRouter.GTFSDataManager;

import java.util.ArrayList;

public class Trip {
    private ArrayList<String> tripList; // Route-specific trip-list; route IDs are in the relevant hashmap

    Trip(ArrayList<String> tripList) {
        this.tripList = tripList;
    }

    Trip() {
        this(new ArrayList<>());    // All values are initialized to nulls and zeroes
    }

    void setTripList(ArrayList<String> tripList) {
        this.tripList = tripList;
    }

    ArrayList<String> getTripList() {
        return this.tripList;
    }
}