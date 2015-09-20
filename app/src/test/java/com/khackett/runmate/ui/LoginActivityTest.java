package com.khackett.runmate.ui;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;

import android.widget.TextView;

import com.khackett.runmate.ui.LoginActivity;
import com.khackett.runmate.ui.SignUpActivity;

import org.junit.Test;

/**
 * Tests LoginActivity in isolation from the system.
 * Created by KHackett on 19/09/15.
 */
public class LoginActivityTest extends ActivityInstrumentationTestCase2<LoginActivity> {

    // Declare test data for Strings
    String emptyString, username, password;

    public LoginActivityTest() {
        super(LoginActivity.class);
    }

    // Declare a LoginActivity object
    LoginActivity mLoginActivity;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Assign value to String test data
        emptyString = "";
        username = "kevinH";
        password = "Password1";
    }

    /**
     * Checks for empty fields in the log in process to return a false value
     */
    @Test
    public void testCheckForEmptyFieldsAllInvalid() throws Exception {
        mLoginActivity = new LoginActivity();
        assertEquals(false, mLoginActivity.checkForEmptyFields(emptyString, emptyString));
    }

    /**
     * Checks that all fields in the log in process are filled and returns a value of true
     */
    @Test
    public void testCheckForEmptyFieldsValid() throws Exception {
        mLoginActivity = new LoginActivity();
        assertEquals(true, mLoginActivity.checkForEmptyFields(username, password));
    }

}
