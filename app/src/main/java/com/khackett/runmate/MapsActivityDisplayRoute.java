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
                // place a green marker for the start position
                marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            }

            if (markerPoints.size() >= 2) {
//                    LatLng point1 = markerPoints.get(markerPoints.size() - 2);
//                    LatLng point2 = markerPoints.get(markerPoints.size() - 1);

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
                System.out.println("Problem with input");
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

    private static final int CAMERA_SPEED = 1000;   // Set the speed of the camera between points to 1 second

    private static int currentLatLngCheck;

    private float cameraZoomLevel;

    public void animateRoute() {

        cameraZoomLevel = 0.1f;

        // Set the currentLatLngCheck to the first index of the array.
        currentLatLngCheck = -1;

        // Zoom the camera in the
        mMap.animateCamera(CameraUpdateFactory.zoomTo(mMap.getCameraPosition().zoom + cameraZoomLevel),
                CAMERA_SPEED,
                cancelableCallback);

        animator.startAnimation(false);
    }

    GoogleMap.CancelableCallback cancelableCallback = new GoogleMap.CancelableCallback() {

        @Override
        public void onCancel() {
            Log.i(TAG, "onCancel() action");
        }

        @Override
        public void onFinish() {

            if (++currentLatLngCheck < allNonDuplicateLatLng.size()) {
                float targetBearing;
                targetBearing = bearingBetweenLatLngPoints(mMap.getCameraPosition().target, allNonDuplicateLatLng.get(currentLatLngCheck));

                LatLng targetLatLng = allNonDuplicateLatLng.get(currentLatLngCheck);

                Log.i(TAG, (currentLatLngCheck + 1) + " of " + allNonDuplicateLatLng.size() + " - bearing: " + targetBearing + " / " + targetLatLng);

                // Set up a camera position and define where it should be pointed at.
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(targetLatLng)                   // Set where the camera should go.
                        .tilt(currentLatLngCheck < allNonDuplicateLatLng.size() - 1 ? 90 : 0) // Set the tilt of the camera.
                        .bearing(targetBearing)                 // Set the camera orientation angle.
                        .zoom(mMap.getCameraPosition().zoom)    // Set the zoom value.
                        .build();                               // Create a CameraPosition from the builder.

                // Pass the CameraPosition to the Google Map animateCamera() method.
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),
                        CAMERA_SPEED,
                        // Repeat process if there are still points in allNonDuplicateLatLng array
                        // to animate through
                        currentLatLngCheck == allNonDuplicateLatLng.size() - 1 ?
                                finalCancelableCallback : cancelableCallback);
            }
        }
    };

    GoogleMap.CancelableCallback finalCancelableCallback = new GoogleMap.CancelableCallback() {
        // onFinish() called after the animation has finished
        @Override
        public void onFinish() {
            // Zoom out to view route once the camera finishes animating.
            zoomToViewRoute();
        }

        // onCancel() called whenever the animation has stopped.
        @Override
        public void onCancel() {

        }
    };

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

    public float bearingBetweenLatLngPoints(LatLng start, LatLng end) {
        Location startLocation = convertLatLngToLocation(start);
        Location endLocation = convertLatLngToLocation(end);
        return startLocation.bearingTo(endLocation);
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

        // transport mode
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
            MarkerOptions markerOptions = new MarkerOptions();

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


//    /**
//     * method to convert an array of ParseGeoPoint elements to an array of LatLng elements
//     *
//     * @param parseList
//     * @return
//     */
//    protected ArrayList convertParseGeoPointToLatLngArray(ArrayList<ParseGeoPoint> parseList) {
//        markerPoints = new ArrayList<LatLng>();
//        for (ParseGeoPoint item : parseList) {
//            LatLng latLngPoint = new LatLng(item.getLatitude(), item.getLongitude());
//            markerPoints.add(latLngPoint);
//        }
//        return markerPoints;
//    }


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
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }


    private Animator animator = new Animator();
    private final Handler mHandler = new Handler();

    public class Animator implements Runnable {

        private static final int ANIMATE_SPEEED = 1500;
        private static final int ANIMATE_SPEEED_TURN = 1000;
        private static final int BEARING_OFFSET = 20;

        private final Interpolator interpolator = new LinearInterpolator();

        int currentIndex = 0;

        float tilt = 90;

        long start = SystemClock.uptimeMillis();

        LatLng endLatLng = null;
        LatLng beginLatLng = null;

        boolean showPolyline = false;

        private Marker trackingMarker;

        public void startAnimation(boolean showPolyLine) {
            if (allNonDuplicateLatLng.size() > 2) {
                animator.initialize(showPolyLine);
            }
        }

        public void stopAnimation() {
            animator.stop();
        }

        public void reset() {
            // resetMarkers();
            start = SystemClock.uptimeMillis();
            currentIndex = 0;
            endLatLng = getEndLatLng();
            beginLatLng = getBeginLatLng();

        }

        public void stop() {
            trackingMarker.remove();
            mHandler.removeCallbacks(animator);
        }

        public void initialize(boolean showPolyLine) {
            reset();
            this.showPolyline = showPolyLine;

            // highLightMarker(0);

            if (showPolyLine) {
                polyLine = initializePolyLine();
            }

            // We first need to put the camera in the correct position for the first run (we need 2 markers for this).....
            LatLng markerPos = allNonDuplicateLatLng.get(0);
            LatLng secondPos = allNonDuplicateLatLng.get(1);

            setupCameraPositionForMovement(markerPos, secondPos);
        }

        private void setupCameraPositionForMovement(LatLng markerPos, LatLng secondPos) {

            float bearing = bearingBetweenLatLngPoints(markerPos, secondPos);

            trackingMarker = mMap.addMarker(new MarkerOptions()
                    .position(markerPos)
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_directions_run_black_24dp)));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(markerPos)
                    .bearing(bearing + BEARING_OFFSET)
                    .tilt(90)
                    .zoom(mMap.getCameraPosition().zoom >= 16 ? mMap.getCameraPosition().zoom : 16)
                    .build();

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),
                    ANIMATE_SPEEED_TURN,
                    new GoogleMap.CancelableCallback() {

                        @Override
                        public void onFinish() {
                            System.out.println("finished camera");
                            animator.reset();
                            Handler handler = new Handler();
                            handler.post(animator);
                        }

                        @Override
                        public void onCancel() {
                            System.out.println("cancelling camera");
                        }
                    }
            );
        }

        private Polyline polyLine;
        private PolylineOptions rectOptions = new PolylineOptions();


        private Polyline initializePolyLine() {
            //polyLinePoints = new ArrayList<LatLng>();
            rectOptions.add(allNonDuplicateLatLng.get(0));
            return mMap.addPolyline(rectOptions);
        }

        /**
         * Add the marker to the polyline.
         */
        private void updatePolyLine(LatLng latLng) {
            List<LatLng> points = polyLine.getPoints();
            points.add(latLng);
            polyLine.setPoints(points);
        }


        @Override
        public void run() {

            long elapsed = SystemClock.uptimeMillis() - start;
            double t = interpolator.getInterpolation((float) elapsed / ANIMATE_SPEEED);

            double lat = t * endLatLng.latitude + (1 - t) * beginLatLng.latitude;
            double lng = t * endLatLng.longitude + (1 - t) * beginLatLng.longitude;
            LatLng newPosition = new LatLng(lat, lng);

            trackingMarker.setPosition(newPosition);

            if (showPolyline) {
                updatePolyLine(newPosition);
            }

            // It's not possible to move the marker + center it through a cameraposition update while another camerapostioning was already happening.
            //navigateToPoint(newPosition,tilt,bearing,currentZoom,false);
            //navigateToPoint(newPosition,false);

            if (t < 1) {
                mHandler.postDelayed(this, 16);
            } else {

                System.out.println("Move to next marker.... current = " + currentIndex + " and size = " + allNonDuplicateLatLng.size());
                // imagine 5 elements -  0|1|2|3|4 currentindex must be smaller than 4
                if (currentIndex < allNonDuplicateLatLng.size() - 2) {

                    currentIndex++;

                    endLatLng = getEndLatLng();
                    beginLatLng = getBeginLatLng();


                    start = SystemClock.uptimeMillis();

                    LatLng begin = getBeginLatLng();
                    LatLng end = getEndLatLng();

                    float bearingL = bearingBetweenLatLngPoints(begin, end);

                    // highLightMarker(currentIndex);

                    CameraPosition cameraPosition =
                            new CameraPosition.Builder()
                                    .target(end) // changed this...
                                    .bearing(bearingL + BEARING_OFFSET)
                                    .tilt(tilt)
                                    .zoom(mMap.getCameraPosition().zoom)
                                    .build();


                    mMap.animateCamera(
                            CameraUpdateFactory.newCameraPosition(cameraPosition),
                            ANIMATE_SPEEED_TURN,
                            null
                    );

                    start = SystemClock.uptimeMillis();
                    mHandler.postDelayed(animator, 16);

                } else {
                    currentIndex++;
                    stopAnimation();
                }

            }
        }

        private LatLng getEndLatLng() {
            return allNonDuplicateLatLng.get(currentIndex + 1);
        }

        private LatLng getBeginLatLng() {
            return allNonDuplicateLatLng.get(currentIndex);
        }

    }

}
