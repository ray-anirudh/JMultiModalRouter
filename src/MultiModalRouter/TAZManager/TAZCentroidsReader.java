package src.MultiModalRouter.TAZManager;

import java.io.*;
import java.util.LinkedHashMap;

public class TAZCentroidsReader {
    private LinkedHashMap<Integer, TAZCentroid> tAZCentroids = new LinkedHashMap<>();

    public void readTAZCentroids(String tAZCentroidsFilePath) {
        try {
            BufferedReader tAZCentroidReader = new BufferedReader(new FileReader(tAZCentroidsFilePath));
            String newline;

            // Read the header array
            String[] tAZHeaderArray = tAZCentroidReader.readLine().split("[,.;]|^");
            int tAZIdIndex = findIndexInArray(tAZHeaderArray, "objectid");
            int tAZXCoordinateIndex = findIndexInArray(tAZHeaderArray, "xCoordinate25832");
            int tAZYCoordinateIndex = findIndexInArray(tAZHeaderArray, "yCoordinate25832");
            int tAZLongitudeIndex = findIndexInArray(tAZHeaderArray, "Longitude");
            int tAZLatitudeIndex = findIndexInArray(tAZHeaderArray, "Latitude");

            // Read data body
            int id = 0;
            while ((newline = tAZCentroidReader.readLine()) != null) {
                String[] tAZCentroidDataRecord = newline.split("[,.;]|^");
                double xCoordinate25832 = Double.parseDouble(tAZCentroidDataRecord[tAZXCoordinateIndex]);
                double yCoordinate25832 = Double.parseDouble(tAZCentroidDataRecord[tAZYCoordinateIndex]);
                double Longitude = Double.parseDouble(tAZCentroidDataRecord[tAZLongitudeIndex]);
                double Latitude = Double.parseDouble(tAZCentroidDataRecord[tAZLatitudeIndex]);

                TAZCentroid tAZCentroid = new TAZCentroid(++id, xCoordinate25832, yCoordinate25832, Longitude,
                        Latitude);
                this.tAZCentroids.put(id, tAZCentroid);
            }
            tAZCentroidReader.close();

        } catch (FileNotFoundException fNFE) {
            System.out.println("File not found at " + tAZCentroidsFilePath);
            fNFE.printStackTrace();
        } catch (IOException iOE) {
            System.out.println("Input-output exception; please check the file at " + tAZCentroidsFilePath);
            iOE.printStackTrace();
        }
    }

    private int findIndexInArray(String[] headerArray, String stringToMatch) {
        int columnIndex = -1;
        for (int i = 0; i < headerArray.length; i++) {
            if (headerArray[i].equalsIgnoreCase(stringToMatch)) {
                columnIndex = i;
                break;
            }
        }

        return columnIndex;
    }
}
