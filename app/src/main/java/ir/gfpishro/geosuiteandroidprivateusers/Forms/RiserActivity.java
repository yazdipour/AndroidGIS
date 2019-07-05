package ir.gfpishro.geosuiteandroidprivateusers.Forms;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.orhanobut.logger.Logger;

import java.io.File;

import ir.gfpishro.geosuiteandroidprivateusers.Activities.ScannerActivity;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.FileUtils;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.Utils;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Keys;
import ir.gfpishro.geosuiteandroidprivateusers.Models.User;
import ir.gfpishro.geosuiteandroidprivateusers.R;

public class RiserActivity extends AppCompatActivity {
    public static final int REQUEST_CODE = 107;
    public static final String LAYER_NAME = "gasnet_serviceriser";
    private EditText editText;
    private int id = 0;
    private String geom = "";

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
        Utils.getConfirmationDialog(
                this,
                "آیا مایل به ذخیره کردن هستید؟",
                "",
                getString(R.string.exit_with_no_save),
                getString(R.string.save),
                (dialog, which) -> {
                    setResult(Activity.RESULT_CANCELED, new Intent());
                    finish();
                },
                (dialog, which) -> findViewById(R.id.btn_save).performClick()
        ).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == REQUEST_CODE) {
            try {
                editText.setText(data.getStringExtra("barcode"));
            } catch (Exception e) {
                Toast.makeText(this, "Error:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                Logger.e(e, "Scanner Result");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riser);
        editText = findViewById(R.id.et);
        findViewById(R.id.btn_qr).setOnClickListener(v -> {
            Intent i = new Intent(RiserActivity.this, ScannerActivity.class);
            i.putExtra("sender", REQUEST_CODE);
            startActivityForResult(i, REQUEST_CODE);
        });
        findViewById(R.id.btn_save).setOnClickListener(v -> {
            String data = editText.getText().toString().trim().replaceAll(",", "");
            if (data.length() == 0) return;
            Intent i = new Intent();
            i.putExtra("id", id);
            i.putExtra("code", data);
            i.putExtra("type", 2);
            i.putExtra("geom", geom);
            setResult(Activity.RESULT_OK, i);
            finish();
        });
        try {
            id = getIntent().getIntExtra("id", 0);
            String data = getIntent().getStringExtra("data");
            geom = getIntent().getStringExtra("geom");
            File file = FileUtils.getMapCodeFile(id, Keys.riserCode);
            String r_num = FileUtils.loadLineInMapCodeCsv(file, id);
            if (r_num.length() < 1)
                r_num = new Gson().fromJson(data, JsonObject.class).get("properties").getAsJsonObject().get("r_num").getAsString();
            else r_num = r_num.split(", ")[1];
            editText.setText(r_num.replace("r", ""));
        } catch (Exception ignored) {
            Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
