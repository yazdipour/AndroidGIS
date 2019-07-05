package ir.gfpishro.geosuiteandroidprivateusers.Helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.Gravity;

import com.crowdfire.cfalertdialog.CFAlertDialog;
import com.google.gson.reflect.TypeToken;
import com.mohamadamin.persianmaterialdatetimepicker.utils.PersianCalendar;
import com.mohamadamin.persianmaterialdatetimepicker.utils.TimeZones;

import org.oscim.core.GeoPoint;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ir.gfpishro.geosuiteandroidprivateusers.R;

import static android.content.Context.MODE_PRIVATE;

@SuppressLint("DefaultLocale")
public class Utils {
    private static SharedPreferences sharedPreferences;

    public static long getUnixTime() {
        return System.currentTimeMillis() / 1000L;
    }

    public static String unixToPersianDate(long unix, boolean isShort, boolean isTime) {
        PersianCalendar date = unixToPersianDateMilSeconds(unix * 1000);
        if (isShort) return isTime ? date.getPersianShortDateTime() : date.getPersianShortDate();
        else return isTime ? date.getPersianLongDateAndTime() : date.getPersianLongDate();
    }

    private static PersianCalendar unixToPersianDateMilSeconds(long unixMilli) {
        PersianCalendar persianCalendar = new PersianCalendar();
        if (unixMilli == 0) persianCalendar.setTimeZone(TimeZones.ASIA_TEHRAN.getTimeZone());
        else persianCalendar.setTimeInMillis(unixMilli);
        return persianCalendar;
    }

    public static SharedPreferences getSharedPref(Context context) {
        if (sharedPreferences == null)
            sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.app_package), MODE_PRIVATE);
        return sharedPreferences;
    }

    public static void unzip(File zipFile, File targetDirectory) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)))) {
            ZipEntry ze;
            int count;
            byte[] buffer = new byte[8192];
            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(targetDirectory, ze.getName());
                File dir = ze.isDirectory() ? file : file.getParentFile();
                if (!dir.isDirectory() && !dir.mkdirs())
                    throw new FileNotFoundException("Failed to ensure directory: " + dir.getAbsolutePath());
                if (ze.isDirectory()) continue;
                try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                    while ((count = zis.read(buffer)) != -1)
                        fileOutputStream.write(buffer, 0, count);
                }
            }
        }
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }


    public static Type getListType(final Class cls) {
        return TypeToken.getParameterized(ArrayList.class, cls).getType();
    }

    public static String getDistanceFormatted(double distance, boolean square) {
        String degree = "متر";
        String prefix = (square) ? " مربع" : "";
        double threshold = Math.pow(10, (square) ? 6 : 3);
        if (distance >= threshold) {
            distance /= threshold;
            degree = "کیلومتر";
        }
        return String.format("%.2f %s %s", distance, degree, prefix);
    }

    public static String getGeom(GeoPoint p) {
        return String.format("POINT (%f %f)", p.getLongitude(), p.getLatitude());
    }

    public static CFAlertDialog.Builder getConfirmationDialog(Context context,
                                                              String title,
                                                              String message,
                                                              String btnNegative,
                                                              String btnPositive,
                                                              DialogInterface.OnClickListener listenerNegative,
                                                              DialogInterface.OnClickListener listenerPositive) {
        return new CFAlertDialog.Builder(context)
                .setDialogStyle(CFAlertDialog.CFAlertStyle.ALERT)
                .setMessage(message)
                .setTitle(title)
                .setTextGravity(Gravity.CENTER_HORIZONTAL)
                .addButton(btnNegative, -1, -1, CFAlertDialog.CFAlertActionStyle.NEGATIVE, CFAlertDialog.CFAlertActionAlignment.JUSTIFIED, listenerNegative)
                .addButton(btnPositive, -1, -1, CFAlertDialog.CFAlertActionStyle.POSITIVE, CFAlertDialog.CFAlertActionAlignment.JUSTIFIED, listenerPositive == null ? (dialog, which) -> dialog.dismiss() : listenerPositive);
    }

    public static Bitmap getThumbnail(final Uri uri) {
        return ThumbnailUtils.createVideoThumbnail(uri.getPath(), MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
    }

    public static String getPackageVersion(Context context) {
        try{
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        }catch (Exception e){
            e.printStackTrace();
            return "1.0";
        }
    }
}
