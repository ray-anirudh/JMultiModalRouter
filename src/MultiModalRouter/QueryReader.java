package src.MultiModalRouter;


// TODO: REVIEW USEFULNESS
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class QueryReader {
    private ArrayList<Query> queries = new ArrayList<>();

    public void readQueryList(String queriesFilePath) {
        try {
            // Reader for queries
            BufferedReader queryReader = new BufferedReader(new FileReader(queriesFilePath));
            String newline;

            // Set up header array
            String[] queriesHeaderArray = queryReader.readLine().split(",");
            int originStopIdIndex = findIndexInArray("origin_stop_id", queriesHeaderArray);
            int destinationStopIdIndex = findIndexInArray("destination_stop_id", queriesHeaderArray);
            int departureTimeIndex = findIndexInArray("departure_time", queriesHeaderArray);

            // Read body and prepare list of queries
            while((newline = queryReader.readLine()) != null) {
                String[] queryDataRecord = newline.split(",");
                int originStopId = Integer.parseInt(queryDataRecord[originStopIdIndex]);
                int destinationStopId = Integer.parseInt(queryDataRecord[destinationStopIdIndex]);
                String departureTimeHourString = queryDataRecord[departureTimeIndex].substring(0,2);
                String departureTimeMinuteString = queryDataRecord[departureTimeIndex].substring(3,5);
                int departureTime = Integer.parseInt(departureTimeHourString) * 60 + Integer.
                        parseInt(departureTimeMinuteString);

                Query query = new Query(originStopId, destinationStopId, departureTime);
                this.queries.add(query);
            }
            System.out.println("Queries read from " + queriesFilePath);

        } catch (FileNotFoundException fNFE) {
            System.out.println("File not found at specified path: " + queriesFilePath);
        } catch (IOException iOE) {
            System.out.println("Input-output exception. Please check input file: " + queriesFilePath);
        }
    }

    // Index finder by column name strings
    private int findIndexInArray(String columnHeaderName, String[] headerArray) {
        int columnPosition = -1;
        for (int i = 0; i <= headerArray.length; i++) {
            if (headerArray[i].equalsIgnoreCase(columnHeaderName)) {
                columnPosition = i;
            }
        }
        return columnPosition;
    }

    // Getter for queries
    public ArrayList<Query> getQueries() {
        return this.queries;
    }
}