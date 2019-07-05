package ir.gfpishro.geosuiteandroidprivateusers.MapHelpers;

import android.content.Context;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import org.oscim.layers.Layer;
import org.oscim.layers.tile.bitmap.BitmapTileLayer;
import org.oscim.layers.tile.vector.OsmTileLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.layers.tile.vector.labeling.LabelTileLoaderHook;
import org.oscim.map.Map;
import org.oscim.theme.ThemeLoader;
import org.oscim.theme.VtmThemes;
import org.oscim.tiling.TileSource;
import org.oscim.tiling.source.bitmap.BitmapTileSource;
import org.oscim.tiling.source.bitmap.DefaultSources;
import org.oscim.tiling.source.mapfile.MapFileTileSource;

import gfp.ir.vtmintegration.vtm.GasLayer;
import gfp.ir.vtmintegration.vtm.GasThemes;
import gfp.ir.vtmintegration.vtm.Mbtile.MbtileTileSource;
import gfp.ir.vtmintegration.vtm.SearchLayer;
import gfp.ir.vtmintegration.vtm.Spatialite.SpatiliteVectorLayer;
import ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.layers.MultiLayerWMSHandler;
import ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.tileUrlFormatter.MBTileUrlFormatter;
import ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.tileUrlFormatter.TMSTileUrlFormatterWithGoogleTileLocation;
import ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.utils.MapHttpFactory;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Keys;

public class LayerFactory {

    private final static String TAG = LayerFactory.class.getCanonicalName();
    private final Context context;
    private final Map map;
    private final MapHandler handler;

    LayerFactory(Context context, Map map, MapHandler handler) {
        this.context = context;
        this.map = map;
        this.handler = handler;
    }

    int addLayerToMap(Layer layer, LayerSchema ml) {
        map.layers().add(layer);
        int mIndex = map.layers().size() - 1;
        ml.setMapIndex(mIndex);
        return mIndex;
    }

    Layer buildLayer(LayerSchema ml) {
        switch (ml.getType()) {
            case search:
                return buildSearchLayer(ml);
            case eis:
                return buildEISLayer();
            case pg_point:
            case pg_pipe:
            case bg_pipe:
            case riser:
            case parcel:
                return buildOfflineLayer(ml);
            case online:
                return buildXMSLayer(ml);
            case base:
                return buildBaseLayer(ml);
            default:
                return null;
        }
    }

    private Layer buildBaseLayer(LayerSchema ml) {
        if (ml.getUrl().contains("[OSM]"))
            return new BitmapTileLayer(map, DefaultSources.OPENSTREETMAP.build());
        else if (ml.getUrl().contains(".map"))
            return buildMapFileTileLayer(handler.BASE_PATH + Keys.mapsFolder + ml.getUrl());
        else if (ml.getUrl().contains(".mbtile"))
            return buildMBTileLayer(handler.BASE_PATH + Keys.mapsFolder + ml.getUrl());
        else return buildXMSLayer(ml,
                    ml.getUrl().contains("http") ? ml.getUrl() : handler.BASE_URL.replace(":8080", "") + ml.getUrl());
    }

    private BitmapTileLayer buildMBTileLayer(String mapPath) {
        return new BitmapTileLayer(map, new MbtileTileSource(mapPath));
    }

    private VectorTileLayer buildMapFileTileLayer(String mapPath) {
        MapFileTileSource tileSource = new MapFileTileSource();
        tileSource.setMapFile(mapPath);
        VectorTileLayer l = new OsmTileLayer(map);
        l.setTileSource(tileSource);
        l.setRenderTheme(ThemeLoader.load(VtmThemes.OSMAGRAY));
        return l;
    }

    private Layer buildXMSLayer(LayerSchema ml) {
        return buildXMSLayer(ml, handler.BASE_URL + ml.getUrl());
    }

