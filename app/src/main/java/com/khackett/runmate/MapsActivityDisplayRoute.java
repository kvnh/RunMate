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
import com.google.android.gms.maps.model.PolylineOptions;
import com.khackett.runmate.utils.DirectionsJSONParser;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivityDisplayRoute extends FragmentActivity implements View.OnClickListener {

    public static final String TAG = MapsActivityDisplayRoute.class.getSimpleName();

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    // Member variable for the UI buttons
    protected Button mButtonAccept;
    protected Button mButtonDecline;
    protected Button mButtonAnimate;

    // member variable to represent an array of LatLng values, used to retrieve the sent route via the Directions API
    protected ArrayList<LatLng> markerPoints;

    // member variable to represent an array of LatLng values, used to zoom to the outer bounds of the map
    // protected ArrayList<LatLng> latLngBoundsPoints;
    protected LatLngBounds mLatLngBounds;

    // member variable to represent an array of ParseGeoPoint values, retrieved from the parse cloud
    protected ArrayList<ParseGeoPoint> parseList;

    // All returned LatLng points from the Directions API - used for animation
    protected ArrayList<LatLng> allNonDuplicateLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_activity_display_route);

        markerPoints = new ArrayList<LatLng>();
        // latLngBoundsPoints = new ArrayList<LatLng>();
        // mLatLngBounds = new LatLngBounds();

        // Instantiate allNonDuplicateLatLng ArrayList
        allNonDuplicateLatLng = new ArrayList<LatLng>();

        // Getting reference to SupportMapFragment of the activity_maps
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        // Getting Map for the SupportMapFragment
        mMap = fm.getMap();

        if (mMap != null) {

            plotRoute();

            mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    zoomToViewRoute();
                }
            });

            // Enable MyLocation Button in the Map
            mMap.setMyLocationEnabled(true);

            // set the zoom controls to visible
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

    public void plotRoute() {
        // assign the JSON String value from the passed in intent to a new String variable
        String jsonArray = getIntent().getStringExtra("parseLatLngList");
        JSONArray array = null;

        try {
            // convert String to a JSONArray
            array = new JSONArray(jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONArray arrayPoints = array;

        for (int i = 0; i < arrayPoints.length(); i++) {
            LatLng latLngObject = new LatLng(arrayPoints.optJSONObject(i).optDouble("latitude"), arrayPoints.optJSONObject(i).optDouble("longitude"));

            // Adding new latlng point to the array list
            markerPoints.add(latLngObject);
        }

        // Creating MarkerOptions object
        MarkerOptions marker = new MarkerOptions();

        for (int i = 0; i < markerPoints.size() - 1; i++) {

            /**
             * For the start location, the colour of the marker is GREEN and
             * for the end location, the colour of the marker is RED.
             */
            if (markerPoints.size() == 1) {
                // Add a green marker for the start position.
                marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            }

            if (markerPoints.size() >= 2) {

                LatLng point1 = markerPoints.get(i);
                LatLng point2 = markerPoints.get(i + 1);

                // marker.position(point2).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                // marker.position(point2).visible(true);
                marker.position(point1).visible(false);

                // Getting URL to the Google Directions API
                // send these values to the getDirectionsUrl() method and assign returned value to string variable url
                String url = getDirectionsUrl(point1, point2);
                // create a DownloadTask object - see nested class below
                DownloadTask downloadTask = new DownloadTask();
                // Start downloading json data from Google Directions API
                downloadTask.execute(url);
            }

            // Add a new marker to the map
            // mMap.addMarker(marker);

        }

        // Add the start and finish markers to the map
        addMarkersToMap(markerPoints);
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

    public void acceptRoute() {
        String objectId = getIntent().getStringExtra("myObjectId");
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.CLASS_ROUTES);

        query.getInBackground(objectId, new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    acceptUserRoute(object);
                } else {
                    // there is an error - notify the user
                    AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivityDisplayRoute.this);
                    builder.setMessage(R.string.error_accepting_route_message)
                            .setTitle(R.string.error_accepting_route_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });

        // Send the user back to the main activity right after the message is deleted.
        // Use finish() to close the current activity, returning to the main activity
        finish();
    }

    public void declineRoute() {
        String objectId = getIntent().getStringExtra("myObjectId");
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.CLASS_ROUTES);
        query.getInBackground(objectId, new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
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
        // Use finish() to close the current activity, returning to the main activity
        finish();
    }

    public void acceptUserRoute(ParseObject object) {
        ParseObject route = object;
        // Add the current user to the accepted list
        ArrayList<String> acceptedRecipientIds = new ArrayList<String>();
        acceptedRecipientIds.add(ParseUser.getCurrentUser().getObjectId());
        route.put(ParseConstants.KEY_ACCEPTED_RECIPIENT_IDS, acceptedRecipientIds);

        List<String> ids = route.getList(ParseConstants.KEY_RECIPIENT_IDS);
        if (ids.size() == 1) {
            // Last recipient - delete the route object
            route.deleteInBackground();
        } else {
            // Remove the recipient and save.
            ids.remove(ParseUser.getCurrentUser().getObjectId());

            ArrayList<String> idsToRemove = new ArrayList<String>();
            idsToRemove.add(ParseUser.getCurrentUser().getObjectId());

            route.removeAll(ParseConstants.KEY_RECIPIENT_IDS, idsToRemove);
            route.saveInBackground();
        }
        Toast.makeText(MapsActivityDisplayRoute.this, R.string.success_accept_route, Toast.LENGTH_LONG).show();
    }

    public void deleteUserRoute(ParseObject object) {
        ParseObject route = object;
        List<String> ids = route.getList(ParseConstants.KEY_RECIPIENT_IDS);

        if (ids.size() == 1) {
            // last recipient - delete the route object
            route.deleteInBackground();
        } else {
            // remove the recipient and save
            ids.remove(ParseUser.getCurrentUser().getObjectId());

            ArrayList<String> idsToRemove = new ArrayList<String>();
            idsToRemove.add(ParseUser.getCurrentUser().getObjectId());

            route.removeAll(ParseConstants.KEY_RECIPIENT_IDS, idsToRemove);
            route.saveInBackground();
        }
        Toast.makeText(MapsActivityDisplayRoute.this, R.string.success_decline_route, Toast.LENGTH_LONG).show();
    }

    public void zoomToViewRoute() {
        // assign the JSON String value from the passed in intent to a new String variable
        String jsonArray = getIntent().getStringExtra("parseLatLngBoundsList");
        JSONArray array = null;

        try {
            // convert String to a JSONArray
            array = new JSONArray(jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONArray arrayPoints = array;

        LatLng southWest = new LatLng(arrayPoints.optJSONObject(0).optDouble("latitude"), arrayPoints.optJSONObject(0).optDouble("longitude"));
        LatLng northEast = new LatLng(arrayPoints.optJSONObject(1).optDouble("latitude"), arrayPoints.optJSONObject(1).optDouble("longitude"));

        LatLngBounds latLngBounds = new LatLngBounds(southWest, northEast);
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 60));
    }

    /**
     * Method to add start and finish markers to the route.
     */
    public void addMarkersToMap(List<LatLng> latLngs) {
        if (latLngs.get(0).toString().equals(latLngs.get(latLngs.size() - 1).toString())) {
            mMap.addMarker(new MarkerOptions()
                    .position(latLngs.get(0))
                    .title("Start/Finish")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))
                    .showInfoWindow();
        } else {
            mMap.addMarker(new MarkerOptions()
                    .position(latLngs.get(0))
                    .title("Start")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))
                    .showInfoWindow();
            mMap.addMarker(new MarkerOptions()
                    .position(latLngs.get(latLngs.size() - 1))
                    .title("Finish")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
                    .showInfoWindow();
        }
    }


    /**
     * Creates a url containing the origin and destination points and other parameters
     * which can then be sent as a HTTP request to the Google Directions API to create data in JSON format
     *
     * @param origin
     * @param dest
     * @return
     */
    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String stringOrigin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String stringDestination = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = stringOrigin + "&" + stringDestination;

        // Output format
        String output = "json";

        // Transport mode
        String transMode = "&mode=walking";

        // Building the url to the web service
        // see https://developers.google.com/maps/documentation/directions/#DirectionsRequests
        // eg. https://maps.googleapis.com/maps/api/directions/json?origin=40.722543,-73.998585&destination=40.7577,-73.9857&mode=walking
        // ... would give the points between lower_manhattan and times_square and the directions in between in JSON format
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + transMode;

        return url;
    }


    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Problem downloading url", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }


    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }


    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                for (LatLng point : points) {
                    Log.i(TAG, "Enhanced for loop with each section LatLng point: " + point.toString());
                    if (allNonDuplicateLatLng.size() == 0) {
                        Log.i(TAG, "Adding first point: " + point.toString());
                        allNonDuplicateLatLng.add(point);
                    } else if (!point.toString().equals(allNonDuplicateLatLng.get(allNonDuplicateLatLng.size() - 1).toString())) {
                        Log.i(TAG, "Adding non repeating points: " + point.longitude + " " + point.latitude);
                        allNonDuplicateLatLng.add(point);
                    } else {
                        // not adding point
                    }
                }
                Log.i(TAG, "Enhanced for loop with all LatLng points: " + allNonDuplicateLatLng.toString());

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(6);
                lineOptions.color(Color.BLUE);
            }

            // Drawing polyline in the Google Map for the i-th route
            mMap.addPolyline(lineOptions);
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
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    public void animateRoute() {
        // Keep the screen on while the user is animating the route
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        animator.startAnimation();
    }

    private Animator animator = new Animator();
    private final Handler mHandler = new Handler();

    private float cameraZoomLevel = 0.1f;

    public class Animator implements Runnable {

        // Set the speed of the camera between points to 1.5 second
        private static final int CAMERA_SPEED = 1500;
        private static final int CAMERA_SPEED_TURN = 1000;

        // Linear interpolator to define the rate of change of animation.
        private final Interpolator interpolator = new LinearInterpolator();

        // Set the currentLatLngCheck to the first index of the array.
        private int currentLatLngIndex = 0;

        private float tilt = 90;

        private long startTime = SystemClock.uptimeMillis();

        private LatLng beginLatLng = null;
        private LatLng endLatLng = null;

        private Marker trackingMarker;

        public void startAnimation() {
            if (allNonDuplicateLatLng.size() > 2) {
                animator.initialize();
            }
        }

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
            reset();

            LatLng firstPoint = allNonDuplicateLatLng.get(0);
            LatLng secondPoint = allNonDuplicateLatLng.get(1);

            setupCameraPositionForMovement(firstPoint, secondPoint);
        }

        private void setupCameraPositionForMovement(LatLng firstPoint, LatLng secondPoint) {

            float cameraBearingStart = bearingBetweenLatLngPoints(firstPoint, secondPoint);

            trackingMarker = mMap.addMarker(new MarkerOptions()
                    .position(firstPoint)
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_directions_run_black_24dp)));

            // Set up camera position for the start point.
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(firstPoint) // Set the initial view for the camera.
                    .tilt(tilt)
                    .bearing(cameraBearingStart)  // Set the camera orientation angle for th first point.
                    .zoom(mMap.getCameraPosition().zoom >= 16 ? mMap.getCameraPosition().zoom : 16) // Set the zoom value.
                    .build();   // Create a CameraPosition from the builder.

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),
                    CAMERA_SPEED_TURN,
                    new GoogleMap.CancelableCallback() {

                        @Override
                        public void onFinish() {
                            Log.i(TAG, "Camera finished");
                            animator.reset();
                            Handler handler = new Handler();
                            handler.post(animator);
                        }

                        @Override
                        public void onCancel() {
                            Log.i(TAG, "Cancelling camera animation");
                        }
                    });
        }

        @Override
        public void run() {

            long elapsedTime = SystemClock.uptimeMillis() - startTime;
            double timeInterpolator = interpolator.getInterpolation((float) elapsedTime / CAMERA_SPEED);

            double lat = timeInterpolator * endLatLng.latitude + (1 - timeInterpolator) * beginLatLng.latitude;
            double lng = timeInterpolator * endLatLng.longitude + (1 - timeInterpolator) * beginLatLng.longitude;
            LatLng newPosition = new LatLng(lat, lng);

            trackingMarker.setPosition(newPosition);

            if (timeInterpolator < 1) {
                mHandler.postDelayed(this, 16);
            } else {

                if (currentLatLngIndex < allNonDuplicateLatLng.size() - 2) {

                    currentLatLngIndex++;

                    endLatLng = getEndLatLng();
                    beginLatLng = getBeginLatLng();

                    startTime = SystemClock.uptimeMillis();

                    LatLng firstPoint = getBeginLatLng();
                    LatLng nextPoint = getEndLatLng();

                    float cameraBearing = bearingBetweenLatLngPoints(firstPoint, nextPoint);

                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(nextPoint)
                            .bearing(cameraBearing)
                            .tilt(tilt)
                            .zoom(mMap.getCameraPosition().zoom)
                            .build();

                    Log.i(TAG, (currentLatLngIndex + 1) + " of " + allNonDuplicateLatLng.size() + " - bearing: " + cameraBearing + " / " + nextPoint);

                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),
                            CAMERA_SPEED_TURN,
                            null);

                    startTime = SystemClock.uptimeMillis();
                    mHandler.postDelayed(animator, 16);

                } else {
                    currentLatLngIndex++;
                    // Zoom out to view route once the camera finishes animating.
                    zoomToViewRoute();
                    // Remove the runner icon
                    trackingMarker.remove();
                    // Remove any callbacks to the Handler object
                    mHandler.removeCallbacks(animator);
                }
            }
        }

        private LatLng getEndLatLng() {
            return allNonDuplicateLatLng.get(currentLatLngIndex + 1);
        }

        private LatLng getBeginLatLng() {
            return allNonDuplicateLatLng.get(currentLatLngIndex);
        }

        /**
         * Method to
         *
         * @param latLng
         * @return
         */
        private Location convertLatLngToLocation(LatLng latLng) {
            Location location = new Location("someLocation");
            location.setLatitude(latLng.latitude);
            location.setLongitude(latLng.longitude);
            return location;
        }

        private float bearingBetweenLatLngPoints(LatLng start, LatLng end) {
            Location startLocation = convertLatLngToLocation(start);
            Location endLocation = convertLatLngToLocation(end);
            return startLocation.bearingTo(endLocation);
        }

    }
}
