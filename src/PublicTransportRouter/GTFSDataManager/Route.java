/**
 * Author: Anirudh Ray
 * Institution: Professorship of Traffic Engineering and Control, Technical University of Munich
 * Department: Mobility Systems Engineering, School of Engineering and Design
 * E-mail Address: Anirudh.Ray@tum.de
 * Purpose: Component of a Java-based multi-modal routing algorithm, built using RAPTOR, Dijkstra-algorithm, and
 * KD-Trees
 */

package src.PublicTransportRouter.GTFSDataManager;

public class Route {    // Route IDs are present in the pertinent hashmap
    private int numberTrips;
    private int numberStops;
    // Maximum number of stops on the route, from amongst all trips operating on the route
    private int routeType;
    // Refer to: https://gtfs.org/de/schedule/reference/#routestxt

    void setNumberTrips(int numberTrips) {
        this.numberTrips = numberTrips;
    }
    void setNumberStops(int numberStops) {
        this.numberStops = numberStops;
    }
    void setRouteType(int routeType) {
        this.routeType = routeType;
    }
    int getNumberTrips() {
        return this.numberTrips;
    }
    int getNumberStops() {
        return this.numberStops;
    }
    int getRouteType() {
        return this.routeType;
    }
}