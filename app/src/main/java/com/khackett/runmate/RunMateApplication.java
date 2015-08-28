package com.khackett.runmate;

import android.app.Application;
import android.util.Log;

import com.khackett.runmate.ui.MainActivity;
import com.khackett.runmate.utils.ParseConstants;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * Created by KHackett on 06/08/15.
 */
public class RunMateApplication extends Application {

    @Override
    public void onCreate() {
        // since we are overriding the method from the parent, add super.onCreate() to make
        // sure all of the code in the base class gets called too
        super.onCreate();
        Parse.enableLocalDatastore(this);
        // the 2 parameters are the application ID and the client ID that we need to access our backend in parse.com
        Parse.initialize(this, "x85NrHETZkoNMOgxiPQIFCJ27iqOPnOeul8P0KA7", "cDcbwrY4pr0yBk9HFvjLt84wQF7EIRgVb9wRd0nk");

        // Save configuration information to the backend on parse
        ParseInstallation.getCurrentInstallation().saveInBackground();

        ParsePush.subscribeInBackground("", new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("com.khackett.runmate", "successfully subscribed to the broadcast channel.");
                } else {
                    Log.e("com.khackett.runmate", "failed to subscribe for push", e);
                }
            }
        });

    }

    /**
     * Creates the ParseInstallation object and applies the current users identity to it
     * @param user
     */
    public static void updateParseInstallationObject(ParseUser user) {
        // Create an Installation variable
        ParseInstallation parseInstallation = ParseInstallation.getCurrentInstallation();
        // Add custom data to the installation object
        parseInstallation.put(ParseConstants.KEY_USER_ID, user.getObjectId());
        // Save the installation to the backend
        parseInstallation.saveInBackground();
    }
}
