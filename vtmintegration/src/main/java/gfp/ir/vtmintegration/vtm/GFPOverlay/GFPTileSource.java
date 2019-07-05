/*
 * Copyright (c) 2018. GFP licenced. www.gfpishro.ir (majid.hojati@ut.ac.ir)
 */

package gfp.ir.vtmintegration.vtm.GFPOverlay;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.oscim.tiling.ITileDataSource;
import org.oscim.tiling.TileSource;

import java.io.File;
import java.io.IOException;

import gfp.ir.vtmintegration.spatilite_core.core.databasehandlers.MbtilesDatabaseHandler;
import jsqlite.Exception;

public class GFPTileSource extends TileSource {
    Bitmap tile;
    OpenResult result;
    File image;
    public GFPTileSource(String bitmappath){
         image = new File(bitmappath);
        if(image.exists()){
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            tile = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
            result= OpenResult.SUCCESS;
        }else{
            result=   null;
        }
    }

    @Override
    public ITileDataSource getDataSource() {
        return new GFPTileDataSource(tile);
    }

    @Override
    public OpenResult open() {

        return result;
    }

    @Override
    public void close() {

    }
}
