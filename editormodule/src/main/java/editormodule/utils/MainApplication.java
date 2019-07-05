/*
 * Project:  NextGIS Mobile
 * Purpose:  Mobile GIS for Android.
 * Author:   Dmitry Baryshnikov (aka Bishop), bishop.dev@gmail.com
 * Author:   NikitaFeodonit, nfeodonit@yandex.com
 * Author:   Stanislav Petriakov, becomeglory@gmail.com
 * *****************************************************************************
 * Copyright (c) 2012-2017 NextGIS, info@nextgis.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package editormodule.utils;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.datasource.Feature;
import com.nextgis.maplib.datasource.Field;
import com.nextgis.maplib.datasource.GeoEnvelope;
import com.nextgis.maplib.datasource.GeoGeometryFactory;
import com.nextgis.maplib.datasource.GeoPoint;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.MapDrawable;
import com.nextgis.maplib.map.VectorLayer;
import com.nextgis.maplib.util.Constants;
import com.nextgis.maplib.util.GeoConstants;
import com.nextgis.maplib.util.SettingsConstants;
import com.nextgis.maplibui.GISApplication;
import com.nextgis.maplibui.mapui.LayerFactoryUI;
import com.nextgis.maplibui.mapui.VectorLayerUI;
import com.nextgis.maplibui.util.SettingsConstantsUI;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.github.nexgis.editor.R;
import editormodule.activity.SettingsActivity;
import editormodule.layerprovider.MbtileTMSProvider;

import static com.nextgis.maplib.util.Constants.MAP_EXT;
import static com.nextgis.maplib.util.Constants.MAP_LIMITS_XY;
import static editormodule.utils.AppSettingsConstants.AUTHORITY;
import static editormodule.utils.AppSettingsConstants.KEY_PREF_APP_VERSION;

/**
 * Main application class
 * The initial layers create here. Also upgrade db from previous version is here too.
 */
public class MainApplication extends GISApplication {
    public static final String LAYER_MBTILESATTLITE = "GoogleSattlite";
    public JSONObject[] OnlineLayer = new JSONObject[3];
    public static final String[] LAYERS = new String[]{"point", "line", "polygon"};

    //    public static final String LAYER_OSM = "osm";
    //    private Tracker mTracker;
    //    public static final String LAYER_TRACKS = "tracks";

    @Override
    public void onCreate() {
//        Sentry.init(this, BuildConfig.SENTRY_DSN);
//        Sentry.captureMessage("NGM2 Sentry is init.", Sentry.SentryEventLevel.DEBUG);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        updateFromOldVersion();

//        GoogleAnalytics.getInstance(this).setAppOptOut(!mSharedPreferences.getBoolean(KEY_PREF_GA, true));
//        GoogleAnalytics.getInstance(this).setDryRun(DEBUG_MODE);
//        getTracker();
//        setExceptionHandler();

        super.onCreate();
    }

//    private void setExceptionHandler() {
//        ExceptionReporter handler = new ExceptionReporter(getTracker(), Thread.getDefaultUncaughtExceptionHandler(), this);
//        StandardExceptionParser exceptionParser =
//                new StandardExceptionParser(getApplicationContext(), null) {
//                    @Override
//                    public String getDescription(String threadName, Throwable t) {
//                        return "{" + threadName + "} " + Log.getStackTraceString(t);
//                    }
//                };
//
//        handler.setExceptionParser(exceptionParser);
//        Thread.setDefaultUncaughtExceptionHandler(handler);
//    }

//    public synchronized Tracker getTracker() {
////        if (mTracker == null)
////            mTracker = GoogleAnalytics.getInstance(this).newTracker(R.xml.app_tracker);
////
////        return mTracker;
//    }

    @Override
    public void sendScreen(String name) {
//        mTracker.setScreenName(name);
//        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public String getAccountsType() {
        return Constants.NGW_ACCOUNT_TYPE;
    }

    @Override
    public void sendEvent(String category, String action, String label) {
//        HitBuilders.EventBuilder event = new HitBuilders.EventBuilder()
//                .setCategory(category)
//                .setAction(action)
//                .setLabel(label);

//        getTracker().send(event.build());
    }

