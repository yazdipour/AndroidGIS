package editormodule.layerprovider;

import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Build;

import com.nextgis.maplib.api.IProgressor;
import com.nextgis.maplib.map.VectorLayer;
import com.nextgis.maplib.util.Constants;
import com.nextgis.maplib.util.GeoJSONUtil;
import com.nextgis.maplib.util.NGException;
import com.nextgis.maplib.util.NetworkUtil;
import com.nextgis.maplibui.mapui.VectorLayerUI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import static com.nextgis.maplib.util.Constants.MAX_CONTENT_LENGTH;

public class GeoserverVectorLayer extends VectorLayerUI {
    public GeoserverVectorLayer(Context context, File path) {
        super(context, path);
    }

    public void createFromGeoJson(
            String FeatureCollection,IProgressor progressor)
            throws IOException, JSONException, NGException, SQLiteException
    {
        JSONObject jsonObject = new JSONObject(FeatureCollection);
        GeoJSONUtil.createLayerFromGeoJSON(this, jsonObject, progressor);
    }





}
