package com.khackett.runmate.ui;

import android.app.Activity;
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
        // Process to get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        // Launch Date Picker Dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // Display Selected date in textbox
                mProposedDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
            }
        }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    public void proposeTime() {
        // Process to get Current Time
        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                // Display Selected time in textbox
                mProposedTime.setText(hourOfDay + ":" + minute);
            }
        }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    public void sendRouteDetails() {
        // First ensure that all of the fields are filled in correctly
        if (!routeDetailsValidCheck()) {
            // Alert the user to fill in the required details
        } else {

            // Create a calendar object and add the date and time values set by the user
            Calendar calendar = Calendar.getInstance();
            calendar.set(mYear, mMonth, mDay, mHour, mMinute);

            Log.i(TAG, calendar.toString());

            // Declare intent to capture a route
            Intent createRouteIntent = new Intent(AddRouteDetailsActivity.this, RouteRecipientsActivity.class);
            // Using android.location to extend Parcelable in order to create and store the LatLng values in an arrayList
            createRouteIntent.putParcelableArrayListExtra("markerPoints", markerPoints);

            // Add the min and max lat and long points to the intent object
            createRouteIntent.putExtra("boundaryPoints", latLngBounds);

            // Add the total distance of the route to the intent object
            createRouteIntent.putExtra("routeDistance", mRouteDistance);

            // Add the proposed date and time for the run
            createRouteIntent.putExtra("proposedTime", calendar);

            // Start RouteRecipientsActivity in order to choose recipients
            startActivity(createRouteIntent);
        }
    }

    public boolean routeDetailsValidCheck() {
        // Check that each of the fields have a value
        // Check that the time is greater than the current
        return true;
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
