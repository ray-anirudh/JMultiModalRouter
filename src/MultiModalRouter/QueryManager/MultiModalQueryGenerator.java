/**
 * Author: Anirudh Ray
 * Institution: Professorship of Traffic Engineering and Control, Technical University of Munich
 * Department: Mobility Systems Engineering, School of Engineering and Design
 * E-mail Address: Anirudh.Ray@tum.de
 * Purpose: Component of a Java-based multi-modal routing algorithm, built using RAPTOR, Dijkstra-algorithm, and
 * KD-Trees
 */

package src.MultiModalRouter.QueryManager;
// ODM: On-demand Mobility

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;

public class MultiModalQueryGenerator {
    private static final double MUNICH_LATITUDE_MIN = 48.0617;
    private static final double MUNICH_LATITUDE_MAX = 48.2481;
    private static final double MUNICH_LONGITUDE_MIN = 11.4436;
    private static final double MUNICH_LONGITUDE_MAX = 11.7226;
    private static final double STUDY_AREA_LATITUDE_MIN = 47.829752;
    private static final double STUDY_AREA_LATITUDE_MAX = 48.433757;
    private static final double STUDY_AREA_LONGITUDE_MIN = 10.962982;
    private static final double STUDY_AREA_LONGITUDE_MAX = 12.043762;
    private static final int EARTH_RADIUS_KM = 6_371;
    private static final double TRIP_LENGTH_FACTOR = 1.4;
    private static final double MINIMUM_TRIP_LENGTH_KM = 10;
    private static final Random RANDOM = new Random();
    private final LinkedHashMap<Long, MultiModalQuery> multiModalQueries = new LinkedHashMap<>();

