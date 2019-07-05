package ir.gfpishro.geosuiteandroidprivateusers.Models;

import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Steal {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("cu_num")
    @Expose
    private String[] cuNum;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("fix_desc")
    @Expose
    private String fix_desc;
    @SerializedName("steal_type")
    @Expose
    private String stealType;
    @SerializedName("steal_status")
    @Expose
    private String stealStatus;
    @SerializedName("create_date")
    @Expose
    private String createDate;
    @SerializedName("geom")
    @Expose
    private String geom;
    @SerializedName("user_location")
    @Expose
    private String userLocation;
    @SerializedName("image")
    @Expose
    private String[] image;
    @SerializedName("parcel")
    @Expose
    private Integer parcel;
    @SerializedName("user")
    @Expose
    private JsonElement user;
    @SerializedName("fix_date")
    @Expose
    private Integer fixDate;
    @SerializedName("steal_date")
    @Expose
    private int stealDate = 0;
    @SerializedName("deleted")
    @Expose
    private Boolean deleted;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String[] getCuNum() {
        return cuNum;
    }

    public void setCuNum(String[] cuNum) {
        this.cuNum = cuNum;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStealType() {
        return stealType;
    }

    public void setStealType(String stealType) {
        this.stealType = stealType;
    }

    public String getStealStatus() {
        return stealStatus;
    }

    public void setStealStatus(String stealStatus) {
        this.stealStatus = stealStatus;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getGeom() {
        return geom;
    }

    public void setGeom(String geom) {
        this.geom = geom;
    }

    public String getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(String userLocation) {
        this.userLocation = userLocation;
    }

    public Integer getParcel() {
        return parcel;
    }

    public void setParcel(Integer parcel) {
        this.parcel = parcel;
    }

    public JsonElement getUser() {
        return user;
    }

    public void setUser(JsonElement user) {
        this.user = user;
    }

    public Integer getFixDate() {
        return fixDate;
    }

    public void setFixDate(Integer fixDate) {
        this.fixDate = fixDate;
    }

    public Integer getStealDate() {
        return stealDate;
    }

    public void setStealDate(Integer stealDate) {
        this.stealDate = stealDate;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public String[] getImage() {
        return image;
    }

    public void setImage(String[] image) {
        this.image = image;
    }

    public String getFixDesc() {
        return fix_desc;
    }

    public void setFixDesc(String fix_desc) {
        this.fix_desc = fix_desc;
    }
}