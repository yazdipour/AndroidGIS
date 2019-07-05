package ir.gfpishro.geosuiteandroidprivateusers.Activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ir.gfpishro.geosuiteandroidprivateusers.Adapters.StealAdapter;
import ir.gfpishro.geosuiteandroidprivateusers.Forms.StealReportActivity;
import ir.gfpishro.geosuiteandroidprivateusers.Services.ApiHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.SettingsHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Steal;
import ir.gfpishro.geosuiteandroidprivateusers.Models.User;
import ir.gfpishro.geosuiteandroidprivateusers.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StealActivity extends AppCompatActivity
        implements SwipeRefreshLayout.OnRefreshListener, StealAdapter.OnItemClickListener {

    public static final int REQUEST_CODE = 120;
    private SwipeRefreshLayout pull_to_refresh;
    final private List<ir.gfpishro.geosuiteandroidprivateusers.Models.Steal> ls = new ArrayList<>();
    private StealAdapter adapter;
    private String geoPointJson = "";

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        pull_to_refresh.setRefreshing(true);
        if (item.getItemId() == R.id.action_refresh) {
            onRefresh();
            return true;
        }
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.refresh, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK
                && requestCode == StealReportActivity.REQUEST_CODE)
            onRefresh();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steal);
        geoPointJson = getIntent().getExtras().getString("geom", "");
        if (geoPointJson.length() < 2) {
            Toast.makeText(this, "No Point Passed", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        pull_to_refresh = findViewById(R.id.pull_to_refresh);
        pull_to_refresh.setOnRefreshListener(this);
        if (!SettingsHandler.getSettings(this).isOnline) {
            Snackbar.make(pull_to_refresh, "برای ثبت سرقت باید به سرور متصل باشید", Snackbar.LENGTH_LONG).show();
            findViewById(R.id.fab).setVisibility(View.GONE);
        }
        findViewById(R.id.fab).setOnClickListener(v -> {
            Intent i = new Intent(StealActivity.this, StealReportActivity.class);
            i.putExtra("geom", geoPointJson);
            startActivityForResult(i, StealReportActivity.REQUEST_CODE);
        });
        RecyclerView recyclerView = findViewById(R.id.rv);
        adapter = new StealAdapter(ls, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        onRefresh();
    }

    @Override
    public void onRefresh() {
        final long distance = 50000000;
        try {
            ApiHandler.getApi("").getSteals(User.getCredential(), geoPointJson, "new", distance)
                    .enqueue(new Callback<Steal[]>() {
                        @Override
                        public void onResponse(@NonNull Call<Steal[]> call, @NonNull Response<Steal[]> response) {
                            pull_to_refresh.setRefreshing(false);
                            if (!response.isSuccessful() || response.body() == null) {
                                onFailure(call, new Throwable(response.body() == null ? "NullResponse" : "ServerCode:" + response.code()));
                                return;
                            }
                            ls.clear();
                            ls.addAll(Arrays.asList(response.body()));
                            adapter.notifyDataSetChanged();
                            if (ls.size() == 0)
                                Snackbar.make(pull_to_refresh, R.string.nothing_found, Snackbar.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(@NonNull Call<Steal[]> call, @NonNull Throwable t) {
                            pull_to_refresh.setRefreshing(false);
                            Logger.e(t, "onFailure");
                            Snackbar snackbar = Snackbar.make(pull_to_refresh, getString(R.string.error) + "\n" + t.getMessage(), Snackbar.LENGTH_LONG);
                            TextView textView = snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                            textView.setMaxLines(3);
                            snackbar.getView().setBackgroundColor(Color.parseColor("#ff0000"));
                            snackbar.show();
                        }
                    });
        } catch (Throwable t) {
            pull_to_refresh.setRefreshing(false);
            Logger.e(t, "RF_Error");
            Snackbar.make(pull_to_refresh, t.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onItemClick(Steal item) {
        Intent i = new Intent(this, StealReportActivity.class);
        i.putExtra("data", new Gson().toJson(item));
        i.putExtra("geom", geoPointJson);
        startActivityForResult(i, StealReportActivity.REQUEST_CODE);
    }
}
