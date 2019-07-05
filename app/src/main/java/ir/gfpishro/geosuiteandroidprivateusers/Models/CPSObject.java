package ir.gfpishro.geosuiteandroidprivateusers.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CPSObject {
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("geom")
    @Expose
    private String geom;
    @SerializedName("voltage")
    @Expose
    private Integer voltage;
    @SerializedName("I")
    @Expose
    private Integer i;
    @SerializedName("contractor")
    @Expose
    private String contractor;
    @SerializedName("year")
    @Expose
    private Integer year;
    @SerializedName("construction_superviser")
    @Expose
    private String constructionSuperviser;
    @SerializedName("construction_year")
    @Expose
    private Integer constructionYear;
    @SerializedName("last_repair_period")
    @Expose
    private Integer lastRepairPeriod;
    @SerializedName("repair_cause")
    @Expose
    private String repairCause;
    @SerializedName("repair_superviser")
    @Expose
    private String repairSuperviser;

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

    public Integer getVoltage() {
        return voltage;
    }

    public void setVoltage(Integer voltage) {
        this.voltage = voltage;
    }

    public Integer getI() {
        return i;
    }

    public void setI(Integer i) {
        this.i = i;
    }

    public String getContractor() {
        return contractor;
    }

    public void setContractor(String contractor) {
        this.contractor = contractor;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getConstructionSuperviser() {
        return constructionSuperviser;
    }

    public void setConstructionSuperviser(String constructionSuperviser) {
        this.constructionSuperviser = constructionSuperviser;
    }

    public Integer getConstructionYear() {
        return constructionYear;
    }

    public void setConstructionYear(Integer constructionYear) {
        this.constructionYear = constructionYear;
    }

    public Integer getLastRepairPeriod() {
        return lastRepairPeriod;
    }

    public void setLastRepairPeriod(Integer lastRepairPeriod) {
        this.lastRepairPeriod = lastRepairPeriod;
    }

    public String getRepairCause() {
        return repairCause;
    }

    public void setRepairCause(String repairCause) {
        this.repairCause = repairCause;
    }

    public String getRepairSuperviser() {
        return repairSuperviser;
    }

    public void setRepairSuperviser(String repairSuperviser) {
        this.repairSuperviser = repairSuperviser;
    }
}
