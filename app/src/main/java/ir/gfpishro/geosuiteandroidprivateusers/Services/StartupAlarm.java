package ir.gfpishro.geosuiteandroidprivateusers.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ir.gfpishro.geosuiteandroidprivateusers.Activities.InitActivity;

public class StartupAlarm extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
                Intent serviceIntent = new Intent(context, InitActivity.class);
                serviceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(serviceIntent);
            }
        } catch (Exception e) {
            com.orhanobut.logger.Logger.e(e, this.getClass().getName());
        }
    }
}