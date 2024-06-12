package src.RoadTransportRouter.OSMDataManager;
// OSM: OpenStreetMap
// OPL: Object Per Line

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

public class OSMDataReaderWriter {

    /**
     * ATTRIBUTE DEFINITIONS
     */
    /* Array to limit the way types (classes) parsed out of the OSM extract
    Refer to: https://wiki.openstreetmap.org/wiki/Key:highway) for more details
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

    private static final int EARTH_RADIUS_M = 6371000;

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
            BufferedReader osmOplExtractReader = new BufferedReader(new FileReader(osmOplExtractFilePath));
            String newline;

            // Read body and process data for all links in the network
            while ((newline = osmOplExtractReader.readLine()) != null) {
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
                                int characterOrder = (i == 0) ? 2 : 1;
                                firstNodeId = Integer.parseInt(nodalArray[i].substring(characterOrder));
                                Node firstNode = new Node(0, 0);

                                int secondNodeId;
                                secondNodeId = Integer.parseInt(nodalArray[i + 1].substring(1));
                                Node secondNode = new Node(0, 0);

                                int linkId = Integer.parseInt(wayId + "00" + (i + 1));
                                Link link = new Link(firstNodeId, secondNodeId, linkType);

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
        int longitudeIndex = 0;
        int latitudeIndex = 0;

        // Reader for first record of "BBBikeOSMExtract.opl"
        try {
            BufferedReader osmOplExtractReader = new BufferedReader(new FileReader(osmOplExtractFilePath));

            // Find relevant indices using the first OPL data record
            String[] firstOsmOplRecordArray = osmOplExtractReader.readLine().split(" ");
            nodeIdIndex = findIndexInArray("n", firstOsmOplRecordArray); // First object is a node
            longitudeIndex = findIndexInArray("x", firstOsmOplRecordArray);
            latitudeIndex = findIndexInArray("y", firstOsmOplRecordArray);

        } catch (IOException iOE) {
            System.out.println("Input-output exception. Please check input file: " + osmOplExtractFilePath);
        }

        // Reader for body of "BBBikeOSMExtract.opl", including the first record
        try {
            BufferedReader osmOplExtractReader = new BufferedReader(new FileReader(osmOplExtractFilePath));
            String newline;

            while ((newline = osmOplExtractReader.readLine()) != null) {
                // Example of node details record: n127290 v0 dV c0 t i0 u T x11.3246338 y48.2164498
                String[] nodeDataRecord = newline.split(" ");
                int nodeId = Integer.parseInt(nodeDataRecord[nodeIdIndex].substring(1));

                if (this.nodes.containsKey(nodeId)) {
                    double nodeLongitude = Double.parseDouble(nodeDataRecord[longitudeIndex].substring(1));
                    double nodeLatitude = Double.parseDouble(nodeDataRecord[latitudeIndex].substring(1));
                    Node replacementNode = new Node(nodeLongitude, nodeLatitude);
                    this.nodes.replace(nodeId, replacementNode);
                }
            }

        } catch (FileNotFoundException fNFE) {
            System.out.println("File not found at specified path: " + osmOplExtractFilePath);
        } catch (IOException iOE) {
            System.out.println("Input-output exception. Please check input file: " + osmOplExtractFilePath);
        }
    }

    // Count the number of links associated with each node
    public void countLinksPerNode() {
        for (HashMap.Entry<Integer, Node> nodeEntry : this.nodes.entrySet()) {
            int numberLinks = 0;
            for (Link link : this.links.values()) {
                if ((link.getFirstNodeId() == nodeEntry.getKey()) || (link.getSecondNodeId() == nodeEntry.getKey())) {
                    numberLinks++;
                }
            }
            nodeEntry.getValue().setNumberLinks(numberLinks);
        }
    }

    // Calculate the travel cost per link (purely in terms of equi-rectangular geometric distances for now, which are beeline distances)
    public void calculateLinkLengths() {
        for (Link link : this.links.values()) {
            int firstNodeId = link.getFirstNodeId();
            int secondNodeId = link.getSecondNodeId();

            double firstNodeLongitudeRadians = Math.toRadians(this.nodes.get(firstNodeId).getNodeLongitude());
            double firstNodeLatitudeRadians = Math.toRadians(this.nodes.get(firstNodeId).getNodeLatitude());
            double secondNodeLongitudeRadians = Math.toRadians(this.nodes.get(secondNodeId).getNodeLongitude());
            double secondNodeLatitudeRadians = Math.toRadians(this.nodes.get(secondNodeId).getNodeLatitude());

            double latitudeDifference = secondNodeLatitudeRadians - firstNodeLatitudeRadians;
            double longitudeDifference = secondNodeLongitudeRadians - firstNodeLongitudeRadians;

            double x = longitudeDifference * Math.cos((firstNodeLatitudeRadians + secondNodeLatitudeRadians) / 2);
            double y = latitudeDifference;

            double linkLengthM = Math.sqrt(x * x + y * y) * EARTH_RADIUS_M;

            link.setLinkLengthM(linkLengthM);
        }
    }

    // Remove links starting and ending at the same node
    public void removeCircularLinks() {
        for (HashMap.Entry<Integer, Link> linkEntry : this.links.entrySet()) {
            if (linkEntry.getValue().getFirstNodeId() == linkEntry.getValue().getSecondNodeId()) {
                this.links.remove(linkEntry.getKey());
            }
        }
    }

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