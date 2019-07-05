package ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.tileUrlFormatter;

import android.util.Log;

import org.oscim.core.Tile;
import org.oscim.tiling.source.UrlTileSource;

import ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.utils.PointConverter;

/**
 * Url Sample
 * url/{Z}/{X}/{Y}.png
 */
public class MBTileUrlFormatter implements UrlTileSource.TileUrlFormatter {
    @Override
    public String formatTilePath(UrlTileSource tileSource, Tile tile) {
        int[] tmsTileXY = PointConverter.googleTile2TmsTile(tile.tileX, tile.tileY, tile.zoomLevel);
        StringBuilder sb = new StringBuilder();
        for (String b : tileSource.getTilePath()) {
            if (b.length() != 1) continue;
            switch (b.charAt(0)) {
                case 'X':
                    sb.append(tileSource.tileXToUrlX(tile.tileX));
                    continue;
                case 'Y':
                    sb.append(tileSource.tileYToUrlY(tmsTileXY[1]));
                    continue;
                case 'Z':
                    sb.append(tileSource.tileZToUrlZ(tile.zoomLevel));
                    continue;
            }
            sb.append(b);
        }
        Log.d("Originalformatter", tile.zoomLevel + "/" + tile.tileX + "/" + tile.tileY);
        Log.d("TMSTILEformatter", tile.zoomLevel + "/" + tile.tileX + "/" + tmsTileXY[1]);
        Log.d(getClass().getName(), tileSource.getUrl() + sb.toString());
        return sb.toString() + ".png";
    }
}
