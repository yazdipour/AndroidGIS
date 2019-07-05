package ir.gfpishro.geosuiteandroidprivateusers.Helpers;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ir.gfpishro.geosuiteandroidprivateusers.Activities.MainActivity;
import ir.gfpishro.geosuiteandroidprivateusers.Activities.MissionsActivity;
import ir.gfpishro.geosuiteandroidprivateusers.Controls.UiControl;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Keys;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Log.AppLog;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Log.LogType;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Mission.Mission;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Settings;
import ir.gfpishro.geosuiteandroidprivateusers.Models.User;
import ir.gfpishro.geosuiteandroidprivateusers.R;
import ir.gfpishro.geosuiteandroidprivateusers.Services.ApiHandler;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static ir.gfpishro.geosuiteandroidprivateusers.Services.ApiHandler.getApi;

public class MissionHandler {
    private List<Mission> missions = new ArrayList<>();
    private Settings settings;
    private String TAG = MissionHandler.class.getName();
    private Context context;

    public MissionHandler(Context context) {
        this.context = context;
        settings = SettingsHandler.getSettings(context);
    }

    private void setMissionsCounter(int count) {
        try {
            MainActivity.counterFab.setCount(count);
        } catch (Exception ignored) {
        }
    }

    private void cacheMissions(List<Mission> missions, final User user) throws Exception {
        (CacheHandler.getHandler(context)).push(AppLogConverter.missionsToAppLog(missions, user), true);
    }

    private void getOnlineMissions(final User user) {
        ApiHandler.getApi(settings.serverIp)
                .getMissions(User.getCredential(), user.getmId(context))
                .enqueue(new Callback<List<Mission>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Mission>> call, @NonNull Response<List<Mission>> response) {
                        if (response.isSuccessful()) update(response.body());
                        else Logger.e(TAG, "onResponse: " + response.code());
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Mission>> call, @NonNull Throwable t) {
                        Logger.e(t, TAG);
                    }
                });
    }

    private void getOfflineMissions(final User user) {
        try {
            AppLog[] pulledLogs = (CacheHandler.getHandler(context)).pullArray(LogType.MISSION, user.getId());
            missions = AppLogConverter.appLogsToMissions(pulledLogs);
            Logger.json(new Gson().toJson(missions));
            saveMissionsInPref();
        } catch (Exception e) {
            Logger.e(e, TAG);
        }
    }

    private void saveMissionsInPref() {
        SharedPreferences pref = Utils.getSharedPref(context);
        pref.edit().putLong(Keys.lastMissionUpdate(User.getCurrentUser(context).getId()), Utils.getUnixTime()).apply();
        pref.edit().putString(Keys.missionsList(User.getCurrentUser(context).getId()), new Gson().toJson(missions)).apply();
    }

    public void update(List<Mission> missionList) {
        if (missionList == null) missionList = new ArrayList<>();
        Collections.sort(missionList, (o1, o2) -> o1.getIssueType().getPriority() - (o2.getIssueType().getPriority()));
        missions.clear();
        missions.addAll(missionList);
        setMissionsCounter(missions.size());
        if (missions.size() == 0 || User.getCurrentUser(context).getBusyWithMission()) {
            NotificationHandler.remove(context, NotificationHandler.IMPORTANT_NOTIFICATION_ID);
            return;
        }
        try {
            saveMissionsInPref();
            Mission hasImportantMission = null;
            for (Mission mission : missions)
                if (mission.getIssueType().getPriority() == 1 && (Utils.getSharedPref(context).getString(Keys.report(mission.getId()), "").length() == 0)) {
                    hasImportantMission = mission;
                    break;
                }
            if (hasImportantMission != null && !User.getCurrentUser(context).getBusyWithMission())
                NotificationHandler.build(context, MissionsActivity.class, "حادثه مهم",
                        String.format("نوع حادثه: %s زمان: %s",
                                hasImportantMission.getIssueType().getLable(),
                                hasImportantMission.getEventDate()),
                        hasImportantMission.getId() + 100, true);
            else NotificationHandler.build(context,
                    MissionsActivity.class,
                    context.getResources().getString(R.string.tv_mission),
                    missions.size() + " ماموریت انجام نشده",
                    NotificationHandler.IMPORTANT_NOTIFICATION_ID,
                    false);
            cacheMissions(missions, User.getCurrentUser(context));
        } catch (Exception e) {
            Logger.e(e, TAG);
        }
    }

    public void pullMissions(final User user) {
        try {
            if (!PermissionHelper.haveFormPermission(MissionsActivity.FORM_CODE)) {
                NotificationHandler.remove(context, NotificationHandler.IMPORTANT_NOTIFICATION_ID);
                return;
            }
            if (!settings.isOnline) throw new NetworkErrorException("Offline");
            getOnlineMissions(user);
        } catch (Exception e) {
            getOfflineMissions(user);
        }
    }

    // REPORT
    public void cacheReport(final User user, List<UiControl> uiControls)
            throws Exception {
        AppLog log = AppLogConverter.reportToAppLog(uiControls, user);
        CacheHandler.getHandler(context).push(log, false);
    }

    public void pushReports(final User user) throws Exception {
        final AppLog[] data = CacheHandler.getHandler(context).pullArray(LogType.REPORT, user.getId());
        if (data == null || data.length == 0) return;
        final Gson gson = new Gson();
        final List<UiControl[]> report = new ArrayList<>();
        for (AppLog log : data) report.add(gson.fromJson(log.getQuery(), UiControl[].class));
        final SharedPreferences pref = Utils.getSharedPref(context);
        getApi(settings.serverIp).postReport(User.getCredential(), report)
                .enqueue(new Callback<JsonArray>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonArray> call, @NonNull Response<JsonArray> response) {
                        if (!response.isSuccessful()) {
                            try {
                                NotificationHandler.build(context, MissionsActivity.class, context.getString(R.string.msg_err_report) + " Code:" + response.code(), "دوباره تلاش کنید", -1, false);
                                (CacheHandler.getHandler(context)).remove(LogType.REPORT, user.getId());
                            } catch (Exception e) {
                                e.printStackTrace();
                                Logger.e(e, getClass().getName() + ".pushReports");
                            }
                        } else {
                            try {
                                JsonArray result = response.body();
                                if (result != null) {
                                    for (int i = 0; i < data.length; i++) {
                                        final Integer mission_id = result.get(i).getAsJsonObject().get("mission_id").getAsInt();
                                        final JsonElement message = result.get(i).getAsJsonObject().get("message");
                                        if (message != null && !message.isJsonArray() && message.getAsString().equals("ok")) {
                                            pref.edit().remove(Keys.report(mission_id)).apply();
                                            pref.edit().remove(Keys.missingInReport(mission_id)).apply();
                                        } else if (message != null) {
                                            JsonArray missing = message.getAsJsonArray();
                                            pref.edit().putString(Keys.missingInReport(mission_id), missing.toString()).apply();
                                            NotificationHandler.build(context, MissionsActivity.class,
                                                    context.getString(R.string.msg_err_report), "شماره ماموریت " + mission_id, -1, false);
                                        }
                                    }
                                }
                                (CacheHandler.getHandler(context)).remove(LogType.REPORT, user.getId());
                            } catch (Exception e) {
                                onFailure(call, e);
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonArray> call, @NonNull Throwable t) {
                        Logger.e(t, TAG);
                    }
                });
    }
}
