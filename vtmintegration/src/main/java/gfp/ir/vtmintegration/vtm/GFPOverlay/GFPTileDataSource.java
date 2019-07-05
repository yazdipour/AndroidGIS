/*
 * Copyright (c) 2018. GFP licenced. www.gfpishro.ir (majid.hojati@ut.ac.ir)
 */

package gfp.ir.vtmintegration.vtm.GFPOverlay;


import org.oscim.backend.CanvasAdapter;
import org.oscim.backend.canvas.Bitmap;
import org.oscim.core.Tile;
import org.oscim.layers.tile.MapTile;
import org.oscim.tiling.ITileDataSink;
import org.oscim.tiling.ITileDataSource;
import org.oscim.tiling.source.ITileDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;


import gfp.ir.vtmintegration.spatilite_core.core.databasehandlers.MbtilesDatabaseHandler;

import static org.oscim.tiling.QueryResult.SUCCESS;

public class GFPTileDataSource implements ITileDataSource {

    static final Logger log = LoggerFactory.getLogger(GFPTileDataSource.class);
    protected  ITileDecoder mTileDecoder;
    protected  android.graphics.Bitmap mTileSource;
    protected  ByteArrayInputStream bs;
    public GFPTileDataSource(android.graphics.Bitmap bitmap) {

        if(bitmap==null)
            return;


        mTileDecoder = new BitmapTileDecoder();

        mTileSource = bitmap;


        int byteSize = bitmap.getRowBytes() * bitmap.getHeight();
        ByteBuffer byteBuffer = ByteBuffer.allocate(byteSize);
        bitmap.copyPixelsToBuffer(byteBuffer);

// Get the byteArray.
        byte[] byteArray = byteBuffer.array();

// Get the ByteArrayInputStream.
        bs = new ByteArrayInputStream(byteArray);


    }
    @Override
    public void query(MapTile tile, ITileDataSink mapDataSink) {

        if(bs==null)
            return;

        try {
            if (mTileDecoder.decode(tile, mapDataSink,bs)) {
                mapDataSink.completed(SUCCESS);
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public class BitmapTileDecoder implements ITileDecoder {

        @Override
        public boolean decode(Tile tile, ITileDataSink sink, InputStream is)
                throws IOException {


            Bitmap bitmap = CanvasAdapter.decodeBitmap(is);
            if (!bitmap.isValid()) {
                log.debug("{} invalid bitmap", tile);
                return false;
            }
            sink.setTileImage(bitmap);

            return true;
        }
    }
    @Override
    public void dispose() {

    }

    @Override
    public void cancel() {

    }
}
