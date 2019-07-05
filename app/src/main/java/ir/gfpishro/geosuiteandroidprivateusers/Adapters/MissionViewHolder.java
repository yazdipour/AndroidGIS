package ir.gfpishro.geosuiteandroidprivateusers.Adapters;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import ir.gfpishro.geosuiteandroidprivateusers.Helpers.StringUtils;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.Utils;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Keys;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Mission.Mission;
import ir.gfpishro.geosuiteandroidprivateusers.R;

class MissionViewHolder extends RecyclerView.ViewHolder {
    private LinearLayout layout;
    private ImageView notify, mini_notify;

    MissionViewHolder(View itemView) {
        super(itemView);
        layout = itemView.findViewById(R.id.mainLayout);
        notify = itemView.findViewById(R.id.notify);
        mini_notify = itemView.findViewById(R.id.mini_notify);
    }

    void bindData(final Mission mission, final MissionAdapter.OnItemClickListener listener) {
        notify.setImageDrawable(itemView.getResources().getDrawable(R.drawable.ic_notifications_black_24dp));
        try {
            String dName = "issue_" + mission.getIssueType().getIssueId();
            int dId = StringUtils.getIdFromString(dName, R.drawable.class);
            notify.setImageDrawable(itemView.getResources().getDrawable(dId));
        } catch (Exception ignored) {
        }
        SharedPreferences pref = Utils.getSharedPref(itemView.getContext());
        final boolean pending = pref.getString(Keys.report(mission.getId()), "").length() > 2;
        final boolean hasMissingFields = pref.getString(Keys.missingInReport(mission.getId()), "").length() > 1;
        itemView.setAlpha(1f);
        if (pending) {
            if (hasMissingFields) {
                notify.setImageDrawable(itemView.getResources().getDrawable(R.drawable.ic_error_outline_black_24dp));
            } else {
                notify.setImageDrawable(itemView.getResources().getDrawable(R.drawable.ic_check_black_24dp));
                itemView.setAlpha(0.5f);
            }
        } else if (mission.getIssueType().getPriority() == 1) {
            mini_notify.setImageTintList(ColorStateList.valueOf(Color.RED));
            mini_notify.setVisibility(View.VISIBLE);
            notify.setImageTintList(ColorStateList.valueOf(Color.RED));
        } else if (mission.getIssueType().getPriority() == 2) {
            mini_notify.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), R.color.primary_dark)));
            mini_notify.setVisibility(View.VISIBLE);
        }
        itemView.setOnClickListener(view -> listener.onItemClick(mission, pending, hasMissingFields));
        layout.removeAllViews();
        if (mission.getRequester().getId() != -1) {
            getTextView("نام مشترک", String.valueOf(mission.getRequester().getName()));
            getTextView("فامیل مشترک", String.valueOf(mission.getRequester().getLast()));
            getTextView("کد اشتراک", String.valueOf(mission.getRequester().getCuNum()));
        }
        getTextView("نام", String.valueOf(mission.getName()));
        getTextView("آدرس درخواست", String.valueOf(mission.getAddress()));
        getTextView("زمان گزارش حادثه", Utils.unixToPersianDate(mission.getNoticeDate(), true, true));
        getTextView("توضیحات", String.valueOf(mission.getDescription()));
        getTextView("منطقه امداد", String.valueOf(mission.getEmergencyZone().getName()));
        getTextView("نوع حادثه", String.valueOf(mission.getIssueType().getLable()));
        getTextView("زمان حادثه", Utils.unixToPersianDate(mission.getEventDate(), true, true));
    }

    private void getTextView(String s, String s2) {
        TextView tv = new TextView(itemView.getContext());
        tv.setText(String.format("%s: %s", s, s2));
        layout.addView(tv);
    }
}
