package ir.gfpishro.geosuiteandroidprivateusers.Activities;

import android.accounts.NetworkErrorException;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.orhanobut.logger.Logger;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import gfp.ir.vtmintegration.vtm.Spatialite.OfflineSearch;
import ir.gfpishro.geosuiteandroidprivateusers.Forms.CustomerActivity;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.DialogHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.PermissionHelper;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.SettingsHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.StringUtils;
import ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.utils.PointConverter;
import ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.utils.WGS84;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Keys;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Map.Feature;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Map.GeoJson;
import ir.gfpishro.geosuiteandroidprivateusers.Models.SearchType;
import ir.gfpishro.geosuiteandroidprivateusers.Models.User;
import ir.gfpishro.geosuiteandroidprivateusers.R;
import ir.gfpishro.geosuiteandroidprivateusers.Services.Api;
import ir.gfpishro.geosuiteandroidprivateusers.Services.ApiHandler;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static ir.gfpishro.geosuiteandroidprivateusers.Models.SearchType.CUSTOMERS;
import static ir.gfpishro.geosuiteandroidprivateusers.Models.SearchType.CUSTOMERS_BY_COORDINATE;
import static ir.gfpishro.geosuiteandroidprivateusers.Models.SearchType.STREET;

public class SearchActivity extends AppCompatActivity {
    public static final int REQUEST_CODE = 101;
    private List<Feature> featureList = new ArrayList<>();
    private List<JsonObject> customerList = new ArrayList<>();
    private SearchView searchView;
    private Spinner spinner;
    private String barcode = null;
    private LinearLayout coordinateSearchLayout;
    private EditText xField, yField;
    private FloatingActionButton searchCoordinateButton;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setUpSpinner();
        setUpListView();

        coordinateSearchLayout = findViewById(R.id.coordinate_search_layout);
        xField = findViewById(R.id.x_field);
        yField = findViewById(R.id.y_field);
        searchCoordinateButton = findViewById(R.id.search_coordinate_button);
        searchCoordinateButton.setOnClickListener(v -> search(""));

