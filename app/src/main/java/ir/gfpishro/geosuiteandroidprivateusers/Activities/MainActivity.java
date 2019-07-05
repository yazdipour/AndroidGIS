package ir.gfpishro.geosuiteandroidprivateusers.Activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.orhanobut.logger.Logger;

import org.json.JSONObject;
import org.oscim.android.MapView;
//import org.oscim.core.BoundingBox;
import org.oscim.core.GeoPoint;
import org.oscim.core.MapPosition;
import org.oscim.layers.Layer;
import org.oscim.layers.LocationLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.map.Map;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import editormodule.utils.MainApplication;
import gfp.ir.vtmintegration.vtm.SearchLayer;
import gfp.ir.vtmintegration.vtm.Spatialite.SpatiliteVectorLayer;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesWithFallbackProvider;
import ir.gfpishro.geosuiteandroidprivateusers.Controls.MissionCardControl;
import ir.gfpishro.geosuiteandroidprivateusers.Controls.ProgressRingControl;
import ir.gfpishro.geosuiteandroidprivateusers.Forms.MarketingActivity;
import ir.gfpishro.geosuiteandroidprivateusers.Forms.ReportActivity;
import ir.gfpishro.geosuiteandroidprivateusers.Forms.RiserActivity;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.DialogHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.FileUtils;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.PermissionHelper;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.SettingsHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.Utils;
import ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.LayerSchema;
import ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.MapEventsReceiver;
import ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.MapHandler;
import ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.utils.PointConverter;
import ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.utils.WGS84;
import ir.gfpishro.geosuiteandroidprivateusers.Models.CodeInfo;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Keys;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Mission.Mission;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Settings;
import ir.gfpishro.geosuiteandroidprivateusers.Models.User;
import ir.gfpishro.geosuiteandroidprivateusers.R;
import ir.gfpishro.geosuiteandroidprivateusers.Services.ApiHandler;
import ir.gfpishro.geosuiteandroidprivateusers.VM.MainActivityVM;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressLint("DefaultLocale")
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static CounterFab counterFab;
    private MapHandler mapHandler;
    private MapView mapView;
    private MissionBottomSheet missionBottomSheet;
    private MissionCardControl cardSheet;
    private ProgressRingControl customProgressBar;
    private Settings settings;
    private SweetAlertDialog pDialog;
    private final Gson gson = new Gson();
    private User user;
    private MainActivityVM mainActivityVM;

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
//        if (mapScaleBar != null)mapScaleBar.destroy();
        try {
            if (mapView != null) mapView.onDestroy();
            if (mapHandler != null) mapHandler.disposeHopper();
        } catch (Exception ignore) {
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        settings = SettingsHandler.getSettings(this);
        user = User.getCurrentUser(this);
        findViewById(R.id.fab_location).setOnClickListener(this);
        findViewById(R.id.fab_settings).setOnClickListener(this);
        findViewById(R.id.fab_layers).setOnClickListener(this);
        findViewById(R.id.fab_edit).setOnClickListener(this);
        findViewById(R.id.fab_rotate).setOnClickListener(this);
        findViewById(R.id.fab_search).setOnClickListener(this);
        findViewById(R.id.fab_qr).setOnClickListener(this);
        findViewById(R.id.btn_navigation).setOnClickListener(this);
        findViewById(R.id.fab_ruler).setOnClickListener(this);
        findViewById(R.id.fab_coordinate).setOnClickListener(this);
        cardSheet = findViewById(R.id.card_sheet);
        cardSheet.setOnClickListener(this);
        counterFab = findViewById(R.id.fab_mission);
        counterFab.setOnClickListener(this);
        customProgressBar = findViewById(R.id.progressBar);
        //Load Helper
        setupMap();
        //  Load Settings
        loadSettings();
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                .setTitleText("")
                .showCancelButton(true);
    }

    private void setupMap() {
        try {
            mapView = findViewById(R.id.mapView);
            mapHandler = new MapHandler(mapView.map(), this, settings.mapServerIp);
            mapView.map().layers().add(new MapEventsReceiver(this, mapHandler));
            mapHandler.setupMapControls(findViewById(R.id.fab_rotate), findViewById(R.id.tv_coordinate));
            mainActivityVM = new MainActivityVM(gson, mapHandler, user, this);
            mainActivityVM.setupLayers();
            //  LocationLayer
            setupLocationLayer();
            //  MarkerLayer
            mapView.map().layers().add(mapHandler.getMarkerLayer());
        } catch (Exception e) {
            Logger.e(e, "SetupMap");
            Toast.makeText(this, "Error:" + e.getMessage(), Toast.LENGTH_SHORT).show();
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        }
    }

    private void setupLocationLayer() {
        final LocationLayer locationLayer = mapHandler.getLocationLayer();
        mapView.map().layers().add(locationLayer);
        mapHandler.moveTo(settings.getCurrentLocationPoint(), -1, false);
        SmartLocation.with(this)
                .location(new LocationGooglePlayServicesWithFallbackProvider(this))
                .start(location -> {
                    if (location.getLatitude() < 10) return;
                    settings.setCurrentLocation(location.getLatitude(), location.getLongitude());
                    settings.isLocationMocked = SmartLocation.with(MainActivity.this).location().state().isMockSettingEnabled();
                    locationLayer.setPosition(location.getLatitude(), location.getLongitude(), location.getAccuracy());
                    if (!locationLayer.isEnabled()) {
                        mapHandler.moveTo(settings.getCurrentLocationPoint(), -1, false);
                        locationLayer.setEnabled(true);
                    }
                    if (missionBottomSheet != null && !missionBottomSheet.isHidden()
                            && missionBottomSheet.getMission() != null
                            && missionBottomSheet.getMission().getArrivalDate() < 1) {
                        GeoPoint locationGeoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                        double distanceTo = locationGeoPoint.sphericalDistance(missionBottomSheet.getMissionGeoPoint());
                        if (distanceTo < 300)
                            missionBottomSheet.getMission().setArrivalDate((int) Utils.getUnixTime());
                    }
                    mapView.map().events.bind((e, mapPosition) -> {
                        if (e == Map.ANIM_END && !settings.isLocationFound) {
                            mapView.map().animator().animateZoom(2000, 50, 0, 0);
                            settings.isLocationFound = true;
                            SettingsHandler.saveSettings(MainActivity.this);
                        }
                    });
                });
    }

    @Override
    public void onClick(View view) {
        if (mapHandler.rulerMode && view.getId() != R.id.fab_ruler) {
            // lets disable ruler mode first and then click
            findViewById(R.id.fab_ruler).performClick();
            return;
        }
        if (mapHandler.navigationMode && view.getId() != R.id.fab_location && view.getId() != R.id.btn_navigation)
            return;
        final Intent i;
        switch (view.getId()) {
            case R.id.fab_coordinate:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("جست و جوی مختصات");
                View viewInflated = LayoutInflater.from(MainActivity.this).inflate(R.layout.input_coordinate, findViewById(R.id.input_coordinate_root), false);


                final EditText inputX = viewInflated.findViewById(R.id.inputX);
                final EditText inputY = viewInflated.findViewById(R.id.inputY);
                builder.setView(viewInflated);
                builder.setPositiveButton(ir.gfpishro.geosuiteandroidprivateusers.R.string.ok, (dialog, which) -> {
                    dialog.dismiss();
                    String x = inputX.getText().toString();
                    String y = inputY.getText().toString();
                    if (!x.isEmpty() && !y.isEmpty()) {

                        WGS84 wgs84 = PointConverter.UTM_TO_WGS84(Double.valueOf(x), Double.valueOf(y));

                        double cx = wgs84.getLongitude();
                        double cy = wgs84.getLatitude();
                        MapPosition mapPosition = mapView.map().getMapPosition();
                        mapPosition.setPosition(cy, cx);
                        mapView.map().animator().animateTo(1000, mapPosition);

                        LayerSchema searchLayer = new LayerSchema();
                        searchLayer.setEnable(true);
                        searchLayer.setId(1000);
                        searchLayer.setReadOnly(false);
                        searchLayer.setTitle(String.format("%s - %s", x, y));
                        searchLayer.setUrl(
                                "    {\n" +
                                        "      \"type\": \"Feature\",\n" +
                                        "      \"properties\": {},\n" +
                                        "      \"geometry\": {\n" +
                                        "        \"type\": \"Point\",\n" +
                                        "        \"coordinates\": [\n" +
                                        cx + "          ,\n" +
                                        cy + "          \n" +
                                        "        ]\n" +
                                        "      }\n" +
                                        "    }\n");
                        searchLayer.setFormatter("false");
                        searchLayer.setType(LayerSchema.LayerType.search);
                        mapHandler.layers.add(searchLayer);
                        mapHandler.generateLayers(searchLayer);
                        mapHandler.updateMap(false);
                    }
                });
                builder.setNegativeButton(ir.gfpishro.geosuiteandroidprivateusers.R.string.cancel, (dialog, which) -> dialog.cancel());
                builder.show();
                break;
            case R.id.fab_ruler:
                if (mapHandler.rulerMode) {
                    mapHandler.disposeRulerLayer();
                    view.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                } else {
                    mapHandler.initRulerLayer(mapView, (FloatingActionButton) view);
                    view.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryDarkDark)));
                }
                mapHandler.rulerMode = !mapHandler.rulerMode;
            case R.id.fab_rotate:
                MapPosition pos = mapView.map().getMapPosition();
                pos.setBearing(0);
                pos.setTilt(0);
                mapView.map().setMapPosition(pos);
                break;
            case R.id.fab_location:
                if (!settings.isLocationFound)
                    Toast.makeText(this, R.string.err_no_location, Toast.LENGTH_SHORT).show();
                else mapHandler.moveTo(settings.getCurrentLocationPoint(), 17, false);
                break;
            case R.id.card_sheet:
                abortMissionClicked(true, true, missionBottomSheet);
                break;
            case R.id.btn_abort:
                abortMissionClicked(false, true, missionBottomSheet);
                break;
            case R.id.fab_settings:
                i = new Intent(MainActivity.this, SettingsActivity.class);
                startActivityForResult(i, SettingsActivity.REQUEST_CODE);
                break;
            case R.id.fab_mission:
                if (!PermissionHelper.haveFormPermission(MissionsActivity.FORM_CODE))
                    PermissionHelper.showNoPermissionMessageAlert(this).show();
                else
                    startActivityForResult(new Intent(MainActivity.this, MissionsActivity.class), MissionsActivity.REQUEST_CODE);
                break;
            case R.id.fab_layers:
                i = new Intent(MainActivity.this, LayersActivity.class);
                i.putExtra("layers", gson.toJson(mapHandler.layers));
                startActivityForResult(i, LayersActivity.REQUEST_CODE);
                break;
            case R.id.fab_search:
                i = new Intent(MainActivity.this, SearchActivity.class);
                i.putExtra("location", settings.getCurrentLocation());
                startActivityForResult(i, SearchActivity.REQUEST_CODE);
                break;
            case R.id.fab_edit:
                if (PermissionHelper.haveFormPermission(7)) startEditActivity();
                else PermissionHelper.showNoPermissionMessageAlert(this).show();
                break;
            case R.id.fab_m_nav:
                mapHandler.moveTo(missionBottomSheet.getMissionGeoPoint(), 16, true);
                if (!settings.isLocationFound)
                    Toast.makeText(this, R.string.err_no_location, Toast.LENGTH_SHORT).show();
                else try {
                    mapHandler.missionNavigationLayer.navigate(mapHandler,
                            new GeoPoint(settings.getCurrentLocation()[0], settings.getCurrentLocation()[1]),
                            new GeoPoint(missionBottomSheet.getMission().getLat(), missionBottomSheet.getMission().getLon()));
                    mapHandler.missionNavigationLayer.addToMap();
                    //ONLINE
                } catch (Throwable throwable) {
                    Logger.e(throwable, this.getClass().getName());
                    try {
                        if (!settings.isOnline)
                            throw new Exception("No Offline/Online Navigator Found!");
                        mapHandler.navigateTo_Online(
                                settings.serverIp,
                                missionBottomSheet.getMission().getId() + 1000,
                                mapHandler.markerHandler.getMarkerLayer().getItemList().size() - 1,
                                missionBottomSheet.getMission().getName(),
                                settings.getCurrentLocation()[0],
                                settings.getCurrentLocation()[1],
                                missionBottomSheet.getMission().getLat(),
                                missionBottomSheet.getMission().getLon());
                    } catch (Exception e) {
                        Toast.makeText(this, "Error:" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        Logger.e(e, "Online Navigation");
                    }
                }
                break;
            case R.id.btn_stop:
            case R.id.fab_m_stop:
                User.getCurrentUser(this).setBusyWithMission(false);
                i = new Intent(MainActivity.this, ReportActivity.class);
                if (missionBottomSheet == null) return;
                i.putExtra("mission", gson.toJson(missionBottomSheet.getMission()));
                startActivityForResult(i, ReportActivity.REQUEST_CODE);
                break;
            case R.id.fab_qr:
                startActivityForResult(new Intent(MainActivity.this, ScannerActivity.class), SearchActivity.REQUEST_CODE);
                break;
            case R.id.btn_reject:
                if (settings.isOnline) {
                    User.getCurrentUser(this).setBusyWithMission(false);
                    final SweetAlertDialog sweetAlertDialog = DialogHandler.getDialog(this,
                            SweetAlertDialog.PROGRESS_TYPE, R.string.sending, true, false, null);
                    sweetAlertDialog.show();
                    missionBottomSheet.getMission().setStatus(2);
                    ApiHandler.getApi("").postEditMission(User.getCredential(), missionBottomSheet.getMission()).enqueue(new Callback<Mission>() {
                        @Override
                        public void onResponse(Call<Mission> call, Response<Mission> response) {
                            if (!response.isSuccessful()) {
                                onFailure(call, null);
                                return;
                            }
                            abortMissionClicked(false, false, missionBottomSheet);
                            sweetAlertDialog.dismissWithAnimation();
                        }

                        @Override
                        public void onFailure(Call<Mission> call, Throwable t) {
                            sweetAlertDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                            sweetAlertDialog.setTitleText(getString(R.string.error));
                            sweetAlertDialog.setContentText(getString(R.string.error_offline));
                        }
                    });
                } else
                    Snackbar.make(mapView, "برای ارجا دادن باید به شبکه متصل باشید", Snackbar.LENGTH_SHORT).show();
                break;
            case R.id.btn_navigation:
                mainActivityVM.exitNavigationMode(view);
                break;
        }
    }

    private void startEditActivity() {
        String basePath = Environment.getExternalStorageDirectory() + Keys.mapsFolder;
        String mapFileName = "tehranmapsroad.mbtiles";
        File file = new File(basePath + mapFileName);
        if (!FileUtils.doesExistsAndBiggerThan(file, 10)) {
            final SweetAlertDialog dialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE);
            dialog.setTitleText("نقشه موجود نیست");
            dialog.setContentText(mapFileName);
            dialog.show();
            return;
        }
        Intent i = new Intent(MainActivity.this, editormodule.activity.EditActivity.class);
        i.putExtra("location", settings.getCurrentLocation());
        final MainApplication app = (MainApplication) getApplication();
        if (!User.IsUserValid(user))
            startActivityForResult(i, 102);
        else {
            try {
                String json = Utils.getSharedPref(this).getString(Keys.editLayer(user.getId()), "");
                if ("".equals(json)) throw new Exception();
                List<String> layersName = Arrays.asList(MainApplication.LAYERS);
                JsonArray res = gson.fromJson(json, JsonArray.class);
                for (int j = 0; j < res.size() && j < app.OnlineLayer.length; j++) {
                    JsonObject objectLayer = res.get(j).getAsJsonObject();
                    int index = layersName.indexOf(objectLayer.get("type").getAsString());
                    if (index == -1) continue;
                    objectLayer.addProperty("type", "FeatureCollection");
                    app.OnlineLayer[index] = new JSONObject(objectLayer.toString());
                }
            } catch (Exception t) {
                Logger.e(t, this.getClass().getName(), "Opening NextGis Activity");
                Toast.makeText(app, t.getMessage(), Toast.LENGTH_LONG).show();
                startActivityForResult(i, 102);
            } finally {
                startActivityForResult(i, 102);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        settings.zoom = mapView.map().getMapPosition().getZoomLevel();
        if (pDialog.isShowing()) pDialog.dismiss();
        ((FloatingActionsMenu) findViewById(R.id.fab_menu)).collapse();
        customProgressBar.setText(settings.getNetworkMessage());
        if (resultCode == Activity.RESULT_OK) switch (requestCode) {
            case LayersActivity.REQUEST_CODE:
                if (data.hasExtra("zoom")) {
                    try {
                        int index = data.getIntExtra("zoom", 0);
                        Layer layer = mapView.map().layers().get(index);
                        if (layer instanceof SearchLayer)
                            mapHandler.moveTo(((SearchLayer) layer).getCenter(), -1, false);
                        else if (layer instanceof SpatiliteVectorLayer)
                            mapHandler.moveTo(((SpatiliteVectorLayer) layer).getCenter(), -1, false);
                    } catch (Throwable t) {
                        Logger.e(String.valueOf(t));
                    }
                } else
                    mapHandler.manageChangesInLayers(gson.fromJson(data.getStringExtra("layers"), Utils.getListType(LayerSchema.class)));
                break;
            case SearchActivity.REQUEST_CODE:
                String featureJson = data.getStringExtra("feature");
                boolean isFeatureCollection = data.getBooleanExtra("featureCollection", false);
                mainActivityVM.fromSearchActivity(featureJson, isFeatureCollection);
                break;
            case 102:
                String[] geoJson = data.getStringArrayExtra("geojson");
                mainActivityVM.fromEditorActivity(geoJson, mapView);
                break;
            case SettingsActivity.REQUEST_CODE:
                loadSettings();
                if (settings.pingSwitchChanged) {
                    mapHandler.onAndOffLayers(settings.isOnline);
                    settings.pingSwitchChanged = false;
                }
                break;
            case MissionsActivity.REQUEST_CODE:
                String missionJson = data.getStringExtra("mission");
                boolean reportExists = data.getBooleanExtra("reportExists", false);
                fromMissionActivity(missionJson, reportExists);
                break;
            case ReportActivity.REQUEST_CODE:
                fromReportActivity();
                break;
            case MarketingActivity.REQUEST_CODE:
            case RiserActivity.REQUEST_CODE:
                int id = data.getIntExtra("id", 0);
                int type = data.getIntExtra("type", 0);
                String geom = data.getStringExtra("geom");
                String code = data.getStringExtra("code");
                if (type == 0) {
                    Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    File file = FileUtils.getMapCodeFile(id, requestCode == MarketingActivity.REQUEST_CODE ? Keys.marketingCodeDirectory : Keys.riserCode);
                    String d = String.format("%s, %s, %s, %s, %s",
                            code, geom, Utils.getUnixTime(),
                            Utils.unixToPersianDate(0, true, true),
                            User.getCurrentUser(this).getUsername());
                    FileUtils.writeInsideCSV(file, id, d);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                CodeInfo ci = new CodeInfo(geom, id, type, code);
                ApiHandler.getApi("").postCodeInfo(User.getCredential(), ci)
                        .enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (!response.isSuccessful())
                                    onFailure(call, new Throwable("خطا در سرور"));
                                else
                                    Toast.makeText(MainActivity.this, "با موفقیت ثبت شد", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Toast.makeText(MainActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                            }
                        });
                break;
        }
    }

    private void loadSettings() {
        findViewById(R.id.fab_rotate).setVisibility(settings.isRotationEnable ? View.VISIBLE : View.GONE);
        mapView.map().viewport().setMaxZoomLevel(settings.maxZoom);
        mapView.map().viewport().setMinZoomLevel(settings.minZoom);
//        mapView.map().viewport().setMapLimit(new BoundingBox(
//                settings.mapLimitMinY,
//                settings.mapLimitMinX,
//                settings.mapLimitMaxY,
//                settings.mapLimitMaxX));
        mapView.map().viewport().setMaxTilt(settings.isTiltEnable ? 65 : 0);
        if (mapHandler.buildingLayer != null)
            mapHandler.buildingLayer.setEnabled(settings.isTiltEnable);
    }

    private void fromReportActivity() {
        abortMissionClicked(false, false, missionBottomSheet);
        customProgressBar.setText("گزارش " + missionBottomSheet.getMission().getId() + "ذخیره شد");
        customProgressBar.stopSpinning();
        mapHandler.missionNavigationLayer.hide(mapHandler.markerHandler);
    }

    private void fromMissionActivity(String missionJson, boolean reportExists) {
        if (reportExists) {
            Intent i = new Intent(MainActivity.this, ReportActivity.class);
            i.putExtra("mission", missionJson);
            startActivityForResult(i, ReportActivity.REQUEST_CODE);
            return;
        }
        final Mission mission = gson.fromJson(missionJson, Mission.class);
        cardSheet.setVisibility(View.VISIBLE);
        if (missionBottomSheet == null) {
            missionBottomSheet = new MissionBottomSheet();
            missionBottomSheet.setOnClickListener(this);
        }
        missionBottomSheet.setMission(mission);
        try {
            final GeoPoint missionPoint = missionBottomSheet.getMissionGeoPoint();
            String msg = "";
            if (settings.isLocationFound) {
                double distance = missionPoint.sphericalDistance(settings.getCurrentLocationPoint());
                msg = "فاصله " + Utils.getDistanceFormatted(distance, false);
            }
            cardSheet.setTexts(String.format("%s %d", getResources().getString(R.string.tv_mission), mission.getId()), msg);
            mapHandler.missionNavigationLayer.hideMarkers(mapHandler.markerHandler);
            MarkerItem marker = mapHandler.markerHandler.getMarkerItem(missionPoint, mission.getName(), mission.getDescription());
            mapHandler.markerHandler.addItem(marker);
            mapHandler.missionNavigationLayer.navigationMarkers[0] = marker;
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.e("fromMissionActivity ", ex);
        }
        findViewById(R.id.fab_m_nav).performClick();
    }

    private void abortMissionClicked(boolean openIt, boolean ask, final MissionBottomSheet missionBottomSheet) {
        User.getCurrentUser(this).setBusyWithMission(false);
        if (openIt) {
            if (missionBottomSheet != null)
                missionBottomSheet.show(this.getSupportFragmentManager(), missionBottomSheet.getTag());
        } else if (ask) {
            Utils.getConfirmationDialog(this,
                    " آیا مطمئن هستید؟", "", "بله", "خیر",
                    (dialog, id) -> {
                        cardSheet.Close(missionBottomSheet, mapHandler);
                        dialog.dismiss();
                    },
                    null).show();
        } else cardSheet.Close(missionBottomSheet, mapHandler);
    }

    public void goNavigationMode(GeoPoint point) {
        mainActivityVM.goNavigationMode(findViewById(R.id.btn_navigation), point);
    }
}