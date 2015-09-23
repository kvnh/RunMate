package com.khackett.runmate.model;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to represent a Route object in the system
 * Created by KHackett on 24/08/15.
 */
public class Route {

    // Declare member variables
    private double mLatitudeMax;
    private double mLongitudeMax;
    private double mLatitudeMin;
    private double mLongitudeMin;
    private ArrayList<Double> totalDistanceArray;
    private ArrayList<LatLng> markerPoints;
    private ArrayList<LatLng> minMaxLatLngArrayList;
    private ArrayList<ArrayList<LatLng>> minMaxLatLngSectionArrayList;

    /**
     * Constructor to initialise the member variables
     */
    public Route() {
        markerPoints = new ArrayList<LatLng>();
        totalDistanceArray = new ArrayList<Double>();
        minMaxLatLngArrayList = new ArrayList<LatLng>();
        minMaxLatLngSectionArrayList = new ArrayList<ArrayList<LatLng>>();
        mLatitudeMax = 0;
        mLongitudeMax = 0;
        mLatitudeMin = 0;
        mLongitudeMin = 0;
    }

    /**
     * Sets the total distance of the route
     *
     * @param distance
     */
    public void setTotalDistance(double distance) {
        totalDistanceArray.add(distance);
    }

    /**
     * Gets the total distance of the route
     *
     * @return the total distance of the route
     */
    public double getTotalDistance() {
        double totalDistanceSum = 0;
        for (Double step : totalDistanceArray) {
            totalDistanceSum += step;
        }
        return totalDistanceSum;
    }

    /**
     * Clear the total distance array
     */
    public void clearTotalDistance() {
        totalDistanceArray.clear();
    }

    /**
     * Removes the last section added to the route
     */
    public void undoLastRouteDistance() {
        totalDistanceArray.remove(totalDistanceArray.size() - 1);
    }

    /**
     * Gets the location points plotted by the user
     *
     * @return an arraylist of LatLng points
     */
    public ArrayList<LatLng> getMarkerPoints() {
        return markerPoints;
    }

    /**
     * Sets the location points plotted by the user
     *
     * @param markerPoint
     */
    public void setMarkerPoint(LatLng markerPoint) {
        markerPoints.add(markerPoint);
    }

    /**
     * Removes the last location point added to the route
     */
    public void undoLastMarkerPoint() {
        markerPoints.remove(markerPoints.size() - 1);
    }

    /**
     * Gets the first location point to send to the Directions API
     *
     * @return
     */
    public LatLng getMarkerPoint1() {
        return markerPoints.get(markerPoints.size() - 2);
    }

    /**
     * Gets the second location point to send to the Directions API
     *
     * @return
     */
    public LatLng getMarkerPoint2() {
        return markerPoints.get(markerPoints.size() - 1);
    }

    /**
     * Sets the LatLng points stored in the route
     *
     * @param minMaxLatLng
     */
    public void setMinMaxLatLng(LatLng minMaxLatLng) {
        minMaxLatLngArrayList.add(minMaxLatLng);
    }

    /**
     * Gets every location point stored in the route
     *
     * @return an array of every location point created in the route
     */
    public ArrayList<LatLng> getMinMaxLatLngArrayList() {
        ArrayList<LatLng> innerList = new ArrayList<LatLng>();
        for (ArrayList<LatLng> minMaxLatLngSection : minMaxLatLngSectionArrayList) {
            for (LatLng minMaxLatLng : minMaxLatLngSection) {
                innerList.add(minMaxLatLng);
            }
        }
        minMaxLatLngArrayList = innerList;
        return minMaxLatLngArrayList;
    }

    /**
     * Sets the section of LatLng points to an array list
     *
     * @param minMaxLatLngSectionArrayList
     */
    public void setMinMaxLatLngSectionArrayList(ArrayList<LatLng> minMaxLatLngSectionArrayList) {
        this.minMaxLatLngSectionArrayList.add(minMaxLatLngSectionArrayList);
    }

