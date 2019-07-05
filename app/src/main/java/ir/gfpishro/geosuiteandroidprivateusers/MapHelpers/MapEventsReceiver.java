package ir.gfpishro.geosuiteandroidprivateusers.MapHelpers;

import android.accounts.NetworkErrorException;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.orhanobut.logger.Logger;

import org.oscim.core.GeoPoint;
import org.oscim.event.Gesture;
import org.oscim.event.GestureListener;
import org.oscim.event.MotionEvent;
import org.oscim.layers.Layer;

import java.util.HashMap;
import java.util.List;

import gfp.ir.vtmintegration.analysis.EisAnalysis;
import gfp.ir.vtmintegration.analysis.OfflineFeature;
import gfp.ir.vtmintegration.vtm.Spatialite.SpatiliteVectorLayer;
import ir.gfpishro.geosuiteandroidprivateusers.Activities.InfoActivity;
import ir.gfpishro.geosuiteandroidprivateusers.Activities.MainActivity;
import ir.gfpishro.geosuiteandroidprivateusers.Forms.CPSActivity;
import ir.gfpishro.geosuiteandroidprivateusers.Forms.HSEActivity;
import ir.gfpishro.geosuiteandroidprivateusers.Forms.MarketingActivity;
import ir.gfpishro.geosuiteandroidprivateusers.Forms.RiserActivity;
import ir.gfpishro.geosuiteandroidprivateusers.Activities.StealActivity;
import ir.gfpishro.geosuiteandroidprivateusers.Forms.SoilActivity;
import ir.gfpishro.geosuiteandroidprivateusers.Forms.StealReportActivity;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.DialogHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.PermissionHelper;
import ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.layers.EISLayer;
import ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.layers.RulerLayer;
import ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.utils.PointConverter;
import ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.utils.UTM;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Map.Feature;
import ir.gfpishro.geosuiteandroidprivateusers.Services.ApiHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.SettingsHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.Utils;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Keys;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Map.GeoJson;
import ir.gfpishro.geosuiteandroidprivateusers.Models.SearchType;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Settings;
import ir.gfpishro.geosuiteandroidprivateusers.Models.User;
import ir.gfpishro.geosuiteandroidprivateusers.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static ir.gfpishro.geosuiteandroidprivateusers.Models.SearchType.BG_NUM;
import static ir.gfpishro.geosuiteandroidprivateusers.Models.SearchType.PARCEL_CODE;
import static ir.gfpishro.geosuiteandroidprivateusers.Models.SearchType.PG_NUM;
import static ir.gfpishro.geosuiteandroidprivateusers.Models.SearchType.RISER_NUM;

@SuppressLint("DefaultLocale")
public class MapEventsReceiver extends Layer implements GestureListener, View.OnClickListener {
    private final MainActivity mainActivity;
    private final MapHandler mapHandler;
    private final Settings settings;
    private final LinearLayout.LayoutParams layoutParamsWithFullWeight;
    private AlertDialog dialog;
    private GeoPoint geoPoint;
    private Gson gson = new Gson();

    public MapEventsReceiver(MainActivity context, MapHandler mapHandler) {
        super(mapHandler.getMap());
        this.mainActivity = context;
        this.mapHandler = mapHandler;
        settings = SettingsHandler.getSettings(mainActivity);

        layoutParamsWithFullWeight = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParamsWithFullWeight.weight = 1;
    }

    @Override
    public boolean onGesture(Gesture g, MotionEvent e) {
        geoPoint = mMap.viewport().fromScreenPoint(e.getX(), e.getY());
        if (!(g instanceof Gesture.LongPress)) return false;
        if (mapHandler.rulerMode) {
            RulerLayer rLayer = mapHandler.getRulerLayer();
            if (rLayer != null) rLayer.addPoint(geoPoint, true);
            return true;
        }
        if (mapHandler.navigationMode) {
            mainActivity.goNavigationMode(geoPoint);
            return true;
        } else {
            createDialogButtons(geoPoint);
            return true;
        }
    }