    // Generate queries to pass into the multi-modal router
    LinkedHashMap<Long, MultiModalQuery> generateQueries(long numberOfQueries) {
        for (long queryCount = 1; queryCount <= numberOfQueries; ) {
            /* Generating longitudes and latitudes of origin and destination points as per multi-variate Gaussian logic
            Arguments are specified to generate destinations within Munich and origins within its environs
            */
            double originLongitude = STUDY_AREA_LONGITUDE_MIN + ((STUDY_AREA_LONGITUDE_MAX - STUDY_AREA_LONGITUDE_MIN) *
                    (RANDOM.nextGaussian(0.5, 0.16)));
            double originLatitude = STUDY_AREA_LATITUDE_MIN + ((STUDY_AREA_LATITUDE_MAX - STUDY_AREA_LATITUDE_MIN) *
                    (RANDOM.nextGaussian(0.5, 0.16)));
            double destinationLongitude = MUNICH_LONGITUDE_MIN + ((MUNICH_LONGITUDE_MAX - MUNICH_LONGITUDE_MIN) *
                    (RANDOM.nextGaussian(0.5, 0.16)));
            double destinationLatitude = MUNICH_LATITUDE_MIN + ((MUNICH_LATITUDE_MAX - MUNICH_LATITUDE_MIN) *
                    (RANDOM.nextGaussian(0.5, 0.16)));

            // Calculate ballpark trip lengths
            double longitudeDifference = Math.toRadians(destinationLongitude - originLongitude);
            double latitudeDifference = Math.toRadians(destinationLatitude - originLatitude);
            double x = longitudeDifference * Math.cos(Math.toRadians((originLatitude + destinationLatitude) / 2));
            double tripLengthKm = EARTH_RADIUS_KM * Math.sqrt(x * x + latitudeDifference * latitudeDifference) *
                    TRIP_LENGTH_FACTOR;

            // Use trips (origin-destination pairs) that are long enough to be ODM-transit-walk trips
            if (tripLengthKm >= MINIMUM_TRIP_LENGTH_KM) {
                // Generating desired departure time for the trip
                double hourRandomizer = RANDOM.nextDouble() * 100;
                double minuteRandomizer = RANDOM.nextDouble();
                final int MINUTES_PER_HOUR = 60;

                int hourOfDay;
                if (hourRandomizer < 0.56) {
                    hourOfDay = 0;
                } else if (hourRandomizer < 1.12) {
                    hourOfDay = 1;
                } else if (hourRandomizer < 1.68) {
                    hourOfDay = 2;
                } else if (hourRandomizer < 2.24) {
                    hourOfDay = 3;
                } else if (hourRandomizer < 3.35) {
                    hourOfDay = 4;
                } else if (hourRandomizer < 5.57) {
                    hourOfDay = 5;
                } else if (hourRandomizer < 8.90) {
                    hourOfDay = 6;
                } else if (hourRandomizer < 16.68) {
                    hourOfDay = 7;
                } else if (hourRandomizer < 26.68) {
                    hourOfDay = 8;
                } else if (hourRandomizer < 35.57) {
                    hourOfDay = 9;
                } else if (hourRandomizer < 40.01) {
                    hourOfDay = 10;
                } else if (hourRandomizer < 43.34) {
                    hourOfDay = 11;
                } else if (hourRandomizer < 46.67) {
                    hourOfDay = 12;
                } else if (hourRandomizer < 51.11) {
                    hourOfDay = 13;
                } else if (hourRandomizer < 56.67) {
                    hourOfDay = 14;
                } else if (hourRandomizer < 62.23) {
                    hourOfDay = 15;
                } else if (hourRandomizer < 68.89) {
                    hourOfDay = 16;
                } else if (hourRandomizer < 76.67) {
                    hourOfDay = 17;
                } else if (hourRandomizer < 83.34) {
                    hourOfDay = 18;
                } else if (hourRandomizer < 88.90) {
                    hourOfDay = 19;
                } else if (hourRandomizer < 93.34) {
                    hourOfDay = 20;
                } else if (hourRandomizer < 96.67) {
                    hourOfDay = 21;
                } else if (hourRandomizer < 98.89) {
                    hourOfDay = 22;
                } else {
                    hourOfDay = 23;
                }

                int departureTime = (int) (hourOfDay * MINUTES_PER_HOUR + minuteRandomizer * MINUTES_PER_HOUR);
                MultiModalQuery multiModalQuery = new MultiModalQuery(originLongitude, originLatitude, departureTime,
                        destinationLongitude, destinationLatitude);
                this.multiModalQueries.put(queryCount++, multiModalQuery);
            }
        }
        System.out.println("Multi-modal queries generated");
        return this.multiModalQueries;
    }

    // Write out multimodal queries to a file
    void writeMultiModalQueries(String multiModalQueriesFilePath) {
        try {
            // Writer for "multiModalQueries.txt"
            BufferedWriter multiModalQueriesWriter = new BufferedWriter(new FileWriter(multiModalQueriesFilePath));

            // Set up header array
            multiModalQueriesWriter.write("query_id,origin_longitude,origin_latitude,destination_longitude," +
                    "destination_latitude,departure_time\n");

            // Write body based on "multiModalQueries" hashmap
            for (HashMap.Entry<Long, MultiModalQuery> multiModalQueryEntry : this.multiModalQueries.entrySet()) {
                long queryId = multiModalQueryEntry.getKey();
                double originLongitude = multiModalQueryEntry.getValue().getOriginLongitude();
                double originLatitude = multiModalQueryEntry.getValue().getOriginLatitude();
                double destinationLongitude = multiModalQueryEntry.getValue().getDestinationLongitude();
                double destinationLatitude = multiModalQueryEntry.getValue().getDestinationLatitude();
                int departureTime = multiModalQueryEntry.getValue().getDepartureTime();

                multiModalQueriesWriter.write(queryId + "," + originLongitude + "," + originLatitude + "," +
                            destinationLongitude + "," + destinationLatitude + "," + departureTime + "\n");
                }
            System.out.println("Multi-modal queries' data written to " + multiModalQueriesFilePath);

        } catch (IOException iOE) {
            System.out.println("Input-output exception. Please check the \"multiModalQueries\" hashmap.");
            iOE.printStackTrace();
        }
    }
}