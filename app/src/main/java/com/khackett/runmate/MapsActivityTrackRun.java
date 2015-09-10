package com.khackett.runmate;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MapsActivityTrackRun extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        ResultCallback<LocationSettingsResult> {

    /**
     * Request code to send to Google Play Services in case of connection failure.
     */
    public static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    /**
     * Request code to send to Google Play Services when services not installed.
     */
    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    /**
     * Constant used in the location settings dialog.
     */
    public static final int REQUEST_CHECK_SETTINGS = 1;

    /**
     * The desired interval for location updates in milliseconds
     */
    public static final long UPDATE_INTERVAL = 1000 * 5;

    /**
     * The fastest rate for location updates in milliseconds.
     * Updates will never be more frequent than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL = 1000 * 1;

    /**
     * The minimum distance from previous update to accept new update in meters.
     */
    public static final int DISPLACEMENT = 1;

    // Keys for storing activity state in the Bundle.
    private static final String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    private static final String LOCATION_KEY = "location-key";
    private static final String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    /**
     * Provides the entry point to Google Play services.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * Used to request a quality of service for location updates
     * and store parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;

    /**
     * The current location of the device
     */
    private Location mCurrentLocation;

    /**
     * Stores the types of location services the client is requesting.
     * Used to check settings and determine if the device has the required location settings.
     */
    protected LocationSettingsRequest mLocationSettingsRequest;

    // TAg for current Activity
    public static final String TAG = MapsActivityTrackRun.class.getSimpleName();

    // member variables for the UI buttons and text outputs
    protected Button mStartUpdatesButton;
    protected Button mStopUpdatesButton;
    protected TextView mLastUpdateTimeTextView;
    protected TextView mLatitudeTextView;
    protected TextView mLongitudeTextView;

    /**
     * Boolean to to track whether the location updates have been turned on or off by the user.
     * Value changes when the user presses the Start Run and Stop Run buttons.
     */
    protected Boolean mCheckLocationUpdates;

    /**
     * Time when the location was updated represented as a String.
     */
    protected String mLastUpdateTime;

    private double mLat;
    private double mLng;

    // Declare array list for location points
    private List<LatLng> latLngPointsArray;
    private Polyline line;

    private List<Polyline> polylines;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_activity_track_run);
        setUpMapIfNeeded();

        // set up member variables for each UI component
        mStartUpdatesButton = (Button) findViewById(R.id.start_updates_button);
        mStopUpdatesButton = (Button) findViewById(R.id.stop_updates_button);
        mLatitudeTextView = (TextView) findViewById(R.id.latitude_text);
        mLongitudeTextView = (TextView) findViewById(R.id.longitude_text);
        mLastUpdateTimeTextView = (TextView) findViewById(R.id.last_update_time_text);

        // set the location update request to false to start the activity
        mCheckLocationUpdates = false;
        mLastUpdateTime = "";

        // Initializing array lists
        latLngPointsArray = new ArrayList<LatLng>();
        polylines = new ArrayList<Polyline>();

        // make the devices location visible
        mMap.setMyLocationEnabled(true);
        // set the zoom controls to visible
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Update previous settings using data stored in the Bundle object
        updateSettingsFromBundle(savedInstanceState);

        // Check availability of Google Play services
        if (checkGooglePlayServices()) {
            // create the Google API client and the location request and request the location services API
            createGoogleApiClient();
            createLocationRequest();
            buildLocationSettingsRequest();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect to the Google API client
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        // Resume receiving location updates if requested
        if (mGoogleApiClient.isConnected() && mCheckLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            // remove location updates, but do not disconnect the GoogleApiClient object
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        // Disconnect the GoogleApiClient object
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    /**
     * Updates fields based on data stored in the bundle.
     */
    private void updateSettingsFromBundle(Bundle savedInstanceState) {
        Log.i(TAG, "Updating values from bundle");
        if (savedInstanceState != null) {
            // Update the value of mCheckLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mCheckLocationUpdates = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY);
                setButtonsEnabledState();
            }
            // Update the value of mCurrentLocation from the Bundle and update the UI to show the correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation is not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }
            // Update the value of mLastUpdateTime from the Bundle.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }
            // Update the UI.
            updateUI();
        }
    }

    /**
     * Method to store activity data in a Bundle
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mCheckLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Method to verify that Google Play Services is available on the device
     */
    private boolean checkGooglePlayServices() {
        // create instance of GoogleApiAvailability
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        // obtain a status code indicating whether there was an error
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            // if error can be resolved via user action
            if (googleAPI.isUserResolvableError(result)) {
                // return dialog to user, and direct them to Play Store if Google Play services is out of date or missing
                googleAPI.getErrorDialog(this, result, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                // otherwise, display a message informing user that Google Play services is out of date
                Toast.makeText(getApplicationContext(),
                        "Device is not supported - please install Google Play services", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Creates the Google API client used to access Google Play services
     */
    protected synchronized void createGoogleApiClient() {
        // create a new GoogleApiClient object using the builder pattern
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                // let the client know that this class will handle connection management
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                        // add the LocationServices API from Google Play Services
                .addApi(LocationServices.API)
                        // build the client
                .build();
    }

    /**
     * Creates the location request and sets the accuracy of the current location
     */
    protected void createLocationRequest() {
        // initialise the mLocationRequest object with desired settings
        mLocationRequest = LocationRequest.create()
                // request the most precise location possible
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                        // set update interval for active location updates
                .setInterval(UPDATE_INTERVAL)
                        // set fastest rate for active location updates
                        // app will never receive updates faster than this setting
                .setFastestInterval(FASTEST_UPDATE_INTERVAL);
                // .setSmallestDisplacement(DISPLACEMENT);
    }

    /**
     * Uses a LocationSettingsRequest Builder to build an object used to check
     * if users device has the required location settings.
     */
    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        // ensure that dialog will always be displayed
        builder.setAlwaysShow(true);
        mLocationSettingsRequest = builder.build();
    }

    /**
     * Method to check if the location settings for the app are adequate.
     * When the PendingResult returns, the client can check the location settings by looking
     * at the status code from the LocationSettingsResult object.
     */
    protected void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, mLocationSettingsRequest);
        result.setResultCallback(this);
    }

    /**
     * The callback invoked when checkLocationSettings() is called.
     * Checks the LocationSettingsResult object to determine if location settings are adequate
     */
    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Log.i(TAG, "All appropriate location settings enabled");
                // Location settings adequate - start checking for location updates
                startLocationUpdates();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.i(TAG, "GPS not enabled - show dialog to enable GPS");
                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result in onActivityResult().
                    status.startResolutionForResult(MapsActivityTrackRun.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    Log.i(TAG, "PendingIntent unable to execute request.");
                }
                break;
//            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
//                Log.i(TAG, "Location settings are inadequate and cannot be fixed here.");
//                break;
            default:
                Log.i(TAG, "Status not recognised - check LocationSettingsResult");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult() in onResult() above
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User made required changes location settings.");
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User did not change required location settings.");
                        break;
                }
                break;
        }
    }

    /**
     * Handles the Start Run button and checks for location settings before proceeding.
     */
    public void startUpdatesButton(View view) {
        // check location settings to ensure GPS is enabled, before running location updates
        checkLocationSettings();
    }

    /**
     * Handles the Stop Run button, and requests removal of location updates.
     */
    public void stopUpdatesButton(View view) {
        // stop checking for location updates
        stopLocationUpdates();
    }

    /**
     * Ensures  only one button is enabled at a time.
     * Stop Run button is enabled if the user is requesting location updates.
     * Start Run button is enabled if the user is not requesting location updates.
     */
    private void setButtonsEnabledState() {
        if (mCheckLocationUpdates) {
            mStartUpdatesButton.setEnabled(false);
            mStopUpdatesButton.setEnabled(true);
        } else {
            mStartUpdatesButton.setEnabled(true);
            mStopUpdatesButton.setEnabled(false);
        }
    }

    /**
     * Called when a successful connection is made to the Google API client
     */
    @Override
    public void onConnected(Bundle bundle) {

        // log that a connection has been made
        Log.i(TAG, "Location services are now connected");

        // if current location is null, get the last known location of device
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            // zoomToCurrentLocation(mCurrentLocation);
            updateUI();
        }

        // If Start Run button is pressed before GoogleApiClient connects,
        // mRequestingLocationUpdates is set to true in startUpdatesButtonHandler()
        // Then start location updates.
        if (mCheckLocationUpdates) {
            startLocationUpdates();
        }

    }

    public void zoomToCurrentLocation(Location location) {
        Log.d(TAG, location.toString());
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
        MarkerOptions options = new MarkerOptions()
                .position(latLng);
        mMap.addMarker(options);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
        mMap.animateCamera(cameraUpdate);
    }

    /**
     * Requests location updates from the FusedLocationApi
     */
    protected void startLocationUpdates() {
        // Calls the onLocationChanged() listener when location has changed
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // mCheckLocationUpdates set to true and state of buttons are reset
                        mCheckLocationUpdates = true;
                        setButtonsEnabledState();
                    }
                });
    }

    /**
     * Removes location updates from the FusedLocationApi when activity is paused or stopped
     */
    protected void stopLocationUpdates() {
        // Removes this listener when the prompted
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // mCheckLocationUpdates set to false and state of buttons are reset
                        mCheckLocationUpdates = false;
                        setButtonsEnabledState();
                    }
                });
    }

    /**
     * Callback from requestLocationUpdates() that is called when the location changes
     */
    @Override
    public void onLocationChanged(Location location) {

        if (!location.hasAccuracy()) {
            return;
        }
        if (location.getAccuracy() > 5) {
            return;
        }

        double latitude, longitude;
        LatLng latLngPoint;

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        latLngPoint = new LatLng(latitude, longitude);

        PolylineOptions polylineOptions = new PolylineOptions()
                .width(6)
                .color(Color.BLUE);
        // .geodesic(true);
        for (int i = 0; i < latLngPointsArray.size(); i++) {
            polylineOptions.add(latLngPointsArray.get(i));
        }
        line = mMap.addPolyline(polylineOptions);
        latLngPointsArray.add(latLngPoint);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLngPoint, 20);
        mMap.animateCamera(cameraUpdate);

        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
        Toast.makeText(this, getResources().getString(R.string.location_updated_message), Toast.LENGTH_SHORT).show();

