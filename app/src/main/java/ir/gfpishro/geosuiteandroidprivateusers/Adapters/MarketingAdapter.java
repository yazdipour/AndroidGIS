package ir.gfpishro.geosuiteandroidprivateusers.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import ir.gfpishro.geosuiteandroidprivateusers.R;

public class MarketingAdapter extends RecyclerView.Adapter<MarketingViewHolder> {

    private final List<String> data;
    private final Events listener;

    public interface Events {
        void onBtnClicked(final String self,final int position);
    }

    public MarketingAdapter(List<String> data, Events listener) {
        this.data = data;
        this.listener = listener;
    }

    @Override
    public MarketingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MarketingViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_marketing, parent, false));
    }

    @Override
    public void onBindViewHolder(MarketingViewHolder holder, int position) {
        holder.bindData(data.get(position), listener, position);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}