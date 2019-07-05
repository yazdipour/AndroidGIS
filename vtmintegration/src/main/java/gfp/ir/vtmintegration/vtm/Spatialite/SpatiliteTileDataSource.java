/*
 * Copyright (c) 2018. GFP licenced. www.gfpishro.ir (majid.hojati@ut.ac.ir)
 */

package gfp.ir.vtmintegration.vtm.Spatialite;

import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.oscim.core.BoundingBox;
import org.oscim.core.MapElement;
import org.oscim.core.Tag;
import org.oscim.layers.tile.MapTile;
import org.oscim.layers.vector.JtsConverter;
import org.oscim.tiling.ITileDataSink;
import org.oscim.tiling.ITileDataSource;

import java.util.ArrayList;
import java.util.List;

import gfp.ir.vtmintegration.geolibrary.util.types.EDataType;
import gfp.ir.vtmintegration.spatilite_core.core.databasehandlers.SpatialiteDatabaseHandler;
import gfp.ir.vtmintegration.spatilite_core.core.geometry.GeometryIterator;
import gfp.ir.vtmintegration.spatilite_core.core.tables.SpatialVectorTable;

import static org.oscim.tiling.QueryResult.SUCCESS;
import static org.oscim.tiling.QueryResult.TILE_NOT_FOUND;

public class SpatiliteTileDataSource implements ITileDataSource {

    private final SpatiliteTileSource mTileSource;
    private final boolean mUseCache;
    private SpatialVectorTable vectorTable;
    private SpatialiteDatabaseHandler spatialiteDatabaseHandler;
    private BoundingBox boundingBox;
    private boolean isCanceled = false;
    private JtsConverter jtsConverter;
    private EDataType fieldtype;
    private final MapElement mMapElement;

    //    final TileClipper mClipper = new TileClipper(-256, -256, 256, 256);
//    private final MapElement mMapElement;
//    GlobalMercator gmerc;

    //    GeoJsonTileDecoder mTileDecoder;

    //    public SpatiliteTileDataSource(SpatialiteDatabaseHandler splite_, SpatialVectorTable vectortable) {
//
//        vectorTable = vectortable;
//        spatialiteDatabaseHandler=splite_;
////        mTileDecoder=tileDecoder;
//        mTagMap = new LinkedHashMap<String, Object>();
//        mMapElement = new MapElement();
//
//
//        mMapElement.layer = 5;
//
//         fieldtype=vectorTable.getTableFieldType(vectorTable.getLabelField());
//         gmerc=new GlobalMercator();
//
//    }

    public SpatiliteTileDataSource(SpatialiteDatabaseHandler spatialiteDatabaseHandler, SpatialVectorTable spatialVectorTable, SpatiliteTileSource tileSource) {
        vectorTable = spatialVectorTable;
        this.spatialiteDatabaseHandler = spatialiteDatabaseHandler;
        float[] b = spatialVectorTable.getTableBounds();
        this.boundingBox = new BoundingBox(b[1], b[0], b[3], b[2]);
        fieldtype = vectorTable.getTableFieldType(vectorTable.getLabelField());
        mMapElement = new MapElement();
//        mMapElement.layer = 5;
//        gmerc=new GlobalMercator();
        this.mTileSource = tileSource;

        mUseCache = (tileSource.tileCache != null);
    }
//    private static final Tag[] mTags = {
//            new Tag("natural", "water")
//    };
//    private static final Tag[] mTagsWay = {
//            new Tag("highway", "primary"),
//            new Tag("name", "Highway Rd")
//    };
//    private static final Tag[] mTagsBoundary = {
//            new Tag("boundary", "administrative"),
//            new Tag("admin_level", "2")
//    };
//
//    private static final Tag[] mTagsPlace = {
//            new Tag("place", "city"),
//            null
//    };


