package ir.gfpishro.geosuiteandroidprivateusers.MapHelpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.graphhopper.PathWrapper;
import com.orhanobut.logger.Logger;

import org.locationtech.jts.geom.Point;
import org.oscim.core.GeoPoint;
import org.oscim.layers.Layer;
import org.oscim.layers.LocationLayer;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.marker.MarkerSymbol;
import org.oscim.layers.tile.bitmap.BitmapTileLayer;
import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.layers.vector.PathLayer;
import org.oscim.map.Map;
import org.oscim.renderer.GLViewport;
import org.oscim.scalebar.DefaultMapScaleBar;
import org.oscim.scalebar.MapScaleBarLayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gfp.ir.vtmintegration.vtm.SearchLayer;
import gfp.ir.vtmintegration.vtm.Spatialite.SpatiliteVectorLayer;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.PermissionHelper;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.SettingsHandler;
import ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.layers.EISLayer;
import ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.layers.MultiLayerWMSHandler;
import ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.layers.NavigationLayer;
import ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.layers.RulerLayer;
import ir.gfpishro.geosuiteandroidprivateusers.Models.User;
import ir.gfpishro.geosuiteandroidprivateusers.R;
import ir.gfpishro.geosuiteandroidprivateusers.Services.ApiHandler;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.LayerSchema.LayerType.bg_pipe;
import static ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.LayerSchema.LayerType.online;
import static ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.LayerSchema.LayerType.parcel;
import static ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.LayerSchema.LayerType.pg_pipe;
import static ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.LayerSchema.LayerType.pg_point;
import static ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.LayerSchema.LayerType.riser;
import static ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.LayerSchema.LayerType.search;

@SuppressLint("DefaultLocale")
public class MapHandler {
    final public String BASE_PATH = Environment.getExternalStorageDirectory().getPath();
    final private Context context;
    final private Map map;
    final public String BASE_URL;
    final private String TAG = MapHandler.class.getName();
    final private Gson gson = new Gson();
    final public MarkerHandler markerHandler = new MarkerHandler();
    public boolean navigationMode = false, rulerMode = false;
    public static boolean godMode = false;
    public NavigationLayer missionNavigationLayer;
    public NavigationLayer navigationLayer;
    public List<LayerSchema> layers;
    public BuildingLayer buildingLayer = null;
    private LabelLayer labelLayer = null;
    private RulerLayer rulerLayer;
    private LayerFactory layerFactory;
    private List<LayerSchema.LayerType> offlineLayers = Arrays.asList(pg_point, pg_pipe, bg_pipe, riser, parcel);

    public MapHandler(Map map, Context context, String BASE_URL) {
        this.map = map;
        this.context = context;
        this.BASE_URL = BASE_URL;
        initNavigationLayer();
        layerFactory = new LayerFactory(context, map, this);
    }

    public void setupMapControls(View miniCompass, TextView tv_coordinate) {
//        UTM utm_b = MapConverter.WGS84_To_UTM(mapPosition.getGeoPoint());
//        tv.setText(String.format("UTM(%s)\n(Lat:%.5f, Long:%.5f, Zoom: %d)", utm_b.toString(), mapPosition.getLatitude(), mapPosition.getLongitude(), mapPosition.getZoomLevel()));
        //  Map Mini-Compass
        map.events.bind((e, mapPosition) -> {
            tv_coordinate.setText(String.format("Lat:%.5f, Long:%.5f, Zoom: %d", mapPosition.getLatitude(),
                    mapPosition.getLongitude(), mapPosition.getZoomLevel()));
            if (SettingsHandler.getSettings(context).isRotationEnable)
                miniCompass.setRotation(mapPosition.getBearing());
        });
        //  Scale bar
        map.layers().add(getMapScaleBar());
    }

