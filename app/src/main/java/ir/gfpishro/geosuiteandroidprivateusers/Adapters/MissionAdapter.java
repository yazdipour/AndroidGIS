package ir.gfpishro.geosuiteandroidprivateusers.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Mission.Mission;
import ir.gfpishro.geosuiteandroidprivateusers.R;

public class MissionAdapter  extends RecyclerView.Adapter<MissionViewHolder> {
    private List<Mission> data;
    private final MissionAdapter.OnItemClickListener listener;

    public MissionAdapter(List<Mission> data, MissionAdapter.OnItemClickListener listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MissionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mission, parent, false);
        return new MissionViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull MissionViewHolder holder, int position) {
        Mission issue = data.get(position);
        holder.bindData(issue,listener);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public interface OnItemClickListener {
        void onItemClick(Mission item,boolean pending, boolean hasMissing);
    }
}
