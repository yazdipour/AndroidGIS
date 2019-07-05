package ir.gfpishro.geosuiteandroidprivateusers.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CodeInfo {

    public CodeInfo(String geom, Integer objId, Integer type, String content) {
        this.geom = geom;
        this.objId = objId;
        this.type = type;
        this.content = content;
    }

    @SerializedName("geom")
    @Expose
    private String geom;
    @SerializedName("obj_id")
    @Expose
    private Integer objId;
    @SerializedName("type")
    @Expose
    private Integer type;
    @SerializedName("content")
    @Expose
    private String content;

    public String getGeom() {
        return geom;
    }

    public void setGeom(String geom) {
        this.geom = geom;
    }

    public Integer getObjId() {
        return objId;
    }

    public void setObjId(Integer objId) {
        this.objId = objId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}