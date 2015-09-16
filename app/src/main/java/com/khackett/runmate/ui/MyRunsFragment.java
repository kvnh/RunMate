package com.khackett.runmate.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
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
 * Created by KHackett on 03/09/15.
 */
public class MyRunsFragment extends ListFragment {

    protected SwipeRefreshLayout mSwipeRefreshLayout;

    // member variable to store the list of routes the user has accepted
    protected List<ParseObject> mAcceptedRoutes;

    private int MY_STATUS_CODE = 1111;

    // Default constructor for MyRunsFragment
    public MyRunsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // the 1st parameter is the layout id that is used for this fragment,
        // the 2nd is the container where the fragment will be displayed (this will be the ViewPager from main activity),
        // the 3rd parameter should be false whenever we add a fragment to an activity in code, which is what we are going to do
        // So this line of code uses an inflater object to create a new view using the layout we provide.
        // It then attaches that view to a parent, which in this case is the ViewPager object from main activity

        View rootView = inflater.inflate(R.layout.fragment_my_runs, container, false);

        // Set SwipeRefreshLayout component
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        // Set the onRefreshListener
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.swipeRefresh1,
                R.color.swipeRefresh2,
                R.color.swipeRefresh3,
                R.color.swipeRefresh4);

        // Return the View object - this is the view of the whole fragment
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Retrieve the accepted routes from the Parse backend
        retrieveAcceptedRoutes();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        // to tell whether it is an image or a video, we need to access the type of the message
        // create the message object which is set to the message at the current position
        ParseObject route = mAcceptedRoutes.get(position);

        JSONArray parseList = route.getJSONArray(ParseConstants.KEY_LATLNG_POINTS);
        JSONArray parseListBounds = route.getJSONArray(ParseConstants.KEY_LATLNG_BOUNDARY_POINTS);
        String objectId = route.getObjectId();
        String routeName = route.getString(ParseConstants.KEY_ROUTE_NAME);
        String creationType = route.getString(ParseConstants.KEY_ROUTE_CREATION_TYPE);

        // Start a map activity to display the route
        Intent intent = new Intent(getActivity(), MapsActivityTrackRun.class);
        intent.putExtra("parseLatLngList", parseList.toString());
        intent.putExtra("parseLatLngBoundsList", parseListBounds.toString());
        intent.putExtra("myRunsObjectId", objectId);
        intent.putExtra("myRunsRouteName", routeName);
        intent.putExtra("creationType", creationType);

        // Start the MapsActivityDisplayRoute activity
        startActivityForResult(intent, MY_STATUS_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == MY_STATUS_CODE) {
//            //Refresh the fragment here
//            retrieveAcceptedRoutes();
//        }
    }

    private void retrieveAcceptedRoutes() {
        // query the routes class/table in parse
        // get messages where the logged in user ID is in the list of the recipient ID's (we only want to retrieve the messages sent to us)
        // querying the message class is similar to how we have been querying users
        ParseQuery<ParseObject> queryRoute = new ParseQuery<ParseObject>(ParseConstants.CLASS_ROUTES);
        // use the 'where' clause to search through the messages to find where our user ID is one of the recipients
        queryRoute.whereEqualTo(ParseConstants.KEY_ACCEPTED_RECIPIENT_IDS, ParseUser.getCurrentUser().getObjectId());
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
                    mAcceptedRoutes = routes;

                    // adapt this data for the list view, showing the senders name

                    // create an array of strings to store the usernames and set the size equal to that of the list returned
                    String[] usernames = new String[mAcceptedRoutes.size()];
                    // enhanced for loop to go through the list of users and create an array of usernames
                    int i = 0;
                    for (ParseObject message : mAcceptedRoutes) {
                        // get the specific key
                        usernames[i] = message.getString(ParseConstants.KEY_SENDER_NAME);
                        i++;
                    }

                    // Create the adapter once and update its state on each refresh
                    if (getListView().getAdapter() == null) {
                        // the above adapter code is now replaced with the following line
                        RouteMessageAdapter adapter = new RouteMessageAdapter(getListView().getContext(), mAcceptedRoutes);

                        // Force a refresh of the list once data has changed
                        adapter.notifyDataSetChanged();

                        // need to call setListAdapter for this activity.  This method is specifically from the ListActivity class
                        setListAdapter(adapter);
                    } else {
                        // refill the adapter
                        // cast it to RouteMessageAdapter
                        ((RouteMessageAdapter) getListView().getAdapter()).refill(mAcceptedRoutes);
                    }
                }
            }
        });
    }

    protected SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            // When list is swiped down to refresh, retrieve the users runs from the Parse backend
            retrieveAcceptedRoutes();
        }
    };

}