        searchView = findViewById(R.id.searchbox);
        searchView.requestFocus();
        searchView.setIconified(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String q) {
                search(q);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return true;
            }
        });
        findViewById(R.id.search_refresh_btn).setOnClickListener(view -> search(searchView.getQuery().toString()));
        barcode = getIntent().getStringExtra("barcode");
        if (barcode != null && barcode.length() > 3) {
            if (!PermissionHelper.haveLayerPermission("GasNet_serviceriser")) {
                DialogHandler.getDialog(this, SweetAlertDialog.ERROR_TYPE,
                        R.string.dont_have_permission, true,
                        false, null).show();
            } else {
                spinner.setSelection(java.util.Arrays.asList(SearchType.values()).indexOf(SearchType.RISER_NUM));
                searchView.setQuery(barcode, true);
            }
        }
    }

    private void setUpSpinner() {
        //Appbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Appbar page filter
        spinner = findViewById(R.id.CmbToolbar);
        List<String> options = new ArrayList<>();

        for (SearchType searchType : SearchType.values()) {
            if (searchType == STREET) options.add(searchType.getTitle());
            else if (searchType == CUSTOMERS || searchType == CUSTOMERS_BY_COORDINATE) {
                if (PermissionHelper.haveFormPermission(CustomerActivity.FORM_CODE))
                    options.add(searchType.getTitle());
            } else {
                if (PermissionHelper.haveLayerPermission(searchType.getTable())) {
                    options.add(searchType.getTitle());
                }
            }
        }

//        for (SearchType searchType : SearchType.values())
//            if (searchType == STREET || searchType == CUSTOMERS_BY_COORDINATE || PermissionHelper.haveLayerPermission(searchType.getTable()))
//                options.add(searchType.getTitle());
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getSupportActionBar().getThemedContext(),
                android.R.layout.simple_list_item_1, options);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SearchType searchType = getSearchType();
                if (searchType == CUSTOMERS_BY_COORDINATE) {
                    searchView.setVisibility(View.GONE);
                    coordinateSearchLayout.setVisibility(View.VISIBLE);
                } else {
                    searchView.setVisibility(View.VISIBLE);
                    coordinateSearchLayout.setVisibility(View.GONE);
                    if (searchType == STREET)
                        searchView.setInputType(InputType.TYPE_CLASS_TEXT);
                    else {
                        searchView.setInputType(InputType.TYPE_CLASS_NUMBER);

                    }
                }

                if (barcode == null) {
                    searchView.setQuery("", false);
                    featureList.clear();
                    adapter.notifyDataSetChanged();
                } else barcode = null;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setUpListView() {
        ListView recyclerView = findViewById(R.id.search_recycler_view);
        ArrayAdapter adapter = new ArrayAdapter<Feature>(this,
                android.R.layout.simple_list_item_2, android.R.id.text1, featureList) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = view.findViewById(android.R.id.text1);
                TextView text2 = view.findViewById(android.R.id.text2);
                text1.setTextDirection(View.TEXT_DIRECTION_RTL);
                text2.setTextDirection(View.TEXT_DIRECTION_RTL);
                JsonObject properties = featureList.get(position).getProperties().getAsJsonObject();
                try {
                    text1.setText(String.format("id: %s", properties.get("pk").getAsString()));
                    text2.setText(String.format("%s: %s", getSearchType().getField(), properties.get(getSearchType().getField()).getAsString()));
                } catch (Exception ignored) {
                }
                return view;
            }
        };
        recyclerView.setOnItemClickListener((parent, view, position, id) -> openCustomDialog(getSearchType(), position));
        recyclerView.setAdapter(adapter);
    }

    private void search(final String query) {
//        double[] lat = (double[]) getIntent().getExtras().get("location");
        final SweetAlertDialog sweetAlertDialog = DialogHandler.getDialog(this,
                SweetAlertDialog.PROGRESS_TYPE, R.string.loading_dialog,
                true, false, null);
        sweetAlertDialog.show();
        if (query.length() == 0 && getSearchType() != CUSTOMERS_BY_COORDINATE) {
            sweetAlertDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
            sweetAlertDialog.setTitleText(getString(R.string.error_et_empty));
            return;
        }
        final String finalQuery = StringUtils.farsiNumbersToEnglish(query);
        final SearchType searchType = getSearchType();
        Api api = ApiHandler.getApi(SettingsHandler.getSettings(this).serverIp);
        String cityCode = SettingsHandler.getSettings(this).cityCode;
        String credential = User.getCredential();
        Call<GeoJson> call;
        switch (searchType) {
            case STREET:
                call = api.getSearchStreet(credential, finalQuery, 15, cityCode);
                break;
            case RISER_NUM:
                call = api.getSearchRiser(credential, finalQuery, 15, 1, null, cityCode);
                break;
            case PG_NUM:
            case BG_NUM:
                call = api.getSearchValve(credential, finalQuery, 15,
                        searchType == SearchType.BG_NUM ? "bg" : "pg", cityCode);
                break;
            default:
            case PARCEL_CODE:
                call = api.getSearchParcel(credential, finalQuery, 15, null, cityCode);
                break;
            case CUSTOMERS_BY_COORDINATE:
                String x = xField.getText().toString();
                String y = yField.getText().toString();

                if (!x.isEmpty() && !y.isEmpty()) {
                    WGS84 wgs84 = PointConverter.UTM_TO_WGS84(Double.valueOf(x), Double.valueOf(y));
                    String geom = "POINT (" + wgs84.getLongitude() + " " + wgs84.getLatitude() + ")";


                    Call<JsonArray> call2 = api.getCustomers(credential, null, cityCode, geom);
                    call2.enqueue(new Callback<JsonArray>() {
                        @Override
                        public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                            try {
                                if (!response.isSuccessful())
                                    throw new NetworkErrorException("Error Code:" + response.code());
                                featureList.clear();
                                customerList.clear();
                                for (JsonElement json : response.body()) {
                                    Feature feature = new Feature();
                                    JsonObject prop = json.getAsJsonObject();
                                    prop.add("pk", prop.get("id"));
                                    feature.setProperties(prop);
                                    featureList.add(feature);
                                    customerList.add(prop);
                                }
                            } catch (Exception e) {
                                showError(e, sweetAlertDialog);
                            } finally {
                                sweetAlertDialog.cancel();
                            }
                        }

                        @Override
                        public void onFailure(Call<JsonArray> call, Throwable t) {
                            showError(new Exception(t.getMessage()), sweetAlertDialog);
                        }
                    });
                    return;
                }
            case CUSTOMERS:
                Call<JsonArray> call2 = api.getCustomers(credential, finalQuery, cityCode, null);
                call2.enqueue(new Callback<JsonArray>() {
                    @Override
                    public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                        try {
                            if (!response.isSuccessful())
                                throw new NetworkErrorException("Error Code:" + response.code());
                            featureList.clear();
                            customerList.clear();
                            for (JsonElement json : response.body()) {
                                Feature feature = new Feature();
                                JsonObject prop = json.getAsJsonObject();
                                prop.add("pk", prop.get("id"));
                                feature.setProperties(prop);
                                featureList.add(feature);
                                customerList.add(prop);
                            }
                        } catch (Exception e) {
                            showError(e, sweetAlertDialog);
                        } finally {
                            sweetAlertDialog.cancel();
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonArray> call, Throwable t) {
                        showError(new Exception(t.getMessage()), sweetAlertDialog);
                    }
                });
                return;
        }
        call.enqueue(new Callback<GeoJson>() {
            @Override
            public void onResponse(@NonNull Call<GeoJson> call, @NonNull Response<GeoJson> response) {
                try {
                    if (!response.isSuccessful())
                        throw new NetworkErrorException("Error Code:" + response.code());
                    featureList.clear();
                    featureList.addAll(response.body().getFeatures());
                } catch (NetworkErrorException e) {
                    try {
                        Logger.e(e, this.getClass().getName());
                        doOfflineSearch(searchType, finalQuery, sweetAlertDialog);
                    } catch (Exception e1) {
                        showError(e1, sweetAlertDialog);
                    }
                } catch (Exception e) {
                    showError(e, sweetAlertDialog);
                } finally {
                    sweetAlertDialog.cancel();
                }
            }


            @Override
            public void onFailure(@NonNull Call<GeoJson> call, @NonNull Throwable e) {
                try {
                    doOfflineSearch(searchType, finalQuery, sweetAlertDialog);
                } catch (Exception e1) {
                    showError(e1, sweetAlertDialog);
                }
            }
        });
    }

    private void showError(Exception e1, SweetAlertDialog sweetAlertDialog) {
        e1.printStackTrace();
        sweetAlertDialog.setContentText(e1.getMessage());
        sweetAlertDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
        sweetAlertDialog.showCancelButton(true);
    }

    private void doOfflineSearch(final SearchType searchType, final String query, final SweetAlertDialog sweetAlertDialog) throws Exception {
        final OfflineSearch ofs = new OfflineSearch(Environment.getExternalStorageDirectory()
                + Keys.mapsFolder + Keys.offlineDataBase);
        final JSONObject result = ofs.Search(searchType.getTable(), searchType.getField(), query);
        if (result == null) throw new Exception(getString(R.string.err_not_found));
        String json = result.toString();
        final GeoJson geoJson = new Gson().fromJson(json, GeoJson.class);
        final List<Feature> features = geoJson.getFeatures();
        if (features == null) throw new Exception(getString(R.string.err_not_found));
        featureList.clear();
        featureList.addAll(features);
        Snackbar.make(searchView, R.string.err_offline_search, Snackbar.LENGTH_SHORT).show();
        sweetAlertDialog.cancel();
    }

    private void openCustomDialog(final SearchType searchType, final int position) {
        if (searchType != CUSTOMERS && searchType != CUSTOMERS_BY_COORDINATE)
            goBack(featureList.get(position));
        else {
            Intent i = new Intent(this, CustomerActivity.class);
            String data = new Gson().toJson(customerList.get(position));
            i.putExtra("data", data);
            startActivity(i);
        }
    }

    private SearchType getSearchType() {
        for (SearchType s : SearchType.values())
            if (s.getTitle().equals(spinner.getSelectedItem().toString()))
                return s;
        return SearchType.values()[spinner.getSelectedItemPosition()];
    }

    private void goBack(Feature item) {
        JsonObject prop = item.getProperties().getAsJsonObject();
        SearchType opt = getSearchType();
        prop.addProperty("name", opt.getTitle() + ": "
                + prop.get(opt.getField()).getAsString().replace("شماره", ""));
        Intent i = new Intent();
        String json = new Gson().toJson(item);
        i.putExtra("feature", json);
        i.putExtra("featureCollection", false);
        setResult(Activity.RESULT_OK, i);
        finish();
    }
}