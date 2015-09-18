package com.khackett.runmate;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.khackett.runmate.model.Route;
import com.khackett.runmate.ui.AddRouteDetailsActivity;
import com.khackett.runmate.utils.DirectionsUtility;

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

public class MapsActivityDirectionsMultiple extends FragmentActivity implements GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, View.OnClickListener {

    public static final String TAG = MapsActivityDirectionsMultiple.class.getSimpleName();

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private List<Polyline> polylines;

    private Route mRoute;

    private ArrayList<LatLng> allLatLngPoints;

    private DirectionsUtility directionsUtility;

    // Member variable for the UI buttons
    private ImageButton mButtonSend;
    private ImageButton mButtonUndo;
    private ImageButton mButtonCompleteLoop;
    private TextView mDistanceCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the layout file for this fragment activity
        setContentView(R.layout.activity_maps_activity_directions_multiple);

        // Instantiate array lists
        polylines = new ArrayList<Polyline>();
        allLatLngPoints = new ArrayList<LatLng>();

        mRoute = new Route();

        directionsUtility = new DirectionsUtility();

        // Getting reference to SupportMapFragment of the activity_maps
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        // Getting Map for the SupportMapFragment
        mMap = fm.getMap();

        if (mMap != null) {
            // Enable MyLocation Button in the Map
            mMap.setMyLocationEnabled(true);
            // Set the zoom controls to visible
            mMap.getUiSettings().setZoomControlsEnabled(true);
            // Setting onClick event listener for the map
            mMap.setOnMapClickListener(this);
            // Setting onClickLong event listener for the map
            mMap.setOnMapLongClickListener(this);
        }

        // Set up member variables for each UI component
        mButtonSend = (ImageButton) findViewById(R.id.btn_send);
        mButtonUndo = (ImageButton) findViewById(R.id.btn_undo);
        mButtonCompleteLoop = (ImageButton) findViewById(R.id.btn_complete_loop);
        mDistanceCount = (TextView) findViewById(R.id.distanceCount);

        // Register buttons with the listener
        mButtonSend.setOnClickListener(this);
        mButtonUndo.setOnClickListener(this);
        mButtonCompleteLoop.setOnClickListener(this);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        // Plot tapped point on map
        plotPoint(latLng);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        // Map will be cleared on long click
        mMap.clear();
        // Removes all marker points from the route
        mRoute.getMarkerPoints().clear();
        // Removes all LatLng points from the route
        mRoute.getMinMaxLatLngArrayList().clear();
        // Removes all LatLng sections from the route
        mRoute.getMinMaxLatLngSectionArrayList().clear();

