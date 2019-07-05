package ir.gfpishro.geosuiteandroidprivateusers.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class HSEObject {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("geom")
    @Expose
    private String geom;
    @SerializedName("number")
    @Expose
    private Integer number;
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

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
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