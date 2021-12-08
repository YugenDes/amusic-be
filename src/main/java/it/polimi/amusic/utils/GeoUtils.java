package it.polimi.amusic.utils;

import com.google.cloud.firestore.GeoPoint;

import java.util.Arrays;
import java.util.List;

/**
 * Classe di Utility per lavorare con la classe google GeoPoint
 */
public class GeoUtils {

    public static final int RADIUS_EARTH = 6371;
    private static final double MIN_LATITUDE = Math.toRadians(-90d);  // -PI/2
    private static final double MAX_LATITUDE = Math.toRadians(90d);   //  PI/2
    private static final double MIN_LONGITUDE = Math.toRadians(-180d); // -PI
    private static final double MAX_LONGITUDE = Math.toRadians(180d);  //  PI

    private GeoUtils() {

    }

    /**
     * Calcola la distanza tra due punti in latitudine e longitude con la formula Haversine
     *
     * @param geoPointStart Punto di partenza
     * @param geoPointEnd   Punto di destinazione
     * @return Distance in km
     */
    public static double distance(GeoPoint geoPointStart, GeoPoint geoPointEnd) {
        double latDistance = Math.toRadians(geoPointEnd.getLatitude() - geoPointStart.getLatitude());
        double lonDistance = Math.toRadians(geoPointEnd.getLongitude() - geoPointStart.getLongitude());
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(geoPointStart.getLatitude())) * Math.cos(Math.toRadians(geoPointEnd.getLatitude()))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return RADIUS_EARTH * c;
    }


    /**
     * Controlla se la latitudine e longitudine in radianti sono entro i limiti
     * latitudine e longitudine non possono essere 90/180 o -90/-180
     *
     * @param latitudeRadians  latitudeRadians
     * @param longitudeRadians longitudeRadians
     */
    public static void checkBounds(double latitudeRadians, double longitudeRadians) {
        if (latitudeRadians < MIN_LATITUDE || latitudeRadians > MAX_LATITUDE ||
                longitudeRadians < MIN_LONGITUDE || longitudeRadians > MAX_LONGITUDE)
            throw new IllegalArgumentException();
    }


    /**
     * @param latitudeRadians  latitudine in radianti.
     * @param longitudeRadians longitudine in radianti.
     */
    public static GeoPoint getGeoPointFromRadians(double latitudeRadians, double longitudeRadians) {
        //Controllo se i valori sono entro i limiti
        checkBounds(latitudeRadians, longitudeRadians);
        //Converto i valori da radianti a gradi
        final double latitudeDegrees = Math.toDegrees(latitudeRadians);
        final double longitudeDegrees = Math.toDegrees(longitudeRadians);
        //Creo il GeoPoint
        return new GeoPoint(latitudeDegrees, longitudeDegrees);
    }


    /**
     * <p>Computes the bounding coordinates of all points on the surface
     * of a sphere that have a great circle distance to the point represented
     * by this GeoLocation instance that is less or equal to the distance
     * argument.</p>
     * <p>For more information about the formulae used in this method visit
     * <a href="http://JanMatuschek.de/LatitudeLongitudeBoundingCoordinates">
     * http://JanMatuschek.de/LatitudeLongitudeBoundingCoordinates</a>.</p>
     *
     * @param distance the distance from the point represented by this
     *                 GeoLocation instance. Must be measured in the same unit as the radius
     *                 argument.
     * @return an array of two GeoLocation objects such that:<ul>
     * <li>The latitude of any point within the specified distance is greater
     * or equal to the latitude of the first array element and smaller or
     * equal to the latitude of the second array element.</li>
     * <li>If the longitude of the first array element is smaller or equal to
     * the longitude of the second element, then
     * the longitude of any point within the specified distance is greater
     * or equal to the longitude of the first array element and smaller or
     * equal to the longitude of the second array element.</li>
     * <li>If the longitude of the first array element is greater than the
     * longitude of the second element (this is the case if the 180th
     * meridian is within the distance), then
     * the longitude of any point within the specified distance is greater
     * or equal to the longitude of the first array element
     * <strong>or</strong> smaller or equal to the longitude of the second
     * array element.</li>
     * </ul>
     */
    public static List<GeoPoint> boundingGeoPoints(GeoPoint location, double distance) {
        if (distance < 0d) throw new IllegalArgumentException();

        double radDist = distance / RADIUS_EARTH;

        double radLat = Math.toRadians(location.getLatitude());
        double radLon = Math.toRadians(location.getLongitude());


        double minLatitude = radLat - radDist;
        double maxLatitude = radLat + radDist;

        double minLongitude;
        double maxLongitude;
        if (minLatitude > MIN_LATITUDE && maxLatitude < MAX_LATITUDE) {
            double deltaLon = Math.asin(Math.sin(radDist) /
                    Math.cos(radLat));
            minLongitude = radLon - deltaLon;
            if (minLongitude < MIN_LONGITUDE) minLongitude += 2d * Math.PI;
            maxLongitude = radLon + deltaLon;
            if (maxLongitude > MAX_LONGITUDE) maxLongitude -= 2d * Math.PI;
        } else {
            // a pole is within the distance
            minLatitude = Math.max(minLatitude, MIN_LATITUDE);
            maxLatitude = Math.min(maxLatitude, MAX_LATITUDE);
            minLongitude = MIN_LONGITUDE;
            maxLongitude = MAX_LONGITUDE;
        }

        //Creo un array contenente i due geoPoint formando il rettangolo dell area
        return Arrays.asList(getGeoPointFromRadians(minLatitude, minLongitude), getGeoPointFromRadians(maxLatitude, maxLongitude));

    }
}
