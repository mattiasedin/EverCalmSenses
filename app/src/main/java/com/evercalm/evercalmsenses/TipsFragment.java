package com.evercalm.evercalmsenses;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 *
 * Created by Anton on 2016-01-11.
 */
public class TipsFragment extends Fragment {

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
        /*Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);*/
        return fragment;
    }

    public TipsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_tabbed_tips, container, false);
        // Create and fill expandablelistview
        initExpandableListView(rootView);

        return rootView;
    }

    private void initExpandableListView(View rootView){

        //Create an ExpandableListView
        ExpandableListView expandableListView = (ExpandableListView) rootView.findViewById(R.id.exp_listview);

        List<String> tip_categories = new ArrayList<String>();
        List<String> tip1 = new ArrayList<String>();
        List<String> tip2 = new ArrayList<String>();
        List<String> tip3 = new ArrayList<String>();

        HashMap<String, List<String>> childList = new HashMap<String, List<String>>();

        // Get all data fron XML-file
        String tipCategories[] = getResources().getStringArray(R.array.tips_categories);
        String t1[] = getResources().getStringArray(R.array.t1_items);
        String t2[] = getResources().getStringArray(R.array.t2_items);
        String t3[] = getResources().getStringArray(R.array.t3_items);

        // Fill all lists (Tip categories and all tips)
        Collections.addAll(tip_categories, tipCategories);
        Collections.addAll(tip1, t1);
        Collections.addAll(tip2, t2);
        Collections.addAll(tip3, t3);

        // Fill the hashmap Key: Category, Value: list of tips
        childList.put(tip_categories.get(0), tip1);
        childList.put(tip_categories.get(1), tip2);
        childList.put(tip_categories.get(2), tip3);

        //Create an adapter for the expandableListView
        MyExpandableListAdapter myExpandableListAdapter = new MyExpandableListAdapter(expandableListView, this.getActivity(), tip_categories, childList);
        expandableListView.setAdapter(myExpandableListAdapter);
    }

}