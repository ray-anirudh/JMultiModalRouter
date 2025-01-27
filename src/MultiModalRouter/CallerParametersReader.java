package src.MultiModalRouter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class CallerParametersReader {
    private long beginQueryId;
    private long numberMultiModalQueries;
    private double minimumDrivingDistance;
    private double maximumDrivingDistance;
    private double avgWalkingSpeedMPMin;
    private double avgDrivingSpeedMPMin;
    private double avgODMWaitTimeMin;
    private double departureTimeForTAZToTAZTravel;
    private int stopTypeToIgnore;
    private int cutoffDailyServiceCountOfStop;
    private String osmOplExtractFilePath;
    private String dijkstraFolderPath;
    private String gtfsFolderPath;
    private String raptorFolderPath;
    private String gtfsParametersFilePath;
    private String multiModalQueriesFilePath;
    private String tAZCentroidsFilePath;


    public void readCallerParameters(String callerParametersFilePath) {
        try {
            BufferedReader callerParametersFileReader = new BufferedReader(new FileReader(callerParametersFilePath));

            // Read and store order of the starting query, and number of queries to be processed
            this.beginQueryId = Long.parseLong(callerParametersFileReader.readLine().split("[=]")[1]);
            this.numberMultiModalQueries = Long.parseLong(callerParametersFileReader.readLine().split("[=]")[1]);

            // Read and store the doughnut-shaped search space's specifications
            this.minimumDrivingDistance = Double.parseDouble(callerParametersFileReader.readLine().split("[=]")
                    [1]);
            this.maximumDrivingDistance = Double.parseDouble(callerParametersFileReader.readLine().split("[=]")
                    [1]);

            // Read and store travelling speeds, departure times, and waiting times
            this.avgWalkingSpeedMPMin = Double.parseDouble(callerParametersFileReader.readLine().split("[=]")[1]);
            this.avgDrivingSpeedMPMin = Double.parseDouble(callerParametersFileReader.readLine().split("[=]")[1]);
            this.avgODMWaitTimeMin = Double.parseDouble(callerParametersFileReader.readLine().split("[=]")[1]);
            this.departureTimeForTAZToTAZTravel = Double.parseDouble(callerParametersFileReader.readLine().
                    split("[=]")[1]);

            // Read and store the heuristics-defining values
            this.stopTypeToIgnore = Integer.parseInt(callerParametersFileReader.readLine().split("[=]")[1]);
            this.cutoffDailyServiceCountOfStop = Integer.parseInt(callerParametersFileReader.readLine().
                    split("[=]")[1]);

            // Read and store all filepaths relevant to JMMR's execution
            this.osmOplExtractFilePath = callerParametersFileReader.readLine().split("[=]")[1];
            this.dijkstraFolderPath = callerParametersFileReader.readLine().split("[=]")[1];
            this.gtfsFolderPath = callerParametersFileReader.readLine().split("[=]")[1];
            this.raptorFolderPath = callerParametersFileReader.readLine().split("[=]")[1];
            this.gtfsParametersFilePath = callerParametersFileReader.readLine().split("[=]")[1];
            this.multiModalQueriesFilePath = callerParametersFileReader.readLine().split("[=]")[1];
            this.tAZCentroidsFilePath = callerParametersFileReader.readLine().split("[=]")[1];

        } catch (FileNotFoundException fNFE) {
            System.out.println("File not found at " + callerParametersFilePath);
        } catch (IOException iOE) {
            System.out.println("Input-output exception; please check the file at " + callerParametersFilePath);
        }
    }

    public long getBeginQueryId() {
        return this.beginQueryId;
    }

    public long getNumberMultiModalQueries() {
        return this.numberMultiModalQueries;
    }

    public double getAvgWalkingSpeedMPMin() {
        return this.avgWalkingSpeedMPMin;
    }

    public double getAvgDrivingSpeedMPMin() {
        return this.avgDrivingSpeedMPMin;
    }

    public double getMinimumDrivingDistance() {
        return this.minimumDrivingDistance;
    }

    public double getMaximumDrivingDistance() {
        return this.maximumDrivingDistance;
    }

    public double getAvgODMWaitTimeMin() {
        return this.avgODMWaitTimeMin;
    }

    public double getDepartureTimeForTAZToTAZTravel() {
        return this.departureTimeForTAZToTAZTravel;
    }

    public int getStopTypeToIgnore() {
        return this.stopTypeToIgnore;
    }

    public int getCutoffDailyServiceCountOfStop() {
        return this.cutoffDailyServiceCountOfStop;
    }

    public String getOsmOplExtractFilePath() {
        return this.osmOplExtractFilePath;
    }

    public String getDijkstraFolderPath() {
        return this.dijkstraFolderPath;
    }

    public String getGtfsFolderPath() {
        return this.gtfsFolderPath;
    }

    public String getRaptorFolderPath() {
        return this.raptorFolderPath;
    }

    public String getGtfsParametersFilePath() {
        return this.gtfsParametersFilePath;
    }

    public String getMultiModalQueriesFilePath() {
        return this.multiModalQueriesFilePath;
    }

    public String getTAZCentroidsFilePath() {
        return this.tAZCentroidsFilePath;
    }
}