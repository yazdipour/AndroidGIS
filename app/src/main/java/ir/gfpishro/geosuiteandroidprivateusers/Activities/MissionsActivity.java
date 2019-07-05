package ir.gfpishro.geosuiteandroidprivateusers.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ir.gfpishro.geosuiteandroidprivateusers.Adapters.MissionAdapter;
import ir.gfpishro.geosuiteandroidprivateusers.Services.ApiHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.MissionHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.SettingsHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.Utils;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Keys;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Mission.Mission;
import ir.gfpishro.geosuiteandroidprivateusers.Models.ServerStatus;
import ir.gfpishro.geosuiteandroidprivateusers.Models.User;
import ir.gfpishro.geosuiteandroidprivateusers.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MissionsActivity extends AppCompatActivity implements MissionAdapter.OnItemClickListener, OnRefreshListener {
    public static final int REQUEST_CODE = 104;
    public static final int FORM_CODE = 11;
    private RecyclerView.Adapter adapter;
    private List<Mission> ls = new ArrayList<>();
    private SwipeRefreshLayout pull_to_refresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_missions);
        if (MainActivity.counterFab != null && User.IsUserValid(User.getCurrentUser(this))) {
            pull_to_refresh = findViewById(R.id.pull_to_refresh);
            pull_to_refresh.setOnRefreshListener(this);
            setUpListView();
        } else {
            Intent i = new Intent(MissionsActivity.this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.refresh, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        pull_to_refresh.setRefreshing(true);
        switch (item.getItemId()) {
            case R.id.action_refresh:
                onRefresh();
                return true;
            default:
                finish();
                return true;
        }
    }

    @Override
    public void onItemClick(Mission item, boolean isPending, boolean hasMissing) {
        if (isPending) {
            new SweetAlertDialog(MissionsActivity.this, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                    .setCustomImage(R.drawable.ic_signal_cellular_connected_no_internet_0_bar_black_24dp)
                    .setTitleText("قفل")
                    .setContentText("در حال انتظار جهت اتصال به شبکه")
                    .showCancelButton(false)
                    .show();
//                                .setCancelText(getString(R.string.close))
        } else {
            Intent i = new Intent(MissionsActivity.this, MainActivity.class);
            i.putExtra("mission", new Gson().toJson(item));
            i.putExtra("reportExists", hasMissing);
            User.getCurrentUser(this).setBusyWithMission(true);
            setResult(Activity.RESULT_OK, i);
            finish();
        }
    }

    @Override
    public void onRefresh() {
        ApiHandler.getApi(SettingsHandler.getSettings(this).serverIp)
                .getMissions(User.getCredential(), User.getCurrentUser(this).getmId(this))
                .enqueue(new Callback<List<Mission>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Mission>> call, @NonNull Response<List<Mission>> response) {
                        if (response.isSuccessful()) {
                            List<Mission> list = response.body();
                            if (list != null && !ls.equals(list)) {
                                new MissionHandler(MissionsActivity.this).update(list);
                                ls.clear();
                                ls.addAll(list);
                                adapter.notifyDataSetChanged();
                                showTime();
                            }
                        } else {
                            try {
                                ServerStatus serverStatus = new Gson().fromJson(response.errorBody().string(), ServerStatus.class);
                                onFailure(call, new Throwable(serverStatus.getMessage()));
                            } catch (Exception e) {
                                try {
                                    onFailure(call, new Throwable(response.errorBody().string()));
                                } catch (Exception e1) {
                                    onFailure(call, new Throwable("ErrorCode: " + response.code()));
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Mission>> call, @NonNull Throwable t) {
                        try {
                            pull_to_refresh.setRefreshing(false);
                            new SweetAlertDialog(MissionsActivity.this, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText(getString(R.string.error))
                                    .setContentText(t.getMessage())
                                    .show();
                            Logger.e(t, this.getClass().getName());
                        } catch (Exception ignored) {
                        }
                    }
                });
    }

    private void setUpListView() {
        pull_to_refresh.setRefreshing(true);
        RecyclerView recyclerView = findViewById(R.id.missions_recycler_view);
        String json = Utils.getSharedPref(this).getString(Keys.missionsList(User.getCurrentUser(this).getId()), "");
        if (json.length() > 2) ls.addAll(Arrays.asList(new Gson().fromJson(json, Mission[].class)));
        adapter = new MissionAdapter(ls, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        showTime();
    }

    private void showTime() {
        long unix = Utils.getSharedPref(this).getLong(Keys.lastMissionUpdate(User.getCurrentUser(this).getId()), 0);
        if (unix != 0) {
            TextView tv = findViewById(R.id.tv_last_mission_update);
            tv.setVisibility(View.VISIBLE);
            tv.setText(String.format("%s: %s", getString(R.string.tv_last_mission_update), Utils.unixToPersianDate(unix, true, true)));
        }
        pull_to_refresh.setRefreshing(false);
    }
}