package ir.gfpishro.geosuiteandroidprivateusers.Activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import org.oscim.core.GeoPoint;
import org.xml.sax.ContentHandler;

import java.util.Objects;

import ir.gfpishro.geosuiteandroidprivateusers.Helpers.Utils;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Mission.Mission;
import ir.gfpishro.geosuiteandroidprivateusers.R;

public class MissionBottomSheet extends BottomSheetDialogFragment implements View.OnClickListener {
    private Mission mission;
    private LinearLayout layout;
    private View.OnClickListener onClickListener;

    public void setMission(Mission mission) {
        this.mission = mission;
    }

    public Mission getMission() {
        return mission;
    }

    public GeoPoint getMissionGeoPoint() {
        return new GeoPoint(mission.getLat(), mission.getLon());
    }

    private TextView getTextView(String text1, String text2) {
        TextView textView = new TextView(getContext());
        textView.setTextSize(18);
        textView.setText(String.format("%s: %s", text1, text2));
        return textView;
    }

    private void setMissionLayout() {
        layout.removeAllViews();
        try {
            layout.addView(getTextView("کد ماموریت", String.valueOf(mission.getId())));
            layout.addView(getTextView("تیم امداد", String.valueOf(mission.getTeam().getName())));
//            layout.addView(getTextView("اقدام کننده", String.valueOf(mission.getResponsible().getName())));
            if (mission.getRequester().getId() != -1) {
                layout.addView(getTextView("نام مشترک", String.valueOf(mission.getRequester().getName())));
                layout.addView(getTextView("فامیل مشترک", String.valueOf(mission.getRequester().getLast())));
                layout.addView(getTextView("کد اشتراک", String.valueOf(mission.getRequester().getCuNum())));
            }
            layout.addView(getTextView("آدرس درخواست", String.valueOf(mission.getAddress())));
            layout.addView(getTextView("زمان تماس", Utils.unixToPersianDate(mission.getEventDate(), true, true)));
            layout.addView(getTextView("زمان ارجاع ماموریت", Utils.unixToPersianDate(mission.getNoticeDate(), true, true)));
            layout.addView(getTextView("زمان شروع ماموریت", Utils.unixToPersianDate(mission.getStartDate(), true, true)));
            layout.addView(getTextView("اولویت حادثه", String.valueOf(mission.getIssueType().getPriority())));
            layout.addView(getTextView("نوع حادثه", String.valueOf(mission.getIssueType().getLable())));
            layout.addView(getTextView("نام", String.valueOf(mission.getName())));
            layout.addView(getTextView("منطقه امداد", String.valueOf(mission.getEmergencyZone().getName())));
            layout.addView(getTextView("شماره تلفن", String.valueOf(mission.getPhone())));
            layout.addView(getTextView("توضیحات", String.valueOf(mission.getDescription())));
        } catch (Exception ignored) {
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View view = View.inflate(getContext(), R.layout.bottom_mission, null);
        dialog.setContentView(view);
        layout = view.findViewById(R.id.lv_info);
        setMissionLayout();
        view.findViewById(R.id.btn_reject).setOnClickListener(this);
        view.findViewById(R.id.btn_abort).setOnClickListener(this);
        view.findViewById(R.id.btn_stop).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        onClickListener.onClick(v);
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