    private void updateFromOldVersion() {
        try {
            int currentVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            int savedVersionCode = mSharedPreferences.getInt(KEY_PREF_APP_VERSION, 0);

            switch (savedVersionCode) {
                case 0:
                    int source;
                    if (mSharedPreferences.contains(SettingsConstants.KEY_PREF_LOCATION_SOURCE)) {
                        source = mSharedPreferences.getInt(SettingsConstants.KEY_PREF_LOCATION_SOURCE, 3);
                        mSharedPreferences.edit()
                                .remove(SettingsConstants.KEY_PREF_LOCATION_SOURCE)
                                .remove(SettingsConstants.KEY_PREF_LOCATION_SOURCE + "_str")
                                .putString(SettingsConstants.KEY_PREF_LOCATION_SOURCE, source + "").apply();
                    }
                    if (mSharedPreferences.contains(SettingsConstants.KEY_PREF_TRACKS_SOURCE)) {
                        source = mSharedPreferences.getInt(SettingsConstants.KEY_PREF_TRACKS_SOURCE, 1);
                        mSharedPreferences.edit()
                                .remove(SettingsConstants.KEY_PREF_TRACKS_SOURCE)
                                .remove(SettingsConstants.KEY_PREF_TRACKS_SOURCE + "_str")
                                .putString(SettingsConstants.KEY_PREF_TRACKS_SOURCE, source + "").apply();
                    }
                case 13:
                case 14:
                case 15:
                    mSharedPreferences.edit().remove(SettingsConstantsUI.KEY_PREF_SHOW_STATUS_PANEL)
                            .remove(SettingsConstantsUI.KEY_PREF_COORD_FORMAT + "_int")
                            .remove(SettingsConstantsUI.KEY_PREF_COORD_FORMAT).apply();
                default:
                    break;
            }

            if (savedVersionCode < currentVersionCode) {
                mSharedPreferences.edit().putInt(KEY_PREF_APP_VERSION, currentVersionCode).apply();
            }
        } catch (PackageManager.NameNotFoundException ignored) {
        }
    }

    @Override
    public MapBase getMap() {
        if (null != mMap) {
            return mMap;
        }

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        File defaultPath = getExternalFilesDir(SettingsConstants.KEY_PREF_MAP);
        if (defaultPath == null) {
            defaultPath = new File(getFilesDir(), SettingsConstants.KEY_PREF_MAP);
        }

        String mapPath = mSharedPreferences.getString(SettingsConstants.KEY_PREF_MAP_PATH, defaultPath.getPath());
        String mapName = mSharedPreferences.getString(SettingsConstantsUI.KEY_PREF_MAP_NAME, "default");

        File mapFullPath = new File(mapPath, mapName + MAP_EXT);

        final Bitmap bkBitmap = getMapBackground();
        mMap = new MapDrawable(bkBitmap, this, mapFullPath, new LayerFactoryUI());
        mMap.setName(mapName);
        mMap.load();

//        checkTracksLayerExist();
//        loadMBTile();

        return mMap;
    }

//    protected void checkTracksLayerExist() {
//        List<ILayer> tracks = new ArrayList<>();
//        LayerGroup.getLayersByType(mMap, Constants.LAYERTYPE_TRACKS, tracks);
//        if (tracks.isEmpty()) {
//            String trackLayerName = getString(R.string.tracks);
//            TrackLayerUI trackLayer =
//                    new TrackLayerUI(getApplicationContext(), mMap.createLayerStorage(LAYER_TRACKS));
//            trackLayer.setName(trackLayerName);
//            trackLayer.setVisible(true);
//            mMap.addLayer(trackLayer);
//            mMap.save();
//        }
//    }

    @Override
    public String getAuthority() {
        return AUTHORITY;
    }

