package src.PublicTransportRouter.RoutingAlgorithm;

public class TransitQueryResponse {
    private int earliestArrivalTimeMinutes;
    private int travelTimeMinutes;

    TransitQueryResponse(int earliestArrivalTimeMinutes, int travelTimeMinutes) {
        this.earliestArrivalTimeMinutes = earliestArrivalTimeMinutes;
        this.travelTimeMinutes = travelTimeMinutes;
    }

    public int getEarliestArrivalTimeMinutes() {
        return this.earliestArrivalTimeMinutes;
    }

    public int getTravelTimeMinutes() {
        return this.travelTimeMinutes;
    }
}