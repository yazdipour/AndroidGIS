package ir.gfpishro.geosuiteandroidprivateusers.Forms;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.orhanobut.logger.Logger;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import org.oscim.core.GeoPoint;

import java.util.ArrayList;
import java.util.List;

import ir.gfpishro.geosuiteandroidprivateusers.Controls.ControlGenerator;
import ir.gfpishro.geosuiteandroidprivateusers.Controls.UiChildControl;
import ir.gfpishro.geosuiteandroidprivateusers.Controls.UiControl;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.ImageUtils;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.MissionHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.SettingsHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.StringUtils;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.Utils;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Keys;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Mission.Mission;
import ir.gfpishro.geosuiteandroidprivateusers.Models.User;
import ir.gfpishro.geosuiteandroidprivateusers.R;

public class ReportActivity extends AppCompatActivity implements IPickResult {
    public static final int REQUEST_CODE = 105;
    private MissionHandler missionHandler;
    private LinearLayout motherLayer;
    private List<UiControl> uiControls = new ArrayList<>();
    private Mission mission;
    private UiControl missionMeta = new UiControl(0, "meta_mid"),
            imageMeta = new UiControl(-1, "meta_img"),
            timeMeta = new UiControl(-2, "meta_time"),
            userIdMeta = new UiControl(-3, "meta_uid");

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Utils.getConfirmationDialog(this,
                getString(R.string.are_u_sure),
                getString(R.string.report_not_saved),
                getString(R.string.exit_with_no_save),
                getString(R.string.stay),
                (dialog, id) -> {
                    setResult(Activity.RESULT_CANCELED, getIntent());
                    finish();
                }, null).show();
    }
    //  END BackPressed

    /**
     * CAMERA
     * Grant Permission to use camera
     * And then Open up Camera Handler
     */
    private void OpenCamera() {
        @SuppressLint("RtlHardcoded") PickSetup setup = new PickSetup()
                .setTitle("انتخاب تصویر")
                .setCancelText("بستن")
                .setCameraButtonText("دوربین")
                .setIconGravity(Gravity.RIGHT)
                .setVideo(false)
                .setMaxSize(500)
                .setGalleryButtonText("گالری");
        PickImageDialog.build(setup)
                .show(this);
    }

    private void rmCameraImage() {
        findViewById(R.id.tv_rm).setVisibility(View.GONE);
        ImageView img = findViewById(R.id.cameraImage);
        img.setImageBitmap(null);
        img.setVisibility(View.GONE);
        ((TextView) findViewById(R.id.cameraTextView)).setText(R.string.image);
        imageMeta.setControlType("image");
    }

    @Override
    public void onPickResult(PickResult pickResult) {
        if (pickResult.getError() == null) {
            ((ScrollView) findViewById(R.id.mainScrollView)).fullScroll(ScrollView.FOCUS_DOWN);
            findViewById(R.id.tv_rm).setVisibility(View.VISIBLE);
            Bitmap imageBitmap = pickResult.getBitmap();
            ImageView img = findViewById(R.id.cameraImage);
            img.setImageBitmap(imageBitmap);
            img.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.cameraTextView)).setText(Utils.getUnixTime() + ".jpg");
            imageMeta.setId(-1);
            assert imageBitmap != null;
            imageMeta.setControlType("image");
            imageMeta.setTitle(ImageUtils.convertBase64(imageBitmap, 80));
        } else {
            Toast.makeText(this, pickResult.getError().getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    //  END CAMERA

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        missionHandler = new MissionHandler(this);
        motherLayer = findViewById(R.id.mother_layer);
        User user = User.getCurrentUser(this);
        if (user == null || user.getId() == -1) {
            Snackbar.make(motherLayer, "خطا. دوباره لاگین کنید", Snackbar.LENGTH_LONG).show();
            return;
        }
        try {
            mission = new Gson().fromJson(getIntent().getStringExtra("mission"), Mission.class);
            boolean reportExists = getIntent().getBooleanExtra("reportExists", false);
            String json = "", missingFields = "";
            if (reportExists) {
                missingFields = Utils.getSharedPref(this).getString(Keys.missingInReport(mission.getId()), "");
                json = Utils.getSharedPref(this).getString(Keys.report(mission.getId()), "");
            } else checkIsInArea(mission, motherLayer);
            if (json.length() < 2) json = StringUtils.getRawString(this, R.raw.report);
            setupDynamicUI(json);
            if (missingFields.length() > 2) markMissingFields(missingFields);
        } catch (Exception e) {
            Logger.e(e, this.getClass().getName());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        //0. add mission id
        missionMeta.setTitle(String.valueOf(mission.getId()));
        uiControls.add(missionMeta);
        //-1.add image
        uiControls.add(imageMeta);
        //-2.add time
        uiControls.add(timeMeta);
        //-3.add user
        uiControls.add(userIdMeta);
        //UI
        findViewById(R.id.tv_rm).setOnClickListener(v -> rmCameraImage());
        findViewById(R.id.cameraBtn).setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ReportActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
            } else OpenCamera();
        });
        findViewById(R.id.submit_report_btn).setOnClickListener(view -> {
            if (bindBack()) Submit();
            else {
                Snackbar snackbar = Snackbar.make(view, getResources().getString(R.string.error_null_editview), Snackbar.LENGTH_LONG);
                snackbar.getView().setBackgroundColor(Color.parseColor("#ff0000"));
                snackbar.show();
            }
        });
    }

    private void checkIsInArea(Mission mission, View view) {
        GeoPoint missionPoint = new GeoPoint(mission.getLat(), mission.getLon());
        GeoPoint currentLocation = SettingsHandler.getSettings(this).getCurrentLocationPoint();
//        double distance = missionPoint.sphericalDistance(currentLocation);
//        if (distance > 500) {
//            Snackbar snackbar = Snackbar.make(view, String.format("شما نزدیک محل حادثه نیستید (فاصله تقریبی %.2f متر)", distance), Snackbar.LENGTH_INDEFINITE);
//            snackbar.getView().setBackgroundColor(Color.parseColor("#f39c12"));
//            snackbar.show();
//            findViewById(R.id.submit_report_btn).setVisibility(View.INVISIBLE);
//        }
    }

    private void markMissingFields(String missingFields) {
        List<Integer> fields = new Gson().fromJson(missingFields, Utils.getListType(Integer.class));
        for (UiControl ui : uiControls) {
            if (ui.getId(false) == null || !fields.contains(ui.getId(true))) continue;
            View view = findViewById(ui.getId(false));
            view.setBackgroundColor(Color.parseColor("#7fe74c3c"));
        }
    }

    private void setupDynamicUI(String json) {
        uiControls = new Gson().fromJson(json, Utils.getListType(UiControl.class));
        int shift = motherLayer.getChildCount();
        for (UiControl ui : uiControls) {
            if (ui.getTitle().length() > 0) {
                TextView tv = ControlGenerator.createHeader(this, ui.getTitle());
                if (ui.getUiChildControls() == null) {
                    tv.setBackgroundColor(Color.parseColor("#cccccc"));
                    tv.setTextSize(18);
                }
                motherLayer.addView(tv, motherLayer.getChildCount() - shift);
            }
            if (ui.getUiChildControls() != null) {
                if (ui.getId(true).equals(1))
                    ui.getUiChildControls().get(0).setText(Utils.unixToPersianDate(mission.getEventDate(), true, true));
                else if (ui.getId(true).equals(2))
                    ui.getUiChildControls().get(0).setText(mission.getPhone());
                else if (ui.getId(true).equals(3))
                    ui.getUiChildControls().get(0).setText(Utils.unixToPersianDate(mission.getNoticeDate(), true, true));
                else if (ui.getId(true).equals(4))
                    ui.getUiChildControls().get(0).setText(mission.getEmergencyZone().getName());
                else if (ui.getId(true).equals(5))
                    ui.getUiChildControls().get(0).setText(mission.getCity());
                motherLayer.addView(ui.getControl(this), motherLayer.getChildCount() - shift);
            }
        }
    }

    private boolean bindBack() {
        boolean isOkToSend = true;
        //bind others
        for (UiControl ui : uiControls) {
            if (ui.getId(false) == null) continue;
            if (ui.getId(false) <= 0) continue;
            View view = findViewById(ui.getId(false));
            ui.bindControl(view);
            if (ui.getUiChildControls() == null) continue;
            if (ui.getUiChildControls().get(0).getRequired()) {
                boolean needToCheck;
                view.setBackgroundColor(Color.TRANSPARENT);
                int idReq = ui.getUiChildControls().get(0).ifIdReq;
                int idNotReq = ui.getUiChildControls().get(0).ifIdNotReq;
                //logic REQ
                if (idNotReq != -1 && checkValue(idNotReq)) continue;
                else needToCheck = idReq == -1 || checkValue(idReq);
                // Conclusion
                if (needToCheck && !checkValue(ui)) {
                    view.setBackgroundColor(Color.parseColor("#7fe74c3c"));
                    isOkToSend = false;
                }
            }
        }
        fillMetaData();
        return isOkToSend;
    }

    private void fillMetaData() {
        //(-2).add time
        timeMeta.setTitle("arriveTime/sendTime");
        ArrayList<UiChildControl> timesChild = new ArrayList<>();
        //arrival Time
        timesChild.add(new UiChildControl(String.valueOf(mission.getArrivalDate())));
        //send Time
        timesChild.add(new UiChildControl(String.valueOf(Utils.getUnixTime())));
        mission.setEndDate((int) Utils.getUnixTime());
        timeMeta.setUiChildControls(timesChild);
        //-3. User Id
        try {
            userIdMeta.setTitle(User.getCurrentUser(this).getId().toString());
        } catch (Exception ignored) {
            Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkValue(UiControl ui) {
        switch (ui.getControlType()) {
            case EDITTEXT:
                return ui.getUiChildControls().get(0).getText().length() > 0;
            case CHECHBOX:
                return ui.getUiChildControls().get(0).getChecked();
        }
        return false;
    }

    private boolean checkValue(Integer id) {
        for (UiControl ui : uiControls) {
            if (ui.getId(true) == null) continue;
            if (ui.getId(true).equals(id)) {
                if (findViewById(ui.getId(false)) instanceof CheckBox)
                    return ((CheckBox) findViewById(ui.getId(false))).isChecked();
                if (findViewById(ui.getId(false)) instanceof RadioButton)
                    return ((RadioButton) findViewById(ui.getId(false))).isChecked();
                if (findViewById(ui.getId(false)) instanceof EditText)
                    return ((EditText) findViewById(ui.getId(false))).getText().length() > 0;
            }
        }
        return false;
    }

    private void Submit() {
        final SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        try {
            missionHandler.cacheReport(User.getCurrentUser(this), uiControls);
            Utils.getSharedPref(this).edit().putString(
                    Keys.report(mission.getId()),
                    new Gson().toJson(uiControls))
                    .apply();
            dialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
            dialog.setTitleText(getString(R.string.msg_report_cached)).setConfirmText("OK").show();
            dialog.setConfirmClickListener(sweetAlertDialog -> {
                Intent i = new Intent();
                setResult(Activity.RESULT_OK, i);
                finish();
            });
        } catch (Exception e) {
            Logger.e(e, getClass().getCanonicalName(), uiControls);
            //Retry Dialog
            dialog.changeAlertType(SweetAlertDialog.WARNING_TYPE);
            dialog.setTitleText("Retry?")
                    .setContentText("FAILED on Caching")
                    .setConfirmText("Yes, retry")
                    .setCancelText("No, just cache")
                    .showCancelButton(true)
                    .setConfirmClickListener(sDialog -> {
                        sDialog.dismissWithAnimation();
                        Submit();
                    })
                    .setCancelClickListener(sDialog -> {
                        sDialog.dismiss();
                        setResult(Activity.RESULT_CANCELED, new Intent());
                        finish();
                    })
                    .show();
        }
    }
}
