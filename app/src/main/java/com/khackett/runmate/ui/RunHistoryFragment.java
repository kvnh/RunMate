package com.khackett.runmate.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.khackett.runmate.MapsActivityRunHistory;
import com.khackett.runmate.R;
import com.khackett.runmate.adapters.RunHistoryAdapter;
import com.khackett.runmate.utils.ParseConstants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Fragment to display the List of Routes in the users Run History
 */
public class RunHistoryFragment extends ListFragment {

    // TAG to represent the InboxRouteFragment class
    public static final String TAG = RunHistoryFragment.class.getSimpleName();

    // Swipe to refresh member variable
    private SwipeRefreshLayout mSwipeRefreshLayout;

    // Member variable to store the list of Routes completed by the user
    private List<ParseObject> mCompletedRuns;

    /**
     * Default constructor for RunHistoryFragment
     */
    public RunHistoryFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 1st parameter: layout id used in this fragment.
        // 2nd parameter: container where the fragment will be displayed (the ViewPager from TabFragmentContainer).
        // 3rd parameter: false whenever a fragment is added to an activity.
        // Inflater object used to create a new View using the layout provided.
        // View is then attached to a parent - the ViewPager object from TabFragmentContainer.
        View rootView = inflater.inflate(R.layout.fragment_run_history, container, false);

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

        // Retrieve the completed routes from Parse
        retrieveCompletedRuns();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        // Create a ParseObject which is set to the selected completed run.
        ParseObject completedRun = mCompletedRuns.get(position);

        // Assign data from the ParseObject to Route variables.
        JSONArray parseLatLngGPSList = completedRun.getJSONArray(ParseConstants.KEY_LATLNG_GPS_POINTS);
        JSONArray parseLatLngBounds = completedRun.getJSONArray(ParseConstants.KEY_LATLNG_BOUNDARY_POINTS);
        String objectId = completedRun.getObjectId();
        int myRunDistance = completedRun.getInt(ParseConstants.KEY_COMPLETED_RUN_DISTANCE);
        int myRunTimeMillis = completedRun.getInt(ParseConstants.KEY_RUN_TIME);
        String myRunName = completedRun.getString(ParseConstants.KEY_ROUTE_NAME);

        // Create an intent to display the route and add relevant run data.
        Intent intent = new Intent(getActivity(), MapsActivityRunHistory.class);
        intent.putExtra("myRunLatLngList", parseLatLngGPSList.toString());
        intent.putExtra("myRunLatLngBoundsList", parseLatLngBounds.toString());
        intent.putExtra("myRunHistoryObjectId", objectId);
        intent.putExtra("myRunDistance", myRunDistance);
        intent.putExtra("myRunTime", myRunTimeMillis);
        intent.putExtra("myRunName", myRunName);

        // Start MapsActivityDisplayRoute with the item data.
        startActivity(intent);
    }

    /**
     * Method to retrieve a List of completed runs from Parse
     */
    private void retrieveCompletedRuns() {

        // Query the CompletedRoutes table in Parse.
        // Get Routes where the logged in user ID is equal to the runner ID.
        ParseQuery<ParseObject> queryCompletedRun = new ParseQuery<ParseObject>(ParseConstants.CLASS_COMPLETED_RUNS);
        // Use 'where' clause to search through the runs to find where the user ID is equal to the runnerId.
        queryCompletedRun.whereEqualTo(ParseConstants.KEY_RUNNER_IDS, ParseUser.getCurrentUser().getObjectId());
        // Order results so that most recent runs are at the top of the list.
        queryCompletedRun.addDescendingOrder(ParseConstants.KEY_CREATED_AT);
        // Query is ready - run it on a background thread.
        queryCompletedRun.findInBackground(new FindCallback<ParseObject>() {
            // When retrieval from query is complete, the done() callback method is called.
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
                    mCompletedRuns = routes;

                    // Create an adapter and set it as the list adapter.
                    // Create the adapter once and update its state on each refresh.
                    if (getListView().getAdapter() == null) {
                        // Use the custom RunHistoryAdapter to display the Route data in Run History.
                        RunHistoryAdapter adapter = new RunHistoryAdapter(getListView().getContext(), mCompletedRuns);

                        // Force a refresh of the list once data has changed.
                        adapter.notifyDataSetChanged();

                        // Call setListAdapter (from ListActivity class) for this activity.
                        setListAdapter(adapter);
                    } else {
                        // Adapter is not available - refill with the List of Routes.
                        ((RunHistoryAdapter) getListView().getAdapter()).refill(mCompletedRuns);
                    }
                }
            }
        });
    }

    // Swipe to refresh listener.
    protected SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            // When list is swiped down to refresh, retrieve the latest List of completed runs from Parse.
            retrieveCompletedRuns();
        }
    };
}