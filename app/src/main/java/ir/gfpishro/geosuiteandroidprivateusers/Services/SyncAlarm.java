package ir.gfpishro.geosuiteandroidprivateusers.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ir.gfpishro.geosuiteandroidprivateusers.Helpers.ServiceHandler;

public class SyncAlarm extends BroadcastReceiver {
    public static final int REQUEST_CODE = 999;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            ServiceHandler.startService(context, SyncService.class);
        } catch (Exception e) {
            com.orhanobut.logger.Logger.e(e, this.getClass().getName());
        }
    }
}
