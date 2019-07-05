package ir.gfpishro.geosuiteandroidprivateusers.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Switch;

import com.google.gson.Gson;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import java.util.ArrayList;
import java.util.List;

import ir.gfpishro.geosuiteandroidprivateusers.Adapters.LayerAdapter;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.Utils;
import ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.LayerSchema;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Keys;
import ir.gfpishro.geosuiteandroidprivateusers.Models.User;
import ir.gfpishro.geosuiteandroidprivateusers.R;

import static ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.LayerSchema.LayerType.base;

public class LayersActivity extends AppCompatActivity {
    public static final int REQUEST_CODE = 100;

    enum ChangeStatus {NONE, CHANGED, RESTART}

    private List<LayerSchema> allLayers = new ArrayList<>();
    private RecyclerView.Adapter adapter;
    private ChangeStatus isChanged = ChangeStatus.NONE;
    private Gson gson = new Gson();

    //  BackPressed
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
        if (isChanged == ChangeStatus.NONE) {
            setResult(Activity.RESULT_CANCELED, new Intent());
            this.finish();
            return;
        }
        String json = gson.toJson(allLayers);
        Intent i = new Intent();
        cacheLayers(User.getCurrentUser(this).getId());
        i.putExtra("layers", json);
        setResult(Activity.RESULT_OK, i);
        this.finish();
    }

    //  END BackPressed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layers);
        loadListViewItems();
        setUpListView();
        ((ScrollView) findViewById(R.id.sv_layers)).smoothScrollTo(0, 0);
    }

    private void setUpListView() {
        final RecyclerView recyclerView = findViewById(R.id.layers_recycler_view);
        LayerAdapter.Events event = new LayerAdapter.Events() {
            @Override
            public void onRmClicked(LayerSchema layer) {
                int i = allLayers.indexOf(layer);
                allLayers.remove(i);
                adapter.notifyItemRemoved(i);
                isChanged = ChangeStatus.CHANGED;
            }

            @Override
            public void onZoomClicked(LayerSchema layer) {
                //MapHandler find by id
//                int i = allLayers.indexOf(layer);
                Intent intent = new Intent();
                intent.putExtra("zoom", layer.getMapIndex());
                setResult(Activity.RESULT_OK, intent);
                finish();
            }

            @Override
            public void onSwitch(LayerSchema layer, boolean isChecked) {
                layer.setEnable(isChecked);
                isChanged = ChangeStatus.CHANGED;
            }
        };
        adapter = new LayerAdapter(allLayers, event);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        mLayoutManager.setAutoMeasureEnabled(true);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.disableAllBtn).setOnClickListener(v -> {
            for (int i = 0; i < recyclerView.getChildCount(); i++)
                ((Switch) recyclerView.findViewHolderForAdapterPosition(i).itemView.findViewById(R.id.switch1)).setChecked(false);
        });
        findViewById(R.id.enableAllBtn).setOnClickListener(v -> {
            for (int i = 0; i < recyclerView.getChildCount(); i++)
                ((Switch) recyclerView.findViewHolderForAdapterPosition(i).itemView.findViewById(R.id.switch1)).setChecked(true);
        });
    }

    private void loadListViewItems() {
        RadioButton[] rb = new RadioButton[]{findViewById(R.id.rb1), findViewById(R.id.rb2), findViewById(R.id.rb3), findViewById(R.id.rb4)};
        String json = getIntent().getStringExtra("layers");
        allLayers = gson.fromJson(json, Utils.getListType(LayerSchema.class));
        for (int inx = 0; inx < rb.length; inx++) {
            LayerSchema layer = allLayers.get(inx);
            if (layer.getType() == base) {
                rb[inx].setText(layer.getTitle());
                rb[inx].setChecked(layer.isEnable());
            } else break;
        }
        ((RadioGroup) findViewById(R.id.rg)).setOnCheckedChangeListener((group, checkedId) -> {
            final SweetAlertDialog dialog = new SweetAlertDialog(LayersActivity.this, SweetAlertDialog.WARNING_TYPE);
            try {
                if (!User.IsLoggedIn())
                    throw new Exception();
            } catch (Exception e) {
                e.printStackTrace();
                dialog.setTitleText(getString(R.string.error_auth));
                dialog.showCancelButton(true);
                dialog.show();
                return;
            }
            dialog.setTitleText(getString(R.string.msg_restart)).setConfirmText("باشه").setCancelText("بعدا");
            dialog.setConfirmClickListener(sweetAlertDialog -> {
                isChanged = ChangeStatus.RESTART;
                for (int i = 0; i < rb.length; i++) {
                    if (rb[i].isChecked())
                        allLayers.get(i).setEnable(true);
                    else allLayers.get(i).setEnable(false);
                }
                cacheLayers(User.getCurrentUser(LayersActivity.this).getId());
                Intent i = new Intent(LayersActivity.this, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            });
            dialog.show();
        });
    }

    private void cacheLayers(int uid) {
        try {
            String json = gson.toJson(allLayers);
            // TODO: Make it stable (mapIndex can change! String is not safe over all)
            json = json.replaceAll("\"mapIndex\":\\d+,", "");
            Utils.getSharedPref(this).edit().putString(Keys.layers(uid), json).apply();
        } catch (Exception ignored) {
        }
    }
}
