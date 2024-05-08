package src.PublicTransportRouter.GTFSDataParser;

public class GTFSDataParserRunner {
    // TODO: FIX THE ABNORMAL TERMINATION
    public static void main(String[] args) {
        GTFSDataReaderWriter gtfsDataReaderWriter = new GTFSDataReaderWriter();
        gtfsDataReaderWriter.readGTFSRoutes("src/PublicTransportRouter/GTFSDatenMVG/routes.txt");
    }
}