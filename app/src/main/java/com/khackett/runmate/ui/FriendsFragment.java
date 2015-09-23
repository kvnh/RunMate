package com.khackett.runmate.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
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
 * Fragment to display the current users friends in a GridView layout.
 */
public class FriendsFragment extends Fragment {

    // TAG to represent the FriendsFragment class
    public static final String TAG = FriendsFragment.class.getSimpleName();

    // Set up a reference to the current user
    private ParseUser mCurrentUser;
    // Set up a member variable to store a list of friends for the current user returned from the ParseUser query
    private List<ParseUser> mFriends;
    // Set up a ParseRelation member to hold ParseUsers related to the current user
    private ParseRelation<ParseUser> mFriendsRelation;
    // Member variable for the GridView layout
    private GridView mGridView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 1st parameter: layout id used in this fragment.
        // 2nd parameter: container where the fragment will be displayed (the ViewPager from TabFragmentContainer).
        // 3rd parameter: false whenever a fragment is added to an activity.
        // Inflater object used to create a new View using the layout provided.
        // View is then attached to a parent - the ViewPager object from TabFragmentContainer.
        View rootView = inflater.inflate(R.layout.user_grid, container, false);

        // Return the view of the fragment
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set the GridView fragment
        mGridView = (GridView) view.findViewById(R.id.friendsGrid);

        // Check that there are friends to display - if not, display a message
        TextView emptyFriendsList = (TextView) view.findViewById(android.R.id.empty);
        // Attach this as the empty text view for the GridView
        mGridView.setEmptyView(emptyFriendsList);
    }


    @Override
    public void onResume() {
        super.onResume();

        // Get the current user using getCurrentUser()
        mCurrentUser = ParseUser.getCurrentUser();
        // For the FriendsRelation for current user, call getRelation()
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

        // Use ParseQuery to retrieve a list of the ParseUsers associated with this user.
        ParseQuery<ParseUser> friendsQuery = mFriendsRelation.getQuery();

        // Sort the list by username before calling it
        friendsQuery.addAscendingOrder(ParseConstants.KEY_USERNAME);

        // Run the query in a background thread to get the List
        friendsQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {

                // If no exception returned
                if (e == null) {

                    // Set the mFriends variable to the List of returned ParseUsers
                    mFriends = friends;

                    // Create an adapter and set it as the list adapter.
                    // Get the adapter associated with the GridView and check to see if it is null.
                    if (mGridView.getAdapter() == null) {
                        // Use the custom UserAdapter to display the user data in the GridView.
                        UserAdapter adapter = new UserAdapter(getActivity(), mFriends);
                        // Call setAdapter for this activity to set the items in the GridView.
                        mGridView.setAdapter(adapter);
                    } else {
                        // Adapter is not available - refill with the List of friends.
                        ((UserAdapter) mGridView.getAdapter()).refill(mFriends);
                    }

                } else {
                    // Log error and display a message to the user informing them of the error
                    Log.e(TAG, e.getMessage());
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    // Set the message from the exception
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