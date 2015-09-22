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
 * Custom adapter that uses the completed_run_item.xml layout file.
 * Adapter has two parts:
 * A custom layout that will be used for each item in the list;
 * A custom subclass, ViewHolder, that adapts each item of the ParseObject into the layout.
 * The common ViewHolder pattern is applied to enable an efficient ListView performance.
 * Created by KHackett on 31/07/15.
 */
public class RunHistoryAdapter extends ArrayAdapter<ParseObject> {

    // Simple class TAG for logcat output
    public static final String TAG = RunHistoryAdapter.class.getSimpleName();

    // Declare Context and CompletedRuns List variables
    protected Context mContext;
    protected List<ParseObject> mCompletedRuns;

    /**
     * Constructor to pass the list of activities to the RunHistoryAdapter
     *
     * @param context       - the context of the application
     * @param completedRuns - the list of completed runs
     */
    public RunHistoryAdapter(Context context, List<ParseObject> completedRuns) {
        super(context, R.layout.run_history_item, completedRuns);
        mContext = context;
        mCompletedRuns = completedRuns;
    }

    /**
     * ViewHolder class that contains the data to be displayed in the
     * custom layout for each RunHistory item.
     * Convention when creating a custom list adapter is to create a
     * private static class that can be referenced in getView().
     */
    private static class ViewHolder {
        // The name of the Route.
        TextView routeNameLabel;
        // The time and date of the run.
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
            // Use the LayoutInflater - Android object that takes XML layouts and turns them into views.
            convertView = LayoutInflater.from(mContext).inflate(R.layout.run_history_item, null);

            // Initialise a new ViewHolder - then initialise the data inside it.
            viewHolder = new ViewHolder();

            // findViewById() is an Activity method - can be called from the View.
            viewHolder.routeNameLabel = (TextView) convertView.findViewById(R.id.routeNameLabel);
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
        ParseObject completedRun = mCompletedRuns.get(position);

        // Set the name of the run.
        viewHolder.routeNameLabel.setText(completedRun.getString(ParseConstants.KEY_ROUTE_NAME));

        // Create an instance of SimpleDateFormat used for formatting.
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        // Convert Date object into a String object.
        Date completedOn = completedRun.getCreatedAt();
        String completedOnString = dateFormat.format(completedOn);
        // Use newly created String Date in the Text View.
        viewHolder.timeAndDateLabel.setText(completedOnString);

        // Return the View
        return convertView;
    }

    /**
     * Method to refill the list with ParseObject data if it is not null
     *
     * @param completedRuns
     */
    public void refill(List<ParseObject> completedRuns) {
        // Clear the current data.
        mCompletedRuns.clear();
        // Add all the new runs.
        mCompletedRuns.addAll(completedRuns);
        // Call notifyDataSetChanged() on the adapter after changing its contents.
        notifyDataSetChanged();
    }
}