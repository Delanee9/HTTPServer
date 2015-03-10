/**
 * Class to calculate the distance between two points.
 */
public class Proximity {
    private final Point pointA;
    private final Point pointB;
    private final Double distance = 550.0;

    /**
     * Constructor
     *
     * @param pointA Point
     * @param pointB Point
     */
    public Proximity(Point pointA, Point pointB) {
        this.pointA = pointA;
        this.pointB = pointB;
    }

    /**
     * Returns if two points are close together
     *
     * @return boolean
     */
    public boolean inCloseProximity() {
        Double result = Math.acos(Math.cos(pointA.getLatitude()) * Math.cos(pointB.getLatitude()) * Math.cos(pointB.getLongitude() - pointA.getLongitude()) + Math.sin(pointA.getLatitude() * Math.sin(pointB.getLatitude())));
        return result <= distance;
    }
}