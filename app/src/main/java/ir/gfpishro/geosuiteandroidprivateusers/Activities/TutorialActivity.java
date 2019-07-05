package ir.gfpishro.geosuiteandroidprivateusers.Activities;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ir.gfpishro.geosuiteandroidprivateusers.Adapters.VideoAdapter;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.FileUtils;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Keys;
import ir.gfpishro.geosuiteandroidprivateusers.R;

public class TutorialActivity extends AppCompatActivity {

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
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
        setContentView(R.layout.activity_tutorial);
        List<String> ls = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.rv);
        VideoAdapter adapter = new VideoAdapter(ls);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        getAllVideosInFolder(ls);
        adapter.notifyDataSetChanged();
    }

    private void getAllVideosInFolder(List<String> ls) {
        String directory = Environment.getExternalStorageDirectory() + Keys.mapsFolder + Keys.videosDirectory;
        File dir = FileUtils.getFolderCreateIfNotExist(directory);
        for (File f : dir.listFiles())
            if (f.isFile() && f.getName().contains(".mp4"))
                ls.add(f.getAbsolutePath());
    }
}