    private void initNavigationLayer() {
        float lineWidth = (4 * context.getResources().getDisplayMetrics().density);
//        missionNavigationLayer = new NavigationLayer(map, 0x9900cc33, lineWidth);
        missionNavigationLayer = new NavigationLayer(map, 0xffff33ff, lineWidth);
//        navigationLayer = new NavigationLayer(map, 0xff4286F5, lineWidth);
        navigationLayer = new NavigationLayer(map, 0xffff33ff, lineWidth);
    }

    public void generateLayers(LayerSchema ml) {
        if (!ml.getAccessName().equals("allow")
                && !godMode
                && ml.getType() != search
                && !"wms".equalsIgnoreCase(ml.getFormatter())
                && !PermissionHelper.haveLayerPermission(ml.getAccessName())) {
            ml.setAccessible(false);
            return;
        }
        ml.setAccessible(true);
        if (!ml.isEnable()) return;
        // Build Layer
        switch (ml.getType()) {
            case base: {
                if (ml.getUrl().contains(".map")) {
                    VectorTileLayer vectorTileLayer = (VectorTileLayer) layerFactory.buildLayer(ml);
                    map.setBaseMap(vectorTileLayer);
                    if (buildingLayer == null) {
                        buildingLayer = new BuildingLayer(map, vectorTileLayer);
                        labelLayer = new LabelLayer(map, vectorTileLayer);
                        map.layers().add(buildingLayer);
                        map.layers().add(labelLayer);
                    }
                } else {
                    map.setBaseMap((BitmapTileLayer) layerFactory.buildLayer(ml));
                    if (buildingLayer != null) {
                        buildingLayer.setEnabled(false);
                        labelLayer.setEnabled(false);
                        buildingLayer = null;
                        labelLayer = null;
                    }
                }
                ml.setMapIndex(1);
                break;
            }
            case online:
                if ("tms".equalsIgnoreCase(ml.getFormatter()) && ml.getMapIndex() != null) {
                    reEnableLayer(ml);
                    break;
                }
                BitmapTileLayer bitmapTileLayer = (BitmapTileLayer) layerFactory.buildLayer(ml);
                if ("wms".equalsIgnoreCase(ml.getFormatter())) {
                    if (MultiLayerWMSHandler.mapIndex == -1) {
                        map.layers().add(bitmapTileLayer);
                        MultiLayerWMSHandler.mapIndex = map.layers().size() - 1;
                    } else {
                        map.layers().remove(MultiLayerWMSHandler.mapIndex);
                        map.layers().add(MultiLayerWMSHandler.mapIndex, bitmapTileLayer);
                    }
                    ml.setMapIndex(MultiLayerWMSHandler.mapIndex);
                } else {
                    map.layers().add(bitmapTileLayer);
                    ml.setMapIndex(map.layers().size() - 1);
                }
                break;
            case pg_point:
            case pg_pipe:
            case bg_pipe:
            case riser:
            case parcel: {
                if (ml.getMapIndex() != null && ml.getMapIndex() != -1) {
                    reEnableLayer(ml);
                    break;
                }
                SpatiliteVectorLayer offlineLayer = (SpatiliteVectorLayer) layerFactory.buildLayer(ml);
                layerFactory.addLayerToMap(offlineLayer, ml);
                Layer labelLayer = layerFactory.buildOfflineLayerLabelLayer(offlineLayer);
                if (labelLayer == null) break;
//                map.layers().add(this.labelLayer);
//                ml.setMarkerIndex(map.layers().size() - 1);
                break;
            }
            case search: {
                if (ml.getMapIndex() != null) {
                    reEnableLayer(ml);
                    break;
                }
                try {
                    Layer layer = layerFactory.buildLayer(ml);
                    layerFactory.addLayerToMap(layer, ml);
                    ((SearchLayer) layer).zoomToExtent(1 << 18);
                } catch (Exception e) {
                    Logger.e(e, "MH196");
                }
                break;
            }
            case eis: {
                if (ml.getUrl().isEmpty() && ml.getMapIndex() != null) {
                    reEnableLayer(ml);
                    break;
                }
                JsonObject hashMap = gson.fromJson(ml.getUrl(), JsonObject.class);
                String eisId = hashMap.get("eisId").getAsString(),
                        type = hashMap.get("type").getAsString(),
                        eisAreaGeoJson = hashMap.get("eisGeoJson").getAsString(),
                        geoJson = hashMap.get("geojson").getAsString();
                ml.setUrl("");

                //MY CODE
                {
                    int areaListIndex = getListIndexById(EISLayer.AREA.jsonId);
                    LayerSchema areaMl = layers.get(areaListIndex);
                    areaMl.setUrl("");
                    SearchLayer eisLayer = (SearchLayer) layerFactory.buildLayer(areaMl);
                    eisLayer.setStyle(EISLayer.AREA.getStyle(context));
                    eisLayer.AddFeatures(eisAreaGeoJson);
                    Integer mapIndex = areaMl.getMapIndex();
                    if (mapIndex == null || mapIndex == -1) {
                        map.layers().add(eisLayer);
                        areaMl.setMapIndex(map.layers().size() - 1);
                    } else {
                        map.layers().remove((int) mapIndex);
                        map.layers().add(mapIndex, eisLayer);
                    }
                    areaMl.setEnable(true);
                    areaMl.setVisibility(View.VISIBLE);
//                    //Enable AREA if isDisabled
//                    layers.get(areaListIndex).setEnable(true);
//                    layers.get(areaListIndex).setVisibility(View.VISIBLE);
//                    map.layers().get(mapIndex).setEnabled(true);

                    //========================================
                    EISLayer theLayer = type.equals(EISLayer.RISER.type) ? EISLayer.RISER :
                            type.equals(EISLayer.PARCEL.type) ? EISLayer.PARCEL : EISLayer.VALVE;

                    int listIndex = getListIndexById(ml.getId());
                    SearchLayer featuresLayer = (SearchLayer) layerFactory.buildLayer(ml);
                    featuresLayer.setStyle(theLayer.getStyle(context));
                    featuresLayer.setEnabled(true);
                    featuresLayer.clear();
                    featuresLayer.AddFeatures(geoJson);
                    layers.get(listIndex).setVisibility(View.VISIBLE);
                    layers.get(listIndex).setEnable(true);
                    GeoPoint p = featuresLayer.getCenter();
                    if (featuresLayer.getFeatures().size() > 0) {
                        Point mExtent = featuresLayer.getFeatures().get(0).getGeometry().getCentroid();
                        p = new GeoPoint(mExtent.getCentroid().getY(), mExtent.getCentroid().getX());
                    }
                    moveTo(p, -1, true);
                    mapIndex = ml.getMapIndex();
                    if (mapIndex == null || mapIndex == -1) {
                        map.layers().add(featuresLayer);
                        ml.setMapIndex(map.layers().size() - 1);
                    } else {
                        map.layers().remove((int) mapIndex);
                        map.layers().add(mapIndex, featuresLayer);
                    }
                    ml.setEnable(true);
                    ml.setVisibility(View.VISIBLE);

                    //=========================================

                    for (EISLayer eLayer : EISLayer.values()) {
                        listIndex = getListIndexById(eLayer.jsonId);
                        int mIndex = layers.get(listIndex).getMapIndex();
                        if (eLayer != theLayer && eLayer != EISLayer.AREA &&
                                !eLayer.eisId.equals(eisId) && mIndex != -1) {
                            map.layers().get(mIndex).setEnabled(false);
                            layers.get(listIndex).setEnable(false);
                            layers.get(listIndex).setVisibility(View.INVISIBLE);
                            eLayer.eisId = "-1";
                        }
                    }

                }
                /*

                //Re-INIT AREA_EIS Then THE_EIS
                int areaListIndex = getListIndexById(EISLayer.AREA.jsonId);
                SearchLayer eisLayer = (SearchLayer) layerFactory.buildLayer(ml);
                eisLayer.setStyle(EISLayer.AREA.getStyle(context));
                Integer mapIndex = layers.get(areaListIndex).getMapIndex();
                if (mapIndex == null || mapIndex == -1) {
                    map.layers().add(eisLayer);
                    layers.get(areaListIndex).setMapIndex(map.layers().size() - 1);
                } else {
                    map.layers().remove((int) mapIndex);
                    map.layers().add(mapIndex, eisLayer);
                }
                EISLayer eis = EISLayer.values()[ml.getId() % EISLayer.AREA.jsonId];
                //============================================== WHY??!
                eisLayer = (SearchLayer) layerFactory.buildLayer(ml);
                eisLayer.setStyle(eis.getStyle(context));
                mapIndex = ml.getMapIndex();
                if (mapIndex == null || mapIndex == -1) {
                    map.layers().add(eisLayer);
                    mapIndex = map.layers().size() - 1;
                } else {
                    map.layers().remove((int) mapIndex);
                    map.layers().add(mapIndex, eisLayer);
                }
                //================================================
                ml.setMapIndex(mapIndex);
                ml.setEnable(true);
                ml.setVisibility(View.VISIBLE);
                //Enable AREA if isDisabled
                layers.get(areaListIndex).setEnable(true);
                layers.get(areaListIndex).setVisibility(View.VISIBLE);
                map.layers().get(layers.get(areaListIndex).getMapIndex()).setEnabled(true);
                // Fill Area
                try {
                    EISLayer.AREA.eisId = eisId;
                    SearchLayer areaLayer = (SearchLayer) map.layers().get(layers.get(areaListIndex).getMapIndex());
                    areaLayer.clear();
                    areaLayer.AddFeatures(eisAreaGeoJson);
                } catch (Exception ignored) {
                }
                //theLayer
                EISLayer theLayer = type.equals(EISLayer.RISER.type) ? EISLayer.RISER :
                        type.equals(EISLayer.PARCEL.type) ? EISLayer.PARCEL : EISLayer.VALVE;
                if (!theLayer.eisId.equals(eisId)) {
                    theLayer.eisId = eisId;
                    int listIndex = getListIndexById(theLayer.jsonId);
                    SearchLayer l = (SearchLayer) map.layers().get(layers.get(listIndex).getMapIndex());
                    l.clear();
                    l.setEnabled(true);
                    l.AddFeatures(geoJson);
                    layers.get(listIndex).setVisibility(View.VISIBLE);
                    layers.get(listIndex).setEnable(true);
                    GeoPoint p = l.getCenter();
                    if (l.getFeatures().size() > 0) {
                        Point mExtent = l.getFeatures().get(0).getGeometry().getCentroid();
                        p = new GeoPoint(mExtent.getCentroid().getY(), mExtent.getCentroid().getX());
                    }
                    moveTo(p, -1, true);
                }
                //handle otherLayers
                for (EISLayer eLayer : EISLayer.values()) {
                    int listIndex = getListIndexById(eLayer.jsonId);
                    int mIndex = layers.get(listIndex).getMapIndex();
                    if (eLayer != theLayer && eLayer != EISLayer.AREA &&
                            !eLayer.eisId.equals(eisId) && mIndex != -1) {
                        map.layers().get(mIndex).setEnabled(false);
                        layers.get(listIndex).setEnable(false);
                        layers.get(listIndex).setVisibility(View.INVISIBLE);
                        eLayer.eisId = "-1";
                    }
                }*/
                break;
            }
            case online_nav:
                Toast.makeText(context, "Online Navigation is not Supported", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void reEnableLayer(LayerSchema layerSchema) {
        if (layerSchema.getMapIndex() == -1) return;
        map.layers().get(layerSchema.getMapIndex()).setEnabled(true);
        layerSchema.setEnable(true);
        layerSchema.setVisibility(View.VISIBLE);
        if (layerSchema.getMarkerIndex() != -1 && offlineLayers.contains(layerSchema.getType()))
            map.layers().get(layerSchema.getMarkerIndex()).setEnabled(true);
    }

    public ItemizedLayer<MarkerItem> getMarkerLayer() {
        MarkerSymbol symbol = markerHandler.getSymbol(R.drawable.map_marker, context, true);
        return (markerHandler.getMarkerLayer(map, symbol, null));
    }


    public MapScaleBarLayer getMapScaleBar() {
        MapScaleBarLayer mapScaleBarLayer = new MapScaleBarLayer(map, new DefaultMapScaleBar(map));
        mapScaleBarLayer.getRenderer().setPosition(GLViewport.Position.BOTTOM_LEFT);
//        mapScaleBarLayer.getRenderer().setOffset(5 * CanvasAdapter.getScale(), 0);
        return mapScaleBarLayer;
    }

    public LocationLayer getLocationLayer() {
        LocationLayer locationLayer = new LocationLayer(map);
        locationLayer.locationRenderer.setShader("location_1_reverse");
        locationLayer.setEnabled(false);
        return locationLayer;
    }

    public void navigateTo_Online(String serverIp, final int id, final int marker_id, final String title,
                                  double lat, double lon, double lat2, double lon2) {
        ApiHandler.getApi(serverIp)
                .getRoute(User.getCredential(), lat, lon, lat2, lon2)
                .enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                        final String geoJson = gson.toJson(response.body());
                        LayerSchema layerSchema = new LayerSchema();
                        layerSchema.setEnable(true);
                        layerSchema.setId(id);
                        layerSchema.setMarkerIndex(marker_id);
                        layerSchema.setReadOnly(false);
                        layerSchema.setTitle(title);
                        layerSchema.setUrl(geoJson);
                        layerSchema.setType(LayerSchema.LayerType.online_nav);
                        generateLayers(layerSchema);
                        Logger.d(response.body());
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                        Logger.e(TAG, "onFailure: ", t);
                    }
                });
    }

