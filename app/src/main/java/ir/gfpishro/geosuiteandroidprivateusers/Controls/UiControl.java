package ir.gfpishro.geosuiteandroidprivateusers.Controls;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UiControl {
    @SerializedName("id")
    @Expose
    private Integer id = null;
    @SerializedName("control-type")
    @Expose
    private String controlType = "textview";
    @SerializedName("ui-child-controls")
    @Expose
    private List<UiChildControl> uiChildControls = null;
    @SerializedName("title")
    @Expose
    private String title = "";

    public UiControl(Integer id, String controlType) {
        this.id = id;
        this.controlType = controlType;
    }

    public UiControl() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getId(boolean isPureId) {
        if (isPureId) return id;
        if (id == null) return null;
        int offset = 0;
        switch (getControlType()) {
            case EDITTEXT:
                offset += 1;
            case COMBOBOX:
                offset += 1;
            case RADIOBOX:
                offset += 1;
            case CHECHBOX:
                offset += 1;
            case TEXTVIEW:
                offset += 1;
        }
        return id + (offset * 100);
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public List<UiChildControl> getUiChildControls() {
        return uiChildControls;
    }

    public void setUiChildControls(List<UiChildControl> uiChildControls) {
        this.uiChildControls = uiChildControls;
    }

    public void setControlType(String controlType) {
        this.controlType = controlType;
    }

    public ControlTypes getControlType() {
        switch (controlType.toLowerCase().trim()) {
            case "textedit":
            case "edittext":
                return ControlTypes.EDITTEXT;
            case "combobox":
            case "spinner":
                return ControlTypes.COMBOBOX;
            case "radiobox":
            case "radiobutton":
                return ControlTypes.RADIOBOX;
            case "checkbox":
                return ControlTypes.CHECHBOX;
            case "meta":
                return ControlTypes.META;
            default:
                return ControlTypes.TEXTVIEW;
        }
    }

    public View getControl(Context context) {
        return new ControlGenerator().getControl(context, this);
    }

    public void bindControl(View view) {
        switch (getControlType()) {
            case EDITTEXT:
                EditText et = (EditText) view;
                getUiChildControls().get(0).setText(et.getText().toString().trim());
                break;
            case COMBOBOX:
                Spinner spinner = (Spinner) view;
                int selected = (int) spinner.getSelectedItemId();
                for (int i = 0; i < getUiChildControls().size(); i++)
                    getUiChildControls().get(selected).setChecked(i == selected);
                break;
            case RADIOBOX:
                RadioGroup rg = (RadioGroup) view;
                for (int i = 0; i < rg.getChildCount(); i++) {
                    View rb = rg.getChildAt(i);
                    if (rb instanceof RadioButton)
                        getUiChildControls().get(i).setChecked(((RadioButton) rb).isChecked());
                }
                break;
            case CHECHBOX:
                CheckBox cb = (CheckBox) view;
                getUiChildControls().get(0).setChecked(cb.isChecked());
                break;
        }
    }
}