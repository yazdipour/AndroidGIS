
package ir.gfpishro.geosuiteandroidprivateusers.Models.Mission;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class IssueType {

    @SerializedName("issue_id")
    @Expose
    private Integer issueId;
    @SerializedName("priority")
    @Expose
    private int priority = 2;
    @SerializedName("parent")
    @Expose
    private Integer parent;
    @SerializedName("lable")
    @Expose
    private String lable;

    public Integer getIssueId() {
        return issueId;
    }

    public void setIssueId(Integer issueId) {
        this.issueId = issueId;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Integer getParent() {
        return parent;
    }

    public void setParent(Integer parent) {
        this.parent = parent;
    }

    public String getLable() {
        return lable;
    }

    public void setLable(String lable) {
        this.lable = lable;
    }

}