    public PathLayer offlineNavigationTo(double lat, double lon, double lat2, double lon2) throws Throwable {
        PathWrapper pathWrap = new GraphHopperHandler.CalcPath(
                GraphHopperHandler.getGraphHopper(), lat, lon, lat2, lon2).execute().get();
        if (pathWrap.hasErrors()) throw pathWrap.getErrors().get(0);
        return GraphHopperHandler.createPathLayer(map, pathWrap);
    }

    /**
     * @param location: Move to Location
     * @param zoom:     Zoom Level, (-1 for Max), (0 for Default)
     */
    public void moveTo(GeoPoint location, int zoom, boolean animate) {
        if (location == null) return;
        if (zoom == -1) {
            int maxZoom = SettingsHandler.getSettings(context).maxZoom;
            zoom = maxZoom > 18 ? 17 : maxZoom;
        } else if (zoom == 0) zoom = map.getMapPosition().getZoomLevel();
        if (animate) map.animator().animateTo(location);
        else map.setMapPosition(location.getLatitude(), location.getLongitude(), 1 << zoom);
    }

    public void removeLayer(LayerSchema layer) {
        int inx = layer.getMapIndex();
        map.layers().get(inx).setEnabled(false);
        map.layers().remove(inx);
        for (int i = layers.indexOf(layer); i < layers.size(); i++)
            layers.get(i).setMapIndex(layers.get(i).getMapIndex() - 1);
    }

