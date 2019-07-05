package ir.gfpishro.geosuiteandroidprivateusers.Forms;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.mohamadamin.persianmaterialdatetimepicker.date.DatePickerDialog;
import com.mohamadamin.persianmaterialdatetimepicker.utils.PersianCalendar;
import com.mohamadamin.persianmaterialdatetimepicker.utils.TimeZones;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import ir.gfpishro.geosuiteandroidprivateusers.Helpers.SettingsHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.Utils;
import ir.gfpishro.geosuiteandroidprivateusers.Models.CPSObject;
import ir.gfpishro.geosuiteandroidprivateusers.Models.User;
import ir.gfpishro.geosuiteandroidprivateusers.R;
import ir.gfpishro.geosuiteandroidprivateusers.Services.ApiHandler;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CPSActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    public static final int REQUEST_CODE = 502;
    public static final int FORM_CODE = 8;
    private String geom;
    private PersianCalendar persianCalendar = new PersianCalendar();
    private EditText repair_period;

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
        setContentView(R.layout.activity_cps);
        if (!User.IsLoggedIn()) {
            Toast.makeText(this, "ابتدا لاگین کنید", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        findViewById(R.id.submit_report_btn).setOnClickListener(view -> onSubmit());
        geom = getIntent().getStringExtra("geom");

        repair_period = findViewById(R.id.repair_period);
        repair_period.setTag(0);
        repair_period.setText(persianCalendar.getPersianShortDate());
        persianCalendar.setTimeZone(TimeZones.ASIA_TEHRAN.getTimeZone());
        persianCalendar.setTimeInMillis(Utils.getUnixTime() * 1000);
        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(
                CPSActivity.this,
                persianCalendar.getPersianYear(),
                persianCalendar.getPersianMonth(),
                persianCalendar.getPersianDay()
        );
        repair_period.setOnClickListener(v -> datePickerDialog.show(getFragmentManager(), "date" + v.getId()));
    }

    private void onSubmit() {
        CPSObject obj;
        try {
            obj = bindBack();
        } catch (Exception e) {
            new SweetAlertDialog(CPSActivity.this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(getString(R.string.error))
                    .setContentText(getString(R.string.error_null_editview))
                    .show();
            return;
        }
        SweetAlertDialog d = new SweetAlertDialog(CPSActivity.this, SweetAlertDialog.PROGRESS_TYPE)
                .setTitleText(getString(R.string.sending));
        d.show();
        ApiHandler.getApi(SettingsHandler.getSettings(this).serverIp)
                .postCPS(User.getCredential(), obj).enqueue(new Callback<Void>() {
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

    private CPSObject bindBack() {
        CPSObject cps = new CPSObject();
        cps.setGeom(geom);
        cps.setI(Integer.parseInt(vl(R.id.et_i)));
        cps.setVoltage(Integer.parseInt(vl(R.id.et_v)));
        cps.setConstructionYear(Integer.parseInt(vl(R.id.year_const)));
        cps.setConstructionSuperviser(vl(R.id.supervisor));
        cps.setContractor(vl(R.id.contractor));
        cps.setRepairCause(vl(R.id.repair_desc));
        cps.setLastRepairPeriod(Integer.parseInt(String.valueOf(repair_period.getTag())));
        cps.setRepairSuperviser(vl(R.id.repair_supervisor));
        cps.setYear(Integer.parseInt(vl(R.id.year)));
        return cps;
    }

    private String vl(int viewId) {
        return ((EditText) findViewById(viewId)).getText().toString();
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        persianCalendar.setPersianDate(year, monthOfYear, dayOfMonth);
        repair_period.setTag(persianCalendar.getTimeInMillis());
        repair_period.setText(persianCalendar.getPersianShortDate());
    }
}
