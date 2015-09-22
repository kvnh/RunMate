package com.khackett.runmate.utils;

/**
 * Class to hold key names used in the Parse backend.
 * Two different types of data will be held in this class - class names and field names
 */
public final class ParseConstants {

    /**
     * Classes that are to be stored in the Parse cloud
     * - naming convention is for classes to begin with a capital letter
     */
    public static final String CLASS_ROUTES = "Routes";
    public static final String CLASS_COMPLETED_RUNS = "CompletedRuns";

    /**
     * Field names - Prefix KEY to the constant name.  Fields to begin with lowercase.
     */
    public static final String KEY_USERNAME = "username";
    public static final String KEY_FRIENDS_RELATION = "friendsRelation";
    public static final String KEY_RECIPIENT_IDS = "recipientIds";
    public static final String KEY_USER_FULL_NAME = "fullName";
    public static final String KEY_ACCEPTED_RECIPIENT_IDS = "acceptedRecipientIds";
    public static final String KEY_SENDER_IDS = "senderId";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEY_RUNNER_IDS = "runnerId";
    public static final String KEY_RUNNER_NAME = "runnerName";
    public static final String KEY_CREATED_AT = "createdAt";
    public static final String TYPE_IMAGE = "image";
    public static final String KEY_LATLNG_GPS_POINTS = "latLngGPSPoints";
    public static final String KEY_LATLNG_POINTS = "latLngPoints";
    public static final String KEY_LATLNG_BOUNDARY_POINTS = "latLngBoundaryPoints";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_ROUTE_DISTANCE = "routeDistance";
    public static final String KEY_COMPLETED_RUN_DISTANCE = "completedRunDistance";
    public static final String KEY_ROUTE_NAME = "routeName";
    public static final String KEY_ROUTE_PROPOSED_TIME = "proposedTime";
    public static final String KEY_RUN_TIME = "completedTime";
    public static final String KEY_ALL_LATLNG_POINTS = "allLatLngPoints";
    public static final String KEY_ORIGINAL_ROUTE_ID = "originalRouteId";
    public static final String KEY_ROUTE_CREATION_TYPE = "creationType";
    public static final String KEY_PROFILE_PICTURE = "profilePic";

}