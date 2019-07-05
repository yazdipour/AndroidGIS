package ir.gfpishro.geosuiteandroidprivateusers.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import ir.gfpishro.geosuiteandroidprivateusers.Helpers.Utils;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Steal;
import ir.gfpishro.geosuiteandroidprivateusers.R;

public class StealViewHolder extends RecyclerView.ViewHolder {

    private final TextView tv1, tv2, tv5, tv4;

    StealViewHolder(View row) {
        super(row);
        tv1 = row.findViewById(R.id.tv1);
        tv2 = row.findViewById(R.id.tv2);
        tv4 = row.findViewById(R.id.tv4);
        tv5 = row.findViewById(R.id.tv5);
    }

    public void bindData(final Steal steal, final StealAdapter.OnItemClickListener listener) {
        itemView.setOnClickListener(v -> listener.onItemClick(steal));
        try {
            tv1.setText(String.format(itemView.getResources().getString(R.string.steal_tv1), steal.getId()));
            tv2.setText(String.format(itemView.getResources().getString(R.string.steal_tv2), steal.getStealType()));
            tv4.setText(String.format(itemView.getResources().getString(R.string.steal_tv4), Utils.unixToPersianDate(steal.getStealDate(), true, true)));
            tv5.setText(String.format(itemView.getResources().getString(R.string.steal_tv5), steal.getDescription()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
