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

/**
 * Activity to choose the recipients for the Route
 */
public class RouteRecipientsActivity extends Activity {

    // TAG to represent the MyRunsFragment class
    public static final String TAG = RouteRecipientsActivity.class.getSimpleName();

    // Set up a reference to the current user
    private ParseUser mCurrentUser;
    // Set up a member variable to store a list of friends for the current user returned from the parse user query
    private List<ParseUser> mFriends;
    // Set up a ParseRelation member to hold ParseUsers
    private ParseRelation<ParseUser> mFriendsRelation;

    // Member variable for the send button - to be set on and off depending on whether a user is selected or not
    // Set this variable in the onCreateOptionsMenu.
    private MenuItem mSendMenuItem;

    // Member variables for the Route object.
    private ArrayList<LatLng> markerPoints;
    private ArrayList<LatLng> allLatLngPoints;
    private LatLngBounds mLatLngBounds;
    private double mRouteDistance;
    private String mRouteName;
    private String mCreationType;
    private Calendar mProposedDateTime;

    // Member variables to represent an array of ParseGeoPoint values to be stored in Parse.
    // Values are those points clicked on the map.
    private ArrayList<ParseGeoPoint> parseLatLngList;
    // Values are the southwest and northeast LatLng points at the bounds of the route.
    private ArrayList<ParseGeoPoint> parseLatLngBoundsList;

    // Member variable for the GridView layout
    private GridView mGridView;

    // Declare the context of the application.
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        // Set the layout view
        setContentView(R.layout.user_grid);

        // Set up the action bar
        getActionBar().setDisplayHomeAsUpEnabled(true);

        // Set the GridView fragment
        mGridView = (GridView) findViewById(R.id.friendsGrid);
        // mGridView keeps track of items that are selected (the check property on each item)
        // Set it to the default grid view and allow items to be checked
        mGridView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        // Set an OnItemClickListener
        mGridView.setOnItemClickListener(mOnItemClickListener);

        // Check that there are friends to display - if not, display a message
        TextView emptyFriendsList = (TextView) findViewById(android.R.id.empty);
        // Attach this as the empty text view for the GridView
        mGridView.setEmptyView(emptyFriendsList);

        // Get all of the Route data from the passed in intent
        markerPoints = getIntent().getParcelableArrayListExtra("markerPoints");
        allLatLngPoints = getIntent().getParcelableArrayListExtra("allLatLngPoints");
        mLatLngBounds = getIntent().getParcelableExtra("boundaryPoints");
        mRouteDistance = getIntent().getDoubleExtra("routeDistance", mRouteDistance);
        mRouteName = getIntent().getStringExtra("routeName");
        mCreationType = getIntent().getStringExtra("routeCreationMethod");
        mProposedDateTime = (Calendar) getIntent().getSerializableExtra("proposedTime");

        Log.i(TAG, "Proposed date and time in RouteRecipientsActivity is (getTime()): " + mProposedDateTime.getTime());

