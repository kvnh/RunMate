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

    // private GoogleMap mMap;

    protected SwipeRefreshLayout mSwipeRefreshLayout;

    // member variable to store the list of routes received by the user
    protected List<ParseObject> mRoutes;

    private int MY_STATUS_CODE = 1111;

    // Default constructor for InboxRouteFragment
    public InboxRouteFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // the 1st parameter is the layout id that is used for this fragment,
        // the 2nd is the container where the fragment will be displayed (this will be the ViewPager from main activity),
        // the 3rd parameter should be false whenever we add a fragment to an activity in code, which is what we are going to do
        // So this line of code uses an inflater object to create a new view using the layout we provide.
        // It then attaches that view to a parent, which in this case is the ViewPager object from main activity
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

        // note that this method returns a view - this is the view of the whole fragment
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Retrieve the routes from the Parse backend
        retrieveRoutes();

        // set the progress bar here
        // getActivity().setProgressBarIndeterminateVisibility(true);

//        // query the message class/table in parse
//        // get messages where the logged in user ID is in the list of the recipient ID's (we only want to retrieve the messages sent to us)
//        // querying the message class is similar to how we have been querying users
//        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseConstants.CLASS_MESSAGES);
//        // use the 'where' clause to search through the messages to find where our user ID is one of the recipients
//        query.whereEqualTo(ParseConstants.KEY_RECIPIENT_IDS, ParseUser.getCurrentUser().getObjectId());
//        // order results so that most recent message are at the top of the inbox
//        query.addDescendingOrder(ParseConstants.KEY_CREATED_AT);
//        // query is ready - run it
//        query.findInBackground(new FindCallback<ParseObject>() {
//            @Override
//            public void done(List<ParseObject> messages, ParseException e) {
//                // dismiss the progress indicator here
//                // getActivity().setProgressBarIndeterminateVisibility(false);
//
//                // the list being returned is a list of messages
//                if (e == null) {
//                    // successful - messages found.  They are stored as a list in messages
//                    mParseUsers = messages;
//
//                    // adapt this data for the list view, showing the senders name
//
//                    // create an array of strings to store the usernames and set the size equal to that of the list returned
//                    String[] usernames = new String[mParseUsers.size()];
//                    // enhanced for loop to go through the list of users and create an array of usernames
//                    int i = 0;
//                    for (ParseObject message : mParseUsers) {
//                        // get the specific key
//                        usernames[i] = message.getString(ParseConstants.KEY_SENDER_NAME);
//                        i++;
//                    }
//
////                    // create an array adapter and set it as the adapter for this activity
////                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
////                            // for the first parameter here, need to get the context
////                            getListView().getContext(),
////                            // no need to have check boxes in this list, so change it to simple_list_item_1 or whatever works well
////                            android.R.layout.simple_list_item_1,
////                            usernames);
//
//                    // the above adapter code is now replaced with the following line
//                    RouteMessageAdapter adapter = new RouteMessageAdapter(getListView().getContext(), mParseUsers);
//
//                    // need to call setListAdapter for this activity.  This method is specifically from the ListActivity class
//                    setListAdapter(adapter);
//                }
//
//            }
//        });

    }


