package ir.gfpishro.geosuiteandroidprivateusers.VM;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.orhanobut.logger.Logger;

import org.oscim.core.GeoPoint;
import org.oscim.layers.marker.MarkerItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ir.gfpishro.geosuiteandroidprivateusers.Helpers.AppLogConverter;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.CacheHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.PermissionHelper;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.StringUtils;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.Utils;
import ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.LayerSchema;
import ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.MapHandler;
import ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.layers.MultiLayerWMSHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Keys;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Log.AppLog;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Map.Feature;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Map.GeoJson;
import ir.gfpishro.geosuiteandroidprivateusers.Models.User;
import ir.gfpishro.geosuiteandroidprivateusers.R;
import ir.gfpishro.geosuiteandroidprivateusers.Services.SyncService;

public class MainActivityVM {

    private Gson gson;
    private MapHandler mapHandler;
    private User user;
    private Activity activity;

    public MainActivityVM(Gson gson, MapHandler mapHandler, User user, Activity context) {
        this.gson = gson;
        this.mapHandler = mapHandler;
        this.user = user;
        this.activity = context;
    }

    private List<LayerSchema> loadLayerScehma() throws IOException {
        SharedPreferences pref = Utils.getSharedPref(activity);
        String json = StringUtils.getRawString(activity, R.raw.layers);
        ArrayList<LayerSchema> fileLayersHistory = gson.fromJson(json, Utils.getListType(LayerSchema.class));

        if (user != null && pref.contains(Keys.layers(user.getId()))) {
            json = pref.getString(Keys.layers(user.getId()), "");
            if (!StringUtils.isNullOrEmpty(json)) {
                ArrayList<LayerSchema> cacheLayersHistory = gson.fromJson(json, Utils.getListType(LayerSchema.class));
                for (int i = 0; i < cacheLayersHistory.size(); i++) {
                    LayerSchema l = cacheLayersHistory.get(i);
                    if (l.getType() != LayerSchema.LayerType.base) break;
                    fileLayersHistory.get(i).setEnable(l.isEnable());
                }
            }
        }
        return fileLayersHistory;
    }

    public void setupLayers() {
        try {
            mapHandler.layers = loadLayerScehma();
            MultiLayerWMSHandler.reset();
            List<LayerSchema> wmsLayers = new ArrayList<>();
            for (LayerSchema layer : mapHandler.layers) {
                try {
                    if ("wms".equalsIgnoreCase(layer.getFormatter()) && layer.getType() == LayerSchema.LayerType.online) {
                        boolean p = PermissionHelper.haveLayerPermission(layer.getAccessName());
                        layer.setAccessible(p);
                        if (p) wmsLayers.add(layer);
                        continue;
                    }
                    if (layer.getType() == LayerSchema.LayerType.eis) {
                        layer.setEnable(false);
                        layer.setVisibility(View.INVISIBLE);
                        layer.setUrl(null);
                        layer.setMapIndex(-1);
                    }
                    mapHandler.generateLayers(layer);
                } catch (Exception ex) {
                    Logger.e(ex, "MapInit1");
                    Toast.makeText(activity, "1." + ex.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
            mapHandler.handleWmsLayers(wmsLayers);
        } catch (Exception e) {
            Logger.e(e, "MapInit2");
            Toast.makeText(activity, "2." + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void fromSearchActivity(String json, boolean isFeatureCollection) {

        List<LayerSchema> toRemove = new ArrayList<>();
        for (LayerSchema layer : mapHandler.layers) {
            if (layer.getType() == LayerSchema.LayerType.search) {
                toRemove.add(layer);
            }
        }

        for (LayerSchema layerSchema : toRemove) {
            mapHandler.removeLayer(layerSchema);
            layerSchema.setEnable(false);
            layerSchema.setMapIndex(-1);
            layerSchema.setVisibility(View.INVISIBLE);
        }

        Feature feature = isFeatureCollection ? gson.fromJson(json, GeoJson.class).getFeatures().get(0) : gson.fromJson(json, Feature.class);
        JsonObject properties = feature.getProperties().getAsJsonObject();
        String pk = properties.get("pk").getAsString();
        String name = properties.get("name").getAsString();
        LayerSchema searchLayer = new LayerSchema();
        searchLayer.setEnable(true);
        searchLayer.setId(100 + Integer.parseInt(pk));
        searchLayer.setReadOnly(false);
        searchLayer.setTitle(String.format("%s - %s", name, pk));
        searchLayer.setUrl(json);
        searchLayer.setFormatter(String.valueOf(isFeatureCollection));
        searchLayer.setType(LayerSchema.LayerType.search);
        mapHandler.layers.add(searchLayer);
        mapHandler.generateLayers(searchLayer);
        mapHandler.updateMap(false);
    }

    public void fromEditorActivity(String[] geoJsonString, View view) {
        JsonArray geoJson = new JsonArray();
        for (String aGeoJsonString : geoJsonString)
            geoJson.add(gson.fromJson(aGeoJsonString, JsonObject.class));
        try {
            Utils.getSharedPref(activity).edit().putString(Keys.editLayer(user.getId()), gson.toJson(geoJson)).apply();
            AppLog log = AppLogConverter.editLayerToAppLog(geoJson, user);
            CacheHandler.getHandler(activity).push(log, true);
            new SyncService().pushEditLayer(new AppLog[]{log});
            Snackbar.make(view, R.string.msg_after_editing, Snackbar.LENGTH_SHORT).show();
        } catch (Exception e) {
            Snackbar.make(view, R.string.error_no_user, Snackbar.LENGTH_SHORT).show();
        }
    }

    public void exitNavigationMode(View fab) {
        fab.setVisibility(View.GONE);
        mapHandler.navigationMode = false;
        mapHandler.navigationLayer.hide(mapHandler.markerHandler);
    }

    public void goNavigationMode(final View fab, final GeoPoint point) {
        fab.setVisibility(View.VISIBLE);
        if (mapHandler.navigationLayer.points[0] == null) {
            mapHandler.navigationLayer.hide(mapHandler.markerHandler);
            mapHandler.navigationLayer.points[0] = point;
            mapHandler.navigationMode = true;
            MarkerItem marker = mapHandler.markerHandler.getMarkerItem(point, "from", "");
            mapHandler.markerHandler.addItem(marker);
            mapHandler.navigationLayer.navigationMarkers[0] = marker;
            Snackbar snackbar = Snackbar.make(fab, "مقصد را انتخاب کنید", Snackbar.LENGTH_LONG);
            snackbar.getView().setBackgroundColor(activity.getResources().getColor(R.color.nav_blue));
            snackbar.show();
            mapHandler.updateMap(false);
            return;
        }
        //else
        mapHandler.navigationMode = false;
        mapHandler.navigationLayer.points[1] = point;
        MarkerItem marker = mapHandler.markerHandler.getMarkerItem(point, "to", "");
        mapHandler.markerHandler.addItem(marker);
        mapHandler.navigationLayer.navigationMarkers[1] = marker;
        try {
            mapHandler.navigationLayer.navigateFromPoints(mapHandler);
            mapHandler.navigationLayer.addToMap();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            Logger.e(throwable, "Navigation Error!");
            Snackbar snackbar = Snackbar.make(fab, R.string.error, Snackbar.LENGTH_LONG);
            snackbar.getView().setBackgroundColor(activity.getResources().getColor(R.color.intro_red));
            snackbar.show();
        }
    }
}