    /**
     * Get the array list of array list for each section plotted on the route
     *
     * @return the list for each section plotted on the route
     */
    public ArrayList<ArrayList<LatLng>> getMinMaxLatLngSectionArrayList() {
        return minMaxLatLngSectionArrayList;
    }

    public void undoLastMinMaxLatLng() {
        minMaxLatLngSectionArrayList.remove(minMaxLatLngSectionArrayList.size() - 1);
    }

    /**
     * Gets the maximum latitude point in the route
     *
     * @return the maximum latitude point
     */
    public double getLatitudeMax() {
        mLatitudeMax = getMinMaxLatLngArrayList().get(0).latitude;
        for (LatLng position : getMinMaxLatLngArrayList()) {
            if (mLatitudeMax < position.latitude)
                mLatitudeMax = position.latitude;
        }
        return mLatitudeMax;
    }

    /**
     * Gets the maximum longitude point in the route
     *
     * @return the maximum longitude point
     */
    public double getLongitudeMax() {
        mLongitudeMax = getMinMaxLatLngArrayList().get(0).longitude;
        for (LatLng position : getMinMaxLatLngArrayList()) {
            if (mLongitudeMax < position.longitude)
                mLongitudeMax = position.longitude;
        }
        return mLongitudeMax;
    }

    /**
     * Gets the minimum latitude point in the route
     *
     * @return the minimum latitude point
     */
    public double getLatitudeMin() {
        mLatitudeMin = getMinMaxLatLngArrayList().get(0).latitude;
        for (LatLng position : getMinMaxLatLngArrayList()) {
            if (mLatitudeMin > position.latitude)
                mLatitudeMin = position.latitude;
        }
        return mLatitudeMin;
    }

    /**
     * Gets the minimum longitude point in the route
     *
     * @return the minimum longitude point
     */
    public double getLongitudeMin() {
        mLongitudeMin = getMinMaxLatLngArrayList().get(0).longitude;
        for (LatLng position : getMinMaxLatLngArrayList()) {
            if (mLongitudeMin > position.longitude)
                mLongitudeMin = position.longitude;
        }
        return mLongitudeMin;
    }

/**
 * Gets the maximum and minimum latitude and longitude points of the route
 *
 * @return the LatLngBounds value of a route
 */
public LatLngBounds getLatLngBounds() {

    // Declare a LatLngBounds object
    LatLngBounds latLngBounds;

    // Get min and max lat and lng values from the entire list of points
    mLatitudeMin = getLatitudeMin();
    mLongitudeMin = getLongitudeMin();
    mLatitudeMax = getLatitudeMax();
    mLongitudeMax = getLongitudeMax();

    // Assign these to southWest and northEast LatLng values
    LatLng southWest = new LatLng(mLatitudeMin, mLongitudeMin);
    LatLng northEast = new LatLng(mLatitudeMax, mLongitudeMax);

    // Assign to the LatLngBounds object and return value
    latLngBounds = new LatLngBounds(southWest, northEast);
    return latLngBounds;
}

    /**
     * Calculates the straight line distance between two locations on a map
     *
     * @param latLngPoints
     * @return - straight line distance between two locations on a map
     */
    public double calculateDistanceBetweenLocations(ArrayList<LatLng> latLngPoints) {

        // Create two Location objects
        Location locationA = new Location("locationA");
        Location locationB = new Location("locationB");

        // Create float to represent the total distance between Locations
        float totalDistance = 0;

        // Iterate through the ArrayList to get the latitude and longitude values.
        for (int i = 0; i < latLngPoints.size() - 1; i++) {
            locationA.setLatitude(latLngPoints.get(i).latitude);
            locationA.setLongitude(latLngPoints.get(i).longitude);
            locationB.setLatitude(latLngPoints.get(i + 1).latitude);
            locationB.setLongitude(latLngPoints.get(i + 1).longitude);

            // Calculate teh distance between the two Location objects
            float distance = locationA.distanceTo(locationB);
            totalDistance += distance;
        }
        return totalDistance;
    }

}