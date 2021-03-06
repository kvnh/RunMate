package com.khackett.runmate.utils;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

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

/**
 * Class to handle requests and data related to interaction with the Google Maps Directions API
 */
public class DirectionsUtility {

    /**
     * Method to create a url containing the origin and destination points and other parameters.
     * These are then sent as a HTTP request to the Google Directions API to create route data in JSON format.
     *
     * @param origin - the origin of the route
     * @param dest   - the destination of the route
     * @return
     */
    public String getDirectionsUrl(LatLng origin, LatLng dest) {
        // Origin of route
        String stringOrigin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String stringDestination = "destination=" + dest.latitude + "," + dest.longitude;
        // Output format in JSON
        String output = "json";
        // Transport mode walking to replicate a pedestrian/runner
        String transMode = "&mode=walking";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + stringOrigin + "&" + stringDestination + transMode;

        return url;
    }

    /**
     * Method that when given a URL, a HttpUrlConnection is established and
     * web page content is retrieved as an InputStream and returned as a String.
     */
    public String downloadUrl(String stringUrl) throws IOException {
        String data = "";
        HttpURLConnection httpURLConnection = null;
        InputStream inputStream = null;
        try {
            URL url = new URL(stringUrl);
            // Creating an http connection to communicate with the url
            httpURLConnection = (HttpURLConnection) url.openConnection();
            // Connecting to the url
            httpURLConnection.connect();
            // Reading data from the url
            inputStream = httpURLConnection.getInputStream();
            // Wrap the inputStream object and buffer the input
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            // Instantiate a StringBuilder object to convert a given datum and append the characters of it to the object
            StringBuilder stringBuilder = new StringBuilder();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                // Append each character of the JSON format file to the StringBuilder
                stringBuilder.append(line);
            }
            data = stringBuilder.toString();
            // Close the BufferedReader
            bufferedReader.close();
        } catch (Exception e) {
            Log.d("Problem downloading url", e.toString());
        } finally {
            // Close the InputStream and disconnect the HttpURLConnection
            inputStream.close();
            httpURLConnection.disconnect();
        }
        return data;
    }

    /**
     * Method that receives a JSONObject and returns a list of LatLng points.
     *
     * @param jsonObject - the JSONObject to parse the LatLng values from
     * @return a List of LatLng values
     */
    public List<LatLng> parseJSONObjectOverviewPolyline(JSONObject jsonObject) {

        // Create a List to hold all LatLng points and initialise to null
        List<LatLng> routePoints = null;

        try {
            // Get the 'routes' data from the JSON object
            JSONObject jsonRouteObject = jsonObject.getJSONArray("routes").getJSONObject(0);
            // Get the single points object from 'overview_polyline'
            JSONObject overviewPolyline = jsonRouteObject.getJSONObject("overview_polyline");
            String points = overviewPolyline.getString("points");
            // Decode the polyline to get a List of LatLng values
            routePoints = decodePoly(points);

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }
        return routePoints;
    }

    /**
     * Method to decode polyline points
     * Courtesy : http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     */
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)), (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }
}