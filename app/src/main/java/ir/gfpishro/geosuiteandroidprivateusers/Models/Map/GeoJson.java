package ir.gfpishro.geosuiteandroidprivateusers.Models.Map;

import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GeoJson {

    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("crs")
    @Expose
    private JsonElement crs;
    @SerializedName("features")
    @Expose
    private List<Feature> features = null;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public JsonElement getCrs() {
        return crs;
    }

    public void setCrs(JsonElement crs) {
        this.crs = crs;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

}