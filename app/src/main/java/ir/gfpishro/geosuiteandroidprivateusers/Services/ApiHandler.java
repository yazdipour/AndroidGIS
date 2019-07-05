package ir.gfpishro.geosuiteandroidprivateusers.Services;

import java.util.concurrent.TimeUnit;

import ir.gfpishro.geosuiteandroidprivateusers.BuildConfig;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.SettingsHandler;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Settings;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiHandler {
    private static Api api;
    public static int timeOut = 60;

    public static Api getApi(String ip) {
        if (api == null) {
            try {
                OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                        .connectTimeout(timeOut, TimeUnit.SECONDS)
                        .readTimeout(timeOut, TimeUnit.SECONDS)
                        .writeTimeout(timeOut, TimeUnit.SECONDS);
                if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
                    httpClient.addInterceptor(loggingInterceptor);
                }
                httpClient.networkInterceptors().add(chain -> chain.proceed(chain.request().newBuilder()
                        .header(api.agent[0], api.agent[1])
                        .header(api.contentType[0], api.contentType[1])
                        .method(chain.request().method(), chain.request().body())
                        .build()));
                Retrofit.Builder retrofit = new Retrofit.Builder()
                        .client(httpClient.build())
                        .addConverterFactory(GsonConverterFactory.create());
                try {
                    retrofit.baseUrl(ip);
                } catch (IllegalArgumentException e) {
                    SettingsHandler.resetSettings();
                    retrofit.baseUrl(new Settings().serverIp);
                }
                api = retrofit.build().create(Api.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return api;
    }

    public static void resetApi() {
        api = null;
    }
}
