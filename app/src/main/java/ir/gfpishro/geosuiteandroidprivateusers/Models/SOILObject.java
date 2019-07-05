package ir.gfpishro.geosuiteandroidprivateusers.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SOILObject {
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("geom")
    @Expose
    private String geom;
    @SerializedName("contractor")
    @Expose
    private String contractor;
    @SerializedName("date")
    @Expose
    private Integer date;
    @SerializedName("superviser")
    @Expose
    private String superviser;
    @SerializedName("distance1")
    @Expose
    private Integer distance1;
    @SerializedName("R")
    @Expose
    private Integer r;
    @SerializedName("distance2")
    @Expose
    private Integer distance2;
    @SerializedName("deleted")
    @Expose
    private Boolean deleted;
    @SerializedName("create_date")
    @Expose
    private String createDate;
    @SerializedName("edit_date")
    @Expose
    private String editDate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getGeom() {
        return geom;
    }

    public void setGeom(String geom) {
        this.geom = geom;
    }

    public String getContractor() {
        return contractor;
    }

    public void setContractor(String contractor) {
        this.contractor = contractor;
    }

    public Integer getDate() {
        return date;
    }

    public void setDate(Integer date) {
        this.date = date;
    }

    public String getSuperviser() {
        return superviser;
    }

    public void setSuperviser(String superviser) {
        this.superviser = superviser;
    }

    public Integer getDistance1() {
        return distance1;
    }

    public void setDistance1(Integer distance1) {
        this.distance1 = distance1;
    }

    public Integer getR() {
        return r;
    }

    public void setR(Integer r) {
        this.r = r;
    }

    public Integer getDistance2() {
        return distance2;
    }

    public void setDistance2(Integer distance2) {
        this.distance2 = distance2;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getEditDate() {
        return editDate;
    }

    public void setEditDate(String editDate) {
        this.editDate = editDate;
    }

}
