package ir.gfpishro.geosuiteandroidprivateusers.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import ir.gfpishro.geosuiteandroidprivateusers.Models.Steal;
import ir.gfpishro.geosuiteandroidprivateusers.R;

public class StealAdapter extends RecyclerView.Adapter<StealViewHolder> {
    private List<Steal> data;
    private final StealAdapter.OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Steal item);
    }

    public StealAdapter(List<Steal> ls, StealAdapter.OnItemClickListener listener) {
        data = ls;
        this.listener = listener;
    }

    @Override
    public StealViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_steal, parent, false);
        return new StealViewHolder(row);
    }

    @Override
    public void onBindViewHolder(StealViewHolder holder, int position) {
        holder.bindData(data.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
