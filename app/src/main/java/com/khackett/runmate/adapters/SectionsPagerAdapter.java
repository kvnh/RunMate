//package com.khackett.runmate.adapters;
//
//import android.content.Context;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentPagerAdapter;
//
//import com.khackett.runmate.ui.FriendsFragment;
//import com.khackett.runmate.ui.InboxRouteFragment;
//import com.khackett.runmate.R;
//
//import java.util.Locale;
//
///**
// * Created by KHackett on 06/08/15.
// * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
// * one of the sections/tabs/pages.
// * This is a component that adapts fragments for a ViewPager
// */
//public class SectionsPagerAdapter extends FragmentPagerAdapter {
//
//    // FragmentPagerAdapter is best when navigating between sibling screens representing a fixed, small number of pages.
//    // Create a new member variable to represent the context used for context methods below
//    // The context will be passed in when this adapter is created
//    protected Context mContext;
//
//    public SectionsPagerAdapter(Context context, FragmentManager fm) {
//        super(fm);
//        // add a context parameter to this constructor
//        mContext = context;
//    }
//
//    @Override
//    public Fragment getItem(int position) {
//        // getItem is called to instantiate the fragment for the given page.
//        switch (position) {
//            case 0:
//                return new InboxRouteFragment();
//            case 1:
//                return new FriendsFragment();
//        }
//        return null;
//    }
//
//    @Override
//    public int getCount() {
//        return 2;
//    }
//
//    @Override
//    public CharSequence getPageTitle(int position) {
//        Locale l = Locale.getDefault();
//        switch (position) {
//            case 0:
//                return mContext.getString(R.string.title_section2).toUpperCase(l);
//            case 1:
//                return mContext.getString(R.string.title_section3).toUpperCase(l);
//        }
//        return null;
//    }
//
//}
