package src.PublicTransportRouter.GTFSDataParser;

public class GTFSDataParserRunner {

    public static void main(String[] args) {
        // Create a GTFS data reader and writer instance
        GTFSDataReaderWriter gtfsDataReaderWriterForRaptor = new GTFSDataReaderWriter();

        // Read routes data
        String gtfsRoutesFilePath = "";
        gtfsDataReaderWriterForRaptor.readAndFilterGTFSRoutes(gtfsRoutesFilePath);

        // Read trips data
        String gtfsTripsFilePath = "";
        gtfsDataReaderWriterForRaptor.readAndFilterGTFSTrips(gtfsTripsFilePath);

        // Read stop times data and manage hashmaps
        String gtfsStopTimesFilePath = "";
        gtfsDataReaderWriterForRaptor.readAndFilterGTFSStopTimes(gtfsStopTimesFilePath);

        gtfsDataReaderWriterForRaptor.sortStopTimes();
        gtfsDataReaderWriterForRaptor.padGTFSRoutes();
        gtfsDataReaderWriterForRaptor.padGTFSRouteStops();

        // Read stops data and manage hashmaps
        String gtfsStopsFilePath = "";
        gtfsDataReaderWriterForRaptor.readAndFilterGTFSStops(gtfsStopsFilePath);

        gtfsDataReaderWriterForRaptor.padStopRoutes();
        gtfsDataReaderWriterForRaptor.buildTransfersHashMap();
        gtfsDataReaderWriterForRaptor.filterTransfersHashMap();
        gtfsDataReaderWriterForRaptor.filterHashMapsOnLatLong();
        gtfsDataReaderWriterForRaptor.makeTransfersTransitive();

        // Write routes data for RAPTOR
        String raptorRoutesFilePath = "";
        gtfsDataReaderWriterForRaptor.writeRaptorRoutes(raptorRoutesFilePath);

        // Write route-wise stops data for RAPTOR
        String raptorRouteStopsFilePath = "";
        gtfsDataReaderWriterForRaptor.writeRaptorRouteStops(raptorRouteStopsFilePath);

        // Write trips data
        String tripsFilePath = "";
        gtfsDataReaderWriterForRaptor.writeTrips(tripsFilePath);

        // Write route-wise stop times data for RAPTOR
        String raptorStopTimesFilePath = "";
        gtfsDataReaderWriterForRaptor.writeRaptorStopTimes(raptorStopTimesFilePath);

        // Write stops data for RAPTOR
        String raptorStopsFilePath = "";
        gtfsDataReaderWriterForRaptor.writeRaptorStops(raptorStopsFilePath);

        // Write stop-wise routes data for RAPTOR
        String raptorStopRoutesFilePath = "";
        gtfsDataReaderWriterForRaptor.writeRaptorStopRoutes(raptorStopRoutesFilePath);

        // Write transfers data for RAPTOR
        String raptorTransfersFilePath = "";
        gtfsDataReaderWriterForRaptor.writeRaptorTransfers(raptorTransfersFilePath);

    }
}