package ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.utils;

import org.oscim.core.GeoPoint;

public class PointConverter {
    /**
     * Converts Google tile coordinates to TMS Tile coordinates.
     * <p/>
     * <p>Code copied from: http://code.google.com/p/gmap-tile-generator/</p>
     *
     * @param tx   the x tile number.
     * @param ty   the y tile number. [osm notation]
     * @param zoom the current zoom level.
     * @return the converted values.
     */
    public static int[] googleTile2TmsTile(int tx, int ty, int zoom) {
        return new int[]{tx, (int) ((Math.pow(2, zoom) - 1) - ty)};
    }

    public static UTM WGS84_To_UTM(GeoPoint p) {
        return new UTM(new WGS84(p.getLatitude(), p.getLongitude()));
    }

    public static WGS84 UTM_TO_WGS84(double x, double y) {
        return new WGS84(new UTM(39, 'S', x, y));
    }
}
