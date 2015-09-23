package com.khackett.runmate;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.khackett.runmate.model.Route;
import com.khackett.runmate.ui.AddRouteDetailsActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to plot a Route on a map manually
 */
public class MapsActivityManualPolyline extends FragmentActivity implements GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, View.OnClickListener {

    // Simple class TAG for logcat output
    public static final String TAG = MapsActivityManualPolyline.class.getSimpleName();

    // Member variables for Map and Route components
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    // An array list to contain all of the points tapped on the map.
    private ArrayList<LatLng> allLatLngPoints;
    private List<Polyline> polylines;
    private PolylineOptions polylineOptions;
    private Route mManualRoute;

    // Member variables for the UI components
    private ImageButton mButtonSend;
    private ImageButton mButtonUndo;
    private ImageButton mButtonCompleteLoop;
    private TextView mDistanceCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the layout file for this fragment activity
        setContentView(R.layout.maps_activity_manual_polyline);
        setUpMapIfNeeded();

        // Instantiate ArrayLists
        allLatLngPoints = new ArrayList<LatLng>();
        polylines = new ArrayList<Polyline>();

        // Instantiate Route object
        mManualRoute = new Route();

        // Getting reference to the SupportMapFragment of activity_maps.xml
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        // Setting the GoogleMap object to the fragment
        mMap = fm.getMap();

        if (mMap != null) {
            // Enable MyLocation Button in the Map - Sets a blue marker to the users location
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
                // undoClick();
                break;
            case R.id.btn_complete_loop:
                // completeLoop();
                break;
            default:
                Log.i(TAG, "Problem with input");
        }
    }


