package src.PublicTransportRouter.GTFSDataManager;

import java.util.ArrayList;

public class Trip {
    private final ArrayList<Integer> tripList;    // Route-specific trip ID list; route IDs are in the relevant hashmap

    Trip() {
        this.tripList = new ArrayList<>();    // All values are initialized to zeroes
    }

    ArrayList<Integer> getTripList() {
        return this.tripList;
    }
}