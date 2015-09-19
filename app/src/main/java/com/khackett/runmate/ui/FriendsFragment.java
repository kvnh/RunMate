package com.khackett.runmate.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import com.khackett.runmate.R;
import com.khackett.runmate.adapters.UserAdapter;
import com.khackett.runmate.utils.ParseConstants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by KHackett on 23/07/15.
 */
public class FriendsFragment extends Fragment {

    public static final String TAG = FriendsFragment.class.getSimpleName();

    // set up a reference to the current user
    protected ParseUser mCurrentUser;
    // set up a member variable to store a list of friends for the current user returned from the parse user query
    protected List<ParseUser> mFriends;
    // set up a ParseRelation member to hold ParseUsers
    protected ParseRelation<ParseUser> mFriendsRelation;
    // add a variable for the gridview
    protected GridView mGridView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // the 1st parameter is the layout id that is used for this fragment,
        // the 2nd is the container where the fragment will be displayed (this will be the ViewPager from main activity),
        // the 3rd parameter should be false whenever we add a fragment to an activity in code, which is what we are going to do
        // So this line of code uses an inflater object to create a new view using the layout we provide.
        // It then attaches that view to a parent, which in this case is the ViewPager object from main activity
        View rootView = inflater.inflate(R.layout.user_grid, container, false);

        // Set the GridView fragment
        mGridView = (GridView) rootView.findViewById(R.id.friendsGrid);

        // Check that there are friends to display - if not, display a message
        TextView emptyFriendsList = (TextView) rootView.findViewById(android.R.id.empty);
        // Attach this as the empty text view for the GridView
        mGridView.setEmptyView(emptyFriendsList);

        // note that this method returns a view - this is the view of the whole fragment
        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();

        // get the current user using the getCurrentUser() method
        mCurrentUser = ParseUser.getCurrentUser();
        // for the relation, from this user we want to call a method called getRelation()
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

        // the first thing we need is a list of the users friends
        // we have the friend relation, but this doesn't give us a list of users to work with
        // the list itself is still on the back end, we need to use the ParseRelation to retrieve it
        // use the built in query to retrieve it - this gets us the query associated with this ParseRelation
        ParseQuery<ParseUser> query = mFriendsRelation.getQuery();

        // sort the list by username before calling it
        query.addAscendingOrder(ParseConstants.KEY_USERNAME);

        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {

                // getActivity().setProgressBarIndeterminateVisibility(false);

                // include an if statement to check the exception
                if (e == null) {

                    // set the mFriends variable based on the list of friends that is returned
                    mFriends = friends;

                    // now we need to use mFriends as the data source for the list view in our fragments
                    // we need to create an adapter and set it as the list adapter, just like we do for lost activities
                    // this is very similar to what we are ding for all users in the EditFriends activity, so copy and paste that code

                    // create an array of strings to store the usernames and set the size equal to that of the list returned
                    String[] usernames = new String[mFriends.size()];
                    // enhanced for loop to go through the list of parse users and create an array of usernames
                    int i = 0;
                    for (ParseUser user : mFriends) {
                        usernames[i] = user.getUsername();
                        i++;
                    }

                    // Get the adapter associated with the GridView and check to see if it is null
                    if (mGridView.getAdapter() == null) {
                        // Use the custom UserAdapter to display the users in the GridView
                        UserAdapter adapter = new UserAdapter(getActivity(), mFriends);
                        // Call setAdapter for this activity to set the items in the GridView
                        mGridView.setAdapter(adapter);
                    } else {
                        // GridView is not available - refill with the list of friends
                        ((UserAdapter) mGridView.getAdapter()).refill(mFriends);
                    }

                } else {
                    // display a message to the user (copied from EditFriendsActivity)
                    // there was an error - log the message
                    Log.e(TAG, e.getMessage());
                    // display an alert to the user
                    // if there is a parse exception then...
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    // set the message from the exception
                    builder.setMessage(e.getMessage())
                            .setTitle(R.string.error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });

    }

}