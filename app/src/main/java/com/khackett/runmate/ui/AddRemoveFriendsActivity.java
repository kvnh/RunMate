package com.khackett.runmate.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.khackett.runmate.R;
import com.khackett.runmate.adapters.UserAdapter;
import com.khackett.runmate.utils.ParseConstants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

/**
 * Class to and add and remove users from a friends list
 */
public class AddRemoveFriendsActivity extends Activity {

    /**
     * The maximum number of users returned from a ParseUser query
     */
    private static final int PARSE_USER_QUERY_LIMIT = 500;

    // TAG to represent the AddRemoveFriendsActivity class
    public static final String TAG = AddRemoveFriendsActivity.class.getSimpleName();

    // Set up a reference to the current user
    private ParseUser mCurrentUser;
    // Set up a member variable to store a list of users returned from the parse user query
    private List<ParseUser> mUsers;
    // Set up a ParseRelation member to hold ParseUsers
    private ParseRelation<ParseUser> mFriendsRelation;
    // Create a variable for the GridView
    private GridView mGridView;
    // Declare the context of the application.
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the GridView layout
        setContentView(R.layout.user_grid);

        // Set the GridView in the layout
        mGridView = (GridView) findViewById(R.id.friendsGrid);
        // mGridView keeps track of items that are selected (the check property on each item)
        // Set it to the default grid view and allow items to be checked
        mGridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE);
        // Add a listener to detect when items on the grid view have been tapped
        mGridView.setOnItemClickListener(mOnItemClickListener);

        // Check that there are friends to display - if not, display a message
        TextView emptyFriendsList = (TextView) findViewById(android.R.id.empty);
        // Attach this as the empty text view for the GridView
        mGridView.setEmptyView(emptyFriendsList);

        // Initialise the Context to AddRemoveFriendsActivity.
        mContext = AddRemoveFriendsActivity.this;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Get the current logged in user
        mCurrentUser = ParseUser.getCurrentUser();
        // For the FriendsRelation for current user, call getRelation()
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

        // Set up a dialog progress indicator box
        final ProgressDialog progressDialog = new ProgressDialog(AddRemoveFriendsActivity.this);
        progressDialog.setTitle(R.string.edit_friends_progress_dialog_title);
        progressDialog.setMessage(mContext.getString(R.string.edit_friends_progress_dialog_message));
        progressDialog.show();

        // Use ParseQuery to retrieve a list of the ParseUsers associated with this user.
        ParseQuery<ParseUser> friendsQuery = ParseUser.getQuery();
        // Sort the results of the query in ascending order by username.
        friendsQuery.orderByAscending(ParseConstants.KEY_USERNAME);
        // Set query limits to 500 users.
        friendsQuery.setLimit(PARSE_USER_QUERY_LIMIT);
        // Execute the query in the background thread.
        friendsQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> users, ParseException e) {

                // Dismiss progress dialog once result returned from backend
                progressDialog.dismiss();

                // If no exception returned
                if (e == null) {

                    // Set the mUsers to the List of returned users
                    mUsers = users;

                    // Use the custom UserAdapter
                    // Get the adapter associated with the GridView and check to see if it is null
                    if (mGridView.getAdapter() == null) {
                        // Use the custom UserAdapter to display the users in the GridView.
                        UserAdapter adapter = new UserAdapter(AddRemoveFriendsActivity.this, mUsers);
                        // Call setAdapter for this activity to set the items in the GridView.
                        mGridView.setAdapter(adapter);
                    } else {
                        // GridView is not available - refill with the list of friends
                        ((UserAdapter) mGridView.getAdapter()).refill(mUsers);
                    }

                    // Add checkmarks to the users friends
                    addFriendCheckMarks();

                } else {
                    // There was an error - log the message.
                    Log.e(TAG, e.getMessage());
                    // Display an alert to the user.
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddRemoveFriendsActivity.this);
                    builder.setMessage(e.getMessage())
                            .setTitle(R.string.error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        // The action bar will automatically handle clicks on the Home/Up button,
        // so long as a parent activity is specified in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Method to add a check mark a selected users picture.
     */
    private void addFriendCheckMarks() {
        // Use mFriendsRelation to return a list of the users friends from Parse
        mFriendsRelation.getQuery().findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {

                if (e == null) {
                    // If no exception - Query successful and list is returned.
                    // Get each user who has been selected
                    for (int i = 0; i < mUsers.size(); i++) {
                        // Store the user in a ParseUser variable
                        ParseUser user = mUsers.get(i);
                        // Iterate through the returned list to set the check mark of each selected user
                        for (ParseUser friend : friends) {
                            if (friend.getObjectId().equals(user.getObjectId())) {
                                // Set the check mark
                                mGridView.setItemChecked(i, true);
                            }
                        }
                    }
                } else {
                    // Log an exception
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

    // Show or hide the image for the check mark overlay
    // The item that is tapped on, gets passed in as the view parameter
    // The view parameter is the relative layout of the user_item.xml
    protected AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // Get a reference to the ImageView
            ImageView checkImageView = (ImageView) view.findViewById(R.id.checkFriendImageView);

            if (mGridView.isItemChecked(position)) {
                // Add a friend if checked
                // Pass in the user that was tapped on as the parameter (the position of the item that is tapped on)
                // Map this to the list of users stored in the variable mUsers - use get()
                mFriendsRelation.add(mUsers.get(position));

                // Manipulate the image view for the check mark - set to visible when selected
                checkImageView.setVisibility(View.VISIBLE);
            } else {
                // Remove the friend by calling the remove() method of ParseRelation
                mFriendsRelation.remove(mUsers.get(position));

                // Manipulate the image view for the check mark - set to invisible when selected
                checkImageView.setVisibility(View.INVISIBLE);
            }

            // Save this relation in Parse using the background thread
            mCurrentUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        // Alert the user of an exception
                        Log.e(TAG, e.getMessage());
                    }
                }
            });
        }
    };
}
