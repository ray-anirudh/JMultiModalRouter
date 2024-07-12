package src.MultiModalRouter;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.LinkedHashMap;

public class MultiModalQueryReader {
    /**
     * BEHAVIOUR DEFINITIONS
     * The reader below is to create a map of multi-modal queries for the JMultiModalRouter algorithm
     */

    // Build hashmap for multi-modal routing queries
    LinkedHashMap<Integer, MultiModalQuery> readMultiModalQueries (String multiModalQueriesFilePath) {
        LinkedHashMap<Integer, MultiModalQuery> multiModalQueries = new LinkedHashMap<>();

        try {
            // Reader for "multiModalQueries.txt"
            BufferedReader multiModalQueriesReader = new BufferedReader(new FileReader(multiModalQueriesFilePath));
            String newline;

            // Set up header array
            String[] multiModalQueriesHeaderArray = multiModalQueriesReader.readLine().split(",");
            int originLongitudeIndex = findIndexInArray("originLongitude",
                    multiModalQueriesHeaderArray);
            int originLatitudeIndex = findIndexInArray("originLatitude", multiModalQueriesHeaderArray);
            int departureTimeIndex = findIndexInArray("departureTime", multiModalQueriesHeaderArray);
            int destinationLongitudeIndex = findIndexInArray("destinationLongitude",
                    multiModalQueriesHeaderArray);
            int destinationLatitudeIndex = findIndexInArray("destinationLatitude",
                    multiModalQueriesHeaderArray);

            int multiModalQueryId = 1;
            // Read body and process data
            while ((newline = multiModalQueriesReader.readLine()) != null) {
                String[] multiModalQueryRecord = newline.split(",");
                double originLongitude = Double.parseDouble(multiModalQueryRecord[originLongitudeIndex]);
                double originLatitude = Double.parseDouble(multiModalQueryRecord[originLatitudeIndex]);
                String departureTimeHourString = multiModalQueryRecord[departureTimeIndex].substring(0, 2);
                String departureTimeMinuteString = multiModalQueryRecord[departureTimeIndex].substring(3, 5);
                int departureTime = Integer.parseInt(departureTimeHourString) * 60 +
                        Integer.parseInt(departureTimeMinuteString);
                double destinationLongitude = Double.parseDouble(multiModalQueryRecord[destinationLongitudeIndex]);
                double destinationLatitude = Double.parseDouble(multiModalQueryRecord[destinationLatitudeIndex]);

                MultiModalQuery multiModalQuery = new MultiModalQuery(originLongitude, originLatitude, departureTime,
                        destinationLongitude, destinationLatitude);
                multiModalQueries.put(multiModalQueryId, multiModalQuery);

                multiModalQueryId++;
            }
            System.out.println("Multi-modal queries read from " + multiModalQueriesFilePath + ", and queries map" +
                    "built");

        } catch (FileNotFoundException fNFE) {
            System.out.println("File not found at specified path: " + multiModalQueriesFilePath);
        } catch (IOException iOE) {
            System.out.println("Input-output exception. Please check input file: " + multiModalQueriesFilePath);
        }

        return multiModalQueries;
    }

    /**
     * Supporting methods are below
     */

    // Index finder by column name strings
    private int findIndexInArray(String columnHeaderName, @NotNull String[] headerArray) {
        int columnPosition = -1;
        for (int i = 0; i <= headerArray.length; i++) {
            if (headerArray[i].equalsIgnoreCase(columnHeaderName)) {
                columnPosition = i;
            }
        }
        return columnPosition;
    }
}