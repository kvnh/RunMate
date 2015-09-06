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
        super.onCreate();
        Parse.enableLocalDatastore(this);
        // Pass in two parameters - the application ID and the client ID that is needed to access the Parse backend
        Parse.initialize(this, "x85NrHETZkoNMOgxiPQIFCJ27iqOPnOeul8P0KA7",
                "cDcbwrY4pr0yBk9HFvjLt84wQF7EIRgVb9wRd0nk");

        // Save configuration information to the backend on parse
        ParseInstallation.getCurrentInstallation().saveInBackground();

        ParsePush.subscribeInBackground("", new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("com.khackett.runmate",
                            "successfully subscribed to the broadcast channel.");
                } else {
                    Log.e("com.khackett.runmate",
                            "failed to subscribe for push", e);
                }
            }
        });
    }

    /**
     * Creates the ParseInstallation object and applies the current users identity to it
     *
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
