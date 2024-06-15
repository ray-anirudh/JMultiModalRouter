package src.RoadTransportRouter.OSMDataManager;
// OSM: OpenStreetMap
// OPL: Object Per Line

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class OSMDataReaderWriter {

    /**
     * ATTRIBUTE DEFINITIONS
     */

    /* Array to limit the way types (classes) parsed out of the OSM extract
    Refer to: https://wiki.openstreetmap.org/wiki/Key:highway) for more details
    */
    private static final String[] LINK_TYPE_ARRAY = {
            "bridleway",
            // "cycleway", Cannot be used by a driving-based mode
            // "footway", Cannot be used by a driving-based mode
            "living_street",
            "motorway",
            "motorway_link",
            // "path", Cannot be used by a driving-based mode
            // "pedestrian", Cannot be used by a driving-based mode
            "primary",
            "primary_link",
            "residential",
            "secondary",
            "secondary_link",
            "service",
            // "steps", Cannot be used by a driving-based mode
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
    LinkedHashMap<Long, Node> nodes = new LinkedHashMap<>();
    // Keys for "nodes" hashmap refer to node IDs, and values pertain to nodal coordinates and associated links' lists

    LinkedHashMap<Long, Link> links = new LinkedHashMap<>();
    // Keys for "links" hashmap refer to link IDs, and values pertain to associated nodes, link types, and travel costs

    /**
     * BEHAVIOUR DEFINITIONS
     */

    /**
     * All readers and dataset manipulators below are for Dijkstra-relevant data, and sourced from OSM files
     */

    // Read and filter links from the OSM extract
    public void readAndFilterOsmLinks(String osmOplExtractFilePath) {
        try {
            // Reader for first record of "BBBikeOSMExtract.opl"
            BufferedReader osmOplExtractReader = new BufferedReader(new FileReader(osmOplExtractFilePath));
            String newline;

            // Find relevant indices using the first OPL data record
            String[] firstOsmOplRecordArray = osmOplExtractReader.readLine().split(" ");
            int linkIdIndex = findIndexInArray("n", firstOsmOplRecordArray);
            // First object is a node
            int linkAttributesIndex = findIndexInArray("T", firstOsmOplRecordArray);
            int linkNodesIndex = linkAttributesIndex + 1;

            // Create link types' hashset for filtering way records pertaining to the road network
            HashSet<String> linkTypeHashSet = new HashSet<>(Arrays.asList(LINK_TYPE_ARRAY));

            // Read body and process data for all links in the road network
            while ((newline = osmOplExtractReader.readLine()) != null) {
                if (newline.substring(0, 1).equalsIgnoreCase("w")) {
                    String[] linkDataRecord = newline.split(" ");
                    String linkAttributes = linkDataRecord[linkAttributesIndex];

                    if (linkAttributes.contains("highway")) {
                        long wayId = Long.parseLong(linkDataRecord[linkIdIndex].substring(1));
                        String[] linkAttributesRecord = linkAttributes.split(",");
                        int linkTypeIndex = findIndexInArray("highway", linkAttributesRecord);
                        /* Example of a link's details record: Thighway=track,maxspeed:type=DE:rural,surface=asphalt,
                        tracktype=grade1 Nn1755165066,n1755165067,n262608882
                        */

                        String linkType = linkAttributesRecord[linkTypeIndex].substring((linkTypeIndex == 0) ? 9 : 8);
                        if (linkTypeHashSet.contains(linkType)) {
                            String[] nodesArray = linkDataRecord[linkNodesIndex].split(",");

                            for (int i = 0; i <= nodesArray.length - 2; i++) {
                                // "first" and "second" nodes do not signify any ordering of nodes/ links
                                long firstNodeId;
                                firstNodeId = Long.parseLong(nodesArray[i].substring((i == 0) ? 2 : 1));
                                Node firstNode = new Node();

                                long secondNodeId;
                                secondNodeId = Long.parseLong(nodesArray[i + 1].substring(1));
                                Node secondNode = new Node();

                                long linkId = Long.parseLong(wayId + "000" + (i + 1));
                                Link link = new Link(firstNodeId, secondNodeId, linkType);

                                this.links.put(linkId, link);
                                this.nodes.put(firstNodeId, firstNode);
                                this.nodes.put(secondNodeId, secondNode);
                            }
                        }
                    }
                }
            }
            System.out.println("Links' data read from " + osmOplExtractFilePath);

        } catch (FileNotFoundException fNFE) {
            System.out.println("File not found at specified path: " + osmOplExtractFilePath);
        } catch (IOException iOE) {
            System.out.println("Input-output exception. Please check input file: " + osmOplExtractFilePath);
        }
    }


    // Remove links starting and ending at the same node (helpful against exceptional cases)
    public void removeCircularLinks() {
        ArrayList<Long> linkIdsList = new ArrayList<>(this.links.keySet());
        for (long linkId : linkIdsList) {
            if (this.links.get(linkId).getFirstNodeId() == this.links.get(linkId).getSecondNodeId()) {
                this.links.remove(linkId);
            }
        }
        System.out.println("Circular links removed");
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
                // Example of a node's details record: n127290 v0 dV c0 t i0 u T x11.3246338 y48.2164498
                String[] nodeDataRecord = newline.split(" ");
                long nodeId = Long.parseLong(nodeDataRecord[nodeIdIndex].substring(1));

                if (this.nodes.containsKey(nodeId)) {
                    double nodeLongitude = Double.parseDouble(nodeDataRecord[longitudeIndex].substring(1));
                    double nodeLatitude = Double.parseDouble(nodeDataRecord[latitudeIndex].substring(1));
                    this.nodes.get(nodeId).setNodeLongitude(nodeLongitude);
                    this.nodes.get(nodeId).setNodeLatitude(nodeLatitude);
                }
            }
            System.out.println("Nodes' data read from " + osmOplExtractFilePath);

        } catch (FileNotFoundException fNFE) {
            System.out.println("File not found at specified path: " + osmOplExtractFilePath);
        } catch (IOException iOE) {
            System.out.println("Input-output exception. Please check input file: " + osmOplExtractFilePath);
        }
    }

    // Associate links with respective nodes
    public void associateLinksWithNode() {
        for (HashMap.Entry<Long, Node> nodeEntry : this.nodes.entrySet()) {
            for (HashMap.Entry<Long, Link> linkEntry : this.links.entrySet()) {
                if ((linkEntry.getValue().getFirstNodeId() == nodeEntry.getKey()) || (linkEntry.getValue().
                        getSecondNodeId() == nodeEntry.getKey())) {
                    nodeEntry.getValue().getLinkIdList().add(linkEntry.getKey());
                }
            }
        }
        System.out.println("Links associated with respective nodes");
    }

    // Calculate equi-rectangular lengths of all links
    public void calculateLinkLengths() {
        for (Link link : this.links.values()) {
            Node firstNode = this.nodes.get(link.getFirstNodeId());
            Node secondNode = this.nodes.get(link.getSecondNodeId());

            double firstNodeLatitudeRadians = Math.toRadians(firstNode.getNodeLatitude());
            double firstNodeLongitudeRadians = Math.toRadians(firstNode.getNodeLongitude());
            double secondNodeLatitudeRadians = Math.toRadians(secondNode.getNodeLatitude());
            double secondNodeLongitudeRadians = Math.toRadians(secondNode.getNodeLongitude());

            double latitudeDifference = secondNodeLatitudeRadians - firstNodeLatitudeRadians;
            double longitudeDifference = secondNodeLongitudeRadians - firstNodeLongitudeRadians;
            double x = longitudeDifference * Math.cos((firstNodeLatitudeRadians + secondNodeLatitudeRadians) / 2);
            double y = latitudeDifference;

            double linkLengthM = Math.sqrt(x * x + y * y) * EARTH_RADIUS_M;
            link.setLinkLengthM(linkLengthM);
        }
        System.out.println("Link lengths calculated");
    }

    // Contract nodes and build shortcuts
    public void contractNodesAndBuildShortcuts() {
        boolean nodesWithTwoLinksExist = true;

        while (nodesWithTwoLinksExist) {
            ArrayList<HashMap.Entry<Long, Node>> nodeEntryList = new ArrayList<>(this.nodes.entrySet());
            for (HashMap.Entry<Long, Node> nodeEntry : nodeEntryList) {
                if (nodeEntry.getValue().getLinkIdList().size() == 2) {
                    long commonNodeId = nodeEntry.getKey();
                    Node commonNode = nodeEntry.getValue();

                    ArrayList<Long> nodesConsideredForContraction = new ArrayList<>();
                    for (long associatedLinkId : commonNode.getLinkIdList()) {
                        nodesConsideredForContraction.add(this.links.get(associatedLinkId).getFirstNodeId());
                        nodesConsideredForContraction.add(this.links.get(associatedLinkId).getSecondNodeId());
                    }
                    nodesConsideredForContraction.removeIf(nodeId -> nodeId.equals(commonNodeId));
                    long firstUncommonNodeId = nodesConsideredForContraction.get(0);
                    long secondUncommonNodeId = nodesConsideredForContraction.get(1);
                    Node firstUncommonNode = this.nodes.get(firstUncommonNodeId);
                    Node secondUncommonNode = this.nodes.get(secondUncommonNodeId);

                    long firstLinkId = commonNode.getLinkIdList().get(0);
                    long secondLinkId = commonNode.getLinkIdList().get(1);

                    Link shortcutLink = new Link(firstUncommonNodeId, secondUncommonNodeId, this.links.get(firstLinkId).
                            getLinkType());
                    shortcutLink.setLinkLengthM(this.links.get(firstLinkId).getLinkLengthM() + this.links.get(secondLinkId).
                            getLinkLengthM());

                    long shortcutLinkId = Long.parseLong(firstLinkId + "01");

                    firstUncommonNode.getLinkIdList().remove(firstLinkId);
                    firstUncommonNode.getLinkIdList().remove(secondLinkId);
                    secondUncommonNode.getLinkIdList().remove(firstLinkId);
                    secondUncommonNode.getLinkIdList().remove(secondLinkId);
                    firstUncommonNode.getLinkIdList().add(shortcutLinkId);
                    secondUncommonNode.getLinkIdList().add(shortcutLinkId);

                    this.links.remove(firstLinkId);
                    this.links.remove(secondLinkId);
                    this.links.put(shortcutLinkId, shortcutLink);

                    this.nodes.remove(commonNodeId);

                    nodesWithTwoLinksExist = false;
                    for (Node node : this.nodes.values()) {
                        if (node.getLinkIdList().size() == 2) {
                            nodesWithTwoLinksExist = true;
                            break;
                        }
                    }
                }
            }
        }
        System.out.println("Nodes contracted and shortcuts built");
    }

    // Index finder using alphabets in the first record of an OSM OPL extract
    private int findIndexInArray(String characterSequenceToFind, @NotNull String[] headerArray) {
        int columnPosition = -1;
        for (int i = 0; i <= headerArray.length; i++) {
            if (headerArray[i].contains(characterSequenceToFind)) {
                columnPosition = i;
            }
        }
        return columnPosition;
    }
}