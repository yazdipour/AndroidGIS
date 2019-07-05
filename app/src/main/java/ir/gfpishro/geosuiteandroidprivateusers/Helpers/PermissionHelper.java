package ir.gfpishro.geosuiteandroidprivateusers.Helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import ir.gfpishro.geosuiteandroidprivateusers.Models.Keys;
import ir.gfpishro.geosuiteandroidprivateusers.Models.User;
import ir.gfpishro.geosuiteandroidprivateusers.R;
import ir.gfpishro.geosuiteandroidprivateusers.Services.Api;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PermissionHelper {
    private static List<Integer> forms = new ArrayList<>();
    private static List<String> layers = new ArrayList<>();

    public static boolean haveFormPermission(int formId) {
        return forms.contains(formId);
    }

    public static boolean haveLayerPermission(String layerName) {
        return layers.contains(layerName.toLowerCase());
    }

    public static SweetAlertDialog showNoPermissionMessageAlert(Context context) {
        return DialogHandler.getDialog(context, SweetAlertDialog.WARNING_TYPE,
                R.string.dont_have_permission, true,
                false, null);
    }

    private static void updateLayersPermission(Intent i, final Activity activity, final User user) {
        try {
            layers.clear();
            String jsonString = Utils.getSharedPref(activity).getString(Keys.accessibleLayers(user.getId()), "");
            String[] json = new Gson().fromJson(jsonString, String[].class);
            if (json == null || json.length == 0) return;
            for (String l : json) layers.add(l.toLowerCase());
            activity.startActivity(i);
            activity.finish();
        } catch (Exception e) {
            Logger.e(e, "Error in LayersPermission");
            Toast.makeText(activity, "خطا در گرفتن دسترسی لایه ها", Toast.LENGTH_SHORT).show();
        }
    }

    private static void updateFormsPermission(final Context context, final User user) {
        try {
            forms.clear();
            String jsonString = Utils.getSharedPref(context).getString(Keys.accessibleForms(user.getId()), "");
            JsonElement json = new Gson().fromJson(jsonString, JsonElement.class);
            if (json.isJsonNull()) return;
            for (JsonElement f : json.getAsJsonArray())
                forms.add(f.getAsJsonObject().get("form_id").getAsInt());
        } catch (Exception e) {
            Logger.e(e, "Error in FormsPermission");
            Toast.makeText(context, "خطا در گرفتن دسترسی ها", Toast.LENGTH_SHORT).show();
        }
    }

    public static void getAccessibilityForms(final Context context, final Api api, final User user) {
        if (user == null || user.getId() == null) return;
        api.getAccessibilityForms(User.getCredential(), user.getId()).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                try {
                    if (!response.isSuccessful())
                        throw new Exception(String.valueOf(response.code()));
                    if (response.body() == null)
                        throw new Exception("Empty result from server");
                    Utils.getSharedPref(context).edit().putString(Keys.accessibleForms(user.getId()),
                            response.body().getAsJsonObject().get("user").toString()).apply();
                    updateFormsPermission(context, user);
                } catch (Exception e) {
                    onFailure(call, e);
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                updateFormsPermission(context, user);
                Logger.e(t, context.getString(R.string.log_accessibility));
            }
        });
    }

    public static void getAccessibilityLayers(final Intent intent, final Activity activity, final Api api, final User user) {
        if (user == null || user.getId() == null) return;
        api.getAccessibilityLayers(User.getCredential(), user.getId()).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                try {
                    if (!response.isSuccessful())
                        throw new Exception(String.valueOf(response.code()));
                    List<String> accessLayerNames = new ArrayList<>();
                    if (response.body() == null)
                        throw new Exception("Empty result from server");
                    for (JsonElement layer : response.body().getAsJsonObject().get("layers").getAsJsonArray())
                        accessLayerNames.add(layer.getAsJsonObject().get("layer").getAsJsonObject().get("name").getAsString());
                    Utils.getSharedPref(activity).edit().putString(Keys.accessibleLayers(user.getId()), new Gson().toJson(accessLayerNames)).apply();
                    updateLayersPermission(intent, activity, user);
                } catch (Exception e) {
                    onFailure(call, e);
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                Logger.e(t, activity.getString(R.string.log_accessibility));
                updateLayersPermission(intent, activity, user);
            }
        });
    }
}
