package src.PublicTransportRouter.GTFSDataManager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class GTFSParametersReader {
    private ArrayList<String> agencyIdList = new ArrayList<>();
    private double studyAreaLatitudeMin;
    private double studyAreaLatitudeMax;
    private double studyAreaLongitudeMin;
    private double studyAreaLongitudeMax;
    private int maxWalkingDistanceM;
    private double avgWalkingSpeedMPMin;
    private double avgDrivingSpeedMPMin;
    private String osmOplExtractFilePath;
    public void readGTFSParameters(String gtfsParametersFilePath) {
        try {
            BufferedReader gtfsParametersFileReader = new BufferedReader(new FileReader(gtfsParametersFilePath));
            String newline;

            // Read and store identifiers of all relevant agencies
            newline = gtfsParametersFileReader.readLine();
            String[] agencyIdsRecord = newline.split("[,=]");

            if (agencyIdsRecord.length > 1) {   // To ensure that agency IDs exist in the record to be parsed
                this.agencyIdList.addAll(Arrays.asList(agencyIdsRecord).subList(1, agencyIdsRecord.length));
            } else {
            System.out.println("There are no transit agencies in the configuration file that can be considered." +
                    "Hence, transit (GTFS) data cannot be parsed.");
            }

            // Read and store coordinates of study area boundaries
            String[] studyAreaLatitudeMinRecord = gtfsParametersFileReader.readLine().split("[,=]");
            String[] studyAreaLatitudeMaxRecord = gtfsParametersFileReader.readLine().split("[,=]");
            String[] studyAreaLongitudeMinRecord = gtfsParametersFileReader.readLine().split("[,=]");
            String[] studyAreaLongitudeMaxRecord = gtfsParametersFileReader.readLine().split("[,=]");

            this.studyAreaLatitudeMin = Double.parseDouble(studyAreaLatitudeMinRecord[1]);
            this.studyAreaLatitudeMax = Double.parseDouble(studyAreaLatitudeMaxRecord[1]);
            this.studyAreaLongitudeMin = Double.parseDouble(studyAreaLongitudeMinRecord[1]);
            this.studyAreaLongitudeMax = Double.parseDouble(studyAreaLongitudeMaxRecord[1]);

            // Read and store walking- and Dijkstra-related parameters
            String[] maxWalkingDistanceMRecord = gtfsParametersFileReader.readLine().split("[,=]");
            String[] avgWalkingSpeedMPMinRecord = gtfsParametersFileReader.readLine().split("[,=]");
            String[] avgDrivingSpeedMPMinRecord = gtfsParametersFileReader.readLine().split("[,=]");
            String[] osmOplExtractFilePathRecord = gtfsParametersFileReader.readLine().split("[,=]");

            this.maxWalkingDistanceM = Integer.parseInt(maxWalkingDistanceMRecord[1]);
            this.avgWalkingSpeedMPMin = Double.parseDouble(avgWalkingSpeedMPMinRecord[1]);
            this.avgDrivingSpeedMPMin = Double.parseDouble(avgDrivingSpeedMPMinRecord[1]);
            this.osmOplExtractFilePath = osmOplExtractFilePathRecord[1];

        } catch (FileNotFoundException fNFE) {
            System.out.println("File not found at " + gtfsParametersFilePath);
        } catch (IOException iOE) {
            System.out.println("Input-output exception; please check the file at " + gtfsParametersFilePath);
        }
    }

    public ArrayList<String> getAgencyIdList() {
        return this.agencyIdList;
    }

    public double getStudyAreaLatitudeMin() {
        return this.studyAreaLatitudeMin;
    }

    public double getStudyAreaLatitudeMax() {
        return this.studyAreaLatitudeMax;
    }

    public double getStudyAreaLongitudeMin() {
        return this.studyAreaLongitudeMin;
    }

    public double getStudyAreaLongitudeMax() {
        return this.studyAreaLongitudeMax;
    }

    public int getMaxWalkingDistanceM() {
        return this.maxWalkingDistanceM;
    }

    public double getAvgWalkingSpeedMPMin() {
        return this.avgWalkingSpeedMPMin;
    }

    public double getAvgDrivingSpeedMPMin() {
        return this.avgDrivingSpeedMPMin;
    }

    public String getOsmOplExtractFilePath() {
        return this.osmOplExtractFilePath;
    }

    // Comma counter in a string
    private int countCommasInString(String string) {
        int commaCount = 0;
        for (int i = 0; i < string.length(); i++) {
            if (string.substring(i, i + 1).equalsIgnoreCase(",")) {
                commaCount++;
            }
        }

        return commaCount;
    }
}