        // Clear the distance array and update UI
        mRoute.clearTotalDistance();
        double routeDistance = mRoute.getTotalDistance();
        mDistanceCount.setText(routeDistance / 1000 + "km");
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send:
                sendRoute();
                break;
            case R.id.btn_undo:
                undoClick();
                break;
            case R.id.btn_complete_loop:
                completeLoop();
                break;
            default:
                Log.i(TAG, "Problem with input");
        }
    }

    public void sendRoute() {
        // First ensure that there are at least 2 points in the ArrayList
        if (!markerCountValidCheck()) {
            // alert user to add more points
        } else {
            // Declare intent to capture a route
            Intent createRouteIntent = new Intent(MapsActivityDirectionsMultiple.this, AddRouteDetailsActivity.class);
            // Using android.location to extend Parcelable in order to create and store the LatLng values in an arrayList
            createRouteIntent.putParcelableArrayListExtra("markerPoints", mRoute.getMarkerPoints());

            createRouteIntent.putParcelableArrayListExtra("allLatLngPoints", allLatLngPoints);

            // Add the min and max lat and long points to the intent object
            createRouteIntent.putExtra("boundaryPoints", mRoute.getLatLngBounds());

            // Add the total distance of the route to the intent object
            createRouteIntent.putExtra("routeDistance", mRoute.getTotalDistance());

            // Add the creation type of the route to the intent object
            createRouteIntent.putExtra("routeCreationMethod", "DIRECTIONS_API");

            // Start RouteRecipientsActivity in order to choose recipients
            startActivity(createRouteIntent);
        }
    }

    public void undoClick() {
        if (mRoute.getMarkerPoints().size() <= 1) {
            // Alert user that they cannot trigger the undo action any more
            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivityDirectionsMultiple.this);
            // Set the message title and text for the button - use String resources for all of these values
            // Chain the methods together as they are all referencing the builder object
            builder.setMessage(R.string.route_undo_error_message)
                    .setTitle(R.string.route_undo_error_title)
                            // Button to dismiss the dialog.  Set the listener to null as we only want to dismiss the dialog
                            // ok is gotten from android resources
                    .setPositiveButton(android.R.string.ok, null);
            // We need to create a dialog and show it
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            // Create variable for the 2nd last point clicked and assign value form markerPoints array list
            LatLng lastPoint;
            lastPoint = mRoute.getMarkerPoints().get(mRoute.getMarkerPoints().size() - 2);

            // Animate camera to centre on the previously touched position
            mMap.animateCamera(CameraUpdateFactory.newLatLng(lastPoint));

            // Remove polyline object from the map
            for (Polyline line : polylines) {
                if (polylines.get(polylines.size() - 1).equals(line)) {
                    line.remove();
                    polylines.remove(line);
                }
            }

            // Remove last value from the markerPoints array list
            mRoute.undoLastMarkerPoint();
            // Remove last value from the minMaxLatLngSectionArrayList array list
            mRoute.undoLastMinMaxLatLng();
            // Remove the last distance added to the distance array
            mRoute.undoLastRouteDistance();
            // Update the distance text and output new value to UI
            double routeDistance = mRoute.getTotalDistance();
            mDistanceCount.setText(routeDistance / 1000 + "km");
        }
    }

    public void completeLoop() {
        // Check that the minimum number of points have been selected
        if (!markerCountValidCheck()) {
            // alert user to add more points
        } else {
            // Complete loop by plotting the first point plotted
            plotPoint(mRoute.getMarkerPoints().get(0));
            zoomToArea();
        }
    }

    private void zoomToArea() {
        LatLngBounds latLngBounds = mRoute.getLatLngBounds();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 60));
    }

    private boolean markerCountValidCheck() {
        // Ensure that there are at least 2 points in the ArrayList
        if (mRoute.getMarkerPoints().size() <= 1) {
            // If not, display a message to the user - use a dialog so that some user interaction is required before it disappears
            // Use Builder to build and configure the alert
            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivityDirectionsMultiple.this);
            // Set the message title and text for the button - use String resources for all of these values
            // Chain the methods together as they are all referencing the builder object
            builder.setMessage(R.string.route_creation_error_message)
                    .setTitle(R.string.route_creation_error_title)
                    .setPositiveButton(android.R.string.ok, null);
            // We need to create a dialog and show it
            AlertDialog dialog = builder.create();
            dialog.show();
            return false;
        } else {
            return true;
        }
    }

    private void plotPoint(LatLng point) {
        // Animate camera to centre on touched position
        mMap.animateCamera(CameraUpdateFactory.newLatLng(point));

        // Adding new latlng point to the array list
        mRoute.setMarkerPoint(point);

        // Creating MarkerOptions object
        MarkerOptions marker = new MarkerOptions();

        // Sets the location for the marker to the touched point
        marker.position(point);

        // For the start location, the colour of the marker is GREEN
        if (mRoute.getMarkerPoints().size() == 1) {
            // Place a green marker for the start position
            marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        }

        if (mRoute.getMarkerPoints().size() >= 2) {
            LatLng point1 = mRoute.getMarkerPoint1();
            LatLng point2 = mRoute.getMarkerPoint2();

            // Hide the next marker for the next position selected
            marker.position(point1).visible(false);

            // Creating URL to send to the Google Directions API.
            String url = directionsUtility.getDirectionsUrl(point1, point2);
            // Create a DownloadTask object - see nested class below
            DownloadURLTask downloadURLTask = new DownloadURLTask();
            // Start downloading json data from Google Directions API
            downloadURLTask.execute(url);
        }

        // Add a new marker to the map
        mMap.addMarker(marker);
    }

    // Asynchronous task to fetch JSON data via the passed in URL
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
                Log.d("Background task error: ", e.toString());
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

            // Create a new ParseDistanceTask object and invoke thread for parsing JSON data
            ParseDistanceTask parseDistanceTask = new ParseDistanceTask();
            parseDistanceTask.execute(urlJSONData);
        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParseLatLngValuesTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);

                // Start parsing data
                routes = directionsUtility.parseJSONObject(jObject);
            } catch (Exception e) {
                Log.d("Background task error: ", e.toString());
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {

            ArrayList<LatLng> sectionLatLng = null;
            PolylineOptions lineOptions = null;
            // Traversing through all the routes
            for (int i = 0; i < routes.size(); i++) {
                lineOptions = new PolylineOptions();
                sectionLatLng = new ArrayList<LatLng>();

                // Get the value of the routes object and assign to route
                List<HashMap<String, String>> route = routes.get(i);
                // Get the lat and lng values from each point in the route
                for (int j = 0; j < route.size(); j++) {
                    HashMap<String, String> point = route.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    // Create a LatLng object and assign the extracted values
                    LatLng position = new LatLng(lat, lng);

                    // Add to th sections array
                    sectionLatLng.add(position);
                    // Set the min/max lat/lng values for the Route object
                    mRoute.setMinMaxLatLng(position);
                    // Add all the points in the route to the PolylineOptions object
                    lineOptions.add(position).width(6).color(Color.BLUE);
                }

                // Set the min/max lat/lng values for the Route object
                mRoute.setMinMaxLatLngSectionArrayList(sectionLatLng);

                // Iterate through the section array list and add to member variable allLatLngPoints
                for (LatLng enhancedPoint : sectionLatLng) {
                    allLatLngPoints.add(enhancedPoint);
                }
                Log.i(TAG, "Enhanced for loop with all LatLng points: " + allLatLngPoints.toString());

                // Add Polyline to list and draw on map
                polylines.add(mMap.addPolyline(lineOptions));
            }
        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParseDistanceTask extends AsyncTask<String, Integer, Double> {
        // Parsing the data in the non-ui thread
        @Override
        protected Double doInBackground(String... jsonData) {
            JSONObject jsonObject;
            Double distance = null;
            try {
                jsonObject = new JSONObject(jsonData[0]);
                // Get all routes from the routes array
                JSONArray array = jsonObject.getJSONArray("routes");
                // Get the first route in the JSON object
                JSONObject routes = array.getJSONObject(0);
                // Get all of the legs from the route and add to legs array
                JSONArray legs = routes.getJSONArray("legs");
                // Get the first leg in the JSON object
                JSONObject steps = legs.getJSONObject(0);
                // Get the distance element
                JSONObject distanceJSON = steps.getJSONObject("distance");
                // Get the value from the distance element and assign to distance
                distance = Double.parseDouble(distanceJSON.getString("value"));
            } catch (JSONException e) {
                Log.d("Background task error: ", e.toString());
            }
            return distance;
        }

        // Executes in UI thread, after the parsing process.
        @Override
        protected void onPostExecute(Double distance) {
            mRoute.setTotalDistance(distance);
            double routeDistance = mRoute.getTotalDistance();
            Log.i(TAG, "Total Distance calculated in AsyncTask in m = " + routeDistance);
            mDistanceCount.setText(routeDistance / 1000 + "km");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that a map not already instantiated.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

}