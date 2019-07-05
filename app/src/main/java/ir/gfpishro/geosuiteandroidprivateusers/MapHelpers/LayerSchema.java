package ir.gfpishro.geosuiteandroidprivateusers.MapHelpers;

import android.view.View;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class LayerSchema {
    @Override
    public boolean equals(Object obj) {
        return obj instanceof LayerSchema && this.getId().equals(((LayerSchema) obj).getId());
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public int getVisibility() {
        return visibility.equals("visible") ? View.VISIBLE : View.INVISIBLE;
    }

    public void setVisibility(int visibility) {
        this.visibility = visibility == View.VISIBLE ? "visible" : "invisible";
    }

    public String getAccessName() {
        return accessName;
    }

    public void setAccessName(String accessName) {
        this.accessName = accessName;
    }

    public Boolean getAccessible() {
        return accessible;
    }

    public void setAccessible(Boolean accessible) {
        this.accessible = accessible;
    }

    public enum LayerType {
        base("base"), online("online"), pg_point("pg_point"),
        pg_pipe("pg_pipe"), bg_pipe("bg_pipe"), parcel("parcel"), search("search"),
        online_nav("online_nav"), eis("eis"), riser("riser");

        private final String name;

        LayerType(String str) {
            name = str;
        }

        public String toString() {
            return this.name;
        }
    }

    @SerializedName("type")
    @Expose
    private LayerType type = LayerType.online;

    @SerializedName("visibility")
    @Expose
    private String visibility = "visible";

    @SerializedName("url")
    @Expose
    private String url = "";

    @SerializedName("formatter")
    @Expose
    private String formatter;

    @SerializedName("tile_path")
    @Expose
    private String tilePath = "";

    @SerializedName("max_zoom")
    @Expose
    private Integer maxZoom = 30;

    @SerializedName("min_zoom")
    @Expose
    private Integer minZoom = 2;

    @SerializedName("id")
    @Expose
    private Integer id = 0;

    @SerializedName("mapIndex")
    @Expose
    private Integer mapIndex = null;

    @SerializedName("title")
    @Expose
    private String title = "title";

    @SerializedName("access_name")
    @Expose
    private String accessName = "";

    @SerializedName("enable")
    @Expose
    private boolean enable = false;

    @SerializedName("read_only")
    @Expose
    private Boolean readOnly = false;

    private int markerIndex = -1;

    @SerializedName("accessible")
    @Expose
    private Boolean accessible = false;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMapIndex() {
        return mapIndex;
    }

    public void setMapIndex(Integer mapIndex) {
        this.mapIndex = mapIndex;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTilePath() {
        return tilePath;
    }

    public void setTilePath(String tilePath) {
        this.tilePath = tilePath;
    }

    public Integer getMaxZoom() {
        return maxZoom;
    }

    public void setMaxZoom(Integer maxZoom) {
        this.maxZoom = maxZoom;
    }

    public String getFormatter() {
        return formatter;
    }

    public void setFormatter(String formatter) {
        this.formatter = formatter;
    }

    public LayerType getType() {
        return type;
    }

    public void setType(LayerType type) {
        this.type = type;
    }

    public Integer getMinZoom() {
        return minZoom;
    }

    public void setMinZoom(Integer minZoom) {
        this.minZoom = minZoom;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public Boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    public int getMarkerIndex() {
        return markerIndex;
    }

    public void setMarkerIndex(int markerIndex) {
        this.markerIndex = markerIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
