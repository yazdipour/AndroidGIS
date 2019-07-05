package ir.gfpishro.geosuiteandroidprivateusers.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import ir.gfpishro.geosuiteandroidprivateusers.R;

public class InitActivity extends AppCompatActivity {

    int ALL_PERMISSIONS = 101;

    final String[] permissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);
        if (checkPermissions()) {
            Intent i = new Intent(InitActivity.this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        } else askPermissions();
        findViewById(R.id.btn_retry).setOnClickListener(view -> {
            if (checkPermissions()) {
                finish();
                startActivity(getIntent());
            } else {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + getPackageName()));
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    private boolean checkPermissions() {
        try {
            for (String permission : permissions)
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                    throw new Exception(String.format("No %s Permission", permission));
            return true;
        } catch (Exception e) {
            Snackbar.make(findViewById(R.id.btn_retry), e.getMessage(), Snackbar.LENGTH_LONG);
            return false;
        }
    }

    private void askPermissions() {
        ActivityCompat.requestPermissions(this, permissions, ALL_PERMISSIONS);
    }
}
