package ir.gfpishro.geosuiteandroidprivateusers.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.LayerSchema;
import ir.gfpishro.geosuiteandroidprivateusers.R;

public class LayerAdapter extends RecyclerView.Adapter<LayerViewHolder> {
    private List<LayerSchema> data;
    private Events listener;
    public interface Events {
        void onRmClicked(LayerSchema layer);

        void onZoomClicked(LayerSchema layer);

        void onSwitch(LayerSchema layer, boolean isChecked);
    }

    public LayerAdapter(List<LayerSchema> data, Events listener) {
        this.data = data;
        this.listener=listener;
    }
    @NonNull
    @Override
    public LayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layer, parent, false);
        return new LayerViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull LayerViewHolder holder, int position) {
        LayerSchema issue = data.get(position);
        holder.bindData(issue, listener, position);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