        // Initialise the Context to RouteRecipientsActivity.
        mContext = RouteRecipientsActivity.this;
    }

    // get a list of all your friends - this code is copied from the onResume() method in the FriendsFragment with some additions
    @Override
    public void onResume() {
        super.onResume();

        // Get the current user using getCurrentUser()
        mCurrentUser = ParseUser.getCurrentUser();
        // For the FriendsRelation for current user, call getRelation()
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

        // Set up a dialog progress indicator box - start it before the query to backend is run
        final ProgressDialog progressDialog = new ProgressDialog(RouteRecipientsActivity.this);
        progressDialog.setTitle(R.string.edit_friends_progress_dialog_title);
        progressDialog.setMessage(mContext.getString(R.string.edit_friends_progress_dialog_message));
        progressDialog.show();

        // Get a list of the users friends.
        // Use ParseQuery to retrieve a list of the ParseUsers associated with this user.
        ParseQuery<ParseUser> friendsQuery = mFriendsRelation.getQuery();

        // Sort the list by username before calling it
        friendsQuery.addAscendingOrder(ParseConstants.KEY_USERNAME);

        // Run the query in a background thread to get the List
        friendsQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {

                // Dismiss progress dialog once result returned from backend
                progressDialog.dismiss();

                // If no exception returned
                if (e == null) {

                    // set the mFriends variable based on the list of friends that is returned
                    mFriends = friends;

                    // Remove the current user from the list if they are there
                    mFriends.remove(ParseUser.getCurrentUser());

                    // Create an adapter and set it as the list adapter.
                    // Get the adapter associated with the GridView and check to see if it is null.
                    if (mGridView.getAdapter() == null) {
                        // Use the custom UserAdapter to display the user data in the GridView.
                        UserAdapter adapter = new UserAdapter(RouteRecipientsActivity.this, mFriends);
                        // Call setAdapter for this activity to set the items in the GridView.
                        mGridView.setAdapter(adapter);
                    } else {
                        // Adapter is not available - refill with the List of friends.
                        ((UserAdapter) mGridView.getAdapter()).refill(mFriends);
                    }
                } else {
                    // Log error and display a message to the user informing them of the error
                    Log.e(TAG, e.getMessage());
                    AlertDialog.Builder builder = new AlertDialog.Builder(RouteRecipientsActivity.this);
                    // Set the message from the exception
                    builder.setMessage(e.getMessage())
                            .setTitle(R.string.error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });// Set the message from the exception
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_route_recipients, menu);

        // Once the menu is inflated, get a menu item object using the getItem() method of the menu that is passed in.
        // Since only one item, get position 0
        mSendMenuItem = menu.getItem(0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        // The action bar will automatically handle clicks on the Home/Up button,
        // so long as a parent activity is specified in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_send) {
            // It user selects the send button, create a parse object for the route
            ParseObject route = createRoute();

            // The Route will be null if there is an error
            if (route == null) {
                // Display error message to the user
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.error_selecting_recipient_message)
                        .setTitle(R.string.error_selecting_recipient_title)
                        .setPositiveButton(android.R.string.ok, null);
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                // Send the created Route
                sendRoute(route);

                // Send the user back to the MainActivity right after the route is sent.
                // Use finish() to close the current activity and start a new MainActivity intent.
                finish();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Method to create a Route object with the generated data
     *
     * @return the Route object
     */
    public ParseObject createRoute() {

        // Create a ParseObject and add the relevant data using the key-value pairs
        ParseObject route = new ParseObject(ParseConstants.CLASS_ROUTES);

        // Add the LatLng points plotted on the map
        route.addAll(ParseConstants.KEY_LATLNG_POINTS, (convertLatLngToParseGeoPointArray(markerPoints)));
        // Add al of the LatLng points generated by the Directions API
        route.addAll(ParseConstants.KEY_ALL_LATLNG_POINTS, (convertLatLngToParseGeoPointArray(allLatLngPoints)));
        // Add the max and min lat and long points
        route.addAll(ParseConstants.KEY_LATLNG_BOUNDARY_POINTS, (convertLatLngBoundsToParseGeoPointArray(mLatLngBounds)));
        // Add the senders Id
        route.put(ParseConstants.KEY_SENDER_IDS, ParseUser.getCurrentUser().getObjectId());
        // Add the senders name
        route.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
        // Add the recipients ID's - use the getRecipientIds() method
        route.put(ParseConstants.KEY_RECIPIENT_IDS, getRecipientIds());
        // Add the Route distance
        route.put(ParseConstants.KEY_ROUTE_DISTANCE, mRouteDistance);
        // Add the Route name
        route.put(ParseConstants.KEY_ROUTE_NAME, mRouteName);
        // Add the creation type of the Route
        route.put(ParseConstants.KEY_ROUTE_CREATION_TYPE, mCreationType);
        // Add the proposed date and time of the Route
        route.put(ParseConstants.KEY_ROUTE_PROPOSED_TIME, mProposedDateTime.getTime());

        // Return the Route
        return route;
    }

    /**
     * Method to return a collection of ID's of the intended recipients
     *
     * @return an ArrayList of the intended recipients
     */
    public ArrayList<String> getRecipientIds() {
        ArrayList<String> recipientIds = new ArrayList<String>();
        // Iterate though each user in the list
        for (int i = 0; i < mGridView.getCount(); i++) {
            if (mGridView.isItemChecked(i)) {
                // If the user is checked on the recipients list add their ID to the array list
                recipientIds.add(mFriends.get(i).getObjectId());
            }
        }
        return recipientIds;
    }

    /**
     * Method to upload a created Route to Parse.
     *
     * @param route the Route object to upload to Parse
     */
    public void sendRoute(ParseObject route) {
        route.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    // If no exception
                    Toast.makeText(RouteRecipientsActivity.this, R.string.success_route, Toast.LENGTH_LONG).show();
                    // Send a push notification to the intended recipients
                    sendPushNotifications();
                } else {
                    // There was an error - notify the user
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
     * Method to convert an array of LatLng elements to an array of ParseGeoPoint elements
     *
     * @param list an ArrayList of LatLng values
     * @return an ArrayList of ParseGeoPoints
     */
    public ArrayList<ParseGeoPoint> convertLatLngToParseGeoPointArray(ArrayList<LatLng> list) {
        // Instantiate the ParseGeoPoint ArrayList
        parseLatLngList = new ArrayList<ParseGeoPoint>();
        // Iterate through the LatLng list and add the latitude and longitude values to the ParseGeoPoint ArrayList
        for (LatLng item : list) {
            ParseGeoPoint parseGeoPoint = new ParseGeoPoint(item.latitude, item.longitude);
            parseLatLngList.add(parseGeoPoint);
        }
        return parseLatLngList;
    }


    /**
     * Method to convert an array of LatLng elements to an array of ParseGeoPoint elements
     *
     * @param latLngBounds
     * @return
     */
    public ArrayList<ParseGeoPoint> convertLatLngBoundsToParseGeoPointArray(LatLngBounds latLngBounds) {
        // Instantiate the ParseGeoPoint ArrayList
        parseLatLngBoundsList = new ArrayList<ParseGeoPoint>();

        // Create LatLng variables to hold southWest and northEast values
        LatLng southWest = latLngBounds.southwest;
        LatLng northEast = latLngBounds.northeast;

        // Convert these to ParseGeoPoint values
        ParseGeoPoint geoPointSouthWest = new ParseGeoPoint(southWest.latitude, southWest.longitude);
        ParseGeoPoint geoPointNorthEast = new ParseGeoPoint(northEast.latitude, northEast.longitude);

        // Add the ParseGeoPoints to the ArrayList and return
        parseLatLngBoundsList.add(0, geoPointSouthWest);
        parseLatLngBoundsList.add(1, geoPointNorthEast);

        return parseLatLngBoundsList;
    }

    // Show or hide the image for the check mark overlay
    // The item that is tapped on, gets passed in as the view parameter
    // The view parameter is the relative layout of the user_item.xml
    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // Change the visibility of the send button (set in menu_route_recipients.xml) whenever a friend is selected
            // Create the menu in onCreateOptionsMenu
            // Check the number of items that are checked on the grid view
            if (mGridView.getCheckedItemCount() > 0) {
                // Set the menuItem to visible if an item is clicked
                mSendMenuItem.setVisible(true);
            } else {
                // Otherwise, if it is 0, hide the menu item
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

    /**
     * Method to send a push notification to a user in the Route recipient list
     */
    protected void sendPushNotifications() {
        // Add the ParseQuery and attach specific targeting data.
        ParseQuery<ParseInstallation> query = ParseInstallation.getQuery();
        // Match with the user ID's in the recipients list.
        // getRecipientIds() returns an array list of Strings.
        // Notify users where the ID is equal to one of the recipients ID's.
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