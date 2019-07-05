
package ir.gfpishro.geosuiteandroidprivateusers.Models.Mission;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Requester {

    @SerializedName("id")
    @Expose
    private int id = -1;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("last")
    @Expose
    private String last;
    @SerializedName("cu_num")
    @Expose
    private String cuNum;
    @SerializedName("cu_num1")
    @Expose
    private String cuNum1;
    @SerializedName("cu_lat")
    @Expose
    private Integer cuLat;
    @SerializedName("cu_lon")
    @Expose
    private Integer cuLon;
    @SerializedName("cu_postal")
    @Expose
    private String cuPostal;
    @SerializedName("unite_count")
    @Expose
    private String uniteCount;
    @SerializedName("capacity")
    @Expose
    private String capacity;
    @SerializedName("frequent")
    @Expose
    private String frequent;
    @SerializedName("city_code")
    @Expose
    private String cityCode;
    @SerializedName("code_address")
    @Expose
    private String codeAddress;
    @SerializedName("user_s")
    @Expose
    private Integer userS;
    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("phone")
    @Expose
    private String phone;
    @SerializedName("lat")
    @Expose
    private Integer lat;
    @SerializedName("lon")
    @Expose
    private Integer lon;
    @SerializedName("serial_counter")
    @Expose
    private String serialCounter;
    @SerializedName("prev_data")
    @Expose
    private String prevData;
    @SerializedName("date_s")
    @Expose
    private Integer dateS;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLast() {
        return last;
    }

    public void setLast(String last) {
        this.last = last;
    }

    public String getCuNum() {
        return cuNum;
    }

    public void setCuNum(String cuNum) {
        this.cuNum = cuNum;
    }

    public String getCuNum1() {
        return cuNum1;
    }

    public void setCuNum1(String cuNum1) {
        this.cuNum1 = cuNum1;
    }

    public Integer getCuLat() {
        return cuLat;
    }

    public void setCuLat(Integer cuLat) {
        this.cuLat = cuLat;
    }

    public Integer getCuLon() {
        return cuLon;
    }

    public void setCuLon(Integer cuLon) {
        this.cuLon = cuLon;
    }

    public String getCuPostal() {
        return cuPostal;
    }

    public void setCuPostal(String cuPostal) {
        this.cuPostal = cuPostal;
    }

    public String getUniteCount() {
        return uniteCount;
    }

    public void setUniteCount(String uniteCount) {
        this.uniteCount = uniteCount;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public String getFrequent() {
        return frequent;
    }

    public void setFrequent(String frequent) {
        this.frequent = frequent;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getCodeAddress() {
        return codeAddress;
    }

    public void setCodeAddress(String codeAddress) {
        this.codeAddress = codeAddress;
    }

    public Integer getUserS() {
        return userS;
    }

    public void setUserS(Integer userS) {
        this.userS = userS;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getLat() {
        return lat;
    }

    public void setLat(Integer lat) {
        this.lat = lat;
    }

    public Integer getLon() {
        return lon;
    }

    public void setLon(Integer lon) {
        this.lon = lon;
    }

    public String getSerialCounter() {
        return serialCounter;
    }

    public void setSerialCounter(String serialCounter) {
        this.serialCounter = serialCounter;
    }

    public String getPrevData() {
        return prevData;
    }

    public void setPrevData(String prevData) {
        this.prevData = prevData;
    }

    public Integer getDateS() {
        return dateS;
    }

    public void setDateS(Integer dateS) {
        this.dateS = dateS;
    }

}
