package src.PublicTransportRouter.RoutingAlgorithm;

public class TransitQueryResponse {
    private double earliestArrivalTimeMinutes;
    private double travelTimeMinutes;

    TransitQueryResponse(double earliestArrivalTimeMinutes, double travelTimeMinutes) {
        this.earliestArrivalTimeMinutes = earliestArrivalTimeMinutes;
        this.travelTimeMinutes = travelTimeMinutes;
    }

    public double getEarliestArrivalTimeMinutes() {
        return this.earliestArrivalTimeMinutes;
    }

    public double getTravelTimeMinutes() {
        return this.travelTimeMinutes;
    }
}