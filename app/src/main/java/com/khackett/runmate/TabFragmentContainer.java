package com.khackett.runmate;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.khackett.runmate.adapters.MyFragmentPagerAdapter;

/**
 * Created by KHackett on 31/08/15.
 */
public class TabFragmentContainer extends Fragment {

    // Create the FragmentPagerAdapter that will provide and manage tabs for each section.
    public static MyFragmentPagerAdapter myFragmentPagerAdapter;

    public static TabLayout tabLayout;

    // The ViewPager is a layout widget in which each child view is a separate tab in the layout.
    // It will host the section contents.
    public static ViewPager viewPager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate tab_layout_fragment_container view and setup views for the TabLayout and ViewPager items.
        View view = inflater.inflate(R.layout.tab_layout_fragment_container, null);

        tabLayout = (TabLayout) view.findViewById(R.id.tabs);

        // Set up the ViewPager with the sections adapter.
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);

        // Instantiate the adapter that will return a fragment for each of the three sections of the main activity
        myFragmentPagerAdapter = new MyFragmentPagerAdapter(getActivity(), getChildFragmentManager());

        // Set up the adapter for the ViewPager
        viewPager.setAdapter(myFragmentPagerAdapter);

        // Runnable() method required to implement setupWithViewPager() method
        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                tabLayout.setupWithViewPager(viewPager);
                viewPager.setCurrentItem(1, false);
                // tabLayout.getTabAt(1).select();
            }
        });

        // Return the created View
        return view;
    }

}