    List<LayerSchema> getLayersByType(LayerSchema.LayerType... types) {
        List<LayerSchema> ls = new ArrayList<>();
        for (LayerSchema layerSchema : layers)
            for (LayerSchema.LayerType t : types)
                if (t == layerSchema.getType()) {
                    ls.add(layerSchema);
                    break;
                }
        return ls;
    }

    List<LayerSchema> getLayersByName(String name) {
        List<LayerSchema> ls = new ArrayList<>();
        for (LayerSchema layerSchema : layers)
            if (layerSchema.getAccessName().equalsIgnoreCase(name))
                ls.add(layerSchema);
        return ls;
    }

    int getListIndexById(int id) {
        for (int i = 0; i < layers.size(); i++)
            if (layers.get(i).getId() == id) return i;
        return -1;
    }

    public void disposeHopper() {
        GraphHopperHandler.killHopper();
    }

    RulerLayer getRulerLayer() {
        return rulerLayer;
    }

    public void disposeRulerLayer() {
        getRulerLayer().dispose();
        rulerLayer = null;
    }

    public void initRulerLayer(org.oscim.android.MapView mapView, final FloatingActionButton fab) {
        rulerLayer = new RulerLayer(mapView, this, context, map.layers().size(), fab);
        map.layers().add(rulerLayer);
    }

