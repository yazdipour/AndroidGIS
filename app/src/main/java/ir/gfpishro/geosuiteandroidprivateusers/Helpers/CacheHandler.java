package ir.gfpishro.geosuiteandroidprivateusers.Helpers;

import android.content.Context;

import com.orhanobut.logger.Logger;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ir.gfpishro.geosuiteandroidprivateusers.Models.Log.AppLog;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Log.LogType;

public class CacheHandler {
    private final static String TAG = "SnappyDB";
    private static DB snappyDB;
    private static CacheHandler cacheHandler;

    private static void OpenDb(final Context context) throws SnappydbException {
        if ((snappyDB == null || !snappyDB.isOpen()) && context != null)
            snappyDB = DBFactory.open(context, "log");
    }

    public static CacheHandler getHandler(final Context context) {
        try {
            OpenDb(context);
        } catch (Exception e) {
            Logger.e(e, TAG);
        }
        return cacheHandler == null ? cacheHandler = new CacheHandler() : cacheHandler;
    }

    private String key(AppLog lg) {
        return lg.getType().name() + lg.getUid();
    }

    private String key(LogType lg, int uid) {
        return lg.name() + uid;
    }

    ///>>>>>>>>>>>>>>>> PUSH <<<<<<<<<<<<<<<<<<<//
    // Push/Save Logs to Database
    private void push(AppLog[] arr) throws SnappydbException {
        if (arr.length > 0) snappyDB.put(key(arr[0]), arr);
    }

    public void push(AppLog log, boolean replace) throws SnappydbException {
        if (log == null) return;
        List<AppLog> appLogs = new ArrayList<>();
        AppLog[] old = pullArray(log.getType(), log.getUid());
        if (old != null && !replace) Collections.addAll(appLogs, old);
        appLogs.add(log);
        push(appLogs.toArray(new AppLog[0]));
    }

    ///>>>>>>>>>>>>>>>> PULL <<<<<<<<<<<<<<<<<<<//
    public AppLog[] pullArray(LogType type, int uid) throws SnappydbException {
        String key = key(type, uid);
        if (!snappyDB.exists(key)) return null;
        return snappyDB.getObjectArray(key, AppLog.class);
//        return snappyDB.getArray(key, AppLog.class);
    }

    private AppLog pull(LogType type, int uid) throws SnappydbException {
        String key = key(type, uid);
        if (!snappyDB.exists(key)) return null;
        return snappyDB.get(key(type, uid), AppLog.class);
    }
    ///>>>>>>>>>>>>>>>> DEL <<<<<<<<<<<<<<<<<<<//

    public void remove(AppLog log) throws SnappydbException {
        snappyDB.del(key(log));
    }

    public void remove(LogType type, int uid) throws SnappydbException {
        snappyDB.del(key(type, uid));
    }
    ///>>>>>>>>>>>>>>>> DONE <<<<<<<<<<<<<<<<<<<//

    public void wipeDb() {
        try {
            snappyDB.destroy();
            snappyDB.close();
            snappyDB = null;
        } catch (Exception e) {
            Logger.e(e, TAG);
        }
    }
}
