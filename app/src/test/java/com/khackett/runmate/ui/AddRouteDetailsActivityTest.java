//package com.khackett.runmate.ui;
//
//import android.test.ActivityInstrumentationTestCase2;
//import android.widget.TextView;
//
//import com.khackett.runmate.R;
//
//import org.junit.Before;
//import org.junit.Test;
//
//import java.util.Calendar;
//
///**
// * * Tests AddRouteDetailsActivityTest in isolation from the system.
// * Created by KHackett on 19/09/15.
// */
//public class AddRouteDetailsActivityTest extends ActivityInstrumentationTestCase2<AddRouteDetailsActivity> {
//
//    // Declare test data for Strings
//    String emptyDateTimeString, proposedDateString, proposedTimeString, routeNameValid;
//
//    // Variable for storing current date and time
//    private int mPastYear, mPastMonth, mPastDay, mPastHour, mPastMinute,
//            mFutureYear, mFutureMonth, mFutureDay, mFutureHour, mFutureMinute;
//
//    private Calendar proposedTime;
//    private Calendar proposedDate;
//
//    private TextView proposedTimeTextView;
//    private TextView proposedDateTextView;
//    private TextView proposedTimeTextViewEmpty;
//    private TextView proposedDateTextViewEmpty;
//
//    Calendar pastDateTime = Calendar.getInstance();
//    Calendar futureDateTime = Calendar.getInstance();
//
//    // Declare a AddRouteDetailsActivity object
//    AddRouteDetailsActivity mAddRouteDetailsActivity;
//
//    public AddRouteDetailsActivityTest() {
//        super(AddRouteDetailsActivity.class);
//    }
//
//    @Before
//    public void setUp() throws Exception {
//        super.setUp();
//
//        // Assign values to test data
//        mAddRouteDetailsActivity = getActivity();
//
//        proposedTimeTextView = (TextView) mAddRouteDetailsActivity.findViewById(R.id.proposeTimeButton);
//        proposedDateTextView = (TextView) mAddRouteDetailsActivity.findViewById(R.id.proposeDateButton);
//
//        mPastYear = 2010;
//        mPastMonth = 11;
//        mPastDay = 11;
//        mPastHour = 11;
//        mPastMinute = 11;
//        pastDateTime.set(mPastYear, mPastMonth, mPastDay, mPastHour, mPastMinute);
//
//        mFutureYear = futureDateTime.get(Calendar.YEAR);
//        mFutureMonth = futureDateTime.get(Calendar.MONTH);
//        mFutureDay = futureDateTime.get(Calendar.DAY_OF_MONTH);
//        mFutureHour = futureDateTime.get(Calendar.HOUR_OF_DAY);
//        mFutureMinute = futureDateTime.get(Calendar.MINUTE);
//        futureDateTime.set(mFutureYear, mFutureMonth, mFutureDay, mFutureHour, mFutureMinute);
//
//        proposedTimeTextView.setText(futureDateTime.toString());
//        proposedDateTextView.setText(futureDateTime.toString());
//
//        emptyDateTimeString = "";
//        routeNameValid = "run";
//        proposedTimeTextViewEmpty.setText(emptyDateTimeString.toString());
//        proposedDateTextViewEmpty.setText(emptyDateTimeString.toString());
//    }
//
//    /**
//     * Verifies that app and test code are configured correctly
//     */
//    @Test
//    public void testActivityExists() {
//        AddRouteDetailsActivity addRouteDetailsActivity = getActivity();
//        assertNotNull(addRouteDetailsActivity);
//    }
//
//    /**
//     * Checks for empty fields in the add route details process to return a false value
//     */
//    @Test
//    public void testCheckForEmptyFieldsAllEmpty() throws Exception {
//        mAddRouteDetailsActivity = new AddRouteDetailsActivity();
//        assertEquals(true, mAddRouteDetailsActivity.checkForEmptyFields(
//                emptyDateTimeString, proposedTimeTextViewEmpty, proposedDateTextViewEmpty));
//    }
//
//    /**
//     * Checks that all fields in the add route details process are filled and returns a value of true
//     */
//    @Test
//    public void testCheckForEmptyFieldsValid() throws Exception {
//        mAddRouteDetailsActivity = new AddRouteDetailsActivity();
//        assertEquals(true, mAddRouteDetailsActivity.checkForEmptyFields(
//                routeNameValid, proposedTimeTextView, proposedDateTextView));
//    }
//
//    @Test
//    public void testRouteTimeDateValidCheck() throws Exception {
//        mAddRouteDetailsActivity = new AddRouteDetailsActivity();
//    }
//}