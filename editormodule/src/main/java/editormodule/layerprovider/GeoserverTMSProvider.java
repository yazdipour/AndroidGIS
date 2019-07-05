package editormodule.layerprovider;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.nextgis.maplib.datasource.TileItem;
import com.nextgis.maplib.map.RemoteTMSLayer;
import com.nextgis.maplib.util.Constants;
import com.nextgis.maplib.util.FileUtil;
import com.nextgis.maplibui.api.ILayerUI;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;

import static com.nextgis.maplib.util.Constants.TAG;

public class GeoserverTMSProvider extends RemoteTMSLayer implements ILayerUI {
    public GeoserverTMSProvider(Context context, File path) {
        super(context, path);
    }
    @Override
    public boolean downloadTile(TileItem tile, boolean tileExists)
    {
        if (null == tile) {
            return false;
        }

        // Try to get tile from local cache.
        File tilePath = new File(mPath, tile.toString("{z}/{x}/{y}" + TILE_EXT));
        boolean exist = tilePath.exists();
        if (exist && (System.currentTimeMillis() - tilePath.lastModified() < mTileMaxAge)) {
            return true;
        }

        if (!mNet.isNetworkAvailable()) {
            return exist;
        }

        if (Constants.DEBUG_MODE && exist) {
            Log.d(Constants.TAG, "Update old tile " + tile.toString() + " tile date:" + tilePath
                    .lastModified() + " current date:" + System.currentTimeMillis());
        }

        // Try to get tile from remote.

        TileItem newTMStile=convertTileToTMS(tile);
        String url = newTMStile.toString(getURLSubdomain());
        if (Constants.DEBUG_MODE) {
            Log.d(TAG, "url: " + url);
        }

        try {
//            if (false) { //!mAvailable.tryAcquire(DELAY, TimeUnit.MILLISECONDS)) {
//                if (!tileExists) {
//                    mLastCheckTime = System.currentTimeMillis();
//                }
//                //if (Constants.DEBUG_MODE) {
//                //    Log.d(TAG, "downloadTile() return: " + exist + ", p4, " + tile.toString());
//                //}
//                return exist;
//            }
//            if (Constants.DEBUG_MODE) {
//                Log.d(TAG, "Semaphore left: " + mAvailable.availablePermits());
//            }

            try {
                getTileFromStream(url, tilePath);
            } catch (InterruptedIOException e) {
                Log.d(TAG, "Thread interrupted, delete the tile file for the url: " + url);
                FileUtil.deleteRecursive(tilePath);
                return false;
            }

//            mAvailable.release();
            return true;

        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            Log.d(
                    TAG,
                    "Problem downloading MapTile: " + url + " Error: " + e.getLocalizedMessage());
        }
//        catch (InterruptedException e) {
//            e.printStackTrace();
//            Log.d(
//                    TAG,
//                    "Problem downloading MapTile, delete the tile file, url: " + url + " Error: "
//                            + e.getLocalizedMessage());
//            FileUtil.deleteRecursive(tilePath);
//            // Preserve interrupt status
//            Thread.currentThread().interrupt();
//            return false;
//        }

        return exist;
    }

    private TileItem convertTileToTMS(TileItem tile) {
        int newy= Integer.valueOf((int) ((Math.pow(2, tile.getZoomLevel()) - 1) - tile.getY()));
        TileItem newtile= new TileItem(tile.getX(),newy,tile.getZoomLevel(),tile.getEnvelope());
        return newtile;
    }

    @Override
    public Drawable getIcon(Context context) {
        return ContextCompat.getDrawable(mContext, com.nextgis.maplibui.R.drawable.ic_raster);
    }

    @Override
    public void changeProperties(Context context) {
            return;
    }
}
