package ir.gfpishro.geosuiteandroidprivateusers.Models.Map;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Province {
    @SerializedName("OBJECTID")
    @Expose
    private Integer oBJECTID;
    @SerializedName("ostn_name")
    @Expose
    private String ostnName;
    @SerializedName("city_code")
    @Expose
    private String cityCode;
    @SerializedName("minx")
    @Expose
    private Double minx;
    @SerializedName("maxx")
    @Expose
    private Double maxx;
    @SerializedName("miny")
    @Expose
    private Double miny;
    @SerializedName("maxy")
    @Expose
    private Double maxy;

    public Integer getoBJECTID() {
        return oBJECTID;
    }

    public void setoBJECTID(Integer oBJECTID) {
        this.oBJECTID = oBJECTID;
    }

    public String getOstnName() {
        return ostnName;
    }

    public void setOstnName(String ostnName) {
        this.ostnName = ostnName;
    }

    public Double getMinx() {
        return minx;
    }

    public void setMinx(Double minx) {
        this.minx = minx;
    }

    public Double getMaxx() {
        return maxx;
    }

    public void setMaxx(Double maxx) {
        this.maxx = maxx;
    }

    public Double getMiny() {
        return miny;
    }

    public void setMiny(Double miny) {
        this.miny = miny;
    }

    public Double getMaxy() {
        return maxy;
    }

    public void setMaxy(Double maxy) {
        this.maxy = maxy;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }
}
