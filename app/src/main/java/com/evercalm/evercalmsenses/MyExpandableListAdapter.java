package com.evercalm.evercalmsenses;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.DefaultItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Anton on 2016-01-12.
 */
public class MyExpandableListAdapter extends BaseExpandableListAdapter{
    private List<String> tips;
    private HashMap<String, List<String>> child_titles;
    private Context context;
    private int lastExpandedGroupPosition = -1;
    private ExpandableListView listView;


    MyExpandableListAdapter(ExpandableListView listView, Context ctx, List<String> tips, HashMap<String, List<String>> child_titles ){
        context = ctx;
        this.tips = tips;
        this.child_titles = child_titles;
        this.listView = listView;
    }

    @Override
    public int getGroupCount() {
        //Numbers of tips
        return tips.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        //1 child per parent
        return child_titles.get(tips.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return tips.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return child_titles.get(tips.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        String title = (String) this.getGroup(groupPosition);

        if(convertView == null){
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.tips_category, parent, false);
        }

        TextView textView = (TextView) convertView.findViewById(R.id.parent_item);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setText(title);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        String title = (String) this.getChild(groupPosition, childPosition);

        if(convertView == null){
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.tips_item, parent, false);
        }

        TextView textView = (TextView) convertView.findViewById(R.id.child_item);
        textView.setText(title);

        YoYo.with(Techniques.FadeIn)
                .duration(700)
                .playOn(convertView.findViewById(R.id.child_item));

        return convertView;
    }


    @Override
    public void onGroupExpanded(int groupPosition) {

        if(lastExpandedGroupPosition != groupPosition){
            listView.collapseGroup(lastExpandedGroupPosition);
        }
        super.onGroupExpanded(groupPosition);

        lastExpandedGroupPosition = groupPosition;
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        super.onGroupCollapsed(groupPosition);
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
