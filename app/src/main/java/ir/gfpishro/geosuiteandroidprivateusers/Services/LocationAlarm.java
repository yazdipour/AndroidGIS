package ir.gfpishro.geosuiteandroidprivateusers.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ir.gfpishro.geosuiteandroidprivateusers.Helpers.ServiceHandler;

public class LocationAlarm extends BroadcastReceiver {
    public static final int REQUEST_CODE = 888;
    public static final int INTERVAL_SEC = 60 * 5;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ServiceHandler.isMyServiceRunning(context, LocationService.class)) {
            try {
                ServiceHandler.startService(context, LocationService.class);
            } catch (Exception e) {
                com.orhanobut.logger.Logger.e(e, this.getClass().getName());
            }
        }
    }
}