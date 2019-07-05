package ir.gfpishro.geosuiteandroidprivateusers.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import ir.gfpishro.geosuiteandroidprivateusers.R;

public class VideoAdapter extends RecyclerView.Adapter<VideoViewHolder> {
    private final List<String> data;

    public VideoAdapter(List<String> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View row = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_video, viewGroup, false);
        return new VideoViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int i) {
        holder.bindData(data.get(i));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
