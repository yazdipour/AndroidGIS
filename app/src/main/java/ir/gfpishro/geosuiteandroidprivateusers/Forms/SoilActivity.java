package ir.gfpishro.geosuiteandroidprivateusers.Forms;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mohamadamin.persianmaterialdatetimepicker.utils.PersianCalendar;
import com.mohamadamin.persianmaterialdatetimepicker.utils.TimeZones;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import ir.gfpishro.geosuiteandroidprivateusers.Helpers.SettingsHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.Utils;
import ir.gfpishro.geosuiteandroidprivateusers.Models.SOILObject;
import ir.gfpishro.geosuiteandroidprivateusers.Models.User;
import ir.gfpishro.geosuiteandroidprivateusers.R;
import ir.gfpishro.geosuiteandroidprivateusers.Services.ApiHandler;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SoilActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 501;
    public static final int FORM_CODE = 9;
    private String geom;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                finish();
                return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soil);
        if (!User.IsLoggedIn()) {
            Toast.makeText(this, "ابتدا لاگین کنید", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        geom = getIntent().getStringExtra("geom");
        PersianCalendar persianCalendar = new PersianCalendar();
        persianCalendar.setTimeZone(TimeZones.ASIA_TEHRAN.getTimeZone());

        ((TextView) findViewById(R.id.et_date)).setText(Utils.unixToPersianDate(0, false, true));
        findViewById(R.id.submit_report_btn).setOnClickListener(view -> onSubmit());
    }

    private void onSubmit() {
        SOILObject obj;
        try {
            obj = bindBack();
        } catch (Exception e) {
            new SweetAlertDialog(SoilActivity.this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(getString(R.string.error))
                    .setContentText(getString(R.string.error_null_editview))
                    .show();
            return;
        }
        SweetAlertDialog d = new SweetAlertDialog(SoilActivity.this, SweetAlertDialog.PROGRESS_TYPE)
                .setTitleText(getString(R.string.sending));
        d.show();
        ApiHandler.getApi(SettingsHandler.getSettings(this).serverIp)
                .postSoilResistance(User.getCredential(), obj).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    onFailure(call, new Throwable("خطا در سرور"));
                    return;
                }
                d.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                d.setTitleText(getString(R.string.msg_report_sent));
                d.setOnDismissListener(dialogInterface -> finish());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                d.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                d.setTitleText(getString(R.string.error));
                d.setContentText(t.getMessage());
            }
        });
    }

    private SOILObject bindBack() {
        SOILObject soilObject = new SOILObject();
        soilObject.setContractor(vl(R.id.contractor));
        soilObject.setDistance1(Integer.parseInt(vl(R.id.distance1)));
        soilObject.setDistance2(Integer.parseInt(vl(R.id.distance2)));
        soilObject.setR(Integer.parseInt(vl(R.id.et_r)));
        soilObject.setSuperviser(vl(R.id.supervisor));
        soilObject.setGeom(geom);
        return soilObject;
    }

    private String vl(int viewId) {
        return ((EditText) findViewById(viewId)).getText().toString();
    }
}