    @Override
    public void query(MapTile tile, ITileDataSink mapDataSink) {
        BoundingBox mTileBoundingBox = tile.getBoundingBox();
        if (!mTileBoundingBox.intersects(boundingBox)
                || tile.zoomLevel > mTileSource.getZoomLevelMax()
                || tile.zoomLevel < mTileSource.getZoomLevelMin()
        ) {
            mapDataSink.completed(TILE_NOT_FOUND);
            return;
        }
        isCanceled = false;
        MapElement e = mMapElement;

//        ITileCache cache = mTileSource.tileCache;
//        if (mUseCache) {
//            ITileCache.TileReader c = cache.getTile(tile);
//            if (c != null) {
//                InputStream is = c.getInputStream();
//                try {
//                    if (mTileDecoder.decode(tile, mapDataSink, is)) {
//                        mapDataSink.completed(SUCCESS);
//                        return;
//                    }
//                } catch (IOException ignored) {
//                    log.debug("{} Cache read: {}", tile, e);
//                } finally {
//                    IOUtils.closeQuietly(is);
//                }
//            }
//        }

//        QueryResult res = FAILED;
//        ITileCache.TileWriter cacheWriter = null;
//        try {
//            mConn.sendRequest(tile);
//            InputStream is = mConn.read();
//            if (mUseCache) {
//                cacheWriter = cache.writeTile(tile);
//                mConn.setCache(cacheWriter.getOutputStream());
//            }
//            if (mTileDecoder.decode(tile, sink, is))
//                res = SUCCESS;
//        } catch (SocketException e) {
//            log.debug("{} Socket Error: {}", tile, e.getMessage());
//        } catch (SocketTimeoutException e) {
//            log.debug("{} Socket Timeout", tile);
//            res = DELAYED;
//        } catch (UnknownHostException e) {
//            log.debug("{} Unknown host: {}", tile, e.getMessage());
//        } catch (IOException e) {
//            log.debug("{} Network Error: {}", tile, e.getMessage());
//        } catch (Exception e) {
//            log.debug("{} Error: {}", tile, e.getMessage());
//        } finally {
//            boolean ok = (res == SUCCESS);
//            if (!mConn.requestCompleted(ok) && ok)
//                res = FAILED;
//            if (cacheWriter != null)
//                cacheWriter.complete(ok);
//            sink.completed(res);
//        }


//        float x1 = -1;
//        float y1 = -1;
//        float x2 = size + 1;
//        float y2 = size + 1;
//
//        // always clear geometry before starting
//        // a different type.
//        e.clear();
//        e.startPolygon();
//        e.addPoint(x1, y1);
//        e.addPoint(x2, y1);
//        e.addPoint(x2, y2);
//        e.addPoint(x1, y2);
//
//        y1 = 5;
//        y2 = size - 5;
//        x1 = 5;
//        x2 = size - 5;
//
//        e.startHole();
//        e.addPoint(x1, y1);
//        e.addPoint(x2, y1);
//        e.addPoint(x2, y2);
//        e.addPoint(x1, y2);
//
//        e.setLayer(0);
//        e.tags.set(mTags);
//        mapDataSink.process(e);
//
//        if (renderWays) {
//            e.clear();
//
//            // middle horizontal
//            e.startLine();
//            e.addPoint(0, size / 2);
//            e.addPoint(size, size / 2);
//
//            // center up
//            e.startLine();
//            e.addPoint(size / 2, -size / 2);
//            e.addPoint(size / 2, size / 2);
//
//            // center down
//            e.startLine();
//            e.addPoint(size / 2, size / 2);
//            e.addPoint(size / 2, size / 2 + size);
//
//            // //e.setLayer(mTagsWay, 0);
//            mapDataSink.process(e);
//
//            e.clear();
//            // left-top to center
//            e.startLine();
//            e.addPoint(size / 2, size / 2);
//            e.addPoint(10, 10);
//
//            e.startLine();
//            e.addPoint(0, 10);
//            e.addPoint(size, 10);
//
//            e.startLine();
//            e.addPoint(10, 0);
//            e.addPoint(10, size);
//
//            e.setLayer(1);
//            e.tags.set(mTagsWay);
//            mapDataSink.process(e);
//        }
//
//        if (renderBoundary) {
//            e.clear();
//            e.startPolygon();
//            float r = size / 2;
//
//            for (int i = 0; i < 360; i += 4) {
//                double d = Math.toRadians(i);
//                e.addPoint(r + (float) Math.cos(d) * (r - 40),
//                        r + (float) Math.sin(d) * (r - 40));
//            }
//
//            e.setLayer(1);
//            e.tags.set(mTagsBoundary);
//            mapDataSink.process(e);
//        }
//
//        if (renderPlace) {
//            e.clear();
//            e.startPoints();
//            e.addPoint(size / 2, size / 2);
//
//            mTagsPlace[1] = new Tag("name", tile.toString());
//            e.tags.set(mTagsPlace);
//            mapDataSink.process(e);
//        }
        GeometryIterator geoms = spatialiteDatabaseHandler.getGeometryIteratorInBounds("4326", vectorTable,
                mTileBoundingBox.getMaxLatitude(), mTileBoundingBox.getMinLatitude(),
                mTileBoundingBox.getMaxLongitude(), mTileBoundingBox.getMinLongitude());
        jtsConverter = new JtsConverter(org.oscim.core.Tile.SIZE);//Tile.SIZE // UNSCALE_COORD
        jtsConverter.setPosition(tile.x, tile.y, Math.pow(2, tile.zoomLevel));
        while (geoms.hasNext()) {
            if (isCanceled) break;
            org.locationtech.jts.geom.Geometry element = geoms.next();
            if (element != null) {
                if (element.isValid())
                    switch (element.getGeometryType()) {
                        case "Polygon":
                        case "MultiPolygon":
                            for (int i = 0; i < element.getNumGeometries(); i++) {
                                e.clear();
                                e = parsetags(e, geoms);
                                jtsConverter.transformPolygon(e, (Polygon) element.getGeometryN(i));
                                if (e.getNumPoints() < 3) continue;
                                mapDataSink.process(e);
                            }
                            break;
                        case "Point":
                        case "MultiPoint":
                            for (int i = 0; i < element.getNumGeometries(); i++) {
                                e.clear();
                                e = parsetags(e, geoms);
                                jtsConverter.transformPoint(e, (Point) element.getGeometryN(i));
                                mapDataSink.process(e);
                            }
                            break;
                        case "LineString":
                        case "MultiLineString":
                            for (int i = 0; i < element.getNumGeometries(); i++) {
                                e.clear();
                                e = parsetags(e, geoms);
                                jtsConverter.transformLineString(e, (LineString) element.getGeometryN(i));
                                if (e.getNumPoints() < 2) continue;
                                mapDataSink.process(e);
                            }
                            break;
                    }
            }
        }
//         parse(geoms,mapDataSink);
        mapDataSink.completed(SUCCESS);
//        GeometryIterator geoms= spatialiteDatabaseHandler.getGeometryIteratorInBounds("4326",vectorTable,bounds[3],bounds[1],bounds[0],bounds[2],"3857");
//        jtsConverter = new JtsConverter(tile.SIZE /4);//Tile.SIZE / UNSCALE_COORD
//        jtsConverter.setPosition(tile.x,tile.y,Math.pow(2, tile.zoomLevel));
//         parse(geoms,mapDataSink);
//        GeometryBufferIterator geoms= spatialiteDatabaseHandler.getGeometryBufferIteratorInBounds("4326",vectorTable,bounds[3],bounds[1],bounds[0],bounds[2],"3857");
//        while(geoms.hasNext()) {
//            if (isCanceled)
//                break;
//            String element = geoms.next();
//            if (element != null) {
//                InputStream stream = new ByteArrayInputStream(element.getBytes(StandardCharsets.UTF_8));
//                try {
//                    if (mTileDecoder.decode(tile, mapDataSink, stream)) {
//                        mapDataSink.completed(SUCCESS);
//                        return;
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
        //if(result)
        //process this element
        //  mapDataSink.process(mMapElement);
    }

