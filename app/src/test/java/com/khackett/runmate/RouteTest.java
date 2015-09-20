package com.khackett.runmate;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Created by KHackett on 19/09/15.
 */
public class RouteTest {

    // Declare test data for Route
    double mLatitudeMax, mLongitudeMax, mLatitudeMin, mLongitudeMin;
    ArrayList<Double> totalDistanceArray;
    ArrayList<LatLng> markerPoints, minMaxLatLngArrayList;
    ArrayList<ArrayList<LatLng>> minMaxLatLngSectionArrayList;
    ArrayList<Polyline> polylines;

    @Before
    public void setUp() throws Exception {

        // assign values to test data
        mLatitudeMax = 1;


    }


}
