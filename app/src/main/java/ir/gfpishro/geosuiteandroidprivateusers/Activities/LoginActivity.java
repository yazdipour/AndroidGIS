package ir.gfpishro.geosuiteandroidprivateusers.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import ir.gfpishro.geosuiteandroidprivateusers.Helpers.FileUtils;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.LogHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.PermissionHelper;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.ServiceHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.SettingsHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.StringUtils;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.Utils;
import ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.LayerSchema;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Keys;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Settings;
import ir.gfpishro.geosuiteandroidprivateusers.Models.User;
import ir.gfpishro.geosuiteandroidprivateusers.R;
import ir.gfpishro.geosuiteandroidprivateusers.Services.ApiHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Services.LocationAlarm;
import ir.gfpishro.geosuiteandroidprivateusers.Services.LocationService;
import ir.gfpishro.geosuiteandroidprivateusers.Services.SyncAlarm;
import ir.gfpishro.geosuiteandroidprivateusers.Services.SyncService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private CheckBox cb_save;
    private Settings settings;
    private String serverIp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ServiceHandler.stopAlarm(this, SyncAlarm.class, SyncAlarm.REQUEST_CODE);
        ServiceHandler.stopAlarm(this, LocationAlarm.class, LocationAlarm.REQUEST_CODE);
        ((TextView) findViewById(R.id.tv_version)).setText(String.format("نسخه: %s", Utils.getPackageVersion(this)));
        findViewById(R.id.btn_login).setOnClickListener(this);
        findViewById(R.id.logo_img).setOnClickListener(this);
        cb_save = findViewById(R.id.cb_save);
        try {
            ((TextView) findViewById(R.id.tv_mid)).setText(User.getPhoneId(this));
        } catch (Exception e) {
            Toast.makeText(this, "Error on get Mobile ID", Toast.LENGTH_SHORT).show();
        }
        if (Utils.getSharedPref(this).getBoolean(getString(R.string.save_auth_key), false)) {
            cb_save.setChecked(true);
            String[] lastUser = User.getLastUser(this);
            if (lastUser[0].length() > 1) {
                ((EditText) findViewById(R.id.input_email)).setText(lastUser[0]);
                EditText pass = findViewById(R.id.input_password);
                pass.setText(lastUser[1]);
                pass.requestFocus();
            }
        }
        findMissingFiles();
        final int[] counter = {0};
        findViewById(R.id.dev).setOnClickListener(v -> {
            if (++counter[0] > 5) {
                byte[] data = Base64.decode(getString(R.string.external_msg), Base64.DEFAULT), data2 = Base64.decode(getString(R.string.external_msg2), Base64.DEFAULT);
                new AlertDialog.Builder(LoginActivity.this).setPositiveButton("Ok", null)
                        .setCancelable(false).setMessage(new String(data, StandardCharsets.UTF_8))
                        .setTitle(new String(data2, StandardCharsets.UTF_8)).create().show();
            }
        });
        settings = SettingsHandler.getSettings(this);
    }

    private void findMissingFiles() {
        StringBuilder msg = new StringBuilder();
        try {
            ArrayList<String> paths = new ArrayList<>();
            String json = StringUtils.getRawString(this, R.raw.layers);
            LayerSchema[] layers = new Gson().fromJson(json, LayerSchema[].class);
            for (LayerSchema layer : layers) {
                if (layer.getType() != LayerSchema.LayerType.online)
                    if (!paths.contains(layer.getUrl()) && layer.getUrl().charAt(0) != '/')
                        paths.add(layer.getUrl());
            }
            String basePath = Environment.getExternalStorageDirectory() + Keys.mapsFolder;
            for (String path : paths) {
                File file = new File(basePath + path);
                if (!FileUtils.doesExistsAndBiggerThan(file, 5))
                    msg.append("\n").append(path);
            }

        } catch (Exception ignored) {
        }
        if (msg.length() < 3) return;
        TextView tv = findViewById(R.id.tv_error);
        tv.setText(String.format("%s%s", getString(R.string.err_file_no_found), msg));
        tv.setOnClickListener(this);
    }

    private void tryLogin(String user, String pass) {
        final String userName = StringUtils.farsiNumbersToEnglish(user);
        final String password = StringUtils.farsiNumbersToEnglish(pass);
        final String auth = User.getCredential(userName, password);
        final SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        dialog.setTitleText("Loading").setCancelable(true);
        dialog.showCancelButton(true);
        dialog.show();
        ApiHandler.resetApi();
        ApiHandler.timeOut = settings.timeOutSecond;
        ApiHandler.getApi(serverIp == null ? settings.serverIp : serverIp).getUser(auth).enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                try {
                    if (response.isSuccessful()) {
                        Utils.getSharedPref(LoginActivity.this).edit().putBoolean(getString(R.string.save_auth_key), cb_save.isChecked()).apply();
                        User user = response.body();
                        if (user == null) throw new Exception("ServerCode:" + response.code());
                        user.setCurrentUser(LoginActivity.this, user);
                        user.cacheUser(LoginActivity.this, user);
                        goMainActivity(dialog);
                    } else {
                        switch (response.code()) {
                            case 403:
                                throw new IllegalAccessException("نام کاربری یا رمز عبور ناصحیح است");
                            case 404:
                                throw new IllegalAccessException("کاربر یافت نشد");
                            case 500:
                                throw new IllegalAccessException("خطای داخلی سرور");
                            default:
                                throw new IllegalAccessException(null);
                        }
                    }
                } catch (Exception ee) {
                    onFailure(call, new IllegalAccessException(ee.getMessage()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, Throwable t) {
                if (t == null) {
                    dialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    dialog.setTitleText(getString(R.string.error)).setContentText("دوباره تلاش کنید");
                } else if (t instanceof IllegalAccessException) {
                    Logger.e(t, "Login Error");
                    dialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    dialog.setTitleText(getString(R.string.error)).setContentText(t.getMessage());
                } else {
                    try {
                        User user = User.getCachedUser(LoginActivity.this, userName, password);
                        if (user == null) throw new Exception();
                        user.setCurrentUser(LoginActivity.this, user);
                        goMainActivity(dialog);
                    } catch (Exception e) {
                        onFailure(call, null);
                    }
                }
            }
        });
    }

    private void goMainActivity(SweetAlertDialog dialog) {
        LogHandler.init();
        User user = User.getCurrentUser(this);
        ApiHandler.resetApi();
        SettingsHandler.resetSettings();
        if (serverIp != null) SettingsHandler.getSettings(this).serverIp = serverIp;
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PermissionHelper.getAccessibilityLayers(i, this, ApiHandler.getApi(SettingsHandler.getSettings(this).serverIp), user);
        PermissionHelper.getAccessibilityForms(this, ApiHandler.getApi(SettingsHandler.getSettings(this).serverIp), user);
        ServiceHandler.stopService(this, SyncService.class);
        ServiceHandler.stopService(this, LocationService.class);
        ServiceHandler.startAlarm(this, SyncAlarm.class, SettingsHandler.getSettings(this).syncIntervalSecond, SyncAlarm.REQUEST_CODE);
        ServiceHandler.startAlarm(this, LocationAlarm.class, LocationAlarm.INTERVAL_SEC, LocationAlarm.REQUEST_CODE);
        dialog.dismiss();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                EditText username = findViewById(R.id.input_email);
                EditText pass = findViewById(R.id.input_password);
                tryLogin(username.getText().toString().trim(), pass.getText().toString());
                break;
            case R.id.logo_img:
                getDialogServerIP();
                break;
            case R.id.tv_error:
                String msg2 = "برای گرفتن فایل ها به قسمت تنظیمات>بروزرسانی رفته و فایلها را دریافت نمایید.";
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setMessage(msg2)
                        .setCancelable(true)
                        .setPositiveButton("باشه", null);
                AlertDialog alert = builder.create();
                alert.show();
                break;
        }
    }

    private void getDialogServerIP() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        EditText edittext = new EditText(this);
        edittext.setText(serverIp == null ? settings.serverIp : serverIp);
        edittext.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        alert.setTitle("آدرس سرور را وارد کنید");
        alert.setView(edittext);
        alert.setPositiveButton("ذخیره", (dialog, whichButton) -> {
            String output = edittext.getText().toString().replaceAll(" |,", "");
            serverIp = StringUtils.farsiNumbersToEnglish(output);
        });
        alert.setNegativeButton("ببند", null);
        alert.show();
    }
}
