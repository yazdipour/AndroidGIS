package ir.gfpishro.geosuiteandroidprivateusers.Helpers;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class StringUtils {

    public static boolean isNullOrEmpty(String json) {
        return json == null || json.length()==0;
    }

    public static String getRawString(Context context, int id) throws IOException {
        InputStream inputStream = context.getResources().openRawResource(id);
        StringBuilder strBuild = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                strBuild.append(line);
            }
        }
        return strBuild.toString();
    }

    public static int getIdFromString(String resourceName, Class<?> c) throws Exception {
        Field idField = c.getDeclaredField(resourceName);
        return idField.getInt(idField);
    }

    public static String farsiNumbersToEnglish(String farsiStr) {
        String[] english = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        String[] farsi = new String[]{"۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹"};
        for (int i = 0; i < farsi.length; i++)
            farsiStr = farsiStr.replaceAll(farsi[i], english[i]);
        return farsiStr;
    }

    public static String getTodayFormatterDate(String ft) {
        SimpleDateFormat df = new SimpleDateFormat(ft);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df.format(new Date());
    }
}