    public void updateMap(boolean redraw) {
        map.updateMap(redraw);
    }

    public Map getMap() {
        return map;
    }

    public void handleWmsLayers(List<LayerSchema> wmsLayers) {
        if (wmsLayers.size() == 0) return;
        MultiLayerWMSHandler.resetLayersName();
        for (LayerSchema layer : wmsLayers)
            if (layer.isEnable() && layer.getAccessible())
                MultiLayerWMSHandler.pushLayer(layer.getAccessName());
        //            else MultiLayerWMSHandler.popLayer(layer.getAccessName());
        if (MultiLayerWMSHandler.getLayerName().size() == 0) {
            if (MultiLayerWMSHandler.mapIndex != -1) //not first time
                if (map.layers().get(MultiLayerWMSHandler.mapIndex) != null)
                    map.layers().get(MultiLayerWMSHandler.mapIndex).setEnabled(false);
            return;
        }
        boolean previousEnabled = wmsLayers.get(0).isEnable();
        boolean previousAccess = wmsLayers.get(0).getAccessible();
        wmsLayers.get(0).setEnable(true);
        wmsLayers.get(0).setAccessible(true);
        generateLayers(wmsLayers.get(0));
        wmsLayers.get(0).setAccessible(previousAccess);
        wmsLayers.get(0).setEnable(previousEnabled);
    }

