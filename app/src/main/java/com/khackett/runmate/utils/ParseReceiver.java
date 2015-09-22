package com.khackett.runmate.utils;

import android.content.Context;
import android.content.Intent;

import com.khackett.runmate.ui.MainActivity;
import com.parse.ParsePushBroadcastReceiver;

/**
 * Receiver class to handle messages sent to users
 */
public class ParseReceiver extends ParsePushBroadcastReceiver {

    /**
     * Called whenever a push message is selected.
     * Opens MainActivity when the push notification is selected.
     *
     * @param context
     * @param intent
     */
    @Override
    public void onPushOpen(Context context, Intent intent) {
        Intent i = new Intent(context, MainActivity.class);
        i.putExtras(intent.getExtras());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

}