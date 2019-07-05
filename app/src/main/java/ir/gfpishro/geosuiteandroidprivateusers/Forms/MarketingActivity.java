package ir.gfpishro.geosuiteandroidprivateusers.Forms;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ir.gfpishro.geosuiteandroidprivateusers.Adapters.MarketingAdapter;
import ir.gfpishro.geosuiteandroidprivateusers.Adapters.MarketingViewHolder;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.FileUtils;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.Utils;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Keys;
import ir.gfpishro.geosuiteandroidprivateusers.Models.User;
import ir.gfpishro.geosuiteandroidprivateusers.R;

public class MarketingActivity extends AppCompatActivity implements MarketingAdapter.Events {
    public static final int REQUEST_CODE = 106;
    public static final int FORM_CODE = 1;
    final private List<String> ls = new ArrayList<>();
    private MarketingAdapter adapter;
    private int id = 0;
    private RecyclerView recyclerView;
    private String geom = "";

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
        final String data = collectData();
        Utils.getConfirmationDialog(
                this,
                "آیا مایل به ذخیره کردن هستید؟",
                data,
                getString(R.string.exit_with_no_save),
                getString(R.string.save),
                (dialog, which) -> {
                    setResult(Activity.RESULT_CANCELED, new Intent());
                    finish();
                },
                (dialog, which) -> {
                    Intent i = new Intent();
                    i.putExtra("id", id);
                    i.putExtra("code", data);
                    i.putExtra("type", 1);
                    i.putExtra("geom", geom);
                    setResult(Activity.RESULT_OK, i);
                    finish();
                }
        ).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marketing);
        findViewById(R.id.btn_save).setOnClickListener(v -> onBackPressed());
        id = getIntent().getIntExtra("id", 0);
        geom = getIntent().getStringExtra("geom");
        String data = getIntent().getStringExtra("data");
        recyclerView = findViewById(R.id.rv);
        setUpUI(id, data);
        findViewById(R.id.btn_add).setOnClickListener(v -> {
            ls.add("");
            adapter.notifyItemInserted(ls.size() - 1);
        });
    }

    @Override
    public void onBtnClicked(String self, int position) {
        ls.remove(self);
        adapter.notifyItemRemoved(position);
    }

    private void setUpUI(int id, String data) {
        File file = FileUtils.getMapCodeFile(id, Keys.marketingCodeDirectory);
        String fileContent = FileUtils.loadLineInMapCodeCsv(file, id);
        if (fileContent.length() > 1)
            ls.addAll(Arrays.asList(fileContent.split(", ")[1].split("\\s\\s")));
        else ls.add("");
        if (ls.size() == 0 || ls.get(0).length() == 0) {
            try {
                JsonObject jsonObject = new Gson().fromJson(data, JsonObject.class);
                String dbMarketing = jsonObject.get("properties").getAsJsonObject().get("pa_marketing").getAsString();
                ls.remove(0);
                ls.addAll(Arrays.asList(dbMarketing.split("\\s\\s")));
            } catch (Exception ignored) {
            }
        }
        adapter = new MarketingAdapter(ls, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private String collectData() {
        StringBuilder data = new StringBuilder();
        for (int i = 0; i < adapter.getItemCount(); i++) {
            MarketingViewHolder holder = (MarketingViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
            if (holder != null) data.append(holder.getCode()).append("  ");
        }
        return data.toString().trim();
    }
}
