package ir.gfpishro.geosuiteandroidprivateusers.Models.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

import ir.gfpishro.geosuiteandroidprivateusers.Controls.UiControl;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.StringUtils;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Mission.Mission;

public class AppLog implements Serializable {
    public AppLog() {
    }

    public AppLog(Integer uid, String mid, Long time, LogType type, String query) {
        this.uid = uid; // USER ID
        this.mid = mid; //Mobile ID
        this.time = time;
        this.type = type;
        this.query = query;
    }

    // USER ID
    @SerializedName("uid")
    @Expose
    private Integer uid;

    // Mobile ID
    @SerializedName("mid")
    @Expose
    private String mid;

    @SerializedName("time")
    @Expose
    private Long time;
    @SerializedName("type")
    @Expose
    private LogType type;
    @SerializedName("query")
    @Expose
    private String query;

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public LogType getType() {
        return type;
    }

    public void setType(LogType type) {
        this.type = type;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setQuery(List<UiControl> report) {
        this.query = new Gson().toJson(report);
        //Farsi Number Reports to English Number
        this.query = StringUtils.farsiNumbersToEnglish(this.query);
    }

    public void setQuery(Mission[] missions) {
        this.query = new Gson().toJson(missions);
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }
}
