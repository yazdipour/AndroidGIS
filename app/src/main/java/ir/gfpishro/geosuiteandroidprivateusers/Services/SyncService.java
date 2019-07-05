package ir.gfpishro.geosuiteandroidprivateusers.Services;

import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.orhanobut.logger.Logger;

import java.util.List;

import ir.gfpishro.geosuiteandroidprivateusers.Helpers.AppLogConverter;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.CacheHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.MissionHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.SettingsHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.Utils;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Keys;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Log.AppLog;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Log.LogType;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Log.TeamLocation;
import ir.gfpishro.geosuiteandroidprivateusers.Models.ServerStatus;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Steal;
import ir.gfpishro.geosuiteandroidprivateusers.Models.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static ir.gfpishro.geosuiteandroidprivateusers.Services.ApiHandler.getApi;

public class SyncService extends Service {
    private final String TAG = SyncService.class.getCanonicalName();
    private User user;
    private boolean oneTimeSync = false;
    private Gson gson = new Gson();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        user = User.getCurrentUser(this);
        try {
            if (user == null)
                throw new Exception("Invalid User: NullUser");
            if (user.getId() == null || user.getId() == -1)
                throw new Exception("Invalid User:" + user.getId());
            if (!oneTimeSync) getStatics();
            MissionHandler missionHandler = new MissionHandler(this);
            for (LogType type : LogType.values()) sync(type, missionHandler);
        } catch (Exception e) {
            Logger.e(e, TAG, user);
        }
        return START_STICKY;
    }

    private void sync(LogType type, MissionHandler missionHandler) {
        try {
            if (!SettingsHandler.getSettings(this).isOnline)
                throw new NetworkErrorException("Offline_SyncService G74");
            switch (type) {
                case REPORT:
                    missionHandler.pushReports(user);
                    break;
                case MISSION:
                    missionHandler.pullMissions(user);
                    break;
                case LOCATION:
                    pushLocation();
                    break;
                case EDIT_LAYER:
                    final AppLog[] data = CacheHandler.getHandler(this).pullArray(LogType.EDIT_LAYER, user.getId());
                    if (data == null) pullEditLayer();
                    else pushEditLayer(data);
                    break;
                case STEAL:
                    pushSteals();
                    break;
            }
        } catch (NetworkErrorException ne) {
            ne.printStackTrace();
        } catch (Exception e) {
            Logger.e(TAG, type.name() + "-sync: ", e);
        }
    }

    public void pushSteals() throws Exception {
        final AppLog[] data = CacheHandler.getHandler(this).pullArray(LogType.STEAL, user.getId());
        if (data == null || data.length == 0) return;
        final Steal[] steals = new Steal[data.length];
        for (int i = 0; i < data.length; i++)
            steals[i] = gson.fromJson(data[i].getQuery(), Steal.class);
        getApi("").postSteal(User.getCredential(), steals)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        try {
                            if (!response.isSuccessful())
                                throw new Exception("ServerCode:" + response.code());
                            (CacheHandler.getHandler(SyncService.this)).remove(LogType.STEAL, user.getId());
                        } catch (Exception e) {
                            e.printStackTrace();
                            Logger.e(e, getClass().getName() + ".pushSTEAL");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Logger.e(t, TAG);
                    }
                });
    }


    public void pushEditLayer(AppLog[] data) throws Exception {
        if (data == null)
            data = CacheHandler.getHandler(this).pullArray(LogType.EDIT_LAYER, user.getId());
        if (data == null) return;
        JsonArray layers = gson.fromJson(data[0].getQuery(), JsonArray.class);
        final AppLog[] finalData = data;
        getApi("").postEditLayers(User.getCredential(), layers).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                try {
                    if (!response.isSuccessful())
                        throw new Exception("ServerError: " + response.code());
                    CacheHandler.getHandler(SyncService.this).remove(finalData[0]);
                } catch (Exception e) {
                    onFailure(call, new Throwable(response.headers().toString()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Logger.e(t, TAG, t.getMessage());
            }
        });
    }

    private void pullEditLayer() {
        getApi("").getEditLayers(User.getCredential(), user.getId()).enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(@NonNull Call<JsonArray> call, @NonNull Response<JsonArray> response) {
                try {
                    if (!response.isSuccessful())
                        throw new Exception("ServerError:" + response.code());
                    JsonArray res = response.body();
                    if (res == null || res.size() == 0) return;
                    String str = res.toString();
                    Utils.getSharedPref(SyncService.this).edit().putString(Keys.editLayer(user.getId()), str).apply();
                } catch (Exception e) {
                    onFailure(call, new Throwable(response.headers().toString()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonArray> call, @NonNull Throwable t) {
                Logger.e(t, TAG, "pull edit layers");
            }
        });
    }

    private void pushLocation() throws Exception {
        AppLog[] data = CacheHandler.getHandler(this).pullArray(LogType.LOCATION, user.getId());
        if (data == null || (data.length < 1)) return;
        List<TeamLocation> teamLocations = AppLogConverter.appLogToTeamLocation(data);
        if (teamLocations.size() < 1) return;
        getApi(SettingsHandler.getSettings(this).serverIp)
                .postLocation(User.getCredential(), teamLocations)
                .enqueue(new Callback<ServerStatus>() {
                    @Override
                    public void onResponse(@NonNull Call<ServerStatus> call, @NonNull Response<ServerStatus> response) {
                        try {
                            if (!response.isSuccessful())
                                throw new Exception(String.valueOf(response.code()));
                            (CacheHandler.getHandler(SyncService.this)).remove(LogType.LOCATION, user.getId());
                        } catch (Exception e) {
                            onFailure(call, e);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ServerStatus> call, @NonNull Throwable t) {
                        Logger.e(t, TAG);
                    }
                });
    }

    private void getStatics() {
        final String userStaticsKey = Keys.statics(user.getId());
        final SharedPreferences pref = Utils.getSharedPref(this);
        ApiHandler.getApi("").getStatics(User.getCredential()).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                if (!response.isSuccessful()) return;
                if (response.body() == null) return;
                JsonObject statics = response.body().getAsJsonObject();
                //MARKETING_CODE
                try {
                    StringBuilder MARKETING_CODE = new StringBuilder();
                    for (JsonElement j : statics.get(Keys.MARKETING_CODE).getAsJsonArray())
                        MARKETING_CODE.append(",").append(j.getAsJsonObject().get("name").getAsString());
                    if (MARKETING_CODE.length() > 0)
                        pref.edit().putString(userStaticsKey + Keys.MARKETING_CODE, MARKETING_CODE.substring(1)).apply();
                } catch (Exception e) {
                    e.printStackTrace();
                    Logger.e(e, "SS193");
                }
                //Steal_TYPE
                try {
                    StringBuilder STEAL_TYPE = new StringBuilder();
                    StringBuilder STEAL_TYPE2 = new StringBuilder();
//                    for (String[] s : gson.fromJson(statics.get(Keys.STEAL_TYPE).toString(), String[][].class)) {
//                        STEAL_TYPE2.append(",").append(s[0]);
//                        STEAL_TYPE.append(",").append(s[1]);
//                    }
//                    pref.edit().putString(userStaticsKey + Keys.STEAL_TYPE, STEAL_TYPE.substring(1)).apply();
//                    pref.edit().putString(userStaticsKey + Keys.STEAL_TYPE + 2, STEAL_TYPE2.substring(1)).apply();
                } catch (Exception e) {
                    e.printStackTrace();
                    Logger.e(e, "SS239");
                }
                oneTimeSync = true;
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                Logger.e(t, "Could not get Statics");
            }
        });
    }
}
