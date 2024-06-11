package src.RoadTransportRouter.OSMDataParser;
// OSM: OpenStreetMap
// OPL: Object Per Line

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;

public class OSMDataReaderWriter {

    /**
     * ATTRIBUTE DEFINITIONS
     */
    private static final String[] LINK_TYPE_ARRAY = {
            "bridleway",
            // "cycleway", Reason: Cannot be used by a driving-based mode
            // "footway", Reason: Cannot be used by a driving-based mode
            "living_street",
            "motorway",
            "motorway_link",
            // "path", Reason: Cannot be used by a driving-based mode
            // "pedestrian", Reason: Cannot be used by a driving-based mode
            "primary",
            "primary_link",
            "residential",
            "secondary",
            "secondary_link",
            "service",
            // "steps", Reason: Cannot be used by a driving-based mode
            "tertiary",
            "tertiary_link",
            "track",
            "track_grade1",
            "track_grade2",
            "track_grade3",
            "track_grade4",
            "track_grade5",
            "trunk",
            "trunk_link",
            "unclassified"
    };

    // Initialize the Dijkstra-relevant hashmaps
    LinkedHashMap<Integer, Node> nodes = new LinkedHashMap<>();
    // Keys for "nodes" hashmap refer to node IDs, and values pertain to nodal coordinates

    LinkedHashMap<Integer, Link> links = new LinkedHashMap<>();
    // Keys for "links" hashmap refers to link IDs, and values pertain to associated nodes, link types, and travel costs

    /**
     * BEHAVIOUR DEFINITIONS
     */

    public void readAndFilterOsmRoads(String osmOplExtractFilePath) {
        try {
            // Reader for "BBBikeOSMExtract.opl"
            BufferedReader osmOplExtractReader = new BufferedReader(new FileReader(osmOplExtractFilePath));
            String newline;

            // Find relevant indices using the first OPL data record
            String[] firstOsmOplRecordArray = osmOplExtractReader.readLine().split(" ");
            int linkIdIndex = findIndexInArray("n", firstOsmOplRecordArray); // First object is a node
            int linkDetailsIndex = findIndexInArray("T", firstOsmOplRecordArray);
            int linkNodalArrayIndex = linkDetailsIndex + 1;

            // Read body and process data for all links in the network
            while((newline = osmOplExtractReader.readLine()) != null) {
                if (newline.substring(0, 1).equalsIgnoreCase("w")) {
                    String[] linkDataRecord = newline.split(" ");
                    String wayType = linkDataRecord[linkDetailsIndex].substring(1, 8);
                    if (wayType.equalsIgnoreCase("highway")) {
                        int wayId = Integer.parseInt(linkDataRecord[linkIdIndex].substring(1));

                        String[] linkDetailsRecord = linkDataRecord[linkDetailsIndex].split(",");
                        /* Example of link details record: Thighway=track,maxspeed:type=DE:rural,surface=asphalt,
                        tracktype=grade1 Nn1755165066,n1755165067,n262608882
                        */

                        String linkType = linkDetailsRecord[0].substring(9);

                        String[] nodalArray = linkDataRecord[linkNodalArrayIndex].split(",");
                        for (int i = 0; i <= nodalArray.length - 2; i++) {
                            int firstNodeId = -1;
                            if (i == 0) {
                                firstNodeId = Integer.parseInt(nodalArray[i].substring(2));
                            } else {
                                firstNodeId = Integer.parseInt(nodalArray[i].substring(1));
                            }

                            int secondNodeId = -1;
                            secondNodeId = Integer.parseInt(nodalArray[i + 1].substring(1));

                            int linkId = Integer.parseInt(wayId + "00" + (i + 1));
                            Link link = new Link(firstNodeId, secondNodeId);
                            link.setLinkType(linkType);

                            this.links.put(linkId, link);
                        }
                    }
                }
            }
        } catch (FileNotFoundException fNFE) {
            System.out.println("File not found at specified path: " + osmOplExtractFilePath);
        } catch (IOException iOE) {
            System.out.println("Input-output exception. Please check input file: " + osmOplExtractFilePath);
        }
    }

    public void read




    // TODO: Remove all links with stupid types, like busstop and platform, like dang
    // TODO: Calculate all link distances, but that needs setting up of nodes first
    // TODO set up the damn nodes dawg

    // Index finder using alphabets in the first record of an OSM OPL extract
    private int findIndexInArray(String alphabetToFind, @NotNull String[] headerArray) {
        int columnPosition = -1;
        for (int i = 0; i <= headerArray.length; i++) {
            if (headerArray[i].contains(alphabetToFind)) {
                columnPosition = i;
            }
        }
        return columnPosition;
    }
}