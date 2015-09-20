package com.khackett.runmate;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.khackett.runmate.model.Route;
import com.khackett.runmate.ui.SignUpActivity;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.ArrayList;

/**
 * Created by KHackett on 19/09/15.
 */
public class RouteTest {

    // Declare test data for Route
    double mLatitudeMax, mLongitudeMax, mLatitudeMin, mLongitudeMin,
            mLatitudeMaxInvalid, mLatitudeMinInvalid,
            mTotalDistance1, mTotalDistance2, mTotalDistance3, distanceBetweenBrisbaneMelbourne;
    ArrayList<Double> totalDistanceArray;
    ArrayList<LatLng> markerPointsSet1, markerPointsSet2, minMaxLatLngArrayList;
    ArrayList<ArrayList<LatLng>> minMaxLatLngSectionArrayList;

    LatLng BRISBANE;
    LatLng MELBOURNE;
    LatLng SYDNEY;
    LatLng ADELAIDE;
    LatLng PERTH;

    LatLng NEWYORK;
    LatLng BOSTON;
    LatLng HOUSTON;
    LatLng LA;
    LatLng LASVEGAS;

    LatLngBounds mLatLngBoundsValid;
    LatLngBounds mLatLngBoundsInvalid;
    LatLng southWestValid;
    LatLng northEastValid;
    LatLng southWestInvalid;
    LatLng northEastInvalid;

    Route mRoute;

    @Before
    public void setUp() throws Exception {

        // assign values and instantiate test data
        mTotalDistance1 = 11.11;
        mTotalDistance2 = 22.22;
        mTotalDistance3 = 33.33;

        totalDistanceArray = new ArrayList<Double>();

        BRISBANE = new LatLng(-27.47093, 153.0235);
        MELBOURNE = new LatLng(-37.81319, 144.96298);
        SYDNEY = new LatLng(-33.87365, 151.20689);
        ADELAIDE = new LatLng(-34.92873, 138.59995);
        PERTH = new LatLng(-31.952854, 115.857342);

        markerPointsSet1 = new ArrayList<LatLng>();
        markerPointsSet1.add(BRISBANE);
        markerPointsSet1.add(MELBOURNE);
        markerPointsSet1.add(SYDNEY);
        markerPointsSet1.add(ADELAIDE);
        markerPointsSet1.add(PERTH);

        mLatitudeMax = BRISBANE.latitude;
        mLongitudeMax = BRISBANE.longitude;
        mLatitudeMin = MELBOURNE.latitude;
        mLongitudeMin = PERTH.longitude;
        southWestValid = new LatLng(mLatitudeMin, mLongitudeMin);
        northEastValid = new LatLng(mLatitudeMax, mLongitudeMax);
        mLatLngBoundsValid = new LatLngBounds(southWestValid, northEastValid);

        mLatitudeMaxInvalid = MELBOURNE.latitude;
        mLatitudeMinInvalid = BRISBANE.latitude;
        southWestInvalid = new LatLng(mLatitudeMinInvalid, mLongitudeMax);
        northEastInvalid = new LatLng(mLatitudeMaxInvalid, mLongitudeMax);
        mLatLngBoundsInvalid = new LatLngBounds(northEastInvalid, southWestInvalid);

        NEWYORK = new LatLng(40.7127, -74.0059);
        BOSTON = new LatLng(42.3601, -71.0589);
        HOUSTON = new LatLng(29.7604, -95.3698);
        LA = new LatLng(34.0500, -118.2500);
        LASVEGAS = new LatLng(36.1215, -115.1739);

        markerPointsSet2 = new ArrayList<LatLng>();
        markerPointsSet2.add(NEWYORK);
        markerPointsSet2.add(BOSTON);
        markerPointsSet2.add(HOUSTON);
        markerPointsSet2.add(LA);
        markerPointsSet2.add(LASVEGAS);

        minMaxLatLngSectionArrayList = new ArrayList<ArrayList<LatLng>>();
        minMaxLatLngArrayList = new ArrayList<LatLng>();

    }

    /**
     * Tests the get and set methods for total distance
     */
    @Test
    public void testSetAndGetTotalDistance() {
        mRoute = new Route();
        mRoute.setTotalDistance(mTotalDistance1);
        assertEquals(mTotalDistance1, mRoute.getTotalDistance(), 0.001);
    }

