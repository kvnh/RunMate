package com.khackett.runmate.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.khackett.runmate.R;
import com.khackett.runmate.ui.FriendsFragment;
import com.khackett.runmate.ui.InboxRouteFragment;
import com.khackett.runmate.ui.MyRunsFragment;
import com.khackett.runmate.ui.SettingsFragment;

import java.util.Locale;

/**
 * Created by KHackett on 06/08/15.
 * FragmentPagerAdapter class that returns a fragment that corresponds to the chosen tabs.
 * This component adapts fragments for a ViewPager in TabFragmentContainer class.
 * FragmentPagerAdapter is best used when navigating between sibling screens that represent a fixed,
 * small number of pages.
 */
public class MyFragmentPagerAdapter extends FragmentPagerAdapter {

    // The context to be passed in when the adapter is created.
    private Context mContext;
    // The number of tabs in the layout.
    public static int numberOfTabs = 2;

    /**
     * Default constructor that accepts a FragmentManager parameter to add or remove fragments.
     *
     * @param context         the context from the activity using the adapter.
     * @param fragmentManager the FragmentManager for managing Fragments inside of the TabFragmentContainer.
     */
    public MyFragmentPagerAdapter(Context context, FragmentManager fragmentManager) {
        super(fragmentManager);
        mContext = context;
    }

    /**
     * Method to return the relevant fragment for the selected tab.
     */
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new MyRunsFragment();
            case 1:
                return new InboxRouteFragment();
        }
        return null;
    }

    /**
     * Method that gets the number of tabs in the layout.
     *
     * @return the number of tabs in the layout.
     */
    @Override
    public int getCount() {
        return numberOfTabs;
    }

    /**
     * Method that returns the title of each tab in the layout.
     */
    @Override
    public CharSequence getPageTitle(int position) {
        Locale locale = Locale.getDefault();
        switch (position) {
            case 0:
                return mContext.getString(R.string.title_section1).toUpperCase(locale);
            case 1:
                return mContext.getString(R.string.title_section2).toUpperCase(locale);
//            case 2:
//                return mContext.getString(R.string.title_section3).toUpperCase(locale);
        }
        return null;
    }
}