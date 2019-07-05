
package ir.gfpishro.geosuiteandroidprivateusers.Models.Mission;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Mission implements Serializable {

    @SerializedName("id")
    @Expose
    private Integer id = -1;
    @SerializedName("lat")
    @Expose
    private Double lat;
    @SerializedName("lon")
    @Expose
    private Double lon;
    @SerializedName("team")
    @Expose
    private Team team;
    @SerializedName("responsible")
    @Expose
    private Responsible responsible;
    @SerializedName("requester")
    @Expose
    private Requester requester;
    @SerializedName("status")
    @Expose
    private Integer status = -1; //2 erja be sarshif //1 erja be team
    @SerializedName("address")
    @Expose
    private String address = "";
    @SerializedName("start_date")
    @Expose
    private Integer startDate = 0;
    @SerializedName("registered_at")
    @Expose
    private Integer registeredAt = 0;
    @SerializedName("phone")
    @Expose
    private String phone = "";
    @SerializedName("city")
    @Expose
    private String city = "";
    @SerializedName("description")
    @Expose
    private String description = "";
    @SerializedName("issue_type")
    @Expose
    private IssueType issueType;
    @SerializedName("event_date")
    @Expose
    private Integer eventDate = 0;
    @SerializedName("end_date")
    @Expose
    private Integer endDate = 0;
    @SerializedName("arrival_date")
    @Expose
    private Integer arrivalDate = 0;
    @SerializedName("notice_date")
    @Expose
    private Integer noticeDate = 0;
    @SerializedName("route_cost")
    @Expose
    private Integer routeCost = 0;
    @SerializedName("actor_unit")
    @Expose
    private Integer actorUnit = 0;
    @SerializedName("account_id")
    @Expose
    private String accountId = "";
    @SerializedName("route")
    @Expose
    private String route = "";
    @SerializedName("name")
    @Expose
    private String name = "";
    @SerializedName("emergency_zone")
    @Expose
    private EmergencyZone emergencyZone;
    @SerializedName("request_method")
    @Expose
    private Integer requestMethod = -1;
    @SerializedName("personel")
    @Expose
    private String personel = "";

    @SerializedName("city_code")
    @Expose
    private String cityCode = "";

    @SerializedName("zone")
    @Expose
    private JsonObject zone;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public Responsible getResponsible() {
        return responsible;
    }

    public void setResponsible(Responsible responsible) {
        this.responsible = responsible;
    }

    public Requester getRequester() {
        return requester;
    }

    public void setRequester(Requester requester) {
        this.requester = requester;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getStartDate() {
        return startDate;
    }

    public void setStartDate(Integer startDate) {
        this.startDate = startDate;
    }

    public Integer getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(Integer registeredAt) {
        this.registeredAt = registeredAt;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public IssueType getIssueType() {
        return issueType;
    }

    public void setIssueType(IssueType issueType) {
        this.issueType = issueType;
    }

    public Integer getEventDate() {
        return eventDate;
    }

    public void setEventDate(Integer eventDate) {
        this.eventDate = eventDate;
    }

    public Integer getEndDate() {
        return endDate;
    }

    public void setEndDate(Integer endDate) {
        this.endDate = endDate;
    }

    public Integer getNoticeDate() {
        return noticeDate;
    }

    public void setNoticeDate(Integer noticeDate) {
        this.noticeDate = noticeDate;
    }

    public Integer getRouteCost() {
        return routeCost;
    }

    public void setRouteCost(Integer routeCost) {
        this.routeCost = routeCost;
    }

    public Integer getActorUnit() {
        return actorUnit;
    }

    public void setActorUnit(Integer actorUnit) {
        this.actorUnit = actorUnit;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EmergencyZone getEmergencyZone() {
        return emergencyZone;
    }

    public void setEmergencyZone(EmergencyZone emergencyZone) {
        this.emergencyZone = emergencyZone;
    }

    public Integer getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(Integer requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getPersonel() {
        return personel;
    }

    public void setPersonel(String personel) {
        this.personel = personel;
    }

    public Integer getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(Integer arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public JsonObject getZone() {
        return zone;
    }

    public void setZone(JsonObject zone) {
        this.zone = zone;
    }
}
