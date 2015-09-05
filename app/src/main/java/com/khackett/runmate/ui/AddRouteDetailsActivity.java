package com.khackett.runmate.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.khackett.runmate.R;

import java.util.ArrayList;
import java.util.Calendar;

public class AddRouteDetailsActivity extends Activity implements View.OnClickListener {

    public static final String TAG = AddRouteDetailsActivity.class.getSimpleName();

    // member variable to represent the array of LatLng values plotted my the user and passed into this activity via the intent that started it
    protected ArrayList<LatLng> markerPoints;
    protected LatLngBounds latLngBounds;
    protected double mRouteDistance;

    protected EditText mRouteName;
    protected TextView mProposedDate;
    protected TextView mProposedTime;
    protected Button mButtonDatePicker;
    protected Button mButtonTimePicker;
    protected Button mChooseFriends;

    // Variable for storing current date and time
    private int mYear, mMonth, mDay, mHour, mMinute;

    protected Calendar proposedDateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_route_details);

        // get the array of LatLng points passed in from the map intent
        markerPoints = getIntent().getParcelableArrayListExtra("markerPoints");
        latLngBounds = getIntent().getParcelableExtra("boundaryPoints");
        mRouteDistance = getIntent().getDoubleExtra("routeDistance", mRouteDistance);

        mRouteName = (EditText) findViewById(R.id.routeName);
        mProposedDate = (TextView) findViewById(R.id.proposedDate);
        mProposedTime = (TextView) findViewById(R.id.proposedTime);
        mChooseFriends = (Button) findViewById(R.id.chooseFriendsButton);
        mButtonDatePicker = (Button) findViewById(R.id.proposeDateButton);
        mButtonTimePicker = (Button) findViewById(R.id.proposeTimeButton);

        mRouteName.setOnClickListener(this);
        mButtonDatePicker.setOnClickListener(this);
        mButtonTimePicker.setOnClickListener(this);
        mChooseFriends.setOnClickListener(this);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.routeName:
                //
                break;
            case R.id.proposeDateButton:
                proposeDate();
                break;
            case R.id.proposeTimeButton:
                proposeTime();
                break;
            case R.id.chooseFriendsButton:
                sendRouteDetails();
                break;
            default:
                System.out.println("Problem with input");
        }
    }

    public void proposeDate() {
        // Process to get and set the Current Date when the dialog is launched
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        // Launch Date Picker Dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // Display selected date
                mProposedDate.setText(dayOfMonth + " / " + (monthOfYear + 1) + " / " + year);
                // Set the member variable to the returned values form the picker
                mYear = year;
                mMonth = monthOfYear;
                mDay = dayOfMonth;
            }
        }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    public void proposeTime() {
        // Process to get and set Current Time when the dialog is launched
        final Calendar calendar = Calendar.getInstance();
        mHour = calendar.get(Calendar.HOUR_OF_DAY);
        mMinute = calendar.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                // Format time so that if the leading digit of the minutes is a zero, this will be displayed
                String proposedTime = String.format("%02d:%02d", hourOfDay, minute);
                // Display Selected time
                mProposedTime.setText(proposedTime);
                // Set the member variable to the returned values form the picker
                mHour = hourOfDay;
                mMinute = minute;
            }
        }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    public void sendRouteDetails() {
        // Check that each of the fields have a value
        String routeName = mRouteName.getText().toString();

        // Create a calendar object and add the proposed date and time values set by the user
        proposedDateTime = Calendar.getInstance();
        proposedDateTime.set(mYear, mMonth, mDay, mHour, mMinute);
        Log.i(TAG, "Proposed date: " + proposedDateTime.toString());


        // First ensure that all of the fields are filled in correctly
        if (checkForEmptyFields(routeName, mProposedDate, mProposedTime)) {
            // Alert user to fill in all of the fields
            showErrorDialog(R.string.send_route_error_title, R.string.send_route_error_message);
        } else if (!routeTimeDateValidCheck()) {
            // Alert the user to propose a time that is greater than the current
            showErrorDialog(R.string.send_route_time_error_title, R.string.send_route_time_error_message);
        } else {

            // Declare intent to send on route details
            Intent createRouteIntent = new Intent(AddRouteDetailsActivity.this, RouteRecipientsActivity.class);

            // Using android.location to extend Parcelable in order to create and store the LatLng values in an arrayList
            createRouteIntent.putParcelableArrayListExtra("markerPoints", markerPoints);

            // Add the min and max lat and long points to the intent object
            createRouteIntent.putExtra("boundaryPoints", latLngBounds);

            // Add the total distance of the route to the intent object
            createRouteIntent.putExtra("routeDistance", mRouteDistance);

            // Add the name of the route to Parse
            createRouteIntent.putExtra("routeName", routeName);


            // Add the proposed date and time for the run
            createRouteIntent.putExtra("proposedTime", proposedDateTime);

            // Start RouteRecipientsActivity in order to choose recipients
            startActivity(createRouteIntent);
        }
    }

    public boolean checkForEmptyFields(String routeName, TextView proposedDate, TextView proposedTime) {
        if (routeName.isEmpty() ||
                proposedDate.getText().equals("") ||
                proposedTime.getText().equals("")) {
            return true;
        }
        return false;
    }

    /**
     * Method to check that the proposed time is greater than the current time
     *
     * @return
     */
    public boolean routeTimeDateValidCheck() {
        // Variables for storing current date and time
        int currentYear, currentMonth, currentDay, currentHour, currentMinute;

        // Create a Calendar object and assign it the current date and time.
        Calendar currentDateTime = Calendar.getInstance();
        currentYear = currentDateTime.get(Calendar.YEAR);
        currentMonth = currentDateTime.get(Calendar.MONTH);
        currentDay = currentDateTime.get(Calendar.DAY_OF_MONTH);
        currentHour = currentDateTime.get(Calendar.HOUR_OF_DAY);
        currentMinute = currentDateTime.get(Calendar.MINUTE);
        currentDateTime.set(currentYear, currentMonth, currentDay, currentHour, currentMinute);

        Log.i(TAG, "Current time: " + currentDateTime.toString());

        // Check current time against proposed time
        if (proposedDateTime.compareTo(currentDateTime) < 0) {
            // Proposed time is earlier than the current time
            Log.i(TAG, "Proposed time is earlier than the current time");
            // Alert user and tell them to set a new date and time later than the current
            return false;
        } else {
            // Proposed time is later than the current time, return a value of true and proceed with intent
            Log.i(TAG, "Proposed time is later than the current time");
            return true;
        }
    }

    /**
     * Error dialog to display to the user if the credentials they entered are not valid
     *
     * @param title   the title of the dialog
     * @param message the message in the dialog
     */
    public void showErrorDialog(int title, int message) {
        // Alert user of relevant error - Use a dialog so user interaction is required.
        // Use Builder to build and configure the alert
        AlertDialog.Builder builder = new AlertDialog.Builder(AddRouteDetailsActivity.this);
        // Set the message title and text for the dialog
        // Chain the methods together as they are all referencing the builder object
        builder.setMessage(message).setTitle(title)
                // Button to dismiss the dialog.
                // Set the listener to null - only want to dismiss the dialog when the button is tapped.
                // ok is from android resources
                .setPositiveButton(android.R.string.ok, null);
        // Create a dialog from the builder object and show it
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_route_details, menu);
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
        }

        return super.onOptionsItemSelected(item);
    }

}
