package ir.gfpishro.geosuiteandroidprivateusers.Activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar;
import com.google.gson.Gson;
import com.nextgis.maplib.api.IGISApplication;
import com.nextgis.maplibui.util.SettingsConstantsUI;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.travijuu.numberpicker.library.NumberPicker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ir.gfpishro.geosuiteandroidprivateusers.Helpers.CacheHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.FileUtils;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.ServiceHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.SettingsHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.StringUtils;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.Utils;
import ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.MapHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Keys;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Map.Feature;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Map.GeoJson;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Map.Province;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Settings;
import ir.gfpishro.geosuiteandroidprivateusers.Models.User;
import ir.gfpishro.geosuiteandroidprivateusers.R;
import ir.gfpishro.geosuiteandroidprivateusers.Services.ApiHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Services.SyncAlarm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsActivity extends AppCompatActivity {
    public static final int REQUEST_CODE = 103;

    private EditText editTextMapIP, editTextServerIP;
    private CrystalRangeSeekbar seekBar;
    private Switch switchTilt, switchRotate, switchCathode, switchHSE;
    private Spinner spinnerCityCode;
    private List<Province> provinces = new ArrayList<>();
    private Settings settings;
    private int secureCount = 0;
    private NumberPicker number_picker_service, number_picker_timeout;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Utils.getConfirmationDialog(this,
                "تنظیمات ذخیره نشده",
                "",
                getString(R.string.exit_with_no_save),
                getString(R.string.save),
                (dialog, id) -> {
                    setResult(Activity.RESULT_CANCELED, getIntent());
                    finish();
                },
                (dialog, which) -> {
                    findViewById(R.id.btn_save).performClick();
                    dialog.dismiss();
                }).show();
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activityx_settings);
        settings = SettingsHandler.getSettings(SettingsActivity.this);
        bind();
        findViewById(R.id.btn_save).setOnClickListener(BindBack);
        findViewById(R.id.btn_folder).setOnClickListener(v -> {
            String directory = Environment.getExternalStorageDirectory() + Keys.mapsFolder + Keys.mapCodeFolder;
            File folder = new File(directory);
            if (!folder.exists()) folder.mkdirs();
            Uri selectedUri = Uri.parse(directory);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(selectedUri, "resource/folder");
            if (intent.resolveActivityInfo(getPackageManager(), 0) != null) startActivity(intent);
            else Toast.makeText(this, "این قابلیت غیر فعال است", Toast.LENGTH_SHORT).show();
        });
        //  Updater
        findViewById(R.id.btn_downloader).setOnClickListener(v -> {
            if (settings.isOnline)
                startActivity(new Intent(SettingsActivity.this, DownloaderActivity.class));
            else {
                Snackbar snackbar = Snackbar.make(findViewById(R.id.btn_downloader), getString(R.string.error_offline), Snackbar.LENGTH_SHORT);
                snackbar.getView().setBackgroundColor(Color.parseColor("#ff0000"));
                snackbar.show();
            }
        });
        findViewById(R.id.btn_tutorial).setOnClickListener(v -> startActivity(new Intent(SettingsActivity.this, TutorialActivity.class)));
        //  Editor Settings
        // If Map Not Exist - No Editor Setting Button
        File file = new File(Environment.getExternalStorageDirectory() + Keys.mapsFolder + "tehranmapsroad.mbtiles");
        if (!FileUtils.doesExistsAndBiggerThan(file, 10))
            findViewById(R.id.btn_subsetting).setVisibility(View.GONE);
        else findViewById(R.id.btn_subsetting).setOnClickListener(v -> {
            try {
                ((IGISApplication) getApplication()).showSettings(SettingsConstantsUI.ACTION_PREFS_GENERAL);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void bind() {
        try {
            ((TextView) findViewById(R.id.user_info1)).setText(String.format("%s %s", User.getCurrentUser(this).getFirstName(), User.getCurrentUser(this).getLastName()));
            ((TextView) findViewById(R.id.user_info2)).setText(String.format("شما پرسنلی: %s", User.getCurrentUser(this).getPersonalId()));
            ((TextView) findViewById(R.id.user_info3)).setText(String.format("کد دستگاه: %s", User.getCurrentUser(this).getmId(this)));
        } catch (Exception e) {
            ((TextView) findViewById(R.id.user_info1)).setText(R.string.error_no_user);
        }
        ((TextView) findViewById(R.id.tv_version)).setText(String.format("نسخه: %s", Utils.getPackageVersion(this)));
        //Logout
        findViewById(R.id.btn_logout).setOnClickListener(logOut);
        // Safe Mode
        findViewById(R.id.btn_hidden).setOnClickListener(v -> {
            if (++secureCount == 5 || MapHandler.godMode) goGodMode();
        });
        // EditText (IP Addresses)
        editTextMapIP = findViewById(R.id.ip_et);
        editTextMapIP.setText(settings.mapServerIp);
        editTextServerIP = findViewById(R.id.ip_server_et);
        editTextServerIP.setText(settings.serverIp);
        //Switch
        switchTilt = findViewById(R.id.switch_tilt);
        switchCathode = findViewById(R.id.switch_cathode);
        switchHSE = findViewById(R.id.switch_hse);
        switchHSE.setChecked(settings.isHSEEnabled);
        switchCathode.setChecked(settings.isCathodeEnabled);
        switchRotate = findViewById(R.id.switch_rotate);
        switchTilt.setChecked(settings.isTiltEnable);
        switchRotate.setChecked(!settings.isRotationEnable);
        final Switch switchOnline = findViewById(R.id.switch_online);
        switchOnline.setChecked(settings.isOnline);
        switchOnline.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settings.pingSwitchChanged = true;
            if (!isChecked) {
                settings.isOnline = false;
                return;
            }
            final SweetAlertDialog dialog = new SweetAlertDialog(SettingsActivity.this, SweetAlertDialog.PROGRESS_TYPE);
            dialog.setTitleText("تلاش برای اتصال").setCancelable(true);
            dialog.showCancelButton(true);
            dialog.show();
            ApiHandler.getApi(settings.serverIp).ping().enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    boolean online = settings.isOnline = response.isSuccessful();
                    dialog.changeAlertType(online ? SweetAlertDialog.SUCCESS_TYPE : SweetAlertDialog.ERROR_TYPE);
                    dialog.setTitleText(settings.getNetworkMessage());
                    switchOnline.setChecked(online);
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    switchOnline.setChecked(false);
                    dialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    dialog.setTitleText(SettingsHandler.getSettings(null).getNetworkMessage());
                }
            });
        });
        //Seeker
        final TextView tv_max = findViewById(R.id.textMax), tv_min = findViewById(R.id.textMin);
        seekBar = findViewById(R.id.seekbar);
        seekBar.setOnRangeSeekbarChangeListener((minValue, maxValue) -> {
            tv_max.setText(String.valueOf(maxValue));
            tv_min.setText(String.valueOf(minValue));
        });
        seekBar.setMinStartValue(settings.minZoom)
                .setMaxStartValue(settings.maxZoom)
                .setGap(5)
                .apply();
        //NumberPicker
        number_picker_service = findViewById(R.id.number_picker_service);
        number_picker_timeout = findViewById(R.id.number_picker_timeout);
        number_picker_service.setValue(settings.syncIntervalSecond);
        number_picker_timeout.setValue(settings.timeOutSecond);
        //Spinner
        spinnerCityCode = findViewById(R.id.spinner_city);
        try {
            List<String> provincesName = new ArrayList<>();
            String json = StringUtils.getRawString(this, R.raw.provinces);
            Gson gson = new Gson();
            List<Feature> features = gson.fromJson(json, GeoJson.class).getFeatures();
            for (Feature feature : features) {
                Province province = gson.fromJson(feature.getProperties(), Province.class);
                provinces.add(province);
                provincesName.add(province.getOstnName());
            }
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, provincesName);
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            boolean canChangeCityCode = (User.getCurrentUser(this) != null && User.getCurrentUser(this).isAdmin());
            spinnerCityCode.setEnabled(canChangeCityCode);
            spinnerCityCode.setClickable(canChangeCityCode);
            spinnerCityCode.setAdapter(spinnerArrayAdapter);
            for (int i = 0; i < provinces.size(); i++)
                if (provinces.get(i).getCityCode().equals(settings.cityCode)) {
                    spinnerCityCode.setSelection(i);
                    break;
                }
            spinnerCityCode.setOnItemSelectedListener(new OnItemSelectedListener() {
                boolean firstTimeEventCall = true;
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    // Province
                    if (firstTimeEventCall) {
                        firstTimeEventCall = false;
                        return;
                    }
//                    int selectedIndex = spinnerCityCode.getSelectedItemPosition();
                    Province province = provinces.get(position);
                    settings.mapLimitMinX = province.getMinx();
                    settings.mapLimitMinY = province.getMiny();
                    settings.mapLimitMaxX = province.getMaxx();
                    settings.mapLimitMaxY = province.getMaxy();
                    settings.cityCode = province.getCityCode();
                    SettingsHandler.saveSettings(SettingsActivity.this);
                    resetRequire(false);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private View.OnClickListener logOut = v -> {
        User.resetCurrentUser(SettingsActivity.this);
        SettingsHandler.resetSettings();
        Intent i = new Intent(SettingsActivity.this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    };

    private void goGodMode() {
        MapHandler.godMode = true;
        Snackbar.make(spinnerCityCode, "God Mode", Snackbar.LENGTH_SHORT).show();
        spinnerCityCode.setClickable(true);
        spinnerCityCode.setEnabled(true);
        editTextMapIP.setEnabled(true);
        editTextMapIP.setInputType(InputType.TYPE_CLASS_TEXT);
        editTextMapIP.setFocusable(true);
        editTextMapIP.setFocusableInTouchMode(true);
        editTextServerIP.setEnabled(true);
        editTextServerIP.setInputType(InputType.TYPE_CLASS_TEXT);
        editTextServerIP.setFocusable(true);
        editTextServerIP.setFocusableInTouchMode(true);
        findViewById(R.id.layer_more1).setVisibility(View.VISIBLE);
        findViewById(R.id.layer_more2).setVisibility(View.VISIBLE);
        View btn_clear = findViewById(R.id.btn_clear);
        btn_clear.setVisibility(View.VISIBLE);
        btn_clear.setOnClickListener(v -> {
            final SweetAlertDialog dialog = new SweetAlertDialog(SettingsActivity.this, SweetAlertDialog.WARNING_TYPE);
            dialog.setTitleText(getString(R.string.are_you_sure)).setConfirmText("باشه").setCancelText("نه").show();
            dialog.setConfirmClickListener(sweetAlertDialog -> {
                Intent i = new Intent(SettingsActivity.this, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                CacheHandler.getHandler(SettingsActivity.this).wipeDb();
                Utils.getSharedPref(SettingsActivity.this).edit().clear().apply();
                startActivity(i);
                finish();
            });
            dialog.show();
        });
    }

    private View.OnClickListener BindBack = v -> {
        boolean resetRequire = false;
        settings.maxZoom = seekBar.getSelectedMaxValue().intValue();
        settings.minZoom = seekBar.getSelectedMinValue().intValue();
        settings.isRotationEnable = !switchRotate.isChecked();
        settings.isTiltEnable = switchTilt.isChecked();
        settings.isHSEEnabled = switchHSE.isChecked();
        settings.isCathodeEnabled = switchCathode.isChecked();
        String sip = editTextServerIP.getText().toString().replaceAll(" ", "");
        if (!settings.serverIp.equals(sip)) {
            ApiHandler.resetApi();
            settings.serverIp = StringUtils.farsiNumbersToEnglish(sip);
        }
        if (settings.syncIntervalSecond != number_picker_service.getValue()) {
            settings.timeOutSecond = number_picker_timeout.getValue();
            ApiHandler.resetApi();
            ApiHandler.timeOut = settings.timeOutSecond;
        }
        if (settings.syncIntervalSecond != number_picker_service.getValue()) {
            settings.syncIntervalSecond = number_picker_service.getValue();
            ServiceHandler.stopAlarm(SettingsActivity.this, SyncAlarm.class, SyncAlarm.REQUEST_CODE);
            ServiceHandler.startAlarm(SettingsActivity.this, SyncAlarm.class, settings.syncIntervalSecond, SyncAlarm.REQUEST_CODE);
        }
        if (!editTextMapIP.getText().toString().equals(settings.mapServerIp)) {
            settings.mapServerIp = editTextMapIP.getText().toString().replaceAll(" ", "");
            resetRequire = true;
        }

        SettingsHandler.saveSettings(SettingsActivity.this);
        if (resetRequire) resetRequire(true);
        else {
            Intent i = new Intent();
            setResult(Activity.RESULT_OK, i);
            finish();
        }
    };

    private void resetRequire(boolean onCancelGoBack) {
        final SweetAlertDialog dialog = new SweetAlertDialog(SettingsActivity.this, SweetAlertDialog.WARNING_TYPE);
        dialog.setTitleText(getString(R.string.msg_restart)).setConfirmText("باشه").setCancelText("بعدا").show();
        dialog.setConfirmClickListener(sweetAlertDialog -> {
            Intent i = new Intent(SettingsActivity.this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        });
        if (onCancelGoBack) dialog.setCancelClickListener(sweetAlertDialog -> {
            Intent i = new Intent();
            setResult(Activity.RESULT_OK, i);
            finish();
        });
        dialog.show();
    }
}
