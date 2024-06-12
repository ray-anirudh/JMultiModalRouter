package src.RoadTransportRouter.OSMDataManager;
// OSM: OpenStreetMap
// OPL: Object Per Line

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
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

    // Read and filter links from the OSM extract
    public void readAndFilterOsmLinks(String osmOplExtractFilePath) {
        // Initializing required indices
        int linkIdIndex = 0;
        int linkDetailsIndex = 0;
        int linkNodalArrayIndex = 0;

        // Create link types' hashset for filtering way records
        HashSet<String> linkTypeHashSet = new HashSet<>(Arrays.asList(LINK_TYPE_ARRAY));

        // Reader for first record of "BBBikeOSMExtract.opl"
        try {
            BufferedReader osmOplExtractReader = new BufferedReader(new FileReader(osmOplExtractFilePath));

            // Find relevant indices using the first OPL data record
            String[] firstOsmOplRecordArray = osmOplExtractReader.readLine().split(" ");
            linkIdIndex = findIndexInArray("n", firstOsmOplRecordArray); // First object is a node
            linkDetailsIndex = findIndexInArray("T", firstOsmOplRecordArray);
            linkNodalArrayIndex = linkDetailsIndex + 1;
        } catch (IOException iOE) {
            System.out.println("Input-output exception. Please check input file: " + osmOplExtractFilePath);
        }

        // Reader for body of "BBBikeOSMExtract.opl", including the first record
        try {
            BufferedReader osmOplExtractReader =new BufferedReader(new FileReader(osmOplExtractFilePath));
            String newline;

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

                        if (linkTypeHashSet.contains(linkType)) {
                            String[] nodalArray = linkDataRecord[linkNodalArrayIndex].split(",");

                            for (int i = 0; i <= nodalArray.length - 2; i++) {
                                int firstNodeId;
                                int characterOrderToParseFrom = (i == 0) ? 2 : 1;
                                firstNodeId = Integer.parseInt(nodalArray[i].substring(characterOrderToParseFrom));
                                Node firstNode = new Node(0, 0);

                                int secondNodeId;
                                secondNodeId = Integer.parseInt(nodalArray[i + 1].substring(1));
                                Node secondNode = new Node(0, 0);

                                int linkId = Integer.parseInt(wayId + "00" + (i + 1));
                                Link link = new Link(firstNodeId, secondNodeId);
                                link.setLinkType(linkType);

                                this.links.put(linkId, link);
                                this.nodes.put(firstNodeId, firstNode);
                                this.nodes.put(secondNodeId, secondNode);
                            }
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

    // Read and filter nodes from the OSM extract
    public void readAndFilterOsmNodes(String osmOplExtractFilePath) {
        // Initializing required indices
        int nodeIdIndex = 0;
        int xCoordinateIndex = 0;
        int yCoordinateIndex = 0;

        // Reader for first record of "BBBikeOSMExtract.opl"
        try {
            BufferedReader osmOplExtractReader = new BufferedReader(new FileReader(osmOplExtractFilePath));

            // Find relevant indices using the first OPL data record
            String[] firstOsmOplRecordArray = osmOplExtractReader.readLine().split(" ");
            nodeIdIndex = findIndexInArray("n", firstOsmOplRecordArray); // First object is a node
            xCoordinateIndex = findIndexInArray("x", firstOsmOplRecordArray);
            yCoordinateIndex = findIndexInArray("y", firstOsmOplRecordArray);
        } catch (IOException iOE) {
            System.out.println("Input-output exception. Please check input file: " + osmOplExtractFilePath);
        }

        // Reader for body of "BBBikeOSMExtract.opl", including the first record
        try {
            BufferedReader osmOplExtractReader =new BufferedReader(new FileReader(osmOplExtractFilePath));
            String newline;
    }




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