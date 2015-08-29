package com.khackett.runmate.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.khackett.runmate.R;
import com.khackett.runmate.utils.MD5Util;
import com.khackett.runmate.utils.ParseConstants;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.List;

// Create a custom list view adapter - similar to the way it is done in fragments.
// The adapter has two parts: a custom layout that will be used for each item in the list;
// And a custom class that adapts one message of the parse object into the layout.

/**
 * Custom adapter that uses the message_item.xml layout file
 * Created by KHackett on 31/07/15.
 */
public class UserAdapter extends ArrayAdapter<ParseUser> {

    protected Context mContext;
    protected List<ParseUser> mParseUsers;

    // pass the list of activities to the RouteMessageAdapter

    // create a constructor
    public UserAdapter(Context context, List<ParseUser> parseUsers) {
        super(context, R.layout.message_item, parseUsers);
        mContext = context;
        mParseUsers = parseUsers;
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.user_item, null);

            // initialise holder as a new ViewHolder - and initialise the image view and text view inside it
            holder = new ViewHolder();
            // findViewById() is an activity method, but we can call it from the convert view
            holder.userImageView = (ImageView) convertView.findViewById(R.id.userImageView);
            holder.checkUserImageView = (ImageView) convertView.findViewById(R.id.checkFriendImageView);
            holder.nameLabel = (TextView) convertView.findViewById(R.id.nameLabel);

            convertView.setTag(holder);
        } else {
            // then it already exists and we can reuse the components - they are already there in memory - we just need to change the data
            // so instead of creating the holder from scratch, do the following. getTag() gets us the ViewHolder that was already created - this is part of the ViewHolder pattern
            holder = (ViewHolder) convertView.getTag();
        }

        // set the data in the view - picture icon and name
        // get the parse object that corresponds to position, because getView() is going to be called for each position in the list
        ParseUser user = mParseUsers.get(position);

        // Get the email address of the user
        String userEmail = user.getEmail().toLowerCase();
        // Email will be an empty String if the user didn't supply an email address
        if (userEmail.equals("")) {
            // If email address is empty, set the default avatar
            holder.userImageView.setImageResource(R.mipmap.avatar_empty);
        } else {
            // Set the Gravatar
            String hash = MD5Util.md5Hex(userEmail);
            String gravatarUrl = "http://www.gravatar.com/avatar/" + hash + "?s=204&d=404";
            Picasso.with(mContext)
                    // Load the URL
                    .load(gravatarUrl)
                            // if a 404 code is returned, use the placeholder image
                    .placeholder(R.mipmap.avatar_empty)
                            // Load into user image view
                    .into(holder.userImageView);
        }

        // Get the name of the user from Parse
        holder.nameLabel.setText(user.getUsername());

        // Get a reference to the GridView, the parent view of the individual item being tapped
        // When the user taps on a GridView item, the getView() method is called
        GridView gridView = (GridView) parent;
        // Check if the item being tapped on is checked or not
        if (gridView.isItemChecked(position)) {
            // Set to visible if it is
            holder.checkUserImageView.setVisibility(View.VISIBLE);
        } else {
            holder.checkUserImageView.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    // class that contains the data that we are adapting and that is
    // going to be displayed in the custom layout for each item
    private static class ViewHolder {
        // we have 3 pieces of data - a text view, an image view and a check for the image view
        ImageView userImageView;
        ImageView checkUserImageView;
        TextView nameLabel;

    }

    // method to refill the list with ParseObject data if it is not null
    public void refill(List<ParseUser> users) {
        // clear the current data
        mParseUsers.clear();
        // add all the new ones
        mParseUsers.addAll(users);
        //  need to call notifyDataSetChanged() on the adapter after changing its contents
        notifyDataSetChanged();
    }
}
