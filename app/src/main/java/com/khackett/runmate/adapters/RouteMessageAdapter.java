package com.khackett.runmate.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.khackett.runmate.R;
import com.khackett.runmate.utils.ParseConstants;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Custom adapter that uses the message_item.xml layout file.
 * Adapter has two parts:
 * A custom layout that will be used for each item in the list;
 * A custom subclass, ViewHolder, that adapts each item of the ParseObject into the layout.
 * The common ViewHolder pattern is applied to enable an efficient ListView performance.
 * Created by KHackett on 31/07/15.
 */
public class RouteMessageAdapter extends ArrayAdapter<ParseObject> {

    // Simple class TAG for logcat output
    public static final String TAG = RouteMessageAdapter.class.getSimpleName();

    // Declare Context and Route List variables
    private Context mContext;
    private List<ParseObject> mRoutes;

    /**
     * Constructor to pass the list of Routes to the RouteMessageAdapter
     *
     * @param context - the context of the application
     * @param routes  - the list of Routes
     */
    public RouteMessageAdapter(Context context, List<ParseObject> routes) {
        super(context, R.layout.message_item, routes);
        mContext = context;
        mRoutes = routes;
    }

    /**
     * ViewHolder class that contains the data to be displayed in the
     * custom layout for each Route item.
     * Convention when creating a custom list adapter is to create a
     * private static class that can be referenced in getView().
     */
    private static class ViewHolder {
        // The image to be displayed
        ImageView profilePicView;
        // The senders name
        TextView senderNameLabel;
        // The name of the route
        TextView routeNameLabel;
        // The time the message was sent
        TextView timeLabel;
        // The distance of the route
        TextView distanceLabel;
        // The time and date of the route
        TextView timeAndDateLabel;
    }

    /**
     * Method to create the View, inflate it into the layout, and attach it onto the ListView.
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Create an instance of the ViewHolder class
        final ViewHolder viewHolder;

        // Check if the view is null
        if (convertView == null) {
            // If so, inflate the View from the layout file, using the context,
            // and then return it to the list.
            // Use the LayoutInflater - Android object that takes XML layouts and turns them into views
            convertView = LayoutInflater.from(mContext).inflate(R.layout.message_item, null);

            // Initialise a new ViewHolder - then initialise the data inside it.
            viewHolder = new ViewHolder();

            // findViewById() is an Activity method - can be called from the View.
            viewHolder.profilePicView = (ImageView) convertView.findViewById(R.id.profilePic);
            viewHolder.senderNameLabel = (TextView) convertView.findViewById(R.id.senderLabel);
            viewHolder.routeNameLabel = (TextView) convertView.findViewById(R.id.routeNameLabel);
            viewHolder.timeLabel = (TextView) convertView.findViewById(R.id.timeLabel);
            viewHolder.distanceLabel = (TextView) convertView.findViewById(R.id.distanceLabel);
            viewHolder.timeAndDateLabel = (TextView) convertView.findViewById(R.id.timeAndDateLabel);
            convertView.setTag(viewHolder);
        } else {
            // View already exists - reuse the components already in memory - just need to change the data.
            // Instead of creating the holder from scratch, call getTag() to get the ViewHolder that is already created.
            // Views are recycled for the ListView - ViewHolder pattern in action.
            // Android system recycles the views if they already exist.
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Get the ParseObject that corresponds to the List position.
        // getView() is going to be called for each position in the List.
        ParseObject route = mRoutes.get(position);

        // Declare a new Date variable to contain the date the route was sent.
        Date sentAt = route.getCreatedAt();
        // Convert Date object into a String object.
        long timeNow = new Date().getTime();
        String stringDate = DateUtils.getRelativeTimeSpanString(
                sentAt.getTime(),
                timeNow,
                DateUtils.SECOND_IN_MILLIS).toString();
        // Use newly created String Date in the TextView.
        viewHolder.timeLabel.setText(stringDate);

        // Get the objectId of the Route sender
        String senderId = route.getString(ParseConstants.KEY_SENDER_IDS);
        // Query Parse to get the matching user in the User table.
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("objectId", senderId);
        query.getFirstInBackground(new GetCallback<ParseUser>() {
            public void done(ParseUser user, ParseException e) {
                if (e == null) {
                    // If no exception
                    ParseFile image = (ParseFile) user.getParseFile("profilePic");
                    if (image == null) {
                        // Assign the empty avatar image in the ViewHolder
                        viewHolder.profilePicView.setImageResource(R.mipmap.avatar_empty);
                    } else {
                        // Assign the users profile image in the ViewHolder
                        Picasso.with(mContext)
                                // Load the URL
                                .load(image.getUrl())
                                        // if a 404 code is returned, use the placeholder image
                                .placeholder(R.mipmap.avatar_empty)
                                .resize(180, 180)
                                        // Load into user image view
                                .into(viewHolder.profilePicView);
                    }

                } else {
                    Log.d(TAG, "Error returned from the Parse backend");
                }
            }
        });

        // Set the name of the sender
        viewHolder.senderNameLabel.setText(route.getString(ParseConstants.KEY_SENDER_NAME));
        // Set the name of the Route
        viewHolder.routeNameLabel.setText(route.getString(ParseConstants.KEY_ROUTE_NAME));

        // Create an instance of SimpleDateFormat used for formatting
        // the String representation of date (day/month/year Hours:Minutes)
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date proposedTimeParse = route.getDate(ParseConstants.KEY_ROUTE_PROPOSED_TIME);
        String proposedTimeString = dateFormat.format(proposedTimeParse);
        viewHolder.timeAndDateLabel.setText(proposedTimeString);

        // Set the distance of the Route
        double routeDistance = route.getDouble(ParseConstants.KEY_ROUTE_DISTANCE);
        viewHolder.distanceLabel.setText(String.format("%.2f km", routeDistance / 1000));

        // Return the View
        return convertView;
    }

    /**
     * Method to refill the list with ParseObject data if it is not null
     *
     * @param routes - the Route items in Parse
     */
    public void refill(List<ParseObject> routes) {
        // Clear the current data
        mRoutes.clear();
        // Add all the new ones
        mRoutes.addAll(routes);
        //  Call notifyDataSetChanged() on the adapter after changing its contents
        notifyDataSetChanged();
    }
}