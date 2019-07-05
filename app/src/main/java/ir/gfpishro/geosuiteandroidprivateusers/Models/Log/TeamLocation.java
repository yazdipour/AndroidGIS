package ir.gfpishro.geosuiteandroidprivateusers.Models.Log;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class TeamLocation implements Serializable{
//[{
//        "Phone_id": 2361,
//                "lat":35.7417819,
//                "lon":51.4473052,
//                "Date_time":1526982826
//    }]
    @SerializedName("Phone_id")
    @Expose
    private String phoneId;
    @SerializedName("lat")
    @Expose
    private Double lat;
    @SerializedName("lon")
    @Expose
    private Double lon;
    @SerializedName("Date_time")
    @Expose
    private Integer dateTime;
    @SerializedName("status")
    @Expose
    private boolean status = false;

    public String getPhoneId() {
        return phoneId;
    }

    public void setPhoneId(String phoneId) {
        this.phoneId = phoneId;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public Integer getDateTime() {
        return dateTime;
    }

    public void setDateTime(Integer dateTime) {
        this.dateTime = dateTime;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
