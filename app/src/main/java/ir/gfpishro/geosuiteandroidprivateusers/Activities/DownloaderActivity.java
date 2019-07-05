package ir.gfpishro.geosuiteandroidprivateusers.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.SpeedCalculator;
import com.liulishuo.okdownload.core.breakpoint.BlockInfo;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.listener.DownloadListener4WithSpeed;
import com.liulishuo.okdownload.core.listener.assist.Listener4SpeedAssistExtend;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import gfp.ir.vtmintegration.spatilite_core.core.databasehandlers.SpatialiteDatabaseHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Adapters.DownloaderAdapter;
import ir.gfpishro.geosuiteandroidprivateusers.Services.ApiHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.SettingsHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.Utils;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Keys;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Map.MapFile;
import ir.gfpishro.geosuiteandroidprivateusers.Models.User;
import ir.gfpishro.geosuiteandroidprivateusers.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DownloaderActivity extends AppCompatActivity {
    private final String TAG = "DOWNLOADER";
    private List<MapFile> mapFilesList = new ArrayList<>();
    private RecyclerView.Adapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public void onBackPressed() {
        boolean isBusy = false;
        for (MapFile f : mapFilesList) {
            if (f.getDownloadTask() != null) {
                isBusy = true;
                break;
            }
        }
        if (!isBusy)
            finish();
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("دانلود تمام نشده. آیا مایل به خروج هستید؟")
                    .setCancelable(false)
                    .setPositiveButton("خروج", (dialog, id) -> {
                        for (MapFile f : mapFilesList)
                            if (f.getDownloadTask() != null)
                                f.getDownloadTask().cancel();
                        finish();
                    })
                    .setNegativeButton("ماندن", null);
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloader);
        final RecyclerView recyclerView = findViewById(R.id.download_list);
        adapter = new DownloaderAdapter(mapFilesList, events);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (mapFilesList.size() == 0) getFilesAddress();
            else swipeRefreshLayout.setRefreshing(false);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        getFilesAddress();
    }

    private void getFilesAddress() {
        swipeRefreshLayout.setRefreshing(true);
        ApiHandler.getApi("").getMapFiles(User.getCredential()).enqueue(new Callback<List<MapFile>>() {
            @Override
            public void onResponse(@NonNull Call<List<MapFile>> call, @NonNull Response<List<MapFile>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    swipeRefreshLayout.setRefreshing(false);
                    mapFilesList.clear();
                    mapFilesList.addAll(Objects.requireNonNull(response.body()));
                    adapter.notifyDataSetChanged();
                } else {
                    try {
                        onFailure(call, new Throwable(Objects.requireNonNull(response.errorBody()).string()));
                    } catch (Exception e) {
                        onFailure(call, new Throwable("Error Code:" + response.code()));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<MapFile>> call, @NonNull Throwable t) {
                Snackbar.make(findViewById(R.id.download_list), t.getMessage(), Snackbar.LENGTH_LONG).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private DownloadTask generateTask(final String url, final File parentFile, final String filename) {
        return new DownloadTask.Builder(url, parentFile)
                .setFilename(filename)
                // the minimal interval millisecond for callback progress
                .setMinIntervalMillisCallbackProcess(64)
                // ignore the same task has already completed in the past.
                .setPassIfAlreadyCompleted(false)
                .build();
    }

    DownloaderAdapter.Events events = (mapFile, self, progressBar, progressBarText) -> {
        if (mapFile.getDownloadTask() != null) {
            mapFile.getDownloadTask().cancel();
            return;
        }
        DownloadListener4WithSpeed listener = new DownloadListener4WithSpeed() {
            private long totalLength = 0;

            @Override
            public void taskStart(@NonNull DownloadTask task) {
                Logger.d(TAG, "taskStart: ");
            }

            @Override
            public void connectStart(@NonNull DownloadTask task, int blockIndex, @NonNull Map<String, List<String>> requestHeaderFields) {
                Logger.d(TAG, "connectStart: ");
                progressBarText.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                progressBarText.setText("...");
                self.setImageDrawable(getDrawable(R.drawable.ic_pause_white_24dp));
            }

            @Override
            public void connectEnd(@NonNull DownloadTask task, int blockIndex, int responseCode, @NonNull Map<String, List<String>> responseHeaderFields) {
                Logger.d(TAG, "connectEnd: " + blockIndex);
            }

            @Override
            public void infoReady(@NonNull DownloadTask task, @NonNull BreakpointInfo info, boolean fromBreakpoint, @NonNull Listener4SpeedAssistExtend.Listener4SpeedModel model) {
                Logger.d(TAG, "infoReady: ");
                totalLength = info.getTotalLength();
            }

            @Override
            public void progressBlock(@NonNull DownloadTask task, int blockIndex, long currentBlockOffset, @NonNull SpeedCalculator blockSpeed) {
            }

            @Override
            public void progress(@NonNull DownloadTask task, long currentOffset, @NonNull SpeedCalculator taskSpeed) {
                final String readableOffset = Utils.humanReadableByteCount(currentOffset, true);
                final String readableTotalLength = Utils.humanReadableByteCount(mapFile.getSize(), true);
                final String progressStatus = readableOffset + "/" + readableTotalLength;
                final String speed = taskSpeed.speed();
                final String progressStatusWithSpeed = progressStatus + "(" + speed + ")";
                progressBarText.setText(progressStatusWithSpeed);

                final float percent = (float) currentOffset / totalLength;
                progressBar.setProgress((int) (percent * progressBar.getMax()));
            }

            @Override
            public void blockEnd(@NonNull DownloadTask task, int blockIndex, BlockInfo info, @NonNull SpeedCalculator blockSpeed) {
            }

            @Override
            public void taskEnd(@NonNull DownloadTask task, @NonNull EndCause cause, @Nullable Exception realCause, @NonNull SpeedCalculator taskSpeed) {
                if (task.getFile() != null && task.getFile().exists() && realCause == null && cause.name().equals("COMPLETED")) {
                    self.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    progressBarText.setText("دانلود با موفقیت انجام شد");
                    try {
                        String path = Environment.getExternalStorageDirectory() + Keys.mapsFolder;
                        if (mapFile.getName().contains(".zip"))
                            Utils.unzip(task.getFile(), new File(path));
                        if (mapFile.getName().contains(Keys.offlineDataBase.split("\\.")[0])) {
                            SpatialiteDatabaseHandler handler = new SpatialiteDatabaseHandler(path + Keys.offlineDataBase);
                            handler.open();
                            handler.updateLayerStatstics();
                        } else if (mapFile.getName().contains(".apk")) {
                            openApkFile(path + mapFile.getName());
                        }
                    } catch (IOException e) {
                        Logger.e(e, TAG);
                        progressBarText.setText("خطا هنگام بازکردن فایل");
                    }
                } else {
                    self.setImageDrawable(getDrawable(android.R.drawable.stat_sys_download));
                    progressBarText.setText(cause.name());
                    progressBar.setVisibility(View.GONE);
                    if (task.getFile() != null) task.getFile().delete();
                }
                mapFile.setDownloadTask(null);
            }
        };
        DownloadTask task = generateTask(SettingsHandler.getSettings(DownloaderActivity.this).serverIp + mapFile.getUrl(),
                new File(Environment.getExternalStorageDirectory() + Keys.mapsFolder),
                mapFile.getName());
        task.enqueue(listener);
        mapFile.setDownloadTask(task);
    };

    private void openApkFile(String path) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(path)), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}