package ir.gfpishro.geosuiteandroidprivateusers.Helpers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import ir.gfpishro.geosuiteandroidprivateusers.R;

public class NotificationHandler {
    public final static int IMPORTANT_NOTIFICATION_ID = 99;

    public static void build(Context context, Class<?> cls,
                             String title, String content,
                             int nId, boolean important) {
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, new Intent(context, cls),
                important ? PendingIntent.FLAG_UPDATE_CURRENT : PendingIntent.FLAG_ONE_SHOT);
        int iconId = important ? R.drawable.ic_notifications_active_red_24dp : R.drawable.ic_notifications_black_24dp;
        String channelId = "GAS";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(iconId)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), iconId))
                .setColor(Color.RED)
                .setAutoCancel(false)
                .setChannelId(channelId)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setPriority(important ? NotificationCompat.PRIORITY_MAX : NotificationCompat.PRIORITY_LOW);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (important) {
//            Uri uri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.notifysnd);
            Uri uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.getPackageName() + "/" + R.raw.notifysnd);
            builder.setOngoing(true);
            builder.setSound(uri);
            NotificationChannel _notificationChannel = buildNotificationChannel(channelId, manager, uri);
            if (_notificationChannel != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                manager.createNotificationChannel(_notificationChannel);
        }

        Notification n = builder.build();
        if (manager != null)
            manager.notify((nId == -1) ? (int) System.currentTimeMillis() : nId, n);
    }

    public static void remove(Context ctx, int notifyId) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
        if (nMgr != null) nMgr.cancel(notifyId);
    }

    public boolean exists(Context ctx, Class<?> cls, int id) {
        Intent intent = new Intent(ctx, cls);
        intent.setAction(Intent.ACTION_VIEW);
        PendingIntent test = PendingIntent.getBroadcast(ctx, id, intent, PendingIntent.FLAG_NO_CREATE);
        return test != null;
    }

    private static NotificationChannel buildNotificationChannel(String channelId,
                                                                NotificationManager manager,
                                                                Uri uri) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return null;
            NotificationChannel _notificationChannel
                    = new NotificationChannel(channelId, "Important", NotificationManager.IMPORTANCE_HIGH);
            AudioAttributes att = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            _notificationChannel.enableLights(true);
            _notificationChannel.enableVibration(true);
            _notificationChannel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            _notificationChannel.setShowBadge(true);
            _notificationChannel.setSound(uri, att);
            return _notificationChannel;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
