package editormodule.utils;

import com.nextgis.maplib.datasource.Feature;
import com.nextgis.maplib.map.VectorLayer;

import org.json.JSONArray;
import org.json.JSONObject;

public class utils {

    public static String VectorLayerToGeoJson(VectorLayer layer, String type) {
        JSONObject geoJson = new JSONObject();
        JSONArray features = new JSONArray();
        for (int i = 0; i < layer.getCount() + 1; i++) {
            try {
                Feature feature = layer.getFeature(i);
                feature.setFieldValue("FEATURE_ID", feature.getId());
                features.put(feature.toJSON());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            geoJson.put("type", type);
            geoJson.put("features", features);
            geoJson.put("layerobject", layer.toJSON());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return geoJson.toString();
    }
}
