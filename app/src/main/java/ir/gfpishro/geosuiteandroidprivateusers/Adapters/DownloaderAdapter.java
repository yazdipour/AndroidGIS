package ir.gfpishro.geosuiteandroidprivateusers.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import ir.gfpishro.geosuiteandroidprivateusers.Models.Map.MapFile;
import ir.gfpishro.geosuiteandroidprivateusers.R;

public class DownloaderAdapter extends RecyclerView.Adapter<DownloaderViewHolder> {

    private final List<MapFile> data;
    private final Events listener;

    public interface Events {
        void onBtnClicked(final MapFile mapFile, final ImageButton self, ProgressBar progressBar, TextView progressBarText);
    }

    public DownloaderAdapter(List<MapFile> data, Events listener) {
        this.data = data;
        this.listener = listener;
    }

    @Override
    public DownloaderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_download, parent, false);
        return new DownloaderViewHolder(row);
    }

    @Override
    public void onBindViewHolder(DownloaderViewHolder holder, int position) {
        MapFile issue = data.get(position);
        holder.bindData(issue, listener, position);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