    /**
     * Tests the clear total distance method
     */
    @Test
    public void testClearTotalDistance() {
        mRoute = new Route();
        // Set the total distance with 2 distance values
        mRoute.setTotalDistance(mTotalDistance1);
        mRoute.setTotalDistance(mTotalDistance2);
        // Clear the total distance array
        mRoute.clearTotalDistance();

        assertEquals(0, mRoute.getTotalDistance(), 0.001);
    }

    /**
     * Tests the undoLastRouteDistance method with 3 values
     */
    @Test
    public void testUndoLastRouteDistanceThreeValues() {
        mRoute = new Route();
        // Set the total distance with 3 distance values
        mRoute.setTotalDistance(mTotalDistance1);
        mRoute.setTotalDistance(mTotalDistance2);
        mRoute.setTotalDistance(mTotalDistance3);
        // Undo the last route added
        mRoute.undoLastRouteDistance();

        assertEquals((mTotalDistance1 + mTotalDistance2), mRoute.getTotalDistance(), 0.001);
    }

    /**
     * Tests the undoLastRouteDistance method with 3 values
     */
    @Test
    public void testUndoLastRouteDistanceToZero() {
        mRoute = new Route();
        // Set the total distance with 1 distance value
        mRoute.setTotalDistance(mTotalDistance1);
        // Undo the last route added
        mRoute.undoLastRouteDistance();
        assertEquals(0, mRoute.getTotalDistance(), 0.001);
    }

    /**
     * Tests the get and set methods for markerPoints
     */
    @Test
    public void testSetAndGetMarkerPoints() {
        mRoute = new Route();
        mRoute.setMarkerPoint(BRISBANE);
        assertEquals(BRISBANE, mRoute.getMarkerPoints().get(0));
    }

    /**
     * Tests the get and set methods for markerPoints with an invalid value
     */
    @Test(expected = AssertionError.class)
    public void testSetAndGetMarkerPointsInvalid() {
        mRoute = new Route();
        mRoute.setMarkerPoint(BRISBANE);
        assertEquals(MELBOURNE, mRoute.getMarkerPoints().get(0));
    }

    /**
     * Tests the getMarkerPoint1 method
     */
    @Test
    public void testGetMarkerPoint1() {
        mRoute = new Route();
        mRoute.setMarkerPoint(BRISBANE);
        mRoute.setMarkerPoint(MELBOURNE);
        mRoute.setMarkerPoint(SYDNEY);
        mRoute.setMarkerPoint(ADELAIDE);
        mRoute.setMarkerPoint(PERTH);

        assertEquals(ADELAIDE, mRoute.getMarkerPoint1());
    }

    /**
     * Tests the getMarkerPoint1 method with an invalid value
     */
    @Test(expected = AssertionError.class)
    public void testGetMarkerPoint1Invalid() {
        mRoute = new Route();
        mRoute.setMarkerPoint(BRISBANE);
        mRoute.setMarkerPoint(MELBOURNE);
        mRoute.setMarkerPoint(SYDNEY);
        mRoute.setMarkerPoint(ADELAIDE);
        mRoute.setMarkerPoint(PERTH);

        assertEquals(BRISBANE, mRoute.getMarkerPoint1());
    }

    /**
     * Tests the getMarkerPoint2 method
     */
    @Test
    public void testGetMarkerPoint2() {
        mRoute = new Route();
        mRoute.setMarkerPoint(BRISBANE);
        mRoute.setMarkerPoint(MELBOURNE);
        mRoute.setMarkerPoint(SYDNEY);
        mRoute.setMarkerPoint(ADELAIDE);
        mRoute.setMarkerPoint(PERTH);

        assertEquals(PERTH, mRoute.getMarkerPoint2());
    }

    /**
     * Tests the getMarkerPoint2 method with an invalid value
     */
    @Test(expected = AssertionError.class)
    public void testGetMarkerPoint2Invalid() {
        mRoute = new Route();
        mRoute.setMarkerPoint(BRISBANE);
        mRoute.setMarkerPoint(MELBOURNE);
        mRoute.setMarkerPoint(SYDNEY);
        mRoute.setMarkerPoint(ADELAIDE);
        mRoute.setMarkerPoint(PERTH);

        assertEquals(BRISBANE, mRoute.getMarkerPoint2());
    }