//        mCurrentLocation = location;
//        mLat = mCurrentLocation.getLatitude();
//        mLng = mCurrentLocation.getLongitude();
//        // LatLng newLatLngPoint = new LatLng((int) (mLat * 1e6), (int) (mLng * 1e6));
//        LatLng newLatLngPoint = new LatLng(mLat, mLng);
//
//        // add each new point detected to the latlng array and print to console
//        latLngPointsArray.add(newLatLngPoint);
//        System.out.println("Printing lat & lng points array accurate");
//        for (LatLng point : latLngPointsArray) {
//            System.out.println(point);
//        }
    }

    //    public void onLocationChanged(Location location) {
//        String msg = "Location:" + location.getLatitude() + "," + location.getLongitude();
//        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
//        double new_lat = location.getLatitude();
//        double new_long = location.getLongitude();
//        double previous_lat = new_lat;
//        double previous_long = new_long;
//        drawTrack(new_lat, new_long, previous_lat, previous_long);
//    }
//
//    private void drawTrack(double new_lat, double new_long, double previous_lat, double previous_long) {
//        PolylineOptions options = new PolylineOptions();
//        options.add(new LatLng(previous_lat, previous_long));
//        options.add(new LatLng(new_lat, new_long));
//        options.width(10);
//        options.color(Color.RED);
//        mMap.addPolyline(options);
//
//        googleMap.addPolyline(new PolylineOptions()
//                .add(new LatLng(Double.parseDouble(YOUR PREVIOUS LATITUDE VALUE),
//                        Double.parseDouble(YOUR PREVIOUS LONGITUDE VALUE),
//                        new LatLng(Double.parseDouble(YOUR LATEST LATITUDE VALUE),
//                                Double.parseDouble(YOUR LATEST LONGITUDE VALUE)
//                                        .width(5).color(getResources()
//                                        .getColor(R.color.BLACK))
//                                        .geodesic(true));
//    }

    /**
     * Updates the latitude, longitude, and last location time in the UI.
     */
    private void updateUI() {
        setButtonsEnabledState();
        if (mCurrentLocation != null) {
            mLatitudeTextView.setText(String.valueOf(mCurrentLocation.getLatitude()));
            mLongitudeTextView.setText(String.valueOf(mCurrentLocation.getLongitude()));
            mLastUpdateTimeTextView.setText(mLastUpdateTime);
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // log that the connection is suspended and try to reconnect
        Log.i(TAG, "Connection to Location services have been suspended. Attempting to reconnect");
        mGoogleApiClient.connect();
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // If connection failed, start a Google Play services activity to resolve the error
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                // Thrown if Google Play services canceled the original PendingIntent
            } catch (IntentSender.SendIntentException e) {
                // Log error
                e.printStackTrace();
            }
        } else {
            // Display a dialog to the user with the error code if no resolution available
            Log.i(TAG, "Location services connection failed. Code = " + connectionResult.getErrorCode());
        }
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera.
     */
    private void setUpMap() {
        // mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }
}
