package ir.gfpishro.geosuiteandroidprivateusers.MapHelpers.layers;

import android.annotation.SuppressLint;
import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.oscim.layers.vector.geometries.Style;

import java.util.HashMap;

import ir.gfpishro.geosuiteandroidprivateusers.Models.SearchType;

public enum EISLayer {
    AREA("-1", 200, "#EE5A24", null),
    VALVE("-1", 201, "#3f48cc", SearchType.PG_NUM.toString()),
    RISER("-1", 202, "#1B1464", SearchType.RISER_NUM.toString()),
    PARCEL("-1", 203, "#FFFF00", SearchType.PARCEL_CODE.toString());

    public String eisId;
    public int jsonId;
    private final String color;
    public String type;

    EISLayer(String eisId, int id, String color, String type) {
        this.eisId = eisId;
        this.color = color;
        this.type = type;
        this.jsonId = id;
    }

    public Style.Builder getStyle(Context context) {
        return Style.builder()
                .strokeColor(color)
                .fillColor(color)
                .fixed(true)
                .buffer((this == VALVE ? 0.000009 : 0.000003) * context.getResources().getDisplayMetrics().density)
                .fillAlpha((this == VALVE || this == RISER) ? 1 : 0.3f)
                .strokeWidth(((this == VALVE || this == RISER) ? 6 : 0)
                        * context.getResources().getDisplayMetrics().density);
    }

    @SuppressLint("DefaultLocale")
    public static String getEisValveMessage(HashMap<String, String> hashMap, String fieldName) {
        JsonObject geoJson = new Gson().fromJson(hashMap.get("geojson"), JsonObject.class);
        StringBuilder pks = new StringBuilder();
        JsonArray features = geoJson.get("features").getAsJsonArray();
        int countFeatures = features.size();
        for (int i = 0; i < countFeatures; i++)
            try {
                pks.append('-').append(
                        features.get(i)
                                .getAsJsonObject()
                                .get("properties")
                                .getAsJsonObject()
                                .get(fieldName)
                                .getAsString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        String msg;

        if (fieldName.charAt(0) == 'v') //valve
            msg = String.format("%s\n%s", hashMap.get("eisMsg"), pks.length() > 1 ? pks.substring(1) : pks)
                    .replaceFirst("بیش از یک", String.valueOf(countFeatures));
        else msg = String.format("%d تعداد علمک ", countFeatures);
        return msg;
    }
}