package ir.gfpishro.geosuiteandroidprivateusers.Helpers;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.BuildConfig;
import com.orhanobut.logger.CsvFormatStrategy;
import com.orhanobut.logger.DiskLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

public class LogHandler {
    public static void init() {
        //DEFAULT TAG = "PRETTY_LOGGER";
        Logger.addLogAdapter(new AndroidLogAdapter() {
            @Override
            public boolean isLoggable(int priority, String tag) {
                return BuildConfig.DEBUG;
            }
        });
        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder().build();
        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));
        FormatStrategy formatStrategy2 = CsvFormatStrategy.newBuilder().build();
        Logger.addLogAdapter(new DiskLogAdapter(formatStrategy2));
    }
}
