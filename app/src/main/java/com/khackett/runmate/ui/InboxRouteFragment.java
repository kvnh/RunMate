package com.khackett.runmate.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.android.gms.maps.GoogleMap;
import com.khackett.runmate.MapsActivityDisplayRoute;
import com.khackett.runmate.R;
import com.khackett.runmate.adapters.RouteMessageAdapter;
import com.khackett.runmate.utils.ParseConstants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;

import java.util.List;

public class InboxRouteFragment extends ListFragment {


    protected SwipeRefreshLayout mSwipeRefreshLayout;

    // member variable to store the list of routes received by the user
    protected List<ParseObject> mRoutes;

    private int MY_STATUS_CODE = 1111;

    // Default constructor for InboxRouteFragment
    public InboxRouteFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 1st parameter is the layout id used for this fragment.
        // 2nd parameter is the container where the fragment will be displayed (this will be the ViewPager from MainActivity)
        // 3rd parameter should be false whenever a fragment is added to an activity in code.
        // Inflater object used to create a new view using the layout provided.
        // View then attached to the parent - the ViewPager object from MainActivity.
        View rootView = inflater.inflate(R.layout.fragment_inbox_route, container, false);

        // Set SwipeRefreshLayout component
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        // Set the onRefreshListener
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.swipeRefresh1,
                R.color.swipeRefresh2,
                R.color.swipeRefresh3,
                R.color.swipeRefresh4);

        // Return the view of the whole fragment
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Retrieve the routes from the Parse backend
        retrieveRoutes();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        // to tell whether it is an image or a video, we need to access the type of the message
        // create the message object which is set to the message at the current position
        ParseObject route = mRoutes.get(position);


        // set the data for the intent using the setData() method - this requires a URI
        // (URI's and URL's can often be used interchangeably)
        // Uri fileUri = Uri.parse(file.getUrl());

        JSONArray parseList = route.getJSONArray("latLngPoints");
        JSONArray parseListBounds = route.getJSONArray("latLngBoundaryPoints");
        String objectId = route.getObjectId();

        // Create an intent to display the route.
        Intent intent = new Intent(getActivity(), MapsActivityDisplayRoute.class);
        intent.putExtra("parseLatLngList", parseList.toString());
        intent.putExtra("parseLatLngBoundsList", parseListBounds.toString());
        intent.putExtra("myObjectId", objectId);

        // Start the MapsActivityDisplayRoute activity.
        startActivityForResult(intent, MY_STATUS_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_STATUS_CODE) {
            // Refresh the fragment.
            retrieveRoutes();
        }
    }

    private void retrieveRoutes() {
        // query the routes class/table in parse
        // get messages where the logged in user ID is in the list of the recipient ID's (we only want to retrieve the messages sent to us)
        // querying the message class is similar to how we have been querying users
        ParseQuery<ParseObject> queryRoute = new ParseQuery<ParseObject>(ParseConstants.CLASS_ROUTES);
        // use the 'where' clause to search through the messages to find where our user ID is one of the recipients
        queryRoute.whereEqualTo(ParseConstants.KEY_RECIPIENT_IDS, ParseUser.getCurrentUser().getObjectId());
        // order results so that most recent message are at the top of the inbox
        queryRoute.addDescendingOrder(ParseConstants.KEY_CREATED_AT);
        // query is ready - run it
        queryRoute.findInBackground(new FindCallback<ParseObject>() {
            // When the retrieval is done from the Parse query, the done() callback method is called
            @Override
            public void done(List<ParseObject> routes, ParseException e) {
                // dismiss the progress indicator here
                // getActivity().setProgressBarIndeterminateVisibility(false);

                // End refreshing once routes are retrieved
                // done() is called from onResume() and the OnRefreshListener
                // Need to check that its called from the the OnRefreshListener before ending it
                if (mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }

                // the list being returned is a list of routes
                if (e == null) {
                    // successful - routes found.  They are stored as a list in messages
                    mRoutes = routes;

                    // adapt this data for the list view, showing the senders name

                    // Create an array of strings to store usernames.
                    // Set the size equal to that of the list returned.
                    String[] usernames = new String[mRoutes.size()];
                    // Enhanced for loop to go through the list of users and create an array of usernames
                    int i = 0;
                    for (ParseObject message : mRoutes) {
                        // Get the specific key
                        usernames[i] = message.getString(ParseConstants.KEY_SENDER_NAME);
                        i++;
                    }

                    // Create the adapter once and update its state on each refresh.
                    if (getListView().getAdapter() == null) {
                        // the above adapter code is now replaced with the following line
                        RouteMessageAdapter adapter = new RouteMessageAdapter(getListView().getContext(), mRoutes);

                        // Force a refresh of the list once data has changed.
                        adapter.notifyDataSetChanged();

                        // Call setListAdapter (from ListActivity class) for this activity.
                        setListAdapter(adapter);
                    } else {
                        // Refill the adapter.
                        // Cast it to RouteMessageAdapter.
                        ((RouteMessageAdapter) getListView().getAdapter()).refill(mRoutes);
                    }
                }
            }
        });
    }

    protected SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            // When list is swiped down to refresh, retrieve the latest routes from the backend.
            retrieveRoutes();
        }
    };
}
