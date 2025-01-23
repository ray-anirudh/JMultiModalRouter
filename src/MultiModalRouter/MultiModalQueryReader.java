/**
 * Author: Anirudh Ray
 * Institution: Professorship of Traffic Engineering and Control, Technical University of Munich
 * Department: Mobility Systems Engineering, School of Engineering and Design
 * E-mail Address: Anirudh.Ray@tum.de
 * Purpose: Component of a Java-based multi-modal routing algorithm, built using RAPTOR, Dijkstra-algorithm, and
 * KD-Trees
 */

package src.MultiModalRouter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.LinkedHashMap;

public class MultiModalQueryReader {

    /**
     * ATTRIBUTE DEFINITIONS
     */
    private static final double MUNICH_LATITUDE_MIN = 48.0617;
    private static final double MUNICH_LATITUDE_MAX = 48.2481;
    private static final double MUNICH_LONGITUDE_MIN = 11.4436;
    private static final double MUNICH_LONGITUDE_MAX = 11.7226;
    private static final double STUDY_AREA_LATITUDE_MIN = 47.829752;
    private static final double STUDY_AREA_LATITUDE_MAX = 48.433757;
    private static final double STUDY_AREA_LONGITUDE_MIN = 10.962982;
    private static final double STUDY_AREA_LONGITUDE_MAX = 12.043762;
    private static final double MINIMUM_TRIP_LENGTH_KM = 10;
    LinkedHashMap<Long, MultiModalQuery> multiModalQueries = new LinkedHashMap<>();

    /**
     * BEHAVIOUR DEFINITIONS
     */

    // Build the "multiModalQueries" hashmap
    public void readMultiModalQueries(String multiModalQueriesFilePath) {
        try {
            // Reader for "multiModalQueries.csv"
            BufferedReader multiModalQueriesReader = new BufferedReader(new FileReader(multiModalQueriesFilePath));
            String newline;

            // Set up header array
            String[] multiModalQueriesHeaderArray = multiModalQueriesReader.readLine().split(",");
            int originLongitudeIndex = findIndexInArray("originLongitude",
                    multiModalQueriesHeaderArray);
            int originLatitudeIndex = findIndexInArray("originLatitude",
                    multiModalQueriesHeaderArray);
            int destinationLongitudeIndex = findIndexInArray("destinationLongitude",
                    multiModalQueriesHeaderArray);
            int destinationLatitudeIndex = findIndexInArray("destinationLatitude",
                    multiModalQueriesHeaderArray);
            int departureTimeIndex = findIndexInArray("departureTime", multiModalQueriesHeaderArray);

            // Read body and process data
            long multiModalQueryId = 0;
            while((newline = multiModalQueriesReader.readLine()) != null) {
                String[] multiModalQueryDataRecord = newline.split(",");
                double originLongitude = Double.parseDouble(multiModalQueryDataRecord[originLongitudeIndex]);
                double originLatitude = Double.parseDouble(multiModalQueryDataRecord[originLatitudeIndex]);
                double destinationLongitude = Double.parseDouble(multiModalQueryDataRecord[destinationLongitudeIndex]);
                double destinationLatitude = Double.parseDouble(multiModalQueryDataRecord[destinationLatitudeIndex]);
                int departureTime = Integer.parseInt(multiModalQueryDataRecord[departureTimeIndex]);

                if ((originLongitude < STUDY_AREA_LONGITUDE_MIN) || (originLatitude < STUDY_AREA_LATITUDE_MIN) ||
                        (destinationLongitude < MUNICH_LONGITUDE_MIN) || (destinationLatitude < MUNICH_LATITUDE_MIN) ||
                        (originLongitude > STUDY_AREA_LONGITUDE_MAX) || (originLatitude > STUDY_AREA_LATITUDE_MAX) ||
                        (destinationLongitude > MUNICH_LONGITUDE_MAX) || (destinationLatitude > MUNICH_LATITUDE_MAX) ||
                        (calculateEquiRectangularDistance(originLongitude, originLatitude, destinationLongitude,
                                destinationLatitude) < MINIMUM_TRIP_LENGTH_KM)) {
                } else {
                    MultiModalQuery multiModalQuery = new MultiModalQuery(originLongitude, originLatitude,
                            departureTime, destinationLongitude, destinationLatitude);
                    this.multiModalQueries.put(++multiModalQueryId, multiModalQuery);
                }
            }
            System.out.println(this.multiModalQueries.size() + " multi-modal queries read from " +
                    multiModalQueriesFilePath + ".");

        } catch (FileNotFoundException fNFE) {
            System.out.println("File not found at specified path: " + multiModalQueriesFilePath);
        } catch (IOException iOE) {
            System.out.println("Input-output exception. Please check input file: " + multiModalQueriesFilePath);
        }
    }

    // Index finder by column name strings
    private static int findIndexInArray(String columnHeaderToFind, String[] headerArray) {
        int columnPosition = -1;
        for (int i = 0; i < headerArray.length; i++) {
            if (headerArray[i].equalsIgnoreCase(columnHeaderToFind)) {
                columnPosition = i;
                break;
            }
        }
        return columnPosition;
    }

    // Calculate equi-rectangular distances
    private static double calculateEquiRectangularDistance(double originLongitude, double originLatitude,
                                                           double destinationLongitude, double destinationLatitude) {
        final int EARTH_RADIUS_KM = 6_371;
        double latitudeDifference = Math.toRadians(destinationLatitude - originLatitude);
        double longitudeDifference = Math.toRadians(destinationLongitude - originLongitude);
        double x = longitudeDifference * Math.cos(Math.toRadians((originLatitude + destinationLatitude) / 2));

        return EARTH_RADIUS_KM * Math.sqrt((x * x) + (latitudeDifference * latitudeDifference));
    }

    // Getter for map of multi-modal queries
    public LinkedHashMap<Long, MultiModalQuery> getMultiModalQueries() {
        return this.multiModalQueries;
    }
}