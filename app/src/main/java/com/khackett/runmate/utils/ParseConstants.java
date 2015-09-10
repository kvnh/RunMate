package com.khackett.runmate.utils;

/**
 * Created by KHackett on 24/07/15.
 * This class will hold two different types of data - class names and field names
 */
public final class ParseConstants {

    /**
     * Classes that are to be stored in the Parse cloud
     * - naming convention is for classes to begin with a capital letter
     */
    public static final String CLASS_ROUTES = "Routes";

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
    public static final String KEY_CREATED_AT = "createdAt";
    public static final String TYPE_IMAGE = "image";
    public static final String KEY_LATLNG_POINTS = "latLngPoints";
    public static final String KEY_LATLNG_BOUNDARY_POINTS = "latLngBoundaryPoints";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_ROUTE_DISTANCE = "routeDistance";
    public static final String KEY_ROUTE_NAME = "routeName";
    public static final String KEY_ROUTE_PROPOSED_TIME = "proposedTime";

}
