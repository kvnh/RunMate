package com.khackett.runmate.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.khackett.runmate.MapsActivityDisplayRoute;
import com.khackett.runmate.MapsActivityTrackRun;
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
 * Fragment to display the List of Routes a user has accepted
 */
public class MyRunsFragment extends ListFragment {

    // TAG to represent the MyRunsFragment class
    public static final String TAG = MyRunsFragment.class.getSimpleName();

    // Swipe to refresh member variable
    private SwipeRefreshLayout mSwipeRefreshLayout;

    // Member variable to store the list of Routes accepted by the user
    private List<ParseObject> mAcceptedRoutes;

    /**
     * Default constructor for MyRunsFragment
     */
    public MyRunsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 1st parameter: layout id used in this fragment.
        // 2nd parameter: container where the fragment will be displayed (the ViewPager from TabFragmentContainer).
        // 3rd parameter: false whenever a fragment is added to an activity.
        // Inflater object used to create a new View using the layout provided.
        // View is then attached to a parent - the ViewPager object from TabFragmentContainer.
        View rootView = inflater.inflate(R.layout.fragment_my_runs, container, false);

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

        // Retrieve the accepted routes from Parse
        retrieveAcceptedRoutes();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        // Create a ParseObject which is set to the selected Route message
        ParseObject route = mAcceptedRoutes.get(position);

        // Assign data from the ParseObject to Route variables.
        JSONArray parseList = route.getJSONArray(ParseConstants.KEY_LATLNG_POINTS);
        JSONArray parseListBounds = route.getJSONArray(ParseConstants.KEY_LATLNG_BOUNDARY_POINTS);
        String objectId = route.getObjectId();
        String routeName = route.getString(ParseConstants.KEY_ROUTE_NAME);
        String creationType = route.getString(ParseConstants.KEY_ROUTE_CREATION_TYPE);

        // Create an intent to display the route and add relevant item data.
        Intent intent = new Intent(getActivity(), MapsActivityTrackRun.class);
        intent.putExtra("parseLatLngList", parseList.toString());
        intent.putExtra("parseLatLngBoundsList", parseListBounds.toString());
        intent.putExtra("myRunsObjectId", objectId);
        intent.putExtra("myRunsRouteName", routeName);
        intent.putExtra("creationType", creationType);
        intent.putExtra("intentName", TAG);

        // Start MapsActivityDisplayRoute with the item data.
        startActivity(intent);
    }

    /**
     * Method to retrieve a List of accepted Route items from Parse
     */
    private void retrieveAcceptedRoutes() {

        // Set up a dialog progress indicator box
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle(R.string.my_runs_progress_dialog_title);
        progressDialog.setMessage(getActivity().getString(R.string.my_runs_progress_dialog_message));
        progressDialog.show();

        // Query the Routes table in Parse.
        // Get Routes where the logged in user ID is in the list of accepted recipient ID's.
        ParseQuery<ParseObject> queryRoute = new ParseQuery<ParseObject>(ParseConstants.CLASS_ROUTES);
        // Use the 'where' clause to find where the user ID is one of the recipients.
        queryRoute.whereEqualTo(ParseConstants.KEY_ACCEPTED_RECIPIENT_IDS, ParseUser.getCurrentUser().getObjectId());
        // Order results so that most forthcoming runs are at the top of the list.
        queryRoute.addAscendingOrder(ParseConstants.KEY_ROUTE_PROPOSED_TIME);
        // Query is ready - run it on a background thread.
        queryRoute.findInBackground(new FindCallback<ParseObject>() {
            // When the retrieval is done from the Parse query, the done() callback method is called
            @Override
            public void done(List<ParseObject> routes, ParseException e) {

                // Dismiss progress dialog once result returned from backend
                progressDialog.dismiss();

                // End refreshing once routes are retrieved.
                // done() is called from onResume() and the OnRefreshListener.
                // Check it is called from the the OnRefreshListener before ending it.
                if (mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }

                // If no exception returned
                if (e == null) {

                    // Store the returned List
                    mAcceptedRoutes = routes;

                    // Create an adapter and set it as the list adapter.
                    // Create the adapter once and update its state on each refresh.
                    if (getListView().getAdapter() == null) {
                        // Use the custom RouteMessageAdapter to display the Route data in My Runs.
                        RouteMessageAdapter adapter = new RouteMessageAdapter(getListView().getContext(), mAcceptedRoutes);

                        // Force a refresh of the list once data has changed.
                        adapter.notifyDataSetChanged();

                        // Call setListAdapter (from ListActivity class) for this activity.
                        setListAdapter(adapter);
                    } else {
                        // Adapter is not available - refill with the List of Routes.
                        ((RouteMessageAdapter) getListView().getAdapter()).refill(mAcceptedRoutes);
                    }
                }
            }
        });
    }

    // Swipe to refresh listener.
    protected SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            // When list is swiped down to refresh, retrieve the latest List of accepted Routes from Parse.
            retrieveAcceptedRoutes();
        }
    };

}