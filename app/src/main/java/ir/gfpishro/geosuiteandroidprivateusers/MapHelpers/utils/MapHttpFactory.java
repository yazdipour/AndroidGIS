package ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.utils;

import org.oscim.tiling.source.OkHttpEngine;

import java.io.File;
import java.util.UUID;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class MapHttpFactory {
    private static OkHttpEngine.OkHttpFactory httpFactory;

    public static OkHttpEngine.OkHttpFactory getHttpFactory(final boolean USE_CACHE) {
        if (httpFactory != null) return httpFactory;
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (USE_CACHE) {
            File cacheDirectory = new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
            //File cacheDirectory = new File(Environment.getExternalStorageDirectory()+ Keys.mapsFolder +Keys.onlineMapCacheFolder);
            int cacheSize = 500 * 1024 * 1024; // 500 MB
            Cache cache = new Cache(cacheDirectory, cacheSize);
            builder.cache(cache);
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
            builder.addInterceptor(loggingInterceptor);
        }
        httpFactory = new OkHttpEngine.OkHttpFactory(builder);
        return httpFactory;
    }
}