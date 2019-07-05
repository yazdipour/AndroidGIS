package ir.gfpishro.geosuiteandroidprivateusers.Adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import ir.gfpishro.geosuiteandroidprivateusers.R;

public class CustomExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private String[] expandableListTitle;
    private HashMap<String, List<String>> expandableListDetail;
//    private View.OnClickListener eventZoomClicked;

    public CustomExpandableListAdapter(Context context, HashMap<String, List<String>> expandableListDetail) {
        this.context = context;
        this.expandableListTitle = expandableListDetail.keySet().toArray(new String[0]);
        this.expandableListDetail = expandableListDetail;
    }

    @Override
    public Object getChild(int listPosition, int expandedListPosition) {
        return this.expandableListDetail.get(this.expandableListTitle[listPosition])
                .get(expandedListPosition);
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    @Override
    public View getChildView(int listPosition, final int expandedListPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final String expandedListText = (String) getChild(listPosition, expandedListPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.item_treeroot, null);
        }
        TextView expandedListTextView = convertView.findViewById(R.id.listTitle);
        expandedListTextView.setText(String.format("\t\t%s", expandedListText.replaceAll("\"", " ")));
        return convertView;
    }

    @Override
    public int getChildrenCount(int listPosition) {
        return this.expandableListDetail.get(this.expandableListTitle[listPosition]).size();
    }

    @Override
    public Object getGroup(int listPosition) {
        return this.expandableListTitle[listPosition];
    }

    @Override
    public int getGroupCount() {
        return this.expandableListTitle.length;
    }

    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }

    @Override
    public View getGroupView(int listPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String listTitle = (String) getGroup(listPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.item_treeroot, null);
        }
        TextView listTitleTextView = convertView.findViewById(R.id.listTitle);
        listTitleTextView.setTypeface(null, Typeface.BOLD);
        listTitleTextView.setTextSize(16);
        listTitleTextView.setText(String.format("↘️ %s", listTitle.replaceAll("\"", " ")));
//        Button btn = convertView.findViewById(R.id.btn_zoom);
//        btn.setOnClickListener(eventZoomClicked);
//        btn.setVisibility(View.VISIBLE);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return true;
    }
}