//    // add the code that takes us to ViewImageActivity - we will need the onListItemClick() method
//    @Override
//    public void onListItemClick(ListView l, View v, int position, long id) {
//        super.onListItemClick(l, v, position, id);
//        // to tell whether it is an image or a video, we need to access the type of the message
//        // create the message object which is set to the message at the current position
//        ParseObject message = mParseUsers.get(position);
//        String messageType = message.getString(ParseConstants.KEY_FILE_TYPE);
//
//        // for both image and videos, we can view them directly from the backend on parse by getting their URL
//        // When we upload the files to parse, unique URL's are created that we can access through the parse file object
//        // So we need to get the parse file for this message
//        ParseFile file = message.getParseFile(ParseConstants.KEY_FILE);
//        // ParseFile has a method getU
//
//        // set the data for the intent using the setData() method - this requires a URI
//        // (URI's and URL's can often be used interchangeably)
//        Uri fileUri = Uri.parse(file.getUrl());
//
//        // now check the message type
//        if (messageType.equals(ParseConstants.TYPE_IMAGE)) {
//            // view the image
//            // create an intent and pass in the context - from a fragment we can use getActivity() as another way to get the context
//            Intent intent = new Intent(getActivity(), ViewImageActivity.class);
//            intent.setData(fileUri);
//            startActivity(intent);
//        } else if (messageType.equals(ParseConstants.TYPE_VIDEO)) {
//            // view the video
//            Intent intent = new Intent(Intent.ACTION_VIEW, fileUri);
//            // set data and type to allow us to specify the file type for the URI we use
//            intent.setDataAndType(fileUri, "video/*");
//            startActivity(intent);
//        } else {
//            // start a map activity to display the route
//            Intent intent = new Intent();
//            startActivity(intent);
//        }
//    }


    // add the code that takes us to ViewImageActivity - we will need the onListItemClick() method
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        // to tell whether it is an image or a video, we need to access the type of the message
        // create the message object which is set to the message at the current position
        ParseObject route = mRoutes.get(position);

        // String messageType = message.getString(ParseConstants.KEY_FILE_TYPE);

        // for both image and videos, we can view them directly from the backend on parse by getting their URL
        // When we upload the files to parse, unique URL's are created that we can access through the parse file object
        // So we need to get the parse file for this message
        // ParseFile file = message.getParseFile(ParseConstants.KEY_FILE);
        // ParseFile has a method getU

        // set the data for the intent using the setData() method - this requires a URI
        // (URI's and URL's can often be used interchangeably)
        // Uri fileUri = Uri.parse(file.getUrl());

        JSONArray parseList = route.getJSONArray("latLngPoints");
        JSONArray parseListBounds = route.getJSONArray("latLngBoundaryPoints");
        String objectId = route.getObjectId();
        // JSONArray ids = route.getJSONArray(ParseConstants.KEY_RECIPIENT_IDS);

        // start a map activity to display the route
        Intent intent = new Intent(getActivity(), MapsActivityDisplayRoute.class);
        intent.putExtra("parseLatLngList", parseList.toString());
        intent.putExtra("parseLatLngBoundsList", parseListBounds.toString());
        intent.putExtra("myObjectId", objectId);

        // Start the MapsActivityDisplayRoute activity
        startActivityForResult(intent, MY_STATUS_CODE);
        // startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_STATUS_CODE) {
            //Refresh the fragment here
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

                    // create an array of strings to store the usernames and set the size equal to that of the list returned
                    String[] usernames = new String[mRoutes.size()];
                    // enhanced for loop to go through the list of users and create an array of usernames
                    int i = 0;
                    for (ParseObject message : mRoutes) {
                        // get the specific key
                        usernames[i] = message.getString(ParseConstants.KEY_SENDER_NAME);
                        i++;
                    }

                    // Create the adapter once and update its state on each refresh
                    if (getListView().getAdapter() == null) {
                        // the above adapter code is now replaced with the following line
                        RouteMessageAdapter adapter = new RouteMessageAdapter(getListView().getContext(), mRoutes);

                        // Force a refresh of the list once data has changed
                        adapter.notifyDataSetChanged();

                        // need to call setListAdapter for this activity.  This method is specifically from the ListActivity class
                        setListAdapter(adapter);
                    } else {
                        // refill the adapter
                        // cast it to RouteMessageAdapter
                        ((RouteMessageAdapter) getListView().getAdapter()).refill(mRoutes);
                    }
                }
            }
        });
    }

    protected SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            // when list is swiped down to refresh, retrieve the routes from the Parse backend
            retrieveRoutes();
        }
    };
}
