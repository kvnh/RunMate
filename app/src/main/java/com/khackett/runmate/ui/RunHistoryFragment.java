package com.khackett.runmate.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
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

public class RunHistoryFragment extends ListFragment {


    protected SwipeRefreshLayout mSwipeRefreshLayout;

    // member variable to store the list of routes received by the user
    protected List<ParseObject> mCompletedRuns;

    private int MY_STATUS_CODE = 1111;

    // Default constructor for RunHistoryFragment
    public RunHistoryFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 1st parameter is the layout id used for this fragment.
        // 2nd parameter is the container where the fragment will be displayed (this will be the ViewPager from MainActivity)
        // 3rd parameter should be false whenever a fragment is added to an activity in code.
        // Inflater object used to create a new view using the layout provided.
        // View then attached to the parent - the ViewPager object from MainActivity.
        View rootView = inflater.inflate(R.layout.fragment_run_history, container, false);

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
        retrieveCompletedRuns();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        // Create a ParseObject which is set to the completed run in the current list position.
        ParseObject completedRun = mCompletedRuns.get(position);

        JSONArray parseLatLngGPSList = completedRun.getJSONArray(ParseConstants.KEY_LATLNG_GPS_POINTS);
        JSONArray parseListBounds = completedRun.getJSONArray("latLngBoundaryPoints");
        String objectId = completedRun.getObjectId();
        double myRunDistance = completedRun.getDouble(ParseConstants.KEY_COMPLETED_RUN_DISTANCE);

        // Create an intent to display the route.
        Intent intent = new Intent(getActivity(), MapsActivityRunHistory.class);
        intent.putExtra("parseLatLngList", parseLatLngGPSList.toString());
        intent.putExtra("parseLatLngBoundsList", parseListBounds.toString());
        intent.putExtra("myRunHistoryObjectId", objectId);
        intent.putExtra("myRunDistance", myRunDistance);

        // Start the MapsActivityRunHistory activity.
        startActivityForResult(intent, MY_STATUS_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_STATUS_CODE) {
            // Refresh the fragment.
            retrieveCompletedRuns();
        }
    }

    private void retrieveCompletedRuns() {
        // Query the CompletedRoutes table in backend.
        // Get runs where the logged in user ID is equal to the runnerId.
        ParseQuery<ParseObject> queryCompletedRun = new ParseQuery<ParseObject>(ParseConstants.CLASS_COMPLETED_RUNS);
        // Use 'where' clause to search through the runs to find where the user ID is equal to the runnerId.
        queryCompletedRun.whereEqualTo(ParseConstants.KEY_RUNNER_IDS, ParseUser.getCurrentUser().getObjectId());
        // Order results so that most recent runs are at the top of the inbox.
        queryCompletedRun.addDescendingOrder(ParseConstants.KEY_CREATED_AT);
        // Query is ready - run it.
        queryCompletedRun.findInBackground(new FindCallback<ParseObject>() {
            // When retrieval from query is complete, the done() callback method is called.
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
                    mCompletedRuns = routes;

                    // adapt this data for the list view, showing the senders name

                    // Create an array of strings to store usernames.
                    // Set the size equal to that of the list returned.
                    String[] usernames = new String[mCompletedRuns.size()];
                    // Enhanced for loop to go through the list of users and create an array of usernames
                    int i = 0;
                    for (ParseObject message : mCompletedRuns) {
                        // Get the specific key
                        usernames[i] = message.getString(ParseConstants.KEY_SENDER_NAME);
                        i++;
                    }

                    // Create the adapter once and update its state on each refresh.
                    if (getListView().getAdapter() == null) {
                        // the above adapter code is now replaced with the following line
                        RunHistoryAdapter adapter = new RunHistoryAdapter(getListView().getContext(), mCompletedRuns);

                        // Force a refresh of the list once data has changed.
                        adapter.notifyDataSetChanged();

                        // Call setListAdapter (from ListActivity class) for this activity.
                        setListAdapter(adapter);
                    } else {
                        // Refill the adapter - Cast it to RunHistoryAdapter.
                        ((RunHistoryAdapter) getListView().getAdapter()).refill(mCompletedRuns);
                    }
                }
            }
        });
    }

    protected SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            // When list is swiped down to refresh, retrieve the latest routes from the backend.
            retrieveCompletedRuns();
        }
    };
}