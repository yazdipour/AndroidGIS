package ir.gfpishro.geosuiteandroidprivateusers.Controls;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import ir.gfpishro.geosuiteandroidprivateusers.Activities.MissionBottomSheet;
import ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.MapHandler;
import ir.gfpishro.geosuiteandroidprivateusers.R;

public class MissionCardControl extends LinearLayout {
    private TextView tvDistance, tvTitle;

    public MissionCardControl(Context context) {
        super(context);
        Init(context);
    }

    public MissionCardControl(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Init(context);
    }

    public MissionCardControl(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Init(context);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        super.setOnClickListener(l);
        findViewById(R.id.fab_m_nav).setOnClickListener(l);
        findViewById(R.id.fab_m_stop).setOnClickListener(l);
    }


    private void Init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View view = inflater.inflate(R.layout.control_mission_card, this, true);
        tvDistance = view.findViewById(R.id.tv_distance);
        tvTitle = view.findViewById(R.id.tv_mission_name);
    }

    public void setTexts(String title, String distance) {
        tvDistance.setText(distance);
        tvTitle.setText(title);
    }

    public void Close(MissionBottomSheet missionBottomSheet, MapHandler mapHandler) {
        try {
            if (mapHandler.missionNavigationLayer != null) mapHandler.missionNavigationLayer.hide(mapHandler.markerHandler);
            this.setVisibility(GONE);
            if (missionBottomSheet != null) missionBottomSheet.dismiss();
        } catch (Exception ignored) {
        }
    }
}
