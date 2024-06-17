package src.RoadTransportRouter.OSMDataManager;
// OSM: OpenStreetMap
// OPL: Object Per Line

import org.jetbrains.annotations.NotNull;
import src.PublicTransportRouter.GTFSDataManager.Route;
import src.PublicTransportRouter.GTFSDataManager.RouteStop;
import src.PublicTransportRouter.GTFSDataManager.Trip;

import java.io.*;
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

    private static final int EARTH_RADIUS_KM = 6371;
    private static final double AVERAGE_DRIVING_SPEED_KMPH = 29;
    // (Source: https://www.tomtom.com/traffic-index/munich-traffic/)
    private static final int MINUTES_PER_HOUR = 60;

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
            // First object is a node
            int linkAttributesIndex = findIndexInArray("T", firstOsmOplRecordArray);
            int linkNodesIndex = linkAttributesIndex + 1;

            // Create link types' hashset for filtering way records pertaining to the road network
            HashSet<String> linkTypeHashSet = new HashSet<>(Arrays.asList(LINK_TYPE_ARRAY));

            // Read body and process data for all links in the road network
            long wayId = 0;
            while ((newline = osmOplExtractReader.readLine()) != null) {
                wayId++;
                if (newline.substring(0, 1).equalsIgnoreCase("w")) {
                    String[] linkDataRecord = newline.split(" ");
                    String linkAttributes = linkDataRecord[linkAttributesIndex];

                    if (linkAttributes.contains("highway")) {
                        String[] linkAttributesRecord = linkAttributes.split(",");
                        int linkTypeIndex = findIndexInArray("highway", linkAttributesRecord);
                        /* Example of a link's record of details: Thighway=track,maxspeed:type=DE:rural,surface=asphalt,
                        tracktype=grade1 Nn1755165066,n1755165067,n262608882
                        */

                        String linkType = linkAttributesRecord[linkTypeIndex].substring((linkTypeIndex == 0) ? 9 : 8);
                        if (linkTypeHashSet.contains(linkType)) {
                            String[] nodesArray = linkDataRecord[linkNodesIndex].split(",");

                            for (int i = 0; i <= nodesArray.length - 2; i++) {
                                // "first" and "second" nodes do not signify any ordering of nodes/ links
                                long firstNodeId;
                                firstNodeId = Long.parseLong(nodesArray[i].substring((i == 0) ? 2 : 1));
                                long secondNodeId;
                                secondNodeId = Long.parseLong(nodesArray[i + 1].substring(1));
                                long linkId = Long.parseLong(wayId + "0" + (i + 1) + "00");
                                Link link = new Link(firstNodeId, secondNodeId, linkType);

                                this.links.put(linkId, link);
                                this.nodes.put(firstNodeId, new Node());
                                this.nodes.put(secondNodeId, new Node());
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
                if (newline.substring(0, 1).equalsIgnoreCase("n")) {
                    String[] nodeDataRecord = newline.split(" ");
                    // Example of a node's data record: n127290 v0 dV c0 t i0 u T x11.3246338 y48.2164498
                    long nodeId = Long.parseLong(nodeDataRecord[nodeIdIndex].substring(1));

                    if (this.nodes.containsKey(nodeId)) {
                        double nodeLongitude = Double.parseDouble(nodeDataRecord[longitudeIndex].
                                substring(1));
                        double nodeLatitude = Double.parseDouble(nodeDataRecord[latitudeIndex].
                                substring(1));
                        this.nodes.get(nodeId).setNodeLongitude(nodeLongitude);
                        this.nodes.get(nodeId).setNodeLatitude(nodeLatitude);
                    }
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

    // Calculate travel times for all links based on equi-rectangular lengths
    public void calculateLinkTravelTimesMin() {
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

            double linkLengthKm = Math.sqrt(x * x + y * y) * EARTH_RADIUS_KM;
            link.setLinkTravelTimeMin(linkLengthKm / AVERAGE_DRIVING_SPEED_KMPH * MINUTES_PER_HOUR);
        }
        System.out.println("Link-wise travel times (in minutes) calculated");
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
                    long firstNoncommonNodeId = nodesConsideredForContraction.get(0);
                    long secondNoncommonNodeId = nodesConsideredForContraction.get(1);
                    Node firstUncommonNode = this.nodes.get(firstNoncommonNodeId);
                    Node secondUncommonNode = this.nodes.get(secondNoncommonNodeId);

                    long firstLinkId = commonNode.getLinkIdList().get(0);
                    long secondLinkId = commonNode.getLinkIdList().get(1);

                    Link shortcutLink = new Link(firstNoncommonNodeId, secondNoncommonNodeId, this.links.
                            get(firstLinkId).getLinkType());
                    shortcutLink.setLinkTravelTimeMin(this.links.get(firstLinkId).getLinkTravelTimeMin() + this.links.
                            get(secondLinkId).getLinkTravelTimeMin());
                    long shortcutLinkId = firstLinkId + 1;

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

                    // Check to see if nodes with two associated links still exist in the graph
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

    /**
     * All writers below are for datasets pertinent to the Dijkstra algorithm, aligned with Dijkstra terminologies
     */

    // Write a "dijkstraLinks.txt" file
    public void writeDijkstraLinks(String dijkstraLinksFilePath) {
        try {
            // Writer for "dijkstraLinks.txt"
            BufferedWriter dijkstraLinksWriter = new BufferedWriter(new FileWriter(dijkstraLinksFilePath));

            // Set up header array
            dijkstraLinksWriter.write("link_id,link_type,first_node_id,second_node_id,link_travel_time_min\n");

            // Write body based on "links" hashmap
            for (HashMap.Entry<Long, Link> linkEntry : this.links.entrySet()) {
                long linkId = linkEntry.getKey();
                String linkType = linkEntry.getValue().getLinkType();
                long firstNodeId = linkEntry.getValue().getFirstNodeId();
                long secondNodeId = linkEntry.getValue().getSecondNodeId();
                double linkTravelTimeMin = linkEntry.getValue().getLinkTravelTimeMin();

                dijkstraLinksWriter.write(linkId + "," + linkType + "," + firstNodeId + "," + secondNodeId + "," +
                        linkTravelTimeMin + "\n");
            }
            System.out.println("Links' data written to " + dijkstraLinksFilePath);

        } catch (IOException iOE) {
            System.out.println("Input-output exception. Please check the \"links\" hashmap.");
        }
    }

    // Write a "dijkstraNodes.txt" file
    public void writeDijkstraNodes(String dijkstraNodesFilePath) {
        try {
            // Writer for "dijkstraNodes.txt"
            BufferedWriter dijkstraNodesWriter = new BufferedWriter(new FileWriter(dijkstraNodesFilePath));

            // Set up header array
            dijkstraNodesWriter.write("node_id,node_longitude,node_latitude,associated_link_id\n");

            // Write body based on "nodes" hashmap
            for (HashMap.Entry<Long, Node> nodeEntry : this.nodes.entrySet()) {
                long nodeId = nodeEntry.getKey();
                double nodeLongitude = nodeEntry.getValue().getNodeLongitude();
                double nodeLatitude = nodeEntry.getValue().getNodeLatitude();
                for (long associatedLinkId : nodeEntry.getValue().getLinkIdList()) {
                    dijkstraNodesWriter.write(nodeId + "," + nodeLongitude + "," + nodeLatitude + "," +
                            associatedLinkId + "\n");
                }
            }
            System.out.println("Nodes' data written to " + dijkstraNodesFilePath);

        } catch (IOException iOE) {
            System.out.println("Input-output exception. Please check the \"nodes\" hashmap.");
        }
    }

    /**
     * All supporting methods are below
     */

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

    // Getters of road network data for Dijkstra queries
    public LinkedHashMap<Long, Link> getLinks() {
        return this.links;
    }

    public LinkedHashMap<Long, Node> getNodes() {
        return this.nodes;
    }
}