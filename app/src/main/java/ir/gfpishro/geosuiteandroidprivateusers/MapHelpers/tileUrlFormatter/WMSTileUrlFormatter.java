package ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.tileUrlFormatter;

import android.util.Log;

import org.oscim.core.BoundingBox;
import org.oscim.core.Tile;
import org.oscim.tiling.source.UrlTileSource;

/**
 * Url Sample
 * http://207.182.149.126:8080/geoserver/topp/wms?service=WMS&version=1.1.0&format=image%2Fpng
 *      &request=GetMap&layers=topp:states&styles=&width=256&height=256&srs=EPSG:4326
 *      &bbox={minx},{miny},{maxx},{maxy}
 */
public class WMSTileUrlFormatter implements UrlTileSource.TileUrlFormatter {

    @Override
    public String formatTilePath(UrlTileSource tileSource, Tile tile) {
        BoundingBox box = tile.getBoundingBox();
        double[] TileBounds = new double[]{box.getMinLongitude(), box.getMinLatitude(), box.getMaxLongitude(), box.getMaxLatitude()};
//        int[] tmsTileXY = googleTile2TmsTile(tile.tileX, tile.tileY, tile.zoomLevel);
//        double[] TileBounds = new GlobalMercator().TileBounds(tile.tileX, tmsTileXY[1], tile.zoomLevel);
        StringBuilder sb = new StringBuilder();
        for (String b : tileSource.getTilePath()) {
            if (b.length() == 1) {
                switch (b.charAt(0)) {
                    case '1':
                        sb.append(TileBounds[0]);
                        continue;
                    case '2':
                        sb.append(TileBounds[1]);
                        continue;
                    case '3':
                        sb.append(TileBounds[2]);
                        continue;
                    case '4':
                        sb.append(TileBounds[3]);
                        continue;
                    default:
                        break;
                }
            }
            sb.append(b);
        }
        Log.d(this.getClass().getName(), tileSource.getUrl() + sb.toString());
        return sb.toString();
    }
}