//    public void undoClick() {
//        if (mManualRoute.getMarkerPoints().size() <= 1) {
//            // Alert user that they cannot trigger the undo action any more
//            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivityManualPolyline.this);
//            // Set the message title and text for the button - use String resources for all of these values
//            // Chain the methods together as they are all referencing the builder object
//            builder.setMessage(R.string.route_undo_error_message)
//                    .setTitle(R.string.route_undo_error_title)
//                            // Button to dismiss the dialog.  Set the listener to null as we only want to dismiss the dialog
//                            // ok is gotten from android resources
//                    .setPositiveButton(android.R.string.ok, null);
//            // We need to create a dialog and show it
//            AlertDialog dialog = builder.create();
//            dialog.show();
//        } else {
//            // Create variable for the 2nd last point clicked and assign value form markerPoints array list
//            LatLng lastPoint;
//            lastPoint = mManualRoute.getMarkerPoints().get(mManualRoute.getMarkerPoints().size() - 2);
//
//            // Animate camera to centre on the previously touched position
//            Log.i(TAG, "Centering camera to previous position at " + lastPoint.toString());
//            mMap.animateCamera(CameraUpdateFactory.newLatLng(lastPoint));
//
//            // Remove polyline object from the map
//            for (Polyline line : polylines) {
//                if (polylines.get(polylines.size() - 1).equals(line)) {
//                    line.remove();
//                    polylines.remove(line);
//                }
//            }
//
//            // Remove last value from the markerPoints array list
//            mManualRoute.undoLastMarkerPoint();
//            mManualRoute.undoLastMinMaxLatLng();
//
//            // Update the distance text and output new value to UI
//            double routeDistance = mManualRoute.calculateDistanceBetweenLocations(allLatLngPoints);
//            mDistanceCount.setText(routeDistance / 1000 + "km");
//        }
//    }

    /**
     * Called when the user makes a tap gesture on the map, but only if none of the overlays of
     * the map handled the gesture.  Implementations of this method are always invoked on the main thread.
     *
     * @param point
     */
    @Override
    public void onMapClick(LatLng point) {
        // Plot a point on the map using the returned LatLng value
        plotPoint(point);
    }

    /**
     * Method to plot a point on the map
     *
     * @param point
     */
    public void plotPoint(LatLng point) {
        // Animate camera to centre on touched position
        mMap.animateCamera(CameraUpdateFactory.newLatLng(point));

        // Adding new LatLng point to the array list
        mManualRoute.setMarkerPoint(point);

        // Creating MarkerOptions object
        MarkerOptions marker = new MarkerOptions();

        // Sets the location for the marker to the touched point
        marker.position(point);

        // For the start location, the colour of the marker is GREEN
        if (mManualRoute.getMarkerPoints().size() == 1) {
            // Place a green marker for the start position
            marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            mMap.addMarker(marker);
        }

        // Initialising the polyline in the map and setting some values
        polylineOptions = new PolylineOptions()
                .color(Color.BLUE)
                .width(6);

        // Adding the tapped point to the ArrayList
        allLatLngPoints.add(point);
        // Adding the tapped point to the section ArrayList in Route object
        mManualRoute.setMinMaxLatLngSectionArrayList(allLatLngPoints);

        // Setting points of polyline
        polylineOptions.addAll(allLatLngPoints);

        // Adding the polyline to the map
        mMap.addPolyline(polylineOptions);

        // Add Polyline to list and draw on map
        polylines.add(mMap.addPolyline(polylineOptions));

        // Output the calculated distance to the UI
        double routeDistance = mManualRoute.calculateDistanceBetweenLocations(allLatLngPoints);
        String routeDistanceString = String.format("%.3f km", routeDistance / 1000);
        mDistanceCount.setText(routeDistanceString);
    }

    /**
     * After long clicking the map, all markers are cleared
     *
     * @param point
     */
    @Override
    public void onMapLongClick(LatLng point) {
        // Removes all markers, polylines, polygons, overlays, etc from the map.
        mMap.clear();
        // Clears all values from the arraylist
        allLatLngPoints.clear();
        // Removes all marker points from the map
        mManualRoute.getMarkerPoints().clear();
        // Removes all LatLng points from the map
        mManualRoute.getMinMaxLatLngArrayList().clear();
        mManualRoute.getMinMaxLatLngSectionArrayList().clear();

        double routeDistance = mManualRoute.calculateDistanceBetweenLocations(allLatLngPoints);
        mDistanceCount.setText(routeDistance / 1000 + "km");
    }

    /**
     * Method to send a Route to a user
     */
    public void sendRoute() {
        // First ensure that there are at least 2 points in the ArrayList
        if (!markerCountValidCheck()) {
            // If not, alert user to add more points
        } else {
            // Declare intent
            Intent createRouteIntent = new Intent(MapsActivityManualPolyline.this, AddRouteDetailsActivity.class);
            // Using android.location to extend Parcelable in order to create and store the LatLng values in an arrayList
            createRouteIntent.putParcelableArrayListExtra("markerPoints", mManualRoute.getMarkerPoints());
            // Using android.location to extend Parcelable in order to create and store the LatLng values in an arrayList
            createRouteIntent.putParcelableArrayListExtra("allLatLngPoints", allLatLngPoints);
            // Add the min and max lat and long points to the intent object
            createRouteIntent.putExtra("boundaryPoints", mManualRoute.getLatLngBounds());
            // Add the total distance of the route to the intent object
            createRouteIntent.putExtra("routeDistance", mManualRoute.calculateDistanceBetweenLocations(allLatLngPoints));
            // Add the creation type of the route to the intent object
            createRouteIntent.putExtra("routeCreationMethod", "MANUAL");
            // Start AddRouteDetailsActivity in order to add extra Route details
            startActivity(createRouteIntent);
        }
    }

    /**
     * Method to check that the user has added at least two points to the Route before they can send.
     *
     * @return false if there are less than 2 points plotted; true if there are 2 or more points plotted.
     */
    private boolean markerCountValidCheck() {
        // Ensure that there are at least 2 points in the ArrayList
        if (mManualRoute.getMarkerPoints().size() <= 1) {
            // If not, display a message to the user - use a dialog so that some user interaction is required before it disappears
            // Use Builder to build and configure the alert
            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivityManualPolyline.this);
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


    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Checks if a map fragment has already been created.
     * If not, then it creates one and calls the setUpMap() method.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        // Set the app to locate the users current location
        mMap.setMyLocationEnabled(true);
    }

}