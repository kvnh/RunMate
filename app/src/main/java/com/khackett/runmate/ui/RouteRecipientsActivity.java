package com.khackett.runmate.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.khackett.runmate.R;
import com.khackett.runmate.adapters.UserAdapter;
import com.khackett.runmate.utils.ParseConstants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RouteRecipientsActivity extends Activity {

    public static final String TAG = RouteRecipientsActivity.class.getSimpleName();

    // set up a reference to the current user
    protected ParseUser mCurrentUser;
    // set up a member variable to store a list of friends for the current user returned from the parse user query
    protected List<ParseUser> mFriends;
    // set up a ParseRelation member to hold ParseUsers
    protected ParseRelation<ParseUser> mFriendsRelation;

    // create a menu item member variable so that it can be referenced below (it is a send button to be set on and off depending on if a user is selected)
    // set this variable in the onCreateOptionsMenu
    protected MenuItem mSendMenuItem;

    // member variable to represent the array of LatLng values plotted my the user and passed into this activity via the intent that started it
    protected ArrayList<LatLng> markerPoints;

    protected ArrayList<LatLng> allLatLngPoints;

    protected LatLngBounds latLngBounds;

    protected double mRouteDistance;

    // Member variable to represent an array of ParseGeoPoint values to be stored in the parse.com cloud
    // Values are those points clicked on the map
    protected ArrayList<ParseGeoPoint> parseLatLngList;

    // Member variable to represent an array of ParseGeoPoint values to be stored in the parse.com cloud
    // Values are the southwest and northeast LatLng points at the bounds of the route
    protected ArrayList<ParseGeoPoint> parseLatLngBoundsList;

    // member variable for the GridView
    protected GridView mGridView;

    protected String mRouteName;

    protected String mCreationType;

    protected Calendar mProposedDateTime;

    // Declare the context of the activity.
    protected Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.user_grid);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        mGridView = (GridView) findViewById(R.id.friendsGrid);

        // mGridView keeps track of items that are selected (this is the check property on each item)
        // loop through the grid to see who is checked - do this when ready to send
        // get the default grid view associated with this activity
        mGridView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);  // we can now check and uncheck multiple friends

        // Set an OnItemClickListener
        mGridView.setOnItemClickListener(mOnItemClickListener);

        // Check that there are friends to display - if not, display a message
        TextView emptyFriendsList = (TextView) findViewById(android.R.id.empty);
        // Attach this as the empty text view for the GridView
        mGridView.setEmptyView(emptyFriendsList);

        // get the array of LatLng points from the passed in intent
        markerPoints = getIntent().getParcelableArrayListExtra("markerPoints");

        allLatLngPoints = getIntent().getParcelableArrayListExtra("allLatLngPoints");

        latLngBounds = getIntent().getParcelableExtra("boundaryPoints");

        // mRouteDistance = getIntent().getParcelableExtra("routeDistance");
        mRouteDistance = getIntent().getDoubleExtra("routeDistance", mRouteDistance);

        // Get the name of the route from the passed in intent
        mRouteName = getIntent().getStringExtra("routeName");

        mCreationType = getIntent().getStringExtra("routeCreationMethod");

        // Get the proposed date and time from the passed in intent
        mProposedDateTime = (Calendar) getIntent().getSerializableExtra("proposedTime");
        Log.i(TAG, "Proposed date and time in RouteRecipientsActivity is: " + mProposedDateTime);
        Log.i(TAG, "Proposed date and time in RouteRecipientsActivity is (getTime()): " + mProposedDateTime.getTime());

        // Initialise the Context to the EditFriendsActivity.
        mContext = RouteRecipientsActivity.this;

    }

    // get a list of all your friends - this code is copied from the onResume() method in the FriendsFragment with some additions
    @Override
    public void onResume() {
        super.onResume();

        // get the current user using the getCurrentUser() method
        mCurrentUser = ParseUser.getCurrentUser();
        // for the relation, from this user we want to call a method called getRelation()
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

        // Set up a dialog progress indicator box - start it before the query to backend is run
        final ProgressDialog progressDialog = new ProgressDialog(RouteRecipientsActivity.this);
        progressDialog.setTitle(R.string.edit_friends_progress_dialog_title);
        progressDialog.setMessage(mContext.getString(R.string.edit_friends_progress_dialog_message));
        progressDialog.show();

        // the first thing we need is a list of the users friends...
        // we have the friend relation, but this doesn't give us a list of users to work with
        // the list itself is still on the back end, we need to use the ParseRelation to retrieve it
        // use the build in query to retrieve it - this gets us the query associated with this ParseRelation
        ParseQuery<ParseUser> query = mFriendsRelation.getQuery();

        // sort the list by username before calling it
        query.addAscendingOrder(ParseConstants.KEY_USERNAME);

        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {

                // Dismiss progress dialog once result returned from backend
                progressDialog.dismiss();

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

                    // Use the custom user adapter
                    // Get the adapter associated with the GridView and check to see if it is null
                    if (mGridView.getAdapter() == null) {
                        // Use the custom UserAdapter to display the users in the GridView
                        UserAdapter adapter = new UserAdapter(RouteRecipientsActivity.this, mFriends);
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(RouteRecipientsActivity.this);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_route_recipients, menu);

        // once the menu is inflated, we can can get a menu item object using the getItem() method of the menu that is passed in
        // use the int parameter to specify its position in the menu - since we only have 1 item, it will be at position 0
        mSendMenuItem = menu.getItem(0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will automatically handle clicks on the Home/Up button,
        // so long as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_send) {
            // create a parse object for the route
            ParseObject route = createRoute();

            // the message variable will be null if something goes wrong
            // so we only want to call the send() method if it is not null
            if (route == null) {
                // display error message
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.error_selecting_recipient_message)
                        .setTitle(R.string.error_selecting_recipient_title)
                        .setPositiveButton(android.R.string.ok, null);
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                // create a send message that will accept the route as a parameter
                sendRoute(route);

                // Send the user back to the main activity right after the message is sent.
                // Use finish() to close the current activity and start a new main activity intent
                finish();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected ParseObject createRoute() {
        // create a new parse object called route
        // (we can create a whole new class of parse objects in the back end by simply using a new name)
        ParseObject route = new ParseObject(ParseConstants.CLASS_ROUTES);

        // add the LatLng points from the plotted map to the ParseObject route
        route.addAll(ParseConstants.KEY_LATLNG_POINTS, (convertLatLngToParseGeoPointArray(markerPoints)));

        route.addAll(ParseConstants.KEY_ALL_LATLNG_POINTS, (convertLatLngToParseGeoPointArray(allLatLngPoints)));

        // add the max and min lat and long points from the plotted map to the ParseObject route
        route.addAll(ParseConstants.KEY_LATLNG_BOUNDARY_POINTS, (convertLatLngBoundsToParseGeoPointArray(latLngBounds)));

        // now that we have an object, we can start adding data, using the key-value pairs...
        // first, get a String representation of the ID
        route.put(ParseConstants.KEY_SENDER_IDS, ParseUser.getCurrentUser().getObjectId());
        // put the senders name
        route.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
        // put the recipient ID's
        // get the selected friends from the list through the helper method getRecipientIds()
        route.put(ParseConstants.KEY_RECIPIENT_IDS, getRecipientIds());

        route.put(ParseConstants.KEY_ROUTE_DISTANCE, mRouteDistance);

        route.put(ParseConstants.KEY_ROUTE_NAME, mRouteName);

        route.put(ParseConstants.KEY_ROUTE_CREATION_TYPE, mCreationType);

        route.put(ParseConstants.KEY_ROUTE_PROPOSED_TIME, mProposedDateTime.getTime());

        // return a successful route
        return route;
    }

    /**
     * method to return a collection of ID's
     *
     * @return
     */
    protected ArrayList<String> getRecipientIds() {
        ArrayList<String> recipientIds = new ArrayList<String>();
        // iterate though each user in the list
        for (int i = 0; i < mGridView.getCount(); i++) {
            // if the user is checked on the recipients list
            if (mGridView.isItemChecked(i)) {
                // add their ID to the array list
                recipientIds.add(mFriends.get(i).getObjectId());
            }
        }
        return recipientIds;
    }

    // method that uploads a file to the backend where recipients will be able to check for it
    protected void sendRoute(ParseObject route) {
        route.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    // successful
                    Toast.makeText(RouteRecipientsActivity.this, R.string.success_route, Toast.LENGTH_LONG).show();
                    sendPushNotifications();
                } else {
                    // there is an error - notify the user so they don't miss it
                    AlertDialog.Builder builder = new AlertDialog.Builder(RouteRecipientsActivity.this);
                    builder.setMessage(R.string.error_sending_route_message)
                            .setTitle(R.string.error_sending_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
    }

    /**
     * method to convert an array of LatLng elements to an array of ParseGeoPoint elements
     *
     * @param list
     * @return
     */
    protected ArrayList<ParseGeoPoint> convertLatLngToParseGeoPointArray(ArrayList<LatLng> list) {
        parseLatLngList = new ArrayList<ParseGeoPoint>();
        for (LatLng item : list) {
            ParseGeoPoint parseGeoPoint = new ParseGeoPoint(item.latitude, item.longitude);
            parseLatLngList.add(parseGeoPoint);
        }
        return parseLatLngList;
    }

    protected ArrayList<ParseGeoPoint> convertLatLngBoundsToParseGeoPointArray(LatLngBounds latLngBounds) {

        parseLatLngBoundsList = new ArrayList<ParseGeoPoint>();

        LatLng southWest = latLngBounds.southwest;
        LatLng northEast = latLngBounds.northeast;

        ParseGeoPoint geoPointSouthWest = new ParseGeoPoint(southWest.latitude, southWest.longitude);
        ParseGeoPoint geoPointNorthEast = new ParseGeoPoint(northEast.latitude, northEast.longitude);

        // Add the ParseGeoPoints to the ArrayList
        parseLatLngBoundsList.add(0, geoPointSouthWest);
        parseLatLngBoundsList.add(1, geoPointNorthEast);

        // return list
        return parseLatLngBoundsList;
    }


    // Show or hide the image for the check mark overlay
    // The item that is tapped on, gets passed in as the view parameter
    // The view parameter is the relative layout of the user_item.xml
    protected AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // Change the visibility of the send button (set in menu_route_recipients.xml) whenever a friend is selected
            // Create the menu in onCreateOptionsMenu
            // Check the number of items that are checked on the grid view
            if (mGridView.getCheckedItemCount() > 0) {
                // set the menuItem to visible if an item is clicked
                mSendMenuItem.setVisible(true);
            } else {
                // otherwise, if it is 0, then hide the menu item
                mSendMenuItem.setVisible(false);
            }

            // Set the check friend image view
            ImageView checkImageView = (ImageView) view.findViewById(R.id.checkFriendImageView);

            if (mGridView.isItemChecked(position)) {
                // Manipulate the image view for the check mark - set to visible when selected
                checkImageView.setVisibility(View.VISIBLE);
            } else {
                // Manipulate the image view for the check mark - set to invisible when selected
                checkImageView.setVisibility(View.INVISIBLE);
            }
        }
    };

    protected void sendPushNotifications() {
        // Add the ParseQuery and attach specific targeting data
        ParseQuery<ParseInstallation> query = ParseInstallation.getQuery();
        // Match with the user ID's in the recipients list
        // getRecipientIds() returns an array list of Strings
        // Notify users where the ID is equal to one of the recipients ID's
        query.whereContainedIn(ParseConstants.KEY_USER_ID, getRecipientIds());

        // Send a push notification
        ParsePush parsePush = new ParsePush();
        // Attach the query defined above
        parsePush.setQuery(query);
        // Set the message of the notification
        parsePush.setMessage(getString(R.string.push_notification_message, ParseUser.getCurrentUser().getUsername()));
        // Send the message in a background thread
        parsePush.sendInBackground();
    }
}