    private MapElement parsetags(MapElement e, GeometryIterator geoms) {
        Tag[] mTags = new Tag[3];
        List<Tag> tags = new ArrayList<>();
        mTags[0] = new Tag("name", geoms.getLabelText());
//        mTags[0] = new Tag("name", "hi");
        mTags[1] = new Tag(vectorTable.getThemeField(), geoms.getThemeFieldValue());
        mTags[2] = new Tag("gasnet_type", vectorTable.getTableName());
//        tags.add(new Tag(vectorTable.getLabelField(),geoms.getLabelText()));
//        tags.add(new Tag(vectorTable.getThemeField(),geoms.getThemeFieldValue()));
//        tags.add(new Tag("name",geoms.getLabelText()));
//        tags.add(new Tag("gasnet_type",vectorTable.getTableName()));
//        Tag[] mTags=(Tag[]) tags.toArray();
        e.tags.set(mTags);
//        e.tags.set(mTagsBoundary);
        mTileSource.decodeTags(e, mTags);
        return e;
//        switch (fieldtype){
//            case TEXT:
//                mTags[0]=new Tag(vectorTable.getLabelField(),geoms.getLabelText());
//                break;
//            case DOUBLE:
//                mTags[0]=new Tag(vectorTable.getLabelField(),geoms.getLabelText());
//                break;
//            case PHONE:
//                break;
//            case DATE:
//                break;
//            case INTEGER:
//                mTags[0]=new Tag(vectorTable.getLabelField(),geoms.getLabelText());
//                break;
//            case FLOAT:
//                mTags[0]=new Tag(vectorTable.getLabelField(),geoms.getLabelText());
//                break;
//            case BLOB:
//                break;
//        }
//        e.tags.set(mTags);
//        mTileSource.decodeTags(e,mTags);
    }

//    private void parse(GeometryIterator geoms,ITileDataSink mapDataSink) {
//        while(geoms.hasNext()) {
//            if(isCanceled)
//                break;
//            Geometry element = geoms.next();
//            if( element!=null){
//                if(element.isValid())
//                    switch (element.getGeometryType()){
//                        case "Polygon":
//                            for (int i = 0; i < element.getNumGeometries(); i++) {
//                                if(isCanceled)
//                                    break;
//                                mMapElement.clear();
//                                mMapElement.tags.clear();
//                                mTagMap.clear();
//                                Tag tg;
//                                TagSet tgset=new TagSet();
//                                jtsConverter.transformPolygon(mMapElement.clear(), (Polygon) element.getGeometryN(i));
//                                if (mMapElement.getNumPoints() < 3)
//                                    continue;
//                                if (!mClipper.clip(mMapElement))
//                                    continue;
//                                Tag tg2=new Tag("landuse","urban");
//                                mMapElement.tags.add(tg2);
//
//                                switch (fieldtype){
//
//                                    case TEXT:
//                                        mTagMap.put(vectorTable.getLabelField(),geoms.getLabelText());
//                                        tg=new Tag(vectorTable.getLabelField(),geoms.getLabelText());
//                                        tgset.add(tg);
//                                        mMapElement.tags.add(tg);
//
//                                        break;
//                                    case DOUBLE:
//                                        mTagMap.put(vectorTable.getLabelField(),geoms.getLabelText());
//                                        tg=new Tag(vectorTable.getLabelField(),geoms.getLabelText());
//                                        tgset.add(tg);
//                                        mMapElement.tags.add(tg);
//
//                                        break;
//                                    case PHONE:
//                                        break;
//                                    case DATE:
//                                        break;
//                                    case INTEGER:
//                                        mTagMap.put(vectorTable.getLabelField(),geoms.getLabelText());
//                                        tg=new Tag(vectorTable.getLabelField(),geoms.getLabelText());
//                                        tgset.add(tg);
//                                        mMapElement.tags.add(tg);
//
//                                        break;
//                                    case FLOAT:
//                                        mTagMap.put(vectorTable.getLabelField(),geoms.getLabelText());
//                                        tg=new Tag(vectorTable.getLabelField(),geoms.getLabelText());
//                                        tgset.add(tg);
//                                        mMapElement.tags.add(tg);
//
//                                        break;
//                                    case BLOB:
//                                        break;
//                                }
//
//                                if(isCanceled)
//                                    break;
//
//                                //add tag information
//                                if (mMapElement.tags.size() == 0)
//                                    continue;
//
//
//                                if (mMapElement.type == GeometryBuffer.GeometryType.NONE)
//                                    continue;
//
//
//                                mapDataSink.process(mMapElement);
//                                mapDataSink.completed(SUCCESS);
//
//                            }
//                    }
//
//
//
//            }
//
//        }
//
//
//
//
////        return true;
//    }

    @Override
    public void dispose() {

    }

    @Override
    public void cancel() {
        isCanceled = true;
    }
}
