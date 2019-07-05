package ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.layers;

import org.oscim.tiling.source.OkHttpEngine;
import org.oscim.tiling.source.bitmap.BitmapTileSource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ir.gfpishro.geosuiteandroidprivateusers.Helpers.SettingsHandler;
import ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.tileUrlFormatter.WMSTileUrlFormatter;
import okhttp3.Cache;
import okhttp3.OkHttpClient;

public class MultiLayerWMSHandler {

    private static List<String> layersName = new ArrayList<>();
    private static String tilePath;
    private static String url;
    public static int mapIndex = -1;
    private OkHttpEngine.OkHttpFactory httpFactory;

    public MultiLayerWMSHandler(String url, String tilePath, OkHttpEngine.OkHttpFactory httpFactory) {
        MultiLayerWMSHandler.tilePath = tilePath;
        MultiLayerWMSHandler.url = url;
        this.httpFactory = httpFactory;
    }

    public static void reset() {
        layersName.clear();
        mapIndex = -1;
    }

    public static List<String> getLayerName() {
        return layersName;
    }

    public BitmapTileSource getTileSource() {
        if (httpFactory == null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            // Cache the tiles into file system
            File cacheDirectory = new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
//                File cacheDirectory = new File(Environment.getExternalStorageDirectory()+ Keys.mapsFolder +Keys.onlineMapCacheFolder);
            int cacheSize = 500 * 1024 * 1024; // 500 MB
            Cache cache = new Cache(cacheDirectory, cacheSize);
            builder.cache(cache);
            httpFactory = new OkHttpEngine.OkHttpFactory(builder);
        }
        BitmapTileSource wmsTileSourceLayer = BitmapTileSource.builder()
                .url(url.replace("{LAYERS}", getJoinedLayerNames(layersName)))
                .tilePath(tilePath)
                .httpFactory(httpFactory)
                .zoomMax(30)
                .build();
        wmsTileSourceLayer.setUrlFormatter(new WMSTileUrlFormatter());
        return wmsTileSourceLayer;
    }

    private String getJoinedLayerNames(List<String> layersName) {
//        return layersName.toString().replaceAll("\\[|\\]| ", "");
        return layersName.toString().replaceAll("\\[|\\]| ", "") + addCQLFilter();
    }

    private String addCQLFilter() {
        String cityCode = SettingsHandler.getSettings(null).cityCode;
        if (cityCode.equals("001")) return "";
        StringBuilder qry = new StringBuilder("&CQL_FILTER=");
        for (int i = 0, layersNameSize = layersName.size(); i < layersNameSize; i++) {
            qry.append("deleted=false");
            if (!cityCode.equals("001"))
                qry.append(" AND city_code='").append(cityCode).append("'");
            if (i < layersNameSize - 1) qry.append(";");
        }
        return String.valueOf(qry);
    }

    public static void resetLayersName() {
        MultiLayerWMSHandler.layersName.clear();
    }

    public static void pushLayer(String layerName) {
        if (!layersName.contains(layerName) || layerName.length() > 1) layersName.add(layerName);
    }
}
