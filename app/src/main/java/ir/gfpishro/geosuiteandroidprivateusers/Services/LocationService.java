package ir.gfpishro.geosuiteandroidprivateusers.Services;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;

import com.orhanobut.logger.Logger;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationAccuracy;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.location.providers.LocationManagerProvider;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.CacheHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.Utils;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Log.AppLog;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Log.LogType;
import ir.gfpishro.geosuiteandroidprivateusers.Models.User;

public class LocationService extends Service implements OnLocationUpdatedListener {
    private final String TAG = "LocationService";
    private LocationParams builder;
    private static boolean permission;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!permission)
            Logger.d(TAG, "NO LOCATION PERMISSION");
        else
            SmartLocation.with(this).location(new LocationManagerProvider()).config(builder).start(this);
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        float LOCATION_DISTANCE = 0.1f;
        int LOCATION_INTERVAL = 5000;
        permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        builder = new LocationParams.Builder()
                .setAccuracy(LocationAccuracy.HIGH)
                .setDistance(LOCATION_DISTANCE)
                .setInterval(LOCATION_INTERVAL)
                .build();
    }

    @Override
    public void onLocationUpdated(Location location) {
        User user = User.getCurrentUser(this);
        int userId = -1;
        try {
            userId = user.getId();
        } catch (Exception e) {
            Logger.e(e, "No Auth on LocationService");
        }
        try {
            AppLog log = new AppLog(userId, User.getPhoneId(this), Utils.getUnixTime(), LogType.LOCATION, null);
            log.setQuery(String.format("%s,%s,%s", location.getLongitude(), location.getLatitude(), user.getBusyWithMission()));
            if (userId != -1) (CacheHandler.getHandler(this)).push(log, false);
            else Logger.json(log.getQuery());
        } catch (Exception e) {
            Logger.e(e, TAG);
        }
    }
}