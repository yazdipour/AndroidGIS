package ir.gfpishro.geosuiteandroidprivateusers.Helpers;

import android.content.Context;

import com.google.gson.Gson;

import ir.gfpishro.geosuiteandroidprivateusers.Models.Keys;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Settings;
import ir.gfpishro.geosuiteandroidprivateusers.Models.User;
import ir.gfpishro.geosuiteandroidprivateusers.Services.SyncService;

public class SettingsHandler {
    private static Settings settings;

    public static Settings getSettings(Context context) {
        if (settings != null) return settings;
        try {
            String json = Utils.getSharedPref(context).getString(Keys.settings, "");
            if (json == null || json.length() < 2) throw new Exception();
            settings = new Gson().fromJson(json, Settings.class);
        } catch (Exception ignored) {
            settings = new Settings();
        }
        User user = User.getCurrentUser(context);
        if (user != null && user.getCityCode() != null && !user.isAdmin())
            settings.cityCode = User.getCurrentUser(context).getCityCode();
        return settings;
    }

    public static void saveSettings(Context context) {
        Utils.getSharedPref(context).edit().putString(Keys.settings, new Gson().toJson(settings)).apply();
    }

    public static void resetSettings() {
        settings = null;
    }

    public static void setOnline(boolean online, Context context) {
        getSettings(context).isOnline = online;
        if (!online) ServiceHandler.stopService(context, SyncService.class);
    }
}
