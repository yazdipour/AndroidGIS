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
import ir.gfpishro.geosuiteandroidprivateusers.Models.HSEObject;
import ir.gfpishro.geosuiteandroidprivateusers.Models.User;
import ir.gfpishro.geosuiteandroidprivateusers.R;
import ir.gfpishro.geosuiteandroidprivateusers.Services.ApiHandler;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HSEActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 500;
    public static final int FORM_CODE = 10;
    private String geom;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hse);
        if (!User.IsLoggedIn()) {
            Toast.makeText(this, "ابتدا لاگین کنید", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        geom = getIntent().getExtras().getString("geom", "");
        bind();
        findViewById(R.id.submit_report_btn).setOnClickListener(view -> onSubmit());
    }

    private void onSubmit() {
        HSEObject obj;
        try {
            obj = bindBack();
        } catch (Exception e) {
            new SweetAlertDialog(HSEActivity.this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(getString(R.string.error))
                    .setContentText(getString(R.string.error_null_editview))
                    .show();
            return;
        }
        if (obj == null) return;
        SweetAlertDialog d = new SweetAlertDialog(HSEActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        d.setTitleText(getString(R.string.sending));
        d.show();
        ApiHandler.getApi(SettingsHandler.getSettings(this).serverIp)
                .postHSE(User.getCredential(), obj).enqueue(new Callback<Void>() {
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

    private void bind() {
        PersianCalendar persianCalendar = new PersianCalendar();
        persianCalendar.setTimeZone(TimeZones.ASIA_TEHRAN.getTimeZone());
        ((EditText) findViewById(R.id.et_location)).setText(geom);
        ((TextView) findViewById(R.id.et_date)).setText(persianCalendar.getPersianLongDateAndTime());
    }

    private HSEObject bindBack() {
        HSEObject hseObject = new HSEObject();
        hseObject.setNumber(Integer.parseInt(((EditText) findViewById(R.id.et_num)).getText().toString()));
        hseObject.setGeom(geom);
        return hseObject;
    }
}
