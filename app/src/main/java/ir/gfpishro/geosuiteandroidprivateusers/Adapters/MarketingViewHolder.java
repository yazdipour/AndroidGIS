package ir.gfpishro.geosuiteandroidprivateusers.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TableRow;

import com.travijuu.numberpicker.library.NumberPicker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ir.gfpishro.geosuiteandroidprivateusers.Helpers.Utils;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Keys;
import ir.gfpishro.geosuiteandroidprivateusers.Models.User;
import ir.gfpishro.geosuiteandroidprivateusers.R;

public class MarketingViewHolder extends RecyclerView.ViewHolder {
    private ImageButton btn;
    private NumberPicker et1, et2;
    private Spinner spinner;
    private TableRow trow1, trow2;
    private static List<String> MARKETING_CODES = new ArrayList<>();

    MarketingViewHolder(View itemView) {
        super(itemView);
        btn = itemView.findViewById(R.id.ib_rm);
        et1 = itemView.findViewById(R.id.np1);
        et2 = itemView.findViewById(R.id.np2);
        spinner = itemView.findViewById(R.id.et3);
        trow1 = itemView.findViewById(R.id.trow1);
        trow2 = itemView.findViewById(R.id.trow2);
    }

    void bindData(final String self, final MarketingAdapter.Events listener, final int position) {
        btn.setOnClickListener(v -> listener.onBtnClicked(self, position));
        final List<String> spinnerOptions = loadStatics();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(itemView.getContext(), android.R.layout.simple_spinner_item, spinnerOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        if (self.length() > 0) try {
            int[] indexes = {self.indexOf('('), self.indexOf(')')};
            String[] values = {self.substring(0, indexes[0]), self.substring(indexes[0] + 1, indexes[1]), self.substring(indexes[1] + 1)};
            et1.setValue(values[0].length() > 0 ? Integer.parseInt(values[0]) : 1);
            spinner.setSelection(spinnerOptions.indexOf(values[1]));
            et2.setValue(values[2].length() > 0 ? Integer.parseInt(values[2]) : 1);
        } catch (Exception e) {
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                switch (spinnerOptions.get(position)) {
                    case "B":
                        trow1.setVisibility(View.VISIBLE);
                        trow2.setVisibility(View.VISIBLE);
                        break;
                    case "1":
                    case "C":
                    case "5":
                    case "V":
                        trow1.setVisibility(View.VISIBLE);
                        trow2.setVisibility(View.GONE);
                        et2.setValue(1);
                        break;
                    default:
                        trow1.setVisibility(View.GONE);
                        trow2.setVisibility(View.GONE);
                        et1.setValue(1);
                        et2.setValue(1);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
        et1.refresh();
        et2.refresh();
    }

    private List<String> loadStatics() {
        if (MARKETING_CODES.size() == 0) {
            String marketingCode = Utils.getSharedPref(null)
                    .getString(Keys.statics(User.getCurrentUser(null).getId()) + Keys.MARKETING_CODE, "A,B,C,D,V,E,F,G,H,K,L,M,N,P,Q,R,S,T,W,X,Y,1,2,3,4,5,0");
            MARKETING_CODES.addAll(Arrays.asList(marketingCode.split(",")));
        }
        return MARKETING_CODES;
    }

    public String getCode() {
        return String.format("%s(%s)%s",
                et1.getValue() == 1 ? "" : et1.getValue(),
                spinner.getSelectedItem(),
                et2.getValue() == 1 ? "" : et2.getValue());
    }
}
