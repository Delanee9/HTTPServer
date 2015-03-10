/**
 * Class to hold location information.
 */
public class Point {
    private final Double latitude;
    private final Double longitude;

    /**
     * Constructor
     *
     * @param location String
     */
    public Point(String location) {
        String[] longLat = location.split(",");
        this.latitude = (Double.valueOf(longLat[0])) / 180.0 * Math.PI;
        this.longitude = (Double.valueOf(longLat[1])) / 180.0 * Math.PI;
    }

    /**
     * Returns the latitude
     *
     * @return Double
     */
    public Double getLatitude() {
        return latitude;
    }

    /**
     * Returns the longitude
     *
     * @return Double
     */
    public Double getLongitude() {
        return longitude;
    }
}