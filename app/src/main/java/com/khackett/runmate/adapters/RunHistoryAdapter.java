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
 * Custom list view adapter that uses the completed_run_item.xml layout file.
 * Adapter has two parts:
 * A custom layout that will be used for each item in the list;
 * A custom class that adapts each item of the parse objects into the layout.
 * Created by KHackett on 31/07/15.
 */
public class RunHistoryAdapter extends ArrayAdapter<ParseObject> {

    public static final String TAG = RunHistoryAdapter.class.getSimpleName();

    protected Context mContext;
    protected List<ParseObject> mCompletedRun;

    /**
     * Constructor to pass the list of activities to the RunHistoryAdapter
     *
     * @param context
     * @param completedRun - the list of completed runs
     */
    public RunHistoryAdapter(Context context, List<ParseObject> completedRun) {
        super(context, R.layout.run_history_item, completedRun);
        mContext = context;
        mCompletedRun = completedRun;
    }

    /**
     * Custom class that contains the data to be displayed in the custom layout for each run_history_item.
     * Convention when creating a custom list adapter is to create a private static class that can be referenced in getView().
     */
    private static class ViewHolder {
        // The name of the route.
        TextView routeNameLabel;
        // The time and date of the run.
        TextView timeAndDateLabel;
    }

    // In fragments, the adapter calls an appropriate method to get a fragment, then it adapts it and put it in the view.
    // Same thing happening here - this adapter (which is going to be attached to a list view) calls getView()
    // It creates the view, inflates it into a layout and then attach it into the list view.
    // Override getView() to use a custom list adapter.
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Create a method for the list view - the more efficient, the better the list view will perform.
        // - this affects things like scrolling or tapping on items.
        // Use the ViewHolder pattern to aid with this.

        // Create a ViewHolder object.
        final ViewHolder holder;

        // When this method is called, the views are recycled for the list view.
        // The android system recycles the views as if they already exist.
        if (convertView == null) {
            // Inflate the view (convertView) from the layout file, using the context, and then return it to the list.
            // Done by using the LayoutInflater - an Android object that takes XML layouts and turns them into views in code that can be used.
            convertView = LayoutInflater.from(mContext).inflate(R.layout.run_history_item, null);

            // Initialise the ViewHolder - and initialise the necessary items inside it.
            holder = new ViewHolder();
            // findViewById() is an activity method, but can be called from the convertView object.
            holder.routeNameLabel = (TextView) convertView.findViewById(R.id.routeNameLabel);
            holder.timeAndDateLabel = (TextView) convertView.findViewById(R.id.timeAndDateLabel);
            convertView.setTag(holder);
        } else {
            // Otherwise it already exists and components can be reused.
            // Components are already in memory - just need to change the values.
            // Instead of creating the holder from scratch - getTag() gets the ViewHolder
            // that was already created - part of the ViewHolder pattern in action.
            holder = (ViewHolder) convertView.getTag();
        }

        // Set the data in the view.
        // Get the parse object that corresponds to the selected position in the list.
        // getView() will be called for each position in the list.
        ParseObject completedRun = mCompletedRun.get(position);

        // Set the name of the run from the completed run.
        holder.routeNameLabel.setText(completedRun.getString(ParseConstants.KEY_ROUTE_NAME));

        // Create an instance of SimpleDateFormat used for formatting.
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        // Convert Date object into a String object to be used in the text view.
        Date completedOn = completedRun.getCreatedAt();
        String completedOnString = dateFormat.format(completedOn);
        // Use newly created String Date in the Text View.
        holder.timeAndDateLabel.setText(completedOnString);

        return convertView;
    }

    /**
     * Method to refill the list with ParseObject data if it is not null
     *
     * @param runs
     */
    public void refill(List<ParseObject> runs) {
        // Clear the current data.
        mCompletedRun.clear();
        // Add all the new runs.
        mCompletedRun.addAll(runs);
        // Call notifyDataSetChanged() on the adapter after changing its contents.
        notifyDataSetChanged();
    }
}