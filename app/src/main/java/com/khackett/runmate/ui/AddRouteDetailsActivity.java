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

/**
 * Activity to add date, time and name details to a recently created route.
 */
public class AddRouteDetailsActivity extends Activity implements View.OnClickListener {

    // Simple class TAG for logcat output
    public static final String TAG = AddRouteDetailsActivity.class.getSimpleName();

    // Member variables
    private ArrayList<LatLng> markerPoints;
    private ArrayList<LatLng> allLatLngPoints;
    private LatLngBounds latLngBounds;
    private double routeDistance;
    private String creationType;

    // Member variable for UI components
    private TextView mButtonDatePicker;
    private TextView mButtonTimePicker;
    private EditText mRouteName;
    private Button mChooseFriends;

    // Member variables for storing current date and time
    private int mYear, mMonth, mDay, mHour, mMinute;
    private Calendar proposedDateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_route_details);

        // Get the data passed in from the map intent and assign to member variables
        markerPoints = getIntent().getParcelableArrayListExtra("markerPoints");
        allLatLngPoints = getIntent().getParcelableArrayListExtra("allLatLngPoints");
        latLngBounds = getIntent().getParcelableExtra("boundaryPoints");
        routeDistance = getIntent().getDoubleExtra("routeDistance", routeDistance);
        creationType = getIntent().getStringExtra("routeCreationMethod");

        // Set up member variables for each UI component
        mButtonDatePicker = (TextView) findViewById(R.id.proposeDateButton);
        mButtonTimePicker = (TextView) findViewById(R.id.proposeTimeButton);
        mRouteName = (EditText) findViewById(R.id.routeName);
        mChooseFriends = (Button) findViewById(R.id.chooseFriendsButton);

        // Register components with the listener
        mButtonDatePicker.setOnClickListener(this);
        mButtonTimePicker.setOnClickListener(this);
        mRouteName.setOnClickListener(this);
        mChooseFriends.setOnClickListener(this);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        // Switch statement to select which action to take depending on component pressed.
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

    /**
     * Method to add a proposed date to the run.
     */
    public void proposeDate() {
        // Process to get and set the Current Date when the dialog is launched
        final Calendar calendar = Calendar.getInstance();
        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        mDay = calendar.get(Calendar.DAY_OF_MONTH);

        // Launch Date Picker Dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // Display selected date
                mButtonDatePicker.setText(dayOfMonth + " / " + (monthOfYear + 1) + " / " + year);
                // Set the member variable to the returned values from the picker
                mYear = year;
                mMonth = monthOfYear;
                mDay = dayOfMonth;
            }
        }, mYear, mMonth, mDay);
        // Show the DatePickerDialog
        datePickerDialog.show();
    }

    /**
     * Method to add a proposed time to the run.
     */
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
                mButtonTimePicker.setText(proposedTime);
                // Set the member variable to the returned values form the picker
                mHour = hourOfDay;
                mMinute = minute;
            }
        }, mHour, mMinute, false);
        // Show the DatePickerDialog
        timePickerDialog.show();
    }

    /**
     * Method to send on the details of the run to the RouteRecipientsActivity.
     */
    public void sendRouteDetails() {

        // Assign the name of the route.
        String routeName = mRouteName.getText().toString();
        // Create a Calendar object and add the proposed date and time values set by the user.
        proposedDateTime = Calendar.getInstance();
        proposedDateTime.set(mYear, mMonth, mDay, mHour, mMinute);

        // Ensure that all of the fields are filled in correctly / have a value
        if (checkForEmptyFields(routeName, mButtonDatePicker, mButtonTimePicker)) {
            // Alert user to fill in all of the fields
            showErrorDialog(R.string.send_route_error_title, R.string.send_route_error_message);
            // Check that the time is greater than the current
        } else if (!routeTimeDateValidCheck()) {
            // Alert the user to propose a time that is greater than the current
            showErrorDialog(R.string.send_route_time_error_title, R.string.send_route_time_error_message);
        } else {
            // All fields so send the route.
            // Declare an intent to send on route details to RouteRecipientsActivity.
            Intent createRouteIntent = new Intent(AddRouteDetailsActivity.this, RouteRecipientsActivity.class);
            // Use android.location to extend Parcelable in order to create and store plotted LatLng values in an arrayList
            createRouteIntent.putParcelableArrayListExtra("markerPoints", markerPoints);
            // Use android.location to extend Parcelable in order to create and store all LatLng values in an arrayList
            createRouteIntent.putParcelableArrayListExtra("allLatLngPoints", allLatLngPoints);
            // Add the min and max lat and long points to the intent object
            createRouteIntent.putExtra("boundaryPoints", latLngBounds);
            // Add the total distance of the route to the intent object
            createRouteIntent.putExtra("routeDistance", routeDistance);
            // Add the name of the route to Parse
            createRouteIntent.putExtra("routeName", routeName);
            // Add the creation type of the route to Parse
            createRouteIntent.putExtra("routeCreationMethod", creationType);
            // Add the proposed date and time for the route
            createRouteIntent.putExtra("proposedTime", proposedDateTime);
            // Start RouteRecipientsActivity in order to choose recipients
            startActivity(createRouteIntent);
        }
    }

    /**
     * Method to check for empty fields in AddRouteDetailsActivity
     *
     * @param routeName    the name of the run
     * @param proposedDate the proposed Date of the run
     * @param proposedTime the proposed Time of the run
     * @return a boolean value
     */
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
     * @return a boolean value
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
        // Handle action bar item clicks here.
        // The action bar will automatically handle clicks on the Home/Up button,
        // so long as a parent activity is specified in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}