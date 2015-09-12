package com.khackett.runmate.utils;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by KHackett on 29/07/15.
 */
public class DirectionsUtility {

    /**
     * Creates a url containing the origin and destination points and other parameters.
     * These are then sent as a HTTP request to the Google Directions API to create data in JSON format.
     *
     * @param origin
     * @param dest
     * @return
     */
    public String getDirectionsUrl(LatLng origin, LatLng dest) {
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
        // See https://developers.google.com/maps/documentation/directions/#DirectionsRequests
        // eg. https://maps.googleapis.com/maps/api/directions/json?origin=40.722543,-73.998585&destination=40.7577,-73.9857&mode=walking
        // ... would give the points between lower_manhattan and times_square and the directions in between in JSON format
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + transMode;

        return url;
    }

    /**
     * Receives a JSONObject and returns a list of lists containing latitude and longitude values.
     */
    public List<List<HashMap<String, String>>> parseJSONObject(JSONObject jObject) {

        List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String, String>>>();
        JSONArray jsonRoutes = null;
        JSONArray jsonLegs = null;
        JSONArray jsonSteps = null;

        try {

            jsonRoutes = jObject.getJSONArray("routes");

            // Traversing through all routes
            for (int i = 0; i < jsonRoutes.length(); i++) {
                jsonLegs = ((JSONObject) jsonRoutes.get(i)).getJSONArray("legs");
                List path = new ArrayList<HashMap<String, String>>();

                // Traversing through all legs
                for (int j = 0; j < jsonLegs.length(); j++) {
                    jsonSteps = ((JSONObject) jsonLegs.get(j)).getJSONArray("steps");

                    // Traversing through all steps
                    for (int k = 0; k < jsonSteps.length(); k++) {
                        String polyline = "";
                        polyline = (String) ((JSONObject) ((JSONObject) jsonSteps.get(k)).get("polyline")).get("points");
                        List<LatLng> list = decodePoly(polyline);

                        // Traversing through all points
                        for (int l = 0; l < list.size(); l++) {
                            HashMap<String, String> hashMap = new HashMap<String, String>();
                            hashMap.put("lat", Double.toString(((LatLng) list.get(l)).latitude));
                            hashMap.put("lng", Double.toString(((LatLng) list.get(l)).longitude));
                            path.add(hashMap);
                        }
                    }
                    routes.add(path);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }
        return routes;
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