    private Layer buildXMSLayer(LayerSchema ml, String uri) {
        BitmapTileSource tileSource;
        // base online mbtile is not Google
        if (ml.getType() == LayerSchema.LayerType.base) {
            tileSource = BitmapTileSource.builder()
                    .url(uri)
                    .tilePath(ml.getTilePath())
                    .zoomMax(ml.getMaxZoom())
                    .zoomMin(ml.getMinZoom())
                    .build();
            tileSource.setHttpEngine(MapHttpFactory.getHttpFactory(true));
            tileSource.setUrlFormatter(new MBTileUrlFormatter());
        } else if ("wms".equalsIgnoreCase(ml.getFormatter()))
            tileSource = new MultiLayerWMSHandler(uri, ml.getTilePath(),
                    MapHttpFactory.getHttpFactory(true))
                    .getTileSource();
        else {
            tileSource = BitmapTileSource.builder()
                    .url(uri)
                    .tilePath(ml.getTilePath())
                    .zoomMax(ml.getMaxZoom())
                    .zoomMin(ml.getMinZoom())
                    .build();
            tileSource.setUrlFormatter(new TMSTileUrlFormatterWithGoogleTileLocation());
        }
        BitmapTileLayer bitmapTileLayer = new BitmapTileLayer(map, tileSource);
        if (ml.getType() != LayerSchema.LayerType.base) bitmapTileLayer.setBitmapAlpha(0.7f);
        return bitmapTileLayer;
    }

    private Layer buildOfflineLayer(LayerSchema ml) {
        try {
            TileSource tileSource = buildGasOfflineLayer(ml.getType(), handler.BASE_PATH + Keys.mapsFolder + ml.getUrl(), ml.getMinZoom(), ml.getMaxZoom());
            SpatiliteVectorLayer vtl = new SpatiliteVectorLayer(map, tileSource);
            vtl.setRenderTheme(ThemeLoader.load(GasThemes.DEFAULT));
            return vtl;
        } catch (Exception e) {
            Toast.makeText(context, "خطا در لود لایه:" + ml.getMapIndex() + ml.getTitle(), Toast.LENGTH_SHORT).show();
            Logger.e(TAG, "generateLayers: ", e);
            return null;
        }
    }

    Layer buildOfflineLayerLabelLayer(SpatiliteVectorLayer vtl) {
        if (vtl == null) return null;
        LabelLayer labelLayer = new LabelLayer(map, vtl, new LabelTileLoaderHook(), map.viewport().getMaxZoomLevel() - 2);
        labelLayer.removeZoomLimit();
        return labelLayer;
    }

    private Layer buildSearchLayer(LayerSchema ml) {
        SearchLayer layer = new SearchLayer(context, map);
        if (Boolean.parseBoolean(ml.getFormatter()))
            layer.AddFeatures(ml.getUrl());
        else layer.AddFeature(ml.getUrl());
        return layer;
    }

    private Layer buildEISLayer() {
        return new SearchLayer(context, map);
    }

    private TileSource buildGasOfflineLayer(LayerSchema.LayerType type, String path, Integer minZoom, Integer maxZoom) {
        TileSource tileSource = null;
        GasLayer.initSpatialiteDatabaseHandler(path);
        switch (type) {
            case bg_pipe:
                tileSource = new GasLayer(
                        GasLayer.FIELDS.BgPipe_TABLE,
                        GasLayer.FIELDS.LAYER,
                        GasLayer.FIELDS.LAYER,
                        minZoom, maxZoom);
                break;
            case parcel:
                tileSource = new GasLayer(
                        GasLayer.FIELDS.Parcel_TABLE,
                        GasLayer.FIELDS.Parcel_RISERGISCODEMAINE,
                        GasLayer.FIELDS.Parcel_MARKETING,
                        minZoom, maxZoom);
                break;
            case pg_pipe:
                tileSource = new GasLayer(
                        GasLayer.FIELDS.PgPipe_TABLE,
                        GasLayer.FIELDS.LAYER,
                        GasLayer.FIELDS.LAYER,
                        minZoom, maxZoom);
                break;
            case pg_point:
                tileSource = new GasLayer(
                        GasLayer.FIELDS.PgPoint_TABLE,
                        GasLayer.FIELDS.FTYPE,
                        GasLayer.FIELDS.FTYPE,
                        minZoom, maxZoom);
                break;
            case riser:
                tileSource = new GasLayer(
                        GasLayer.FIELDS.Riser_TABLE,
                        GasLayer.FIELDS.ID,
                        GasLayer.FIELDS.R_NUM,
                        minZoom, maxZoom);
        }
        return tileSource;
    }
}