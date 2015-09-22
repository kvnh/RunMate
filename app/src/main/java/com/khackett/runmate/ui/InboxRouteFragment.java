package com.khackett.runmate.ui;

import android.app.ProgressDialog;
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

/**
 * Fragment to display the List of Routes in the users inbox
 */
public class InboxRouteFragment extends ListFragment {

    // TAG to represent the InboxRouteFragment class
    public static final String TAG = InboxRouteFragment.class.getSimpleName();

    // Swipe to refresh member variable
    private SwipeRefreshLayout mSwipeRefreshLayout;

    // Member variable to store the list of Routes received by the user
    private List<ParseObject> mRoutes;

    /**
     * Default constructor for InboxRouteFragment
     */
    public InboxRouteFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 1st parameter: layout id used in this fragment.
        // 2nd parameter: container where the fragment will be displayed (the ViewPager from TabFragmentContainer).
        // 3rd parameter: false whenever a fragment is added to an activity.
        // Inflater object used to create a new View using the layout provided.
        // View is then attached to a parent - the ViewPager object from TabFragmentContainer.
        View rootView = inflater.inflate(R.layout.fragment_inbox_route, container, false);

        // Return the view of the fragment
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set SwipeRefreshLayout component
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        // Set the onRefreshListener
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.swipeRefresh1,
                R.color.swipeRefresh2,
                R.color.swipeRefresh3,
                R.color.swipeRefresh4);

        // Retrieve the routes from Parse
        retrieveRoutes();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        // Create a ParseObject which is set to the selected Route message
        ParseObject route = mRoutes.get(position);

        // Assign data from the ParseObject to Route variables.
        JSONArray parseList = route.getJSONArray(ParseConstants.KEY_LATLNG_POINTS);
        JSONArray parseListBounds = route.getJSONArray(ParseConstants.KEY_LATLNG_BOUNDARY_POINTS);
        String objectId = route.getObjectId();
        String creationType = route.getString(ParseConstants.KEY_ROUTE_CREATION_TYPE);

        // Create an intent to display the route and add relevant item data.
        Intent intent = new Intent(getActivity(), MapsActivityDisplayRoute.class);
        intent.putExtra("parseLatLngList", parseList.toString());
        intent.putExtra("parseLatLngBoundsList", parseListBounds.toString());
        intent.putExtra("myObjectId", objectId);
        intent.putExtra("creationType", creationType);

        // Start MapsActivityDisplayRoute with the item data.
        startActivity(intent);
    }

    /**
     * Method to retrieve a List of Route items from Parse
     */
    private void retrieveRoutes() {

        // Query the Routes table in Parse.
        // Get Routes where the logged in user ID is in the list of recipient ID's.
        ParseQuery<ParseObject> queryRoute = new ParseQuery<ParseObject>(ParseConstants.CLASS_ROUTES);
        // Use the 'where' clause to find where the user ID is one of the recipients.
        queryRoute.whereEqualTo(ParseConstants.KEY_RECIPIENT_IDS, ParseUser.getCurrentUser().getObjectId());
        // Order results so that most recent Routes are at the top of the inbox.
        queryRoute.addDescendingOrder(ParseConstants.KEY_CREATED_AT);
        // Query is ready - run it on a background thread.
        queryRoute.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> routes, ParseException e) {

                // End refreshing once routes are retrieved.
                // done() is called from onResume() and the OnRefreshListener.
                // Check it is called from the the OnRefreshListener before ending it.
                if (mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }

                // If no exception returned
                if (e == null) {

                    // Store the returned List
                    mRoutes = routes;

                    // Create an adapter and set it as the list adapter.
                    // Create the adapter once and update its state on each refresh.
                    if (getListView().getAdapter() == null) {
                        // Use the custom RouteMessageAdapter to display the Route data in the inbox.
                        RouteMessageAdapter adapter = new RouteMessageAdapter(getListView().getContext(), mRoutes);

                        // Force a refresh of the list once data has changed.
                        adapter.notifyDataSetChanged();

                        // Call setListAdapter (from ListActivity class) for this activity.
                        setListAdapter(adapter);
                    } else {
                        // Adapter is not available - refill with the List of Routes.
                        ((RouteMessageAdapter) getListView().getAdapter()).refill(mRoutes);
                    }
                }
            }
        });
    }

    // Swipe to refresh listener.
    protected SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            // When list is swiped down to refresh, retrieve the latest List of Routes from Parse.
            retrieveRoutes();
        }
    };
}