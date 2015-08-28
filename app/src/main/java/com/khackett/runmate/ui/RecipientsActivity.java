package com.khackett.runmate.ui;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.khackett.runmate.R;
import com.khackett.runmate.utils.FileHelper;
import com.khackett.runmate.utils.ParseConstants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;


public class RecipientsActivity extends ListActivity {

    public static final String TAG = RecipientsActivity.class.getSimpleName();

    // set up a reference to the current user
    protected ParseUser mCurrentUser;
    // set up a member variable to store a list of friends for the current user returned from the parse user query
    protected List<ParseUser> mFriends;
    // set up a ParseRelation member to hold ParseUsers
    protected ParseRelation<ParseUser> mFriendsRelation;

    // create a menu item member variable so that it can be referenced below (it is a send button to be set on and off depending on if a user is selected)
    // set this variable in the onCreateOptionsMenu
    protected MenuItem mSendMenuItem;

    // member variable to represent the path that was passed into this activity via the intent that started it
    protected Uri mMediaUri;

    // member variable to represent the file type of the file to be sent to a recipient
    protected String mFileType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        // setContentView(R.layout.activity_recipients);

        // line to ensure the action bar displays in the layout
        getActionBar().setDisplayHomeAsUpEnabled(true);

        // the list view keeps track of items that are selected (this is the check property on each item)
        // loop through the list to see who is checked - do this when we are ready to send