    /**
     * Tests the set and get for getMinMaxLatLngArrayList method
     */
    @Test
    public void testGetMinMaxLatLngArrayList() {
        mRoute = new Route();
        mRoute.setMinMaxLatLngSectionArrayList(markerPointsSet1);
        mRoute.setMinMaxLatLngSectionArrayList(markerPointsSet2);

        minMaxLatLngSectionArrayList.add(markerPointsSet1);
        minMaxLatLngSectionArrayList.add(markerPointsSet2);

        ArrayList<LatLng> latLngList = new ArrayList<LatLng>();
        latLngList.add(BRISBANE);
        latLngList.add(MELBOURNE);
        latLngList.add(SYDNEY);
        latLngList.add(ADELAIDE);
        latLngList.add(PERTH);
        latLngList.add(NEWYORK);
        latLngList.add(BOSTON);
        latLngList.add(HOUSTON);
        latLngList.add(LA);
        latLngList.add(LASVEGAS);

        assertEquals(latLngList, mRoute.getMinMaxLatLngArrayList());
    }

    /**
     * Tests the set and get for MinMaxLatLngSectionArrayList method with one array
     */
    @Test
    public void testSetAndGetMinMaxLatLngSectionOneArray() {
        mRoute = new Route();
        mRoute.setMinMaxLatLngSectionArrayList(markerPointsSet1);

        minMaxLatLngSectionArrayList.add(markerPointsSet1);

        assertEquals(minMaxLatLngSectionArrayList, mRoute.getMinMaxLatLngSectionArrayList());
    }

    /**
     * Tests the set and get for MinMaxLatLngSectionArrayList method with two arrays
     */
    @Test
    public void testSetAndGetMinMaxLatLngSectionTwoArrays() {
        mRoute = new Route();
        mRoute.setMinMaxLatLngSectionArrayList(markerPointsSet1);
        mRoute.setMinMaxLatLngSectionArrayList(markerPointsSet2);

        minMaxLatLngSectionArrayList.add(markerPointsSet1);
        minMaxLatLngSectionArrayList.add(markerPointsSet2);

        assertEquals(minMaxLatLngSectionArrayList, mRoute.getMinMaxLatLngSectionArrayList());
    }

    /**
     * Tests the undoLastMinMaxLatLng method
     */
    @Test
    public void testUndoLastMinMaxLatLng() {
        mRoute = new Route();
        mRoute.setMinMaxLatLngSectionArrayList(markerPointsSet1);
        mRoute.setMinMaxLatLngSectionArrayList(markerPointsSet2);

        minMaxLatLngSectionArrayList.add(markerPointsSet1);

        mRoute.undoLastMinMaxLatLng();

        assertEquals(minMaxLatLngSectionArrayList, mRoute.getMinMaxLatLngSectionArrayList());
    }

    /**
     * Tests the getLongitudeMax method
     */
    @Test
    public void testGetLatitudeMax() {
        mRoute = new Route();
        mRoute.setMinMaxLatLngSectionArrayList(markerPointsSet1);

        assertEquals(mLatitudeMax, mRoute.getLatitudeMax(), 0.001);
    }

    /**
     * Tests the getLongitudeMax method
     */
    @Test
    public void testGetLongitudeMax() {
        mRoute = new Route();
        mRoute.setMinMaxLatLngSectionArrayList(markerPointsSet1);

        assertEquals(mLongitudeMax, mRoute.getLongitudeMax(), 0.001);
    }

    /**
     * Tests the getLatitudeMin method
     */
    @Test
    public void testGetLatitudeMin() {
        mRoute = new Route();
        mRoute.setMinMaxLatLngSectionArrayList(markerPointsSet1);

        assertEquals(mLatitudeMin, mRoute.getLatitudeMin(), 0.001);
    }

    /**
     * Tests the getLongitudeMin method
     */
    @Test
    public void testGetLongitudeMin() {
        mRoute = new Route();
        mRoute.setMinMaxLatLngSectionArrayList(markerPointsSet1);

        assertEquals(mLongitudeMin, mRoute.getLongitudeMin(), 0.001);
    }


    /**
     * Tests the getLatLngBounds method with valid values
     */
    @Test
    public void testGetLatLngBoundsValid() {
        mRoute = new Route();
        mRoute.setMinMaxLatLngSectionArrayList(markerPointsSet1);

        assertEquals(mLatLngBoundsValid, mRoute.getLatLngBounds());
    }

    /**
     * Tests the getLatLngBounds method with invalid values
     */
    @Test(expected = AssertionError.class)
    public void testGetLatLngBoundsInvalid() {
        mRoute = new Route();
        mRoute.setMinMaxLatLngSectionArrayList(markerPointsSet1);

        assertEquals(mLatLngBoundsInvalid, mRoute.getLatLngBounds());
    }

}