    private void createDialogButtons(final GeoPoint p) {
        @SuppressLint("InflateParams") final View alertDialogView = LayoutInflater.from(mainActivity).inflate(R.layout.dialog_search, null);
        dialog = new AlertDialog.Builder(mainActivity).setView(alertDialogView).setCancelable(true).create();
        final LinearLayout vLayout = alertDialogView.findViewById(R.id.dialog_layout);
        LinearLayout hLayout = alertDialogView.findViewById(R.id.h_layer);
        if (p == null) return;
        SearchType[] options = new SearchType[]{SearchType.PG_NUM, RISER_NUM, SearchType.PARCEL_CODE};
        final SweetAlertDialog sweetAlertDialog = DialogHandler.getDialog(mainActivity,
                SweetAlertDialog.PROGRESS_TYPE, R.string.loading_dialog, true, false, null);
//        Forms Button
        addButtonsForForms(alertDialogView.findViewById(R.id.h_layer2), sweetAlertDialog);
//        __EIS__
        if (PermissionHelper.haveLayerPermission("GasNet_eis"))
            for (final SearchType val : options) {
                Button btn = new Button(new ContextThemeWrapper(mainActivity, R.style.ModernButton), null, R.style.ModernButton);
                btn.setLayoutParams(layoutParamsWithFullWeight);
                btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(mainActivity, R.color.midnight_blue)));
                btn.setText(String.format("%s ناحیه ایزولاسیون", val.getTitle2()));
                btn.setTextColor(Color.WHITE);
                btn.setOnClickListener(v -> {
                    dialog.dismiss();
                    dialog.hide();
                    sweetAlertDialog.show();
                    searchForEIS(p, sweetAlertDialog, val);
                });
                hLayout.addView(btn);
            }
//        ثبت سرقت
        if (map().getMapPosition().zoomLevel >= 17) {
            vLayout.findViewById(R.id.btn_steal).setEnabled(true);
            vLayout.findViewById(R.id.btn_steal).setOnClickListener(this);
        }
//        مسیریابی
        vLayout.findViewById(R.id.btn_nav).setOnClickListener(this);
//        INFO
        vLayout.findViewById(R.id.btn_info).setOnClickListener(this);
//        HSE
        if (PermissionHelper.haveFormPermission(HSEActivity.FORM_CODE)) {
            vLayout.findViewById(R.id.btn_hse).setVisibility(View.VISIBLE);
            vLayout.findViewById(R.id.btn_hse).setOnClickListener(this);
        }
//        Cathode
        if (settings.isCathodeEnabled) {
            vLayout.findViewById(R.id.layer_cathode).setVisibility(View.VISIBLE);
            vLayout.findViewById(R.id.btn_soil).setOnClickListener(this);
            vLayout.findViewById(R.id.btn_cps).setOnClickListener(this);
        }
