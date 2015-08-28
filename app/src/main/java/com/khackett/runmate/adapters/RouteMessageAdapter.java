package com.khackett.runmate.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.khackett.runmate.R;
import com.khackett.runmate.utils.ParseConstants;
import com.parse.ParseObject;

import java.util.Date;
import java.util.List;

// need to create a custom list view adapter - very similar to the way it is done in fragments
// our adapter has 2 parts: we need to define a custom layout that will be used for each item in the list;
// then we need to create a custom class that adapts one message of our parse objects into the layout

/**
 * Custom adapter that uses the message_item.xml layout file
 * Created by KHackett on 31/07/15.
 */
public class RouteMessageAdapter extends ArrayAdapter<ParseObject> {

    protected Context mContext;
    protected List<ParseObject> mRoutes;

    // pass the list of activities to the RouteMessageAdapter

    // create a constructor
    public RouteMessageAdapter(Context context, List<ParseObject> routes) {
        super(context, R.layout.message_item, routes);
        mContext = context;
        mRoutes = routes;
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
        ViewHolder holder; // we need to create this ViewHolder class

        // when doing a custom list adapter, the convention is to create a private static class that we can reference - see below

        // this method is called in such a way that the views are recycled for the list view
        // the android system recycles the views if they already exist
        if (convertView == null) {
            // we want to inflate the view (convertView) from the layout file, using the context, and then return it to the list
            // Done by using the LayoutInflater - an Android object that takes XML layouts and turns them into views in code that we can use
            convertView = LayoutInflater.from(mContext).inflate(R.layout.message_item, null);

            // initialise holder as a new ViewHolder - and initialise the image view and text view inside it
            holder = new ViewHolder();
            // findViewById() is an activity method, but we can call it from the convert view
            holder.iconImageView = (ImageView) convertView.findViewById(R.id.messageIcon);
            holder.nameLabel = (TextView) convertView.findViewById(R.id.senderLabel);
            holder.timeLabel = (TextView) convertView.findViewById(R.id.timeLabel);
            convertView.setTag(holder);
        } else {
            // then it already exists and we can reuse the components - they are already there in memory - we just need to change the data
            // so instead of creating the holder from scratch, do the following. getTag() gets us the ViewHolder that was already created - this is part of the ViewHolder pattern
            holder = (ViewHolder) convertView.getTag();
        }

        // set the data in the view - picture icon and name
        // get the parse object that corresponds to position, because getView() is going to be called for each position in the list
        ParseObject route = mRoutes.get(position);

        // Declare a new Java Date variable to contain the date the route was sent
        Date sentAt = route.getCreatedAt();
        // Convert Date object into a String object to be used in the text view
        long timeNow = new Date().getTime();
        String stringDate = DateUtils.getRelativeTimeSpanString(
                sentAt.getTime(),
                timeNow,
                DateUtils.SECOND_IN_MILLIS).toString();

        // Use newly created String Date in the Text View
        holder.timeLabel.setText(stringDate);

//        // change the icon based on the route type
//        if (route.getString(ParseConstants.KEY_FILE_TYPE).equals(ParseConstants.TYPE_IMAGE)) {
//            holder.userImageView.setImageResource(R.mipmap.ic_action_picture);
//        } else {
//            holder.userImageView.setImageResource(R.mipmap.ic_action_play_over_video);
//        }
        holder.iconImageView.setImageResource(R.mipmap.ic_action_picture);

        holder.nameLabel.setText(route.getString(ParseConstants.KEY_SENDER_NAME));

        return convertView;
    }

    // class that contains the data that is going to be displayed in the custom layout for each item
    private static class ViewHolder {
        // we have 2 pieces of data - a text view and an image view
        ImageView iconImageView;
        TextView nameLabel;
        TextView timeLabel;

    }

    // method to refill the list with ParseObject data if it is not null
    public void refill(List<ParseObject> messages){
        // clear the current data
        mRoutes.clear();
        // add all the new ones
        mRoutes.addAll(messages);
        //  need to call notifyDataSetChanged() on the adapter after changing its contents
        notifyDataSetChanged();
    }
}
