/*
 * Copyright (c) 2018. GFP licenced. www.gfpishro.ir (majid.hojati@ut.ac.ir)
 */

package gfp.ir.vtmintegration.vtm.Mbtile;

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

import gfp.ir.vtmintegration.spatilite_core.core.databasehandlers.MbtilesDatabaseHandler;

import static org.oscim.tiling.QueryResult.SUCCESS;

public class MbtileTileDataSource implements ITileDataSource {

    static final Logger log = LoggerFactory.getLogger(MbtileTileDataSource.class);
    protected final ITileDecoder mTileDecoder;
    protected final MbtilesDatabaseHandler mTileSource;

    public MbtileTileDataSource(MbtilesDatabaseHandler mbtile) {
        mTileDecoder = new BitmapTileDecoder();
        mTileSource = mbtile;
        mTileSource.getDatabasePath();

    }
    @Override
    public void query(MapTile tile, ITileDataSink mapDataSink) {

        ByteArrayInputStream bs=  mTileSource.getBitmapTile(tile.tileX,tile.tileY,tile.zoomLevel,256);

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
