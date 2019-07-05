package ir.gfpishro.geosuiteandroidprivateusers.Helpers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import ir.gfpishro.geosuiteandroidprivateusers.Controls.UiControl;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Log.AppLog;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Log.LogType;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Log.TeamLocation;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Mission.Mission;
import ir.gfpishro.geosuiteandroidprivateusers.Models.User;

public class AppLogConverter {

    static List<Mission> appLogsToMissions(AppLog[] logs) {
        List<Mission> missions = new ArrayList<>();
        Gson gson = new Gson();
        for (AppLog appLog : logs)
            missions.add(gson.fromJson(appLog.getQuery(), Mission.class));
        return missions;
    }

    static AppLog missionsToAppLog(List<Mission> missions, User user) {
        if (user == null) {
            Logger.e("AppLogConverter", "missionsToAppLog: NULL User");
            return null;
        }
        AppLog log = new AppLog(user.getId(), user.getmId(null),
                Utils.getUnixTime(), LogType.MISSION, null);
        log.setQuery(missions.toArray(new Mission[missions.size()]));
        return log;
    }

    public static List<TeamLocation> appLogToTeamLocation(AppLog[] data) {
        List<TeamLocation> teamLocations = new ArrayList<>();
        for (AppLog log : data) teamLocations.add(appLogToTeamLocation(log));
        return teamLocations;
    }

    private static TeamLocation appLogToTeamLocation(AppLog log) {
        TeamLocation geo = new TeamLocation();
        geo.setDateTime(log.getTime().intValue());
        String[] q = log.getQuery().split(",");
        geo.setLon(Double.parseDouble(q[0]));
        geo.setLat(Double.parseDouble(q[1]));
        try {
            geo.setStatus(Boolean.valueOf(q[2]));
        } catch (Exception e) {
            geo.setStatus(false);
        }
        geo.setPhoneId(log.getMid());
        return geo;
    }

    static AppLog reportToAppLog(List<UiControl> uiControls, User user) {
        AppLog appLog = new AppLog(user.getId(), user.getmId(null),
                Utils.getUnixTime(), LogType.REPORT, null);
        appLog.setQuery(uiControls);
        return appLog;
    }

    public static AppLog editLayerToAppLog(JsonArray geoJson, User user) {
        return new AppLog(user.getId(), user.getmId(null),
                Utils.getUnixTime(), LogType.EDIT_LAYER, geoJson.toString());
    }
}
