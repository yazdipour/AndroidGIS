package ir.gfpishro.geosuiteandroidprivateusers.Forms;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hootsuite.nachos.NachoTextView;
import com.hootsuite.nachos.terminator.ChipTerminatorHandler;
import com.mohamadamin.persianmaterialdatetimepicker.date.DatePickerDialog;
import com.mohamadamin.persianmaterialdatetimepicker.time.RadialPickerLayout;
import com.mohamadamin.persianmaterialdatetimepicker.time.TimePickerDialog;
import com.mohamadamin.persianmaterialdatetimepicker.utils.PersianCalendar;
import com.mohamadamin.persianmaterialdatetimepicker.utils.TimeZones;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import ir.gfpishro.geosuiteandroidprivateusers.Services.ApiHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.CacheHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.ImageUtils;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.SettingsHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.Utils;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Keys;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Log.AppLog;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Log.LogType;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Steal;
import ir.gfpishro.geosuiteandroidprivateusers.Models.User;
import ir.gfpishro.geosuiteandroidprivateusers.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressLint("DefaultLocale")
public class StealReportActivity extends AppCompatActivity implements IPickResult,
        View.OnClickListener, TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {
    public static final int REQUEST_CODE = 121;
    public static final int FORM_CODE = 6;
    private Steal steal = null;
    private Gson gson = new Gson();
    private String geom;
    private EditText et_steal_date, et_resolve_date, et_steal_type;
    private boolean newStealMode = false;
    private NachoTextView chips_shared_code;
    private PersianCalendar persianCalendar = new PersianCalendar();
    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;
    private User user;
    private ArrayList<String> stealTypesKeys = new ArrayList<>();
    private List<Bitmap> images = new ArrayList<>();
    private List<String> base64Image = new ArrayList<>();
    private ImageAdapterGridView adapter;
    private GridView gridView;

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
                getString(R.string.are_u_sure),
                getString(R.string.report_not_saved),
                getString(R.string.exit_with_no_save),
                getString(R.string.stay),
                (dialog, id) -> {
                    setResult(Activity.RESULT_CANCELED, getIntent());
                    finish();
                }, null).show();
    }

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
        PickImageDialog.build(setup).show(this);
    }

    @Override
    public void onPickResult(PickResult pickResult) {
        if (pickResult.getError() != null)
            Toast.makeText(this, pickResult.getError().getMessage(), Toast.LENGTH_LONG).show();
        else {
            ((ScrollView) findViewById(R.id.mainScrollView)).fullScroll(ScrollView.FOCUS_DOWN);
            Bitmap imageBitmap = pickResult.getBitmap();
            if (imageBitmap == null) return;
            images.add(imageBitmap);
            base64Image.add(ImageUtils.convertBase64(imageBitmap, 80));
            gridView.setNumColumns(images.size());
            adapter.notifyDataSetChanged();
        }
    }

    private void setupCalender(long stealTime) {
        persianCalendar.setTimeZone(TimeZones.ASIA_TEHRAN.getTimeZone());
        if (stealTime != 0) persianCalendar.setTimeInMillis(stealTime * 1000);
        datePickerDialog = DatePickerDialog.newInstance(
                StealReportActivity.this,
                persianCalendar.getPersianYear(),
                persianCalendar.getPersianMonth(),
                persianCalendar.getPersianDay()
        );
        timePickerDialog = TimePickerDialog.newInstance(
                StealReportActivity.this,
                persianCalendar.getTime().getHours(),
                persianCalendar.getTime().getMinutes(), false);
//        PersianCalendar now = new PersianCalendar();
//        now.setTimeZone(TimeZones.IRAN.getTimeZone());
//        datePickerDialog.setMaxDate(now);
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        persianCalendar.setPersianDate(year, monthOfYear, dayOfMonth);
        timePickerDialog.show(getFragmentManager(), "timePickerDialog");
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
        long newShift = (hourOfDay * 60 + minute) * 60000;
        long oldShift = (persianCalendar.getTime().getHours() * 60 + persianCalendar.getTime().getMinutes()) * 60000;
        long newTime = persianCalendar.getTimeInMillis() - oldShift + newShift;
        persianCalendar.setTimeInMillis(newTime);
        String date = String.format("%s %d:%d", persianCalendar.getPersianShortDate(), hourOfDay, minute);
        if (newStealMode) et_steal_date.setText(date);
        else et_resolve_date.setText(date);
    }

    private void setupChips() {
        chips_shared_code = findViewById(R.id.et_code_eshterak);
//        chips_shared_code.setInputType(InputType.TYPE_NUMBER_VARIATION_NORMAL);
        chips_shared_code.setFocusable(true);
        chips_shared_code.addChipTerminator(' ', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_TO_TERMINATOR);
        chips_shared_code.addChipTerminator('*', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_TO_TERMINATOR);
        chips_shared_code.addChipTerminator(',', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_TO_TERMINATOR);
        chips_shared_code.addChipTerminator('.', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_TO_TERMINATOR);
        chips_shared_code.addChipTerminator('\n', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_TO_TERMINATOR);
        chips_shared_code.addChipTerminator('#', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_TO_TERMINATOR);
        chips_shared_code.addChipTerminator('+', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_TO_TERMINATOR);
        chips_shared_code.setIllegalCharacters('-', '/', '(', ')', 'N');
        chips_shared_code.enableEditChipOnTouch(false, false);
        chips_shared_code.setEnabled(newStealMode);
        if (!newStealMode && steal.getCuNum() != null)
            chips_shared_code.setText(Arrays.asList(steal.getCuNum()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steal_report);
        user = User.getCurrentUser(this);
        if (user == null || user.getId() == null) {
            finish();
            Toast.makeText(this, R.string.error_no_user, Toast.LENGTH_SHORT).show();
            return;
        }
        //setup UI
        String stealData = Objects.requireNonNull(getIntent().getExtras()).getString("data", "");
        geom = getIntent().getExtras().getString("geom", "");
        if (stealData.length() > 0) steal = gson.fromJson(stealData, Steal.class);
        newStealMode = (steal == null);
        TextView tv_info = findViewById(R.id.tv_info);
        et_steal_date = findViewById(R.id.et_date_steal);
        findViewById(R.id.submit_report_btn).setOnClickListener(this);
        // binding
        setupCalender(newStealMode ? 0 : steal.getStealDate());
        et_steal_date.setText(persianCalendar.getPersianLongDateAndTime());
        tv_info.setText(String.format(getString(R.string.tv_date_created), et_steal_date.getText(), geom));
        setupSpinner();
        if (!newStealMode) {
            et_steal_type.setText(steal.getStealType());
        }
        setupChips();
        setupGridView();
        if (newStealMode) {
            findViewById(R.id.solve_card).setVisibility(View.GONE);
            et_steal_date.setOnClickListener(this);
            findViewById(R.id.cameraBtn).setOnClickListener(this);
//            findViewById(R.id.tv_rm_img).setOnClickListener(this);
        } else {
            findViewById(R.id.cameraBtn).setVisibility(View.INVISIBLE);
            findViewById(R.id.et_description).setEnabled(false);
            et_steal_date.setEnabled(false);
            tv_info.setText(String.format("%s\n سرقت شماره %d \n پارسل %d", tv_info.getText(), steal.getId(), steal.getParcel()));
            et_resolve_date = findViewById(R.id.et_solve_date);
            et_resolve_date.setText(Utils.unixToPersianDate(Utils.getUnixTime(), true, true));
            et_resolve_date.setOnClickListener(this);
            if (steal.getImage() == null || steal.getImage().length == 0)
                findViewById(R.id.cameraLayout).setVisibility(View.GONE);
            else for (String url : steal.getImage())
                Picasso.get().load(SettingsHandler.getSettings(this).serverIp + url).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        images.add(bitmap);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                    }
                });
        }
    }

    private void setupGridView() {
        gridView = findViewById(R.id.gridview);
        adapter = new ImageAdapterGridView(this);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener((parent, v, position, id) -> {
            if (!newStealMode) return;
            try {
                images.remove(position);
                base64Image.remove(position);
                adapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.submit_report_btn:
                try {
                    bindBack();
                    if (SettingsHandler.getSettings(StealReportActivity.this).isOnline) Submit();
                    else CacheSteal();
                } catch (Exception e) {
                    e.printStackTrace();
                    Snackbar snackbar = Snackbar.make(v, getResources().getString(R.string.error_null_editview), Snackbar.LENGTH_LONG);
                    snackbar.getView().setBackgroundColor(Color.parseColor("#ff0000"));
                    snackbar.show();
                }
                break;
            case R.id.cameraBtn:
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(StealReportActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
                } else OpenCamera();
                break;
            case R.id.et_solve_date:
            case R.id.et_date_steal:
                datePickerDialog.show(getFragmentManager(), "date" + v.getId());
                break;
//            case R.id.tv_rm_img:
//                rmCameraImage(v);
//                break;
        }
    }

    private void CacheSteal() {
        final SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        dialog.setTitleText(getString(R.string.msg_report_cached)).showCancelButton(false).setCancelable(false);
        dialog.show();
        try {
            AppLog log = new AppLog(user.getId(), user.getmId(this), Utils.getUnixTime(), LogType.STEAL, gson.toJson(steal));
            CacheHandler.getHandler(this).push(log, false);
        } catch (Exception e) {
            dialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
            dialog.setTitleText(getString(R.string.error));
            dialog.setContentText(e.getMessage());
            dialog.setCancelable(true);
        }
    }

    private void bindBack() {
        if (et_steal_type.getText().toString().isEmpty()) throw new IllegalArgumentException();
        if (steal == null) steal = new Steal();
        steal.setStealType(et_steal_type.getText().toString());
//        steal.setStealType(stealTypesKeys.get(et_steal_type.getText().toString()));
        if (newStealMode) {
            if (base64Image != null) steal.setImage(base64Image.toArray(new String[]{}));
            steal.setStealDate((int) (persianCalendar.getTimeInMillis() / 1000f));
            steal.setCuNum(chips_shared_code.getChipAndTokenValues().toArray(new String[0]));
            steal.setDescription(((EditText) findViewById(R.id.et_description)).getText().toString());
            steal.setGeom(geom);
            steal.setUserLocation(Utils.getGeom(SettingsHandler.getSettings(this).getCurrentLocationPoint()));
            steal.setStealStatus("new");
        } else {
            steal.setStealStatus("resolve");
            steal.setFixDate((int) (persianCalendar.getTimeInMillis() / 1000f));
            steal.setFixDesc(((EditText) findViewById(R.id.et_solve_desc)).getText().toString());
        }
    }

    private void Submit() {
        String auth = User.getCredential();
        if (auth.equals("")) return;
        final SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        dialog.setTitleText(getString(R.string.sending)).showCancelButton(false).setCancelable(false);
        dialog.show();
        ApiHandler.getApi("").postSteal(auth, new Steal[]{steal}).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    dialog.dismissWithAnimation();
                    setResult(Activity.RESULT_OK, new Intent());
                    finish();
                } else onFailure(call, new Throwable("ServerError: " + response.code()));
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                dialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                dialog.setTitleText(getString(R.string.error));
                dialog.setContentText(t.getMessage());
                dialog.setCancelable(true);
            }
        });
    }

    private void setupSpinner() {
        et_steal_type = findViewById(R.id.et_steal_type);
        List<String> stealTypes = new ArrayList<>();
        String types = Utils.getSharedPref(this).getString(Keys.statics(user.getId()) + Keys.STEAL_TYPE, "");
        if (types.length() > 1) stealTypes.addAll(Arrays.asList(types.split(",")));
        else stealTypes.add("NAN");
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, stealTypes);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        et_steal_type.setAdapter(spinnerArrayAdapter);
//        et_steal_type.setSelection(0);
        et_steal_type.setEnabled(newStealMode);
//        et_steal_type.setClickable(newStealMode);
        String typesKey = Utils.getSharedPref(this).getString(Keys.statics(user.getId()) + Keys.STEAL_TYPE + 2, "");
        if (typesKey.length() > 1) stealTypesKeys.addAll(Arrays.asList(typesKey.split(",")));
    }

    public class ImageAdapterGridView extends BaseAdapter {
        private Context mContext;

        ImageAdapterGridView(Context c) {
            mContext = c;
        }

        public int getCount() {
            return images.size();
        }

        public Object getItem(int position) {
            return images.get(position);
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView mImageView;
            if (convertView == null) {
                mImageView = new ImageView(mContext);
                mImageView.setLayoutParams(new GridView.LayoutParams(220, 320));
                mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                mImageView.setPadding(16, 16, 16, 16);
            } else mImageView = (ImageView) convertView;
            mImageView.setImageBitmap(images.get(position));
            return mImageView;
        }
    }
}
