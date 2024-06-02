package src.MultiModalRouter;
// TODO: HEURISTICS CAN BE BASED ON STOPS (STOPTYPE (ASCRIBED VIA ROUTETYPE - BIGGEST CURRENT CANDIDATE), PARENTSTOPPRESENCE), ROUTES (ROUTETYPES, #TRIPS, #STOPS)
// TODO: HEURISTIC DATA CAN BE ASCRIBED IN GTFSDATAMANAGER
// TODO: QUERY PARSING TO PEERFECT INPUT TYPES MUST HAPPEN IN THE CALLER, AND NOT HERE, OR IN THE QUERY CODE (SUCH AS LOCATING STOP AND THEN HOMING IN ON THE STOPIDS)

public class Caller {


//// Display results
//        for (HashMap.Entry<TransitQuery, Double> queryTravelTimeEntry : shortestTravelTimes.entrySet()) {
//        System.out.println("Origin stop: " + queryTravelTimeEntry.getKey().getOriginStopId() + "\n" +
//        "Destination stop: " + queryTravelTimeEntry.getKey().getDestinationStopId() + "\n" +
//        "Departure time: " + queryTravelTimeEntry.getKey().getDepartureTime() + "\n" +
//        "Travel time: " + queryTravelTimeEntry.getValue() + "\n");
//        }


//    public static void main(String[] args) {
//        // GTFS data reader-writer instantiation to read, write, and store data
//        GTFSDataReaderWriter gtfsDataReaderWriterForRaptor = new GTFSDataReaderWriter();
//        String gtfsFolderPath = "";
//        String raptorFolderPath = "";
//        getRaptorMaps(gtfsFolderPath, raptorFolderPath, gtfsDataReaderWriterForRaptor);
//
//        // Get all data for RAPTOR execution
//        HashMap<Integer, Route> routes = gtfsDataReaderWriterForRaptor.getRoutes();
//        HashMap<Integer, RouteStop> routeStops = gtfsDataReaderWriterForRaptor.getRouteStops();
//        HashMap<Integer, Trip> trips = gtfsDataReaderWriterForRaptor.getTrips();
//        HashMap<Integer, StopTime> stopTimes = gtfsDataReaderWriterForRaptor.getStopTimes();
//        HashMap<Integer, Stop> stops = gtfsDataReaderWriterForRaptor.getStops();
//        HashMap<Integer, StopRoute> stopRoutes = gtfsDataReaderWriterForRaptor.getStopRoutes();
//        HashMap<Integer, Transfer> transfers = gtfsDataReaderWriterForRaptor.getTransfers();


//    // Get RAPTOR-relevant arrays and hashmaps ready
//    public static void getRaptorMaps(String gtfsFolderPath,
//                                     String raptorFolderPath,
//                                     GTFSDataReaderWriter gtfsDataReaderWriterForRaptor) {
//        // Ready filepath arguments to read
//        String gtfsRoutesFilePath = gtfsFolderPath + "/routes.txt";
//        String gtfsTripsFilePath = gtfsFolderPath + "/trips.txt";
//        String gtfsStopTimesFilePath = gtfsFolderPath + "/stop_times.txt";
//        String gtfsStopsFilePath = gtfsFolderPath + "/stops.txt";
//
//        // Ready filepath arguments to write
//        String raptorRoutesFilePath = raptorFolderPath + "/routes.txt";
//        String raptorRouteStopsFilePath = raptorFolderPath + "/routeStops.txt";
//        String tripsFilePath = raptorFolderPath + "/trips.txt";
//        String raptorStopTimesFilePath = raptorFolderPath + "/stop_times.txt";
//        String raptorStopsFilePath = raptorFolderPath + "/stops.txt";
//        String raptorStopRoutesFilePath = raptorFolderPath + "/stopRoutes.txt";
//        String raptorTransfersFilePath = raptorFolderPath + "/transfers.txt";
//
//        // Read and manage data for main RAPTOR loop
//        gtfsDataReaderWriterForRaptor.readAndFilterGTFSRoutes(gtfsRoutesFilePath);
//        gtfsDataReaderWriterForRaptor.readAndFilterGTFSTrips(gtfsTripsFilePath);
//        gtfsDataReaderWriterForRaptor.readAndFilterGTFSStopTimes(gtfsStopTimesFilePath);
//        gtfsDataReaderWriterForRaptor.sortStopTimes();
//        gtfsDataReaderWriterForRaptor.padGTFSRoutes();
//        gtfsDataReaderWriterForRaptor.padGTFSRouteStops();
//
//        // Read and manage data for ancillary RAPTOR loop
//        gtfsDataReaderWriterForRaptor.readAndFilterGTFSStops(gtfsStopsFilePath);
//        gtfsDataReaderWriterForRaptor.padStopRoutes();
//        gtfsDataReaderWriterForRaptor.buildTransfersHashMap();
//        gtfsDataReaderWriterForRaptor.filterTransfersHashMap();
//
//        // Limit dataset to study area and ensure transitivity of transfers
//        gtfsDataReaderWriterForRaptor.filterHashMapsOnLatLong();
//        gtfsDataReaderWriterForRaptor.makeTransfersTransitive();
//
//        // Write out data used for RAPTOR
//        gtfsDataReaderWriterForRaptor.writeRaptorRoutes(raptorRoutesFilePath);
//        gtfsDataReaderWriterForRaptor.writeRaptorRouteStops(raptorRouteStopsFilePath);
//        gtfsDataReaderWriterForRaptor.writeTrips(tripsFilePath);
//        gtfsDataReaderWriterForRaptor.writeRaptorStopTimes(raptorStopTimesFilePath);
//        gtfsDataReaderWriterForRaptor.writeRaptorStops(raptorStopsFilePath);
//        gtfsDataReaderWriterForRaptor.writeRaptorStopRoutes(raptorStopRoutesFilePath);
//        gtfsDataReaderWriterForRaptor.writeRaptorTransfers(raptorTransfersFilePath);
//    }

/*
    int desiredDepartureHour = desiredDepartureTime / 60;
    int desiredDepartureMinute = desiredDepartureTime % 60;

    double earliestArrivalTimeAtDestination = summaryEarliestArrivalTimeMap.get(destinationStopId);
    double earliestArrivalHourAtDestination = earliestArrivalTimeAtDestination / 60;
    double earliestArrivalMinuteAtDestination = earliestArrivalTimeAtDestination % 60;
    double travelTime = summaryEarliestArrivalTimeMap.get(destinationStopId) - desiredDepartureTime;
    double travelTimeHour = travelTime / 60;
    double travelTimeMinute = travelTime % 60;

    output = "Origin stop: " + stops.get(originStopId).getStopName() + "\n" +
            "Destination stop: " + stops.get(destinationStopId).getStopName() + "\n" +
            "Departure time from origin stop: " + desiredDepartureHour + ":" + desiredDepartureMinute + "\n" +
            "Earliest possible arrival time at destination: " + (int) earliestArrivalHourAtDestination + ":" +
            (int) earliestArrivalMinuteAtDestination + "\n" +
            "Travel time: " + travelTimeHour + ":" + travelTimeMinute + "\n";

            */

    //           shortestTravelTimes.put(query, summaryEarliestArrivalTimeMap.get(destinationStopId));

}