    @Override
    public void showSettings(String settings) {
        if (TextUtils.isEmpty(settings)) {
            settings = SettingsConstantsUI.ACTION_PREFS_GENERAL;
        }

        switch (settings) {
            case SettingsConstantsUI.ACTION_PREFS_GENERAL:
            case SettingsConstantsUI.ACTION_PREFS_LOCATION:
            case SettingsConstantsUI.ACTION_PREFS_TRACKING:
                break;
            default:
                return;
        }

        Intent intent = new Intent(this, SettingsActivity.class);
        intent.setAction(settings);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onFirstRun() {
        //initBaseLayers();
    }

    public void initBaseLayers() {
//        if (mMap.getLayerByPathName(LAYER_OSM) == null) {
//            //add OpenStreetMap layer
//            String layerName = getString(R.string.osm);
//            String layerURL = SettingsConstantsUI.OSM_URL;
//            final RemoteTMSLayerUI layer = new RemoteTMSLayerUI(getApplicationContext(), mMap.createLayerStorage(LAYER_OSM));
//            layer.setName(layerName);
//            layer.setURL(layerURL);
//            layer.setTMSType(TMSTYPE_OSM);
//            layer.setVisible(true);
//            layer.setMinZoom(GeoConstants.DEFAULT_MIN_ZOOM);
//            layer.setMaxZoom(19);
//            mMap.addLayer(layer);
//            mMap.moveLayer(0, layer);
//            new Handler().post(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        layer.fillFromZip(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.mapnik), null);
//                    } catch (IOException | NGException | RuntimeException e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
//        }
        File storageDirectory = Environment.getExternalStorageDirectory();
        if (mMap.getLayerByName(LAYER_MBTILESATTLITE) == null) {
            String path = storageDirectory + "/gfp/tehranmapsroad.mbtiles";
            MbtileTMSProvider mbTile = new MbtileTMSProvider(this,
                    mMap.createLayerStorage(LAYER_MBTILESATTLITE), path);
            mbTile.setName(LAYER_MBTILESATTLITE);
            mbTile.setVisible(true);
            mMap.addLayer(mbTile);
            mMap.moveLayer(0, mbTile);
        }
        //****LIMIT
        mMap.setLimits(new GeoEnvelope(5635549.221409474, 5948635.289265558,
                4070118.8821290657, 4383204.9499851465), MAP_LIMITS_XY);
        mMap.setMaxZoom(27f);
        mMap.setMinZoom(10f);
        mMap.setZoomAndCenter(12, new GeoPoint(5721159, 4260906));
        //****LIMIT
        List<Field> fields = new ArrayList<>(2);
        fields.add(new Field(GeoConstants.FTInteger, "FID", "FID"));
        fields.add(new Field(GeoConstants.FTString, "TEXT", "TEXT"));
        try {
            if (OnlineLayer == null) throw new Exception("Empty JSONObject");
//            if (mMap.getLayerByPathName(LAYER_A) == null || OnlineLayer[0] == null)
            mMap.addLayer(createEmptyVectorLayer(getString(R.string.points_for_edit), LAYERS[0], GeoConstants.GTPoint, fields, OnlineLayer[0]));
            mMap.addLayer(createEmptyVectorLayer(getString(R.string.lines_for_edit), LAYERS[1], GeoConstants.GTLineString, fields, OnlineLayer[1]));
            mMap.addLayer(createEmptyVectorLayer(getString(R.string.polygons_for_edit), LAYERS[2], GeoConstants.GTPolygon, fields, OnlineLayer[2]));
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMap.save();
    }

    public VectorLayer createEmptyVectorLayer(
            String layerName,
            String layerPath,
            int layerType,
            List<Field> fields) {
        VectorLayerUI vectorLayer = new VectorLayerUI(this, mMap.createLayerStorage(layerPath));
        vectorLayer.setName(layerName);
        vectorLayer.setVisible(true);
        vectorLayer.setMinZoom(GeoConstants.DEFAULT_MIN_ZOOM);
        vectorLayer.setMaxZoom(GeoConstants.DEFAULT_MAX_ZOOM);
        vectorLayer.create(layerType, fields);
        return vectorLayer;
    }

    private VectorLayer createEmptyVectorLayer(
            String layerName,
            String layerPath,
            int layerType,
            List<Field> fields,
            JSONObject cachedLayer) throws Exception {
        VectorLayer vectorLayer = createEmptyVectorLayer(layerName, layerPath, layerType, fields);
        if (cachedLayer == null) return vectorLayer;
        if (!cachedLayer.has("features")) return vectorLayer;
        JSONArray features = cachedLayer.getJSONArray("features");
        for (int i = 0; features != null && i < features.length(); i++) {
            try {
                JSONObject featuresJSONObject = features.getJSONObject(i);
                JSONObject propertiesJSONObject = featuresJSONObject.optJSONObject("properties");
                int FEATURE_ID = propertiesJSONObject.optInt("FEATURE_ID");
                final Feature feature = new Feature();
//                feature.setId(FEATURE_ID);
                feature.setFieldValue("FEATURE_ID", FEATURE_ID);
                feature.setGeometry(GeoGeometryFactory.fromJson(featuresJSONObject.optJSONObject("geometry")));
                feature.setFieldValue("properties", propertiesJSONObject);
                if (vectorLayer.createFeature(feature) == Constants.NOT_FOUND)
                    throw new Exception("NOT_FOUND G358");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            String rendererJson = cachedLayer.optString("layerobject");
            JSONObject rendererObject = rendererJson.length() > 0 ? new JSONObject(rendererJson) : cachedLayer.optJSONObject("layerobject");
            rendererObject = rendererObject.optJSONObject("renderer_properties");
            vectorLayer.setRenderer(rendererObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vectorLayer;
    }

    public void reInit() {
        for (int i = getMap().getLayerCount() - 1; i >= 0; i--) {
            ILayer layer = getMap().getLayer(i);
            if (!layer.getPath().getName().equals(MainApplication.LAYER_MBTILESATTLITE)
                    ) {
                //&& !layer.getPath().getName().equals(MainApplication.LAYER_A)
                //                    && !layer.getPath().getName().equals(MainApplication.LAYER_B)
                //                    && !layer.getPath().getName().equals(MainApplication.LAYER_C)
                layer.delete();
            }
        }
        initBaseLayers();
    }
}