    public void onAndOffLayers(boolean goOnline) {
        List<LayerSchema> newLayers = new ArrayList<>(layers);
        for (LayerSchema layerSchema : newLayers)
            if (layerSchema.getType() == online)
                layerSchema.setEnable(goOnline);
            else if (offlineLayers.contains(layerSchema.getType()))
                layerSchema.setEnable(!goOnline);
        manageChangesInLayers(newLayers);
    }

    public void manageChangesInLayers(List<LayerSchema> newLayers) {
        List<LayerSchema> needToRemove = new ArrayList<>();
        List<LayerSchema> wmsLayers = new ArrayList<>();
        for (LayerSchema oldLayer : layers) {
            if (!offlineLayers.contains(oldLayer.getType()) && oldLayer.getMarkerIndex() != -1)
                markerHandler.getMarkerLayer().removeItem(oldLayer.getMarkerIndex());
            boolean found = false;
            for (LayerSchema layer : newLayers)
                if (layer.equals(oldLayer)) {
                    found = true;
                    if ("wms".equalsIgnoreCase(layer.getFormatter()) && layer.getType() == online) {
                        oldLayer.setEnable(layer.isEnable());
                        wmsLayers.add(oldLayer);
                        continue;
                    }
                    if (oldLayer.isEnable() != layer.isEnable()) try {
                        oldLayer.setEnable(layer.isEnable());
                        if (layer.isEnable()) generateLayers(oldLayer);
                        else if (oldLayer.getType() != LayerSchema.LayerType.base) {
                            Integer mapIndex = oldLayer.getMapIndex();
                            if (mapIndex == null || mapIndex == -1) continue;
                            map.layers().get(mapIndex).setEnabled(false);
                            // Remove MarkerLayer of the Layer
                            if (offlineLayers.contains(oldLayer.getType()))
                                if (oldLayer.getMarkerIndex() != -1)
                                    map.layers().get(oldLayer.getMarkerIndex()).setEnabled(false);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            // Remove Layer
            if (!found) try {
                needToRemove.add(oldLayer);
                removeLayer(oldLayer);
            } catch (Exception ignored) {
            }
        }
        handleWmsLayers(wmsLayers);
        for (LayerSchema layer : needToRemove) layers.remove(layer);
        map.updateMap(true);
    }
}