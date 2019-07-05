package ir.gfpishro.geosuiteandroidprivateusers.Controls;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

//  JSON MODEL
//  /API/Models.md
public class ControlGenerator {
    private FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    );

    View getControl(Context context, UiControl uiControl) {
        switch (uiControl.getControlType()) {
            case EDITTEXT:
                return createTextEdit(context, uiControl);
            case COMBOBOX:
                return createComboBox(context, uiControl);
            case RADIOBOX:
                return createRadioButton(context, uiControl);
            case CHECHBOX:
                return createCheckBox(context, uiControl);
            case TEXTVIEW:
                return createTextView(context, uiControl);
            default:
                return null;
        }
    }

    public static TextView createHeader(Context context, String title) {
        TextView tv = new TextView(context);
        tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
        tv.setTextDirection(View.TEXT_DIRECTION_RTL);
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tv.setBackgroundColor(Color.parseColor("#eeeeee"));
        tv.setText(title);
        tv.setPadding(0, 28, 0, 28);
        tv.setTextSize(16);
        return tv;
    }

    private TextView createTextView(Context context, UiControl uiControl) {
        TextView v = new TextView(context);
        if (uiControl.getId(false) == null) v.setId(uiControl.getId(false));
        v.setTextDirection(View.TEXT_DIRECTION_RTL);
        v.setLayoutParams(layoutParams);
        v.setText(uiControl.getUiChildControls().get(0).getText());
        v.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        v.setPadding(0, 16, 0, 16);
        return v;
    }

    private EditText createTextEdit(Context context, UiControl uiControl) {
        EditText v = new EditText(context);
        v.setId(uiControl.getId(false));
        v.setTextDirection(View.TEXT_DIRECTION_RTL);
        v.setLayoutParams(layoutParams);
        v.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        v.setHint(uiControl.getUiChildControls().get(0).getText());
        if (uiControl.getUiChildControls().get(0).getChecked()) {
            v.setLines(3);
            v.setSingleLine(false);
            v.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
            v.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        }
        if (uiControl.getUiChildControls().get(0).getNumberField())
            v.setInputType(InputType.TYPE_CLASS_NUMBER);
        return v;
    }

    private Spinner createComboBox(final Context context, final UiControl uiControl) {
        Spinner v = new Spinner(context);
        v.setTextDirection(View.TEXT_DIRECTION_RTL);
        v.setId(uiControl.getId(false));
        List<String> spinnerArray = new ArrayList<>();
        for (UiChildControl tb : uiControl.getUiChildControls())
            spinnerArray.add(tb.getText());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, spinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        v.setAdapter(adapter);
        v.setLayoutParams(layoutParams);
        return v;
    }

    private RadioGroup createRadioButton(Context context, UiControl uiControl) {
        RadioGroup v = new RadioGroup(context);
        v.setId(uiControl.getId(false));
        int offset = 1000;
        for (UiChildControl tb : uiControl.getUiChildControls()) {
            RadioButton radioButton = new RadioButton(context);
            radioButton.setId(uiControl.getId(false) + (offset++));
            radioButton.setTextDirection(View.TEXT_DIRECTION_RTL);
            radioButton.setText(tb.getText());
            radioButton.setChecked(tb.getChecked());
            v.addView(radioButton);
        }
        v.setLayoutParams(layoutParams);
        return v;
    }

    private CheckBox createCheckBox(Context context, UiControl uiControl) {
        CheckBox v = new CheckBox(context);
        v.setTextDirection(View.TEXT_DIRECTION_RTL);
        v.setId(uiControl.getId(false));
        v.setText(uiControl.getUiChildControls().get(0).getText());
        v.setChecked(uiControl.getUiChildControls().get(0).getChecked());
        v.setLayoutParams(layoutParams);
        return v;
    }
}
