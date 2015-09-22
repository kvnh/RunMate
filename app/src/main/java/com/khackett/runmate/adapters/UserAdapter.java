package com.khackett.runmate.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.khackett.runmate.R;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Custom adapter that uses the message_item.xml layout file.
 * Adapter has two parts:
 * A custom layout that will be used for each item in the list;
 * A custom subclass, ViewHolder, that adapts each item of the ParseObject into the layout.
 * The common ViewHolder pattern is applied to enable an efficient ListView performance.
 * Created by KHackett on 31/07/15.
 */
public class UserAdapter extends ArrayAdapter<ParseUser> {

    // Simple class TAG for logcat output
    public static final String TAG = UserAdapter.class.getSimpleName();

    // Declare Context and ParseUsers List variables
    protected Context mContext;
    protected List<ParseUser> mParseUsers;

    /**
     * Constructor to pass the list of activities to the UserAdapter
     *
     * @param context    - the context of the application
     * @param parseUsers - the list of completed runs
     */
    public UserAdapter(Context context, List<ParseUser> parseUsers) {
        super(context, R.layout.message_item, parseUsers);
        mContext = context;
        mParseUsers = parseUsers;
    }

    /**
     * ViewHolder class that contains the data to be displayed in the
     * custom layout for each ParseUser item.
     * Convention when creating a custom list adapter is to create a
     * private static class that can be referenced in getView().
     */
    private static class ViewHolder {
        // The users image
        ImageView userImageView;
        // The image when a user is checked
        ImageView checkUserImageView;
        // The name of the user
        TextView nameLabel;
    }

    /**
     * * Method to create the View, inflate it into the layout, and attach it onto the ListView.
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.user_item, null);

            // Initialise a new ViewHolder - then initialise the data inside it.
            viewHolder = new ViewHolder();

            // findViewById() is an Activity method - can be called from the View.
            viewHolder.userImageView = (ImageView) convertView.findViewById(R.id.userImageView);
            viewHolder.checkUserImageView = (ImageView) convertView.findViewById(R.id.checkFriendImageView);
            viewHolder.nameLabel = (TextView) convertView.findViewById(R.id.nameLabel);
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
        ParseUser user = mParseUsers.get(position);

        // Set the users image picture.
        ParseFile image = (ParseFile) user.getParseFile("profilePic");
        // image will be null if the user didn't supply one.
        if (image == null) {
            // If image file is empty, set the default avatar.
            Log.d(TAG, "No profile picture for: " + user.getUsername());
            viewHolder.userImageView.setImageResource(R.mipmap.avatar_empty);
        } else {
            // Add image using Picasso
            Picasso.with(mContext)
                    // Load the URL
                    .load(image.getUrl())
                            // if a 404 code is returned, use the placeholder image
                    .placeholder(R.mipmap.avatar_empty)
                    .resize(250, 250)
                            // Load into user image view
                    .into(viewHolder.userImageView);
        }

        // Set the name of the user.
        viewHolder.nameLabel.setText(user.getUsername());

        // Get a reference to the GridView, the parent view of the individual item being tapped.
        // When a user taps on a GridView item, getView() method is called.
        GridView gridView = (GridView) parent;
        // Check if the item being tapped on is checked or not.
        if (gridView.isItemChecked(position)) {
            // Set to visible if it is.
            viewHolder.checkUserImageView.setVisibility(View.VISIBLE);
        } else {
            // Otherwise set to invisible.
            viewHolder.checkUserImageView.setVisibility(View.INVISIBLE);
        }

        // Return the View
        return convertView;
    }

    /**
     * * Method to refill the list with ParseUser data if it is not null
     *
     * @param users
     */
    public void refill(List<ParseUser> users) {
        // Clear the current data
        mParseUsers.clear();
        // Add all the new users
        mParseUsers.addAll(users);
        // Call notifyDataSetChanged() on the adapter after changing its contents.
        notifyDataSetChanged();
    }
}