/*
 * Copyright (c) 2018. GFP licenced. www.gfpishro.ir (majid.hojati@ut.ac.ir)
 */

package gfp.ir.vtmintegration.vtm.Mbtile;

import org.oscim.tiling.ITileDataSource;
import org.oscim.tiling.TileSource;

import java.io.IOException;

import gfp.ir.vtmintegration.spatilite_core.core.databasehandlers.MbtilesDatabaseHandler;
import jsqlite.Exception;

public class MbtileTileSource extends TileSource {
    MbtilesDatabaseHandler mbtile;

    public MbtileTileSource(String dbpath){
        try {
            mbtile=new MbtilesDatabaseHandler(dbpath,null);
            mbtile.open();
            mbtile.loadMetadata();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String getName(){
       return mbtile.getName();
    }
    @Override
    public ITileDataSource getDataSource() {
        return new MbtileTileDataSource(mbtile);
    }

    @Override
    public OpenResult open() {

        if(mbtile!=null&&mbtile.isValid()&&mbtile.isOpen()){
            return  OpenResult.SUCCESS;
        }
        else{
            return null;
        }
    }

    @Override
    public void close() {
        try {
            if(mbtile.isOpen())
                return;
                mbtile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
