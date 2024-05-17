package src.PublicTransportRouter.RAPTOR;

import src.PublicTransportRouter.GTFSDataManager.*;

import java.util.HashMap;

public class RoutingAlgorithm {

    public static void main(String[] args) {
        GTFSDataReaderWriter gtfsDataReaderWriterForRaptor = new GTFSDataReaderWriter();
        String gtfsFolderPath = "";

        String gtfsRoutesFilePath = gtfsFolderPath + "/routes.txt";
        gtfsDataReaderWriterForRaptor.readAndFilterGTFSRoutes(gtfsRoutesFilePath);

        String gtfsTripsFilePath = gtfsFolderPath + "/trips.txt";
        gtfsDataReaderWriterForRaptor.readAndFilterGTFSTrips(gtfsTripsFilePath);

        String gtfsStopTimesFilePath = gtfsFolderPath + "/stop_times.txt";
        gtfsDataReaderWriterForRaptor.readAndFilterGTFSStopTimes(gtfsStopTimesFilePath);

        gtfsDataReaderWriterForRaptor.sortStopTimes();
        gtfsDataReaderWriterForRaptor.padGTFSRoutes();
        gtfsDataReaderWriterForRaptor.padGTFSRouteStops();

        String gtfsStopsFilePath = gtfsFolderPath + "/stops.txt";
        gtfsDataReaderWriterForRaptor.readAndFilterGTFSStops(gtfsStopsFilePath);

        gtfsDataReaderWriterForRaptor.padStopRoutes();
        gtfsDataReaderWriterForRaptor.buildTransfersHashMap();
        gtfsDataReaderWriterForRaptor.filterTransfersHashMap();
        gtfsDataReaderWriterForRaptor.filterHashMapsOnLatLong();
        gtfsDataReaderWriterForRaptor.makeTransfersTransitive();

        String raptorFolderPath = "";
        String raptorRoutesFilePath = raptorFolderPath + "/routes.txt";
        gtfsDataReaderWriterForRaptor.writeRaptorRoutes(raptorRoutesFilePath);

        String raptorRouteStopsFilePath = raptorFolderPath + "/routeStops.txt";
        gtfsDataReaderWriterForRaptor.writeRaptorRouteStops(raptorRouteStopsFilePath);

        String tripsFilePath = raptorFolderPath + "/trips.txt";
        gtfsDataReaderWriterForRaptor.writeTrips(tripsFilePath);

        String raptorStopTimesFilePath = raptorFolderPath + "/stop_times.txt";
        gtfsDataReaderWriterForRaptor.writeRaptorStopTimes(raptorStopTimesFilePath);

        String raptorStopsFilePath = raptorFolderPath + "/stops.txt";
        gtfsDataReaderWriterForRaptor.writeRaptorStops(raptorStopsFilePath);

        String raptorStopRoutesFilePath = raptorFolderPath + "/stopRoutes.txt";
        gtfsDataReaderWriterForRaptor.writeRaptorStopRoutes(raptorStopRoutesFilePath);

        String raptorTransfersFilePath = raptorFolderPath + "/transfers.txt";
        gtfsDataReaderWriterForRaptor.writeRaptorTransfers(raptorTransfersFilePath);

        HashMap<String, Route> routes = gtfsDataReaderWriterForRaptor.getRoutes();
        HashMap<String, RouteStop> routeStops = gtfsDataReaderWriterForRaptor.getRouteStops();
        HashMap<String, Trip> trips = gtfsDataReaderWriterForRaptor.getTrips();
        HashMap<String, StopTime> stopTimes = gtfsDataReaderWriterForRaptor.getStopTimes();
        HashMap<String, Stop> stops = gtfsDataReaderWriterForRaptor.getStops();
        HashMap<String, StopRoutes> stopRoutes = gtfsDataReaderWriterForRaptor.getStopRoutes();
        HashMap<String, Transfer> transfers = gtfsDataReaderWriterForRaptor.getTransfers();
    }

    private static void initializeAlgorithm() {

    }

}