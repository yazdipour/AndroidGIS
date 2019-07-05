package ir.gfpishro.geosuiteandroidprivateusers.Controls;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UiChildControl {

    @SerializedName("if-id-req")
    @Expose
    public int ifIdReq = -1;

    @SerializedName("if-id-not-req")
    @Expose
    public int ifIdNotReq = -1;

    @SerializedName("is-number-field")
    @Expose
    private Boolean isNumberField = false;

    @SerializedName("checked")
    @Expose
    private Boolean checked = false;

    @SerializedName("required")
    @Expose
    private Boolean required = false;

    @SerializedName("text")
    @Expose
    private String text = "";

    //need for gson
    public UiChildControl() {
    }

    public UiChildControl(String text) {
        this.text = text;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Boolean getNumberField() {
        return isNumberField;
    }

    public void setNumberField(Boolean numberField) {
        isNumberField = numberField;
    }
}
