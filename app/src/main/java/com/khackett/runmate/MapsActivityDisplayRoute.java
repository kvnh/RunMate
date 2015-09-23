package com.khackett.runmate;

import android.app.AlertDialog;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.khackett.runmate.utils.DirectionsUtility;
import com.khackett.runmate.utils.ParseConstants;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to display a Route on a map.
 */
public class MapsActivityDisplayRoute extends FragmentActivity implements View.OnClickListener {

    // Simple class TAG for logcat output
    public static final String TAG = MapsActivityDisplayRoute.class.getSimpleName();

    // GoogleMap object
    // Might be null if Google Play services APK is not available.
    private GoogleMap mMap;

    // Member variable to represent an array of LatLng values, used to retrieve the sent route via the Directions API
    private ArrayList<LatLng> markerPoints;
    // All returned LatLng points from the Directions API - used for animation
    private ArrayList<LatLng> allNonDuplicateLatLng;

    // Create a DirectionsUtility object
    private DirectionsUtility directionsUtility;

    // Member variable for the UI buttons
    private Button mButtonAccept;
    private Button mButtonDecline;
    private Button mButtonAnimate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_activity_display_route);

        // Instantiate markerPoints ArrayList
        markerPoints = new ArrayList<LatLng>();

        // Instantiate allNonDuplicateLatLng ArrayList
        allNonDuplicateLatLng = new ArrayList<LatLng>();

        // Instantiate DirectionsUtility object
        directionsUtility = new DirectionsUtility();

        // Getting reference to SupportMapFragment of the activity_maps
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        // Getting Map for the SupportMapFragment
        mMap = fm.getMap();

        if (mMap != null) {

            // Check whether the Route was created manually/GPS or with the Directions API
            String creationType = getIntent().getStringExtra("creationType");
            if (creationType.equals("MANUAL")) {
                plotManualRoute();
            } else {
                plotDirectionsRoute();
            }

            mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    zoomToViewRoute();
                }
            });

            // Enable MyLocation Button in the Map
            mMap.setMyLocationEnabled(true);

            // Set the zoom controls to visible
            mMap.getUiSettings().setZoomControlsEnabled(true);

        }

        // Set up member variables for each UI component
        mButtonAnimate = (Button) findViewById(R.id.btn_animate);
        mButtonAccept = (Button) findViewById(R.id.btn_accept);
        mButtonDecline = (Button) findViewById(R.id.btn_decline);

        // Register buttons with the listener
        mButtonAnimate.setOnClickListener(this);
        mButtonAccept.setOnClickListener(this);
        mButtonDecline.setOnClickListener(this);
    }

    /**
     * Method to plot a Route that was created manually or via GPS tracking
     */
    public void plotManualRoute() {
        // Assign the JSON String value from the passed in intent to a new String variable
        String jsonArray = getIntent().getStringExtra("parseLatLngList");
        JSONArray array = null;
        PolylineOptions polylineOptions = null;

        try {
            // convert String to a JSONArray
            array = new JSONArray(jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Create JSONArray to hold points
        JSONArray arrayPoints = array;

        // Iterate through the array and plot on the map
        for (int i = 0; i < arrayPoints.length(); i++) {
            // Extract the latitude and longitude values for each point in the array
            LatLng latLngObject = new LatLng(arrayPoints.optJSONObject(i).optDouble("latitude"),
                    arrayPoints.optJSONObject(i).optDouble("longitude"));

            // Adding new LatLng point to the array list
            markerPoints.add(latLngObject);

            // Initialising the polyline in the map and setting values
            polylineOptions = new PolylineOptions()
                    .color(Color.BLUE)
                    .width(6);

            // Setting points of polyline
            polylineOptions.addAll(markerPoints);

        }

        // Add the markerPoints to allNonDuplicateLatLng for the animation feature
        allNonDuplicateLatLng = new ArrayList<LatLng>(markerPoints);

        // Adding the polyline to the map
        mMap.addPolyline(polylineOptions);

        // Add the start and finish markers to the map
        addMarkersToMap(markerPoints);
    }

    /**
     * Method to plot a Route that was created via the Directions API
     */
    public void plotDirectionsRoute() {
        // Assign the JSON String value from the passed in intent to a new String variable
        String jsonArray = getIntent().getStringExtra("parseLatLngList");
        JSONArray array = null;

        try {
            // convert String to a JSONArray
            array = new JSONArray(jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONArray arrayPoints = array;

        // Iterate through the array and plot on the map via Directions requests
        for (int i = 0; i < arrayPoints.length(); i++) {
            // Extract the latitude and longitude values for each point in the array
            LatLng latLngObject = new LatLng(arrayPoints.optJSONObject(i).optDouble("latitude"),
                    arrayPoints.optJSONObject(i).optDouble("longitude"));

            // Adding new latlng point to the array list
            markerPoints.add(latLngObject);
        }

        // Creating MarkerOptions object
        MarkerOptions marker = new MarkerOptions();

        for (int i = 0; i < markerPoints.size() - 1; i++) {

            // For the start location, the colour of the marker is GREEN
            if (markerPoints.size() == 1) {
                // Add a green marker for the start position.
                marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            }

            // Once the second point is plotted, begin calls to the Directions API
            if (markerPoints.size() >= 2) {

                // Assign values to LatLng objects
                LatLng point1 = markerPoints.get(i);
                LatLng point2 = markerPoints.get(i + 1);

                // Hide the next marker for the next position selected
                marker.position(point1).visible(false);

                // Creating URL to send to the Google Directions API.
                String url = directionsUtility.getDirectionsUrl(point1, point2);
                // Create a DownloadURLTask object - see nested class below
                DownloadURLTask downloadURLTask = new DownloadURLTask();
                // Start downloading JSON data from Google Directions API
                downloadURLTask.execute(url);
            }
        }
        // Add the start and finish markers to the map
        addMarkersToMap(markerPoints);
    }

    /**
     * Asynchronous task to fetch JSON data via the passed in URL
     */
    private class DownloadURLTask extends AsyncTask<String, Void, String> {
        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {
            // For storing data from web service
            String urlJSONData = "";
            try {
                // Fetch and process the web page content and return resultant String
                urlJSONData = directionsUtility.downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return urlJSONData;
        }

        // Executes in UI thread, after the execution of doInBackground()
        @Override
        protected void onPostExecute(String urlJSONData) {
            super.onPostExecute(urlJSONData);

            // Create a ParseLatLngValuesTask object and invoke thread for parsing JSON data
            ParseLatLngValuesTask parseLatLngValuesTask = new ParseLatLngValuesTask();
            parseLatLngValuesTask.execute(urlJSONData);
        }
    }

    /**
     * An AsyncTask class to parse the LatLng values from the Directions API JSON data.
     * LatLng values will then be used to plot a line on the map.
     */
    private class ParseLatLngValuesTask extends AsyncTask<String, Integer, List<LatLng>> {
        // Parsing the data in non-ui thread
        @Override
        protected List<LatLng> doInBackground(String... jsonData) {
            // Create a JSONObject to store the returned JSON data
            JSONObject jsonObject;
            // List to hold all of the route points in the returned JSON data
            List<LatLng> routePoints = null;

            try {
                jsonObject = new JSONObject(jsonData[0]);
                // Start parsing data
                routePoints = directionsUtility.parseJSONObjectOverviewPolyline(jsonObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Return the routePoints to the onPostExecute()
            return routePoints;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<LatLng> routePoints) {
            // Create a PolylineOptions object
            PolylineOptions lineOptions = new PolylineOptions();

            // Remove all duplicates from the list - this is to ensure a smooth animation
            for (LatLng routePoint : routePoints) {
                Log.i(TAG, "Enhanced for loop with each section LatLng point: " + routePoint.toString());
                if (allNonDuplicateLatLng.size() == 0) {
                    Log.i(TAG, "Adding first point: " + routePoint.toString());
                    allNonDuplicateLatLng.add(routePoint);
                } else if (!routePoint.toString().equals(allNonDuplicateLatLng.get(allNonDuplicateLatLng.size() - 1).toString())) {
                    Log.i(TAG, "Adding non repeating points: " + routePoint.longitude + " " + routePoint.latitude);
                    allNonDuplicateLatLng.add(routePoint);
                }
            }
            Log.i(TAG, "Enhanced for loop with all LatLng points: " + allNonDuplicateLatLng.toString());

            // Adding all the points in the route to LineOptions
            lineOptions.addAll(routePoints);
            lineOptions.width(6);
            lineOptions.color(Color.BLUE);

            // Drawing polyline on the map
            mMap.addPolyline(lineOptions);
        }
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_accept:
                acceptRoute();
                break;
            case R.id.btn_decline:
                declineRoute();
                break;
            case R.id.btn_animate:
                animateRoute();
                break;
            default:
                Log.i(TAG, "Problem with input");
        }
    }

    /**
     * Method to accept a received route
     */
    public void acceptRoute() {
        // Get the objects ID from the intent
        String objectId = getIntent().getStringExtra("myObjectId");
        // Create a query in relation to the Route class
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.CLASS_ROUTES);
        // Get the object in a background thread
        query.getInBackground(objectId, new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    // If no exception - get the Route object and accept it
                    acceptUserRoute(object);
                } else {
                    // There was an error - notify the user
                    AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivityDisplayRoute.this);
                    builder.setMessage(R.string.error_accepting_route_message)
                            .setTitle(R.string.error_accepting_route_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
        // Send the user back to MainActivity after the message is accepted.
        finish();
    }

    /**
     * Method to decline a received route
     */
    public void declineRoute() {
        // Get the objects ID from the intent
        String objectId = getIntent().getStringExtra("myObjectId");
        // Create a query in relation to the Route class
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.CLASS_ROUTES);
        // Get the object in a background thread
        query.getInBackground(objectId, new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    // If no exception - get the Route object and delete the current user from it
                    deleteUserRoute(object);
                } else {
                    // there is an error - notify the user
                    AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivityDisplayRoute.this);
                    builder.setMessage(R.string.error_declining_route_message)
                            .setTitle(R.string.error_declining_route_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
        // Send the user back to the main activity right after the message is deleted.
        finish();
    }

    /**
     * Method to add the accepted recipient to the Route object
     *
     * @param object
     */
    public void acceptUserRoute(ParseObject object) {
        // Create a new ParseObject
        ParseObject route = object;
        // Add the current user to the accepted list and update the Route object
        ArrayList<String> acceptedRecipientIds = new ArrayList<String>();
        acceptedRecipientIds.add(ParseUser.getCurrentUser().getObjectId());
        route.put(ParseConstants.KEY_ACCEPTED_RECIPIENT_IDS, acceptedRecipientIds);

        // Get the list of recipients
        List<String> recipientList = route.getList(ParseConstants.KEY_RECIPIENT_IDS);
        if (recipientList.size() == 1) {
            // If it is the last recipient - delete the Route object
            route.deleteInBackground();
        } else {
            // Remove the current user from the recipient list and save.
            recipientList.remove(ParseUser.getCurrentUser().getObjectId());

            ArrayList<String> recipientsToRemove = new ArrayList<String>();
            recipientsToRemove.add(ParseUser.getCurrentUser().getObjectId());

            route.removeAll(ParseConstants.KEY_RECIPIENT_IDS, recipientsToRemove);
            route.saveInBackground();
        }
        Toast.makeText(MapsActivityDisplayRoute.this, R.string.success_accept_route, Toast.LENGTH_LONG).show();
    }

    /**
     * * Method to remove the recipient from the Route object
     *
     * @param object
     */
    public void deleteUserRoute(ParseObject object) {
        // Create a new ParseObject
        ParseObject route = object;
        // Create a list and assign the recipients list to it
        List<String> recipientList = route.getList(ParseConstants.KEY_RECIPIENT_IDS);

        if (recipientList.size() == 1) {
            // Last recipient - delete the route object
            route.deleteInBackground();
        } else {
            // Remove the recipient and save
            recipientList.remove(ParseUser.getCurrentUser().getObjectId());

            ArrayList<String> recipientsToRemove = new ArrayList<String>();
            recipientsToRemove.add(ParseUser.getCurrentUser().getObjectId());

            route.removeAll(ParseConstants.KEY_RECIPIENT_IDS, recipientsToRemove);
            route.saveInBackground();
        }
        Toast.makeText(MapsActivityDisplayRoute.this, R.string.success_decline_route, Toast.LENGTH_LONG).show();
    }

    /**
     * Method to zoom to the boundary extremes of a plotted route.
     */
    public void zoomToViewRoute() {
        // Assign the JSON String value from the passed in intent to a new String variable
        String jsonArrayString = getIntent().getStringExtra("parseLatLngBoundsList");
        // Create JSONArray object.
        JSONArray jsonArray = null;

        try {
            // Convert String to a JSONArray object
            jsonArray = new JSONArray(jsonArrayString);
        } catch (JSONException e) {
            // Otherwise, print exception
            e.printStackTrace();
        }

        // Create a new JSONArray
        JSONArray pointsArray = jsonArray;

        // Get LatLng values for the extreme southWest and northEast corners of the route
        LatLng southWest = new LatLng(pointsArray.optJSONObject(0).optDouble("latitude"),
                pointsArray.optJSONObject(0).optDouble("longitude"));
        LatLng northEast = new LatLng(pointsArray.optJSONObject(1).optDouble("latitude"),
                pointsArray.optJSONObject(1).optDouble("longitude"));

        // Set these values to LatLngBounds and animate camera to view
        LatLngBounds latLngBounds = new LatLngBounds(southWest, northEast);
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 60));
    }

    /**
     * Method to add start and finish markers to the route.
     */
    public void addMarkersToMap(List<LatLng> latLngs) {
        // If start and finish points are the same display a single marker
        if (latLngs.get(0).toString().equals(latLngs.get(latLngs.size() - 1).toString())) {
            mMap.addMarker(new MarkerOptions()
                    .position(latLngs.get(0))
                    .title("Start/Finish")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))
                    .showInfoWindow();
        } else {
            // Otherwise start and finish points are different - display 2 markers
            mMap.addMarker(new MarkerOptions()
                    .position(latLngs.get(latLngs.size() - 1))
                    .title("Finish")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
                    .showInfoWindow();
            mMap.addMarker(new MarkerOptions()
                    .position(latLngs.get(0))
                    .title("Start")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))
                    .showInfoWindow();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
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
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }


    /**
     * Method to begin the animation of the camera along the route
     */
    public void animateRoute() {
        // Keep the screen on while the user is animating the route
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // Start the animation
        mRunnerAnimation.startAnimation();
    }

    // Create a RunMateAnimator object
    private RunnerAnimation mRunnerAnimation = new RunnerAnimation();
    // Handler object to process the Runnable object
    private final Handler mHandler = new Handler();

    /**
     * Class to handle the animation feature of the MapsActivityDisplayRoute class
     * Some code taken from:
     * https://github.com/ddewaele/GoogleMapsV2WithActionBarSherlock/blob/master/GoogleMapsV2WithActionBarSherlock/docs/part3.md
     */
    public class RunnerAnimation implements Runnable {

        /**
         * The speed of the camera during animation
         */
        private static final int CAMERA_SPEED = 1500;

        /**
         * The duration of an animation between 2 points
         */
        private static final int ANIMATION_DURATION = 1000;

        /**
         * The initial zoom value of the camera
         */
        private static final int INITIAL_CAMERA_ZOOM_VALUE = 16;

        /**
         * The title angle of the camera
         */
        private static final float CAMERA_TILT_VALUE = 90;

        /**
         * The frame rate of the runner icon during animation
         */
        private static final long MARKER_FRAME_RATE_MILLISECONDS = 16;

        // Linear interpolator to define the rate of change of the animation.
        private final Interpolator interpolator = new LinearInterpolator();

        // Set the currentLatLngCheck to the first index of the array.
        private int currentLatLngIndex = 0;

        // Time in milliseconds since the system booted up
        private long startTime = SystemClock.uptimeMillis();

        // LatLng variable for the beginning and end points in each animation sequence
        private LatLng beginLatLng = null;
        private LatLng endLatLng = null;

        // The marker represented by a runner icon
        private Marker runnerMarker;

        /**
         * Method to start animation when there are more the 2 points to animate through
         */
        public void startAnimation() {
            if (allNonDuplicateLatLng.size() > 2) {
                mRunnerAnimation.initialize();
            }
        }

        /**
         * Method to reset the animation values at the end of each sequence
         */
        public void reset() {
            startTime = SystemClock.uptimeMillis();
            currentLatLngIndex = 0;
            endLatLng = getEndLatLng();
            beginLatLng = getBeginLatLng();
        }

        /**
         * Method to set the initial position of the camera based on the first and second LatLng points
         */
        public void initialize() {

            // Reset the animation values
            reset();

            // Get the LatLng values of the first and second points
            LatLng firstPoint = allNonDuplicateLatLng.get(0);
            LatLng secondPoint = allNonDuplicateLatLng.get(1);

            // Set the camera to its first position
            setupCameraPositionForMovement(firstPoint, secondPoint);
        }

        /**
         * Method to set up the camera's initial position and to add the Runner icon to the Map
         *
         * @param firstPoint  the first point in the animation sequence
         * @param secondPoint the second point in the animation sequence
         */
        private void setupCameraPositionForMovement(LatLng firstPoint, LatLng secondPoint) {
            // Set the initial camera bearing to a value based on the first 2 points
            float cameraBearingStart = bearingBetweenLatLngPoints(firstPoint, secondPoint);

            // Create the runner icon
            runnerMarker = mMap.addMarker(new MarkerOptions()
                    .position(firstPoint)
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_directions_run_black_24dp)));

            // Set up camera position for the start point.
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(firstPoint) // Set the initial view for the camera.
                    .tilt(CAMERA_TILT_VALUE)
                    .bearing(cameraBearingStart)  // Set the camera orientation angle for th first point.
                    .zoom(mMap.getCameraPosition().zoom >= INITIAL_CAMERA_ZOOM_VALUE
                            ? mMap.getCameraPosition().zoom : INITIAL_CAMERA_ZOOM_VALUE) // Set the initial zoom value.
                    .build();   // Create a CameraPosition from the builder.

            // Animate the camera to the new position
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),
                    ANIMATION_DURATION,
                    new GoogleMap.CancelableCallback() {

                        @Override
                        public void onFinish() {
                            Log.i(TAG, "Camera animation finished");
                            // Reset the animation values
                            mRunnerAnimation.reset();
                            // The Handler’s post() method posts the Runnable object to the message queue
                            // which kicks off the run() method to begin the next stage of the animation sequence.
                            Handler handler = new Handler();
                            handler.post(mRunnerAnimation);
                        }

                        @Override
                        public void onCancel() {
                            Log.i(TAG, "Camera animation cancelled");
                        }
                    });
        }

        @Override
        public void run() {

            // Start the timer for the Runnable thread.
            long elapsedTime = SystemClock.uptimeMillis() - startTime;

            // Map a value representing the elapsed fraction of an animation to a value that represents the interpolated fraction.
            double interpolatedFraction = interpolator.getInterpolation((float) elapsedTime / CAMERA_SPEED);

            // Generate latitude and longitude values of the markers position on the path, based on the interpolated fraction.
            double lat = (interpolatedFraction * endLatLng.latitude) + ((1 - interpolatedFraction) * beginLatLng.latitude);
            double lng = (interpolatedFraction * endLatLng.longitude) + ((1 - interpolatedFraction) * beginLatLng.longitude);
            LatLng newPosition = new LatLng(lat, lng);

            // Set the position of the marker to the calculated LatLng value
            runnerMarker.setPosition(newPosition);

            // If the interpolator fraction is less than 1
            if (interpolatedFraction < 1) {
                // Add Runnable to the message queue to display the marker moving.
                mHandler.postDelayed(this, MARKER_FRAME_RATE_MILLISECONDS);
                // Otherwise move to the next point in the ArrayList
            } else {
                // If there are still points in the list
                if (currentLatLngIndex < allNonDuplicateLatLng.size() - 2) {

                    // Increment to the next point in the ArrayList
                    currentLatLngIndex++;

                    // Get point A and point B LatLng values
                    beginLatLng = getBeginLatLng();
                    endLatLng = getEndLatLng();

                    // Reset time to the time since system reboot
                    startTime = SystemClock.uptimeMillis();

                    // Calculate the cameras bearing point based on the next 2 points in the route
                    float cameraBearing = bearingBetweenLatLngPoints(beginLatLng, endLatLng);

                    // Set the camera position to the next 2 points in the route
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(endLatLng)
                            .bearing(cameraBearing)
                            .tilt(CAMERA_TILT_VALUE)
                            .zoom(mMap.getCameraPosition().zoom)
                            .build();

                    // Animate the camera to the next point
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),
                            ANIMATION_DURATION,
                            null);

                    // Begin the Runnable thread again
                    mHandler.postDelayed(mRunnerAnimation, MARKER_FRAME_RATE_MILLISECONDS);

                } else {
                    // Increment the current point on the route
                    currentLatLngIndex++;
                    // Zoom out to view route once the camera finishes animating.
                    zoomToViewRoute();
                    // Remove the runner icon
                    runnerMarker.remove();
                    // Remove any callbacks to the Handler object and stop the thread
                    mHandler.removeCallbacks(mRunnerAnimation);
                }
            }
        }

        /**
         * Method to get the end LatLng value
         *
         * @return the end LatLng value
         */
        private LatLng getEndLatLng() {
            return allNonDuplicateLatLng.get(currentLatLngIndex + 1);
        }

        /**
         * Method to get the beginning LatLng value
         *
         * @return the beginning LatLng value
         */
        private LatLng getBeginLatLng() {
            return allNonDuplicateLatLng.get(currentLatLngIndex);
        }

        /**
         * Method to find the optimum bearing point between to LatLng values
         *
         * @param start - the start LatLng point
         * @param end   - the end LatLng point
         * @return the bearing point between the two LatLng values
         */
        private float bearingBetweenLatLngPoints(LatLng start, LatLng end) {
            Location startLocation = convertLatLngToLocation(start);
            Location endLocation = convertLatLngToLocation(end);
            return startLocation.bearingTo(endLocation);
        }

        /**
         * Method to convert LatLng values to Location objects
         *
         * @param latLng - the LatLng value to be converted to a Location object
         * @return
         */
        private Location convertLatLngToLocation(LatLng latLng) {
            // Create a new Location object
            Location location = new Location("someLocation");
            // Set its latitude and longitude values based on the supplied LatLng value
            location.setLatitude(latLng.latitude);
            location.setLongitude(latLng.longitude);
            return location;
        }

    }
}