        // get the default list view associated with this activity
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);  // we can now check and uncheck multiple friends

        // get the path/Uri value that was passed into this activity via the intent that started it
        // getIntent() gets the intent; getData() gets the URI
        mMediaUri = getIntent().getData();

        // get the file type of the file passed in with the intent
        mFileType = getIntent().getExtras().getString(ParseConstants.KEY_FILE_TYPE);
    }

    // get a list of all your friends - this code is copied from the onResume() method in the FriendsFragment with some additions
    @Override
    public void onResume() {
        super.onResume();

        // get the current user using the getCurrentUser() method
        mCurrentUser = ParseUser.getCurrentUser();
        // for the relation, from this user we want to call a method called getRelation()
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

        // start the progress indicator before we run our query
        // use the getActivity() to get a reference to the activity in which the fragment is running (as setProgressBarIndeterminateVisibility() is an Activity method)
        // note: Window provided Progress Bars are now deprecated with Toolbar.
        // see: http://stackoverflow.com/questions/27788195/setprogressbarindeterminatevisibilitytrue-not-working
        // getActivity().setProgressBarIndeterminateVisibility(true);

        // the first thing we need is a list of the users friends...
        // we have the friend relation, but this doesn't give us a list of users to work with
        // the list itself is still on the back end, we need to use the ParseRelation to retrieve it
        // use the build in query to retrieve it - this gets us the query associated with this ParseRelation
        ParseQuery<ParseUser> query = mFriendsRelation.getQuery();

        // sort the list by username before calling it
        query.addAscendingOrder(ParseConstants.KEY_USERNAME);

        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {

                // set a progress indicator in the progress bar

                // include an if statement to check the exception
                if (e == null) {

                    // set the mFriends variable based on the list of friends that is returned
                    mFriends = friends;

                    // now we need to use mFriends as the data source for the list view in our fragments
                    // we need to create an adapter and set it as the list adapter, just like we do for lost activities
                    // this is very similar to what we are ding for all users in the EditFriends activity, so copy and paste that code

                    // create an array of strings to store the usernames and set the size equal to that of the list returned
                    String[] usernames = new String[mFriends.size()];
                    // enhanced for loop to go through the list of parse users and create an array of usernames
                    int i = 0;
                    for (ParseUser user : mFriends) {
                        usernames[i] = user.getUsername();
                        i++;
                    }
                    // create an array adapter and set it as the adapter for this activity
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            // for the first parameter here, need to get the context of a fragment through the list view itself
                            // the list view knows which context it is in because of its layout in the fragment and in the activity that contains the fragment, so use...
                            getListView().getContext(),
                            android.R.layout.simple_list_item_checked,
                            usernames);
                    // need to call setListAdapter for this activity.  This method is specifically from the ListActivity class
                    setListAdapter(adapter);
                } else {
                    // display a message to the user (copied from EditFriendsActivity)
                    // there was an error - log the message
                    Log.e(TAG, e.getMessage());
                    // display an alert to the user
                    // if there is a parse exception then...
                    AlertDialog.Builder builder = new AlertDialog.Builder(RecipientsActivity.this);
                    // set the message from the exception
                    builder.setMessage(e.getMessage())
                            .setTitle(R.string.error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recipients, menu);

        // once the menu is inflated, we can can get a menu item object using the getItem() method of the menu that is passed in
        // use the int parameter to specify its position in the menu - since we only have 1 item, it will be at position 0
        mSendMenuItem = menu.getItem(0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_send) {
            // create a parse object for our message

            ParseObject message = createMessage();
            // the message variable will be null if something goes wrong
            // so we only want to call the send() method if it is not null
            if (message == null) {
                // display error message
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.error_selecting_recipient_message)
                        .setTitle(R.string.error_selecting_recipient_title)
                        .setPositiveButton(android.R.string.ok, null);
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                // create a send message that will accept the message as a parameter
                send(message);

                // In either case, we want to send the user back to the main activity right after the message is sent.
                // Use the activity method finish() to close the activity - this finishes the current activity and the
                // the previous activity (main activity) will be displayed in the message once again
                finish();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // change the visibility of the send button (set in menu_recipients.xml) whenever a a friend is selected
    // we create the menu in onCreateOptionsMenu
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        // check the number of items that are checked on the list
        if (l.getCheckedItemCount() > 0) {
            // set the menuItem to visible if an item is clicked
            mSendMenuItem.setVisible(true);
        } else {
            // otherwise, if it is 0, then hide the menu item
            mSendMenuItem.setVisible(false);
        }

        // add a listener for the send button
        // menu items fire the onOptionsItemSelected() method (such as the home button)

    }

    protected ParseObject createMessage() {
        // create a new parse object called message
        // (we can create a whole new class of parse objects in the back end by simply using a new name)
        ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGES);
        // now that we have an object, we can start adding data, using the key-value pairs...
        // first, get a String representation of the ID
        message.put(ParseConstants.KEY_SENDER_IDS, ParseUser.getCurrentUser().getObjectId());
        // put the senders name
        message.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
        // set the recipient ID's
        // get the selected friends from the list through the helper method getRecipientIds()
        message.put(ParseConstants.KEY_RECIPIENT_IDS, getRecipientIds());
        // get the file type of the file that was passed in
        message.put(ParseConstants.KEY_FILE_TYPE, mFileType);

        // we also need a parse file for the message - used for files which are too large for regular parse objects (such as images, videos, etc)
        // need to convert the file into a byte array - then create a ParseFile from the array
        // 3rd party library from tree house used to get the byte array from the file
        byte[] fileBytes = FileHelper.getByteArrayFromFile(this, mMediaUri);

        if (fileBytes == null) {
            // there is a problem
            return null;
        } else {
            // fileBytes has data - create the parse file and return it
            // first check if it is an image
            if (mFileType.equals(ParseConstants.TYPE_IMAGE)) {
                // then reduce the size of the file
                fileBytes = FileHelper.reduceImageForUpload(fileBytes);
            }

            // set the file name
            String fileName = FileHelper.getFileName(this, mMediaUri, mFileType);
            // use this file name to create a new parse file
            ParseFile file = new ParseFile(fileName, fileBytes);

            // attach this file to our parse object message
            message.put(ParseConstants.KEY_FILE, file);

            // return a successful message
            return message;
        }
    }

    /**
     * method to return a collection of ID's
     *
     * @return
     */
    protected ArrayList<String> getRecipientIds() {
        ArrayList<String> recipientIds = new ArrayList<String>();
        // iterate though each user in the list
        for (int i = 0; i < getListView().getCount(); i++) {
            // if the user is checked on the recipients list
            if (getListView().isItemChecked(i)) {
                // add their ID to the array list
                recipientIds.add(mFriends.get(i).getObjectId());
            }
        }
        return recipientIds;
    }

    // method that uploads a file to the backend where recipients will be able to check for it
    protected void send(ParseObject message) {
        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    // successful
                    Toast.makeText(RecipientsActivity.this, R.string.success_message, Toast.LENGTH_LONG).show();
                } else {
                    // there is an error - notify the user so they don't miss it
                    AlertDialog.Builder builder = new AlertDialog.Builder(RecipientsActivity.this);
                    builder.setMessage(R.string.error_sending_message)
                            .setTitle(R.string.error_sending_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
    }
}
