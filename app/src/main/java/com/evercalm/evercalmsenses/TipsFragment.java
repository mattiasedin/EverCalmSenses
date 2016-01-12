package com.evercalm.evercalmsenses;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Anton on 2016-01-11.
 */
public class TipsFragment extends Fragment {
    ExpandableListView expandableListView;

    /**
     * The
     * fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static TipsFragment newInstance(int sectionNumber) {
        TipsFragment fragment = new TipsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public TipsFragment() {

    }

    public void initExpList(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_tabbed_tips, container, false);
        TextView textView = (TextView) rootView.findViewById(R.id.section_label);
        textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));

        //ExpandableList test
        expandableListView = (ExpandableListView) rootView.findViewById(R.id.exp_listview);

        //Change to images later
        List<String> tip_categories = new ArrayList<String>();
        List<String> tip1 = new ArrayList<String>();
        List<String> tip2 = new ArrayList<String>();
        List<String> tip3 = new ArrayList<String>();

        HashMap<String, List<String>> childList = new HashMap<String, List<String>>();

        String tipCategories[] = getResources().getStringArray(R.array.tips_categories);
        String t1[] = getResources().getStringArray(R.array.t1_items);
        String t2[] = getResources().getStringArray(R.array.t2_items);

        for(String categories: tipCategories ){
            tip_categories.add(categories);
        }

        for(String tips: t1){
            tip1.add(tips);
        }
        for(String tips: t2){
            tip2.add(tips);
        }

        childList.put(tip_categories.get(0), tip1);
        childList.put(tip_categories.get(1), tip2);

        MyExpandableListAdapter myExpandableListAdapter = new MyExpandableListAdapter(expandableListView, this.getActivity(), tip_categories, childList);
        expandableListView.setAdapter(myExpandableListAdapter);

        return rootView;
    }
}