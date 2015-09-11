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

// need to create a custom list view adapter - very similar to the way it is done in fragments
// our adapter has 2 parts: we need to define a custom layout that will be used for each item in the list;
// then we need to create a custom class that adapts one message of our parse objects into the layout

/**
 * Custom adapter that uses the completed_run_item.xml layout file
 * Created by KHackett on 31/07/15.
 */
public class RunHistoryAdapter extends ArrayAdapter<ParseObject> {

    public static final String TAG = RunHistoryAdapter.class.getSimpleName();

    protected Context mContext;
    protected List<ParseObject> mCompletedRun;

    // pass the list of activities to the RouteMessageAdapter

    /**
     * @param context
     * @param completedRun - the list of
     */
    public RunHistoryAdapter(Context context, List<ParseObject> completedRun) {
        super(context, R.layout.message_item, completedRun);
        mContext = context;
        mCompletedRun = completedRun;
    }

    /**
     * Custom class that contains the data that is going to be displayed in the custom layout for each item.
     */
    private static class ViewHolder {
        // The name of the route
        TextView routeNameLabel;
        // The distance of the route
        TextView distanceLabel;
        // The time taken to complete the run
        TextView runTimeLabel;
        // The average seed throughout the run
        TextView averageSpeedLabel;
        // The time and date of the route
        TextView timeAndDateLabel;
    }

    // in fragments, the adapter called an appropriate method to get a fragment, then it adapter it and then put in the view
    // ...the same thing is happening here - this adapter (which is going to be attached to a list view) is going to call a
    // method called getView(), its going to create the view, inflate it into a layout and then attach it into the list view
    // override getView() to use a custom list adapter
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // we want to create a method that is efficient for the list view
        // the more efficient we are in this method, the better our list view will perform
        // - this affects things like scrolling or tapping on items
        // a common pattern that help with this is the ViewHolder PATTERN!!!
        final ViewHolder holder; // we need to create this ViewHolder class

        // when doing a custom list adapter, the convention is to create a private static class that we can reference - see below

        // this method is called in such a way that the views are recycled for the list view
        // the android system recycles the views if they already exist
        if (convertView == null) {
            // we want to inflate the view (convertView) from the layout file, using the context, and then return it to the list
            // Done by using the LayoutInflater - an Android object that takes XML layouts and turns them into views in code that we can use
            convertView = LayoutInflater.from(mContext).inflate(R.layout.run_history_item, null);

            // initialise holder as a new ViewHolder - and initialise the image view and text view inside it
            holder = new ViewHolder();
            // findViewById() is an activity method, but we can call it from the convert view
            holder.routeNameLabel = (TextView) convertView.findViewById(R.id.routeNameLabel);
            holder.distanceLabel = (TextView) convertView.findViewById(R.id.distanceLabel);
            holder.runTimeLabel = (TextView) convertView.findViewById(R.id.runTimeLabel);
            holder.timeAndDateLabel = (TextView) convertView.findViewById(R.id.timeAndDateLabel);
            convertView.setTag(holder);
        } else {
            // then it already exists and we can reuse the components - they are already there in memory - we just need to change the data
            // so instead of creating the holder from scratch, do the following. getTag() gets us the ViewHolder that was already created - this is part of the ViewHolder pattern
            holder = (ViewHolder) convertView.getTag();
        }

        // set the data in the view - picture icon and name
        // get the parse object that corresponds to position, because getView() is going to be called for each position in the list
        ParseObject completedRun = mCompletedRun.get(position);

        // Create an instance of SimpleDateFormat used for formatting
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        // Convert Date object into a String object to be used in the text view
        Date completedOn = completedRun.getCreatedAt();
        String completedOnString = dateFormat.format(completedOn);
        // Use newly created String Date in the Text View
        holder.timeAndDateLabel.setText(completedOnString);

//        int completedRunTime = completedRun.getInt(ParseConstants.KEY_RUN_TIME);
//        holder.runTimeLabel.setText(completedRunTime);

        double completedRunDistance = completedRun.getDouble(ParseConstants.KEY_COMPLETED_RUN_DISTANCE);
        holder.distanceLabel.setText(String.format("%.2f km", completedRunDistance / 1000));

        return convertView;
    }

    // method to refill the list with ParseObject data if it is not null
    public void refill(List<ParseObject> messages) {
        // clear the current data
        mCompletedRun.clear();
        // add all the new ones
        mCompletedRun.addAll(messages);
        //  need to call notifyDataSetChanged() on the adapter after changing its contents
        notifyDataSetChanged();
    }
}
