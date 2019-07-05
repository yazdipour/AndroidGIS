package ir.gfpishro.geosuiteandroidprivateusers.Forms;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import ir.gfpishro.geosuiteandroidprivateusers.R;

public class CustomerActivity extends AppCompatActivity {

    public static final int FORM_CODE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer);
        try {
            String json = getIntent().getStringExtra("data");
            JsonObject jsonObject = new Gson().fromJson(json, JsonObject.class);
            etSetText(R.id.et_city_code, jsonObject, "city_code");
            etSetText(R.id.et_name, jsonObject, "name");
            etSetText(R.id.et_family, jsonObject, "last");
            etSetText(R.id.et_melli, jsonObject, "code_m");
            etSetText(R.id.et_postal, jsonObject, "cu_postal");
            etSetText(R.id.et_phone, jsonObject, "phone");
            etSetText(R.id.et_cu_num, jsonObject, "cu_num");
            etSetText(R.id.et_cu_num1, jsonObject, "cu_num1");
            etSetText(R.id.et_serial, jsonObject, "serial_counter");
            etSetText(R.id.et_cap, jsonObject, "capacity");
            etSetText(R.id.et_address, jsonObject, "address");
            etSetText(R.id.et_address_code, jsonObject.getAsJsonArray("code_address_array").get(0).getAsString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void etSetText(int id, JsonObject jsonObject, String key) {
        try {
            etSetText(id, jsonObject.get(key).getAsString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void etSetText(int id, String str) {
        ((TextView) findViewById(id)).setText(str);
    }
}
