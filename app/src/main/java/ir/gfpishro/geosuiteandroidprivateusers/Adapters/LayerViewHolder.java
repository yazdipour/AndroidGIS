package ir.gfpishro.geosuiteandroidprivateusers.Adapters;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Switch;

import ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.LayerSchema;
import ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.MapHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.SettingsHandler;
import ir.gfpishro.geosuiteandroidprivateusers.R;

class LayerViewHolder extends RecyclerView.ViewHolder {
    private Switch mSwitch;
    private ImageButton ib_rm, ib_zoom;

    LayerViewHolder(View itemView) {
        super(itemView);
        mSwitch = itemView.findViewById(R.id.switch1);
        ib_rm = itemView.findViewById(R.id.ib_rm);
        ib_zoom = itemView.findViewById(R.id.ib_zoom);
    }

    void bindData(final LayerSchema item, final LayerAdapter.Events listener, final int position) {
        boolean hide = item.getVisibility() != View.VISIBLE || (!item.getAccessible() && !MapHandler.godMode);
        if (!hide)
            hide = item.getType() == LayerSchema.LayerType.online && !SettingsHandler.getSettings(null).isOnline;
        if (hide) {
            itemView.setVisibility(View.GONE);
            itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            return;
        }
        ib_zoom.setVisibility(View.GONE);
        ib_rm.setVisibility(View.GONE);
        Drawable img = null;
        switch (item.getType()) {
            case online:
                if (item.getFormatter().equalsIgnoreCase("wms"))
                    img = itemView.getResources().getDrawable(R.drawable.world_color);
                else img = itemView.getResources().getDrawable(R.drawable.world_internet);
                break;
            case pg_pipe:
            case pg_point:
            case bg_pipe:
            case parcel:
            case riser:
                img = itemView.getResources().getDrawable(R.drawable.world_bw);
                if (item.isEnable()) ib_zoom.setVisibility(View.VISIBLE);
                ib_zoom.setOnClickListener(v -> listener.onZoomClicked(item));
                break;
            case eis:
            case search:
                img = itemView.getResources().getDrawable(item.getType() ==
                        LayerSchema.LayerType.search ?
                        R.drawable.ic_search_white_24dp :
                        R.drawable.ic_format_shapes_black_24dp);
                img.setTint(Color.BLACK);
                if (item.isEnable()) ib_zoom.setVisibility(View.VISIBLE);
                ib_zoom.setOnClickListener(v -> listener.onZoomClicked(item));
                break;
            case online_nav:
                img = itemView.getResources().getDrawable(R.drawable.ic_track_dark);
                img.setTint(Color.BLACK);
                break;
        }
        if (img != null) mSwitch.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null);
        //Visible Switch
        mSwitch.setText(item.getTitle());
        mSwitch.setVisibility(View.VISIBLE);
        mSwitch.setChecked(item.isEnable());
        mSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> listener.onSwitch(item, isChecked));
        //Rm Button
        if (!item.isReadOnly()) {
            ib_rm.setVisibility(View.VISIBLE);
            ib_rm.setOnClickListener(v -> listener.onRmClicked(item));
        }
    }
}
