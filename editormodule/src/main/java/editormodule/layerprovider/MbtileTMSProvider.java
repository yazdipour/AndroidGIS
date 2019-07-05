package editormodule.layerprovider;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.nextgis.maplib.datasource.TileItem;
import com.nextgis.maplib.map.TMSLayer;
import com.nextgis.maplib.util.GeoConstants;
import com.nextgis.maplibui.api.ILayerUI;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import gfp.ir.vtmintegration.spatilite_core.core.databasehandlers.MbtilesDatabaseHandler;

public class MbtileTMSProvider extends TMSLayer implements ILayerUI {
    MbtilesDatabaseHandler mbtile;
    private static final String TAG = "MbtileTMSProvider";

    public MbtileTMSProvider(Context context, File path,String databasepath) {
        super(context, path);
        File file=new File(databasepath);
        if(!file.exists()){
            Log.e(TAG, "Provided Path does not exist.  " + databasepath);
            return;
        }
        try {
            mbtile=new MbtilesDatabaseHandler(file.getAbsolutePath(),null);
            this.setName(mbtile.getName());
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                this.setMaxZoom(Float.valueOf(mbtile.getMaxZoom()));
                this.setMinZoom(Float.valueOf(mbtile.getMinZoom()));
            }else{
                Log.e(TAG, "Well you dont have KITKAT so we set min and max zoom to constants 30 and 0  ");
//                this.setMaxZoom(19);
                this.setMaxZoom(30);
                this.setMinZoom(GeoConstants.DEFAULT_MIN_ZOOM);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error opening MbtileDatabasepath  " + e.getMessage());

        }
    }


    @Override
    public Drawable getIcon(Context context)
    {
        return ContextCompat.getDrawable(mContext, com.nextgis.maplibui.R.drawable.ic_raster);
    }

    @Override
    public void changeProperties(Context context) {
        return;
    }

    @Override
    public Bitmap getBitmap(TileItem tile) {
        if(mbtile!=null & mbtile.isValid() && mbtile.isOpen()){
            try {
                ByteArrayInputStream bs=  mbtile.getBitmapTile(tile.getX(),tile.getY(),tile.getZoomLevel(),256,false);
                Bitmap bp=BitmapFactory.decodeStream(bs); //decode stream to a bitmap image
                return bp;
            }catch (Exception ex){
                Log.e(TAG, "Error happend while geting Mbtile data from databse  " + ex.getMessage());
                return null;
            }
        }else{
            Log.e(TAG, "Database File is not valid or is not open or even it is null. is your path valid? " );
            return null;
        }
    }
}