//        UTM
        vLayout.addView(getUTMTextView(p));
        dialog.show();
    }

    private void addButtonsForForms(final LinearLayout layout, final SweetAlertDialog sweetAlertDialog) {
        List<LayerSchema> layers = mapHandler.getLayersByType(LayerSchema.LayerType.parcel,
                LayerSchema.LayerType.riser);
        for (final LayerSchema l : layers) {
            if (!PermissionHelper.haveLayerPermission(l.getAccessName())) continue;
            Button btn = new Button(layout.getContext());
            btn.setText(String.format("فرم %s", l.getTitle()));
            btn.setLayoutParams(layoutParamsWithFullWeight);
            btn.setOnClickListener(v -> {
                dialog.dismiss();
                dialog.hide();
                try {
                    if (!settings.isOnline || User.getCurrentUser(mainActivity) == null)
                        throw new Exception();
                    String auth = User.getCredential();
                    String geom = Utils.getGeom(geoPoint);
                    Call<GeoJson> call = l.getType() == LayerSchema.LayerType.riser ?
                            ApiHandler.getApi(settings.serverIp).getSearchRiser(auth, null, 1, 1, geom, settings.cityCode) :
                            ApiHandler.getApi(settings.serverIp).getSearchParcel(auth, null, 1, geom, settings.cityCode);
                    call.enqueue(new Callback<GeoJson>() {
                        @Override
                        public void onResponse(Call<GeoJson> call, Response<GeoJson> response) {
                            try {
                                if (!response.isSuccessful() || response.body() == null || response.body().getFeatures().size() == 0)
                                    throw new Exception();
                                List<Feature> features = response.body().getFeatures();
                                Feature feature = features.get(0);
                                String pks = feature.getProperties().getAsJsonObject().get("pk").getAsString();
                                int pk = Integer.parseInt(pks);
                                goToFormsActivity(l, gson.toJson(feature), pk);
                            } catch (Exception e) {
                                onFailure(call, new Throwable());
                            }
                        }

                        @Override
                        public void onFailure(Call<GeoJson> call, Throwable t) {
                            findOfflineRiserAndParcel(l, sweetAlertDialog);
                        }
                    });
                } catch (Exception e) {
                    findOfflineRiserAndParcel(l, sweetAlertDialog);
                }
            });
            layout.addView(btn);
        }
    }

    private void findOfflineRiserAndParcel(LayerSchema l, SweetAlertDialog sweetAlertDialog) {
        try {
            if (!l.isEnable()) throw new Exception();
            Layer mapLayer = map().layers().get(l.getMapIndex());
            if (!(mapLayer instanceof SpatiliteVectorLayer)) throw new Exception();
            OfflineFeature features = ((SpatiliteVectorLayer) mapLayer).getFeatureAt(geoPoint, map().viewport().getMapLimit());
            if (features == null || features.getGeojson() == null)
                throw new Exception(l.getTitle() + "  ");
            else
                goToFormsActivity(l, features.getFeature().toString(), features.getId());
        } catch (Exception e) {
            showError(sweetAlertDialog, new Exception(l.getTitle() + " یافت نشد "), SweetAlertDialog.NORMAL_TYPE);
        }
    }

    private void goToFormsActivity(LayerSchema layer, String featureJson, int id) {
        Class<?> _class = MarketingActivity.class;
        int reqCode = MarketingActivity.REQUEST_CODE;
        int formCode = MarketingActivity.FORM_CODE;
        if (layer.getType() == LayerSchema.LayerType.riser) {
            _class = RiserActivity.class;
            reqCode = RiserActivity.REQUEST_CODE;
            if (!PermissionHelper.haveLayerPermission(RiserActivity.LAYER_NAME)) {
                PermissionHelper.showNoPermissionMessageAlert(mainActivity).show();
                return;
            }
        } else if (!PermissionHelper.haveFormPermission(formCode)) {
            PermissionHelper.showNoPermissionMessageAlert(mainActivity).show();
            return;
        }
        Intent i = new Intent(mainActivity, _class);
        i.putExtra("data", featureJson);
        i.putExtra("geom", Utils.getGeom(geoPoint));
        i.putExtra("id", id);
        mainActivity.startActivityForResult(i, reqCode);
    }

    private void searchForEIS(@NonNull final GeoPoint p,
                              final SweetAlertDialog sweetAlertDialog, final SearchType searchType) {
        try {
            if (!settings.isOnline) throw new NetworkErrorException();
            final String outType = searchType == PARCEL_CODE ? "parcel" : searchType == RISER_NUM ? "riser" : "pg_valve";
            ApiHandler.getApi(settings.serverIp).getValveFromGeom(User.getCredential(), Utils.getGeom(p), outType).enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                    try {
                        if (!response.isSuccessful())
                            throw new NetworkErrorException("ErrorCode:" + response.code());
                        JsonObject result = response.body().getAsJsonObject();
                        String geoJson = result.get("output").getAsJsonObject().toString();
                        String eisGeoJson = result.get("eis").getAsJsonObject().toString();
                        GeoJson eis = gson.fromJson(eisGeoJson, GeoJson.class);
                        JsonObject eisProp = eis.getFeatures().get(0).getProperties().getAsJsonObject();
                        String pk = eisProp.get("pk").getAsString();
                        String type = eisProp.get("type").getAsString();
                        HashMap<String, String> hashMap = new EisAnalysis().getAsHashMap(geoJson, pk, type, eisGeoJson, call.request().url().toString());
                        buildLayer(hashMap, sweetAlertDialog, searchType);
                    } catch (Exception e) {
                        onFailure(call, new Throwable("ServerError:" + response.code()));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                    Logger.e(t, "EIS");
                    buildLayer(doOfflineSearch(p, searchType), sweetAlertDialog, searchType);
                }
            });
        } catch (Exception e) {
            Logger.i("OfflineEIS");
            buildLayer(doOfflineSearch(p, searchType), sweetAlertDialog, searchType);
        }
    }

    private HashMap<String, String> doOfflineSearch(GeoPoint p, SearchType val) {
        EisAnalysis eis = new EisAnalysis(Environment.getExternalStorageDirectory() + Keys.mapsFolder + Keys.offlineDataBase);
        switch (val) {
            case PARCEL_CODE:
                return eis.getParcelByPoint(p.getLongitude(), p.getLatitude());
            case PG_NUM:
                return eis.getValveByPoint(p.getLongitude(), p.getLatitude());
            case RISER_NUM:
                return eis.getRiserByPoint(p.getLongitude(), p.getLatitude());
        }
        return null;
    }

    private void buildLayer(HashMap<String, String> hashMap,
                            final SweetAlertDialog sweetAlertDialog,
                            final SearchType type) {
        try {
            if (hashMap == null || hashMap.get("geojson") == null)
                throw new Exception(type.getTitle2() + " یافت نشد ");
            hashMap.put("type", type.toString());
            Logger.d(hashMap.get("q"));
            int id = type == RISER_NUM ? EISLayer.RISER.jsonId :
                    type == PARCEL_CODE ? EISLayer.PARCEL.jsonId : EISLayer.VALVE.jsonId;
            LayerSchema layer = mapHandler.layers.get(mapHandler.getListIndexById(id));
            hashMap.remove("q");

            String json = gson.toJson(hashMap);

            layer.setUrl(json);

            layer.setEnable(true);

            if (id != EISLayer.PARCEL.jsonId) {
                sweetAlertDialog.changeAlertType(SweetAlertDialog.NORMAL_TYPE);
//            sweetAlertDialog.setTitleText("EIS_ID: " + hashMap.get("eisId"));
                sweetAlertDialog.setTitleText("");
                String contentText = (type == PG_NUM || type == BG_NUM) ? hashMap.get("eisMsg") : EISLayer.getEisValveMessage(hashMap, type.getField());
                sweetAlertDialog.setContentText(contentText);
            } else {
                sweetAlertDialog.cancel();
            }

            mapHandler.generateLayers(layer);
        } catch (Exception e) {
            Logger.e(e, "MER245");
            showError(sweetAlertDialog, e, SweetAlertDialog.NORMAL_TYPE);
        }
    }

    private TextView getUTMTextView(GeoPoint p) {
        UTM utm_b = PointConverter.WGS84_To_UTM(p);
        String utm = String.format("UTM(%s)\n(Lat:%f, Long:%f)", utm_b.toString(), p.getLatitude(), p.getLongitude());
        TextView tv = new TextView(mainActivity);
        tv.setText(utm);
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        return tv;
    }

    private void showError(SweetAlertDialog dialog, Exception e, int type) {
        if (dialog == null)
            dialog = new SweetAlertDialog(mainActivity);
        dialog.changeAlertType(type);
        dialog.setTitleText(e == null ? mainActivity.getString(R.string.error) : e.getMessage());
        dialog.setContentText("");
        if (!dialog.isShowing()) dialog.show();
    }

    @Override
    public void onClick(View view) {
        dialog.dismiss();
        dialog.hide();
        String geom = Utils.getGeom(geoPoint);
        switch (view.getId()) {
            case R.id.btn_steal: {
                if (!PermissionHelper.haveFormPermission(StealReportActivity.FORM_CODE)) {
                    PermissionHelper.showNoPermissionMessageAlert(mainActivity).show();
                    break;
                }
                Intent i = new Intent(mainActivity, StealActivity.class);
                i.putExtra("geom", geom);
                mainActivity.startActivityForResult(i, StealActivity.REQUEST_CODE);
                break;
            }
            case R.id.btn_nav:
                mainActivity.goNavigationMode(geoPoint);
                break;
            case R.id.btn_info: {
                Intent i = new Intent(mainActivity, InfoActivity.class);
                i.putExtra("geom", geom);
                mainActivity.startActivityForResult(i, InfoActivity.REQUEST_CODE);
                break;
            }
            case R.id.btn_hse: {
                if (!PermissionHelper.haveFormPermission(HSEActivity.FORM_CODE)) {
                    PermissionHelper.showNoPermissionMessageAlert(mainActivity).show();
                    break;
                }
                Intent i = new Intent(mainActivity, HSEActivity.class);
                i.putExtra("geom", geom);
                mainActivity.startActivityForResult(i, HSEActivity.REQUEST_CODE);
                break;
            }
            case R.id.btn_soil: {
                if (!PermissionHelper.haveFormPermission(SoilActivity.FORM_CODE)) {
                    PermissionHelper.showNoPermissionMessageAlert(mainActivity).show();
                    break;
                }
                Intent i = new Intent(mainActivity, SoilActivity.class);
                i.putExtra("geom", geom);
                mainActivity.startActivityForResult(i, SoilActivity.REQUEST_CODE);
                break;
            }
            case R.id.btn_cps: {
                if (!PermissionHelper.haveFormPermission(CPSActivity.FORM_CODE)) {
                    PermissionHelper.showNoPermissionMessageAlert(mainActivity).show();
                    break;
                }
                Intent i = new Intent(mainActivity, CPSActivity.class);
                i.putExtra("geom", geom);
                mainActivity.startActivityForResult(i, CPSActivity.REQUEST_CODE);
                break;
            }
        }
    }
}