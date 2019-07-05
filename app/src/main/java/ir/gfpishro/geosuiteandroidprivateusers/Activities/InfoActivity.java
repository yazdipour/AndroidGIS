package ir.gfpishro.geosuiteandroidprivateusers.Activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ir.gfpishro.geosuiteandroidprivateusers.Adapters.CustomExpandableListAdapter;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.PermissionHelper;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Map.Feature;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Map.GeoJson;
import ir.gfpishro.geosuiteandroidprivateusers.Models.User;
import ir.gfpishro.geosuiteandroidprivateusers.R;
import ir.gfpishro.geosuiteandroidprivateusers.Services.ApiHandler;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InfoActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 503;
    private HashMap<String, List<String>> expandableListDetail = new HashMap<>();
    private HashMap<String, List<String>> expandableListDetail2 = new HashMap<>();
    private ExpandableListView expandableListView, expandableListView2;
    private ProgressBar progressBar;
    private JsonObject firstParcel;

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
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        progressBar = findViewById(R.id.progressBar);
        String geom = getIntent().getExtras().getString("geom", "");
        getData(geom);
        expandableListView = findViewById(R.id.expandableListView);
        expandableListView2 = findViewById(R.id.expandableListView2);
//        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
//            Toast.makeText(
//                    getApplicationContext(),
//                    expandableListTitle.get(groupPosition)
//                            + " -> "
//                            + expandableListDetail.get(
//                            expandableListTitle.get(groupPosition)).get(
//                            childPosition), Toast.LENGTH_SHORT
//            ).show();
//            return false;
//        });
    }

    private void bindTheList(ExpandableListView _expandableListView,
                             HashMap<String, List<String>> expandableList) throws Exception {
        if (expandableList.size() == 0) throw new Exception();
        ExpandableListAdapter expandableListAdapter = new CustomExpandableListAdapter(InfoActivity.this, expandableList);
        _expandableListView.setAdapter(expandableListAdapter);
    }

    private void getData(String geom) {
        ApiHandler.getApi("").getEverythingInDistance(User.getCredential(), geom).enqueue(new Callback<List<GeoJson>>() {
            @Override
            public void onResponse(Call<List<GeoJson>> call, Response<List<GeoJson>> response) {
                try {
                    if (!response.isSuccessful() || response.body() == null) {
                        onFailure(call, new Throwable("ErrorCode:" + response.code()));
                        return;
                    }
                    for (GeoJson geoJsonParent : response.body()) {
                        JsonObject crs = geoJsonParent.getCrs().getAsJsonObject();
                        String title = crs.get("type").getAsString();
                        if (!PermissionHelper.haveLayerPermission("GasNet_" + title)) continue;
                        List<Feature> features = geoJsonParent.getFeatures();
                        for (Feature feature : features) {
                            try {
                                List<String> children = new ArrayList<>();
                                JsonObject prop = feature.getProperties().getAsJsonObject();
                                String subtitle = title + ":" + prop.get("pk");
                                for (Map.Entry<String, JsonElement> entry : prop.entrySet())
                                    children.add(entry.getKey() + ": " + entry.getValue());
                                expandableListDetail.put(subtitle, children);
                                if (firstParcel == null && title.toLowerCase().equals("parcel"))
                                    firstParcel = prop;
                            } catch (Exception ignored) {
                            }
                        }
                    }
                    bindTheList(expandableListView, expandableListDetail);
                    if (firstParcel != null) {
                        findViewById(R.id.tv_customer).setVisibility(View.VISIBLE);
                        getCustomers(firstParcel);
                    }
                } catch (Exception e) {
                    Snackbar.make(findViewById(R.id.mother_layer), getString(R.string.nothing_found), Snackbar.LENGTH_LONG).show();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<List<GeoJson>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Snackbar snackbar = Snackbar.make(findViewById(R.id.mother_layer), getString(R.string.error_offline), Snackbar.LENGTH_LONG);
                snackbar.getView().setBackgroundColor(Color.parseColor("#ff0000"));
                snackbar.show();
            }
        });
    }

    private void getCustomers(JsonObject parcelProp) {
        String addressCode = parcelProp.get("code_address").getAsString();
        String cityCode = parcelProp.get("city_code").getAsString();
        String auth = User.getCredential();
        ApiHandler.getApi("").getCustomers(auth, addressCode, cityCode, null).enqueue(
                new Callback<JsonArray>() {
                    @Override
                    public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                        try {
                            if (!response.isSuccessful() || response.body() == null)
                                throw new Exception();
                            for (JsonElement jsonElement : response.body()) {
                                JsonObject customer = jsonElement.getAsJsonObject();
                                String subtitle = "customer:" + customer.get("id");
                                List<String> children = new ArrayList<>();
                                for (Map.Entry<String, JsonElement> entry : customer.entrySet())
                                    children.add(entry.getKey() + ": " + entry.getValue());
                                expandableListDetail2.put(subtitle, children);
                            }
                            bindTheList(expandableListView2, expandableListDetail2);
                        } catch (Exception e) {
                            onFailure(call, e);
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonArray> call, Throwable t) {
                        t.printStackTrace();
//                        Toast.makeText(InfoActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}